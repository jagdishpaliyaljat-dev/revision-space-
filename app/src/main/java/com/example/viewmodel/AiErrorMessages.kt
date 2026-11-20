package com.example.viewmodel

/**
 * Turns raw Firebase AI / network failures into short, user-facing messages.
 *
 * The messages never expose keys or secrets and never fabricate an answer.
 */
object AiErrorMessages {

    fun forThrowable(error: Throwable): String {
        val raw = (error.localizedMessage ?: error.message ?: "").lowercase()
        return when {
            raw.contains("restrictserviceusage") ||
                raw.contains("organization's constraints") ||
                raw.contains("organization constraints") ->
                "AI is blocked by your organization's cloud policy. " +
                    "Ask your administrator to allow the Gemini Developer API " +
                    "(generativelanguage.googleapis.com) for this project, or to " +
                    "allow firebasevertexai.googleapis.com if you want Vertex AI."

            raw.contains("403") || raw.contains("permission denied") ||
                raw.contains("unauthorized") || raw.contains("forbidden") ->
                "Permission denied. Make sure Firebase AI Logic is enabled for " +
                    "this project and the API key has access."

            raw.contains("404") || raw.contains("not found") ||
                raw.contains("model") && raw.contains("not") ->
                "The AI model is currently unavailable. Please update the app " +
                    "or try again later."

            raw.contains("timeout") || raw.contains("timed out") ->
                "The answer took too long. Please try again."

            raw.contains("unable to resolve host") ||
                raw.contains("network") || raw.contains("connection") ||
                raw.contains("unreachable") || raw.contains("offline") ->
                "No internet connection. Check your network and try again."

            raw.contains("quota") || raw.contains("rate limit") ||
                raw.contains("resource exhausted") ->
                "AI usage limit reached. Please try again in a few minutes."

            else -> error.localizedMessage
                ?: "Could not reach the AI. Please try again."
        }
    }
}
