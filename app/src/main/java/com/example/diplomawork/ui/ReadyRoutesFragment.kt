package com.example.diplomawork.ui

import ParcelablePoint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.diplomawork.ListViewClasses.CategorData
import com.example.diplomawork.ListViewClasses.ListAdapter
import com.example.diplomawork.ListViewClasses.RoutesData
import com.example.diplomawork.ListViewClasses.categorAdapter
import com.example.diplomawork.R
import com.example.diplomawork.databinding.FragmentReadyRoutesBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yandex.mapkit.geometry.Point


class ReadyRoutesFragment : Fragment() {
    private lateinit var mBinding: FragmentReadyRoutesBinding
    var list = mutableListOf<CategorData>()
    private lateinit var adapter: categorAdapter
    private lateinit var mAdapter: ListAdapter
    var mList = mutableListOf<RoutesData>()
    private lateinit var Categorid: CategorData
    private lateinit var PointDesc: RoutesData
    private var openned = false
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentReadyRoutesBinding.inflate(inflater, container, false)
        readCity()
        return mBinding.root
    }

    private fun readCity() {
        list.clear()
        val db = Firebase.firestore
        db.collection("Cities")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val id = document.getString("id") ?: ""
                    val routeID = document.getString("routeID") ?: ""
                    list.add(CategorData(name, id, routeID))
                }
                adapter = categorAdapter(requireContext(), R.layout.row_categories, list)
                mBinding.CategoriesList.adapter = adapter
                mBinding.CategoriesList.setOnItemClickListener { parent, view, position, id ->
                    Categorid = adapter.getItem(position)!!
                    readRoutes(Categorid.routeID)
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    fun readRoutes(ID: String) {
        list.clear()
        val db = Firebase.firestore
        db.collection(ID).get().addOnSuccessListener { result ->
            for (document in result) {
                val name = document.getString("route_name") ?: ""
                val description = document.getString("description") ?: ""
                val route_id = document.getString("route_id") ?: ""
                list.add(CategorData(name, description, route_id))
            }
            adapter = categorAdapter(requireContext(), R.layout.row_categories, list)
            mBinding.CategoriesList.adapter = adapter
            mBinding.CategoriesList.setOnItemClickListener { parent, view, position, id ->
                Categorid = adapter.getItem(position)!!
                readWaypoints(Categorid.routeID, ID)
                mBinding.RouteDesc.setText(Categorid.id)
                if (!openned) {

                    mBinding.PointLayout.visibility = View.VISIBLE
                    mBinding.CategoriesList.visibility = View.INVISIBLE
                    openned = true
                } else {

                    mBinding.PointLayout.visibility = View.INVISIBLE
                    mBinding.CategoriesList.visibility = View.VISIBLE
                    openned = false
                }
                mBinding.BtnClose.setOnClickListener {

                    mBinding.RouteDesc.clearComposingText()
                    mBinding.PointLayout.visibility = View.INVISIBLE
                    mBinding.CategoriesList.visibility = View.VISIBLE
                    openned = false
                    readRoutes(ID)
                }

            }

        }

    }

    fun readWaypoints(routeid: String, ID: String) {
        mList.clear()
        val db = Firebase.firestore
        val waypoints = mutableListOf<ParcelablePoint>()
        db.collection(ID).document(routeid).collection("waypoints").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getString("latitude") ?: ""
                    val lon = document.getString("longitude") ?: ""
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    mList.add(RoutesData(name, lon, lat, description))
                    waypoints.add(ParcelablePoint(lat.toDouble(), lon.toDouble()))

                }
                mAdapter = ListAdapter(requireContext(), R.layout.row, mList)
                mBinding.PointList.adapter = mAdapter
                mBinding.PointList.setOnItemClickListener { parent, view, position, id ->
                    PointDesc = mAdapter.getItem(position)!!
                    mBinding.PointDesc.setText(PointDesc.description)

                    mBinding.RouteDesc.setText(Categorid.id)
                    if (!flag) {

                        mBinding.PointDescLayout.visibility = View.VISIBLE
                        mBinding.PointLayout.visibility = View.INVISIBLE
                        flag = true
                    } else {

                        mBinding.PointDescLayout.visibility = View.INVISIBLE
                        mBinding.PointLayout.visibility = View.VISIBLE
                        flag = false
                    }
                    mBinding.BtnClosePointDesc.setOnClickListener {

                        mBinding.PointDesc.clearComposingText()
                        mBinding.PointDescLayout.visibility = View.INVISIBLE
                        mBinding.PointLayout.visibility = View.VISIBLE
                        flag = false

                    }
                }
                mBinding.BtnMakeRoute.setOnClickListener {

                    val newMapFragment = MapFragment().apply {
                        arguments = Bundle().apply {
                            putParcelableArrayList("waypoints", ArrayList(waypoints))
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.dataContainer, newMapFragment)
                        .addToBackStack(null)
                        .commit()

                }

            }.addOnFailureListener { exception ->
            }

    }
}
