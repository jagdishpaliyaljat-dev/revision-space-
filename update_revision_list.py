with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "r") as f:
    content = f.read()

# 1. Add state variables
target_state = 'var searchQuery by remember { mutableStateOf("") }'
replacement_state = '''var searchQuery by remember { mutableStateOf("") }
    var showTopicExplosion by remember { mutableStateOf(false) }
    var completedTopicTitle by remember { mutableStateOf("Topic Marked as Revised! 🎉") }'''

if target_state in content:
    content = content.replace(target_state, replacement_state)

# 2. Add explosion overlay at end of RevisionListScreen Box
target_end = '            }\n        }\n    }\n}\n'
replacement_end = '''            }
        }

        if (showTopicExplosion) {
            CheckmarkExplosionEffect(
                modifier = Modifier.fillMaxSize(),
                title = completedTopicTitle,
                onAnimationEnd = { showTopicExplosion = false }
            )
        }
    }
}
'''

if target_end in content:
    content = content.replace(target_end, replacement_end)

# 3. Trigger explosion on onComplete
target_complete = '''                                onComplete = {
                                    viewModel.recordTimerSession(topic.topicId, true, topic.estimatedRevisionTimeMins * 60 * 1000L)
                                }'''

replacement_complete = '''                                onComplete = {
                                    completedTopicTitle = "${topic.topicName.take(24)} Marked as Revised! 🎉"
                                    showTopicExplosion = true
                                    viewModel.recordTimerSession(topic.topicId, true, topic.estimatedRevisionTimeMins * 60 * 1000L)
                                }'''

if target_complete in content:
    content = content.replace(target_complete, replacement_complete)

with open("app/src/main/java/com/example/ui/screens/revision/RevisionListScreen.kt", "w") as f:
    f.write(content)

print("Updated RevisionListScreen.kt successfully!")
