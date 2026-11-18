const { calculateNextRevisionDate } = require('./srsEngine');

/**
 * Synchronizes study data for a chapter to Firestore, ensuring a single source of truth.
 *
 * @param {Object} db - The Firestore database instance (e.g., from firebase-admin or firebase/firestore).
 * @param {string} userId - The ID of the user.
 * @param {string} chapterId - The ID of the chapter being studied.
 * @param {Object} sessionData - Details about the study session.
 * @param {number} sessionData.durationMs - The duration of the session in milliseconds.
 * @param {boolean} sessionData.isRevision - Whether this was a revision or initial study.
 * @param {number[]} sessionData.intervals - The user's custom SRS intervals array.
 * @returns {Promise<void>}
 */
async function syncStudyData(db, userId, chapterId, sessionData) {
    const chapterRef = db.collection('users').doc(userId).collection('chapters').doc(chapterId);
    const historyRef = chapterRef.collection('studyHistory').doc();
    
    try {
        await db.runTransaction(async (transaction) => {
            const chapterDoc = await transaction.get(chapterRef);
            
            if (!chapterDoc.exists) {
                throw new Error("Chapter does not exist!");
            }
            
            const chapterData = chapterDoc.data();
            
            // Extract current data or provide defaults
            const currentRevisionCount = chapterData.revisionCount || 0;
            const currentTotalStudyTimeMs = chapterData.totalStudyTimeMs || 0;
            
            // Determine if we should increment the revision count
            const newRevisionCount = sessionData.isRevision ? currentRevisionCount + 1 : currentRevisionCount;
            
            // Calculate next revision date based on the *new* revision count
            const now = new Date();
            const nextRevisionDate = calculateNextRevisionDate(now, sessionData.intervals, newRevisionCount);
            
            // Prepare updates for the Chapter Document
            const chapterUpdates = {
                totalStudyTimeMs: currentTotalStudyTimeMs + sessionData.durationMs,
                lastRevisionDate: now.toISOString(),
                nextRevisionDate: nextRevisionDate ? nextRevisionDate.toISOString() : null,
                revisionCount: newRevisionCount,
                updatedAt: now.toISOString(),
            };
            
            // Update first study date if this is the very first time studying
            if (!sessionData.isRevision && !chapterData.firstStudyDate) {
                chapterUpdates.firstStudyDate = now.toISOString();
            }
            
            // 1. Update the chapter data
            transaction.update(chapterRef, chapterUpdates);
            
            // 2. Record the entry in the studyHistory sub-collection
            const historyEntry = {
                id: historyRef.id,
                chapterId: chapterId,
                durationMs: sessionData.durationMs,
                isRevision: sessionData.isRevision,
                timestamp: now.toISOString(),
                previousRevisionCount: currentRevisionCount,
                newRevisionCount: newRevisionCount,
                nextRevisionDate: nextRevisionDate ? nextRevisionDate.toISOString() : null
            };
            
            transaction.set(historyRef, historyEntry);
        });
        
        console.log(`Successfully synced study data for chapter: ${chapterId}`);
    } catch (error) {
        console.error(`Failed to sync study data for chapter ${chapterId}:`, error);
        throw error; // Re-throw to be handled by the caller
    }
}

module.exports = {
    syncStudyData
};
