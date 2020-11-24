/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.postfive.visionfoodie.java.labeldetector;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabelerOptionsBase;
import com.google.mlkit.vision.label.ImageLabeling;
import com.postfive.visionfoodie.FoodVO;
import com.postfive.visionfoodie.GraphicOverlay;
import com.postfive.visionfoodie.java.ListActivity;
import com.postfive.visionfoodie.java.StillImageActivity;
import com.postfive.visionfoodie.java.VisionProcessorBase;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

/** Custom InputImage Classifier Demo. */
public class LabelDetectorProcessor extends VisionProcessorBase<List<ImageLabel>> {

  private static final String TAG = "LabelDetectorProcessor";

  private final ImageLabeler imageLabeler;
  static Context context ;
  public LabelDetectorProcessor(Context context, ImageLabelerOptionsBase options) {
    super(context);
    this.context = context;
    imageLabeler = ImageLabeling.getClient(options);
    Log.d(TAG, "LabelDetectorProcessor 생성자");
  }

  @Override
  public void stop() {
    super.stop();
    Log.d(TAG, "stop");
    try {
      imageLabeler.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close ImageLabelerClient: " + e);
    }
  }

  @Override
  protected Task<List<ImageLabel>> detectInImage(InputImage image) {
    Log.d(TAG, "detectInImage");

    return imageLabeler.process(image);
  }

  @Override
  protected void onSuccess(
          @NonNull List<ImageLabel> labels, @NonNull GraphicOverlay graphicOverlay) {

    //graphicOverlay.add(new LabelGraphic(graphicOverlay, labels));
    Log.d(TAG, "onSuccess size "+labels.size());
    logExtrasForTesting(labels);
  }

  private static void logExtrasForTesting(List<ImageLabel> labels) {
    if (labels == null) {
      Log.v(MANUAL_TESTING_LOG, "No labels detected");
    } else {
      for (ImageLabel label : labels) {
        Log.d(TAG, "lable size "+label.getText() + " " + label.getConfidence());

        Log.v(
            MANUAL_TESTING_LOG,
            String.format("Label %s, confidence %f", label.getText(), label.getConfidence()));
      }

      if(labels.get(0).getConfidence() < 0.7){
        StillImageActivity.showFailMsg();

      }else{

        StillImageActivity.showSucessMsg(labels.get(0));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {


            Realm.init(context);

        App app = new App(new AppConfiguration.Builder("application-0-laohu")
                .appName("My App")
                .requestTimeout(30, TimeUnit.SECONDS)
                .build());
        Credentials credentials = Credentials.emailPassword("visionfoodieApp", "1qaz2wsx");//Credentials.anonymous();

        app.loginAsync(credentials, it -> {
          if (it.isSuccess()) {
            User user = app.currentUser();
            assert user != null;
            MongoClient mongoClient = user.getMongoClient("Vision-Foodie");
            if (mongoClient != null) {
              Log.v("loginAsync", "Successfully connected to the MongoDB instance.");
            } else {
              Log.e("loginAsync", "Error connecting to the MongoDB instance.");
            }

            MongoDatabase mongoDatabase = mongoClient.getDatabase("visionFoodie");
            MongoCollection<Document> mongoCollection  = mongoDatabase.getCollection("contents");

            Document queryFilter  = new Document("keyword", labels.get(0).getText());
            RealmResultTask<MongoCursor<Document>> findTask = mongoCollection.find(queryFilter).iterator();

            findTask.getAsync(task -> {
              if (task.isSuccess()) {
                MongoCursor<Document> results = task.get();
                Log.v("EXAMPLE", "successfully found all plants for Store 42:");

                ArrayList<FoodVO> foodList = new ArrayList<>();
                while (results.hasNext()) {
                  String tmp = results.next().toString();
                  foodList.add(setFoodVO(tmp));
                  Log.v("EXAMPLE", tmp);
                }

                for(FoodVO a : foodList){

                  Log.d(TAG,a.getKeyword());
                  Log.d(TAG,a.getName());
                  Log.d(TAG,a.getTitle());
                  Log.d(TAG,a.getUrl());
                  Log.d(TAG,a.getBrod_date());
                }

                Intent intent = new Intent(context, ListActivity.class);

                intent.putExtra("keyword", foodList);

                ((StillImageActivity) context).startActivityForResult(intent, StillImageActivity.RECEIVER_VISIBLE_TO_INSTANT_APPS);

              } else {
                Log.e("EXAMPLE", "failed to find documents with: ", task.getError());
              }
            });

          }else {
            Log.e("loginAsync", "Error logging into the Realm app. Make sure that anonymous authentication is enabled.");
          }
        });

            }
          }, 2000);

      }
    }
  }
  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.w(TAG, "Label detection failed." + e);
  }

  public static FoodVO setFoodVO(String strFoodInfo){

    strFoodInfo = strFoodInfo.substring(strFoodInfo.indexOf("{{"), strFoodInfo.lastIndexOf("}}"));

    String[] arrInfo = strFoodInfo.split(", ");

    FoodVO food = new FoodVO();

    for(String tmp : arrInfo){
      String[] tmpM = tmp.split("=");

      if(tmpM[0].equals("keyword")){
        food.setKeyword(tmpM[1]);
      }else if(tmpM[0].equals("name")){
        food.setName(tmpM[1]);
      }else if(tmpM[0].equals("title")){
        food.setTitle(tmpM[1]);
      }else if(tmpM[0].equals("brod_date")){
        food.setBrod_date(tmpM[1]);
      }else if(tmpM[0].equals("url")){
        food.setUrl(tmpM[1]);
      }

    }

    return food;
  }
}

