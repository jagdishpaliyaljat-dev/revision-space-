import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove requestPermissionLauncher
content = re.sub(r'private val requestPermissionLauncher.*?\}\n  \}\n', '', content, flags=re.DOTALL)

# Remove SDK TIRAMISU check
content = re.sub(r'if \(Build\.VERSION\.SDK_INT >= Build\.VERSION_CODES\.TIRAMISU\).*?\} else \{\n\s*fetchFcmToken\(\)\n\s*\}', 'fetchFcmToken()', content, flags=re.DOTALL)

# Remove AlarmScheduler effect
content = re.sub(r'LaunchedEffect\(revisionSettings\) \{.*?\n\s*\}', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
