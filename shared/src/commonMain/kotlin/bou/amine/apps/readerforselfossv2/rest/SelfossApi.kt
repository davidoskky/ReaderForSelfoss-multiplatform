package bou.amine.apps.readerforselfossv2.rest

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import bou.amine.apps.readerforselfossv2.rest.SelfossModel.SelfossModel.constructUrl
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmField

class SelfossApi {
    /**
     * TODO:
     * Self signed certs
     * Timeout + 408
     * Auth digest/basic
     * Loging
     */

    val baseUrl = "http://10.0.2.2:8888"
    val userName = ""
    val password = ""
    val client = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

    }

    private fun url(path: String) =
        "$baseUrl$path"


    suspend fun login() =
        client.get<String>(url("/login"))// Todo: params

    suspend fun getItems(type: String,
                         items: Int,
                         offset: Int,
                         tag: String? = "",
                         source: Long? = null,
                         search: String? = "",
                         updatedSince: String? = ""): List<SelfossModel.Item> =
        client.get(url("/items")) {
            parameter("username", userName)
            parameter("password", password)
            parameter("type", type)
            parameter("tag", tag)
            parameter("source", source)
            parameter("search", search)
            parameter("updatedsince", updatedSince)
            parameter("items", items)
            parameter("offset", offset)
        }

    suspend fun stats(): SelfossModel.Stats =
        client.get(url("/stats")) {
            parameter("username", userName)
            parameter("password", password)
        }

    suspend fun tags(): List<SelfossModel.Tag> =
        client.get(url("/tags")) {
            parameter("username", userName)
            parameter("password", password)
        }

    suspend fun update(): String =
        client.get(url("/update")) {
            parameter("username", userName)
            parameter("password", password)
        }

    suspend fun spouts(): Map<String, SelfossModel.Spout> =
        client.get(url("/sources/spouts")) {
            parameter("username", userName)
            parameter("password", password)
        }

    suspend fun sources(): List<SelfossModel.Source> =
        client.get(url("/sources/list")) {
            parameter("username", userName)
            parameter("password", password)
        }

    suspend fun version(): SelfossModel.ApiVersion =
        client.get(url("/api/about"))

    suspend fun markAsRead(id: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/mark/$id"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
            },
            encodeInQuery = true
        )

    suspend fun unmarkAsRead(id: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/unmark/$id"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
            },
            encodeInQuery = true
        )

    suspend fun starr(id: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/starr/$id"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
            },
            encodeInQuery = true
        )

    suspend fun unstarr(id: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/unstarr/$id"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
            },
            encodeInQuery = true
        )

    suspend fun markAllAsRead(ids: List<String>): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/mark"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
                append("ids[]", ids.joinToString(","))
            },
            encodeInQuery = true
        )

    suspend fun createSource(title: String, url: String, spout: String, tags: String, filter: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/source"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
                append("title", title)
                append("url", url)
                append("spout", spout)
                append("tags", tags)
                append("filter", filter)
            },
            encodeInQuery = true
        )

    suspend fun createSource2(title: String, url: String, spout: String, tags: String, filter: String): SelfossModel.SuccessResponse =
        client.submitForm(
            url = url("/source"),
            formParameters = Parameters.build {
                append("username", userName)
                append("password", password)
                append("title", title)
                append("url", url)
                append("spout", spout)
                append("tags[]", tags)
                append("filter", filter)
            },
            encodeInQuery = true
        )

    suspend fun deleteSource(id: String) =
        client.delete<SelfossModel.SuccessResponse>(url("/source/$id")) {
            parameter("username", userName)
            parameter("password", password)
        }
}