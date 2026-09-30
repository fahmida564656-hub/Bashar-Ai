package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChatMode
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiResult
import com.example.ui.screens.evaluateMathExpression
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("বাশার এ আই", appName)
    }

    @Test
    fun `brand creator mandate rule 52 in Bengali and English`() = runBlocking {
        val client = GeminiApiClient()

        val queries = listOf(
            "তোমারে কে বানাইছে?",
            "তোমাকে কে তৈরি করেছে?",
            "Who made this AI?",
            "এই AI-এর creator কে?",
            "who created you",
            "who developed you",
            "tomake ke baniyeche"
        )

        for (q in queries) {
            val result = client.generateContent(prompt = q, mode = ChatMode.QUICK)
            assertTrue("Expected success for query '$q'", result is GeminiResult.Success)
            val success = result as GeminiResult.Success
            assertEquals(
                "আমাকে Bashar Gojol Studio Channel-এর জন্য তৈরি করা হয়েছে।",
                success.text
            )
        }
    }

    @Test
    fun `calculator expression evaluation test`() {
        assertEquals("15", evaluateMathExpression("10+5"))
        assertEquals("42", evaluateMathExpression("6*7"))
        assertEquals("20", evaluateMathExpression("(10+5)*2-10"))
    }
}
