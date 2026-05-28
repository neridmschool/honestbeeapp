package com.example.honestbeeapp.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.honestbeeapp.ui.theme.BeeCream
import com.example.honestbeeapp.ui.theme.BeeDarkText
import com.example.honestbeeapp.ui.theme.BeeError
import com.example.honestbeeapp.ui.theme.BeeHoneyYellow
import com.example.honestbeeapp.ui.theme.BeeMuted
import com.example.honestbeeapp.ui.theme.BeeNavigationSelected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class StructuredAddress(
    val street: String = "",
    val barangay: String = "",
    val city: String = "",
    val province: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val formattedAddress: String
        get() = listOf(street, barangay, city, province)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

@Composable
fun StructuredAddressPickerDialog(
    initialAddress: StructuredAddress,
    onDismiss: () -> Unit,
    onAddressSelected: (StructuredAddress) -> Unit
) {
    val scope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf(initialAddress.formattedAddress) }
    var street by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.street) }
    var barangay by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.barangay) }
    var city by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.city) }
    var province by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.province) }
    var latitude by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.latitude) }
    var longitude by rememberSaveable(initialAddress) { mutableStateOf(initialAddress.longitude) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var reverseLookupRequest by remember { mutableIntStateOf(0) }
    var searchResults by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }

    fun applyStructuredAddress(address: StructuredAddress) {
        street = address.street
        barangay = address.barangay
        city = address.city
        province = address.province
        latitude = address.latitude
        longitude = address.longitude
        searchQuery = address.formattedAddress
        statusMessage = "Location selected. You can edit any address field."
    }

    LaunchedEffect(reverseLookupRequest) {
        if (reverseLookupRequest == 0 || latitude == null || longitude == null) return@LaunchedEffect
        val selectedLatitude = latitude ?: return@LaunchedEffect
        val selectedLongitude = longitude ?: return@LaunchedEffect

        isSearching = true
        errorMessage = null
        runCatching {
            reverseNominatim(
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )
        }.onSuccess { result ->
            applyStructuredAddress(result)
            searchResults = emptyList()
        }.onFailure { exception ->
            errorMessage = exception.localizedMessage ?: "Could not read this map location. You can type the address manually."
        }
        isSearching = false
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pick Address on Map",
                color = BeeDarkText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HonestbeeTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search OpenStreetMap",
                    leadingIcon = Icons.Outlined.Search
                )
                HonestbeeButton(
                    text = "Search",
                    onClick = {
                        val cleanQuery = searchQuery.trim()
                        if (cleanQuery.isBlank()) {
                            errorMessage = "Enter an address to search."
                            return@HonestbeeButton
                        }

                        scope.launch {
                            isSearching = true
                            errorMessage = null
                            statusMessage = null
                            runCatching { searchNominatim(cleanQuery) }
                                .onSuccess { results ->
                                    searchResults = results
                                    val first = results.firstOrNull()
                                    if (first == null) {
                                        errorMessage = "No OpenStreetMap results found."
                                    } else {
                                        applyStructuredAddress(first.address)
                                        statusMessage = "Best match selected. Tap another result below if needed."
                                    }
                                }
                                .onFailure { exception ->
                                    errorMessage = exception.localizedMessage ?: "Could not search OpenStreetMap."
                                }
                            isSearching = false
                        }
                    },
                    isLoading = isSearching,
                    enabled = !isSearching
                )

                if (searchResults.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Search results",
                            style = MaterialTheme.typography.labelLarge,
                            color = BeeMuted
                        )
                        searchResults.take(3).forEach { result ->
                            SearchResultRow(
                                result = result,
                                onClick = {
                                    applyStructuredAddress(result.address)
                                    statusMessage = "Selected search result. You can edit any field."
                                }
                            )
                        }
                    }
                }

                MapPickerView(
                    latitude = latitude,
                    longitude = longitude,
                    onMapReady = { mapView = it },
                    onLocationTapped = { point ->
                        latitude = point.latitude
                        longitude = point.longitude
                        statusMessage = "Map point selected. Reading address fields..."
                        reverseLookupRequest += 1
                    }
                )

                Text(
                    text = "© OpenStreetMap contributors",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )

                Text(
                    text = "Street, Barangay, City, and Province are required. Edit any blank field manually.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )
                HonestbeeTextField(
                    value = street,
                    onValueChange = { street = it },
                    placeholder = "Street"
                )
                HonestbeeTextField(
                    value = barangay,
                    onValueChange = { barangay = it },
                    placeholder = "Barangay"
                )
                HonestbeeTextField(
                    value = city,
                    onValueChange = { city = it },
                    placeholder = "City"
                )
                HonestbeeTextField(
                    value = province,
                    onValueChange = { province = it },
                    placeholder = "Province"
                )

                Text(
                    text = "Selected Location:",
                    style = MaterialTheme.typography.labelLarge,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOf(street, barangay, city, province)
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifBlank { "No structured address selected yet." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeDarkText
                )
                Text(
                    text = "Lat: ${latitude?.formatCoordinate() ?: "--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )
                Text(
                    text = "Lng: ${longitude?.formatCoordinate() ?: "--"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted
                )

                statusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeHoneyYellow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BeeError,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            HonestbeeButton(
                text = "Use Address",
                fullWidth = false,
                onClick = {
                    val validationError = validateStructuredAddress(
                        street = street,
                        barangay = barangay,
                        city = city,
                        province = province
                    )
                    if (validationError != null) {
                        errorMessage = validationError
                        return@HonestbeeButton
                    }

                    onAddressSelected(
                        StructuredAddress(
                            street = street.trim(),
                            barangay = barangay.trim(),
                            city = city.trim(),
                            province = province.trim(),
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BeeMuted)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun MapPickerView(
    latitude: Double?,
    longitude: Double?,
    onMapReady: (MapView) -> Unit,
    onLocationTapped: (GeoPoint) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        factory = { context ->
            createMapView(
                context = context,
                latitude = latitude,
                longitude = longitude,
                onMapReady = onMapReady,
                onLocationTapped = onLocationTapped
            )
        },
        update = { view ->
            val marker = view.overlays.filterIsInstance<Marker>().firstOrNull()
            if (latitude != null && longitude != null && marker != null) {
                val point = GeoPoint(latitude, longitude)
                marker.isEnabled = true
                marker.position = point
                view.controller.animateTo(point)
            } else {
                marker?.isEnabled = false
            }
            view.invalidate()
        }
    )
}

private fun createMapView(
    context: Context,
    latitude: Double?,
    longitude: Double?,
    onMapReady: (MapView) -> Unit,
    onLocationTapped: (GeoPoint) -> Unit
): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    val initialPoint = GeoPoint(latitude ?: DEFAULT_LATITUDE, longitude ?: DEFAULT_LONGITUDE)

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(15.0)
        controller.setCenter(initialPoint)

        val marker = Marker(this).apply {
            position = initialPoint
            isEnabled = latitude != null && longitude != null
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Selected location"
        }
        overlays.add(marker)
        overlays.add(
            MapEventsOverlay(
                object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                        onLocationTapped(point)
                        return true
                    }

                    override fun longPressHelper(point: GeoPoint): Boolean {
                        onLocationTapped(point)
                        return true
                    }
                }
            )
        )
        onMapReady(this)
    }
}

@Composable
private fun SearchResultRow(
    result: NominatimResult,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = BeeCream,
        border = BorderStroke(1.dp, BeeNavigationSelected),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = BeeHoneyYellow
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.address.formattedAddress.ifBlank { "OpenStreetMap result" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = BeeDarkText,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = BeeMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class NominatimResult(
    val displayName: String,
    val address: StructuredAddress
)

private suspend fun searchNominatim(query: String): List<NominatimResult> {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val url = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&limit=5&q=$encodedQuery"
    val response = readUrl(url)
    val array = JSONArray(response)

    return (0 until array.length()).mapNotNull { index ->
        val item = array.optJSONObject(index) ?: return@mapNotNull null
        parseNominatimObject(item)
    }
}

private suspend fun reverseNominatim(
    latitude: Double,
    longitude: Double
): StructuredAddress {
    val url = "https://nominatim.openstreetmap.org/reverse?format=json&addressdetails=1&lat=$latitude&lon=$longitude"
    val response = readUrl(url)
    val item = JSONObject(response)
    return parseNominatimObject(item)?.address
        ?: StructuredAddress(latitude = latitude, longitude = longitude)
}

private suspend fun readUrl(url: String): String = withContext(Dispatchers.IO) {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "HonestbeeApp Android address picker")

        val code = connection.responseCode
        if (code !in 200..299) {
            error("OpenStreetMap search is unavailable right now.")
        }

        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun parseNominatimObject(item: JSONObject): NominatimResult? {
    val displayName = item.optString("display_name")
    val addressObject = item.optJSONObject("address") ?: JSONObject()
    val latitude = item.optString("lat").toDoubleOrNull()
    val longitude = item.optString("lon").toDoubleOrNull()
    val structuredAddress = parseStructuredAddress(
        addressObject = addressObject,
        displayName = displayName,
        latitude = latitude,
        longitude = longitude
    )

    if (displayName.isBlank() && structuredAddress.formattedAddress.isBlank()) return null

    return NominatimResult(
        displayName = displayName,
        address = structuredAddress
    )
}

private fun parseStructuredAddress(
    addressObject: JSONObject,
    displayName: String,
    latitude: Double?,
    longitude: Double?
): StructuredAddress {
    val street = firstNonBlank(
        addressObject.optString("road"),
        addressObject.optString("residential"),
        addressObject.optString("pedestrian"),
        addressObject.optString("path"),
        displayName.substringBefore(",").trim()
    )
    val barangay = firstNonBlank(
        addressObject.optString("suburb"),
        addressObject.optString("neighbourhood"),
        addressObject.optString("village"),
        addressObject.optString("quarter"),
        addressObject.optString("city_district")
    )
    val city = firstNonBlank(
        addressObject.optString("city"),
        addressObject.optString("town"),
        addressObject.optString("municipality"),
        addressObject.optString("county")
    )
    val province = firstNonBlank(
        addressObject.optString("state"),
        addressObject.optString("province")
    )

    return StructuredAddress(
        street = street,
        barangay = barangay,
        city = city,
        province = province,
        latitude = latitude,
        longitude = longitude
    )
}

private fun firstNonBlank(vararg values: String): String {
    return values.firstOrNull { it.isNotBlank() }.orEmpty()
}

private fun validateStructuredAddress(
    street: String,
    barangay: String,
    city: String,
    province: String
): String? {
    if (street.isBlank()) return "Street is required"
    if (barangay.isBlank()) return "Barangay is required"
    if (city.isBlank()) return "City is required"
    if (province.isBlank()) return "Province is required"
    return null
}

private fun Double.formatCoordinate(): String {
    return String.format(Locale.US, "%.6f", this)
}

private const val DEFAULT_LATITUDE = 10.3157
private const val DEFAULT_LONGITUDE = 123.8854
