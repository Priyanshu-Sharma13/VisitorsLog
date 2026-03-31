package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.Society
import com.example.visitorslogs.domain.repository.SocietyRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.visitorslogs.domain.model.UserProfile
import com.example.visitorslogs.domain.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SocietyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SocietyRepository {

    private val societiesCollection = firestore.collection("societies")

    override suspend fun addSociety(name: String, address: String, adminEmail: String, adminPassword: String): Resource<String> {
        return try {
            val mainApp = FirebaseApp.getInstance()
            val options = mainApp.options
            
            val secondaryApp = try {
                FirebaseApp.getInstance("SecondaryApp")
            } catch (e: IllegalStateException) {
                FirebaseApp.initializeApp(mainApp.applicationContext, options, "SecondaryApp")
            }
            
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)
            
            val authResult = secondaryAuth.createUserWithEmailAndPassword(adminEmail, adminPassword).await()
            val adminUid = authResult.user?.uid ?: throw Exception("Failed to get Admin UID")
            
            val docRef = societiesCollection.document()
            
            val society = Society(
                societyId = docRef.id,
                name = name,
                address = address,
                createdAtMillis = System.currentTimeMillis(),
                adminEmail = adminEmail,
                adminPassword = adminPassword,
                adminUserId = adminUid
            )
            
            val adminProfile = UserProfile(
                userId = adminUid,
                name = "$name Admin",
                phoneNumber = "",
                role = UserRole.ADMIN,
                societyId = docRef.id
            )
            
            firestore.collection("users").document(adminUid).set(adminProfile).await()
            docRef.set(society).await()
            
            secondaryAuth.signOut()
            
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add society")
        }
    }

    override suspend fun updateSociety(societyId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            societiesCollection.document(societyId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update society")
        }
    }

    override suspend fun removeSociety(societyId: String): Resource<Unit> {
        return try {
            societiesCollection.document(societyId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove society")
        }
    }

    override fun getAllSocieties(): Flow<List<Society>> = callbackFlow {
        val listener = societiesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Society::class.java).sortedByDescending { it.createdAtMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
}
