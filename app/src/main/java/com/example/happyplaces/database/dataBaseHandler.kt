package com.example.happyplaces.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.happyplaces.models.happyPlaceModel

class dataBaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION= 1
        private const val DATABASE_NAME= "HappyPlacesDatabase"
        private const val TABLE_HAPPYPLACE= "HappyPlacesTable"

        private const val KEY_ID= "id"
        private const val KEY_TITLE= "title"
        private const val KEY_IMAGE= "image"
        private const val KEY_DESCRIPTION= "description"
        private const val KEY_DATE= "date"
        private const val KEY_LOCATION= "location"
        private const val KEY_LATITUDE= "latitude"
        private const val KEY_LONGITUDE= "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACE_TABLE= ("CREATE TABLE " + TABLE_HAPPYPLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")

        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPYPLACE")
        onCreate(db)
    }

    // function to insert data
    fun addHappyPlace(happyPlace: happyPlaceModel): Long
    {
        val db= this.writableDatabase

        val contentValues= ContentValues()

        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        // Inserting employee details using insert query.
        val success= db.insert(TABLE_HAPPYPLACE, null, contentValues)
        //2nd argument is String containing nullColumnHack

        db.close()
        return success
    }

    fun updateHappyPlace(happyPlace: happyPlaceModel): Int
    {
        val db= this.writableDatabase

        val contentValues= ContentValues()

        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        // Inserting employee details using insert query.
        val success= db.update(TABLE_HAPPYPLACE, contentValues, KEY_ID+"="+happyPlace.id, null)
        //2nd argument is String containing nullColumnHack

        db.close()
        return success
    }

    fun deleteHappyPlace(happyPlace: happyPlaceModel): Int
    {
        val db= this.readableDatabase
        val success= db.delete(TABLE_HAPPYPLACE, KEY_ID+"="+happyPlace.id, null)
        db.close()
        return success
    }

    //Method to read the records from database in form of ArrayList
    fun getHappyPlacesList(): ArrayList<happyPlaceModel>
    {
        val happyPlaceList: ArrayList<happyPlaceModel> = ArrayList<happyPlaceModel>()

        // Query to select all the records from the table.
        val selectQuery= "SELECT * FROM $TABLE_HAPPYPLACE"
        val db= this.readableDatabase

        // Cursor is used to read the record one by one. Add them to data model class.
        var cursor: Cursor?= null

        try {
            cursor= db.rawQuery(selectQuery, null)
        }catch (e: SQLException){
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var image: String
        var description: String
        var date: String
        var location: String
        var latitude: Double
        var longitude: Double

        if(cursor.moveToFirst())
        {
            do {
                id= cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                title= cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE))
                image= cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE))
                description= cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION))
                date= cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                location= cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION))
                latitude= cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE))
                longitude= cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE))

                val happyPlaceModel= happyPlaceModel(id, title, image, description, date, location, latitude,longitude)
                happyPlaceList.add(happyPlaceModel)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return happyPlaceList

    }
}