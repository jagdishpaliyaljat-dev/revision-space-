    suspend fun markRevisionComplete(revisionId: String) {
        if (userId.isEmpty()) return
        val ref = db.collection("users").document(userId).collection("revisions").document(revisionId)
        val snapshot = ref.get().await()
        val rev = snapshot.toObject(Revision::class.java) ?: return
        
        val newCount = rev.revisionCount + 1
        val nextIntervalIndex = (newCount - 1).coerceAtMost(rev.pattern.lastIndex)
        val daysMulti = rev.pattern.getOrElse(nextIntervalIndex) { 1 }
        val now = System.currentTimeMillis()
        val nextDate = now + (daysMulti * 86400000L)
        
        // 1. Update the EXISTING revision document (NO DUPLICATES)
        val updated = rev.copy(
            revisionCount = newCount,
            lastRevisionDateMs = now,
            nextRevisionDateMs = nextDate,
            status = "Pending" // Still pending for its next schedule
        )
        ref.set(updated).await()
        
        // 2. Also update topic summary (read-only)
        if (rev.topicId.isNotEmpty()) {
