package com.example.diplomawork.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.diplomawork.ListViewClasses.CategorData
import com.example.diplomawork.ListViewClasses.categorAdapter
import com.example.diplomawork.Profile
import com.example.diplomawork.R
import com.example.diplomawork.databinding.FragmentCategoriesBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoriesFragment : Fragment() {

    private lateinit var mBinding: FragmentCategoriesBinding
    var list = mutableListOf<CategorData>()
    private lateinit var adapter : categorAdapter
    private lateinit var Categorid: CategorData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentCategoriesBinding.inflate(inflater, container, false)
        readCity()
        return mBinding.root
    }
    private  fun readCity(){
        list.clear()
        val db  = Firebase.firestore
        db.collection("Cities")
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val name =document.getString("name")?:""
                    val id = document.getString("id")?:""
                    list.add(CategorData(name, id,id))
                }
                adapter = categorAdapter(requireContext(), R.layout.row_categories, list)
                mBinding.CategoriesList.adapter = adapter
                mBinding.CategoriesList.setOnItemClickListener { parent, view, position, id ->
                    Categorid = adapter.getItem(position)!!

                    readCategories()



                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }


    private fun readCategories() {
        list.clear()

        val db = Firebase.firestore
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.getString("name") ?: ""
                    val id = document.getString("ID") ?: ""
                    list.add(CategorData(name, id,id))
                }
                adapter = categorAdapter(requireContext(), R.layout.row_categories, list)
                mBinding.CategoriesList.adapter = adapter
                mBinding.CategoriesList.setOnItemClickListener { parent, view, position, id ->
                    val category = adapter.getItem(position)
                    openCategoryFragment(category)

                }
            }
            .addOnFailureListener { exception ->
                // Обработка ошибки
            }
    }

    private fun getFragmentById(id: String): Fragment? {
        return when (id) {
            "Routes" -> Routes()  // Замените на ваш конкретный фрагмент

            // Добавьте остальные фрагменты
            else -> null
        }
    }

  fun openCategoryFragment(category: CategorData?) {
        val fragment = getFragmentById(category?.id ?: "")
        if (fragment != null) {
            val args = Bundle()
            args.putString("ID", category?.id) // Передайте необходимые данные
            args.putString("id",Categorid.id)
            fragment.arguments = args
            parentFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, fragment) // Замените на ваш контейнер
                .addToBackStack(null)
                .commit()
        } else {
            // Обработка случая, когда фрагмент не найден
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as Profile).disableDrawer()
    }

    override fun onStop() {
        super.onStop()
        (activity as Profile).enableDrawer()
    }
}
