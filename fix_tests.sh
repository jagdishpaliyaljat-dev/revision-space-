#!/bin/bash
sed -i 's/import com.example.ui.ProfileScreen/import com.example.ui.screens.profile.ProfileScreen/g' app/src/test/java/com/example/ProfileUiTest.kt
sed -i 's/import com.example.ui.SignInScreen/import com.example.ui.screens.auth.SignInScreen/g' app/src/test/java/com/example/RememberMeAutoFillTest.kt
sed -i 's/import com.example.ui.SecureStorage/import com.example.ui.utils.SecureStorage/g' app/src/test/java/com/example/RememberMeAutoFillTest.kt
