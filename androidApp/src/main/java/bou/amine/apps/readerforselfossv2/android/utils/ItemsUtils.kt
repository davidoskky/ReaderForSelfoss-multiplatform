package bou.amine.apps.readerforselfossv2.android.utils

import android.content.Context
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Item
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossTagType

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

fun Item.sourceAndDateText(): String {
    val formattedDate = parseRelativeDate(this.datetime)

    return this.getSourceTitle() + formattedDate
}

fun Item.toggleStar(): Item {
    this.starred = !this.starred
    return this
}

fun List<Item>.flattenTags(): List<Item> =
    this.flatMap {
        val item = it
        val tags: List<String> = it.tags.tags.split(",")
        tags.map { t ->
            item.copy(tags = SelfossTagType(t.trim()))
        }
    }