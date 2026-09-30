package com.example.data.model

enum class ChatMode(
    val labelBn: String,
    val labelEn: String,
    val iconEmoji: String,
    val description: String
) {
    QUICK("দ্রুত উত্তর", "Quick Answer", "⚡", "সংক্ষিপ্ত, দ্রুত ও সরাসরি উত্তর"),
    REASONING("গভীর যুক্তি", "Reasoning", "🧠", "ধাপে ধাপে বিশ্লেষণ ও চিন্তাশীল সমাধান"),
    RESEARCH("ডিপ রিসার্চ", "Deep Research", "🔎", "বহুস্তরীয় গবেষণা, তুলনা ও সূত্রসহ রিপোর্ট"),
    CODING("কোডিং", "Coding", "💻", "প্রোগ্রামিং সমস্যা সমাধান, ডিবাগ ও ব্যাখ্যা"),
    STUDY("পড়াশোনা", "Study", "📚", "সহজ উদাহরণসহ বিষয়ভিত্তিক শিক্ষাদান"),
    WRITING("লেখালেখি", "Writing", "✍️", "গজল, কবিতা, স্ক্রিপ্ট ও সৃজনশীল রচনা"),
    DOCUMENT("নথি বিশ্লেষণ", "Document", "📄", "টেক্সট, ফাইল ও সারসংক্ষেপ তৈরি"),
    MATH("গণিত", "Math", "🧮", "সমীকরণ সমাধান ও বিস্তারিত ধাপ")
}

data class SourceCitation(
    val title: String,
    val domain: String,
    val snippet: String,
    val url: String = ""
)

data class ResearchStep(
    val stepIndex: Int,
    val title: String,
    val detail: String,
    val isDone: Boolean = false,
    val isActive: Boolean = false
)

data class AiToolItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val iconEmoji: String,
    val category: String,
    val defaultPrompt: String,
    val inputPlaceholder: String
)

enum class ResponseStyle(val titleBn: String, val titleEn: String) {
    SHORT("সংক্ষিপ্ত", "Short"),
    BALANCED("ভারসাম্যপূর্ণ", "Balanced"),
    DETAILED("বিস্তারিত", "Detailed")
}

enum class AiPersonality(val titleBn: String, val promptInstruction: String) {
    PROFESSIONAL("মার্জিত সহকারী (Professional)", "Maintain an authoritative, elegant, polite, and professional tone."),
    FRIENDLY("বন্ধুভাবাপন্ন (Friendly)", "Be warm, encouraging, conversational, and empathetic."),
    TEACHER("শিক্ষক (Teacher)", "Explain like an inspiring, patient teacher with vivid analogies and easy-to-follow steps."),
    CONCISE("সুনির্দিষ্ট (Concise)", "Be extremely concise, to the point, bulleted, avoiding fluff.")
}

enum class PreferredLanguage(val titleBn: String, val code: String) {
    BANGLA("বাংলা (Bengali)", "bn"),
    ENGLISH("English", "en"),
    AUTO("স্বয়ংক্রিয় (Auto-Detect)", "auto")
}

data class UserPreferences(
    val responseStyle: ResponseStyle = ResponseStyle.BALANCED,
    val personality: AiPersonality = AiPersonality.PROFESSIONAL,
    val language: PreferredLanguage = PreferredLanguage.BANGLA,
    val isDemoMode: Boolean = false,
    val customApiKey: String = "",
    val selectedModel: String = "gemini-3.5-flash",
    val webSearchEnabled: Boolean = true,
    val memoryEnabled: Boolean = true,
    val isDarkTheme: Boolean = true
)
