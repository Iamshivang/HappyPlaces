package com.example.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.happyplaces.R
import com.example.happyplaces.database.dataBaseHandler
import com.example.happyplaces.models.happyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener{

    private var cal= Calendar.getInstance()//An variable to get an instance calendar using the default time zone and locale.
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener //A variable for DatePickerDialog OnDateSetListener.The listener used to indicate the user has finished selecting a date. Which we will be initialize later on.
    private var saveImageToInternalStorage: Uri?= null
    private var mLatitude: Double= 0.0
    private var mLongitude: Double= 0.0
    private var mHappyPlaceDetails: happyPlaceModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        if(!Places.isInitialized())
        {
            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS))
        {
            mHappyPlaceDetails= intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as? happyPlaceModel
        }

        // create an OnDateSetListener
        dateSetListener= DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->

            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()// Called a function as updateDateInView where after selecting a date from date picker is populated in the UI component.
        }
        updateDateInView()

        if(mHappyPlaceDetails!= null)
        {
            supportActionBar?.title= "Edit Happy Place"

            etTitle.setText(mHappyPlaceDetails!!.title)
            etDescription.setText(mHappyPlaceDetails!!.description)
            etLocation.setText(mHappyPlaceDetails!!.location)
            etDate.setText(mHappyPlaceDetails!!.date)
            mLatitude= mHappyPlaceDetails!!.latitude
            mLongitude= mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage= Uri.parse(mHappyPlaceDetails!!.image)
            ivPlaceImage.setImageURI(saveImageToInternalStorage)
            btnSave.text= "UPDATE"
        }
        etDate.setOnClickListener(this) // We have extended the onClickListener above and the override method as onClick added and here we are setting a listener to date edittext.
        tvAddImage.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        etLocation.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // Launching the dataPicker dialog on click of date edittext.
            // START
            R.id.etDate -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener, // This is the variable which have created globally and initialized in setupUI method.
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR), // Here the cal instance is created globally and used everywhere in the class where it is required.
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tvAddImage -> {
                val pictureDialog= AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItem= arrayOf("Select Photo from Gallery", "Capture Photo from Camera")
                pictureDialog.setItems(pictureDialogItem){
                        dialog, which ->
                    when(which)
                    {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.btnSave->{
                when{
                    etTitle.text.isNullOrEmpty()->{
                        Toast.makeText(applicationContext, "Please enter Title", Toast.LENGTH_LONG).show()
                    }

                    etDescription.text.isNullOrEmpty()->{
                        Toast.makeText(applicationContext, "Please enter Description", Toast.LENGTH_LONG).show()
                    }

                    etLocation.text.isNullOrEmpty()->{
                        Toast.makeText(applicationContext, "Please enter Location", Toast.LENGTH_LONG).show()
                    }

                    saveImageToInternalStorage== null ->{
                        Toast.makeText(applicationContext, "Please select an Image", Toast.LENGTH_LONG).show()
                    }else->{

                        val happyPlaceModel: happyPlaceModel= happyPlaceModel(
                            if(mHappyPlaceDetails== null) 0 else mHappyPlaceDetails!!.id,
                        etTitle.text.toString(),
                        saveImageToInternalStorage.toString(),
                        etDescription.text.toString(),
                        etDate.text.toString(),
                        etLocation.text.toString(),
                        mLatitude,
                        mLongitude)

                        val dataBaseHandler= dataBaseHandler(this)

                    if(mHappyPlaceDetails== null)
                    {
                        val status= dataBaseHandler.addHappyPlace(happyPlaceModel)
                        if(status> 0)
                        {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                    }else{
                        val status= dataBaseHandler.updateHappyPlace(happyPlaceModel)
                        if(status> 0)
                        {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                    }
                }
            }

            R.id.etLocation->{
                try {
                    // These are the list of fields which we required is passed
                    val field= listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS)

                    // Start the autocomplete intent with a unique request code.
                    val intent= Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, field).build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK)
        {
            if(requestCode== GALLERY)
            {
                if(data!= null)
                {
                    val contentURI= data.data
                    try {
                        val selectedImageBitmap= MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        // Saving an image which is selected from GALLERY. And printed the path in logcat.
                        saveImageToInternalStorage= saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image", "Path: $saveImageToInternalStorage")
                        ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException)
                    {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,"Failed to load image from Gallery", Toast.LENGTH_LONG).show()
                    }
                }

            }
            if(requestCode== CAMERA)
            {
                val thumbNail: Bitmap= data!!.extras!!.get("data") as Bitmap

                // Saving an image which is selected from CAMERA. And printed the path in logcat.
                saveImageToInternalStorage= saveImageToInternalStorage(thumbNail)
                Log.e("Saved Image", "Path: $saveImageToInternalStorage")
                ivPlaceImage.setImageBitmap(thumbNail)
            }

            if(requestCode== PLACE_AUTOCOMPLETE_REQUEST_CODE)
            {
                val place: Place= Autocomplete.getPlaceFromIntent(data!!)
                etLocation.setText(place.address)
                mLatitude= place.latLng!!.latitude
                mLongitude= place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera()
    {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted())
                {
                    val galleryIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery()
    {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted())
                {
                    val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private  fun showRationalDialogForPermissions()
    {
        AlertDialog.Builder(this@AddHappyPlaceActivity).setMessage("It looks like you have turned Off the required permissions for this feature. It can enabled under ApplicationS Setting.")
            .setPositiveButton("GO TO SETTINGS")
            {
                _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun updateDateInView()// Created a function as updateDateInView where after selecting a date from date picker is populated in the UI component.
    {
        val myFormat= "dd.MM.yyyy"
        val sdf= SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(cal.time).toString())
    }

    // Creating a method to save a copy of an selected image to internal storage for use of Happy Places App.
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri
    {
        // Get the context wrapper instance
        val wrapper= ContextWrapper(applicationContext)

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file= wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        // Create a file to save the image
        file= File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream= FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException)
        {
           e.printStackTrace()
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }

    companion object{
        private const val GALLERY= 1
        private const val CAMERA= 2
        private const val IMAGE_DIRECTORY= "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE= 3
    }
}