package com.newthingwidgets.clone.widgets

import com.newthingwidgets.clone.R

class AiAppComboWidgetProvider : BaseComboAppWidgetProvider() {
    override val layoutResId: Int = R.layout.ai_mix_bar3_r

    override val clickMap: Map<Int, String> = mapOf(
        R.id.chat_gpt6 to "ChatGPT",
        R.id.perplex3 to "Perplexity AI",
        R.id.deepseek3 to "DeepSeek",
        R.id.gamini2 to "Gemini Assistant",
        R.id.copilot2 to "Copilot",
        R.id.groock to "Grok"
    )
}
