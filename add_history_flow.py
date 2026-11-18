import re

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'r') as f:
    content = f.read()

new_flow = """
    open val recentRevisionHistory: Flow<List<RevisionHistoryRecord>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("users").document(uid).collection("revision_history")
                .orderBy("completedDateMs", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val records = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(RevisionHistoryRecord::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } ?: emptyList()
                    trySend(records)
                }
            awaitClose { listener.remove() }
        }
    }
"""

# insert before `open val allSubjects`
content = content.replace('open val allSubjects: Flow<List<Subject>>', new_flow + '\n    open val allSubjects: Flow<List<Subject>>')

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'w') as f:
    f.write(content)
