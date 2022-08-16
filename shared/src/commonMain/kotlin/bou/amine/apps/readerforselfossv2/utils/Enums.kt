package bou.amine.apps.readerforselfossv2.utils

enum class ItemType(val position: Int, val type: String) {
    UNREAD(1, "unread"),
    ALL(2, "all"),
    STARRED(3, "starred");

    companion object {
        fun fromInt(value: Int) = values().first { it.position == value }
    }
}