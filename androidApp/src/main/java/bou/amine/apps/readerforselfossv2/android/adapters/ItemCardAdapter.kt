package bou.amine.apps.readerforselfossv2.android.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import androidx.recyclerview.widget.RecyclerView
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.databinding.CardItemBinding
import bou.amine.apps.readerforselfossv2.android.model.*
import bou.amine.apps.readerforselfossv2.android.persistence.database.AppDatabase
import bou.amine.apps.readerforselfossv2.android.themes.AppColors
import bou.amine.apps.readerforselfossv2.android.utils.*
import bou.amine.apps.readerforselfossv2.android.utils.customtabs.CustomTabActivityHelper
import bou.amine.apps.readerforselfossv2.android.utils.glide.bitmapCenterCrop
import bou.amine.apps.readerforselfossv2.android.utils.glide.circularBitmapDrawable
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import bou.amine.apps.readerforselfossv2.service.SearchService
import bou.amine.apps.readerforselfossv2.utils.DateUtils
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemCardAdapter(
    override val app: Activity,
    override var items: ArrayList<SelfossModel.Item>,
    override val api: SelfossApi,
    override val apiDetailsService: ApiDetailsService,
    override val db: AppDatabase,
    private val helper: CustomTabActivityHelper,
    private val internalBrowser: Boolean,
    private val articleViewer: Boolean,
    private val fullHeightCards: Boolean,
    override val appColors: AppColors,
    override val userIdentifier: String,
    override val config: Config,
    override val searchService: SearchService,
    override val updateItems: (ArrayList<SelfossModel.Item>) -> Unit
) : ItemsAdapter<ItemCardAdapter.ViewHolder>() {
    private val c: Context = app.baseContext
    private val generator: ColorGenerator = ColorGenerator.MATERIAL
    private val imageMaxHeight: Int =
        c.resources.getDimension(R.dimen.card_image_max_height).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            val itm = items[position]

            binding.favButton.isSelected = itm.starred == 1
            binding.title.text = itm.getTitleDecoded()

            binding.title.setOnTouchListener(LinkOnTouchListener())

            binding.title.setLinkTextColor(appColors.colorAccent)

            binding.sourceTitleAndDate.text = itm.sourceAndDateText(DateUtils(apiDetailsService))

            if (!fullHeightCards) {
                binding.itemImage.maxHeight = imageMaxHeight
                binding.itemImage.scaleType = ScaleType.CENTER_CROP
            }

            if (itm.getThumbnail(apiDetailsService.getBaseUrl()).isEmpty()) {
                binding.itemImage.visibility = View.GONE
                Glide.with(c).clear(binding.itemImage)
                binding.itemImage.setImageDrawable(null)
            } else {
                binding.itemImage.visibility = View.VISIBLE
                c.bitmapCenterCrop(config, itm.getThumbnail(apiDetailsService.getBaseUrl()), binding.itemImage)
            }

            if (itm.getIcon(apiDetailsService.getBaseUrl()).isEmpty()) {
                val color = generator.getColor(itm.getSourceTitle())

                val drawable =
                        TextDrawable
                                .builder()
                                .round()
                                .build(itm.getSourceTitle().toTextDrawableString(c), color)
                binding.sourceImage.setImageDrawable(drawable)
            } else {
                c.circularBitmapDrawable(config, itm.getIcon(apiDetailsService.getBaseUrl()), binding.sourceImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: CardItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            handleClickListeners()
            handleCustomTabActions()
        }

        private fun handleClickListeners() {

            binding.favButton.setOnClickListener {
                val item = items[bindingAdapterPosition]
                if (c.isNetworkAvailable()) {
                    if (item.starred == 1) {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Todo: SharedItems.unstarItem(c, api, db, item)
                        }
                        item.starred = 0
                        binding.favButton.isSelected = false
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            // Todo: SharedItems.starItem(c, api, db, item)
                        }
                        item.starred = 1
                        binding.favButton.isSelected = true
                    }
                }
            }

                binding.shareBtn.setOnClickListener {
                    val item = items[bindingAdapterPosition]
                    c.shareLink(item.getLinkDecoded(), item.getTitleDecoded())
                }

                binding.browserBtn.setOnClickListener {
                    c.openInBrowserAsNewTask(items[bindingAdapterPosition])
                }
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
                    app,
                    searchService
                )
            }
        }
    }
}
