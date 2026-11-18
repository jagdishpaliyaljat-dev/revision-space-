with open('app/src/main/java/com/example/ui/TimerScreen.kt', 'r') as f:
    content = f.read()

injection = """
    LaunchedEffect(isRunning, settings.ambientSound) {
        AmbientSoundPlayer.setSound(settings.ambientSound)
        if (isRunning) {
            AmbientSoundPlayer.play()
        } else {
            AmbientSoundPlayer.stop()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            AmbientSoundPlayer.stop()
        }
    }
"""

content = content.replace('val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()', 'val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()\n' + injection)

with open('app/src/main/java/com/example/ui/TimerScreen.kt', 'w') as f:
    f.write(content)
