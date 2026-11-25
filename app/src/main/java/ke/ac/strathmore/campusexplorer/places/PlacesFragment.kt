package ke.ac.strathmore.campusexplorer.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import ke.ac.strathmore.campusexplorer.R
import ke.ac.strathmore.campusexplorer.data.Place
import ke.ac.strathmore.campusexplorer.databinding.FragmentPlacesBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Tab 1: RecyclerView + Firebase.
 * Shows the Firestore "places" collection in a list. Tap a row to see it on the map.
 */
class PlacesFragment : Fragment() {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlacesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlaceAdapter(onClick = ::showOnMap, onDelete = ::confirmDelete)
        binding.recyclerPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlaces.adapter = adapter

        binding.fabAdd.setOnClickListener { showAddPlaceDialog(onSave = viewModel::addPlace) }
        binding.btnSeed.setOnClickListener { viewModel.addSamplePlaces() }

        // Collect only while the fragment is visible (STARTED) and stop automatically after.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.places, viewModel.loading) { places, loading -> places to loading }
                        .collect { (places, loading) ->
                            adapter.submitList(places)
                            binding.progress.isVisible = loading
                            binding.emptyState.isVisible = !loading && places.isEmpty()
                        }
                }
                launch {
                    viewModel.message.collect { message ->
                        if (message != null) {
                            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                            viewModel.messageShown()
                        }
                    }
                }
            }
        }
    }

    /** Remember which place was tapped, then switch to the Map tab. */
    private fun showOnMap(place: Place) {
        viewModel.focusOn(place)
        requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
            .selectedItemId = R.id.mapFragment
    }

    private fun confirmDelete(place: Place) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_place_title, place.name))
            .setMessage(R.string.delete_place_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deletePlace(place) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
