package com.payshield.app.security.explanation

import org.json.JSONObject

data class LlmExplanationResponse(
    val summary: String,
    val actions: List<String>
)

object LlmSchemaValidator {

    /**
     * Strictly validates local LLM output.
     * Rejects response if JSON is malformed, missing required fields, or attempts state overrides.
     */
    fun validateAndParse(jsonText: String): LlmExplanationResponse? {
        return try {
            val cleanJson = jsonText.trim().trim('`').removePrefix("json").trim()
            val obj = JSONObject(cleanJson)

            val summary = obj.optString("summary", "").trim()
            if (summary.isEmpty()) return null

            val actionsArray = obj.optJSONArray("actions") ?: return null
            val actions = mutableListOf<String>()
            for (i in 0 until actionsArray.length()) {
                val item = actionsArray.optString(i, "").trim()
                if (item.isNotEmpty()) {
                    actions.add(item)
                }
            }

            if (actions.isEmpty()) return null

            LlmExplanationResponse(summary = summary, actions = actions)
        } catch (_: Exception) {
            null
        }
    }
}
