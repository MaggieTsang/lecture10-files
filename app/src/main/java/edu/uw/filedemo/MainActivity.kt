package edu.uw.filedemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import java.io.*

//TODO: Refactor to avoid repeated code
class MainActivity : AppCompatActivity() {

    private val TAG = "Main"

    private val FILE_NAME = "myFile2.txt"
    private val WRITE_REQUEST_CODE = 1

    private lateinit var textEntry: EditText //save reference for quick access
    private lateinit var externalButton: RadioButton //save reference for quick access

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        externalButton = findViewById<RadioButton>(R.id.radio_external)
        textEntry = findViewById<EditText>(R.id.txt_entry) //what we're going to save
    }

    //handle the save button press
    fun saveFile(v: View) {
        Log.v(TAG, "Saving file...")

        if (externalButton.isChecked) { //external storage
            if (isExternalStorageWritable()) {
                //check permission to write
                val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    //have permission, can go ahead and do stuff
                    Log.v(TAG, "Permission granted!")
                    saveToExternalFile()
                } else { //if we're missing permission.
                    Log.v(TAG, "Permission denied!")
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_CODE)
                }
            }
        } else { //internal storage
            try {
                val fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE)

                val out = PrintWriter(fos)
                out.println(textEntry.text.toString())
                out.close()

            } catch (ioe: IOException) {
                Log.d(TAG, Log.getStackTraceString(ioe))
            }

        }
    }

    //actually write to the file
    private fun saveToExternalFile() {
        try {
            val dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!dir.exists()) {
                dir.mkdirs() //make Documents directory if doesn't otherwise exist << emulator workaround
            }
            val file = File(dir, FILE_NAME)
            Log.v(TAG, "Saving to  " + file.absolutePath)

            val out = PrintWriter(FileWriter(file, true))
            out.println(textEntry.text.toString())
            out.close()

        } catch (ioe: IOException) {
            Log.d(TAG, Log.getStackTraceString(ioe))
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //now we have permissions!
                    saveToExternalFile() //do the work
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    fun loadFile(v: View) {
        Log.v(TAG, "Loading file...")
        val textDisplay = findViewById<View>(R.id.txt_display) as TextView //what we're going to save
        textDisplay.text = "" //clear initially

        if (externalButton.isChecked) { //external storage
            if (isExternalStorageWritable()) {
                try {
                    val dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val file = File(dir, FILE_NAME)
                    if (!file.exists()) return

                    val reader = BufferedReader(FileReader(file))
                    val text = StringBuilder()

                    //read the file
                    var line: String? = reader.readLine()
                    while (line != null) {
                        text.append(line + "\n")
                        line = reader.readLine()
                    }

                    textDisplay.text = text.toString()

                    reader.close()

                } catch (ioe: IOException) {
                    Log.d(TAG, Log.getStackTraceString(ioe))
                }

            }
        } else { //internal storage

            try {
                val file = File(filesDir, FILE_NAME)
                if (!file.exists()) return

                val fis = openFileInput(FILE_NAME)

                val reader = BufferedReader(InputStreamReader(fis))
                val text = StringBuilder()

                //read the file
                var line: String? = reader.readLine()
                while (line != null) {
                    text.append(line + "\n")
                    line = reader.readLine()
                }

                textDisplay.text = text.toString()

                reader.close()
            } catch (ioe: IOException) {
                Log.d(TAG, Log.getStackTraceString(ioe))
            }

        }
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun shareFile(v: View) {
        Log.v(TAG, "Sharing file...")

        var dir = if (externalButton.isChecked) { //external storage
            //File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS); //
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) //return
        } else { //internal storage
            filesDir //return
        }

        val file = File(dir, FILE_NAME) //return
        Log.v(TAG, "Sharing ${file.absolutePath}")
        val fileUri: Uri? =
            FileProvider.getUriForFile(
                    this,
                    "edu.uw.filedemo.fileprovider",
                    file)

        if (fileUri != null) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_STREAM, fileUri) //external app needs permission to this (public) Uri
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.share_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_photo -> {
                startActivity(Intent(this, PhotoActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}