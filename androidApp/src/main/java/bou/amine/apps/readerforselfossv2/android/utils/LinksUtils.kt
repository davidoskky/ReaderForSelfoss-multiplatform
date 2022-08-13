package bou.amine.apps.readerforselfossv2.android.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.ReaderActivity
import bou.amine.apps.readerforselfossv2.android.model.getLinkDecoded
import bou.amine.apps.readerforselfossv2.android.utils.customtabs.CustomTabActivityHelper
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.utils.toStringUriWithHttp
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun Context.buildCustomTabsIntent(): CustomTabsIntent {

    val actionIntent = Intent(Intent.ACTION_SEND)
    actionIntent.type = "text/plain"
    val pflags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
    val createPendingShareIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            actionIntent,
            pflags
        )

    val intentBuilder = CustomTabsIntent.Builder()

    // TODO: change to primary when it's possible to customize custom tabs title color
    //intentBuilder.setToolbarColor(c.getResources().getColor(R.color.colorPrimary));
    intentBuilder.setToolbarColor(resources.getColor(R.color.colorAccentDark))
    intentBuilder.setShowTitle(true)


    intentBuilder.setStartAnimations(
        this,
        R.anim.slide_in_right,
        R.anim.slide_out_left
    )
    intentBuilder.setExitAnimations(
        this,
        android.R.anim.slide_in_left,
        android.R.anim.slide_out_right
    )

    val closeicon = BitmapFactory.decodeResource(resources, R.drawable.ic_close_white_24dp)
    intentBuilder.setCloseButtonIcon(closeicon)

    val shareLabel = this.getString(R.string.label_share)
    val icon = BitmapFactory.decodeResource(
        resources,
        R.drawable.ic_share_white_24dp
    )
    intentBuilder.setActionButton(icon, shareLabel, createPendingShareIntent)

    return intentBuilder.build()
}

fun Context.openItemUrlInternally(
    allItems: ArrayList<SelfossModel.Item>,
    currentItem: Int,
    linkDecoded: String,
    customTabsIntent: CustomTabsIntent,
    articleViewer: Boolean,
    app: Activity
) {
    if (articleViewer) {
        ReaderActivity.allItems = allItems
        val intent = Intent(this, ReaderActivity::class.java)
        intent.putExtra("currentItem", currentItem)
        app.startActivity(intent)
    } else {
        this.openItemUrlInternalBrowser(
                linkDecoded,
                customTabsIntent,
                app)
    }
}

fun Context.openItemUrlInternalBrowser(
        linkDecoded: String,
        customTabsIntent: CustomTabsIntent,
        app: Activity
) {
    try {
        CustomTabActivityHelper.openCustomTab(
                app,
                customTabsIntent,
                Uri.parse(linkDecoded)
        ) { _, uri ->
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    } catch (e: Exception) {
        openInBrowser(linkDecoded, app)
    }
}

fun Context.openItemUrl(
    allItems: ArrayList<SelfossModel.Item>,
    currentItem: Int,
    linkDecoded: String,
    customTabsIntent: CustomTabsIntent,
    internalBrowser: Boolean,
    articleViewer: Boolean,
    app: Activity
) {

    if (!linkDecoded.isUrlValid()) {
        Toast.makeText(
            this,
            this.getString(R.string.cant_open_invalid_url),
            Toast.LENGTH_LONG
        ).show()
    } else {
        if (!internalBrowser) {
            openInBrowser(linkDecoded, app)
        } else if (articleViewer) {
            this.openItemUrlInternally(
                allItems,
                currentItem,
                linkDecoded,
                customTabsIntent,
                articleViewer,
                app
            )
        } else {
            this.openItemUrlInternalBrowser(
                    linkDecoded,
                    customTabsIntent,
                    app
            )
        }
    }
}

private fun openInBrowser(linkDecoded: String, app: Activity) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(linkDecoded)
    try {
        app.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(app.baseContext, e.message, Toast.LENGTH_LONG).show()
    }
}

fun String.isUrlValid(): Boolean =
    this.toHttpUrlOrNull() != null && Patterns.WEB_URL.matcher(this).matches()

fun String.isBaseUrlValid(ctx: Context): Boolean {
    val baseUrl = this.toHttpUrlOrNull()
    var existsAndEndsWithSlash = false
    if (baseUrl != null) {
        val pathSegments = baseUrl.pathSegments
        existsAndEndsWithSlash = "" == pathSegments[pathSegments.size - 1]
    }

    return Patterns.WEB_URL.matcher(this).matches() && existsAndEndsWithSlash
}

fun Context.openInBrowserAsNewTask(i: SelfossModel.Item) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = Uri.parse(i.getLinkDecoded().toStringUriWithHttp())
    startActivity(intent)
}

class LinkOnTouchListener: View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        var ret = false
        val widget: TextView = v as TextView
        val text: CharSequence = widget.text
        val stext = Spannable.Factory.getInstance().newSpannable(text)

        val action = event!!.action

        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN) {
            var x: Float = event.x
            var y: Float = event.y

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y.toInt())
            val off = layout.getOffsetForHorizontal(line, x)

            val link = stext.getSpans(off, off, ClickableSpan::class.java)

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                }
                ret = true
            }
        }
        return ret
    }
}
