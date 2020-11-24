package com.postfive.visionfoodie.java;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.postfive.visionfoodie.FoodVO;
import com.postfive.visionfoodie.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    final String TAG = "ListActivity";

    ArrayList<FoodVO> foodList = new ArrayList<>();
    MyArrayAdapter adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        TextView searchResult = (TextView)findViewById(R.id.search_result);

        foodList = (ArrayList<FoodVO>) getIntent().getSerializableExtra("keyword");

        searchResult.setText("'"+foodList.get(0).getName()+"'으(로) 검색된 데이터 "+ Integer.toString(foodList.size())+"건");

        adapter = new MyArrayAdapter(this, R.layout.list_item, foodList);

        ListView listView = findViewById(R.id.test_activity_list_view);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG,"웹뷰로" + foodList.get(position).getUrl());

        /*Class<?> clicked = CLASSES[position];
        startActivity(new Intent(this, clicked));*/

        String url = foodList.get(position).getUrl();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Here we use an intent without a Chooser unlike the next example
            startActivity(intent);
        }

    }




    private ArrayList<FoodVO> getFoodList(String keyword) {
        ArrayList<FoodVO> listFood = new ArrayList<>();
        Gson gson = new Gson();

        try {
            InputStream is = getAssets().open("contents.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("contents");

            int index = 0;
            while (index < jsonArray.length()) {
                FoodVO foodVO = gson.fromJson(jsonArray.get(index).toString(), FoodVO.class);
                Log.d(TAG, foodVO.getTitle());
                index++;
                if(!foodVO.getKeyword().equals(keyword)){
                    continue;
                }

                listFood.add(foodVO);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listFood;
    }


    private static class MyArrayAdapter extends ArrayAdapter<FoodVO> {

        private final Context context;
        private final List<FoodVO> classes;
        private String[] descriptionIds;

        MyArrayAdapter(Context context, int resource, List<FoodVO> objects) {
            super(context, resource, objects);

            this.context = context;
            classes = objects;
        }

        @SuppressLint("ResourceType")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, null);
            }
            TextView text1 =  (TextView) view.findViewById(R.id.item_title);
            TextView text2 =  (TextView) view.findViewById(R.id.item_url);
            TextView text3 =  (TextView) view.findViewById(R.id.item_date);

            text1.setText(classes.get(position).getTitle());
            text2.setText(classes.get(position).getUrl());
            text3.setText(classes.get(position).getBrod_date());
            text1.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            text2.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            text3.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));

            // 짝수 색
            if(position%2 == 0) {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
                text2.setTextColor(getContext().getResources().getColor(R.color.gray));

                // 짝수 색
            }else {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey_400));
                text2.setTextColor(getContext().getResources().getColor(R.color.light_grey_400));

            }

            return view;
        }/*
    private static class MyArrayAdapter extends ArrayAdapter<VisionFoodieVO> {

        private final Context context;
        private final List<VisionFoodieVO> classes;
        private String[] descriptionIds;

        MyArrayAdapter(Context context, int resource, List<VisionFoodieVO> objects) {
            super(context, resource, objects);

            this.context = context;
            classes = objects;
        }

        @SuppressLint("ResourceType")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, null);
            }
            TextView text1 =  (TextView) view.findViewById(R.id.item_title);
            TextView text2 =  (TextView) view.findViewById(R.id.item_url);
            TextView text3 =  (TextView) view.findViewById(R.id.item_date);

            text1.setText(classes.get(position).getTitle());
            text2.setText(classes.get(position).getUrl());
            text3.setText(classes.get(position).getBrod_date());
            text1.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            text2.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            text3.setTextColor(getContext().getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));

            // 짝수 색
            if(position%2 == 0) {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
                // 짝수 색
            }else {
                view.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey_400));
            }

            return view;
        }*/
/*
        void setDescriptionIds(String[] descriptionIds) {
            this.descriptionIds = descriptionIds;
        }*/
    }
}
