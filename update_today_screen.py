with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "r") as f:
    content = f.read()

# 1. Add state variables
target_state = 'var showConfetti by remember { mutableStateOf(false) }'
replacement_state = '''var showConfetti by remember { mutableStateOf(false) }
    var showTopicExplosion by remember { mutableStateOf(false) }
    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised! 🎉") }'''

if target_state in content:
    content = content.replace(target_state, replacement_state)

# 2. Add explosion overlay at end of Box
target_overlay = '''    if (showGoalDialog) {
        DailyGoalCongratsDialog(
            goalTarget = settings.dailyGoal,
            unitName = "revision sessions",
            streakDays = userProfile?.currentStreak ?: 1,
            onDismiss = { showGoalDialog = false },
            onReplayAnimation = { showConfetti = true }
        )
    }'''

replacement_overlay = '''    if (showGoalDialog) {
        DailyGoalCongratsDialog(
            goalTarget = settings.dailyGoal,
            unitName = "revision sessions",
            streakDays = userProfile?.currentStreak ?: 1,
            onDismiss = { showGoalDialog = false },
            onReplayAnimation = { showConfetti = true }
        )
    }

    if (showTopicExplosion) {
        CheckmarkExplosionEffect(
            modifier = Modifier.fillMaxSize(),
            title = completedTopicTitle,
            onAnimationEnd = { showTopicExplosion = false }
        )
    }'''

if target_overlay in content:
    content = content.replace(target_overlay, replacement_overlay)

# 3. Trigger explosion on quick complete button click
target_click = '''                                var isCompleting by remember { mutableStateOf(false) }
                                FilledIconButton(
                                    onClick = {
                                        isCompleting = true
                                        viewModel.recordTimerSession(
                                            topic.topicId,
                                            isRevision = true,
                                            durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L
                                        )
                                    },'''

replacement_click = '''                                var isCompleting by remember { mutableStateOf(false) }
                                FilledIconButton(
                                    onClick = {
                                        isCompleting = true
                                        completedTopicTitle = "${topic.topicName.take(24)} Marked as Revised! 🎉"
                                        showTopicExplosion = true
                                        viewModel.recordTimerSession(
                                            topic.topicId,
                                            isRevision = true,
                                            durationMs = topic.estimatedRevisionTimeMins * 60 * 1000L
                                        )
                                    },'''

if target_click in content:
    content = content.replace(target_click, replacement_click)

with open("app/src/main/java/com/example/ui/screens/revision/TodayRevisionScreen.kt", "w") as f:
    f.write(content)

print("Updated TodayRevisionScreen.kt successfully!")
