package com.example.diplomawork.Func

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.diplomawork.ListViewClasses.categorAdapter
import com.example.diplomawork.ListViewClasses.ListAdapter
import com.example.diplomawork.ListViewClasses.CategorData
import com.example.diplomawork.R
import com.example.diplomawork.ListViewClasses.RoutesData
import com.example.diplomawork.R.layout.row_categories
import com.example.diplomawork.databinding.FragmentCategoriesBinding
import com.example.diplomawork.databinding.FragmentRoutesBinding
import com.example.diplomawork.ui.CategoriesFragment
import com.example.diplomawork.ui.OnRouteItemClickListener
import com.example.diplomawork.ui.Routes
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RouteFunc(
    private val routeItemClickListener: OnRouteItemClickListener,

    private val fragment: Routes,
    private val mBinding: FragmentRoutesBinding



    ) {
    var list = mutableListOf<RoutesData>()
    private lateinit var adapter: ListAdapter


    fun readcords(ID:String) {
        val db = Firebase.firestore

        db.collection(ID)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val lat = document.getString("latitude") ?: ""
                    val lon = document.getString("longitude") ?: ""
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""

                    list.add(RoutesData(name, lat, lon, description))

                }
                adapter = ListAdapter(fragment.requireContext(), R.layout.row, list)
                mBinding.RouteList.adapter = adapter
                mBinding.RouteList.setOnItemClickListener { parent, view, position, id ->
                    val route = adapter.getItem(position)
                    routeItemClickListener.onRouteItemClick(
                        route?.longitude,
                        route?.latitude,
                        route?.description
                    )

                }

            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }


    }




}
