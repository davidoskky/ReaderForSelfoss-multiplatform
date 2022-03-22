package bou.amine.apps.readerforselfossv2.android.utils

import android.content.Context
import android.content.Intent
import bou.amine.apps.readerforselfossv2.android.R

fun String?.isEmptyOrNullOrNullString(): Boolean =
    this == null || this == "null" || this.isEmpty()

fun String.longHash(): Long {
    var h = 98764321261L
    val l = this.length
    val chars = this.toCharArray()

    for (i in 0 until l) {
        h = 31 * h + chars[i].code.toLong()
    }
    return h
}

fun String.toStringUriWithHttp(): String =
    if (!this.startsWith("https://") && !this.startsWith("http://")) {
        "http://" + this
    } else {
        this
    }

fun Context.shareLink(itemUrl: String, itemTitle: String) {
    val sendIntent = Intent()
    sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_TEXT, itemUrl.toStringUriWithHttp())
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, itemTitle)
    sendIntent.type = "text/plain"
    startActivity(
        Intent.createChooser(
            sendIntent,
            getString(R.string.share)
        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}