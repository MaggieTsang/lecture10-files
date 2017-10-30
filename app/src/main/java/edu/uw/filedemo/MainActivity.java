package edu.uw.filedemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import static android.os.Environment.getExternalStoragePublicDirectory;

//TODO: Refactor to avoid repeated code
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    private static final String FILE_NAME = "myFile2.txt";

    private static final int WRITE_REQUEST_CODE = 1;

    private EditText textEntry; //save reference for quick access
    private RadioButton externalButton; //save reference for quick access

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        externalButton = (RadioButton)findViewById(R.id.radio_external);
        textEntry = (EditText)findViewById(R.id.txt_entry); //what we're going to save
    }

    //handle the save button press
    public void saveFile(View v){
        Log.v(TAG, "Saving file...");

        if(externalButton.isChecked()){ //external storage
            if(isExternalStorageWritable()){
                //check permission to write
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    //have permission, can go ahead and do stuff
                    Log.v(TAG, "Permission granted!");
                    saveToExternalFile();
                }
                else { //if we're missing permission.
                    Log.v(TAG, "Permission denied!");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
                }
            }
        }
        else { //internal storage
            try {
                FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);

                PrintWriter out = new PrintWriter(fos);
                out.println(textEntry.getText().toString());
                out.close();

            }catch (IOException ioe){
                Log.d(TAG, Log.getStackTraceString(ioe));
            }
        }
    }

    //actually write to the file
    private void saveToExternalFile(){
        try {
            File dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!dir.exists()) {
                dir.mkdirs(); //make Documents directory if doesn't otherwise exist << emulator workaround
            }
            File file = new File(dir, FILE_NAME);
            Log.v(TAG, "Saving to  " + file.getAbsolutePath());

            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.println(textEntry.getText().toString());
            out.close();

        } catch (IOException ioe) {
            Log.d(TAG, Log.getStackTraceString(ioe));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case WRITE_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //now we have permissions!
                    saveToExternalFile(); //do the work
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void loadFile(View v){
        Log.v(TAG, "Loading file...");
        TextView textDisplay = (TextView)findViewById(R.id.txt_display); //what we're going to save
        textDisplay.setText(""); //clear initially

        if(externalButton.isChecked()){ //external storage
            if (isExternalStorageWritable()) {
                try {
                    File dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File file = new File(dir, FILE_NAME);
                    if(!file.exists()) return;

                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    StringBuilder text = new StringBuilder();

                    //read the file
                    String line = reader.readLine();
                    while (line != null) {
                        text.append(line + "\n");
                        line = reader.readLine();
                    }

                    textDisplay.setText(text.toString());

                    reader.close();

                } catch (IOException ioe) {
                    Log.d(TAG, Log.getStackTraceString(ioe));
                }
            }
        }
        else { //internal storage

            try {
                File file = new File(getFilesDir(), FILE_NAME);
                if(!file.exists()) return;

                FileInputStream fis = openFileInput(FILE_NAME);

                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder text = new StringBuilder();

                //read the file
                String line = reader.readLine();
                while (line != null) {
                    text.append(line + "\n");
                    line = reader.readLine();
                }

                textDisplay.setText(text.toString());

                reader.close();
            } catch (IOException ioe) {
                Log.d(TAG, Log.getStackTraceString(ioe));
            }
        }
    }


    public void shareFile(View v) {
        Log.v(TAG, "Sharing file...");

        Uri fileUri = null;
        if(externalButton.isChecked()){ //external storage
            //File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS); //
            File dir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(dir, FILE_NAME);
            fileUri = Uri.fromFile(file);
        }
        else { //internal storage
            File dir = getFilesDir();
            File file = new File(dir, FILE_NAME);
            fileUri = Uri.fromFile(file);
        }

        if(fileUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri); //external app needs permission to this (public) Uri

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }


    //helper method to confirm external storage is around
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_photo:
                startActivity(new Intent(MainActivity.this, PhotoActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}