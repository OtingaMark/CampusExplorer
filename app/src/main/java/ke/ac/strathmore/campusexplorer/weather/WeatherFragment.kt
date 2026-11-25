package ke.ac.strathmore.campusexplorer.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import ke.ac.strathmore.campusexplorer.R
import ke.ac.strathmore.campusexplorer.databinding.FragmentWeatherBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Tab 3: Internet connection.
 *  - shows whether the device is online (NetworkMonitor)
 *  - downloads a 7-day forecast for Nairobi from a REST API (Retrofit) into a RecyclerView
 *  - pull down to refresh; retries by itself when the connection comes back
 */
class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ForecastAdapter()
        binding.recyclerForecast.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerForecast.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        val networkMonitor = NetworkMonitor(requireContext().applicationContext)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect { state -> render(state, adapter) } }
                launch {
                    networkMonitor.isOnline.collect { online ->
                        binding.tvConnection.setText(if (online) R.string.online else R.string.offline)
                        binding.tvConnection.setBackgroundResource(
                            if (online) R.color.status_online else R.color.status_offline
                        )
                        // Connection just came back after a failed download -> try again.
                        if (online && viewModel.state.value is WeatherUiState.Error) viewModel.refresh()
                    }
                }
            }
        }
    }

    private fun render(state: WeatherUiState, adapter: ForecastAdapter) {
        binding.swipeRefresh.isRefreshing = state is WeatherUiState.Loading
        binding.tvError.isVisible = state is WeatherUiState.Error

        when (state) {
            is WeatherUiState.Loading -> Unit
            is WeatherUiState.Error -> binding.tvError.text = state.message
            is WeatherUiState.Success -> {
                adapter.submitList(state.days)
                val current = state.current
                if (current != null) {
                    binding.tvCurrentTemp.text =
                        getString(R.string.current_temp, current.temperature.roundToInt())
                    binding.tvCurrentWind.text =
                        getString(R.string.current_wind, current.windSpeed.roundToInt())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
