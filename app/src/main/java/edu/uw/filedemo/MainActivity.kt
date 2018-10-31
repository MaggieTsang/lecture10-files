package edu.uw.filedemo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView

//TODO: Refactor to avoid repeated code
class MainActivity : AppCompatActivity() {

    private val TAG = "Main"

    private val FILE_NAME = "myFile.txt"

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
            saveToExternalFile()
        } else { //internal storage

        }
    }

    //actually write to the file
    private fun saveToExternalFile() {

    }




    fun loadFile(v: View) {
        Log.v(TAG, "Loading file...")
        val textDisplay = findViewById<TextView>(R.id.txt_display) //what we're going to save
        textDisplay.text = "" //clear initially

        if (externalButton.isChecked) { //external storage


        }
        else { //internal storage

        }
    }



    fun shareFile(v: View) {
        Log.v(TAG, "Sharing file...")

        if (externalButton.isChecked) { //external storage

        }
        else { //internal storage
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