package com.example.ggmap_getlocationtextview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class joinDialog extends BottomSheetDialogFragment implements DirectionFinderListener{
    private BottomSheetListener mListener;
    private TextView txt_address;
    private Double currentLatitude ;
    private Double currentLongtitude ;
    private Double wasteLatitude ;
    private Double wasteLongtitude ;
    private ImageView img_wasted;
    private ImageButton btn_direction;
    private Button btn_join;
    private TextView txt_size;
    private TextView txt_people;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.join_dialog_layout, container, false);
//        Bundle bundle = getArguments();
//        String str = bundle.getString("key","");

        //getData("http://192.168.1.6/upload/uploads/1.jpg");
        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reflect(view);

        //lấy giá trị từ Mapacivity qua
        final String address = getArguments().getString("address");
        final String people = getArguments().getString("people");
        final String size = getArguments().getString("size");
        final String username = getArguments().getString("username");
        final String dateOfBirth = getArguments().getString("dateOfBirth");
        final String phoneNumber = getArguments().getString("phoneNumber");
        final String userJob = getArguments().getString("job");
        final String userGender = getArguments().getString("gender");
        final String image_name = getArguments().getString("image_name").trim();

        currentLatitude = getArguments().getDouble("currentLatitude",0);
        currentLongtitude = getArguments().getDouble("currentLongtitude",0);
        wasteLatitude = getArguments().getDouble("wasteLatitude",0);
        wasteLongtitude = getArguments().getDouble("wasteLongtitude",0);



        //set giá trị cho joinDialog.
        txt_address.setText(address);
        txt_people.setText(people);
        txt_size.setText(size);

        //Load image của this waste vào dialog
        final String image_url = "http://192.168.1.2/upload/uploads/" + image_name;
        new LoadImages().execute(image_url);

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), JoinActivity.class);
                intent.putExtra("address", address);
                intent.putExtra("people",people);
                intent.putExtra("size",size);
                intent.putExtra("username",username);
                intent.putExtra("dateOfBirth",dateOfBirth);
                intent.putExtra("phoneNumber",phoneNumber);
                intent.putExtra("job",userJob);
                intent.putExtra("gender",userGender);
                intent.putExtra("image_url",image_url);
                intent.putExtra("wasteLatitude",wasteLatitude);
                intent.putExtra("wasteLongtitude",wasteLongtitude);
                startActivity(intent);
                onStop();

            }
        });

        btn_direction = view.findViewById(R.id.btn_direction);
        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequestDirection();
                onStop();
            }
        });

    }

    private void reflect(View view){
        img_wasted = view.findViewById(R.id.img_wasted);
        txt_address = view.findViewById(R.id.txt_address);
        btn_join = view.findViewById(R.id.btn_join);
        txt_size = view.findViewById(R.id.txt_size);
        txt_people = view.findViewById(R.id.txt_people);
    }

    public void sendRequestDirection(){
        DirectionFinder a = new DirectionFinder(this,currentLatitude,currentLongtitude,wasteLatitude,wasteLongtitude);
        a.execute();
    }

    @Override
    public void setText(List<Route> routes) {
        mListener.onDirectionFinderSuccess(routes);
    }

    @Override
    public void changeColorJoinMaker(double wasteJoinLat, double wasteJoinLon) {
        Log.e("JoinButton-a","Vào đây2");
        mListener.changeColorJoinMaker(wasteJoinLat,wasteJoinLon);
    }


    public interface BottomSheetListener {
        void onDirectionFinderStart();
        void onDirectionFinderSuccess(List<Route> routes);
        void changeColorJoinMaker(double wasteJoinLat, double wasteJoinLon);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mListener = (BottomSheetListener) context;
        }catch (ClassCastException e){
            throw  new ClassCastException(context.toString() + "must implement BottomSheetListener");
        }
    }

    //class doc du lieu anh
    private class LoadImages extends AsyncTask<String, Void, Bitmap> {
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


       private  View view;
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.join_dialog_layout, container, false);
            return view;
        }

    }


}
