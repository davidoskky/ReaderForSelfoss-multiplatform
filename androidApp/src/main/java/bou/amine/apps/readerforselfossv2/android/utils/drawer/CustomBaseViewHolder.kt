/* From https://github.com/mikepenz/MaterialDrawer/blob/develop/app/src/main/java/com/mikepenz/materialdrawer/app/drawerItems/CustomBaseViewHolder.java */
package bou.amine.apps.readerforselfossv2.android.utils.drawer

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import bou.amine.apps.readerforselfossv2.android.R

open class CustomBaseViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    var icon: ImageView = view.findViewById(R.id.material_drawer_icon)
    var name: TextView = view.findViewById(R.id.material_drawer_name)
    var description: TextView = view.findViewById(R.id.material_drawer_description)
}
