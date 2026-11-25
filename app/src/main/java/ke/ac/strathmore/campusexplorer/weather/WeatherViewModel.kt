package ke.ac.strathmore.campusexplorer.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.ac.strathmore.campusexplorer.places.PlacesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/** Everything the Weather screen can be showing. */
sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val current: CurrentWeather?, val days: List<DailyForecast>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            _state.value = try {
                val response = WeatherService.api.getForecast(
                    PlacesViewModel.CAMPUS_LAT, PlacesViewModel.CAMPUS_LNG
                )
                WeatherUiState.Success(
                    current = response.current,
                    days = response.daily?.toDailyForecasts().orEmpty()
                )
            } catch (e: IOException) {
                // No connection, timeout, DNS failure...
                WeatherUiState.Error("Could not reach the server. Check your internet connection.")
            } catch (e: HttpException) {
                // The server answered, but with an error code (4xx / 5xx).
                WeatherUiState.Error("Server error ${e.code()}")
            }
        }
    }
}
