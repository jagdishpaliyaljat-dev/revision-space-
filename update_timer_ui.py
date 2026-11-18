with open('app/src/main/java/com/example/ui/TimerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'var showSettingsSheet by remember { mutableStateOf(false) }',
    'var showSettingsSheet by remember { mutableStateOf(false) }\n    var showAmbientSoundSheet by remember { mutableStateOf(false) }'
)

old_settings_sheet = """    if (showSettingsSheet) {
        FocusSettingsBottomSheet(
            currentSettings = settings,
            activeMode = activeMode,
            onDismiss = { showSettingsSheet = false },
            onSave = { updatedSettings ->
                userProfile?.let {
                    viewModel.updateUserProfile(it.copy(focusSettings = updatedSettings))
                }
                showSettingsSheet = false
            }
        )
    }"""

new_settings_sheet = """    if (showSettingsSheet) {
        FocusSettingsBottomSheet(
            currentSettings = settings,
            activeMode = activeMode,
            onDismiss = { showSettingsSheet = false },
            onSave = { updatedSettings ->
                userProfile?.let {
                    viewModel.updateUserProfile(it.copy(focusSettings = updatedSettings))
                }
                showSettingsSheet = false
            }
        )
    }

    if (showAmbientSoundSheet) {
        AmbientSoundBottomSheet(
            currentSound = settings.ambientSound,
            onDismiss = { showAmbientSoundSheet = false },
            onSelect = { newSound ->
                userProfile?.let {
                    val newSettings = settings.copy(ambientSound = newSound, soundEnabled = true)
                    viewModel.updateUserProfile(it.copy(focusSettings = newSettings))
                }
                showAmbientSoundSheet = false
            }
        )
    }"""

content = content.replace(old_settings_sheet, new_settings_sheet)

old_music_btn = """                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F0FF)).clickable {
                             android.widget.Toast.makeText(context, "Music feature coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = FocusTextDark, modifier = Modifier.size(20.dp))
                        }"""

new_music_btn = """                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3F0FF)).clickable {
                             showAmbientSoundSheet = true
                        }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = FocusTextDark, modifier = Modifier.size(20.dp))
                        }"""

content = content.replace(old_music_btn, new_music_btn)

with open('app/src/main/java/com/example/ui/TimerScreen.kt', 'w') as f:
    f.write(content)
