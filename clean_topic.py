import re

with open("app/src/main/java/com/example/ui/TopicDetailScreen.kt", "r") as f:
    content = f.read()

target = """                            text = if (recentlyLogged) "Logged!" else if (isInitialStudy) "Save Revision" else "Save Revision","""

replacement = """                            text = if (recentlyLogged) "Logged!" else "Save Revision","""

content = content.replace(target, replacement, 1)

with open("app/src/main/java/com/example/ui/TopicDetailScreen.kt", "w") as f:
    f.write(content)

