package ke.ac.strathmore.campusexplorer.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * All Firebase code lives here. The rest of the app only sees Place objects.
 */
class PlacesRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = db.collection("places")

    /**
     * A live stream of the collection. The snapshot listener fires once with the current
     * data and then again every time a document is added, changed or removed, on this
     * phone or any other. That is why the list and the map update without a refresh button.
     */
    fun observePlaces(): Flow<List<Place>> = callbackFlow {
        val registration = collection.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Place::class.java).orEmpty())
            }
        // Stop listening when nobody is collecting the flow any more.
        awaitClose { registration.remove() }
    }

    /** CREATE: Firestore generates the document id. */
    fun addPlace(place: Place): Task<DocumentReference> = collection.add(place)

    /** DELETE */
    fun deletePlace(id: String): Task<Void> = collection.document(id).delete()
}
