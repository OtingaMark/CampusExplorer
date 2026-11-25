package ke.ac.strathmore.campusexplorer.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ke.ac.strathmore.campusexplorer.R
import ke.ac.strathmore.campusexplorer.data.Place
import ke.ac.strathmore.campusexplorer.places.PlacesViewModel
import ke.ac.strathmore.campusexplorer.places.showAddPlaceDialog
import kotlinx.coroutines.launch

/**
 * Tab 2: Google Maps API.
 *  - one marker per Firestore place (same shared ViewModel as the list)
 *  - long-press anywhere to add a new place at that point
 *  - blue "my location" dot once the location permission is granted
 */
class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private val viewModel: PlacesViewModel by activityViewModels()

    private var map: GoogleMap? = null
    private val markers = mutableMapOf<String, Marker>()   // place id -> marker

    // Modern way to ask for a runtime permission. Must be registered before the fragment starts.
    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enableMyLocation()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The map itself is a fragment nested inside this one (see fragment_map.xml).
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)   // loads the map in the background -> onMapReady()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (view == null) return   // the tab was closed before the map finished loading
        map = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(PlacesViewModel.CAMPUS_LAT, PlacesViewModel.CAMPUS_LNG), 13f
            )
        )

        googleMap.setOnMapLongClickListener { point ->
            showAddPlaceDialog(point.latitude, point.longitude, onSave = viewModel::addPlace)
        }

        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.places.collect { places -> showMarkers(googleMap, places) } }
                launch {
                    viewModel.focusPlace.collect { place ->
                        if (place != null) {
                            focusOn(googleMap, place)
                            viewModel.focusHandled()
                        }
                    }
                }
            }
        }
    }

    private fun showMarkers(googleMap: GoogleMap, places: List<Place>) {
        googleMap.clear()
        markers.clear()
        for (place in places) {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(place.latitude, place.longitude))
                    .title(place.name)
                    .snippet(place.description)
            )
            if (marker != null) markers[place.id] = marker
        }
    }

    private fun focusOn(googleMap: GoogleMap, place: Place) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(place.latitude, place.longitude), 16f)
        )
        markers[place.id]?.showInfoWindow()
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")   // only called after the permission check
    private fun enableMyLocation() {
        map?.isMyLocationEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map = null
        markers.clear()
    }
}
