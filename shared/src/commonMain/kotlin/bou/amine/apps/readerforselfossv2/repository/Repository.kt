package bou.amine.apps.readerforselfossv2.repository

import bou.amine.apps.readerforselfossv2.rest.SelfossModel

interface Repository {

    // TODO: remove the items variables in favor of storing everything in the database
    var items: List<SelfossModel.Item>
    var selectedItems: List<SelfossModel.Item>
    var baseUrl: String

    // API
    var apiMajorVersion: Int

    fun getMoreItems(): List<SelfossModel.Item>
    fun stats(): SelfossModel.Stats
    fun getTags(): List<SelfossModel.Tag>
    fun getSpouts(): List<SelfossModel.Spout>
    fun getSources(): List<SelfossModel.Source>
    suspend fun markAsRead(id: String): Boolean
    suspend fun unmarkAsRead(id: String): Boolean
    suspend fun starr(id: String): Boolean
    suspend fun unstarr(id: String): Boolean
    fun markAllAsRead(ids: List<String>): Boolean
    fun createSource(title: String,
                     url: String,
                     spout: String,
                     tags: String,
                     filter: String): Boolean
    fun deleteSource(id: Int): Boolean
    fun updateRemote(): Boolean
    suspend fun login(): Boolean
    fun refreshLoginInformation()
}