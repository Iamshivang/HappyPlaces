package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.dataBaseHandler
import com.example.happyplaces.models.happyPlaceModel
import kotlinx.android.synthetic.main.item_happy_places.view.*

class HappyPlacesAdapter(private val context: Context, private val items: ArrayList<happyPlaceModel>): RecyclerView.Adapter<HappyPlacesAdapter.ViewHolder>() {

// 2 Add a variable for onClickListener interface.
    private var onClickListener: OnClickListener?= null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_happy_places, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Binds each item in the ArrayList to a view
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

         val item= items.get(position)

        holder.itemView.tvTitle.text= item.title
        holder.itemView.tvDescription.text= item.description
        holder.itemView.ivPlaceImage.setImageURI(item.image?.toUri())

        // 4 Finally add an onclickListener to the item.
        holder.itemView.setOnClickListener{
            if(onClickListener!= null)
            {
                onClickListener!!.onCLick(position, item)
            }
        }
        if(position%2== 0)
        {
            holder.itemView.cvHappyPlaces.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ptrn5))
        }else{
            holder.itemView.cvHappyPlaces.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ptrn4))
        }

    }

    fun removeAt(position: Int)
    {
        val dbHandler= dataBaseHandler(context)
        val isDeleted= dbHandler.deleteHappyPlace(items[position])
        if(isDeleted> 0)
        {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int)
    {
        val intent= Intent(context, AddHappyPlaceActivity:: class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, items[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    // 3 A function to bind the onclickListener
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

//    1 Create an interface for onclickListener
    interface OnClickListener{
        fun onCLick(position: Int, model: happyPlaceModel)
    }

    // A ViewHolder describes an item view and metadata about its place within the RecyclerView.
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}