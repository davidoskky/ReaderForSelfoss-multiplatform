package bou.amine.apps.readerforselfossv2.rest

import android.os.Parcelable
import android.text.Html
import kotlinx.serialization.Serializable

class SelfossModel {

    @Serializable
    data class Tag(
        val tag: String,
        val color: String,
        val unread: Int
    ) {
        fun getTitleDecoded(): String {
            return Html.fromHtml(tag).toString()
        }
    }

    @Serializable
    class SuccessResponse(val success: Boolean) {
        val isSuccess: Boolean
            get() = success
    }

    @Serializable
    class Stats(
        val total: Int,
        val unread: Int,
        val starred: Int
    )

    @Serializable
    data class Spout(
        val name: String,
        val description: String
    )

    @Serializable
    data class ApiVersion(
        val version: String?,
        val apiversion: String?
    ) {
        fun getApiMajorVersion() : Int {
            var versionNumber = 0
            if (apiversion != null) {
                versionNumber = apiversion.substringBefore(".").toInt()
            }
            return versionNumber
        }
    }

    @Serializable
    data class Source(
        val id: String,
        val title: String,
        val tags: List<String>,
        val spout: String,
        val error: String,
        val icon: String
    )

    @Serializable
    data class Item(
        val id: String,
        val datetime: String,
        val title: String,
        val content: String,
        var unread: Int,
        var starred: Int,
        val thumbnail: String?,
        val icon: String?,
        val link: String,
        val sourcetitle: String,
        val tags: String
    )
}