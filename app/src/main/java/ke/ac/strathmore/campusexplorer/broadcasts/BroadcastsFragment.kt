package ke.ac.strathmore.campusexplorer.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ke.ac.strathmore.campusexplorer.R
import ke.ac.strathmore.campusexplorer.databinding.FragmentBroadcastsBinding
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

/**
 * Tab 4: Broadcasts. Four ideas on one screen:
 *
 *  1. DYNAMIC receiver for SYSTEM broadcasts: registered in onStart(), unregistered in onStop().
 *     Toggle airplane mode or plug/unplug the charger and a row appears in the log.
 *  2. CUSTOM broadcast: the button sends our own action, and the same receiver picks it up.
 *  3. STICKY broadcast: ACTION_BATTERY_CHANGED can be read at any time without a receiver.
 *  4. STATIC receiver: BootReceiver (declared in the manifest) records the last device boot.
 */
class BroadcastsFragment : Fragment() {

    private var _binding: FragmentBroadcastsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BroadcastsViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val event = when (intent.action) {
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val on = intent.getBooleanExtra("state", false)
                    BroadcastEvent("Airplane mode changed", if (on) "Airplane mode is ON" else "Airplane mode is OFF")
                }
                Intent.ACTION_POWER_CONNECTED -> BroadcastEvent("Power connected", "Charger plugged in")
                Intent.ACTION_POWER_DISCONNECTED -> BroadcastEvent("Power disconnected", "Charger unplugged")
                Intent.ACTION_BATTERY_LOW -> BroadcastEvent("Battery low", "Time to find a charger")
                ACTION_DEMO -> BroadcastEvent(
                    "Custom broadcast", intent.getStringExtra(EXTRA_MESSAGE).orEmpty()
                )
                else -> return
            }
            viewModel.addEvent(event)
            showBatteryLevel()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BroadcastEventAdapter()
        binding.recyclerEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEvents.adapter = adapter

        binding.btnSend.setOnClickListener { sendCustomBroadcast() }
        binding.btnClear.setOnClickListener { viewModel.clear() }

        showLastBoot()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { events ->
                    adapter.submitList(events)
                    binding.tvEmpty.isVisible = events.isEmpty()
                }
            }
        }
    }

    /** Register while the screen is visible... */
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(ACTION_DEMO)
        }
        // Apps targeting Android 14+ must say whether OTHER APPS may send to this receiver.
        // NOT_EXPORTED = only the system and our own app can. ContextCompat makes the
        // same call work on older versions too.
        ContextCompat.registerReceiver(
            requireContext(), receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        showBatteryLevel()
    }

    /** ...and always unregister, otherwise the receiver leaks. */
    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(receiver)
    }

    private fun sendCustomBroadcast() {
        val message = binding.inputMessage.text?.toString()?.trim().orEmpty()
            .ifEmpty { getString(R.string.default_broadcast_message) }
        val intent = Intent(ACTION_DEMO)
            .setPackage(requireContext().packageName)   // deliver only inside our own app
            .putExtra(EXTRA_MESSAGE, message)
        requireContext().sendBroadcast(intent)
    }

    /**
     * ACTION_BATTERY_CHANGED is "sticky": the system keeps the last one around. Passing a
     * null receiver simply returns that last Intent, with nothing left to unregister.
     */
    private fun showBatteryLevel() {
        val battery = ContextCompat.registerReceiver(
            requireContext(), null, IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        binding.tvBattery.text = if (level >= 0 && scale > 0) {
            getString(
                R.string.battery_level, level * 100 / scale,
                getString(if (charging) R.string.charging else R.string.not_charging)
            )
        } else {
            getString(R.string.battery_unknown)
        }
    }

    private fun showLastBoot() {
        val lastBoot = requireContext()
            .getSharedPreferences(BootReceiver.PREFS, Context.MODE_PRIVATE)
            .getLong(BootReceiver.KEY_LAST_BOOT, 0L)
        binding.tvBoot.text = if (lastBoot == 0L) {
            getString(R.string.boot_never)
        } else {
            getString(R.string.boot_last, DateFormat.getDateTimeInstance().format(Date(lastBoot)))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ACTION_DEMO = "ke.ac.strathmore.campusexplorer.ACTION_DEMO"
        const val EXTRA_MESSAGE = "message"
    }
}
