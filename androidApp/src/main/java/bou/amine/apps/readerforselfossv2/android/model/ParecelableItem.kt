package bou.amine.apps.readerforselfossv2.android.model

import android.os.Parcel
import android.os.Parcelable
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import com.google.gson.annotations.SerializedName

fun SelfossModel.Item.toParcelable() : ParecelableItem =
    ParecelableItem(
        this.id,
        this.datetime,
        this.title,
        this.content,
        this.unread,
        this.starred,
        this.thumbnail,
        this.icon,
        this.link,
        this.sourcetitle,
        this.tags
    )
data class ParecelableItem(
    @SerializedName("id") val id: String,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("unread") var unread: Int,
    @SerializedName("starred") var starred: Int,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("link") val link: String,
    @SerializedName("sourcetitle") val sourcetitle: String,
    @SerializedName("tags") val tags: String
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParecelableItem> = object : Parcelable.Creator<ParecelableItem> {
            override fun createFromParcel(source: Parcel): ParecelableItem = ParecelableItem(source)
            override fun newArray(size: Int): Array<ParecelableItem?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        id = source.readString().orEmpty(),
        datetime = source.readString().orEmpty(),
        title = source.readString().orEmpty(),
        content = source.readString().orEmpty(),
        unread = source.readInt(),
        starred = source.readInt(),
        thumbnail = source.readString(),
        icon = source.readString(),
        link = source.readString().orEmpty(),
        sourcetitle = source.readString().orEmpty(),
        tags = source.readString().orEmpty()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(datetime)
        dest.writeString(title)
        dest.writeString(content)
        dest.writeInt(unread)
        dest.writeInt(starred)
        dest.writeString(thumbnail)
        dest.writeString(icon)
        dest.writeString(link)
        dest.writeString(sourcetitle)
        dest.writeString(tags)
    }
}