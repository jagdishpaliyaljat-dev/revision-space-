import re

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'r') as f:
    content = f.read()

old_broad = """    fun getBroadcastAnalytics(): Flow<List<NotificationBroadcast>> = callbackFlow {
        val listener = db.collection("broadcast_analytics")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_broad = """    fun getBroadcastAnalytics(): Flow<List<NotificationBroadcast>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("broadcast_analytics")
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_broad in content:
    content = content.replace(old_broad, new_broad)
    content = content.replace('        awaitClose { listener.remove() }\n    }\n\n    suspend fun markBroadcastAsOpened', '        awaitClose { listener.remove() }\n        }\n    }\n\n    suspend fun markBroadcastAsOpened', 1)

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'w') as f:
    f.write(content)
