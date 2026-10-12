package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

// Data classes for Parsed Food Items
@JsonClass(generateAdapter = true)
data class ParsedFoodItem(
    @Json(name = "name") val name: String,
    @Json(name = "weightGrams") val weightGrams: Double,
    @Json(name = "calories") val calories: Double,
    @Json(name = "proteinG") val proteinG: Double,
    @Json(name = "carbsG") val carbsG: Double,
    @Json(name = "fatG") val fatG: Double,
    @Json(name = "fiberG") val fiberG: Double = 0.0,
    @Json(name = "category") val category: String = "Lunch"
)

@JsonClass(generateAdapter = true)
data class ParsedFoodAnalysis(
    @Json(name = "summary") val summary: String,
    @Json(name = "mealType") val mealType: String,
    @Json(name = "items") val items: List<ParsedFoodItem>,
    @Json(name = "totalCalories") val totalCalories: Double,
    @Json(name = "totalProteinG") val totalProteinG: Double,
    @Json(name = "totalCarbsG") val totalCarbsG: Double,
    @Json(name = "totalFatG") val totalFatG: Double,
    @Json(name = "confidence") val confidence: Double = 0.95,
    @Json(name = "nutritionTip") val nutritionTip: String = ""
)

// Data classes for AI Meal Suggestions
@JsonClass(generateAdapter = true)
data class SuggestedMeal(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "calories") val calories: Double,
    @Json(name = "proteinG") val proteinG: Double,
    @Json(name = "carbsG") val carbsG: Double,
    @Json(name = "fatG") val fatG: Double,
    @Json(name = "weightGrams") val weightGrams: Double = 300.0,
    @Json(name = "prepTimeMinutes") val prepTimeMinutes: Int = 15,
    @Json(name = "ingredients") val ingredients: List<String> = emptyList(),
    @Json(name = "healthBenefit") val healthBenefit: String = "",
    @Json(name = "mealType") val mealType: String = "Dinner"
)

@JsonClass(generateAdapter = true)
data class MealSuggestionsResponse(
    @Json(name = "suggestions") val suggestions: List<SuggestedMeal>,
    @Json(name = "reasoning") val reasoning: String = ""
)
