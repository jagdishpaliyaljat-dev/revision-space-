package com.example.data.local

import com.example.data.*

fun SubjectEntity.toDomain(): Subject = Subject(
    id = id,
    userId = userId,
    examId = examId,
    name = name,
    colorHex = colorHex,
    icon = icon,
    totalTopicsExpected = totalTopicsExpected,
    isArchived = isArchived
)

fun Subject.toEntity(): SubjectEntity = SubjectEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    examId = examId,
    name = name,
    colorHex = colorHex,
    icon = icon,
    totalTopicsExpected = totalTopicsExpected,
    isArchived = isArchived
)

fun ChapterEntity.toDomain(): Chapter = Chapter(
    id = id,
    userId = userId,
    subjectId = subjectId,
    name = name,
    isCompleted = isCompleted,
    revisionCount = revisionCount,
    progress = progress,
    lastRevisionDateMs = lastRevisionDateMs,
    importance = importance,
    notes = notes,
    scoreObtained = scoreObtained,
    scoreTotal = scoreTotal,
    colorHex = colorHex,
    icon = icon,
    isArchived = isArchived,
    firstStudyDateMs = firstStudyDateMs,
    lastStudyDateMs = lastStudyDateMs,
    totalStudyTimeMs = totalStudyTimeMs,
    totalRevisionTimeMs = totalRevisionTimeMs,
    nextRevisionDateMs = nextRevisionDateMs,
    status = status
)

fun Chapter.toEntity(): ChapterEntity = ChapterEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    subjectId = subjectId,
    name = name,
    isCompleted = isCompleted,
    revisionCount = revisionCount,
    progress = progress,
    lastRevisionDateMs = lastRevisionDateMs,
    importance = importance,
    notes = notes,
    scoreObtained = scoreObtained,
    scoreTotal = scoreTotal,
    colorHex = colorHex,
    icon = icon,
    isArchived = isArchived,
    firstStudyDateMs = firstStudyDateMs,
    lastStudyDateMs = lastStudyDateMs,
    totalStudyTimeMs = totalStudyTimeMs,
    totalRevisionTimeMs = totalRevisionTimeMs,
    nextRevisionDateMs = nextRevisionDateMs,
    status = status
)

fun TopicEntity.toDomain(): Topic {
    val effectiveRevCount = maxOf(revisionCount, completedRevisions)
    val effectiveFirstStudyDate = firstStudyDate
    val effectiveFirstStudyDuration = if (firstStudyDuration > 0L) firstStudyDuration else studyTimeMs
    val effectiveFirstStudyTime = firstStudyTime ?: if (effectiveFirstStudyDuration > 0L) com.example.data.formatDurationSpent(effectiveFirstStudyDuration) else null
    val effectiveLastRevDateMs = maxOf(lastRevisionDateMs, lastRevisionDate ?: 0L)
    val effectiveLastRevDate = lastRevisionDate ?: effectiveLastRevDateMs.takeIf { it > 0L }
    val effectiveLastRevDuration = if (lastRevisionDuration > 0L) lastRevisionDuration else revisionTimeMs
    val effectiveLastRevTime = lastRevisionTime ?: if (effectiveLastRevDuration > 0L) com.example.data.formatDurationSpent(effectiveLastRevDuration) else null
    val effectiveNextRevDateMs = maxOf(nextRevisionDateMs, nextRevisionDate ?: 0L)
    val effectiveNextRevDate = nextRevisionDate ?: effectiveNextRevDateMs.takeIf { it > 0L }
    val effectivePattern = if (revisionPattern.isNotEmpty()) revisionPattern else if (customSpacingPattern.isNotEmpty()) customSpacingPattern else listOf(1, 3, 7, 14, 30)
    val effectiveCustomPattern = if (customSpacingPattern.isNotEmpty()) customSpacingPattern else effectivePattern
    val effectiveStudyCompleted = studyCompleted || effectiveFirstStudyDate != null || effectiveRevCount > 0 || status == "Complete"
    val effectiveTotalRequired = if (totalRequiredRevisions > 0) totalRequiredRevisions else effectivePattern.size.coerceAtLeast(4)
    val effectiveProgress = if (progressScore > 0) progressScore else if (progressPercentage > 0) progressPercentage else com.example.data.calculateProgressScore(effectiveStudyCompleted, effectiveRevCount, effectiveTotalRequired)

    return Topic(
        id = id,
        userId = userId,
        chapterId = chapterId,
        subjectId = subjectId,
        name = name,
        notes = notes,
        difficulty = difficulty,
        priority = priority,
        estimatedRevisionTimeMins = estimatedRevisionTimeMins,
        status = status,
        studyTimeMs = studyTimeMs,
        revisionTimeMs = revisionTimeMs,
        revisionCount = effectiveRevCount,
        lastRevisionDateMs = effectiveLastRevDateMs,
        nextRevisionDateMs = effectiveNextRevDateMs,
        masteryScore = masteryScore,
        examIds = examIds,
        revisionPattern = effectivePattern,
        startDateMs = startDateMs ?: effectiveFirstStudyDate,
        tags = tags,
        firstStudyDate = effectiveFirstStudyDate,
        firstStudyTime = effectiveFirstStudyTime,
        firstStudyDuration = effectiveFirstStudyDuration,
        totalRevisions = effectiveRevCount,
        totalRevisionCount = effectiveRevCount,
        lastRevisionDate = effectiveLastRevDate,
        lastRevisionTime = effectiveLastRevTime,
        lastRevisionDuration = effectiveLastRevDuration,
        nextRevisionDate = effectiveNextRevDate,
        revisionStatus = revisionStatus,
        progressPercentage = effectiveProgress,
        expectedRevisionTime = if (expectedRevisionTime > 0) expectedRevisionTime else estimatedRevisionTimeMins,
        currentRevisionStage = maxOf(currentRevisionStage, effectiveRevCount),
        customSpacingPattern = effectiveCustomPattern,
        studyCompleted = effectiveStudyCompleted,
        completedRevisions = effectiveRevCount,
        totalRequiredRevisions = effectiveTotalRequired,
        progressScore = effectiveProgress
    )
}

fun Topic.toEntity(): TopicEntity {
    val effectiveRevCount = maxOf(revisionCount, completedRevisions)
    val effectiveFirstStudyDate = firstStudyDate
    val effectiveFirstStudyDuration = if (firstStudyDuration > 0L) firstStudyDuration else studyTimeMs
    val effectiveFirstStudyTime = firstStudyTime ?: if (effectiveFirstStudyDuration > 0L) com.example.data.formatDurationSpent(effectiveFirstStudyDuration) else null
    val effectiveLastRevDateMs = maxOf(lastRevisionDateMs, lastRevisionDate ?: 0L)
    val effectiveLastRevDate = lastRevisionDate ?: effectiveLastRevDateMs.takeIf { it > 0L }
    val effectiveLastRevDuration = if (lastRevisionDuration > 0L) lastRevisionDuration else revisionTimeMs
    val effectiveLastRevTime = lastRevisionTime ?: if (effectiveLastRevDuration > 0L) com.example.data.formatDurationSpent(effectiveLastRevDuration) else null
    val effectiveNextRevDateMs = maxOf(nextRevisionDateMs, nextRevisionDate ?: 0L)
    val effectiveNextRevDate = nextRevisionDate ?: effectiveNextRevDateMs.takeIf { it > 0L }
    val effectivePattern = revisionPattern?.takeIf { it.isNotEmpty() } ?: customSpacingPattern?.takeIf { it.isNotEmpty() } ?: listOf(1, 3, 7, 14, 30)
    val effectiveCustomPattern = customSpacingPattern?.takeIf { it.isNotEmpty() } ?: effectivePattern
    val effectiveStudyCompleted = studyCompleted || effectiveFirstStudyDate != null || effectiveRevCount > 0 || status == "Complete"
    val effectiveTotalRequired = if (totalRequiredRevisions > 0) totalRequiredRevisions else effectivePattern.size.coerceAtLeast(4)
    val effectiveProgress = if (progressScore > 0) progressScore else if (progressPercentage > 0) progressPercentage else com.example.data.calculateProgressScore(effectiveStudyCompleted, effectiveRevCount, effectiveTotalRequired)

    return TopicEntity(
        id = id.ifBlank { java.util.UUID.randomUUID().toString() },
        userId = userId.ifBlank { "offline_user" },
        chapterId = chapterId,
        subjectId = subjectId,
        name = name,
        notes = notes,
        difficulty = difficulty,
        priority = priority,
        estimatedRevisionTimeMins = estimatedRevisionTimeMins,
        status = status,
        studyTimeMs = studyTimeMs,
        revisionTimeMs = revisionTimeMs,
        revisionCount = effectiveRevCount,
        lastRevisionDateMs = effectiveLastRevDateMs,
        nextRevisionDateMs = effectiveNextRevDateMs,
        masteryScore = masteryScore,
        examIds = examIds,
        revisionPattern = effectivePattern,
        startDateMs = startDateMs ?: effectiveFirstStudyDate,
        tags = tags,
        firstStudyDate = effectiveFirstStudyDate,
        firstStudyTime = effectiveFirstStudyTime,
        firstStudyDuration = effectiveFirstStudyDuration,
        totalRevisions = effectiveRevCount,
        totalRevisionCount = effectiveRevCount,
        lastRevisionDate = effectiveLastRevDate,
        lastRevisionTime = effectiveLastRevTime,
        lastRevisionDuration = effectiveLastRevDuration,
        nextRevisionDate = effectiveNextRevDate,
        revisionStatus = revisionStatus ?: "Pending",
        progressPercentage = effectiveProgress,
        expectedRevisionTime = if (expectedRevisionTime > 0) expectedRevisionTime else estimatedRevisionTimeMins,
        currentRevisionStage = maxOf(currentRevisionStage, effectiveRevCount),
        customSpacingPattern = effectiveCustomPattern,
        studyCompleted = effectiveStudyCompleted,
        completedRevisions = effectiveRevCount,
        totalRequiredRevisions = effectiveTotalRequired,
        progressScore = effectiveProgress
    )
}

fun RevisionEntity.toDomain(): Revision = Revision(
    id = id,
    userId = userId,
    subjectId = subjectId,
    chapterId = chapterId,
    topicId = topicId,
    subjectName = subjectName,
    chapterName = chapterName,
    topicName = topicName,
    revisionCount = revisionCount,
    pattern = pattern,
    lastRevisionDateMs = lastRevisionDateMs,
    nextRevisionDateMs = nextRevisionDateMs,
    status = status,
    notes = notes,
    studyDate = studyDate,
    studyTime = studyTime,
    duration = duration,
    priority = priority,
    createdAt = createdAt
)

fun Revision.toEntity(): RevisionEntity = RevisionEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    subjectId = subjectId,
    chapterId = chapterId,
    topicId = topicId,
    subjectName = subjectName,
    chapterName = chapterName,
    topicName = topicName,
    revisionCount = revisionCount,
    pattern = pattern,
    lastRevisionDateMs = lastRevisionDateMs,
    nextRevisionDateMs = nextRevisionDateMs,
    status = status,
    notes = notes,
    studyDate = studyDate,
    studyTime = studyTime,
    duration = duration,
    priority = priority,
    createdAt = createdAt
)

fun StudyLogEntity.toDomain(): StudyLog = StudyLog(
    id = id,
    userId = userId,
    dateString = dateString,
    studyTimeMs = studyTimeMs,
    revisionTimeMs = revisionTimeMs,
    focusSessions = focusSessions,
    deepFocusTimeMs = deepFocusTimeMs,
    longestSessionMs = longestSessionMs,
    totalDistractions = totalDistractions,
    sessionCount = sessionCount
)

fun StudyLog.toEntity(): StudyLogEntity = StudyLogEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    dateString = dateString,
    studyTimeMs = studyTimeMs,
    revisionTimeMs = revisionTimeMs,
    focusSessions = focusSessions,
    deepFocusTimeMs = deepFocusTimeMs,
    longestSessionMs = longestSessionMs,
    totalDistractions = totalDistractions,
    sessionCount = sessionCount
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    bio = bio,
    email = email,
    phoneNumber = phoneNumber,
    createdAtMs = createdAtMs,
    lastActiveTimeMs = lastActiveTimeMs,
    isPremium = isPremium,
    role = role,
    isBanned = isBanned,
    banReason = banReason,
    studyTimeMinutes = studyTimeMinutes,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    totalCoins = totalCoins,
    totalRevisions = totalRevisions,
    referralCode = referralCode,
    referredBy = referredBy,
    referralCount = referralCount,
    referralCoinsEarned = referralCoinsEarned,
    isEmailVerified = isEmailVerified,
    isMobileVerified = isMobileVerified,
    totalCoinsWithdrawn = totalCoinsWithdrawn,
    profilePictureUrl = profilePictureUrl,
    profileImageUrl = profileImageUrl,
    totalTestsAttempted = totalTestsAttempted,
    averageAccuracy = averageAccuracy,
    fcmToken = fcmToken,
    airRank = airRank,
    topicsCompleted = topicsCompleted,
    badge = badge,
    league = league,
    rankPosition = rankPosition,
    isRanked = isRanked
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id.ifBlank { "offline_user" },
    name = name,
    bio = bio,
    email = email,
    phoneNumber = phoneNumber,
    createdAtMs = createdAtMs,
    lastActiveTimeMs = lastActiveTimeMs,
    isPremium = isPremium,
    role = role,
    isBanned = isBanned,
    banReason = banReason,
    studyTimeMinutes = studyTimeMinutes,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    totalCoins = totalCoins,
    totalRevisions = totalRevisions,
    referralCode = referralCode,
    referredBy = referredBy,
    referralCount = referralCount,
    referralCoinsEarned = referralCoinsEarned,
    isEmailVerified = isEmailVerified,
    isMobileVerified = isMobileVerified,
    totalCoinsWithdrawn = totalCoinsWithdrawn,
    profilePictureUrl = profilePictureUrl,
    profileImageUrl = profileImageUrl,
    totalTestsAttempted = totalTestsAttempted,
    averageAccuracy = averageAccuracy,
    fcmToken = fcmToken,
    airRank = airRank,
    topicsCompleted = topicsCompleted,
    badge = badge,
    league = league,
    rankPosition = rankPosition,
    isRanked = isRanked
)

fun ExamEntity.toDomain(): ExamCountdown = ExamCountdown(
    id = id,
    userId = userId,
    name = name,
    examDateMs = examDateMs,
    targetStudyHours = targetStudyHours,
    createdAtMs = createdAtMs,
    progress = progress,
    category = category,
    targetRank = targetRank,
    totalSubjects = totalSubjects
)

fun ExamCountdown.toEntity(): ExamEntity = ExamEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    name = name,
    examDateMs = examDateMs,
    targetStudyHours = targetStudyHours,
    createdAtMs = createdAtMs,
    progress = progress,
    category = category,
    targetRank = targetRank,
    totalSubjects = totalSubjects
)

fun RevisionHistoryEntity.toDomain(): RevisionHistoryRecord = RevisionHistoryRecord(
    id = id,
    topicId = topicId,
    topicName = topicName,
    completedDateMs = completedDateMs,
    revisionNumber = revisionNumber,
    durationMs = durationMs,
    notes = notes
)

fun RevisionHistoryRecord.toEntity(): RevisionHistoryEntity = RevisionHistoryEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = "offline_user",
    topicId = topicId,
    topicName = topicName,
    completedDateMs = completedDateMs,
    revisionNumber = revisionNumber,
    durationMs = durationMs,
    notes = notes
)

fun SubjectTestEntity.toDomain(): SubjectTest = SubjectTest(
    id = id,
    userId = userId,
    subjectId = subjectId,
    testName = testName,
    dateMs = dateMs,
    marksObtained = marksObtained,
    totalMarks = totalMarks,
    remarks = remarks
)

fun SubjectTest.toEntity(): SubjectTestEntity = SubjectTestEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    subjectId = subjectId,
    testName = testName,
    dateMs = dateMs,
    marksObtained = marksObtained,
    totalMarks = totalMarks,
    remarks = remarks
)

fun QuickTaskEntity.toDomain(): QuickTask = QuickTask(
    id = id,
    userId = userId,
    title = title,
    subject = subject,
    isOneTime = isOneTime,
    isCarryForward = isCarryForward,
    isCompleted = isCompleted,
    dateCreatedStr = dateCreatedStr,
    completedDateStr = completedDateStr
)

fun QuickTask.toEntity(): QuickTaskEntity = QuickTaskEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    title = title,
    subject = subject,
    isOneTime = isOneTime,
    isCarryForward = isCarryForward,
    isCompleted = isCompleted,
    dateCreatedStr = dateCreatedStr,
    completedDateStr = completedDateStr
)

fun CoinTransactionEntity.toDomain(): CoinTransaction = CoinTransaction(
    id = id,
    userId = userId,
    amount = amount,
    description = description,
    timestampMs = timestampMs,
    type = type
)

fun CoinTransaction.toEntity(): CoinTransactionEntity = CoinTransactionEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = userId.ifBlank { "offline_user" },
    amount = amount,
    description = description,
    timestampMs = timestampMs,
    type = type
)

fun AppNotificationEntity.toDomain(): AppNotification = AppNotification(
    id = id,
    topicId = topicId,
    title = title,
    message = message,
    timestampMs = timestampMs,
    isRead = isRead,
    type = type,
    notificationType = notificationType,
    examId = examId,
    subjectId = subjectId,
    chapterId = chapterId,
    revisionId = revisionId
)

fun AppNotification.toEntity(): AppNotificationEntity = AppNotificationEntity(
    id = id.ifBlank { java.util.UUID.randomUUID().toString() },
    userId = "offline_user",
    topicId = topicId,
    title = title,
    message = message,
    timestampMs = timestampMs,
    isRead = isRead,
    type = type,
    notificationType = notificationType,
    examId = examId,
    subjectId = subjectId,
    chapterId = chapterId,
    revisionId = revisionId
)
