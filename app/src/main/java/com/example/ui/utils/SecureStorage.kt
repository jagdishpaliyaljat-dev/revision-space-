package com.example.ui.utils
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecureStorage {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "RevisionSpaceSecureKeyV1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PREFS_NAME = "revision_space_secure_prefs"
    
    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
            (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun encrypt(context: Context, plainText: String): String {
        try {
            val secretKey = getSecretKey() ?: return Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptionIv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted bytes
            val combined = ByteArray(encryptionIv.size + encryptedBytes.size)
            System.arraycopy(encryptionIv, 0, combined, 0, encryptionIv.size)
            System.arraycopy(encryptedBytes, 0, combined, encryptionIv.size, encryptedBytes.size)
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        }
    }

    fun decrypt(context: Context, encryptedBase64: String): String {
        try {
            val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val secretKey = getSecretKey() ?: return String(combined, Charsets.UTF_8)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivSize = 12
            if (combined.size < ivSize) return ""
            val iv = ByteArray(ivSize)
            System.arraycopy(combined, 0, iv, 0, ivSize)
            
            val encryptedBytes = ByteArray(combined.size - ivSize)
            System.arraycopy(combined, ivSize, encryptedBytes, 0, encryptedBytes.size)
            
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return try {
                String(Base64.decode(encryptedBase64, Base64.DEFAULT), Charsets.UTF_8)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    fun saveCredentials(context: Context, email: String, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val encryptedPassword = encrypt(context, password)
            prefs.edit()
                .putString("saved_email", email)
                .putString("saved_password_enc", encryptedPassword)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCredentials(context: Context): Pair<String, String>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString("saved_email", null) ?: return null
        val encryptedPassword = prefs.getString("saved_password_enc", null) ?: return null
        return try {
            val decryptedPassword = decrypt(context, encryptedPassword)
            if (decryptedPassword.isNotEmpty()) {
                Pair(email, decryptedPassword)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearCredentials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("saved_email")
            .remove("saved_password_enc")
            .apply()
    }

    fun isRememberLoginEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("remember_login_enabled", true)
    }

    fun setRememberLoginEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("remember_login_enabled", enabled).apply()
    }
}
