package ke.ac.strathmore.campusexplorer.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * One document in the Firestore "places" collection.
 *
 * Every property has a default value so that Kotlin generates the empty constructor
 * Firestore needs when it converts a document into a Place (toObject / toObjects).
 */
data class Place(
    @DocumentId val id: String = "",              // filled in from the document id, not stored as a field
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @ServerTimestamp val createdAt: Date? = null   // null on write -> Firestore sets the server time
)
