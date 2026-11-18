import re

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'r') as f:
    content = f.read()

new_signIn = """    open suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                // Self-healing: if auth is successful but user profile in Firestore doesn't exist, create it on-demand
                val userEmail = auth.currentUser?.email ?: email
                val userName = userEmail.substringBefore("@")
                val profile = UserProfile(
                    id = userId,
                    name = userName,
                    email = userEmail,
                    referralCode = generateReferralCodeForEmail(userEmail),
                    createdAtMs = System.currentTimeMillis()
                )
                db.collection("users").document(userId).set(profile).await()
                
                try {
                    db.collection("users").document(userId).collection("data").document("settings").set(
                        mapOf(
                            "dailyTarget" to 0,
                            "notificationsEnabled" to true,
                            "soundEnabled" to true,
                            "vibrationEnabled" to true
                        )
                    ).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val isBanned = userDoc.getBoolean("isBanned") ?: false
                if (isBanned) {
                    auth.signOut()
                    throw Exception("Your account has been suspended by administrator.\\nContact support for assistance.")
                }
            }
        }
    }"""

old_signIn = """    open suspend fun signInWithEmail(email: String, password: String) {
        android.util.Log.d("AuthPerf", "signInWithEmail: Starting")
        auth.signInWithEmailAndPassword(email, password).await()
        val userId = auth.currentUser?.uid
        android.util.Log.d("AuthPerf", "signInWithEmail: auth succeeded, uid: $userId")
        if (userId != null) {
            try {
                android.util.Log.d("AuthPerf", "signInWithEmail: getting userDoc")
                val userDoc = db.collection("users").document(userId).get().await()
                android.util.Log.d("AuthPerf", "signInWithEmail: userDoc exists: ${userDoc.exists()}")
                if (!userDoc.exists()) {
                    val userEmail = auth.currentUser?.email ?: email
                    val userName = userEmail.substringBefore("@")
                    val profile = UserProfile(
                        id = userId,
                        name = userName,
                        email = userEmail,
                        referralCode = generateReferralCodeForEmail(userEmail),
                        createdAtMs = System.currentTimeMillis()
                    )
                    android.util.Log.d("AuthPerf", "signInWithEmail: creating user")
                    db.collection("users").document(userId).set(profile).await()
                    
                    try {
                        db.collection("users").document(userId).collection("data").document("settings").set(
                            mapOf(
                                "dailyTarget" to 0,
                                "notificationsEnabled" to true,
                                "soundEnabled" to true,
                                "vibrationEnabled" to true
                            )
                        ).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    val isBanned = userDoc.getBoolean("isBanned") ?: false
                    if (isBanned) {
                        auth.signOut()
                        throw Exception("Your account has been suspended by administrator.\\nContact support for assistance.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthPerf", "signInWithEmail: error", e)
                throw e
            }
        }
    }"""

if old_signIn in content:
    content = content.replace(old_signIn, new_signIn)
    with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'w') as f:
        f.write(content)
    print("Reverted signInWithEmail")
else:
    print("Could not find old_signIn with logs")
