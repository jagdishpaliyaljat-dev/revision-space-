import re

# 1. Update ProfileScreen
with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    profile_content = f.read()

# Replace showSettingsSheet = true with navigate
profile_content = profile_content.replace("showSettingsSheet = true", "navController.navigate(com.example.ui.SettingsRoute)")

# We want to remove showSettingsSheet state
profile_content = re.sub(r"var showSettingsSheet by remember \{ mutableStateOf\(false\) \}\n", "", profile_content)

# Remove the ProfileSettingsBottomSheet rendering block
sheet_block_regex = r"if \(showSettingsSheet\) \{[\s\S]*?ProfileSettingsBottomSheet\([\s\S]*?onLogoutClick = \{[\s\S]*?\}\n\s*\)\n\s*\}"
profile_content = re.sub(sheet_block_regex, "", profile_content)

# Remove the fun ProfileSettingsBottomSheet entirely
fun_block_regex = r"fun ProfileSettingsBottomSheet\([\s\S]*?\)\s*\{[\s\S]*?ModalBottomSheet\([\s\S]*?\}\n\}\n"
profile_content = re.sub(fun_block_regex, "", profile_content)

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "w") as f:
    f.write(profile_content)

print("ProfileScreen updated")

