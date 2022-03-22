package bou.amine.apps.readerforselfossv2.rest

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.JvmField
import org.jsoup.Jsoup
import java.util.Locale.US

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
        val tags: String,
        val spout: String,
        val error: String,
        val icon: String
    ) {
        fun getIcon(baseUrl: String): String {
            return constructUrl(baseUrl, "favicons", icon)
        }

        fun getTitleDecoded(): String {
            return Html.fromHtml(title).toString()
        }
    }

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
    ) {

        fun getIcon(baseUrl: String): String {
            return constructUrl(baseUrl, "favicons", icon)
        }

        fun getThumbnail(baseUrl: String): String {
            return constructUrl(baseUrl, "thumbnails", thumbnail)
        }

        fun getImages() : ArrayList<String> {
            val allImages = ArrayList<String>()

            for ( image in Jsoup.parse(content).getElementsByTag("img")) {
                val url = image.attr("src")
                if (url.lowercase(US).contains(".jpg") ||
                    url.lowercase(US).contains(".jpeg") ||
                    url.lowercase(US).contains(".png") ||
                    url.lowercase(US).contains(".webp"))
                {
                    allImages.add(url)
                }
            }
            return allImages
        }

        fun getTitleDecoded(): String {
            return Html.fromHtml(title).toString()
        }

        fun getSourceTitle(): String {
            return Html.fromHtml(sourcetitle).toString()
        }

        // TODO: maybe find a better way to handle these kind of urls
        fun getLinkDecoded(): String {
            var stringUrl: String
            stringUrl =
                if (link.startsWith("http://news.google.com/news/") || link.startsWith("https://news.google.com/news/")) {
                    if (link.contains("&amp;url=")) {
                        link.substringAfter("&amp;url=")
                    } else {
                        this.link.replace("&amp;", "&")
                    }
                } else {
                    this.link.replace("&amp;", "&")
                }

            // handle :443 => https
            if (stringUrl.contains(":443")) {
                stringUrl = stringUrl.replace(":443", "").replace("http://", "https://")
            }

            // handle url not starting with http
            if (stringUrl.startsWith("//")) {
                stringUrl = "http:$stringUrl"
            }

            return stringUrl
        }
    }

    companion object SelfossModel {
        private fun String?.isEmptyOrNullOrNullString(): Boolean =
            this == null || this == "null" || this.isEmpty()

        fun constructUrl(baseUrl: String, path: String, file: String?): String {
            return if (file.isEmptyOrNullOrNullString()) {
                ""
            } else {
                val baseUriBuilder = Uri.parse(baseUrl).buildUpon()
                baseUriBuilder.appendPath(path).appendPath(file)

                baseUriBuilder.toString()
            }
        }
    }
}