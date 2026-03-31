package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole
import com.example.visitorslogs.domain.repository.AdminUserRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminUserRepository {

    // We use a secondary Firebase implementation to create users without logging out the current Admin
    // For simplicity without setting up a secondary app, we can use Cloud Functions in a real app.
    // However, since we're strictly on Android client:
    
    private val usersCollection = firestore.collection("users")
    
    override suspend fun registerUser(
        userProfile: UserProfile,
        emailOrUsername: String,
        password: String
    ): Resource<String> {
        return try {
            // Generating valid email for guards if they are passed as usernames
            val email = if (userProfile.role == UserRole.GUARD && !emailOrUsername.contains("@")) {
                "${emailOrUsername}@visitorslogs.com"
            } else {
                emailOrUsername
            }
            
            // To prevent the Admin from being logged out when creating a user, we temporarily cache their current uid
            // Note: Creating a user on the client SDK inherently logs that new user in. 
            // In a production app, this should be done via a Firebase Cloud Function.
            // For MVP purposes, we will rely on creating it and then forcing the Admin to log back in, OR 
            // relying on the manual auth process for now. Let's do a simple basic creation.
            
            val uid = try {
                val mainApp = FirebaseApp.getInstance()
                val secondaryApp = try {
                    FirebaseApp.getInstance("SecondaryAppUser")
                } catch (e: IllegalStateException) {
                    FirebaseApp.initializeApp(mainApp.applicationContext, mainApp.options, "SecondaryAppUser")
                }
                
                val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
                val result = secondaryAuth.createUserWithEmailAndPassword(email, password).await()
                
                val newUid = result.user?.uid ?: throw Exception("Failed to get UID for new user")
                secondaryAuth.signOut()
                newUid
            } catch (e: Exception) {
                if (e is FirebaseAuthUserCollisionException || e.message?.contains("already in use", ignoreCase = true) == true) {
                    try {
                        val secondaryApp = FirebaseApp.getInstance("SecondaryAppUser")
                        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
                        val signinResult = secondaryAuth.signInWithEmailAndPassword(email, password).await()
                        val existingUid = signinResult.user?.uid ?: throw e
                        secondaryAuth.signOut()
                        existingUid
                    } catch (signInError: Exception) {
                        return Resource.Error("Account exists but credentials mismatched. Cannot restore.")
                    }
                } else {
                    return Resource.Error(e.localizedMessage ?: "Failed to register user")
                }
            }

            val profileToSave = userProfile.copy(userId = uid)
            
            firestore.collection("users").document(uid).set(profileToSave).await()
            
            Resource.Success("User registered successfully. The new user is now active.")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to register user")
        }
    }

    override fun getSocietyUsers(societyId: String): kotlinx.coroutines.flow.Flow<List<UserProfile>> = kotlinx.coroutines.flow.callbackFlow {
        val listener = usersCollection
            .whereEqualTo("societyId", societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(UserProfile::class.java)
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteUser(userId: String): Resource<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete user")
        }
    }

    override suspend fun updateUser(userId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user")
        }
    }
}
