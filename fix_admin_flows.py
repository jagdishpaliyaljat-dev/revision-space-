import re

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'r') as f:
    content = f.read()

# 1. getAllUsersProfiles
old_users = """    open fun getAllUsersProfiles(): Flow<List<UserProfile>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_users = """    open fun getAllUsersProfiles(): Flow<List<UserProfile>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("users").whereEqualTo("id", uid).addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""

if old_users in content:
    content = content.replace(old_users, new_users)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

# 2. getAllCoinTransactions
old_coins = """    open fun getAllCoinTransactions(): Flow<List<CoinTransaction>> = callbackFlow {
        val listener = db.collection("coin_history")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_coins = """    open fun getAllCoinTransactions(): Flow<List<CoinTransaction>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("coin_history").whereEqualTo("userId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_coins in content:
    content = content.replace(old_coins, new_coins)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

# 3. getAllReferrals
old_refs = """    open fun getAllReferrals(): Flow<List<ReferralRecord>> = callbackFlow {
        val listener = db.collection("referrals")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_refs = """    open fun getAllReferrals(): Flow<List<ReferralRecord>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("referrals").whereEqualTo("referrerId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_refs in content:
    content = content.replace(old_refs, new_refs)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

# 4. getAllWithdrawalRequests
old_with = """    open fun getAllWithdrawalRequests(): Flow<List<WithdrawalRequest>> = callbackFlow {
        val listener = db.collection("withdrawal_requests")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_with = """    open fun getAllWithdrawalRequests(): Flow<List<WithdrawalRequest>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("withdrawal_requests").whereEqualTo("userId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_with in content:
    content = content.replace(old_with, new_with)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

# 5. getAdminLogs
old_admin = """    fun getAdminLogs(): Flow<List<AdminLog>> = callbackFlow {
        val listener = db.collection("admin_logs")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_admin = """    fun getAdminLogs(): Flow<List<AdminLog>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("admin_logs").whereEqualTo("adminId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_admin in content:
    content = content.replace(old_admin, new_admin)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

# 6. getBroadcastAnalytics
old_broad = """    fun getBroadcastAnalytics(): Flow<List<NotificationBroadcast>> = callbackFlow {
        val listener = db.collection("notification_broadcasts")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
new_broad = """    fun getBroadcastAnalytics(): Flow<List<NotificationBroadcast>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList())
        else callbackFlow {
            val listener = db.collection("notification_broadcasts").whereEqualTo("adminId", uid)
                .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->"""
if old_broad in content:
    content = content.replace(old_broad, new_broad)
    content = content.replace('        awaitClose { listener.remove() }\n    }', '        awaitClose { listener.remove() }\n        }\n    }', 1)

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'w') as f:
    f.write(content)
