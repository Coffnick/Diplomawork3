package com.example.diplomawork.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.diplomawork.Profile
import com.example.diplomawork.R
import com.example.diplomawork.databinding.ActivityProfileBinding
import com.example.diplomawork.databinding.FragmentMapBinding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider


class MapFragment() : Fragment(), UserLocationObjectListener, CameraListener, Parcelable {


    private lateinit var mBinding: FragmentMapBinding
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>

    private lateinit var userLocationLayer: UserLocationLayer

    private var routeStartLocation = Point(0.0, 0.0)

    private var permissionLocation = false
    private var followUserLocation = false

    constructor(parcel: Parcel) : this() {
        permissionLocation = parcel.readByte() != 0.toByte()
        followUserLocation = parcel.readByte() != 0.toByte()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MapKitFactory.setApiKey("c7916f1b-9206-4fa1-92d2-dad1bc862be6")
        MapKitFactory.initialize(requireContext())
        mBinding = FragmentMapBinding.inflate(inflater, container, false)
        return mBinding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Регистрация запроса разрешений после создания представления
        checkLocationPermission = this.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                onMapReady()
            }
        }

        // Проверка разрешений
        checkPermission()

        // Настройка пользовательского интерфейса
        userInterface()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onMapReady()
        } else {
            checkLocationPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    private fun userInterface() {
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
    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        userLocationLayer = mapKit.createUserLocationLayer(mBinding.mapview.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(this)

        mBinding.mapview.mapWindow.map.addCameraListener(this)

        cameraUserPosition()

        permissionLocation = true
    }
    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {
            routeStartLocation = userLocationLayer.cameraPosition()!!.target
            mBinding.mapview.mapWindow.map.move(
                CameraPosition(routeStartLocation, 16f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        } else {
            mBinding.mapview.mapWindow.map.move(CameraPosition(Point(0.0, 0.0), 16f, 0f, 0f))
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
                ( mBinding.mapview.width * 0.5).toFloat(), ( mBinding.mapview.height * 0.5).toFloat()
            ),
            PointF(
                ( mBinding.mapview.width * 0.5).toFloat(), ( mBinding.mapview.height * 0.83).toFloat()
            )
        )

        mBinding.userLocationFab.setImageResource(R.drawable.ic_my_location_black_24dp)

        followUserLocation = false
    }
    private fun noAnchor() {
        userLocationLayer.resetAnchor()

        mBinding.userLocationFab.setImageResource(R.drawable.ic_location_searching_black_24dp)
    }
    override fun onObjectAdded(userLocationView: UserLocationView) {
        setAnchor()

        userLocationView.pin.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.user_arrow))
        userLocationView.arrow.setIcon(ImageProvider.fromResource(requireContext(), R.drawable.user_arrow))
        userLocationView.accuracyCircle.fillColor = Color.BLUE
    }
    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onStop() {
        mBinding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mBinding.mapview.onStart()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (permissionLocation) 1 else 0)
        parcel.writeByte(if (followUserLocation) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MapFragment> {
        override fun createFromParcel(parcel: Parcel): MapFragment {
            return MapFragment(parcel)
        }

        override fun newArray(size: Int): Array<MapFragment?> {
            return arrayOfNulls(size)
        }
    }

}
