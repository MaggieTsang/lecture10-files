package edu.uw.filedemo;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "Photo";

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri pictureFileUri = null; //for saving
    private Uri mediaStoreUri = null; //for sharing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        //action bar "back"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void takePicture(View v){
        Log.v(TAG, "Taking picture...");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File file = null;
            try {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); //include timestamp

                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                file = new File(dir, "PIC_"+timestamp+".jpg");
                boolean created = file.createNewFile(); //actually make the file!
                Log.v(TAG, "File created: "+created);

            } catch (IOException ioe) {
                Log.d(TAG, Log.getStackTraceString(ioe));
            }

            if(file != null){ //make sure we can save the file!
                pictureFileUri = Uri.fromFile(file);
                Log.v(TAG, "Uri: "+pictureFileUri);

                //for sharing the media (produces a Uri the Messenger has permissions for)
                MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                mediaStoreUri = uri;
                                Log.v(TAG, "MediaStore Uri: "+uri);
                            }
                        });


                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            ImageView imageView = (ImageView)findViewById(R.id.img_thumbnail);
            imageView.setImageURI(pictureFileUri);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void sharePicture(View v){
        Log.v(TAG, "Sharing picture...");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, mediaStoreUri);
        Log.v(TAG, "Uri: "+mediaStoreUri);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
