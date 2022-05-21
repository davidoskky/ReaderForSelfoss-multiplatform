package bou.amine.apps.readerforselfossv2.android.utils

import android.content.Context
import android.content.Intent
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.utils.toStringUriWithHttp

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