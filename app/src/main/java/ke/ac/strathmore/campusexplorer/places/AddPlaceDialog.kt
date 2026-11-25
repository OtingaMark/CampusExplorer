package ke.ac.strathmore.campusexplorer.places

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ke.ac.strathmore.campusexplorer.R
import ke.ac.strathmore.campusexplorer.data.Place
import ke.ac.strathmore.campusexplorer.databinding.DialogAddPlaceBinding
import java.util.Locale

/**
 * Small form used by both the Places tab (+ button) and the Map tab (long-press).
 * Written as an extension function so any Fragment can call showAddPlaceDialog().
 */
fun Fragment.showAddPlaceDialog(
    latitude: Double = PlacesViewModel.CAMPUS_LAT,
    longitude: Double = PlacesViewModel.CAMPUS_LNG,
    onSave: (Place) -> Unit
) {
    val form = DialogAddPlaceBinding.inflate(layoutInflater)
    form.inputLatitude.setText(String.format(Locale.US, "%.5f", latitude))
    form.inputLongitude.setText(String.format(Locale.US, "%.5f", longitude))

    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.add_place)
        .setView(form.root)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.save) { _, _ ->
            val name = form.inputName.text?.toString()?.trim().orEmpty()
            val lat = form.inputLatitude.text?.toString()?.toDoubleOrNull()
            val lng = form.inputLongitude.text?.toString()?.toDoubleOrNull()

            val valid = name.isNotEmpty() &&
                lat != null && lat in -90.0..90.0 &&
                lng != null && lng in -180.0..180.0
            if (!valid) {
                Toast.makeText(requireContext(), R.string.invalid_place, Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }
            onSave(
                Place(
                    name = name,
                    description = form.inputDescription.text?.toString()?.trim().orEmpty(),
                    latitude = lat!!,
                    longitude = lng!!
                )
            )
        }
        .show()
}
