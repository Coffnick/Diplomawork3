package com.example.diplomawork.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.diplomawork.Func.RouteFunc
import com.example.diplomawork.ListViewClasses.ListAdapter
import com.example.diplomawork.Profile
import com.example.diplomawork.R
import com.example.diplomawork.ListViewClasses.RoutesData
import com.example.diplomawork.databinding.FragmentRoutesBinding
import com.yandex.mapkit.directions.driving.Description

interface OnRouteItemClickListener {
    fun onRouteItemClick(lat: String?, lon: String?, description: String?)
}

class Routes : Fragment(), OnRouteItemClickListener {

    private lateinit var routeItemClickListener: OnRouteItemClickListener
    private lateinit var mBinding: FragmentRoutesBinding
    private lateinit var routeFunc: RouteFunc
    private var categoryId: String? = null
    private var openned = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString("id")
    }
    override fun onStart() {
        super.onStart()
        (activity as Profile).disableDrawer()
    }

    override fun onStop() {
        super.onStop()
        (activity as Profile).enableDrawer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentRoutesBinding.inflate(inflater, container, false)
        routeItemClickListener = this
        routeFunc = RouteFunc(routeItemClickListener, this, mBinding)
        if (categoryId != null) {
            routeFunc.readcords(categoryId!!)
        }
        return mBinding.root
    }

    override fun onRouteItemClick(lat: String?, lon: String?, description: String?) {
        // Передать данные о координатах в MapFragment

        if (!openned) {
            mBinding.DescLayout.visibility = View.VISIBLE
            mBinding.RouteList.visibility = View.INVISIBLE
            openned = true
        } else {
            mBinding.DescLayout.visibility = View.INVISIBLE
            mBinding.RouteList.visibility = View.VISIBLE
            openned = false
        }
        mBinding.Close.setOnClickListener {

            mBinding.textViewDesc.clearComposingText()
            mBinding.DescLayout.visibility = View.INVISIBLE
            mBinding.RouteList.visibility = View.VISIBLE
            openned = false

        }

        mBinding.textViewDesc.setText(description)
        mBinding.ShowRoute.setOnClickListener {
            val mapFragment = childFragmentManager.findFragmentById(R.id.mapview) as? MapFragment
            if (mapFragment != null) {
                // Если MapFragment уже добавлен, просто показать маршрут
                mapFragment.showRoute(lat.toString(), lon.toString())
            } else {
                // Иначе добавить MapFragment и передать данные через аргументы
                val newMapFragment = MapFragment()
                val args = Bundle()
                args.putString("latitude", lat)
                args.putString("longitude", lon)
                newMapFragment.arguments = args

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.dataContainer,
                        newMapFragment
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }
        Toast.makeText(requireContext(), "Coordinates: lat=$lat, lon=$lon", Toast.LENGTH_SHORT)
            .show()
    }
}
