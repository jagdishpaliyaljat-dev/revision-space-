import re

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'r') as f:
    content = f.read()

old_text = 'com.example.utils.DateUtils.formatTimestampToDateString(session.completedDateMs)'
new_text = 'java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(session.completedDateMs))'

content = content.replace(old_text, new_text)

with open('app/src/main/java/com/example/ui/screens/home/DashboardScreen.kt', 'w') as f:
    f.write(content)

