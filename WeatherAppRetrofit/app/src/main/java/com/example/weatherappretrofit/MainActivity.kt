package com.example.weatherappretrofit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weatherappretrofit.api.RetrofitService
import com.example.weatherappretrofit.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), LocationListener {

    lateinit var viewModel: MainViewModel
    lateinit var binding: ActivityMainBinding

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var locationManager: LocationManager
    private var initLocation = true

    private val permissionId = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val retrofitService = RetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService)

        viewModel = ViewModelProvider(this, MyViewModelFactory(mainRepository)).get(MainViewModel::class.java)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        viewModel.list.observe(this) {
            binding.progressDialog.visibility = View.GONE

            binding.address.text = it.name + ", " + it.sys!!.country
            binding.updatedAt.text = it.dt.toString()
            binding.status.text = it.weather[0].description!!.capitalize()
            binding.temp.text = it.main!!.temp.toString().plus("°C")
            binding.tempMin.text = "Min Temp: " + it.main!!.tempMin + "°C"
            binding.tempMax.text = "Max Temp: " + it.main!!.tempMax + "°C"
            binding.sunrise.text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(it.sys!!.sunrise?.times(1000) ?: 0))
            binding.sunset.text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(it.sys!!.sunset!! * 1000))
            binding.wind.text = it.wind!!.speed.toString()
            binding.pressure.text = it.main!!.pressure.toString()
            binding.humidity.text = it.main!!.humidity.toString()
        }

        viewModel.errorMessage.observe(this) {
            binding.progressDialog.visibility = View.GONE
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(this, Observer {
            if (it) {
                binding.progressDialog.visibility = View.VISIBLE
            } else {
                binding.progressDialog.visibility = View.GONE
            }
        })

        binding.searchBt.setOnClickListener {
            if(viewModel.isOnline(context = this)) {
                viewModel.getWeather(binding.searchWeatherEdt.text.toString(),this)
                binding.searchWeatherEdt.text.clear()
            }else
                Toast.makeText(this, "Please Switch on Internet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i("LOCATION", "onLocationChanged:${location.latitude} " +
                "\n ${location.longitude} ")
        val geocoder = Geocoder(this, Locale.getDefault())
        val list: List<Address> =
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.i("LOCATION", "getLocation:${list[0].latitude} " +
                "\n ${list[0].longitude} " +
                "\n ${list[0].countryName}" +
                "\n ${list[0].locality}" +
                "\n ${list[0].adminArea}" +
                "\n ${list[0].getAddressLine(0)}")
        if(initLocation) {
//            viewModel.getWeather((list[0].locality + "," + list[0].adminArea +" ," + list[0].countryName).replace(",",""),this)
            viewModel.getWeather("Salem, Kannankurichi, TamilNadu, India",this) //working
            initLocation= false
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                Log.i("LOCATION", "isLocationEnabled: ")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                getLocation()
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }
}