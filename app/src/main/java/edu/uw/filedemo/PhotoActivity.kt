package edu.uw.filedemo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoActivity : AppCompatActivity() {

    private val TAG = "Photo"

    private val WRITE_REQUEST_CODE = 1
    private val REQUEST_IMAGE_CAPTURE = 1

    private var pictureFilePath: String? = null //where the picture is saved
    private var pictureFileUri: Uri? = null //content provider Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        //action bar "back"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    fun takePicture(v: View?) {
        Log.v(TAG, "Taking picture...")

        //permission check if we don't have it yet...
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //have permission, can go ahead and do stuff
            Log.v(TAG, "Permission granted!")
            //continue...
        } else { //if we're missing permission.
            Log.v(TAG, "Permission denied!")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_CODE)
            return //end
        }

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            var file: File? = try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) //include timestamp
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                val file = File(dir, "PIC_$timestamp.jpg")
                file.createNewFile() //actually make the file!
                file //return
            } catch (ioe: IOException) {
                Log.d(TAG, Log.getStackTraceString(ioe))
                null //return
            }

            if (file != null) { //make sure we can save the file!
//                pictureFileUri = Uri.fromFile(file)

                pictureFilePath = file.absolutePath;
                Log.v(TAG, "File created at $pictureFilePath")

                pictureFileUri = FileProvider.getUriForFile(
                                this,
                                "edu.uw.filedemo.fileprovider",
                                file)

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //now we have permissions!
                    takePicture(null) //do the work
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            val imageView = findViewById<View>(R.id.img_thumbnail) as ImageView
            imageView.setImageURI(pictureFileUri)

            //let other media services also know about the new picture!
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(pictureFilePath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun sharePicture(v: View) {
        Log.v(TAG, "Sharing picture...")

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, pictureFileUri) //already made available!
        Log.v(TAG, "Uri: $pictureFileUri")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
