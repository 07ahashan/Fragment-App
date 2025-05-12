package com.anviam.fragmentapp.model

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class DirectionsJSONParser {

    fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {
        val routes = mutableListOf<List<HashMap<String, String>>>()
        val jRoutes = jObject.getJSONArray("routes")

        for (i in 0 until jRoutes.length()) {
            val path = mutableListOf<HashMap<String, String>>()
            val jLegs = jRoutes.getJSONObject(i).getJSONArray("legs")

            for (j in 0 until jLegs.length()) {
                val jSteps = jLegs.getJSONObject(j).getJSONArray("steps")

                for (k in 0 until jSteps.length()) {
                    val polyline = jSteps.getJSONObject(k).getJSONObject("polyline").getString("points")
                    val list = decodePoly(polyline)

                    for (latLng in list) {
                        val hm = HashMap<String, String>()
                        hm["lat"] = latLng.latitude.toString()
                        hm["lng"] = latLng.longitude.toString()
                        path.add(hm)
                    }
                }
            }
            routes.add(path)
        }

        return routes
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat / 1E5, lng / 1E5)
            poly.add(latLng)
        }

        return poly
    }
}
