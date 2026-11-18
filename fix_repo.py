import re

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'r') as f:
    content = f.read()

# 1. globalConfig
old_globalConfig = """    val globalConfig: Flow<GlobalAppConfig> = callbackFlow {
        try {
            val listener = db.collection("global_config").document("settings")
                .addSnapshotListener { snapshot, _ ->"""

new_globalConfig = """    val globalConfig: Flow<GlobalAppConfig> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(GlobalAppConfig())
        else callbackFlow {
        try {
            val listener = db.collection("global_config").document("settings")
                .addSnapshotListener { snapshot, _ ->"""
if old_globalConfig in content:
    content = content.replace(old_globalConfig, new_globalConfig)
    content = content.replace('        } catch (e: Exception) {\n            e.printStackTrace()\n            trySend(GlobalAppConfig())\n            close(e)\n        }\n    }', '        } catch (e: Exception) {\n            e.printStackTrace()\n            trySend(GlobalAppConfig())\n            close(e)\n        }\n    }\n    }', 1)

with open('app/src/main/java/com/example/data/RevisionRepository.kt', 'w') as f:
    f.write(content)
