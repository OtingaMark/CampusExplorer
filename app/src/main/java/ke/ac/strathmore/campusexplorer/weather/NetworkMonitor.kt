package ke.ac.strathmore.campusexplorer.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Tells us whether the device currently has an internet connection, and keeps telling us
 * as it changes. Requires the ACCESS_NETWORK_STATE permission.
 *
 * Teaching note: this used to be done with the CONNECTIVITY_ACTION broadcast. That broadcast
 * is deprecated; NetworkCallback is the replacement.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    fun isOnlineNow(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        trySend(isOnlineNow())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
