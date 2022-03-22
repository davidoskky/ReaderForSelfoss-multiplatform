package bou.amine.apps.readerforselfossv2.android.api.selfoss

import android.app.Activity
import android.content.Context
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.SharedItems
import bou.amine.apps.readerforselfossv2.android.utils.getUnsafeHttpClient
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.basic.BasicAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class SelfossApi(
    c: Context,
    callingActivity: Activity?,
    isWithSelfSignedCert: Boolean,
    timeout: Long
) {

    private lateinit var service: SelfossService
    private val config: Config = Config(c)
    private val userName: String
    private val password: String

    fun OkHttpClient.Builder.maybeWithSelfSigned(isWithSelfSignedCert: Boolean): OkHttpClient.Builder =
        if (isWithSelfSignedCert) {
            getUnsafeHttpClient()
        } else {
            this
        }

    fun OkHttpClient.Builder.maybeWithSettingsTimeout(timeout: Long): OkHttpClient.Builder =
        if (timeout != -1L) {
            this.readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS)
        } else {
            this
        }

    fun Credentials.createAuthenticator(): DispatchingAuthenticator =
        DispatchingAuthenticator.Builder()
            .with("digest", DigestAuthenticator(this))
            .with("basic", BasicAuthenticator(this))
            .build()

    fun DispatchingAuthenticator.getHttpClien(isWithSelfSignedCert: Boolean, timeout: Long): OkHttpClient.Builder {
        val authCache = ConcurrentHashMap<String, CachingAuthenticator>()
        return OkHttpClient
            .Builder()
            .maybeWithSettingsTimeout(timeout)
            .maybeWithSelfSigned(isWithSelfSignedCert)
            .authenticator(CachingAuthenticatorDecorator(this, authCache))
            .addInterceptor(AuthenticationCacheInterceptor(authCache))
            .addInterceptor(object: Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request: Request = chain.request()
                    val response: Response = chain.proceed(request)

                    if (response.code == 408) {
                        return response
                    }
                    return response
                }
            })
    }

    init {
        userName = config.userLogin
        password = config.userPassword

        val authenticator =
            Credentials(
                config.httpUserLogin,
                config.httpUserPassword
            ).createAuthenticator()

        val gson =
            GsonBuilder()
                .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanTypeAdapter())
                .registerTypeAdapter(SelfossTagType::class.java, SelfossTagTypeTypeAdapter())
                .setLenient()
                .create()

        val logging = HttpLoggingInterceptor()


        logging.level = HttpLoggingInterceptor.Level.NONE
        val httpClient = authenticator.getHttpClien(isWithSelfSignedCert, timeout)

        val timeoutCode = 504
        httpClient
                .addInterceptor { chain ->
                    val res = chain.proceed(chain.request())
                    if (res.code == timeoutCode) {
                        throw SocketTimeoutException("timeout")
                    }
                    res
                }
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request()
                    try {
                        chain.proceed(request)
                    } catch (e: SocketTimeoutException) {
                        Response.Builder()
                                .code(timeoutCode)
                                .protocol(Protocol.HTTP_2)
                                .body("".toResponseBody("text/plain".toMediaTypeOrNull()))
                                .message("")
                                .request(request)
                                .build()
                    }
                }

        try {
            val retrofit =
                Retrofit
                    .Builder()
                    .baseUrl(config.baseUrl)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            service = retrofit.create(SelfossService::class.java)
        } catch (e: IllegalArgumentException) {
            if (callingActivity != null) {
                Config.logoutAndRedirect(c, callingActivity, config.settings.edit(), baseUrlFail = true)
            }
        }
    }

    fun login(): Call<SuccessResponse> =
        service.loginToSelfoss(config.userLogin, config.userPassword)

    suspend fun readItems(
        itemsNumber: Int,
        offset: Int
    ): retrofit2.Response<List<Item>> =
        getItems("read", SharedItems.tagFilter, SharedItems.sourceIDFilter, SharedItems.searchFilter, itemsNumber, offset)

    suspend fun newItems(
        itemsNumber: Int,
        offset: Int
    ): retrofit2.Response<List<Item>> =
        getItems("unread", SharedItems.tagFilter, SharedItems.sourceIDFilter, SharedItems.searchFilter, itemsNumber, offset)

    suspend fun starredItems(
        itemsNumber: Int,
        offset: Int
    ): retrofit2.Response<List<Item>> =
        getItems("starred", SharedItems.tagFilter, SharedItems.sourceIDFilter, SharedItems.searchFilter, itemsNumber, offset)

    fun allItems(): Call<List<Item>> =
        service.allItems(userName, password)

    suspend fun allNewItems(): retrofit2.Response<List<Item>> =
            getItems("unread", null, null, null, 200, 0)

    suspend fun allReadItems(): retrofit2.Response<List<Item>> =
            getItems("read", null, null, null, 200, 0)

    suspend fun allStarredItems(): retrofit2.Response<List<Item>> =
        getItems("read", null, null, null, 200, 0)

    private suspend fun getItems(
        type: String,
        tag: String?,
        sourceId: Long?,
        search: String?,
        items: Int,
        offset: Int
    ): retrofit2.Response<List<Item>> =
        service.getItems(type, tag, sourceId, search, null, userName, password, items, offset)

    suspend fun updateItems(
        updatedSince: String
    ): retrofit2.Response<List<Item>> =
        service.getItems("read", null, null, null, updatedSince, userName, password, 200, 0)

    fun markItem(itemId: String): Call<SuccessResponse> =
        service.markAsRead(itemId, userName, password)

    fun unmarkItem(itemId: String): Call<SuccessResponse> =
        service.unmarkAsRead(itemId, userName, password)

    suspend fun readAll(ids: List<String>): SuccessResponse =
        service.markAllAsRead(ids, userName, password)

    fun starrItem(itemId: String): Call<SuccessResponse> =
        service.starr(itemId, userName, password)

    fun unstarrItem(itemId: String): Call<SuccessResponse> =
        service.unstarr(itemId, userName, password)

    suspend fun stats(): retrofit2.Response<Stats> = service.stats(userName, password)

    val tags: Call<List<Tag>>
        get() = service.tags(userName, password)

    fun update(): Call<String> =
        service.update(userName, password)

    val apiVersion: Call<ApiVersion>
        get() = service.version()

    val sources: Call<List<Source>>
        get() = service.sources(userName, password)

    fun deleteSource(id: String): Call<SuccessResponse> =
        service.deleteSource(id, userName, password)

    fun spouts(): Call<Map<String, Spout>> =
        service.spouts(userName, password)

    fun createSource(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): Call<SuccessResponse> =
        service.createSource(title, url, spout, tags, filter, userName, password)

    fun createSourceApi2(
        title: String,
        url: String,
        spout: String,
        tags: List<String>,
        filter: String
    ): Call<SuccessResponse> =
        service.createSourceApi2(title, url, spout, tags, filter, userName, password)
}
