package com.example.chattery.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.chattery.Columns
import com.example.chattery.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class SettingsActivity : AppCompatActivity() {
    //Firebase user & reference to users Database
    lateinit var mDataBaseRef: DatabaseReference
    lateinit var mCurrentUser: FirebaseUser
    lateinit var mUserId:String

    //Firebase Storage reference
    lateinit var mStorageRef: StorageReference

    lateinit var mProfilePic: CircularImageView
    lateinit var mUsername: TextView
    lateinit var mStatus: TextView
    lateinit var mSetStatusButton: Button
    lateinit var mSetPicButton: Button

    lateinit var mProgress:ProgressDialog;

    private val REQUEST_IMAGE = 0;
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mProfilePic = findViewById(R.id.settings_profile_image)
        mUsername = findViewById(R.id.settings_username)
        mStatus = findViewById(R.id.settings_status)

        mSetPicButton = findViewById(R.id.settings_set_photo)
        mSetPicButton.setOnClickListener {
            startImageCropper()
        }

        mSetStatusButton = findViewById(R.id.settings_set_status)
        mSetStatusButton.setOnClickListener {
            if (!isNetworkAvailableAndConnected()) {
                Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setStatusDialog()
        }

        //Get current user ID
        mCurrentUser = FirebaseAuth.getInstance().currentUser!!
        mUserId = mCurrentUser.uid

        //Get current user instance from the database
        mDataBaseRef = FirebaseDatabase.getInstance().reference.child(Columns.Users).child(mUserId)
        mDataBaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            //Update the view as soon as the data is retrieved from the database
            override fun onDataChange(snapshot: DataSnapshot) {
                //Retrieve data
                val UserName = snapshot.child(Columns.UserName).value.toString()
                val Status = snapshot.child(Columns.Status).value.toString()
                val Image = snapshot.child(Columns.Image).value.toString()
                val ThumbImage = snapshot.child(Columns.ImageThumbnail).value.toString()

                //Update UI
                mUsername.text = UserName
                mStatus.text = Status
                if(!Image.equals("default")){
                    Picasso.get().load(Image).placeholder(R.drawable.avatar_empty).into(mProfilePic);
                }
            }

        })

    }

    private fun startImageCropper() {
        val imageIntent = Intent()
        imageIntent.type = "image/*"
        imageIntent.action = Intent.ACTION_GET_CONTENT
        val chooserIntent = Intent.createChooser(imageIntent, "Choose Image")

        startActivityForResult(chooserIntent, REQUEST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "Got image URI from local storage")
            val imageURI = data?.data

            //Start a crop activity using the image uri recieved
            CropImage.activity(imageURI)
                .setAspectRatio(1, 1)
                .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            //image uri
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                //Add Croped image to firebase storage
                Log.i(TAG, "Recived cropped image")
                val resultUri = result.uri!!
                addUserImageAndThumbnailToDatabase(resultUri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }

    private fun addUserImageAndThumbnailToDatabase(imageUri: Uri) {
        Log.i(TAG, "Adding image to firebase storage")

        //Image and thumbnail references in Firebase
        mStorageRef = FirebaseStorage.getInstance().reference
        val mImagePath = mStorageRef.child(Columns.ProfileImagesDirectory).child( mUserId + ".jpg")
        val mThumbnailPath = mStorageRef.child(Columns.ProfileImagesDirectory).child(Columns.ProfileThumbnailDirectory).child( mUserId + ".jpg")

        // create a compressed image from image uri
        val thumbnail_file = File(imageUri.path)

        var thumbnail_imageUri: Uri? = null
        GlobalScope.launch(Dispatchers.IO) {
            thumbnail_imageUri = Uri.fromFile(Compressor.compress(this@SettingsActivity, thumbnail_file, Dispatchers.Main) {
                quality(75)
                resolution(500, 500)
            })
        }


        //Create and start dialog
        mProgress = ProgressDialog(this)
        mProgress.setTitle("Uploading...")
        mProgress.setMessage("Please wait while we upload the image")
        mProgress.setCanceledOnTouchOutside(false)
        mProgress.show()

        // Add image to database storage
        mImagePath.putFile(imageUri).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Image added to firebase storage successfully")
                //When successful add thumbnail to storage
                mThumbnailPath.putFile(thumbnail_imageUri!!).addOnCompleteListener {
                    if (it.isSuccessful){
                        Log.i(TAG, "Thumbnnail added to firebase storage successfully")

                        // Get the image and thumbnail urls:
                        var image_url:String? = null
                        var thumbnail_url:String? = null

                        mImagePath.downloadUrl.addOnSuccessListener {
                            image_url = it.toString()
                            mThumbnailPath.downloadUrl.addOnSuccessListener {
                                thumbnail_url = it.toString()
                                updateDatabaseImageAndThumbnail(image_url!!,thumbnail_url!!)
                            }
                        }
                    }else{
                        mProgress.dismiss()
                        Log.i(TAG, "Failed to add thumbnail to firebase storage")
                    }
                }
            } else {
                mProgress.dismiss()
                Log.i(TAG, "Failed to add image to firebase storage")
                Toast.makeText(this, "Failed to add Image to Database", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDatabaseImageAndThumbnail(imageUrl: String, thumbnailUrl:String) {
        val data = HashMap<String,Any?>()
        data[Columns.Image] = imageUrl
        data[Columns.ImageThumbnail] = thumbnailUrl

        mDataBaseRef.updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful){
                mProgress.dismiss()
                Log.i(TAG, "Updated image and thumbnail values in database")
            }else{
                mProgress.dismiss()
                Log.i(TAG,"Failed to update image and thumbnail values in database")
            }
        }
    }


    private fun setStatusDialog() {
        val view = layoutInflater.inflate(R.layout.status_dialog, null)
        val textview = view.findViewById<EditText>(R.id.status_dialog_edittext)
        textview.setText(mStatus.text)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Set Status")
            .setView(view)
            .setPositiveButton(android.R.string.ok) { p0, p1 ->
                if (!TextUtils.isEmpty(textview.text.toString())) changeStatus(textview.text.toString()) else Toast.makeText(
                    this,
                    "Empty status!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .create()

        dialog.show()
    }

    private fun changeStatus(newStatus: String) {
        //Update user status field in database
        mDataBaseRef.child(Columns.Status).setValue(newStatus).addOnCompleteListener {
            if (it.isSuccessful) {

                Log.d("Dialog", "new status saved")
                Toast.makeText(this@SettingsActivity, "New status saved", Toast.LENGTH_LONG).show()
            } else {

                Log.d("Dialog", "new status not saved")
                Toast.makeText(
                    this@SettingsActivity,
                    "Couldn't save changes try again later",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun isNetworkAvailableAndConnected(): Boolean {
        val connectivitymanager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return !(connectivitymanager.activeNetworkInfo == null || !connectivitymanager.activeNetworkInfo.isConnected)
    }

}
