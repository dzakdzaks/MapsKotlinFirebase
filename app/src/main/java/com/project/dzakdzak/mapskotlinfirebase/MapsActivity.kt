package com.project.dzakdzak.mapskotlinfirebase

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.project.dzakdzak.mapskotlinfirebase.Init.InitRetrofit
import com.project.dzakdzak.mapskotlinfirebase.Init.ResponseJSON
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*



class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    var awal: LatLng? = null
    var akhir: LatLng? = null

    var gps : GPSTracker? = null

    var lat : Double? = null
    var long : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var permission = (android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(permission), 2)

        gps = GPSTracker(this@MapsActivity)

        //check gps device
        if(gps!!.canGetLocation){
            //get coordinat
             lat = gps!!.getLatitude()
             long = gps!!.getLongitude()

            Log.d("coordinat ",lat.toString()+","+long.toString())

            //get name location based coordinat
//            var name = convertCoordinateToName(lat,long)
//            tvFrom.setText(name)

        } else {
            gps!!.showSettingGps()
        }

        // Write a message to the database
        val database = FirebaseDatabase.getInstance().getReference("lokasi")

        btnCheckIn.setOnClickListener {
                var lokasi = Lokasi(tvFrom.text.toString(),tvDestination.text.toString(),tvJarak.text.toString(),
                        tvHarga.text.toString(),tvWaktu.text.toString(),
                        lat.toString(),long.toString())

                var key = database.push().key

            database.child(key).setValue(lokasi)
        }
    }

    private fun convertCoordinateToName(lat: Double, long: Double) : String{


        var nameLocation : String? = null
        //deklarasi geocoder
        var geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
        var insertCoor = geoCoder.getFromLocation(lat,long,1)

        if(insertCoor.size > 0){
            nameLocation = insertCoor.get(0).getAddressLine(0)

        }

        return nameLocation!!
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        tvFrom.setOnClickListener {
            AutoComplete(1)
        }

        tvDestination.setOnClickListener {
            AutoComplete(2)
        }

        // Add a marker in Sydney and move the camera
        val IMA = LatLng(-6.195308, 106.79485)
        mMap!!.addMarker(MarkerOptions().position(IMA).title("Marker in IMA STUDIO"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(IMA))

        //auto zoom
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(IMA, 17.toFloat()))

        //set setting
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.uiSettings.isCompassEnabled = true
        mMap!!.uiSettings.setAllGesturesEnabled(true)
       // mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.isBuildingsEnabled = true
        mMap!!.isMyLocationEnabled = true
        mMap!!.isTrafficEnabled = true


    }

    private fun AutoComplete(i: Int) {
        val typeFilter = AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_MOSQUE).setCountry("ID")
                .build()
        var intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter)
                .build(this@MapsActivity)
        startActivityForResult(intent, i)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2) {
            mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == 1 && resultCode != null) {

                //get data return
                var place = PlaceAutocomplete.getPlace(this, data)
                var lat = place.latLng.latitude
                var long = place.latLng.longitude

                //include latlong
                awal = LatLng(lat, long)

                mMap!!.clear()



                tvFrom.setText(place.address.toString())


                //add marker
                mMap!!.addMarker(MarkerOptions().position(awal!!)
                        .title(place.address.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                if (tvDestination.text.toString().length > 0) {
                    mMap!!.addMarker(MarkerOptions().position(akhir!!)
                            .title(place.address.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

                }
                //set camera zoom
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(awal, 20.toFloat()))
            } else if (requestCode == 2 && resultCode != null) {

                //get data return
                var place = PlaceAutocomplete.getPlace(this, data)
                var lat = place.latLng.latitude
                var long = place.latLng.longitude

                //include latlong
                akhir = LatLng(lat, long)





                tvDestination.setText(place.address.toString())
                //add marker
                ActionRoute()
                mMap!!.addMarker(MarkerOptions().position(akhir!!)
                        .title(place.address.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                if (tvFrom.text.toString().length > 0) {
                    mMap!!.addMarker(MarkerOptions().position(awal!!)
                            .title(place.address.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

                }
                //set camera zoom
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(akhir, 20.toFloat()))


            } else if (resultCode == 0) {
                Toast.makeText(applicationContext, "Belum Pilih Lokasi Bro", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {

        }
    }

    private fun ActionRoute() {

        //get init retrofit
        var api = InitRetrofit().getInitInstance()

        //request to server base end point
        var call = api.request_route(tvFrom.text.toString(),
                tvDestination.text.toString(),"driving")

        //walking,transit,driving,bycicyle

        //get response
        call.enqueue(object : Callback<ResponseJSON> {
            override fun onResponse(call: Call<ResponseJSON>?, response: Response<ResponseJSON>?) {

                Log.d("response : ", response?.message())
                if (response != null) {
                    if (response.isSuccessful) {
                        //get json array route
                        var route = response.body()?.routes
                        //get object overview polyline
                        var overview = route?.get(0)?.overviewPolyline
                        //get string json point
                        var point = overview?.points

                        var direction = DirectionMapsV2(this@MapsActivity)
                        direction.gambarRoute(mMap!!, point!!)

                        var legs = route?.get(0)?.legs

                        var distance = legs?.get(0)?.distance
                        tvJarak.setText(distance?.text.toString())

                        var duration = legs?.get(0)?.duration
                        tvWaktu.setText(duration?.text.toString())

                        var dist = Math.ceil(distance?.value?.toDouble()!! /1000)
                        tvHarga.setText("Rp. " + (dist* 5000).toString())






                    }
                    //                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                }
            }

            override fun onFailure(call: Call<ResponseJSON>?, t: Throwable?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })


        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
