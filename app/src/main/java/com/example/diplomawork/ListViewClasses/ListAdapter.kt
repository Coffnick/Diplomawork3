package com.example.diplomawork.ListViewClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.diplomawork.R


class ListAdapter(var mCtx:Context, var resource:Int, var items:List<RoutesData>): ArrayAdapter<RoutesData>(mCtx,resource,items) {

    override fun getView(
        position: Int,
        convertView: android.view.View?,
        parent: ViewGroup
    ): android.view.View {
        val layoutInflater:LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resource, null)
        val textView:TextView = view.findViewById(R.id.text_view_item)
//        val lat:TextView = view.findViewById(R.id.text_view_lat)
//        val lon:TextView = view.findViewById(R.id.text_view_lon)
        var mItems: RoutesData = items[position]
        textView.text = mItems.name
//        lat.text = mItems.latitude
//        lon.text = mItems.longitude
        return view
    }
}



