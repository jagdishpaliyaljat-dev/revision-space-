with open('app/src/main/java/com/example/ui/TopicListScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'onCheckedChange = { viewModel.toggleTopicCompletion(context, topic.topicId) },',
    'onCheckedChange = { checked ->\n                                    if (checked) AudioFeedback.playCompletionSound(context)\n                                    viewModel.toggleTopicCompletion(context, topic.topicId)\n                                },'
)

with open('app/src/main/java/com/example/ui/TopicListScreen.kt', 'w') as f:
    f.write(content)
