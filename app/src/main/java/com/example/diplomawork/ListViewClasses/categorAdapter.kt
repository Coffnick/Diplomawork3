package com.example.diplomawork.ListViewClasses

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.diplomawork.R

class categorAdapter(var mCtx: Context, var resource:Int, var items:List<CategorData>): ArrayAdapter<CategorData>(mCtx,resource,items) {
    @SuppressLint("ViewHolder")
    override fun getView(
        position: Int,
        convertView: android.view.View?,
        parent: ViewGroup
    ): android.view.View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)
        val view: View = layoutInflater.inflate(resource, null)
        val textView: TextView = view.findViewById(R.id.text_view_categor)
        var mItems: CategorData = items[position]
        textView.text = mItems.name
        return view
    }
}