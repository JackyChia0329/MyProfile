package com.example.myprofile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*

class MainActivity : AppCompatActivity() {
    var IMAGE_PICK_CODE = 1000
    var CAMERA_REQUEST_CODE = 123
    val db= FirebaseFirestore.getInstance()
    lateinit var imageUri:Uri
    lateinit var name:String
    lateinit var pass:String
    lateinit var p : Profile



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         var image = findViewById<View>(R.id.circularImageView)
//        Toast.makeText(this,"fk",Toast.LENGTH_SHORT).show()
        getProfile()

        image.setOnClickListener(){
            openFileChooser()
        }
        UpdateBtn.setOnClickListener(){
            update()
        }
        btnChangePass.setOnClickListener {
            val intent = Intent(this,passwordPage::class.java)
//            intent.putExtra("name",p.name)
//            intent.putExtra("pass",p.password)
//            intent.putExtra("uri",p.imageint)
            startActivity(intent)
        }
    }

    private fun getProfile() {
        db.collection("Profile").document("jacky")
            .get()
            .addOnSuccessListener {

                if (it != null && it.exists()) {
//                    Toast.makeText(this, "got", Toast.LENGTH_SHORT).show()
                    name = it.get("name").toString()
                    Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
                    editTextName.setText(name)
                    imageUri = Uri.parse(it.get("pic").toString())
                    pass = it.get("pass").toString()

                    p = Profile(it.get("imageint").toString(),name,pass)

                    try {
                        Picasso.get().load(it.get("imageint").toString()).into(circularImageView)
                    }catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }

            }
            .addOnFailureListener{
                    exception ->
                //                Toast.makeText(MainActivity(),"not",Toast.LENGTH_SHORT).show()
                Log.d("Error DB","Fail to load account",exception)
            }

    }

    private fun update() {
        if (imageUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")

        ref.putFile(imageUri!!)
            .addOnSuccessListener{
                Log.d("UpdateActivity", "Successfully updated: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("UpdateActivity","File Location: $it")
                    Toast.makeText(this,"yes",Toast.LENGTH_SHORT).show()
                    p = Profile(it.toString(),name,pass)
                    updateUser(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
                Log.d("RegisterActivity", "Failed to upload image")
            }

    }

    private fun updateUser(imageURI: String) {
//        Toast.makeText(this,imageURI.toString(),Toast.LENGTH_SHORT).show()
        db.collection("Profile").document("jacky")
            .set(p)
            .addOnSuccessListener {
                Toast.makeText(this,"sucess",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this,"fail",Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFileChooser() {

//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Profile pic")
//            builder.setMessage("Choose the method u want")
//            builder.setPositiveButton("take selfie"){dialog, which ->
//                Toast.makeText(this,"smile",Toast.LENGTH_SHORT).show()
//                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//                if (intent.resolveActivity(packageManager) != null) {
//                    startActivityForResult(Intent.createChooser(intent,"select photo"), CAMERA_REQUEST_CODE)
//                }
//            }
//            builder.setNegativeButton("gallery"){dialog, which ->
                Toast.makeText(this,"select a photo",Toast.LENGTH_SHORT).show()
                val intent=Intent()
                intent.type="image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent,"select picture"),IMAGE_PICK_CODE)
//            }
//        builder.show()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {

            imageUri = data?.data!!
            try {
                Picasso.get().load(imageUri).into(circularImageView)
//                Toast.makeText(this,imageUri.toString(),Toast.LENGTH_SHORT).show()
            }catch (e: Throwable) {
                e.printStackTrace()
            }
        }else if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            imageUri =data?.data!!
            val extras = data?.getExtras()
            val imageBitMap = extras?.get("data") as Bitmap
            circularImageView.setImageBitmap(imageBitMap)
//            Toast.makeText(this,imageUri.toString(),Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this,"fail to upload",Toast.LENGTH_SHORT).show()
        }
    }

}
