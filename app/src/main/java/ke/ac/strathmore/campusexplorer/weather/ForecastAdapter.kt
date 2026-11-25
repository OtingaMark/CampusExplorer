package ke.ac.strathmore.campusexplorer.weather

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ke.ac.strathmore.campusexplorer.databinding.ItemForecastBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

class ForecastAdapter : ListAdapter<DailyForecast, ForecastAdapter.ForecastViewHolder>(DIFF) {

    class ForecastViewHolder(private val binding: ItemForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: DailyForecast) {
            // "2026-11-13" -> "Fri, 13 Nov"
            binding.tvDate.text = runCatching { LocalDate.parse(day.date).format(DATE_FORMAT) }
                .getOrDefault(day.date)
            binding.tvRain.text = day.rainChance?.let { "Rain ${it.roundToInt()}%" } ?: "Rain n/a"
            binding.tvTemps.text = "${day.minTemp.asDegrees()} / ${day.maxTemp.asDegrees()}"
        }

        private fun Double?.asDegrees() = this?.let { "${it.roundToInt()}°C" } ?: "n/a"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ForecastViewHolder(
        ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)

        val DIFF = object : DiffUtil.ItemCallback<DailyForecast>() {
            override fun areItemsTheSame(oldItem: DailyForecast, newItem: DailyForecast) =
                oldItem.date == newItem.date
            override fun areContentsTheSame(oldItem: DailyForecast, newItem: DailyForecast) =
                oldItem == newItem
        }
    }
}
