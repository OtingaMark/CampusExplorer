package ke.ac.strathmore.campusexplorer.broadcasts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ke.ac.strathmore.campusexplorer.databinding.ItemBroadcastBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.util.Date

/** One received broadcast, shown as a row in the log. */
data class BroadcastEvent(
    val title: String,
    val detail: String,
    val time: Long = System.currentTimeMillis()
)

/** Keeps the log in memory so it survives screen rotation and tab switches. */
class BroadcastsViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<BroadcastEvent>>(emptyList())
    val events: StateFlow<List<BroadcastEvent>> = _events.asStateFlow()

    fun addEvent(event: BroadcastEvent) {
        _events.value = listOf(event) + _events.value   // newest first
    }

    fun clear() { _events.value = emptyList() }
}

class BroadcastEventAdapter :
    ListAdapter<BroadcastEvent, BroadcastEventAdapter.EventViewHolder>(DIFF) {

    class EventViewHolder(private val binding: ItemBroadcastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: BroadcastEvent) {
            binding.tvTitle.text = event.title
            binding.tvDetail.text = event.detail
            binding.tvTime.text =
                DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date(event.time))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EventViewHolder(
        ItemBroadcastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<BroadcastEvent>() {
            override fun areItemsTheSame(oldItem: BroadcastEvent, newItem: BroadcastEvent) =
                oldItem.time == newItem.time && oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: BroadcastEvent, newItem: BroadcastEvent) =
                oldItem == newItem
        }
    }
}
