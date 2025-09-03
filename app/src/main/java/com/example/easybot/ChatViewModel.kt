package com.example.easybot

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.TextPart
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {                //lazy=(lateinit) performance optimization,messageList will be initialized when its first accessed not when object creasted
        mutableStateListOf<MessageModel>()
    }

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
        apiKey = Constants.apikey
    )

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        Content(role = it.role, parts = listOf(TextPart(it.message)))
                    }
                )
                //messageList.map { ... }: converts each MessageModel into a Content object
                //Content(...): wraps the role and message parts.
                //TextPart(it.message): wraps the actual message string so Gemini knows it's plain text

                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing....", "model"))

                val response = chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))

            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Error : " + e.message.toString(), "model"))
            }
        }
    }
}
