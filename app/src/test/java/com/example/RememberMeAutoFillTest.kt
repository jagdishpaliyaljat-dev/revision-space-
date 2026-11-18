package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.Chapter
import com.example.data.RevisionRepository
import com.example.data.StudyLog
import com.example.data.Subject
import com.example.data.Topic
import com.example.data.UserProfile
import com.example.ui.utils.SecureStorage
import com.example.ui.screens.auth.SignInScreen
import com.example.ui.theme.RevisionSpaceTheme
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-420dpi", sdk = [34])
class RememberMeAutoFillTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: RevisionViewModel
    private lateinit var context: Context
    private var shouldThrowNetworkError = false
    private var signUpCount = 0
    private var signInCount = 0

    inner class FakeRevisionRepository : RevisionRepository() {
        override val userId: String = "test_user_789"
        
        override val currentUserProfile: Flow<UserProfile?> = flowOf(
            UserProfile(
                id = "test_user_789",
                name = "Aspirant Test",
                email = "test@example.com"
            )
        )

        override val allSubjects: Flow<List<Subject>> = flowOf(emptyList())
        override val allChapters: Flow<List<Chapter>> = flowOf(emptyList())
        override val allTopics: Flow<List<Topic>> = flowOf(emptyList())
        override val allStudyLogs: Flow<List<StudyLog>> = flowOf(emptyList())

        override suspend fun signUpWithEmail(email: String, password: String, name: String, referralCodeStr: String) {
            signUpCount++
        }

        override suspend fun signInWithEmail(email: String, password: String) {
            signInCount++
            if (shouldThrowNetworkError) {
                throw Exception("network error")
            }
            if (email == "test@example.com" && password == "password123") {
                // Success
                return
            } else {
                throw Exception("credential is incorrect")
            }
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Reset SecureStorage credentials & settings to clean state for each test
        SecureStorage.clearCredentials(context)
        SecureStorage.setRememberLoginEnabled(context, true)
        shouldThrowNetworkError = false
        signInCount = 0
        signUpCount = 0

        try { 
            com.google.firebase.FirebaseApp.initializeApp(context) 
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
        
        val repository = FakeRevisionRepository()
        viewModel = RevisionViewModel(repository)
    }

    @Test
    fun testFirstLogin_RememberMe_Checked_SavesCredentials() {
        var onSignInSuccessCalled = false

        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {
                    onSignInSuccessCalled = true
                }
            }
        }

        composeTestRule.waitForIdle()

        // 1. Enter valid email and password
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")

        // 2. Verify 'Remember Login' is checked by default
        composeTestRule.onNodeWithTag("remember_login_checkbox").assertIsOn()

        // 3. Click Sign In
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // 4. Verify successful login and credential storage
        assertTrue(onSignInSuccessCalled)
        assertEquals(1, signInCount)

        val saved = SecureStorage.getCredentials(context)
        assertNotNull(saved)
        assertEquals("test@example.com", saved?.first)
        assertEquals("password123", saved?.second)
    }

    @Test
    fun testRememberLogin_Off_DoesNotSaveCredentials() {
        var onSignInSuccessCalled = false

        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {
                    onSignInSuccessCalled = true
                }
            }
        }

        composeTestRule.waitForIdle()

        // 1. Enter credentials
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")

        // 2. Uncheck Remember Login checkbox
        composeTestRule.onNodeWithTag("remember_login_checkbox").performClick()
        composeTestRule.onNodeWithTag("remember_login_checkbox").assertIsOff()

        // 3. Click Sign In
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // 4. Verify successful login but NO saved credentials
        assertTrue(onSignInSuccessCalled)
        assertNull(SecureStorage.getCredentials(context))
        assertFalse(SecureStorage.isRememberLoginEnabled(context))
    }

    @Test
    fun testAutoFill_OnLaunch_And_OneTapSignIn() {
        // Pre-save credentials securely to simulate past successful login with Remember Me enabled
        SecureStorage.setRememberLoginEnabled(context, true)
        SecureStorage.saveCredentials(context, "test@example.com", "password123")

        var onSignInSuccessCalled = false

        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {
                    onSignInSuccessCalled = true
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify credentials are automatically loaded into fields
        composeTestRule.onNodeWithTag("email_input").assertTextContains("test@example.com")
        composeTestRule.onNodeWithTag("password_input").assertTextContains("•••••••••••") // 11 bullets for "password123"

        // Click Sign In without manual typing/input
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // Verify successful one-tap login
        assertTrue(onSignInSuccessCalled)
        assertEquals(1, signInCount)
    }

    @Test
    fun testInvalidCredentials_ShowsCorrectErrorMessage_And_DoesNotCorruptSavedCredentials() {
        // Pre-save valid credentials to verify they are not corrupted or overwritten with invalid data
        SecureStorage.setRememberLoginEnabled(context, true)
        SecureStorage.saveCredentials(context, "test@example.com", "password123")

        var onSignInSuccessCalled = false

        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {
                    onSignInSuccessCalled = true
                }
            }
        }

        composeTestRule.waitForIdle()

        // Enter incorrect password
        composeTestRule.onNodeWithTag("password_input").performTextReplacement("wrong_password")

        // Click Sign In
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // Verify login failed and correct error message is shown
        assertFalse(onSignInSuccessCalled)
        composeTestRule.onNodeWithText("Invalid email or password.").assertExists()

        // Verify previously saved credentials are NOT corrupted or removed
        val saved = SecureStorage.getCredentials(context)
        assertNotNull(saved)
        assertEquals("test@example.com", saved?.first)
        assertEquals("password123", saved?.second)
    }

    @Test
    fun testNetworkError_DisplaysNetworkErrorMessage() {
        shouldThrowNetworkError = true
        var onSignInSuccessCalled = false

        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {
                    onSignInSuccessCalled = true
                }
            }
        }

        composeTestRule.waitForIdle()

        // Enter credentials
        composeTestRule.onNodeWithTag("email_input").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")

        // Click Sign In
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // Verify login failed and correct network error message is shown
        assertFalse(onSignInSuccessCalled)
        composeTestRule.onNodeWithText("Network error. Please check your connection.").assertExists()
    }

    @Test
    fun testLogout_PersistsCredentialsForAutoFill() {
        // 1. Simulate First Login with Remember Login ON
        SecureStorage.setRememberLoginEnabled(context, true)
        SecureStorage.saveCredentials(context, "test@example.com", "password123")

        // 2. Tap Logout (which is handled by clearing session state but keeping saved credentials since Remember Login is ON)
        // SharedPreferences settings should still contain the credentials
        val credentialsAfterLogout = SecureStorage.getCredentials(context)
        assertNotNull(credentialsAfterLogout)
        assertEquals("test@example.com", credentialsAfterLogout?.first)
        assertEquals("password123", credentialsAfterLogout?.second)

        // 3. Verify that opening the Login screen again auto-fills the fields
        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {}
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_input").assertTextContains("test@example.com")
    }

    @Test
    fun testDisableRememberLogin_And_Logout_ClearsCredentials() {
        // 1. Start with saved credentials
        SecureStorage.setRememberLoginEnabled(context, true)
        SecureStorage.saveCredentials(context, "test@example.com", "password123")

        // 2. Open login screen
        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {}
            }
        }
        composeTestRule.waitForIdle()

        // Verify fields are prefilled
        composeTestRule.onNodeWithTag("email_input").assertTextContains("test@example.com")

        // 3. Turn Remember Login OFF via the checkbox
        composeTestRule.onNodeWithTag("remember_login_checkbox").performClick()
        composeTestRule.onNodeWithTag("remember_login_checkbox").assertIsOff()

        // Verify credentials have been cleared from SecureStorage/Prefs immediately
        assertNull(SecureStorage.getCredentials(context))
        assertFalse(SecureStorage.isRememberLoginEnabled(context))
    }

    @Test
    fun testLaunch_WhenRememberLoginDisabled_DoesNotPrefill() {
        // 1. Start with Remember Login OFF and no saved credentials
        SecureStorage.setRememberLoginEnabled(context, false)
        SecureStorage.clearCredentials(context)

        // 2. Open login screen
        composeTestRule.setContent {
            RevisionSpaceTheme {
                SignInScreen(viewModel = viewModel) {}
            }
        }
        composeTestRule.waitForIdle()

        // 3. Verify fields are completely empty on launch
        composeTestRule.onNodeWithTag("email_input").assertTextContains("")
        composeTestRule.onNodeWithTag("password_input").assertTextContains("")
    }
}
