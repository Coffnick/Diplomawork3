package com.example.diplomawork.Func

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.example.diplomawork.R
import com.example.diplomawork.databinding.FragmentMapBinding
import com.example.diplomawork.ui.MapFragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.user_location.UserLocationLayer

class MapFunc(
    private val fragment: MapFragment,
    private val checkLocationPermission: ActivityResultLauncher<Array<String>>,
    private val mBinding: FragmentMapBinding
) {

    var permissionLocation = false
    var followUserLocation = false
    private lateinit var userLocationLayer: UserLocationLayer
    private var routeStartLocation = Point(0.0, 0.0)

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun userInterface() {
        val mapLogoAlignment = Alignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
        mBinding.mapview.mapWindow.map.logo.setAlignment(mapLogoAlignment)

        mBinding.userLocationFab.setOnClickListener {
            if (permissionLocation) {
                cameraUserPosition()
                followUserLocation = true
            } else {
                checkPermission()
            }
        }
    }

    fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mBinding.mapview.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(fragment)

        mBinding.mapview.mapWindow.map.addCameraListener(fragment)

        cameraUserPosition()
        permissionLocation = true
    }

    fun cameraUserPosition() {
        val cameraPosition = userLocationLayer.cameraPosition()
        if (cameraPosition != null) {
            routeStartLocation = cameraPosition.target
            mBinding.mapview.mapWindow.map.move(
                CameraPosition(routeStartLocation, 16f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        } else {
            mBinding.mapview.mapWindow.map.move(
                CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f)
            )
        }
    }

    fun setAnchor() {
        userLocationLayer.setAnchor(
            PointF(
                (mBinding.mapview.width * 0.5).toFloat(), (mBinding.mapview.height * 0.5).toFloat()
            ),
            PointF(
                (mBinding.mapview.width * 0.5).toFloat(), (mBinding.mapview.height * 0.83).toFloat()
            )
        )

        mBinding.userLocationFab.setImageResource(R.drawable.ic_my_location_black_24dp)
        followUserLocation = false
    }

    fun noAnchor() {
        userLocationLayer.resetAnchor()
        mBinding.userLocationFab.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }

    fun onRequestPermissionsResult(permissions: Map<String, Boolean>) {
        permissionLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionLocation) {
            onMapReady()
        }
    }
}
