package edu.uw.filedemo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView

class PhotoActivity : AppCompatActivity() {

    private val TAG = "Photo"

    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        //action bar "back"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    fun takePicture(v: View?) {
        Log.v(TAG, "Taking picture...")

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras?.apply {
                val imageBitmap = get("data") as Bitmap
                val imageView = findViewById<ImageView>(R.id.img_thumbnail)
                imageView.setImageBitmap(imageBitmap)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun sharePicture(v: View) {
        Log.v(TAG, "Sharing picture...")

    }
}
