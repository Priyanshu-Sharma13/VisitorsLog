package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole
import com.example.visitorslogs.domain.repository.AuthRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUser(): Flow<UserProfile?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
            } else {
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { snapshot ->
                        val userProfile = snapshot.toObject(UserProfile::class.java)
                        trySend(userProfile)
                    }
                    .addOnFailureListener {
                        trySend(null)
                    }
            }
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    override suspend fun loginAsSuperAdmin(
        email: String,
        password: String
    ): Resource<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            
            if (userProfile?.role == UserRole.SUPER_ADMIN) {
                Resource.Success(userProfile)
            } else {
                auth.signOut()
                Resource.Error("User is not an App Owner")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Invalid super admin credentials")
        }
    }

    override suspend fun loginAsAdmin(
        email: String,
        password: String,
        societyId: String
    ): Resource<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            
            if (userProfile?.role == UserRole.ADMIN && userProfile.societyId == societyId) {
                Resource.Success(userProfile)
            } else {
                auth.signOut()
                Resource.Error("User is not an Admin for this society")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred during login")
        }
    }

    override suspend fun loginAsGuard(
        username: String,
        password: String,
        societyId: String
    ): Resource<UserProfile> {
        // Guards login with a username (we can map username to a generated email behind the scenes e.g., 'username@society.com' or query firestore)
        // For simplicity we will assume guards are registered with an email like username@visitorslogs.com
        val guardEmail = "$username@visitorslogs.com"
        return try {
            val result = auth.signInWithEmailAndPassword(guardEmail, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            
            if (userProfile?.role == UserRole.GUARD && userProfile.societyId == societyId) {
                Resource.Success(userProfile)
            } else {
                auth.signOut()
                Resource.Error("User is not a Guard for this society")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Invalid guard credentials")
        }
    }

    override suspend fun loginAsResident(
        email: String,
        password: String,
        societyId: String
    ): Resource<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            
            val snapshot = firestore.collection("users").document(uid).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            
            if (userProfile?.role == UserRole.RESIDENT && userProfile.societyId == societyId) {
                Resource.Success(userProfile)
            } else {
                auth.signOut()
                Resource.Error("User is not a Resident for this society")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred during login")
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
