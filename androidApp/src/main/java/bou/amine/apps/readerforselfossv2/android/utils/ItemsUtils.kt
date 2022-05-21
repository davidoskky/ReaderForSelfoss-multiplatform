package bou.amine.apps.readerforselfossv2.android.utils

import android.content.Context
import bou.amine.apps.readerforselfossv2.android.model.getSourceTitle
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.utils.DateUtils
import bou.amine.apps.readerforselfossv2.utils.parseRelativeDate

fun String.toTextDrawableString(c: Context): String {
    val textDrawable = StringBuilder()
    for (s in this.split(" ".toRegex()).filter { it.isNotEmpty() }.toTypedArray()) {
        try {
            textDrawable.append(s[0])
        } catch (e: StringIndexOutOfBoundsException) {
        }
    }
    return textDrawable.toString()
}

fun SelfossModel.Item.sourceAndDateText(dateUtils: DateUtils): String {
    val formattedDate = parseRelativeDate(dateUtils)

    return getSourceTitle() + formattedDate
}

fun SelfossModel.Item.toggleStar(): SelfossModel.Item {
    this.starred = !this.starred
    return this
}