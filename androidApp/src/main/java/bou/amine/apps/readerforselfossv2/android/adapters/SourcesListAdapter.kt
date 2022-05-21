package bou.amine.apps.readerforselfossv2.android.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import bou.amine.apps.readerforselfossv2.android.R
import bou.amine.apps.readerforselfossv2.android.databinding.SourceListItemBinding
import bou.amine.apps.readerforselfossv2.android.model.getIcon
import bou.amine.apps.readerforselfossv2.android.model.getTitleDecoded
import bou.amine.apps.readerforselfossv2.android.utils.Config
import bou.amine.apps.readerforselfossv2.android.utils.glide.circularBitmapDrawable
import bou.amine.apps.readerforselfossv2.android.utils.network.isNetworkAvailable
import bou.amine.apps.readerforselfossv2.android.utils.toTextDrawableString
import bou.amine.apps.readerforselfossv2.rest.SelfossApi
import bou.amine.apps.readerforselfossv2.rest.SelfossModel
import bou.amine.apps.readerforselfossv2.service.ApiDetailsService
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourcesListAdapter(
    private val app: Activity,
    private val items: ArrayList<SelfossModel.Source>,
    private val api: SelfossApi,
    private val apiDetailsService: ApiDetailsService
) : RecyclerView.Adapter<SourcesListAdapter.ViewHolder>() {
    private val c: Context = app.baseContext
    private val generator: ColorGenerator = ColorGenerator.MATERIAL
    private lateinit var config: Config
    private lateinit var binding: SourceListItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = SourceListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itm = items[position]
        config = Config(c)

        if (itm.getIcon(apiDetailsService.getBaseUrl()).isEmpty()) {
            val color = generator.getColor(itm.getTitleDecoded())

            val drawable =
                TextDrawable
                    .builder()
                    .round()
                    .build(itm.getTitleDecoded().toTextDrawableString(c), color)
            binding.itemImage.setImageDrawable(drawable)
        } else {
            c.circularBitmapDrawable(config, itm.getIcon(apiDetailsService.getBaseUrl()), binding.itemImage)
        }

        binding.sourceTitle.text = itm.getTitleDecoded()
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(internal val mView: ConstraintLayout) : RecyclerView.ViewHolder(mView) {

        init {
            handleClickListeners()
        }

        private fun handleClickListeners() {

            val deleteBtn: Button = mView.findViewById(R.id.deleteBtn)

            deleteBtn.setOnClickListener {
                if (c.isNetworkAvailable(null)) {
                    val (id) = items[adapterPosition]
                    CoroutineScope(Dispatchers.IO).launch {
                        val action = api.deleteSource(id)
                        if (action != null && action.isSuccess) {
                            items.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            notifyItemRangeChanged(adapterPosition, itemCount)
                        } else {
                            Toast.makeText(
                                app,
                                R.string.can_delete_source,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
