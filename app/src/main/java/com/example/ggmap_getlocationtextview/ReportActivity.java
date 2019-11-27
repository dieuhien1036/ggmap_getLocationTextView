package com.example.ggmap_getlocationtextview;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {
    private static String id = "a";
    private static String JSON_STRING;
    private static final String UPLOAD_URL = "http://192.168.1.3//upload/insert_image.php";
    private static final int IMAGE_REQUEST_CODE = 3;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private ImageView imageView;
    private EditText etCaption;
    private TextView tvPath,tvIdmax;
    private ImageButton btnReport;
    private Bitmap bitmap;
    private Uri filePath;
    private RadioButton radioButton_Small, radioButton_Medium, radioButton_Large;
    private String size = "";
    private EditText etMaterial;
    private EditText etNumberOfPeople;
    private double latitude, longtitude;
    private String addressWaste;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_activity);
        reflect();
        requestStoragePermission();

        imageView.setOnClickListener(this);
        btnReport.setOnClickListener(this);

    }
    private void reflect(){
        //id = tvIdmax.getText().toString();
        imageView = (ImageView) findViewById(R.id.image);
        etCaption = (EditText) findViewById(R.id.etCaption);
        etMaterial = findViewById(R.id.etMaterial);
        etNumberOfPeople = findViewById(R.id.etNumberOfPeople);
        tvPath = (TextView) findViewById(R.id.path);
        radioButton_Small = findViewById(R.id.radioButton_Small);
        radioButton_Medium = findViewById(R.id.radioButton_Medium);
        radioButton_Large = findViewById(R.id.radioButton_Large);
        btnReport=findViewById((R.id.btnReport));
        tvIdmax = findViewById(R.id.idmax);
    }
    @Override
    public void onClick(View view) {
        if (view == imageView) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE);
        } else if (view == btnReport) {
            uploadMultipart();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            uploadMultipart1();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                tvPath.setText("Path: ".concat(getPath(filePath)));
                imageView.setImageBitmap(bitmap);
                //XU LY TAI DAY
                uploadMultipart1();
                //HIEN THI NHANH QUA
                Toast.makeText(this, "Loading recommend please wait...", Toast.LENGTH_SHORT).show();
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    public void run() {
                        getPeople();
                        getSize();
                        getMaterial();
                    }
                }, 6600);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadMultipart1() {
        String url = "http://192.168.1.3//upload/insert_image1.php";

        String caption = etCaption.getText().toString().trim();
        //String size=etSize.getText().toString().trim();
        //getting the actual path of the image
        String path = getPath(filePath);
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, url)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", caption) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            //Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void uploadMultipart() {
        Intent intent=getIntent();
        latitude=intent.getDoubleExtra("wasteLocation_latitude",0.);
        longtitude=intent.getDoubleExtra("wasteLocation_longtitude",0.);
        addressWaste = intent.getStringExtra("wasteLocation_address");

        if (radioButton_Small.isChecked()) {
            size = "Small";
        }
        if (radioButton_Medium.isChecked()) {
            size = "Medium";
        }
        if (radioButton_Large.isChecked()) {
            size = "Large";
        }
        String caption = etCaption.getText().toString().trim();
        //String size=etSize.getText().toString().trim();
        String material = etMaterial.getText().toString().trim();
        String people = etNumberOfPeople.getText().toString().trim();
        //getting the actual path of the image
        String path = getPath(filePath);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, UPLOAD_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", caption) //Adding text parameter to the request
                    .addParameter("size", size)
                    .addParameter("material", material)
                    .addParameter("people", people)
                    .addParameter("wasteLocation_longtitude", String.valueOf(longtitude))
                    .addParameter("wasteLocation_latitude", String.valueOf(latitude))
                    .addParameter("wasteLocation_address", String.valueOf(addressWaste))
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private UploadServiceBroadcastReceiver receiver = new UploadServiceBroadcastReceiver(){
        @Override
        public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {
            super.onCompleted(uploadId, serverResponseCode, serverResponseBody);
            Intent intentBackMap=new Intent(ReportActivity.this, MapsActivity.class);
            startActivity(intentBackMap);

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        receiver.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.unregister(this);
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }

    }

    public void getId()
    {
        new docId().execute();
    }
    public void getPeople()
    {
        new docSoNguoi().execute();
    }
    public void getMaterial()
    {
        new docChatLieu().execute();
    }
    public void getSize()
    {
        new docSize().execute();
    }


    public class docSoNguoi extends AsyncTask<Void, Void, String>{

        String url;

        @Override
        protected void onPreExecute() {
            url = "http://192.168.1.3/upload/getPeople.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url1 = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url1.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(JSON_STRING+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }
            catch (MalformedURLException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            etNumberOfPeople.setText(result);
        }
    }

    public class docId extends AsyncTask<Void, Void, String>{

        String url;

        @Override
        protected void onPreExecute() {
            url = "http://192.168.1.3/upload/Count.txt";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url1 = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url1.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(JSON_STRING+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }
            catch (MalformedURLException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            tvIdmax.setText(result);
        }
    }

    public class docChatLieu extends AsyncTask<Void, Void, String>{

        String url;

        @Override
        protected void onPreExecute() {
            url = "http://192.168.1.3/upload/getMaterial.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url1 = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url1.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(JSON_STRING+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }
            catch (MalformedURLException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            etMaterial.setText(result);
        }
    }

    public class docSize extends AsyncTask<Void, Void, String>{

        String url;

        @Override
        protected void onPreExecute() {
            url = "http://192.168.1.3/upload/getSize.php";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try{
                URL url1 = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url1.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((JSON_STRING = bufferedReader.readLine())!= null)
                {
                    stringBuilder.append(JSON_STRING+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim();
            }
            catch (MalformedURLException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Small")) {
                radioButton_Small.setChecked(true);
            }
            if (result.equals("Medium")) {
                radioButton_Medium.setChecked(true);
            }
            if (result.equals("Large")) {
                radioButton_Large.setChecked(true);
            }
        }
    }
    public static class SingleUploadBroadcastReceiver extends UploadServiceBroadcastReceiver {

        public interface Delegate {
            void onProgress(int progress);
            void onProgress(long uploadedBytes, long totalBytes);
            void onError(Exception exception);
            void onCompleted(int serverResponseCode, byte[] serverResponseBody);
            void onCancelled();
        }

        private String mUploadID;
        private Delegate mDelegate;

        public void setUploadID(String uploadID) {
            mUploadID = uploadID;
        }

        public void setDelegate(Delegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void onProgress(String uploadId, int progress) {
            if (uploadId.equals(mUploadID) && mDelegate != null) {
                mDelegate.onProgress(progress);
            }
        }

        @Override
        public void onProgress(String uploadId, long uploadedBytes, long totalBytes) {
            if (uploadId.equals(mUploadID) && mDelegate != null) {
                mDelegate.onProgress(uploadedBytes, totalBytes);
            }
        }

        @Override
        public void onError(String uploadId, Exception exception) {
            if (uploadId.equals(mUploadID) && mDelegate != null) {
                mDelegate.onError(exception);
            }
        }

        @Override
        public void onCompleted(String uploadId, int serverResponseCode, byte[] serverResponseBody) {
            if (uploadId.equals(mUploadID) && mDelegate != null) {
                mDelegate.onCompleted(serverResponseCode, serverResponseBody);
            }
        }

        @Override
        public void onCancelled(String uploadId) {
            if (uploadId.equals(mUploadID) && mDelegate != null) {
                mDelegate.onCancelled();
            }
        }
    }

}

