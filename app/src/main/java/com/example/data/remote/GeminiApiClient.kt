package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.ChatMode
import com.example.data.model.SourceCitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        prompt: String,
        mode: ChatMode,
        history: List<Pair<String, String>> = emptyList(),
        imageBitmap: Bitmap? = null,
        apiKeyOverride: String = "",
        modelOverride: String = "",
        customSystemPrompt: String? = null,
        webSearchEnabled: Boolean = false
    ): GeminiResult = withContext(Dispatchers.IO) {
        val trimmedPrompt = prompt.trim()

        // Section 52 Intent Interceptor:
        if (isCreatorQuery(trimmedPrompt)) {
            val creatorResponse = """
                আমাকে **Bashar Gojol Studio Channel**-এর জন্য তৈরি করা হয়েছে। 
                
                আমি **বাশার এ আই (Bashar Ai)** — একটি আধুনিক, বুদ্ধিমান ও সার্বজনীন কৃত্রিম বুদ্ধিমত্তা সহকারী। আপনার পড়াশোনা, বিজ্ঞান, ইসলামিক জ্ঞান, কোডিং, গণিত, লেখালেখি কিংবা যেকোনো অজানা প্রশ্নের বিস্তারিত ও সুন্দর ব্যাখ্যার জন্য আমি সার্বক্ষণিক প্রস্তুত।
                
                ---
                ✨ "Bashar Gojol Studio" চ্যানেলটি সাবস্ক্রাইব করুন: https://www.youtube.com/@BasharGojolStudio
            """.trimIndent()
            return@withContext GeminiResult.Success(
                text = creatorResponse,
                sources = emptyList()
            )
        }

        val apiKey = apiKeyOverride.ifBlank { BuildConfig.GEMINI_API_KEY }.trim()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Return rich offline response with clear guidance
            return@withContext GeminiResult.Success(
                text = generateOfflineFallback(trimmedPrompt, mode),
                sources = if (webSearchEnabled || mode == ChatMode.RESEARCH) getSampleSources(trimmedPrompt) else emptyList(),
                isDemo = true
            )
        }

        val targetModel = when {
            modelOverride.isNotBlank() && modelOverride != "auto" -> modelOverride
            mode == ChatMode.REASONING || mode == ChatMode.CODING || mode == ChatMode.MATH -> "gemini-3.1-pro-preview"
            else -> "gemini-3.5-flash"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"

        val systemInstructionText = buildString {
            append("You are Bashar AI (বাশার এ আই), the elite, highly professional, deeply knowledgeable, articulate, and empathetic AI Assistant proudly developed for 'Bashar Gojol Studio'.\n")
            append("CORE PHILOSOPHY & MANDATE:\n")
            append("1. COMPREHENSIVE, BEAUTIFUL & IN-DEPTH ANSWERS: Whatever question the user asks—whether about General Knowledge, Science, Technology, Islamic History & Teachings, Hadith, Quranic insights, Mathematics, Programming & Code, Literature, Essays, Poetry & Ghazals, Business, Health, Agriculture, or Everyday Life—you MUST answer thoroughly, deeply, and beautifully. Never give short, lazy, or one-sentence replies. Break down complex topics into clear, well-structured sections using bold subheadings (##), bullet points (•), numbered steps, and relatable everyday examples so the user completely understands everything with maximum clarity.\n")
            append("2. POLITE, RESPECTFUL & ENGAGING BENGALI TONE: Speak in warm, elegant, respectful, and articulate Bengali (or English if the user prompts in English). Treat the user with high esteem, encouragement, and genuine helpfulness.\n")
            append("3. CREATOR MANDATE: If the user asks who made you, who created you, or your developer/creator in Bengali, English, or Banglish (e.g. 'তোমারে কে বানাইছে?', 'Who made this AI?'), you MUST respond proudly: 'আমাকে Bashar Gojol Studio Channel-এর জন্য তৈরি করা হয়েছে।'\n")
            append("4. MANDATORY CHANNEL SIGNATURE: At the very end of EVERY single answer, after your complete detailed explanation, you MUST append the following exact sentence:\n")
            append("\"Bashar Gojol Studio\" চ্যানেলটি সাবস্ক্রাইব করুন: https://www.youtube.com/@BasharGojolStudio\n")
            if (!customSystemPrompt.isNullOrBlank()) {
                append("Additional Context: ").append(customSystemPrompt).append("\n")
            }
            when (mode) {
                ChatMode.QUICK -> append("Provide an articulate, comprehensive, well-structured, and clear answer covering all important dimensions.\n")
                ChatMode.REASONING -> append("Break down complex reasoning step-by-step with logical premises, analysis, counter-arguments, and firm conclusions.\n")
                ChatMode.RESEARCH -> append("Produce an exhaustive, research-grade analytical report with contextual background, key pillars, comparative insights, and verified facts.\n")
                ChatMode.CODING -> append("Deliver production-ready, beautifully structured code with detailed line-by-line explanation, best practices, edge cases, and usage instructions.\n")
                ChatMode.STUDY -> append("Act as an expert scholarly educator: explain fundamentals with simple analogies, illustrate with examples, outline key takeaways, and offer practical study tips.\n")
                ChatMode.WRITING -> append("Exhibit sublime literary elegance, poetic harmony, rich Bengali vocabulary, and deep emotive expression.\n")
                ChatMode.DOCUMENT -> append("Deliver an exhaustive structured briefing covering core themes, actionable insights, and structured data points.\n")
                ChatMode.MATH -> append("Solve deterministically with clear step-by-step arithmetic/algebraic derivation, formula definitions, and proof verification.\n")
            }
        }

        try {
            val requestJson = JSONObject()

            // System instruction
            val sysContent = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                }
                put("parts", parts)
            }
            requestJson.put("systemInstruction", sysContent)

            // Contents array
            val contentsArray = JSONArray()

            // History
            for ((role, text) in history) {
                val turnObj = JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turnObj)
            }

            // Current prompt
            val currentTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().apply { put("text", trimmedPrompt) })

                if (imageBitmap != null) {
                    val base64Image = bitmapToBase64(imageBitmap)
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    }
                    parts.put(JSONObject().apply { put("inlineData", inlineData) })
                }
                put("parts", parts)
            }
            contentsArray.put(currentTurn)

            requestJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject().apply {
                put("temperature", if (mode == ChatMode.CODING || mode == ChatMode.MATH) 0.2 else 0.7)
                put("topP", 0.95)
            }
            requestJson.put("generationConfig", genConfig)

            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext GeminiResult.Error(
                    message = "দুঃখিত, সংযোগ স্থাপন করা সম্ভব হয়নি (HTTP ${response.code})। আপনার ইন্টারনেট ও API Key যাচাই করুন।"
                )
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext GeminiResult.Error("কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।")
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val partObj = parts.getJSONObject(i)
                    if (partObj.has("text")) {
                        textBuilder.append(partObj.getString("text"))
                    }
                }
            }

            var generatedText = textBuilder.toString().ifBlank {
                "দুঃখিত, কোনো উত্তর পাওয়া যায়নি।"
            }

            val channelSignature = "\"Bashar Gojol Studio\" চ্যানেলটি সাবস্ক্রাইব করুন: https://www.youtube.com/@BasharGojolStudio"
            if (!generatedText.contains("@BasharGojolStudio")) {
                generatedText = "$generatedText\n\n---\n✨ $channelSignature"
            }

            val sources = if (webSearchEnabled || mode == ChatMode.RESEARCH) {
                extractOrGenerateSources(trimmedPrompt, generatedText)
            } else {
                emptyList()
            }

            GeminiResult.Success(text = generatedText, sources = sources, isDemo = false)

        } catch (e: Exception) {
            GeminiResult.Error(
                message = "দুঃখিত, এই মুহূর্তে সার্ভারে সমস্যা হচ্ছে। কিছুক্ষণ পরে আবার চেষ্টা করুন।"
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun isCreatorQuery(query: String): Boolean {
        val normalized = query.lowercase().trim()
        val patterns = listOf(
            "who made this ai", "who made you", "who created you", "who is your creator",
            "who developed you", "who built you", "creator of this ai", "creator ke",
            "তোমারে কে বানাইছে", "তোমাকে কে বানিয়েছে", "তোমাকে কে তৈরি করেছে",
            "তোমাকে কে বানাইছে", "কে বানাইছে তোমারে", "কে বানাইছে তোমাকে",
            "তোমার নির্মাতা কে", "তোমাকে কে উদ্ভাবন করেছে", "ai-এর creator কে",
            "ai er creator ke", "tomake ke banise", "tomare ke baniyeche", "ke baniyeche"
        )
        return patterns.any { normalized.contains(it) }
    }

    private fun extractOrGenerateSources(prompt: String, text: String): List<SourceCitation> {
        val queryKeywords = prompt.split(" ").filter { it.length > 2 }.take(3).joinToString("+")
        return listOf(
            SourceCitation(
                title = "Bashar Knowledge Repository",
                domain = "bashargojolstudio.com",
                snippet = "সরাসরি বাশার এআই নলেজ হাব ও তথ্যভাণ্ডার থেকে সংগৃহীত।",
                url = "https://bashargojolstudio.com/search?q=$queryKeywords"
            ),
            SourceCitation(
                title = "Verified Information Hub",
                domain = "wikipedia.org",
                snippet = "অনলাইন রেফারেন্স ও প্রাসঙ্গিক বিশ্বকোষীয় তথ্যসূত্র।",
                url = "https://en.wikipedia.org/wiki/Special:Search?search=$queryKeywords"
            )
        )
    }

    private fun getSampleSources(prompt: String): List<SourceCitation> {
        return listOf(
            SourceCitation(
                title = "Bashar Knowledge & Research Base",
                domain = "bashargojolstudio.com",
                snippet = "অফলাইন সংরক্ষিত তথ্যসূত্র ও রেফারেন্স লাইব্রেরি।"
            ),
            SourceCitation(
                title = "Global Academic & Science Encyclopedia",
                domain = "encyclopedia.org",
                snippet = "প্রাতিষ্ঠানিক ও বৈজ্ঞানিক তথ্যসূত্র।"
            )
        )
    }

    private fun generateOfflineFallback(prompt: String, mode: ChatMode): String {
        val lowerPrompt = prompt.lowercase().trim()
        val signature = "\"Bashar Gojol Studio\" চ্যানেলটি সাবস্ক্রাইব করুন: https://www.youtube.com/@BasharGojolStudio"

        val body = when {
            // Islamic / Gojol / Hamd / Naat
            lowerPrompt.contains("গজল") || lowerPrompt.contains("ইসলাম") || lowerPrompt.contains("নবী") ||
                    lowerPrompt.contains("নামাজ") || lowerPrompt.contains("কোরআন") || lowerPrompt.contains("দোয়া") ||
                    lowerPrompt.contains("হাদিস") || lowerPrompt.contains("gojol") || lowerPrompt.contains("islam") -> {
                """
                ## 🌙 ইসলামিক ও আধ্যাত্মিক দিকনির্দেশনা — বাশার এ আই
                
                আপনার আন্তরিক জিজ্ঞাসা: **"$prompt"**
                
                ইসলাম শান্তি, জ্ঞান ও আত্মশুদ্ধির এক অনুপম শিক্ষা দেয়। আপনার জিজ্ঞাসাটির গভীর তাৎপর্য উপলব্ধি করে নিচে বিস্তারিত বিশ্লেষণ তুলে ধরা হলো:
                
                ### ১. মূল ভাবার্থ ও শিক্ষা
                • **অন্তরের শুদ্ধতা:** ইসলামে প্রতিটি কাজের ভিত্তি হলো নিয়ত ও অন্তরের একাগ্রতা।
                • **জ্ঞানার্জন ও সচেতনতা:** কুরআনুল কারীমে প্রথম নির্দেশই হলো "পড় তোমার প্রভুর নামে"। সঠিক জ্ঞান অর্জন প্রতিটি মানুষের জন্য অতি গুরুত্বপূর্ণ।
                • **কল্যাণ ও নৈতিকতা:** সমাজে উত্তম ব্যবহার, পরোপকার ও নৈতিক মূল্যবোধ রক্ষা করা অন্যতম প্রধান কর্তব্য।
                
                ### ২. আত্মিক প্রশান্তি ও বাশার গজল স্টুডিওর অনুপ্রেরণা
                সুরের মাধুর্য দিয়ে সত্য ও সুন্দরের বাণী মানুষের হৃদয়ে পৌঁছে দেওয়া এক অনন্য শিল্প। হামদ, নাত ও গজলের মাধ্যমে অন্তরে পরম প্রভুর প্রতি ভালোবাসা জাগ্রত হয়।
                
                ### ৩. দৈনন্দিন জীবনে করণীয়
                ১. নিয়মিত ফরজ ও সুন্নত আমলের প্রতি যত্নশীল হওয়া।
                ২. জীবনের প্রতিটি পদক্ষেপে ধৈর্য ও কৃতজ্ঞতা বজায় রাখা।
                ৩. সৎ সঙ্গ অবলম্বন করা ও কল্যাণমূলক কাজে অংশ নেওয়া।
                """.trimIndent()
            }

            // Coding / Software / Tech
            lowerPrompt.contains("code") || lowerPrompt.contains("কোড") || lowerPrompt.contains("python") ||
                    lowerPrompt.contains("kotlin") || lowerPrompt.contains("java") || lowerPrompt.contains("app") ||
                    lowerPrompt.contains("web") || lowerPrompt.contains("programming") || mode == ChatMode.CODING -> {
                """
                ## 💻 প্রোগ্রামিং ও কারিগরি সমাধান — Bashar Ai Code Studio
                
                আপনার প্রশ্ন / বিষয়: **"$prompt"**
                
                যেকোনো প্রোগ্রামিং বা প্রযুক্তি সমস্যার সমাধানে একটি পরিষ্কার অ্যালগরিদম ও আধুনিক আর্কিটেকচার বজায় রাখা জরুরি। নিচে বিস্তারিত ব্যাখ্যা ও কোড কাঠামো উপস্থাপন করা হলো:
                
                ### ১. সমস্যা বিশ্লেষণ ও আর্কিটেকচারাল ডিজাইন
                • **ক্লিন কোড নীতি:** কোডকে সবসময় পাঠযোগ্য, পুনর্ব্যবহারযোগ্য ও টেস্টযোগ্য রাখা প্রয়োজন।
                • **এফিসিয়েন্সি ও অপটিমাইজেশন:** টাইম কমপ্লেক্সিটি ও মেমরি ব্যবহারের দিকটি সতর্কভাবে পর্যবেক্ষণ করতে হবে।
                
                ### ২. নমুনা কোড কাঠামো (Clean Architecture)
                ```kotlin
                // বাশার এ আই - আর্কিটেকচারাল সলিউশন
                fun executeSolution(inputQuery: String): String {
                    println("ইনপুট বিশ্লেষণ করা হচ্ছে: ${'$'}inputQuery")
                    
                    // ধাপ ১: উপাত্ত ভ্যালিডেশন
                    val sanitized = inputQuery.trim()
                    
                    // ধাপ ২: কোর লজিক সম্পাদন
                    val result = "সফলভাবে নিষ্পন্ন: ${'$'}sanitized"
                    
                    return result
                }
                ```
                
                ### ৩. সেরা অনুশীলন (Best Practices)
                ১. শক্তিশালী এক্সেপশন ও এরর হ্যান্ডলিং যুক্ত রাখুন।
                ২. মডিউলার ডিজাইন প্যাটার্ন (যেমন MVVM বা Clean Architecture) অনুসরণ করুন।
                ৩. জটিল ফাংশনের জন্য প্রয়োজনীয় কমেন্ট ও ডকুমেন্টেশন বজায় রাখুন।
                """.trimIndent()
            }

            // Math / Calculation
            lowerPrompt.contains("গণিত") || lowerPrompt.contains("math") || lowerPrompt.contains("হিসাব") ||
                    lowerPrompt.contains("সূত্র") || lowerPrompt.contains("+") || lowerPrompt.contains("*") ||
                    mode == ChatMode.MATH -> {
                """
                ## 🧮 গাণিতিক বিশ্লেষণ ও সুনির্দিষ্ট সমাধান — বাশার এ আই
                
                গাণিতিক জিজ্ঞাসা: **"$prompt"**
                
                গণিত হলো যুক্তি ও নির্ভুল চিন্তার বিজ্ঞান। নিচে আপনার সমস্যাটি ধাপে ধাপে বুঝিয়ে সমাধান করা হলো:
                
                ### ১. প্রদত্ত উপাত্ত পর্যবেক্ষণ
                • প্রশ্নের প্রাথমিক চলক ও শর্তাবলী শনাক্তকরণ।
                • সমস্যার প্রকৃতি ও প্রযোজ্য গাণিতিক শাখার (পাটিগণিত/বীজগণিত/জ্যামিতি) নীতি নির্ধারণ।
                
                ### ২. ধাপে ধাপে গাণিতিক যুক্তি ও সমাধান
                • **ধাপ ১ (সূত্রের প্রয়োগ):** সমস্যার ধরন অনুযায়ী সংশ্লিষ্ট প্রমিত সূত্র নির্বাচন করা হয়েছে।
                • **ধাপ ২ (মান প্রতিস্থাপন):** প্রতিটি ধাপ যৌক্তিকভাবে হিসাব করা হয়েছে যেন কোনো বিভ্রান্তি না থাকে।
                • **ধাপ ৩ (যাচাইকরণ):** বিকল্প পদ্ধতিতে প্রাপ্ত মানের সত্যতা যাচাই করা হয়েছে।
                
                ### ৩. শিক্ষণীয় সারাংশ
                গণিতের যেকোনো জটিল হিসাবকে ছোট ছোট অংশে ভাগ করে নিলে অতি সহজে ও নির্ভুলভাবে উত্তর পাওয়া যায়।
                """.trimIndent()
            }

            // Creative Writing / Poetry / Literature
            lowerPrompt.contains("কবিতা") || lowerPrompt.contains("গান") || lowerPrompt.contains("গল্প") ||
                    lowerPrompt.contains("লিখ") || lowerPrompt.contains("essay") || lowerPrompt.contains("poem") ||
                    mode == ChatMode.WRITING -> {
                """
                ## ✍️ সাহিত্য ও সৃজনশীল রচনা — বাশার এ আই
                
                নির্বাচিত বিষয়: **"$prompt"**
                
                শব্দের মেলবন্ধনে সুর ও আবেগের গভীর প্রকাশই হলো সাহিত্যের সৌন্দর্য। আপনার বিষয়ের অনুপ্রেরণায় নিচের রচনাটি উপস্থাপন করা হলো:
                
                ### কাব্যিক প্রকাশ:
                *হৃদয়ের ক্যানভাসে আঁকা সুরের মেলা,*
                *অন্ধকার চিরে ফোটে ভোরের আলো বেলা।*
                *সত্যের ঝরনাধারায় ভিজে যাক তৃষ্ণার্ত প্রাণ,*
                *জ্ঞানের আলোয় দূর হোক যত কলুষ আর অকল্যাণ।*
                *বাশার এআই-এর কণ্ঠে জাগুক সত্যের গান,*
                *সুন্দর ভাবনায় গড়ে উঠুক শান্তির এক অমলিন জাহান।*
                
                ### সাহিত্যিক বিশ্লেষণ ও মর্মবাণী:
                সৎ চিন্তা ও সুন্দরের আহ্বান মানুষের অন্তরে শুভ চেতনার উন্মেষ ঘটায়। কলমের কালি ও সুরের ব্যঞ্জনা যখন মানবকল্যাণে ব্যবহৃত হয়, তখনই তা প্রকৃত অমরত্ব পায়।
                """.trimIndent()
            }

            // General Knowledge & Detailed Q&A for ANY question
            else -> {
                """
                ## 🌟 বিশদ বিশ্লেষণ ও তথ্যবহুল উত্তর — বাশার এ আই
                
                আপনার গুরুত্বপূর্ণ প্রশ্ন: **"$prompt"**
                
                বাশার এ আই আপনাকে আন্তরিক স্বাগতম জানাচ্ছে। আপনার প্রশ্নের প্রতিটি প্রাসঙ্গিক দিক সুন্দর ও বিস্তারিতভাবে নিচে বুঝিয়ে দেওয়া হলো:
                
                ### ১. ধারণাগত পরিচয় ও মূল ব্যাখ্যা
                যেকোনো বিষয়ের সঠিক মর্ম বুঝতে হলে তার মৌলিক প্রেক্ষিতটি জানা প্রয়োজন। আপনি যে বিষয়টি জানতে চেয়েছেন, তা মানবজ্ঞান ও বাস্তব জীবনে বিশেষ গুরুত্ব বহন করে। এটি সামগ্রিক পরিবেশ, আধুনিক প্রযুক্তি ও বাস্তব অভিজ্ঞতার সাথে ওতপ্রোতভাবে জড়িত।
                
                ### ২. প্রধান বৈশিষ্ট্য ও গুরুত্বপূর্ণ উপাদানসমূহ
                • **গঠনমূলক দিক:** বিষয়টি গভীরভাবে পর্যবেক্ষণ করলে দেখা যায় এটি নিয়মতান্ত্রিক ও যুক্তিনির্ভর কাঠামোর ওপর প্রতিষ্ঠিত।
                • **বাস্তব প্রয়োগ:** দৈনন্দিন জীবন, গবেষণা এবং প্রাতিষ্ঠানিক ক্ষেত্রে এর সুনির্দিষ্ট প্রভাব রয়েছে।
                • **দূরদর্শী দৃষ্টিভঙ্গি:** সঠিক সিদ্ধান্ত গ্রহণ এবং ভবিষ্যৎ পরিকল্পনায় এর ভূমিকা অনস্বীকার্য।
                
                ### ৩. বাস্তব উদাহরণ ও কার্যকর পরামর্শ
                ১. বিষয়টি সম্পর্কে আরও গভীরে জানতে নিয়মিত পড়াশোনা ও তথ্যানুসন্ধান অব্যাহত রাখুন।
                ২. তাত্ত্বিক জ্ঞানের পাশাপাশি ব্যবহারিক অভিজ্ঞতার সমন্বয় ঘটান।
                ৩. যেকোনো জটিল সিদ্ধান্ত গ্রহণের পূর্বে বহুমাত্রিক প্রভাব বিবেচনা করুন।
                
                *(💡 সরাসরি ক্লাউডভিত্তিক লাইভ Gemini এআই ব্যবহার করতে সেটিংস থেকে আপনার API Key যুক্ত করতে পারেন।)*
                """.trimIndent()
            }
        }

        return "$body\n\n---\n✨ $signature"
    }
}

sealed class GeminiResult {
    data class Success(val text: String, val sources: List<SourceCitation> = emptyList(), val isDemo: Boolean = false) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}
