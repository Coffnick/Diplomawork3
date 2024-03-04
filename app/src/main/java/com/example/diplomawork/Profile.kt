package com.example.diplomawork

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color.BLUE
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.diplomawork.databinding.ActivityProfileBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.Animation.Type.SMOOTH
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment.LEFT
import com.yandex.mapkit.logo.VerticalAlignment.BOTTOM
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider.fromResource



class Profile() : AppCompatActivity(), UserLocationObjectListener, CameraListener, Parcelable {
    private lateinit var bindning: ActivityProfileBinding
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>

    private lateinit var userLocationLayer: UserLocationLayer

    private var routeStartLocation = Point(0.0, 0.0)

    private var permissionLocation = false
    private var followUserLocation = false

    constructor(parcel: Parcel) : this() {
        permissionLocation = parcel.readByte() != 0.toByte()
        followUserLocation = parcel.readByte() != 0.toByte()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("c7916f1b-9206-4fa1-92d2-dad1bc862be6")
        bindning = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindning.root)

        MapKitFactory.initialize(this)
//        bindning.mapview.mapWindow.map.move(CameraPosition(com.yandex.mapkit.geometry.Point(47.219775,39.718409),11.0f,0.0f,0.0f),
//        Animation(Animation.Type.SMOOTH,300f),null)

        checkLocationPermission = this.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[ACCESS_FINE_LOCATION] == true ||
                permissions[ACCESS_COARSE_LOCATION] == true) {
                onMapReady()
            }
        }

        checkPermission()

        userInterface()
    }

    private fun checkPermission() {
        if (checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
            checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION))
        }
    }

    private fun userInterface() {
        val mapLogoAlignment = Alignment(LEFT, BOTTOM)
        bindning.mapview.mapWindow.map.logo.setAlignment(mapLogoAlignment)

        bindning.userLocationFab.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()

                followUserLocation = true
            } else {
                checkPermission()
            }
        }
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(bindning.mapview.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        bindning.mapview.mapWindow.map.addCameraListener(this)

        cameraUserPosition()

        permissionLocation = true
    }

    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStartLocation = userLocationLayer.cameraPosition()!!.target
            bindning.mapview.mapWindow.map.move(
                CameraPosition(routeStartLocation, 16f, 0f, 0f), Animation(Animation.Type.SMOOTH, 1f), null
            )
        } else {
            bindning.mapview.mapWindow.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
        }
    }

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateReason, finish: Boolean
    ) {
        if (finish) {
            if (followUserLocation) {
                setAnchor()
            }
        } else {
            if (!followUserLocation) {
                noAnchor()
            }
        }
    }

    private fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF(
                (bindning.mapview.width * 0.5).toFloat(), (bindning.mapview.height * 0.5).toFloat()
            ),
            PointF(
                (bindning.mapview.width * 0.5).toFloat(), (bindning.mapview.height * 0.83).toFloat()
            )
        )

       bindning.userLocationFab.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }

    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        bindning.userLocationFab.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        userLocationView.pin.setIcon(fromResource(this, R.drawable.user_arrow))
        userLocationView.arrow.setIcon(fromResource(this, R.drawable.user_arrow))
        userLocationView.accuracyCircle.fillColor = BLUE
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onStop() {
       bindning.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (permissionLocation) 1 else 0)
        parcel.writeByte(if (followUserLocation) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        bindning.mapview.onStart()
    }
}