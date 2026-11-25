package ke.ac.strathmore.campusexplorer.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.ac.strathmore.campusexplorer.data.Place
import ke.ac.strathmore.campusexplorer.data.PlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Shared by PlacesFragment (the list) and MapFragment (the markers). Both fragments ask for it
 * with `by activityViewModels()`, so they get the same instance and always show the same data.
 */
class PlacesViewModel : ViewModel() {

    private val repository = PlacesRepository()

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    /** One-off message for a Snackbar (errors). */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Set when a list item is tapped; the map moves its camera to this place. */
    private val _focusPlace = MutableStateFlow<Place?>(null)
    val focusPlace: StateFlow<Place?> = _focusPlace.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePlaces()
                .catch { e ->
                    _loading.value = false
                    _message.value = "Firestore error: ${e.message}"
                }
                .collect { list ->
                    _places.value = list
                    _loading.value = false
                }
        }
    }

    fun addPlace(place: Place) {
        // No success listener needed: the snapshot listener above delivers the new document.
        repository.addPlace(place)
            .addOnFailureListener { e -> _message.value = "Could not save: ${e.message}" }
    }

    fun deletePlace(place: Place) {
        repository.deletePlace(place.id)
            .addOnFailureListener { e -> _message.value = "Could not delete: ${e.message}" }
    }

    fun addSamplePlaces() = SAMPLE_PLACES.forEach(::addPlace)

    fun messageShown() { _message.value = null }

    fun focusOn(place: Place) { _focusPlace.value = place }

    fun focusHandled() { _focusPlace.value = null }

    companion object {
        // Approximate coordinates, good enough for a demo.
        const val CAMPUS_LAT = -1.3089
        const val CAMPUS_LNG = 36.8121

        private val SAMPLE_PLACES = listOf(
            Place(name = "Strathmore University", description = "Main campus, Madaraka",
                latitude = CAMPUS_LAT, longitude = CAMPUS_LNG),
            Place(name = "Nyayo National Stadium", description = "Sports stadium near campus",
                latitude = -1.3040, longitude = 36.8245),
            Place(name = "KICC", description = "Kenyatta International Convention Centre",
                latitude = -1.2886, longitude = 36.8233),
            Place(name = "Uhuru Park", description = "Public park next to the city centre",
                latitude = -1.2900, longitude = 36.8170),
            Place(name = "Nairobi National Museum", description = "Museum Hill",
                latitude = -1.2740, longitude = 36.8143)
        )
    }
}
