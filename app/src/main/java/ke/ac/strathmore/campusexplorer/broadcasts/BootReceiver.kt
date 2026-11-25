package ke.ac.strathmore.campusexplorer.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * STATIC receiver: declared in AndroidManifest.xml, so Android can start it even when the
 * app is not running. It listens for BOOT_COMPLETED (sent once when the phone finishes
 * starting) and saves the time, which the Broadcasts tab then displays.
 *
 * Since Android 8 only a short list of system broadcasts may be received this way;
 * BOOT_COMPLETED is one of them. For everything else, register in code
 * (see BroadcastsFragment).
 *
 * To test: run the app once, restart the phone/emulator, open the app again.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("BootReceiver", "BOOT_COMPLETED received")
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BOOT, System.currentTimeMillis())
            .apply()
    }

    companion object {
        const val PREFS = "broadcast_prefs"
        const val KEY_LAST_BOOT = "last_boot"
    }
}
