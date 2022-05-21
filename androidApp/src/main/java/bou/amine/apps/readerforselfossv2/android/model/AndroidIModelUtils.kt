package bou.amine.apps.readerforselfossv2.android.model

import android.content.Context
import android.net.Uri
import android.text.Html
import android.webkit.URLUtil
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.jsoup.Jsoup
import java.util.*


/**
 * Items extension methods
 */
fun SelfossModel.Item.getIcon(baseUrl: String): String {
    return constructUrl(baseUrl, "favicons", icon)
}

fun SelfossModel.Item.getThumbnail(baseUrl: String): String {
    return constructUrl(baseUrl, "thumbnails", thumbnail)
}

fun SelfossModel.Item.getImages() : ArrayList<String> {
    val allImages = ArrayList<String>()

    for ( image in Jsoup.parse(content).getElementsByTag("img")) {
        val url = image.attr("src")
        if (url.lowercase(Locale.US).contains(".jpg") ||
            url.lowercase(Locale.US).contains(".jpeg") ||
            url.lowercase(Locale.US).contains(".png") ||
            url.lowercase(Locale.US).contains(".webp"))
        {
            allImages.add(url)
        }
    }
    return allImages
}

fun SelfossModel.Item.preloadImages(context: Context) : Boolean {
    val imageUrls = this.getImages()

    val glideOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).timeout(10000)


    try {
        for (url in imageUrls) {
            if ( URLUtil.isValidUrl(url)) {
                Glide.with(context).asBitmap()
                    .apply(glideOptions)
                    .load(url).submit()
            }
        }
    } catch (e : Error) {
        return false
    }

    return true
}

fun SelfossModel.Item.getTitleDecoded(): String {
    return Html.fromHtml(title).toString()
}

fun SelfossModel.Item.getSourceTitle(): String {
    return Html.fromHtml(sourcetitle).toString()
}

// TODO: maybe find a better way to handle these kind of urls
fun SelfossModel.Item.getLinkDecoded(): String {
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


/**
 * Sources extension methods
 */

fun SelfossModel.Source.getIcon(baseUrl: String): String {
    return constructUrl(baseUrl, "favicons", icon)
}

fun SelfossModel.Source.getTitleDecoded(): String {
    return Html.fromHtml(title).toString()
}



/**
 * Common methods
 */
private fun constructUrl(baseUrl: String, path: String, file: String?): String {
    return if (file == null || file == "null" || file.isEmpty()) {
        ""
    } else {
        val baseUriBuilder = Uri.parse(baseUrl).buildUpon()
        baseUriBuilder.appendPath(path).appendPath(file)

        baseUriBuilder.toString()
    }
}