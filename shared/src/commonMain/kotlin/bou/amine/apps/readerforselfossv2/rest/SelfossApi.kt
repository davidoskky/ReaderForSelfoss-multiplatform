package bou.amine.apps.readerforselfossv2.rest

import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SelfossApi(private val apiDetailsService: ApiDetailsService) {

    var client = createHttpClient()

    private fun createHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                install(HttpCache)
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        apiDetailsService.logApiCalls(message)
                    }
                }
                level = LogLevel.ALL
            }
            /* TODO: Auth as basic
            if (apiDetailsService.getUserName().isNotEmpty() && apiDetailsService.getPassword().isNotEmpty()) {

                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = apiDetailsService.getUserName(), password = apiDetailsService.getPassword())
                        }
                        sendWithoutRequest {
                            true
                        }
                    }
                }
            }*/
            expectSuccess = false
        }
    }

    fun url(path: String) =
        "${apiDetailsService.getBaseUrl()}$path"

    fun refreshLoginInformation() {
        apiDetailsService.refresh()
        client = createHttpClient()
    }

    suspend fun login(): SelfossModel.SuccessResponse? =
        client.get(url("/login")) {
            parameter("username", apiDetailsService.getUserName())
            parameter("password", apiDetailsService.getPassword())
        }.body()

    suspend fun getItems(
        type: String,
        items: Int,
        offset: Int,
        tag: String?,
        source: Long?,
        search: String?,
        updatedSince: String?
    ): List<SelfossModel.Item>? =
        client.get(url("/items")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
                parameter("type", type)
                parameter("tag", tag)
                parameter("source", source)
                parameter("search", search)
                parameter("updatedsince", updatedSince)
                parameter("items", items)
                parameter("offset", offset)
            }.body()

    suspend fun stats(): SelfossModel.Stats? =
        client.get(url("/stats")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()

    suspend fun tags(): List<SelfossModel.Tag>? =
        client.get(url("/tags")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()

    suspend fun update(): SelfossModel.SuccessResponse? =
        client.get(url("/update")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()

    suspend fun spouts(): Map<String, SelfossModel.Spout>? =
        client.get(url("/sources/spouts")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()

    suspend fun sources(): ArrayList<SelfossModel.Source>? =
        client.get(url("/sources/list")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()

    suspend fun version(): SelfossModel.ApiVersion? =
        client.get(url("/api/about")).body()

    suspend fun markAsRead(id: String): SelfossModel.SuccessResponse? =
        client.post(url("/mark/$id")) {
            parameter("username", apiDetailsService.getUserName())
            parameter("password", apiDetailsService.getPassword())
        }.body()

    suspend fun unmarkAsRead(id: String): SelfossModel.SuccessResponse? =
        client.post(url("/unmark/$id")) {
            parameter("username", apiDetailsService.getUserName())
            parameter("password", apiDetailsService.getPassword())
        }.body()

    suspend fun starr(id: String): SelfossModel.SuccessResponse? =
        client.post(url("/starr/$id")) {
            parameter("username", apiDetailsService.getUserName())
            parameter("password", apiDetailsService.getPassword())
        }.body()

    suspend fun unstarr(id: String): SelfossModel.SuccessResponse? =
        client.post(url("/unstarr/$id")) {
            parameter("username", apiDetailsService.getUserName())
            parameter("password", apiDetailsService.getPassword())
        }.body()

    suspend fun markAllAsRead(ids: List<String>): SelfossModel.SuccessResponse? =
        client.submitForm(
            url = url("/mark"),
            formParameters = Parameters.build {
                append("username", apiDetailsService.getUserName())
                append("password", apiDetailsService.getPassword())
                ids.map { append("ids[]", it) }
            }
        ).body()

    suspend fun createSourceForVersion(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String,
        version: Int
    ): SelfossModel.SuccessResponse? =
        if (version > 1) {
            createSource2(title, url, spout, tags, filter)
        } else {
            createSource(title, url, spout, tags, filter)
        }

    suspend fun createSource(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): SelfossModel.SuccessResponse? =
        client.submitForm(
            url = url("/source?username=${apiDetailsService.getUserName()}&password=${apiDetailsService.getPassword()}"),
            formParameters = Parameters.build {
                append("title", title)
                append("url", url)
                append("spout", spout)
                append("tags", tags)
                append("filter", filter)
            }
        ).body()

    suspend fun createSource2(
        title: String,
        url: String,
        spout: String,
        tags: String,
        filter: String
    ): SelfossModel.SuccessResponse? =
        client.submitForm(
            url = url("/source?username=${apiDetailsService.getUserName()}&password=${apiDetailsService.getPassword()}"),
            formParameters = Parameters.build {
                append("title", title)
                append("url", url)
                append("spout", spout)
                append("tags[]", tags)
                append("filter", filter)
            }
        ).body()

    suspend fun deleteSource(id: Int): SelfossModel.SuccessResponse? =
        client.delete(url("/source/$id")) {
                parameter("username", apiDetailsService.getUserName())
                parameter("password", apiDetailsService.getPassword())
            }.body()
}