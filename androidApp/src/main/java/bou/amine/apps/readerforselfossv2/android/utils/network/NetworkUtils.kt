package bou.amine.apps.readerforselfossv2.android.utils.network

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.TextView
import bou.amine.apps.readerforselfossv2.android.R
import com.google.android.material.snackbar.Snackbar

var snackBarShown = false
var view: View? = null
lateinit var s: Snackbar

fun Context.isNetworkAvailable(
    v: View? = null,
    overrideOffline: Boolean = false
): Boolean {
    val networkIsAccessible = isNetworkAccessible(this)

    if (v != null && (!networkIsAccessible || overrideOffline) && (!snackBarShown || v != view)) {
        view = v
        s = Snackbar
            .make(
                v,
                R.string.no_network_connectivity,
                Snackbar.LENGTH_INDEFINITE
            )

        s.setAction(android.R.string.ok) {
            snackBarShown = false
            s.dismiss()
        }

        val view = s.view
        val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(Color.WHITE)
        s.show()
        snackBarShown = true
    }
    if (snackBarShown && networkIsAccessible && !overrideOffline) {
        s.dismiss()
    }
    return if(overrideOffline) overrideOffline else networkIsAccessible
}

private fun isNetworkAccessible(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    } else {
        val network = connectivityManager.activeNetworkInfo ?: return false
        return network.isConnectedOrConnecting
    }
}