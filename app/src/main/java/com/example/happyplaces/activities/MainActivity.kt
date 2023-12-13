package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.dataBaseHandler
import com.example.happyplaces.models.happyPlaceModel
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallBack
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddHappyPlaces.setOnClickListener{
            val intent= Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB()
    {
        val dataBaseHandler: dataBaseHandler= dataBaseHandler(this)
        val getHappyPlacesList: ArrayList<happyPlaceModel> =dataBaseHandler.getHappyPlacesList()

        if(getHappyPlacesList.size> 0)
        {
                rvHappyPlacesList.visibility= View.VISIBLE
                tvNoHappyPlacesFoundYet.visibility= View.GONE
                setupHappyPlacesRecycleView(getHappyPlacesList)
        }else{
            rvHappyPlacesList.visibility= View.GONE
            tvNoHappyPlacesFoundYet.visibility= View.VISIBLE
        }
    }

    private fun setupHappyPlacesRecycleView(happyPlacesList: ArrayList<happyPlaceModel>)
    {
        rvHappyPlacesList.layoutManager= LinearLayoutManager(this)
        val adapter= HappyPlacesAdapter(this, happyPlacesList)
        rvHappyPlacesList.adapter= adapter
        rvHappyPlacesList.setHasFixedSize(true)

        // 4 Bind the onclickListener with adapter onClick function

        adapter.setOnClickListener(object: HappyPlacesAdapter.OnClickListener{
            override fun onCLick(position: Int, model: happyPlaceModel) {

                val intent= Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler= object: SwipeToEditCallBack(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter= rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper= ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)

        val deleteSwipeHandler= object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter= rvHappyPlacesList.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()
            }
        }

        val deleteItemTouchHelper= ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvHappyPlacesList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== ADD_PLACE_ACTIVITY_REQUEST_CODE)
        {
            if( resultCode== Activity.RESULT_OK)
            {
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Message", "Cancelled or Back pressed")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE= 1
        var EXTRA_PLACE_DETAILS= "extra_place_details"
    }
}