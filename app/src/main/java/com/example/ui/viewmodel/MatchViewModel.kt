package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.MatchRecord
import com.example.data.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MatchViewModel(
    application: Application,
    private val repository: MatchRepository
) : AndroidViewModel(application) {

    // Inputs
    private val _jobTitle = MutableStateFlow("")
    val jobTitle = _jobTitle.asStateFlow()

    private val _candidateName = MutableStateFlow("")
    val candidateName = _candidateName.asStateFlow()

    private val _jobDescription = MutableStateFlow("")
    val jobDescription = _jobDescription.asStateFlow()

    private val _keyResponsibilities = MutableStateFlow("")
    val keyResponsibilities = _keyResponsibilities.asStateFlow()

    private val _keyAccountabilities = MutableStateFlow("")
    val keyAccountabilities = _keyAccountabilities.asStateFlow()

    private val _keyPerformanceIndicators = MutableStateFlow("")
    val keyPerformanceIndicators = _keyPerformanceIndicators.asStateFlow()

    private val _qualifications = MutableStateFlow("")
    val qualifications = _qualifications.asStateFlow()

    private val _candidateProfile = MutableStateFlow("")
    val candidateProfile = _candidateProfile.asStateFlow()

    // UI States
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _isProcessingJobImage = MutableStateFlow(false)
    val isProcessingJobImage = _isProcessingJobImage.asStateFlow()

    private val _isProcessingKeyResponsibilitiesImage = MutableStateFlow(false)
    val isProcessingKeyResponsibilitiesImage = _isProcessingKeyResponsibilitiesImage.asStateFlow()

    private val _isProcessingKeyAccountabilitiesImage = MutableStateFlow(false)
    val isProcessingKeyAccountabilitiesImage = _isProcessingKeyAccountabilitiesImage.asStateFlow()

    private val _isProcessingKeyPerformanceIndicatorsImage = MutableStateFlow(false)
    val isProcessingKeyPerformanceIndicatorsImage = _isProcessingKeyPerformanceIndicatorsImage.asStateFlow()

    private val _isProcessingQualificationsImage = MutableStateFlow(false)
    val isProcessingQualificationsImage = _isProcessingQualificationsImage.asStateFlow()

    private val _isProcessingCandidateImage = MutableStateFlow(false)
    val isProcessingCandidateImage = _isProcessingCandidateImage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _currentAnalysisResult = MutableStateFlow<String?>(null)
    val currentAnalysisResult = _currentAnalysisResult.asStateFlow()

    private val _currentMatchScore = MutableStateFlow<Int?>(null)
    val currentMatchScore = _currentMatchScore.asStateFlow()

    // Selected record details
    private val _selectedRecord = MutableStateFlow<MatchRecord?>(null)
    val selectedRecord = _selectedRecord.asStateFlow()

    // Navigation/Tab state
    private val _activeTab = MutableStateFlow(0) // 0: Analyze, 1: History, 2: About
    val activeTab = _activeTab.asStateFlow()

    // Database flow
    val historyRecords: StateFlow<List<MatchRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateJobTitle(title: String) {
        _jobTitle.value = title
    }

    fun updateCandidateName(name: String) {
        _candidateName.value = name
    }

    fun updateJobDescription(desc: String) {
        _jobDescription.value = desc
    }

    fun updateKeyResponsibilities(value: String) {
        _keyResponsibilities.value = value
    }

    fun updateKeyAccountabilities(value: String) {
        _keyAccountabilities.value = value
    }

    fun updateKeyPerformanceIndicators(value: String) {
        _keyPerformanceIndicators.value = value
    }

    fun updateQualifications(value: String) {
        _qualifications.value = value
    }

    fun updateCandidateProfile(profile: String) {
        _candidateProfile.value = profile
    }

    fun setTab(index: Int) {
        _activeTab.value = index
    }

    fun selectRecord(record: MatchRecord?) {
        _selectedRecord.value = record
        if (record != null) {
            _currentAnalysisResult.value = record.analysisResult
            _currentMatchScore.value = record.matchScore
            _jobTitle.value = record.jobTitle
            _candidateName.value = record.candidateName
            _jobDescription.value = record.jobDescription
            _keyResponsibilities.value = record.keyResponsibilities
            _keyAccountabilities.value = record.keyAccountabilities
            _keyPerformanceIndicators.value = record.keyPerformanceIndicators
            _qualifications.value = record.qualifications
            _candidateProfile.value = record.resumeText
        } else {
            _currentAnalysisResult.value = null
            _currentMatchScore.value = null
            _keyResponsibilities.value = ""
            _keyAccountabilities.value = ""
            _keyPerformanceIndicators.value = ""
            _qualifications.value = ""
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
            if (_selectedRecord.value?.id == id) {
                _selectedRecord.value = null
                _currentAnalysisResult.value = null
                _currentMatchScore.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllRecords()
            _selectedRecord.value = null
            _currentAnalysisResult.value = null
            _currentMatchScore.value = null
        }
    }

    fun analyzeAndMatch() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val jobDesc = _jobDescription.value.trim()
        val keyResp = _keyResponsibilities.value.trim()
        val keyAcc = _keyAccountabilities.value.trim()
        val keyKpi = _keyPerformanceIndicators.value.trim()
        val qual = _qualifications.value.trim()
        val candidateProf = _candidateProfile.value.trim()
        val title = _jobTitle.value.trim().ifEmpty { "IT Position" }
        val name = _candidateName.value.trim().ifEmpty { "Applicant" }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _errorMessage.value = "Gemini API Key is not set or invalid. Please configure it in the Secrets panel in AI Studio."
            return
        }

        if (jobDesc.isEmpty() && keyResp.isEmpty() && keyAcc.isEmpty() && keyKpi.isEmpty() && qual.isEmpty()) {
            _errorMessage.value = "Please provide at least one job detail or job description field."
            return
        }

        if (candidateProf.isEmpty()) {
            _errorMessage.value = "Please provide Candidate Profile / Resume / GitHub info."
            return
        }

        _isAnalyzing.value = true
        _errorMessage.value = null
        _currentAnalysisResult.value = null
        _currentMatchScore.value = null
        _selectedRecord.value = null

        val fullJobDesc = buildString {
            if (jobDesc.isNotEmpty()) {
                append(jobDesc)
            }
            if (keyResp.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append("### The key responsibilities for this role\n")
                append(keyResp)
            }
            if (keyAcc.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append("### Key Accountabilities\n")
                append(keyAcc)
            }
            if (keyKpi.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append("### Key Performance Indicators\n")
                append(keyKpi)
            }
            if (qual.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append("### Qualifications\n")
                append(qual)
            }
        }.trim()

        viewModelScope.launch {
            try {
                val analysisText = repository.generateAnalysis(
                    apiKey = apiKey,
                    jobDescription = fullJobDesc,
                    candidateProfile = candidateProf
                )

                val score = extractMatchScore(analysisText)
                _currentMatchScore.value = score
                _currentAnalysisResult.value = analysisText

                // Save to historical records automatically
                val record = MatchRecord(
                    candidateName = name,
                    jobTitle = title,
                    matchScore = score,
                    resumeText = candidateProf,
                    jobDescription = jobDesc,
                    keyResponsibilities = keyResp,
                    keyAccountabilities = keyAcc,
                    keyPerformanceIndicators = keyKpi,
                    qualifications = qual,
                    analysisResult = analysisText
                )
                repository.insertRecord(record)

            } catch (e: Exception) {
                _errorMessage.value = "Screening Failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun extractMatchScore(text: String): Int? {
        try {
            // 1. Try to find the precise machine-readable comment <!--SCORE:85-->
            val commentRegex = Regex("<!--SCORE:.*?(\\d+)-->")
            val commentMatch = commentRegex.find(text)
            if (commentMatch != null) {
                val scoreStr = commentMatch.groupValues[1]
                return scoreStr.toInt().coerceIn(0, 100)
            }

            // 2. Try to find standard "Match Score: 85" patterns
            val regex = Regex("Match Score:.*?(\\d+)")
            val matchResult = regex.find(text)
            if (matchResult != null) {
                val scoreStr = matchResult.groupValues[1]
                return scoreStr.toInt().coerceIn(0, 100)
            }

            // 3. Fallback: look for any percentage number in the response
            val percentRegex = Regex("(\\d+)%")
            val percentMatch = percentRegex.find(text)
            if (percentMatch != null) {
                return percentMatch.groupValues[1].toInt().coerceIn(0, 100)
            }
        } catch (e: Exception) {
            // Ignore and return null
        }
        return null // No random fallback, show as indeterminate instead
    }

    // Presets for easy testing
    val presetJobs = listOf(
        JobPreset(
            title = "Senior Full-Stack Developer (Kotlin/Node.js)",
            description = """
                We are looking for a Senior Full-Stack Developer to lead our enterprise portal team.
                
                Requirements:
                - 5+ years of experience in Software Development.
                - Strong expertise in Kotlin, Java, and Jetpack Compose for mobile or backend.
                - Expertise in Node.js, Express, and NestJS for backend microservices.
                - Strong understanding of SQL (PostgreSQL, MySQL) and NoSQL databases.
                - Knowledge of cloud architecture (GCP, AWS) and CI/CD pipelines (Docker, Kubernetes).
                - Excellent problem-solving skills and communication.
            """.trimIndent()
        ),
        JobPreset(
            title = "DevOps & Cloud Infrastructure Specialist",
            description = """
                Seeking an experienced DevOps Engineer to design, scale, and maintain our cloud-native infrastructure.
                
                Requirements:
                - Proven experience as a DevOps Engineer, SRE, or Infrastructure Engineer.
                - Expert level in Containerization: Docker, Kubernetes (CKA certification is a major plus).
                - Experience with Infrastructure as Code (IaC) using Terraform.
                - Strong scripting skills in Bash, Python, or Go.
                - Direct experience with Google Cloud Platform (GCP) or AWS cloud-native components.
                - Mastery of CI/CD systems like GitHub Actions, GitLab CI, or Jenkins.
            """.trimIndent()
        ),
        JobPreset(
            title = "AI/ML Engineer (NLP & GenAI integrations)",
            description = """
                We are recruiting an AI/ML Specialist to integrate advanced generative AI systems into our corporate tools.
                
                Requirements:
                - 3+ years working in Data Science, Machine Learning, or AI engineering.
                - Proficient in Python, TensorFlow, PyTorch, or HuggingFace ecosystem.
                - Practical experience with Large Language Models (LLMs) and Vector Databases (Pinecone, ChromaDB).
                - Strong experience in prompt engineering, retrieval-augmented generation (RAG), and agentic systems.
                - Experience deploying models to production using FastAPI, Docker, and GCP.
            """.trimIndent()
        )
    )

    val presetCandidates = listOf(
        CandidatePreset(
            name = "Niphan S.",
            profile = """
                ### RESUME
                Niphan S. - Senior Full-Stack Software Engineer
                Email: niphan.dev@gmail.com | GitHub: github.com/niphans-dev
                
                SUMMARY:
                High-performing Full-Stack Developer with over 6 years of experience building scalable web and mobile applications. Specializes in Kotlin, Node.js, and GCP-native architecture.
                
                WORK EXPERIENCE:
                - Senior Software Engineer at TechGlobal Inc. (2022 - Present)
                  * Designed and migrated a legacy backend to Node.js microservices, improving request processing speed by 40%.
                  * Developed and styled responsive dashboards using React and Jetpack Compose for Android admin client.
                - Full-Stack Developer at InnovateLab (2020 - 2022)
                  * Built and deployed Node.js REST APIs serving 100k+ active daily users.
                  * Integrated GCP Pub/Sub for asynchronous event-driven notifications.
                
                SKILLS:
                - Languages: Kotlin, JavaScript, TypeScript, Python, HTML/CSS
                - Frameworks: React, Node.js, Express, NestJS, Jetpack Compose
                - Databases: PostgreSQL, MongoDB, Redis
                - Tools: Docker, Kubernetes, Terraform, Git, GCP, AWS
                
                ### GITHUB REPOSITORIES
                - repo-1 (infra-planner-ai): An AI-driven cloud infrastructure visualizer and planning tool. Built with Python, Node.js, Docker, and React.
                - repo-2 (compose-dashboard): Responsive Material 3 design template for Android platforms. Built with Kotlin and Jetpack Compose.
                
                ### CERTIFICATIONS
                - AWS Certified Solutions Architect - Associate
                - Google Cloud Certified Professional Cloud Developer
                - TOEIC Score: 885
            """.trimIndent()
        ),
        CandidatePreset(
            name = "Arisara K.",
            profile = """
                ### RESUME
                Arisara K. - Cloud Infrastructure & Site Reliability Engineer
                Email: arisara.cloud@outlook.com | GitHub: github.com/arisara-ops
                
                SUMMARY:
                Certified Kubernetes Administrator (CKA) with 4 years of dedicated experience automating cloud deployments and optimizing system reliability in production environments.
                
                WORK EXPERIENCE:
                - SRE / Cloud Engineer at CloudVantage (2023 - Present)
                  * Provisioned and maintained production Kubernetes clusters hosting 150+ microservices on AWS EKS and GCP GKE.
                  * Authored declarative Terraform configs to manage infrastructure as code, reducing manual environment setup times by 90%.
                - Systems Administrator at SecureNetwork Co. (2021 - 2023)
                  * Automated system maintenance tasks using Bash and Python scripts, cutting down administrative overhead.
                  * Administered Linux servers, firewalls, and proxy systems.
                
                SKILLS:
                - Platforms: GCP, AWS, Linux, Windows Server
                - Containerization & IaC: Docker, Kubernetes (EKS, GKE, K8s bare-metal), Terraform, Ansible
                - Languages & Scripting: Python, Bash, Go
                - CI/CD: GitHub Actions, Jenkins, Helm charts
                
                ### GITHUB REPOSITORIES
                - repo-ops (k8s-multi-cluster-manager): Advanced Kubernetes cluster automation script suite. Uses Go and Python to manage cross-cloud workloads.
                - repo-terraform (gcp-secure-sandbox): Secure, repeatable landing-zone deployment configurations using Terraform.
                
                ### CERTIFICATIONS
                - Certified Kubernetes Administrator (CKA) - credential ID: CKA-2200451
                - HashiCorp Certified: Terraform Associate
                - TOEIC Score: 790
            """.trimIndent()
        ),
        CandidatePreset(
            name = "Kitti T.",
            profile = """
                ### RESUME
                Kitti T. - Machine Learning Engineer
                Email: kitti.ml@gmail.com | GitHub: github.com/kitti-ai
                
                SUMMARY:
                Passionate AI/ML Engineer focusing on Natural Language Processing (NLP), Large Language Models (LLMs), and Retrieval-Augmented Generation (RAG) frameworks.
                
                WORK EXPERIENCE:
                - ML Engineer at AlphaIntelligence (2024 - Present)
                  * Designed and productionalized a custom RAG chatbot using Pinecone and LangChain, improving HR query satisfaction by 65%.
                  * Developed backend services with Python and FastAPI to expose model inferences securely.
                - Junior Data Scientist at DataCorp (2022 - 2024)
                  * Trained and fine-tuned BERT models for enterprise-level sentiment analysis and topic modeling.
                  * Constructed and optimized ETL data pipelines using Pandas and Apache Spark.
                
                SKILLS:
                - Core: Python, SQL, R, Bash
                - Libraries: PyTorch, HuggingFace, FastAPI, Pandas, NumPy, Scikit-Learn
                - Vector Databases: Pinecone, ChromaDB, Qdrant
                - Cloud/DevOps: Docker, AWS, GCP, FastAPI, Git
                
                ### GITHUB REPOSITORIES
                - repo-rag (rag-document-agent): Production-ready RAG application using Python, LangChain, Pinecone, and FastAPI, complete with high-test-coverage Dockerfiles.
                - repo-ml (sentiment-analyzer): PyTorch model training scripts and serialized model configs for real-time sentiment analysis.
                
                ### CERTIFICATIONS
                - DeepLearning.AI Generative AI with LLMs Specialization
                - TensorFlow Developer Certificate
                - TOEIC Score: 810
            """.trimIndent()
        )
    )

    fun applyPreset(jobIndex: Int, candidateIndex: Int) {
        val job = presetJobs[jobIndex]
        val candidate = presetCandidates[candidateIndex]

        _jobTitle.value = job.title
        _jobDescription.value = job.description

        _candidateName.value = candidate.name
        _candidateProfile.value = candidate.profile

        // Reset output
        _currentAnalysisResult.value = null
        _currentMatchScore.value = null
        _selectedRecord.value = null
    }

    fun extractCandidateFromImage(bitmap: android.graphics.Bitmap) {
        extractCandidateFromImages(listOf(bitmap))
    }

    fun extractCandidateFromImages(bitmaps: List<android.graphics.Bitmap>) {
        if (bitmaps.isEmpty()) return
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _errorMessage.value = "Gemini API Key is not set or invalid. Please configure it in the Secrets panel in AI Studio."
            return
        }

        _isProcessingCandidateImage.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Resize and convert bitmaps to base64
                val base64List = bitmaps.map { bitmapToBase64(it) }
                
                // Call repository
                val extractedText = repository.extractCandidateFromMultipleImages(apiKey, base64List)
                
                // Parse Candidate Name from the text (best effort)
                val lines = extractedText.lines()
                var parsedName = ""
                for (line in lines.take(15)) {
                    val clean = line.replace(Regex("[#*`_\\-]"), "").trim()
                    if (clean.startsWith("Name:", ignoreCase = true) || clean.startsWith("ชื่อ:", ignoreCase = true)) {
                        parsedName = clean.substringAfter(":").trim()
                        break
                    }
                    if (clean.isNotEmpty() && parsedName.isEmpty() && !clean.contains("resume", ignoreCase = true) && !clean.contains("profile", ignoreCase = true) && clean.split(" ").size in 2..4) {
                        parsedName = clean
                    }
                }
                
                if (parsedName.isNotEmpty()) {
                    _candidateName.value = parsedName
                }
                _candidateProfile.value = extractedText
                
            } catch (e: Exception) {
                _errorMessage.value = "Image Parsing Failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isProcessingCandidateImage.value = false
            }
        }
    }

    fun extractJobFromImages(bitmaps: List<android.graphics.Bitmap>) {
        if (bitmaps.isEmpty()) return
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _errorMessage.value = "Gemini API Key is not set or invalid. Please configure it in the Secrets panel in AI Studio."
            return
        }

        _isProcessingJobImage.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Resize and convert bitmaps to base64
                val base64List = bitmaps.map { bitmapToBase64(it) }
                
                // Call repository
                val extractedText = repository.extractJobFromMultipleImages(apiKey, base64List)
                
                // Parse Job Title from the text (best effort)
                val lines = extractedText.lines()
                var parsedTitle = ""
                for (line in lines.take(15)) {
                    val clean = line.replace(Regex("[#*`_\\-]"), "").trim()
                    if (clean.startsWith("Job Title:", ignoreCase = true) || 
                        clean.startsWith("Title:", ignoreCase = true) || 
                        clean.startsWith("ตำแหน่ง:", ignoreCase = true) || 
                        clean.startsWith("ชื่อตำแหน่ง:", ignoreCase = true)) {
                        parsedTitle = clean.substringAfter(":").trim()
                        break
                    }
                }
                
                if (parsedTitle.isNotEmpty()) {
                    _jobTitle.value = parsedTitle
                }
                _jobDescription.value = extractedText
                
            } catch (e: Exception) {
                _errorMessage.value = "Image Parsing Failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isProcessingJobImage.value = false
            }
        }
    }

    private fun extractFieldFromImages(
        bitmaps: List<android.graphics.Bitmap>,
        fieldName: String,
        promptInstruction: String,
        processingFlow: MutableStateFlow<Boolean>,
        targetFlow: MutableStateFlow<String>
    ) {
        if (bitmaps.isEmpty()) return
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _errorMessage.value = "Gemini API Key is not set or invalid. Please configure it in the Secrets panel in AI Studio."
            return
        }

        processingFlow.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val base64List = bitmaps.map { bitmapToBase64(it) }
                val extractedText = repository.extractFieldFromMultipleImages(
                    apiKey = apiKey,
                    base64Images = base64List,
                    fieldName = fieldName,
                    promptInstruction = promptInstruction
                )
                targetFlow.value = extractedText
            } catch (e: Exception) {
                _errorMessage.value = "Image Parsing Failed for $fieldName: ${e.message ?: "Unknown error"}"
            } finally {
                processingFlow.value = false
            }
        }
    }

    fun extractKeyResponsibilitiesFromImages(bitmaps: List<android.graphics.Bitmap>) {
        extractFieldFromImages(
            bitmaps = bitmaps,
            fieldName = "The key responsibilities for this role",
            promptInstruction = "Extract the key duties, tasks, and responsibilities for the job role shown in the document. Arrange them as a clean, sequential bullet-pointed list.",
            processingFlow = _isProcessingKeyResponsibilitiesImage,
            targetFlow = _keyResponsibilities
        )
    }

    fun extractKeyAccountabilitiesFromImages(bitmaps: List<android.graphics.Bitmap>) {
        extractFieldFromImages(
            bitmaps = bitmaps,
            fieldName = "Key Accountabilities",
            promptInstruction = "Extract the main areas of accountability, ownership, or deliverables for the role. Arrange them as a clean, sequential list of key accountability items.",
            processingFlow = _isProcessingKeyAccountabilitiesImage,
            targetFlow = _keyAccountabilities
        )
    }

    fun extractKeyPerformanceIndicatorsFromImages(bitmaps: List<android.graphics.Bitmap>) {
        extractFieldFromImages(
            bitmaps = bitmaps,
            fieldName = "Key Performance Indicators (KPIs)",
            promptInstruction = "Extract the metrics, measurements, or KPIs used to measure performance or success for this position. Arrange them as a clean list.",
            processingFlow = _isProcessingKeyPerformanceIndicatorsImage,
            targetFlow = _keyPerformanceIndicators
        )
    }

    fun extractQualificationsFromImages(bitmaps: List<android.graphics.Bitmap>) {
        extractFieldFromImages(
            bitmaps = bitmaps,
            fieldName = "Qualifications",
            promptInstruction = "Extract the academic qualifications, certifications, years of experience, technical skills, or language requirements for the role. Arrange them neatly as bullet points.",
            processingFlow = _isProcessingQualificationsImage,
            targetFlow = _qualifications
        )
    }

    private fun bitmapToBase64(bitmap: android.graphics.Bitmap, maxSize: Int = 1024): String {
        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (newWidth, newHeight) = if (aspectRatio > 1) {
                maxSize to (maxSize / aspectRatio).toInt()
            } else {
                (maxSize * aspectRatio).toInt() to maxSize
            }
            android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
        val outputStream = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }
}

data class JobPreset(val title: String, val description: String)
data class CandidatePreset(val name: String, val profile: String)

class MatchViewModelFactory(
    private val application: Application,
    private val repository: MatchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
