package bou.amine.apps.readerforselfossv2.android.adapters

import android.app.Activity
import android.graphics.Color
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Item
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossApi
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.SharedItems
import com.google.android.material.snackbar.Snackbar

abstract class ItemsAdapter<VH : RecyclerView.ViewHolder?> : RecyclerView.Adapter<VH>() {
    abstract var items: ArrayList<Item>
    abstract val api: SelfossApi
    abstract val db: AppDatabase
    abstract val userIdentifier: String
    abstract val app: Activity
    abstract val appColors: AppColors
    abstract val config: Config
    abstract val updateItems: (ArrayList<Item>) -> Unit

    fun updateAllItems() {
        items = SharedItems.focusedItems
        notifyDataSetChanged()
        updateItems(items)
    }

    private fun unmarkSnackbar(i: Item, position: Int) {
        val s = Snackbar
            .make(
                app.findViewById(R.id.coordLayout),
                R.string.marked_as_read,
                Snackbar.LENGTH_LONG
            )
            .setAction(R.string.undo_string) {
                SharedItems.unreadItem(app, api, db, i)
                if (SharedItems.displayedItems == "unread") {
                    addItemAtIndex(i, position)
                } else {
                    notifyItemChanged(position)
                }
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
                SharedItems.readItem(app, api, db, items[position])
                items = SharedItems.focusedItems
                if (SharedItems.displayedItems == "unread") {
                    notifyItemRemoved(position)
                    updateItems(items)
                } else {
                    notifyItemChanged(position)
                }
            }

        val view = s.view
        val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(Color.WHITE)
        s.show()
    }

    fun handleItemAtIndex(position: Int) {
        if (SharedItems.unreadItemStatusAtIndex(position)) {
            readItemAtIndex(position)
        } else {
            unreadItemAtIndex(position)
        }
    }

    private fun readItemAtIndex(position: Int) {
        val i = items[position]
        SharedItems.readItem(app, api, db, i)
        if (SharedItems.displayedItems == "unread") {
            items.remove(i)
            notifyItemRemoved(position)
            updateItems(items)
        } else {
            notifyItemChanged(position)
        }
        unmarkSnackbar(i, position)
    }

    private fun unreadItemAtIndex(position: Int) {
        SharedItems.unreadItem(app, api, db, items[position])
        notifyItemChanged(position)
        markSnackbar(position)
    }

    fun addItemAtIndex(item: Item, position: Int) {
        items.add(position, item)
        notifyItemInserted(position)
        updateItems(items)

    }

    fun addItemsAtEnd(newItems: List<Item>) {
        val oldSize = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(oldSize, newItems.size)
        updateItems(items)

    }
}