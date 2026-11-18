import re

with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "r") as f:
    content = f.read()

target = """model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(if (safeProfile.profileImageUrl != null && safeProfile.profileImageUrl.isNotEmpty()) safeProfile.profileImageUrl else safeProfile.profilePictureUrl)
                                        .memoryCacheKey(safeProfile.profileImageUrl ?: safeProfile.profilePictureUrl)
                                        .diskCacheKey(safeProfile.profileImageUrl ?: safeProfile.profilePictureUrl)
                                        .crossfade(true)"""
if target not in content:
    old_target = """model = coil.request.ImageRequest.Builder(LocalContext.current)
                                        .data(if (safeProfile.profileImageUrl != null && safeProfile.profileImageUrl.isNotEmpty()) safeProfile.profileImageUrl else safeProfile.profilePictureUrl)
                                        .crossfade(true)"""
    content = content.replace(old_target, target)
    
with open("app/src/main/java/com/example/ui/ProfileScreen.kt", "w") as f:
    f.write(content)
