package com.example.ggmap_getlocationtextview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class JoinActivity extends AppCompatActivity {
    TextView txt_address;
    TextView txt_people;
    TextView txt_size;
    TextView txt_user;
    TextView txt_phone;
    TextView txt_job;
    ImageView img_wasted;
    Button btn_join;
    DirectionFinderListener listener;
    Double wasteLatitude;
    Double wasteLongtitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join3);

        reflect();
        Bundle bundle = getIntent().getExtras();

        String image_url = bundle.getString("image_url");
        wasteLatitude = bundle.getDouble("wasteLatitude");
        wasteLongtitude = bundle.getDouble("wasteLongtitude");

        new JoinActivity.LoadImages().execute(image_url);

        if (bundle != null) {
            if (bundle.getString("address") != null) {
                txt_address.setText(bundle.getString("address"));
            }
            if (bundle.getString("people") != null) {
                txt_people.setText(bundle.getString("people"));
            }
            if (bundle.getString("size") != null) {
                txt_size.setText(bundle.getString("size"));
            }
            if (bundle.getString("job") != null) {
                txt_job.setText(bundle.getString("job"));
            }
            if (bundle.getString("username") != null) {
                txt_user.setText(bundle.getString("username"));
            }
            if (bundle.getString("phoneNumber") != null) {
                txt_phone.setText(bundle.getString("phoneNumber"));
            }
        }

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("JoinButton-a","Vào đây1");
                listener.changeColorJoinMaker(wasteLatitude, wasteLongtitude);
            }
        });
    }

    private void reflect(){
        txt_address = (TextView) findViewById(R.id.txt_address);
        txt_people = (TextView) findViewById(R.id.txt_people);
        txt_size = (TextView) findViewById(R.id.txt_size);
        txt_job = (TextView) findViewById(R.id.txt_job);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_user = (TextView) findViewById(R.id.txt_user);
        img_wasted = (ImageView) findViewById(R.id.img_wasted);
        btn_join = (Button) findViewById(R.id.btn_join);

    }
    //class doc du lieu anh
    public class LoadImages extends AsyncTask<String, Void, Bitmap> {
        Bitmap bitmaphinh;
        InputStream inputStream = null;
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                try {
                    inputStream = url.openConnection().getInputStream();
                    bitmaphinh = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return bitmaphinh;
        }

        @Override //hien thi anh len imageview
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            img_wasted.setImageBitmap(bitmaphinh);
        }

    }

}
