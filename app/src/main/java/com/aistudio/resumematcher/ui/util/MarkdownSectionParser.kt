package com.aistudio.resumematcher.ui.util

data class ParsedSection(
    val title: String,
    val icon: String, // "bar_chart", "check_circle", "warning", "build", "lightbulb", "rocket"
    val content: String
)

object MarkdownSectionParser {
    fun parse(markdown: String): List<ParsedSection> {
        if (markdown.isBlank()) return emptyList()

        val sections = mutableListOf<ParsedSection>()
        
        // Define known heading patterns
        val headingPatterns = listOf(
            Triple("📊 Overview & Match Score", "bar_chart", listOf("Overview", "Match Score")),
            Triple("✅ Strong Matches (Key Strengths)", "check_circle", listOf("Strong Matches", "Key Strengths")),
            Triple("⚠️ Gaps & Areas of Improvement", "warning", listOf("Gaps", "Areas of Improvement", "Area of Improvement")),
            Triple("🛠️ GitHub & Project Analysis", "build", listOf("GitHub", "Project Analysis")),
            Triple("💡 Recommendation for Application", "lightbulb", listOf("Recommendation", "Application")),
            Triple("🚀 ATS & AI Optimization", "rocket", listOf("ATS", "AI Optimization"))
        )

        val lines = markdown.lines()
        var currentSectionTitle = ""
        var currentSectionIcon = "description"
        val currentContent = StringBuilder()

        for (line in lines) {
            val cleanLine = line.trim()
            val isHeading = cleanLine.startsWith("##") || (cleanLine.startsWith("#") && !cleanLine.startsWith("##"))
            
            if (isHeading) {
                // Find matching pattern
                val matchedHeading = headingPatterns.firstOrNull { pattern ->
                    pattern.third.any { keyword -> cleanLine.contains(keyword, ignoreCase = true) }
                }

                if (matchedHeading != null) {
                    // Save previous section
                    if (currentSectionTitle.isNotEmpty() && currentContent.trim().isNotEmpty()) {
                        sections.add(
                            ParsedSection(
                                title = currentSectionTitle,
                                icon = currentSectionIcon,
                                content = currentContent.toString().trim()
                            )
                        )
                    }
                    currentSectionTitle = matchedHeading.first
                    currentSectionIcon = matchedHeading.second
                    currentContent.clear()
                } else {
                    currentContent.append(line).append("\n")
                }
            } else {
                currentContent.append(line).append("\n")
            }
        }

        // Add the last section
        if (currentSectionTitle.isNotEmpty() && currentContent.trim().isNotEmpty()) {
            sections.add(
                ParsedSection(
                    title = currentSectionTitle,
                    icon = currentSectionIcon,
                    content = currentContent.toString().trim()
                )
            )
        }

        // Fallback: If no structured sections were captured, return the entire text as "Full Screening Report"
        if (sections.isEmpty() && markdown.isNotBlank()) {
            sections.add(ParsedSection("Full Screening Report", "description", markdown))
        }

        return sections
    }
}
