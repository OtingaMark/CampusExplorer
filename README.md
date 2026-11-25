# Campus Explorer

A small Kotlin teaching app for the Mobile Application Development unit. One activity,
four bottom tabs, one concept per tab.

| Tab | Concept | What it does |
|---|---|---|
| **Places** | RecyclerView + Firebase | Live list of the Firestore `places` collection. Add (+), delete, tap a row to jump to the map. |
| **Map** | Google Maps API | A marker for every place. Long-press the map to save a new place. Shows your location. |
| **Weather** | Internet connection | Online/offline banner, plus a 7-day Nairobi forecast downloaded from a REST API. |
| **Broadcasts** | Broadcasts | Dynamic, custom, sticky and manifest-declared receivers, with a log of what arrived. |

The bottom tabs are a `BottomNavigationView` wired to the Navigation component in `MainActivity`.

## Setup (about 10 minutes, once)

The project will not build until step 2 is done, because the Firebase plugin needs its config file.

1. **Open** the `CampusExplorer` folder in a current Android Studio and let Gradle sync.
   The project uses Android Gradle Plugin 9.3.3 and Gradle 9.5.0; if your Android Studio is
   older, update it or accept the upgrade prompt.
2. **Firebase**
   1. Go to <https://console.firebase.google.com>, create a project, and add an Android app with
      package name `ke.ac.strathmore.campusexplorer`.
   2. Download `google-services.json` and put it in the `app/` folder.
   3. In the console choose *Build > Firestore Database > Create database* and start in **test mode**.
      Test mode lets anyone read and write for 30 days. That is fine for class and not for a real app.
3. **Google Maps key**
   1. In <https://console.cloud.google.com> select the same project, enable **Maps SDK for Android**,
      and create an API key under *APIs & Services > Credentials*.
   2. Add this line to `local.properties` in the project root:
      `MAPS_API_KEY=your_key_here`
4. **Run** on an emulator image that includes Google Play services (or a real phone).
   On the Places tab press **Load sample places**.

## Where each concept lives

All paths are under `app/src/main/java/ke/ac/strathmore/campusexplorer/`.

| Concept | Files to read, in order |
|---|---|
| Bottom tabs | `res/menu/bottom_nav_menu.xml`, `res/navigation/nav_graph.xml`, `res/layout/activity_main.xml`, `MainActivity.kt` |
| RecyclerView | `res/layout/item_place.xml`, `places/PlaceAdapter.kt`, `places/PlacesFragment.kt` (two more adapters: `weather/ForecastAdapter.kt`, `broadcasts/BroadcastEvent.kt`) |
| Firebase | `data/Place.kt`, `data/PlacesRepository.kt`, `places/PlacesViewModel.kt` |
| Google Maps | `AndroidManifest.xml` (API key), `res/layout/fragment_map.xml`, `map/MapFragment.kt` |
| Internet | `weather/WeatherApi.kt`, `weather/WeatherViewModel.kt`, `weather/NetworkMonitor.kt`, `weather/WeatherFragment.kt` |
| Broadcasts | `broadcasts/BroadcastsFragment.kt`, `broadcasts/BootReceiver.kt`, `AndroidManifest.xml` (`<receiver>`) |

## Suggested classroom demo

1. **Firebase is live.** Project the Firestore console next to the emulator. Add a place in the app
   and watch the document appear; edit or delete it in the console and watch the list change.
   Two emulators side by side make the same point.
2. **Shared data.** Long-press the map to add a place, then switch to Places: it is already there,
   because both fragments use the same `PlacesViewModel`.
3. **Internet.** Turn on airplane mode on the Weather tab: the banner turns red and pull-to-refresh
   shows an error. Turn it off: the banner turns green and the forecast reloads by itself.
4. **Broadcasts.** On the Broadcasts tab toggle airplane mode and plug/unplug the charger
   (emulator: *Extended controls > Battery*). Send a custom broadcast. Restart the emulator and reopen
   the app to see the boot time recorded by `BootReceiver`.

## Exercises for students

- Add an *edit* action to a place (Firestore `update`).
- Replace test-mode security rules with Firebase Authentication and per-user rules.
- Draw a `Polyline` on the map joining all places.
- Show the weather for a tapped place instead of always Nairobi.
- Post a notification from `BootReceiver`.

## Troubleshooting

| Symptom | Cause |
|---|---|
| Build fails: `File google-services.json is missing` | Setup step 2 not done. |
| Map tab is blank/grey with a Google logo | Wrong or missing `MAPS_API_KEY`, or *Maps SDK for Android* not enabled for the key's project. |
| Snackbar `PERMISSION_DENIED` on Places | Firestore rules are not in test mode, or test mode has expired. |
| Places spinner never stops | No Firestore database created yet, or the emulator has no internet. |
| No boot time shown after restart | The app must have been opened at least once before the restart. |
