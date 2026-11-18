import re

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace EditProfileRoute with Toast
content = content.replace("navController.navigate(com.example.ui.EditProfileRoute)", "Toast.makeText(context, \"Edit Profile on the Profile Screen\", Toast.LENGTH_SHORT).show()")
content = content.replace("navController.navigate(com.example.ui.ExamGoalRoute)", "Toast.makeText(context, \"Set Goal on the Profile Screen\", Toast.LENGTH_SHORT).show()")

with open("app/src/main/java/com/example/ui/SettingsScreen.kt", "w") as f:
    f.write(content)
