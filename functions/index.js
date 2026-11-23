const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendAdminNotification = functions.firestore
    .document('broadcast_requests/{docId}')
    .onCreate(async (snap, context) => {
        const payload = snap.data();
        const { title, message, audience, adminEmail, requesterUid, categoryData } = payload;
        
        let requesterId = requesterUid || payload.adminId;
        
        if (!requesterId) {
            console.error("Missing requester ID.");
            return null;
        }
        
        // Verifies the requester's admin role
        const userDoc = await admin.firestore().collection('users').doc(requesterId).get();
        if (!userDoc.exists || userDoc.data().role !== 'Admin') {
            console.error("Unauthorized: only admins can send notifications.");
            return null;
        }
        
        // validates the target category (all, specific, or exam-wise)
        if (!['All Users', 'Specific Users', 'Selected Users', 'Exam-wise Users', 'Active Users', 'Inactive Users'].includes(audience)) {
             console.error("Invalid target category.");
             return null;
        }
        
        let targetTokens = [];
        let targets = [];
        
        try {
            const usersSnap = await admin.firestore().collection('users').get();
            usersSnap.forEach(doc => {
                const user = doc.data();
                
                let shouldAdd = false;
                if (audience === 'All Users') {
                    shouldAdd = true;
                } else if (audience === 'Active Users') {
                    const lastActiveMs = user.lastActiveTimeMs || 0;
                    if (!user.isBanned && (Date.now() - lastActiveMs < 7 * 24 * 60 * 60 * 1000)) {
                        shouldAdd = true;
                    }
                } else if (audience === 'Inactive Users') {
                    const lastActiveMs = user.lastActiveTimeMs || 0;
                    if (!user.isBanned && (Date.now() - lastActiveMs >= 7 * 24 * 60 * 60 * 1000)) {
                        shouldAdd = true;
                    }
                } else if (audience === 'Selected Users' || audience === 'Specific Users') {
                     shouldAdd = categoryData?.userIds?.includes(doc.id) || false;
                } else if (audience === 'Exam-wise Users') {
                     shouldAdd = categoryData?.examId === user.examId; // Assuming user has examId
                } else {
                     shouldAdd = true;
                }
                
                if (shouldAdd) {
                    targets.push(doc.id);
                    if (user.fcmToken) {
                        targetTokens.push(user.fcmToken);
                    }
                }
            });
            
            const broadcastId = context.params.docId;
            let deliveredCount = 0;
            let failedCount = 0;
            
            // 1. Add to user notification list in batches
            const batchArrays = [admin.firestore().batch()];
            let operationCounter = 0;
            let batchIndex = 0;
            
            for (const targetUid of targets) {
                const notifRef = admin.firestore().collection('users').doc(targetUid).collection('notifications').doc();
                batchArrays[batchIndex].set(notifRef, {
                    id: notifRef.id,
                    topicId: broadcastId,
                    title: title,
                    message: message,
                    timestampMs: Date.now(),
                    isRead: false,
                    type: "BROADCAST"
                });
                operationCounter++;
                
                if (operationCounter === 450) {
                    batchArrays.push(admin.firestore().batch());
                    batchIndex++;
                    operationCounter = 0;
                }
            }
            for (const b of batchArrays) {
                await b.commit();
            }
            
            // 2. Send FCM
            if (targetTokens.length > 0) {
                const sendPromises = [];
                for (let i = 0; i < targetTokens.length; i += 500) {
                    const tokensChunk = targetTokens.slice(i, i+500);
                    const pushMessage = {
                        notification: {
                            title: title,
                            body: message
                        },
                        data: {
                            topicId: broadcastId,
                            title: title,
                            message: message,
                            type: "BROADCAST",
                            timestampMs: Date.now().toString()
                        },
                        tokens: tokensChunk
                    };
                    
                    const p = admin.messaging().sendEachForMulticast(pushMessage).then(async (response) => {
                        deliveredCount += response.successCount;
                        failedCount += response.failureCount;
                        if (response.failureCount > 0) {
                            const failedTokensToRemove = [];
                            response.responses.forEach((resp, idx) => {
                                if (!resp.success) {
                                    const errCode = resp.error?.code;
                                    if (errCode === 'messaging/invalid-registration-token' ||
                                        errCode === 'messaging/registration-token-not-registered') {
                                        failedTokensToRemove.push(tokensChunk[idx]);
                                    }
                                }
                            });
                            if (failedTokensToRemove.length > 0) {
                                const cleanupPromises = failedTokensToRemove.map(async (tok) => {
                                    // Query users with this token and remove token
                                    const userDocs = await admin.firestore().collection('users').where('fcmToken', '==', tok).get();
                                    const batch = admin.firestore().batch();
                                    userDocs.forEach(d => batch.update(d.ref, { fcmToken: admin.firestore.FieldValue.delete() }));
                                    return batch.commit();
                                });
                                await Promise.all(cleanupPromises);
                            }
                        }
                    });
                    sendPromises.push(p);
                }
                
                await Promise.all(sendPromises);
            }
            
            // 3. Create Broadcast Analytics Doc Update Analytics
            const broadcastAnalyticInfo = {
                id: broadcastId,
                title: title,
                message: message,
                audience: audience,
                timestampMs: Date.now(),
                targetCount: targets.length,
                deliveredCount: deliveredCount,
                openedCount: 0,
                failedCount: failedCount
            };
            await admin.firestore().collection('broadcast_analytics').doc(broadcastId).set(broadcastAnalyticInfo);
            
            // 4. Log to notificationLogs per user request
            await admin.firestore().collection("notificationLogs").add({
                action: "Admin triggered notification",
                adminEmail: adminEmail || userDoc.data().email,
                adminId: requesterId,
                status: failedCount === 0 && deliveredCount > 0 ? "Success" : (deliveredCount > 0 ? "Partial Success" : "Failure"),
                details: `Title: ${title} | To: ${audience} | Msg: ${message}`,
                timestampMs: Date.now(),
                timestamp: admin.firestore.FieldValue.serverTimestamp(),
                results: {
                    targetCount: targets.length,
                    success: deliveredCount,
                    failure: failedCount
                }
            });
            
            // Also log in admin_logs to maintain back-compat
            await admin.firestore().collection("admin_logs").add({
                action: "Broadcast Notification",
                adminEmail: adminEmail || userDoc.data().email,
                adminId: requesterId,
                details: `Title: ${title} | To: ${audience} | Msg: ${message}`,
                timestampMs: Date.now()
            });
            
            return null;
            
        } catch (e) {
            console.error("Error sending broadcast", e);
            return null;
        }
    });

exports.calculateRankings = functions.firestore
    .document('users/{userId}')
    .onWrite(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();

        // Check if ranking metrics changed
        if (before && after) {
            if (
                before.totalCoins === after.totalCoins &&
                before.currentStreak === after.currentStreak &&
                before.topicsCompleted === after.topicsCompleted &&
                before.studyTimeMinutes === after.studyTimeMinutes &&
                before.totalRevisions === after.totalRevisions &&
                before.createdAtMs === after.createdAtMs
            ) {
                return null;
            }
        }

        const usersSnap = await admin.firestore().collection('users').get();
        let users = [];
        usersSnap.forEach(doc => {
            const data = doc.data();
            if (!data.isBanned && data.role !== 'Admin') {
                users.push({ id: doc.id, ...data });
            }
        });

        // Ranking Priority:
        // 1. Highest Total Coins
        // 2. Highest Day Streak
        // 3. Highest Topics Completed
        // 4. Highest Total Study Time
        // 5. Highest Total Revisions
        // 6. Earliest Account Creation Date
        users.sort((a, b) => {
            const coinsA = a.totalCoins || 0;
            const coinsB = b.totalCoins || 0;
            if (coinsA !== coinsB) return coinsB - coinsA;

            const streakA = a.currentStreak || 0;
            const streakB = b.currentStreak || 0;
            if (streakA !== streakB) return streakB - streakA;

            const topicsA = a.topicsCompleted || 0;
            const topicsB = b.topicsCompleted || 0;
            if (topicsA !== topicsB) return topicsB - topicsA;

            const studyA = a.studyTimeMinutes || 0;
            const studyB = b.studyTimeMinutes || 0;
            if (studyA !== studyB) return studyB - studyA;

            const revA = a.totalRevisions || 0;
            const revB = b.totalRevisions || 0;
            if (revA !== revB) return revB - revA;

            const createdA = a.createdAtMs || 0;
            const createdB = b.createdAtMs || 0;
            return createdA - createdB;
        });

        const batches = [];
        let currentBatch = admin.firestore().batch();
        let operationCount = 0;

        for (let i = 0; i < users.length; i++) {
            const user = users[i];
            const rank = i + 1;
            if (user.airRank !== rank) {
                const ref = admin.firestore().collection('users').doc(user.id);
                currentBatch.update(ref, { airRank: rank });
                operationCount++;
                
                if (operationCount === 450) {
                    batches.push(currentBatch);
                    currentBatch = admin.firestore().batch();
                    operationCount = 0;
                }
            }
        }

        if (operationCount > 0) {
            batches.push(currentBatch);
        }

        for (const batch of batches) {
            await batch.commit();
        }

        return null;
    });
