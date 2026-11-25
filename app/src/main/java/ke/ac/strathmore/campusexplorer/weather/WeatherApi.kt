package ke.ac.strathmore.campusexplorer.weather

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit turns this interface into real HTTP calls. The function below performs:
 *
 *   GET https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=..&daily=..&timezone=..
 *
 * Open-Meteo is a free weather API that needs no API key, which makes it handy for class.
 * `suspend` means the call runs off the main thread and the coroutine waits for the result.
 */
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,wind_speed_10m",
        @Query("daily") daily: String =
            "temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): ForecastResponse
}

/** One Retrofit instance for the whole app, created the first time it is used. */
object WeatherService {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())   // JSON -> Kotlin objects
            .build()
            .create(WeatherApi::class.java)
    }
}

// ---- Classes that mirror the JSON the server sends back ----

data class ForecastResponse(
    val current: CurrentWeather? = null,
    val daily: DailyBlock? = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double = 0.0,
    @SerializedName("wind_speed_10m") val windSpeed: Double = 0.0
)

/** The API returns one array per measurement; index 0 is today, 1 is tomorrow, and so on. */
data class DailyBlock(
    val time: List<String> = emptyList(),
    @SerializedName("temperature_2m_max") val maxTemps: List<Double?> = emptyList(),
    @SerializedName("temperature_2m_min") val minTemps: List<Double?> = emptyList(),
    @SerializedName("precipitation_probability_max") val rainChances: List<Double?> = emptyList()
)

/** One row of the forecast RecyclerView. */
data class DailyForecast(
    val date: String,
    val maxTemp: Double?,
    val minTemp: Double?,
    val rainChance: Double?
)

fun DailyBlock.toDailyForecasts(): List<DailyForecast> = time.mapIndexed { i, date ->
    DailyForecast(date, maxTemps.getOrNull(i), minTemps.getOrNull(i), rainChances.getOrNull(i))
}
