package org.csu.lumtrace.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.csu.lumtrace.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "lumtrace_prefs")

class TokenStore(private val context: Context) {

    private val tokenKey = stringPreferencesKey("jwt_token")
    private val nicknameKey = stringPreferencesKey("nickname")

    @Volatile
    var cachedToken: String? = null
        private set

    suspend fun restore() {
        cachedToken = context.dataStore.data.map { it[tokenKey] }.first()
    }

    suspend fun save(token: String, nickname: String?) {
        context.dataStore.edit {
            it[tokenKey] = token
            if (nickname != null) it[nicknameKey] = nickname
        }
        cachedToken = token
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedToken = null
    }
}

interface ApiService {

    @POST("user/login")
    suspend fun login(@Body body: LoginRequest): ApiResult<LoginData>

    @POST("user/register")
    suspend fun register(@Body body: RegisterRequest): ApiResult<UserDto>

    @GET("user/me")
    suspend fun me(): ApiResult<UserDto>

    @GET("symptom/list")
    suspend fun symptoms(): ApiResult<List<SymptomDto>>

    @GET("symptom/search")
    suspend fun searchSymptoms(@Query("keyword") keyword: String): ApiResult<List<SymptomDto>>

    @POST("diagnosis/submit")
    suspend fun submitDiagnosis(@Body body: DiagnosisRequest): ApiResult<DiagnosisResponse>

    @GET("diagnosis/history")
    suspend fun history(): ApiResult<List<DiagnosisResponse>>
}

object ApiClient {

    lateinit var tokenStore: TokenStore
        private set

    lateinit var appContext: Context
        private set

    private var api: ApiService? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        tokenStore = TokenStore(context)

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                tokenStore.cachedToken?.let { builder.header("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()

        api = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(ApiService::class.java)
    }

    fun service(): ApiService = api ?: error("ApiClient 未初始化")
}

class AuthRepository(
    private val api: ApiService = ApiClient.service(),
    private val tokenStore: TokenStore = ApiClient.tokenStore
) {

    suspend fun login(username: String, password: String): LoginData {
        val result = api.login(LoginRequest(username, password))
        if (!result.isSuccess) throw ApiException(result.code, result.message ?: "登录失败")
        val data = result.requireData()
        tokenStore.save(data.token, data.nickname)
        return data
    }

    suspend fun register(username: String, password: String, nickname: String?, phone: String?): UserDto {
        val result = api.register(RegisterRequest(username, password, nickname, phone))
        if (!result.isSuccess) throw ApiException(result.code, result.message ?: "注册失败")
        return result.requireData()
    }

    suspend fun logout() = tokenStore.clear()
}