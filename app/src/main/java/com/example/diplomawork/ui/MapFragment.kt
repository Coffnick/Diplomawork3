package com.example.diplomawork.ui


import ParcelablePoint
import android.annotation.SuppressLint

import android.graphics.Color
import com.yandex.mapkit.geometry.Point

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.diplomawork.Func.MapFunc
import com.example.diplomawork.R
import com.example.diplomawork.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map // Добавьте этот импорт
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

class MapFragment() : Fragment(), UserLocationObjectListener, CameraListener, Parcelable,
    DrivingSession.DrivingRouteListener {
    constructor(parcel: Parcel) : this() {
        // Ничего не требуется
    }

    private lateinit var mBinding: FragmentMapBinding
    private lateinit var checkLocationPermission: ActivityResultLauncher<Array<String>>
    private lateinit var mapFunc: MapFunc
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null
    private var mapObjects: MapObjectCollection? = null
    private var ROUTE_START_LOCATION = Point(47.229410, 39.718281)
    private var ROUTE_END_LOCATION = Point(47.214004, 39.794605)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMapBinding.inflate(inflater, container, false)
        return mBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLocationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            mapFunc.onRequestPermissionsResult(permissions)
        }

        mapFunc = MapFunc(this, checkLocationPermission, mBinding)

        mapFunc.checkPermission()
        mapFunc.userInterface()
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mBinding.mapview.map.mapObjects.addCollection()
        arguments?.let { bundel ->
            val lat = bundel.getString("latitude")
            val lon = bundel.getString("longitude")
            if (lat != null && lon != null) {
                Toast.makeText(requireContext(), lat, Toast.LENGTH_LONG).show()
                showRoute(lat, lon)
            }
        }
        arguments?.getParcelableArrayList<ParcelablePoint>("waypoints")?.let { parcelablePoints ->
            val waypoints = parcelablePoints.map { it.toPoint() }
            showManyRoutes(waypoints)
        }
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        mapFunc.setAnchor()
        userLocationView.pin.setIcon(
            ImageProvider.fromResource(
                requireContext(),
                R.drawable.user_arrow
            )
        )
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                requireContext(),
                R.drawable.user_arrow
            )
        )
        userLocationView.accuracyCircle.fillColor = Color.BLUE
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}

    override fun onObjectRemoved(p0: UserLocationView) {}

    override fun onCameraPositionChanged(
        map: Map, cPos: CameraPosition, cUpd: CameraUpdateReason, finish: Boolean
    ) {
        if (finish) {
            if (mapFunc.followUserLocation) {
                mapFunc.setAnchor()
            }
        } else {
            if (!mapFunc.followUserLocation) {
                mapFunc.noAnchor()
            }
        }
    }

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

    @SuppressLint("MissingPermission")
    fun showRoute(lat: String, lon: String) {

        val drivingOptions = DrivingOptions().apply { routesCount = 1 }
        val vehicleOptions = VehicleOptions()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            ROUTE_START_LOCATION = Point(
                location?.latitude.toString().toDouble(),
                location?.longitude.toString().toDouble()
            )
            ROUTE_END_LOCATION = Point(lat.toDouble(), lon.toDouble())
            val requestPoints: ArrayList<RequestPoint> = ArrayList()
            requestPoints.add(
                RequestPoint(
                    ROUTE_START_LOCATION,
                    RequestPointType.WAYPOINT,
                    null,
                    null
                )
            )
            requestPoints.add(
                RequestPoint(
                    ROUTE_END_LOCATION,
                    RequestPointType.WAYPOINT,
                    null,
                    null
                )
            )
            addMarker(ROUTE_END_LOCATION, R.drawable.waypoint_marker)
            drivingSession =
                drivingRouter!!.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this)
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Не удалось получить текущую локацию",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    @SuppressLint("MissingPermission")
    fun showManyRoutes(waypoints: List<Point>) {
        val drivingOptions = DrivingOptions().apply { routesCount = 1 }
        val vehicleOptions = VehicleOptions()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                ROUTE_START_LOCATION = Point(location.latitude, location.longitude)

                // Используем последнюю точку из списка waypoints как конечную точку маршрута
                if (waypoints.isNotEmpty()) {
                    ROUTE_END_LOCATION = waypoints.last()
                } else {
                    Toast.makeText(requireContext(), "ЧТо то пошло не по плану", Toast.LENGTH_LONG)
                        .show()
                }

                val requestPoints: ArrayList<RequestPoint> = ArrayList()
                requestPoints.add(
                    RequestPoint(
                        ROUTE_START_LOCATION,
                        RequestPointType.WAYPOINT,
                        null,
                        null
                    )
                )

                // Добавляем промежуточные точки, кроме последней
                for (i in 0 until waypoints.size - 1) {
                    requestPoints.add(
                        RequestPoint(
                            waypoints[i],
                            RequestPointType.WAYPOINT,
                            null,
                            null
                        )
                    )
                    addMarker(waypoints[i], R.drawable.waypoint_marker)
                }

                requestPoints.add(
                    RequestPoint(
                        ROUTE_END_LOCATION,
                        RequestPointType.WAYPOINT,
                        null,
                        null
                    )
                )

                drivingSession = drivingRouter!!.requestRoutes(
                    requestPoints,
                    drivingOptions,
                    vehicleOptions,
                    this
                )
                addMarker(ROUTE_END_LOCATION, R.drawable.waypoint_marker)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Не удалось получить текущую локацию",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun addMarker(point: Point, drawableId: Int) {
        val imageProvider = ImageProvider.fromResource(requireContext(), drawableId)
        val placemark = mapObjects?.addPlacemark(point, imageProvider)
        placemark?.apply {
            opacity = 1.0f
            isDraggable = false
            setIconStyle(
                IconStyle().apply {
                    scale = 0.1f // Установите фиксированный масштаб (например, 0.1f)
                    zIndex = 1.0f
                    // Попробуем без использования isFlat, так как он может быть не доступен в этом контексте
                }
            )
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Не удалось добавить метку на карту",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (mapFunc.permissionLocation) 1 else 0)
        parcel.writeByte(if (mapFunc.followUserLocation) 1 else 0)
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

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for (route in p0) {
            mapObjects!!.addPolyline(route.geometry)
            val timeInMinutes = route.metadata.weight.time.text // Получаем время маршрута
            mBinding.routeTime.text = "Время маршрута: $timeInMinutes"
            mBinding.routeTime.visibility = View.VISIBLE
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = "Неизвестная ошибка!"
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()


    }
}
