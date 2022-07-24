package bou.amine.apps.readerforselfossv2.repository

import bou.amine.apps.readerforselfossv2.rest.SelfossModel

interface Repository {

    // TODO: remove the items variables in favor of storing everything in the database
    var items: ArrayList<SelfossModel.Item>
    var baseUrl: String

    var selectedType: String

    // API
    var apiMajorVersion: Int
    var badgeUnread: Int
    var badgeAll: Int
    var badgeStarred: Int

    suspend fun getNewerItems(): ArrayList<SelfossModel.Item>
    suspend fun getOlderItems(): ArrayList<SelfossModel.Item>
    suspend fun reloadBadges(): Boolean
    suspend fun getTags(): List<SelfossModel.Tag>?
    suspend fun getSpouts(): Map<String, SelfossModel.Spout>?
    suspend fun getSources(): ArrayList<SelfossModel.Source>?
    suspend fun markAsRead(id: Int): Boolean
    suspend fun unmarkAsRead(id: Int): Boolean
    suspend fun starr(id: Int): Boolean
    suspend fun unstarr(id: Int): Boolean
    suspend fun markAllAsRead(ids: List<Int>): Boolean
    suspend fun createSource(title: String,
                             url: String,
                             spout: String,
                             tags: String,
                             filter: String): Boolean
    suspend fun deleteSource(id: Int): Boolean
    suspend fun updateRemote(): Boolean
    suspend fun login(): Boolean
    fun refreshLoginInformation()
}