package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ChatMode
import com.example.ui.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChatViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = ChatViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest(testDispatcher) {
        assertNotNull(viewModel.messages.value)
        assertFalse(viewModel.isGenerating.value)
        assertEquals(ChatMode.QUICK, viewModel.currentMode.value)
        assertNull(viewModel.errorMessage.value)
        assertNull(viewModel.selectedImage.value)
    }

    @Test
    fun `setChatMode updates mode and uiState`() = runTest(testDispatcher) {
        viewModel.setChatMode(ChatMode.CODING)
        assertEquals(ChatMode.CODING, viewModel.currentMode.value)

        viewModel.setChatMode(ChatMode.RESEARCH)
        assertEquals(ChatMode.RESEARCH, viewModel.currentMode.value)
    }

    @Test
    fun `sendMessage with creator intent handles response via Gemini API service layer`() = runTest(testDispatcher) {
        val job = viewModel.sendMessage("তোমারে কে বানাইছে?")
        job.join()

        val convId = viewModel.currentConversationId.value
        assertNotNull("Conversation ID should be created", convId)
        val msgs = viewModel.repository.getMessagesList(convId!!)
        assertEquals(2, msgs.size)
        assertEquals("user", msgs[0].role)
        assertEquals("model", msgs[1].role)
        assertEquals(
            "আমাকে Bashar Gojol Studio Channel-এর জন্য তৈরি করা হয়েছে।",
            msgs[1].content
        )
        assertFalse(viewModel.isGenerating.value)
    }

    @Test
    fun `clearError clears error state`() = runTest(testDispatcher) {
        viewModel.clearError()
        assertNull(viewModel.errorMessage.value)
    }
}
