package ke.ac.strathmore.campusexplorer.places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ke.ac.strathmore.campusexplorer.data.Place
import ke.ac.strathmore.campusexplorer.databinding.ItemPlaceBinding
import java.util.Locale

/**
 * RecyclerView adapter. The three pieces every adapter needs:
 *   1. a ViewHolder          - holds the views of ONE row
 *   2. onCreateViewHolder()  - inflates a row layout (called only a few times)
 *   3. onBindViewHolder()    - puts the data for a position into a recycled row
 *
 * ListAdapter + DiffUtil work out which rows changed when a new list arrives,
 * so we never call notifyDataSetChanged() ourselves.
 */
class PlaceAdapter(
    private val onClick: (Place) -> Unit,
    private val onDelete: (Place) -> Unit
) : ListAdapter<Place, PlaceAdapter.PlaceViewHolder>(DIFF) {

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            binding.tvName.text = place.name
            binding.tvDescription.text = place.description
            binding.tvDescription.isVisible = place.description.isNotBlank()
            binding.tvCoordinates.text =
                String.format(Locale.US, "%.4f, %.4f", place.latitude, place.longitude)
            binding.root.setOnClickListener { onClick(place) }
            binding.btnDelete.setOnClickListener { onDelete(place) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(oldItem: Place, newItem: Place) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Place, newItem: Place) = oldItem == newItem
        }
    }
}
