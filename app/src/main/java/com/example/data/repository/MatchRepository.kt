package com.example.data.repository

import com.example.data.api.GenerateContentRequest
import com.example.data.api.Content
import com.example.data.api.Part
import com.example.data.api.InlineData
import com.example.data.api.GenerationConfig
import com.example.data.api.RetrofitClient
import com.example.data.local.MatchRecordDao
import com.example.data.model.MatchRecord
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class MatchRepository(private val matchRecordDao: MatchRecordDao) {

    val allRecords: Flow<List<MatchRecord>> = matchRecordDao.getAllMatchRecords()

    suspend fun getRecordById(id: Int): MatchRecord? {
        return matchRecordDao.getMatchRecordById(id)
    }

    suspend fun insertRecord(record: MatchRecord): Long {
        return matchRecordDao.insertMatchRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        matchRecordDao.deleteMatchRecordById(id)
    }

    suspend fun clearAllRecords() {
        matchRecordDao.clearAllRecords()
    }

    suspend fun generateAnalysis(
        apiKey: String,
        jobDescription: String,
        candidateProfile: String
    ): String {
        val systemPrompt = """
            You are an expert IT Recruitment Consultant and ATS (Applicant Tracking System) Specialist. Your task is to perform a "Smart Match" analysis between a candidate's profile and a specific job description. 
            
            Analyze the provided Candidate Information against the Job Description. Be objective, thorough, and precise.
            
            CRITICAL INSTRUCTION FOR PARSING: You MUST start your response with a machine-readable HTML comment holding the calculated match score percentage, exactly like this: <!--SCORE:85--> (where 85 is the calculated score between 0 and 100 based on the skills and requirements met). Do not omit this!
            
            Please provide the output in the following format (Strictly use Markdown with the exact section headings):
            
            ## 📊 Overview & Match Score
            - **Match Score:** [Provide a percentage matching score, e.g., 85%]
            - **Executive Summary:** [A 2-3 sentence summary explaining why this candidate is or isn't a good fit for this role.]
            
            ## ✅ Strong Matches (Key Strengths)
            - [Bullet points of skills, projects, or certifications that perfectly align with the job requirements. Cite specific examples from the candidate's GitHub or Resume.]
            
            ## ⚠️ Gaps & Areas of Improvement
            - [Bullet points of missing skills, technical stacks, or experience requested in the job description that the candidate lacks or hasn't explicitly shown.]
            
            ## 🛠️ GitHub & Project Analysis
            - [Analyze the candidate's GitHub repositories. Highlight how their practical projects and code align with the technology stack required for this job.]
            
            ## 💡 Recommendation for Application
            - **Resume Tailoring Tips:** [How should the candidate update their resume to better fit this specific role?]
            - **Interview Preparation:** [What technical topics or past experiences should the candidate highlight during the interview?]
            
            ## 🚀 ATS & AI Optimization (How to stand out to HR AI)
            - **Top Keywords to Include:** [Identify 5-10 specific technical and soft skill keywords from the Job Description that the candidate MUST insert into their resume to pass the AI screening.]
            - **Impact Statement Rewriting:** [Take 2-3 current bullet points from the candidate's resume/projects and rewrite them using the "X-Y-Z formula" (Accomplished [X] as measured by [Y], by doing [Z]) to maximize AI scoring for impact.]
            - **GitHub Highlight Strategy for AI:** [How should the candidate present their GitHub projects (e.g., specific wording in the resume or README) so that an AI resume parser recognizes it as production-ready, professional experience? Create a brief, modular section detailing tech stacks, key features, and outcomes.]
        """.trimIndent()

        val prompt = """
            ### 🎯 [JOB DESCRIPTION]
            $jobDescription
            
            ---
            
            ### 📄 [CANDIDATE PROFILE]
            $candidateProfile
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.2f)
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (textResponse != null) {
            return textResponse
        } else {
            throw Exception("Received an empty response from Gemini API.")
        }
    }

    suspend fun extractCandidateFromImage(
        apiKey: String,
        base64Image: String,
        mimeType: String = "image/jpeg"
    ): String {
        val prompt = """
            Analyze the provided document image (which could be a resume, certificate, transcript, or candidate profile).
            Extract all relevant candidate details and format it as a professional Resume/Profile in plain text.
            Include:
            - Full Name
            - Contact Info (if visible)
            - Education / Grades (if visible)
            - Professional Experience (if visible)
            - Skills & Core Technologies (if visible)
            - Certifications (if visible)
            
            Strictly return ONLY the extracted information formatted in clean Markdown. Do NOT include any intro, outro, conversational fillers, or explanations. Just start directly with the candidate's name or title.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(
                Part(text = prompt),
                Part(inlineData = InlineData(mimeType = mimeType, data = base64Image))
            ))),
            generationConfig = GenerationConfig(temperature = 0.1f)
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (textResponse != null) {
            return textResponse
        } else {
            throw Exception("Received an empty response from Gemini API.")
        }
    }
}
