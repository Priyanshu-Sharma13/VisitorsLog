package com.example.visitorslogs.data.repository

import com.example.visitorslogs.domain.model.Notice
import com.example.visitorslogs.domain.repository.NoticeRepository
import com.example.visitorslogs.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NoticeRepository {

    private val noticesCollection = firestore.collection("notices")

    override suspend fun postNotice(notice: Notice): Resource<String> {
        return try {
            val docRef = noticesCollection.document()
            val noticeWithId = notice.copy(noticeId = docRef.id, dateMillis = System.currentTimeMillis())
            docRef.set(noticeWithId).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to post notice")
        }
    }

    override fun getNotices(societyId: String): Flow<List<Notice>> = callbackFlow {
        val listener = noticesCollection
            .whereEqualTo("societyId", societyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Notice::class.java).sortedByDescending { it.dateMillis }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteNotice(noticeId: String): Resource<Unit> {
        return try {
            noticesCollection.document(noticeId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete notice")
        }
    }

    override suspend fun updateNotice(noticeId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            noticesCollection.document(noticeId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update notice")
        }
    }

    override suspend fun voteOnPoll(noticeId: String, option: String, userId: String): Resource<Unit> {
        return try {
            val docRef = noticesCollection.document(noticeId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val notice = snapshot.toObject(Notice::class.java)
                    ?: throw Exception("Notice not found")
                
                if (!notice.isPoll || !notice.isPollActive) {
                    throw Exception("Poll is not active.")
                }
                if (notice.votedUserIds.contains(userId)) {
                    throw Exception("You have already voted.")
                }
                
                val currentVotes = notice.pollVotes.toMutableMap()
                val count = currentVotes[option] ?: 0
                currentVotes[option] = count + 1
                
                val updatedVoters = notice.votedUserIds + userId
                
                transaction.update(docRef, mapOf(
                    "pollVotes" to currentVotes,
                    "votedUserIds" to updatedVoters
                ))
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to vote")
        }
    }
}
