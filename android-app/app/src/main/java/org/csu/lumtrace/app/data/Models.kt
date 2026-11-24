package org.csu.lumtrace.app.data

data class ApiResult<T>(
    val code: Int = 0,
    val message: String? = null,
    val data: T? = null,
    val timestamp: Long = 0L
) {
    val isSuccess: Boolean get() = code == 200

    fun requireData(): T = data ?: throw ApiException(code, message ?: "服务器未返回数据")
}

class ApiException(val code: Int, override val message: String) : Exception(message)

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null
)

data class LoginData(
    val token: String = "",
    val userId: Long = 0,
    val username: String = "",
    val nickname: String? = null,
    val role: String = "PATIENT"
)

data class UserDto(
    val id: Long = 0,
    val username: String = "",
    val nickname: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val gender: Int = 0,
    val role: String = "PATIENT"
)

data class SymptomDto(
    val id: Long = 0,
    val name: String = "",
    val category: String? = null,
    val description: String? = null,
    val defaultSeverity: Int = 1
)

data class DiagnosisRequest(
    val symptomIds: List<Long>,
    val selfDescription: String? = null,
    val age: Int? = null,
    val gender: Int? = null
)

data class CandidateDto(
    val disease: String = "",
    val score: Double = 0.0,
    val hitSymptoms: List<String> = emptyList()
)

data class DiagnosisResponse(
    val recordId: Long = 0,
    val topDisease: String? = null,
    val confidence: Double = 0.0,
    val severity: Int = 1,
    val suggestion: String? = null,
    val candidates: List<CandidateDto> = emptyList()
)