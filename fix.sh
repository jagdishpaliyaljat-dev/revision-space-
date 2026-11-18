sed -i '/private val requestPermissionLauncher =/,/    }/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {/,/    } else {/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/fetchFcmToken()/!b;n;c\    ' app/src/main/java/com/example/MainActivity.kt
