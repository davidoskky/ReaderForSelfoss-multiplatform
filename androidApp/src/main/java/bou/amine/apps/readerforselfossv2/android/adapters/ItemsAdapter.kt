package bou.amine.apps.readerforselfossv2.android.adapters

import android.app.Activity
import android.graphics.Color
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import bou.amine.apps.readerforselfossv2.service.SearchService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ItemsAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {
    abstract var items: ArrayList<SelfossModel.Item>
    abstract val api: SelfossApi
    abstract val apiDetailsService: ApiDetailsService
    abstract val db: AppDatabase
    abstract val userIdentifier: String
    abstract val app: Activity
    abstract val appColors: AppColors
    abstract val config: Config
    abstract val searchService: SearchService
    abstract val updateItems: (ArrayList<SelfossModel.Item>) -> Unit

    fun updateAllItems() {
        items = ArrayList() // TODO: SharedItems.focusedItems
        notifyDataSetChanged()
        updateItems(items)
    }

    private fun unmarkSnackbar(i: SelfossModel.Item, position: Int) {
        val s = Snackbar
            .make(
                app.findViewById(R.id.coordLayout),
                R.string.marked_as_read,
                Snackbar.LENGTH_LONG
            )
            .setAction(R.string.undo_string) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Todo: SharedItems.unreadItem(app, api, db, i)
                }
                // Todo:
//                if (SharedItems.displayedItems == "unread") {
//                    addItemAtIndex(i, position)
//                } else {
//                    notifyItemChanged(position)
//                }
            }

        val view = s.view
        val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(Color.WHITE)
        s.show()
    }

    private fun markSnackbar(position: Int) {
        val s = Snackbar
            .make(
                app.findViewById(R.id.coordLayout),
                R.string.marked_as_unread,
                Snackbar.LENGTH_LONG
            )
            .setAction(R.string.undo_string) {
                CoroutineScope(Dispatchers.IO).launch {
                    // Todo: SharedItems.readItem(app, api, db, items[position])
                }
                // Todo: items = SharedItems.focusedItems
                // Todo:
//                if (SharedItems.displayedItems == "unread") {
//                    notifyItemRemoved(position)
//                    updateItems(items)
//                } else {
//                    notifyItemChanged(position)
//                }
            }

        val view = s.view
        val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(Color.WHITE)
        s.show()
    }

    fun handleItemAtIndex(position: Int) {
        // Todo:
//        if (SharedItems.unreadItemStatusAtIndex(position)) {
//            readItemAtIndex(position)
//        } else {
//            unreadItemAtIndex(position)
//        }
    }

    private fun readItemAtIndex(position: Int) {
        val i = items[position]
        CoroutineScope(Dispatchers.IO).launch {
            // Todo: SharedItems.readItem(app, api, db, i)
        }
        // Todo:
//        if (SharedItems.displayedItems == "unread") {
//            items.remove(i)
//            notifyItemRemoved(position)
//            updateItems(items)
//        } else {
//            notifyItemChanged(position)
//        }
        unmarkSnackbar(i, position)
    }

    private fun unreadItemAtIndex(position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            // Todo: SharedItems.unreadItem(app, api, db, items[position])
        }
        notifyItemChanged(position)
        markSnackbar(position)
    }

    fun addItemAtIndex(item: SelfossModel.Item, position: Int) {
        items.add(position, item)
        notifyItemInserted(position)
        updateItems(items)

    }

    fun addItemsAtEnd(newItems: List<SelfossModel.Item>) {
        val oldSize = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(oldSize, newItems.size)
        updateItems(items)

    }
}