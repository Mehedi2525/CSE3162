package bd.ac.ru.cse3162

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    lateinit var fusedlocation: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedlocation = LocationServices.getFusedLocationProviderClient(this)

        bSearch.setOnClickListener {
            getLastLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermission()) {
            if (locationEnable()) {
                fusedlocation.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location != null) {
                        val lat = location.latitude.toString()
                        var long = location.longitude.toString()

                        if (lat != null && long != null) {
                            // Getting JSON data from api
                            weather_btn.setOnClickListener {
                                getCurrentJsonData(lat, long)
                            }

                            // Update info to the UI
                            var (cityName, country) = getAddress(lat.toDouble(), long.toDouble())
                            city.text = cityName + ", "+ country
                            latitude.text = lat
                            longitude.text = long
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun locationEnable(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 1001
            )
        } catch (e: Exception) {

        }
    }

    private fun checkPermission(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                requestPermission()
            }
        }
    }

    data class Address(var cityName: String, val country: String)
    private fun getAddress(lat: Double, long: Double): Address {
        var geoCoder = Geocoder(this, Locale.getDefault())
        var adress = geoCoder.getFromLocation(lat, long, 1)

        return Address(adress.get(0).locality, adress.get(0).countryName)
    }

    private fun getCurrentJsonData(lat: String, long: String) {
        val API_KEY = "60e1c1301a9f41628ce2c173be2e24e5"
        val queue = Volley.newRequestQueue(this)

        val url =
            "https://api.weatherbit.io/v2.0/current?lat=${lat}&lon=${long}&key=${API_KEY}&include=minutely"

        try {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener { response ->
                    setValues(response)
                },
                Response.ErrorListener {
                    Toast.makeText(
                        this,
                        "Please turn on internet connection",
                        Toast.LENGTH_LONG
                    ).show()
                })


            queue.add(jsonRequest)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "ERROR" + e.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setValues(response: JSONObject) {
        var tempCel = response.getJSONArray("data").getJSONObject(0).getInt("temp")
        temp_c.text =  "${tempCel} °C"
        tempF.text =  "${tempCel * 1.8 + 32} °F"
        var hdt = response.getJSONArray("data").getJSONObject(0).getString("rh")
        humidity.text =  hdt + "%"
        pres.text = response.getJSONArray("data").getJSONObject(0).getString("pres") + "mBar"
        vis.text = response.getJSONArray("data").getJSONObject(0).getString("vis") + "km"
        var weatherStatus = response.getJSONArray("data").getJSONObject(0).getJSONObject("weather").getString("description")
        weather.text = weatherStatus

        ob_time.text = response.getJSONArray("data").getJSONObject(0).getString("datetime")

        sunrise.text = response.getJSONArray("data").getJSONObject(0).getString("sunrise")
        sunset.text = response.getJSONArray("data").getJSONObject(0).getString("sunset")
    }

}