package bou.amine.apps.readerforselfossv2.android.adapters

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import bou.amine.apps.readerforselfossv2.android.api.selfoss.Item
import bou.amine.apps.readerforselfossv2.android.api.selfoss.SelfossApi
import bou.amine.apps.readerforselfossv2.android.databinding.ListItemBinding
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.LinkOnTouchListener
import bou.amine.apps.readerforselfossv2.android.utils.buildCustomTabsIntent
import bou.amine.apps.readerforselfossv2.android.utils.customtabs.CustomTabActivityHelper
import bou.amine.apps.readerforselfossv2.android.utils.glide.bitmapCenterCrop
import bou.amine.apps.readerforselfossv2.android.utils.glide.circularBitmapDrawable
import bou.amine.apps.readerforselfossv2.android.utils.openItemUrl
import bou.amine.apps.readerforselfossv2.android.utils.sourceAndDateText
import bou.amine.apps.readerforselfossv2.android.utils.toTextDrawableString
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import kotlin.collections.ArrayList

class ItemListAdapter(
    override val app: Activity,
    override var items: ArrayList<Item>,
    override val api: SelfossApi,
    override val db: AppDatabase,
    private val helper: CustomTabActivityHelper,
    private val internalBrowser: Boolean,
    private val articleViewer: Boolean,
    override val userIdentifier: String,
    override val appColors: AppColors,
    override val config: Config,
    override val updateItems: (ArrayList<Item>) -> Unit
) : ItemsAdapter<ItemListAdapter.ViewHolder>() {
    private val generator: ColorGenerator = ColorGenerator.MATERIAL
    private val c: Context = app.baseContext

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val itm = items[position]

            binding.title.text = itm.getTitleDecoded()

            binding.title.setOnTouchListener(LinkOnTouchListener())

            binding.title.setLinkTextColor(appColors.colorAccent)

            binding.sourceTitleAndDate.text = itm.sourceAndDateText()

            if (itm.getThumbnail(c).isEmpty()) {

                if (itm.getIcon(c).isEmpty()) {
                    val color = generator.getColor(itm.getSourceTitle())

                    val drawable =
                            TextDrawable
                                    .builder()
                                    .round()
                                    .build(itm.getSourceTitle().toTextDrawableString(c), color)

                    binding.itemImage.setImageDrawable(drawable)
                } else {
                    c.circularBitmapDrawable(config, itm.getIcon(c), binding.itemImage)
                }
            } else {
                c.bitmapCenterCrop(config, itm.getThumbnail(c), binding.itemImage)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            handleCustomTabActions()
        }

        private fun handleCustomTabActions() {
            val customTabsIntent = c.buildCustomTabsIntent()
            helper.bindCustomTabsService(app)

            binding.root.setOnClickListener {
                c.openItemUrl(
                    items,
                    bindingAdapterPosition,
                    items[bindingAdapterPosition].getLinkDecoded(),
                    customTabsIntent,
                    internalBrowser,
                    articleViewer,
                    app
                )
            }
        }
    }
}
