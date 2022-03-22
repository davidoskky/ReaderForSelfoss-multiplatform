package bou.amine.apps.readerforselfossv2.android.utils.persistence

import bou.amine.apps.readerforselfossv2.android.api.selfoss.Item
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossTagType
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Source
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Tag
import bou.amine.apps.readerforselfossv2.android.persistence.entities.ItemEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.SourceEntity
import bou.amine.apps.readerforselfossv2.android.persistence.entities.TagEntity

fun TagEntity.toView(): Tag =
        Tag(
            this.tag,
            this.color,
            this.unread
        )

fun SourceEntity.toView(): Source =
        Source(
            this.id,
            this.title,
            SelfossTagType(this.tags),
            this.spout,
            this.error,
            this.icon
        )

fun Source.toEntity(): SourceEntity =
        SourceEntity(
            this.id,
            this.getTitleDecoded(),
            this.tags.tags,
            this.spout,
            this.error,
            this.icon.orEmpty()
        )

fun Tag.toEntity(): TagEntity =
        TagEntity(
            this.tag,
            this.color,
            this.unread
        )

fun ItemEntity.toView(): Item =
        Item(
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
            SelfossTagType(this.tags)
        )

fun Item.toEntity(): ItemEntity =
    ItemEntity(
        this.id,
        this.datetime,
        this.getTitleDecoded(),
        this.content,
        this.unread,
        this.starred,
        this.thumbnail,
        this.icon,
        this.link,
        this.getSourceTitle(),
        this.tags.tags
    )