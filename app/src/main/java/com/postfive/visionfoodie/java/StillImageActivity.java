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

package com.postfive.visionfoodie.java;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.postfive.visionfoodie.BitmapUtils;
import com.postfive.visionfoodie.GraphicOverlay;
import com.postfive.visionfoodie.R;
import com.postfive.visionfoodie.VisionImageProcessor;
import com.postfive.visionfoodie.java.labeldetector.LabelDetectorProcessor;

import java.io.IOException;

/** Activity demonstrating different image detector features with a still image from camera. */
@KeepName
public final class StillImageActivity extends AppCompatActivity {

  private static final String TAG = "StillImageActivity";

  private static final String OBJECT_DETECTION = "Object Detection";
  private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection (Birds)";
  private static final String FACE_DETECTION = "Face Detection";
  private static final String BARCODE_SCANNING = "Barcode Scanning";
  private static final String TEXT_RECOGNITION = "Text Recognition";
  private static final String IMAGE_LABELING = "Image Labeling";
  private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
  private static final String AUTOML_LABELING = "AutoML Labeling";
  private static final String POSE_DETECTION = "Pose Detection";

  private static final String SIZE_SCREEN = "w:screen"; // Match screen width
  private static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
  private static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio

  private static final String KEY_IMAGE_URI = "com.postfive.visionfoodie.KEY_IMAGE_URI";
  private static final String KEY_SELECTED_SIZE = "com.postfive.visionfoodie.KEY_SELECTED_SIZE";

  private static final int REQUEST_IMAGE_CAPTURE = 1001;
  private static final int REQUEST_CHOOSE_IMAGE = 1002;
  public static final int SHOW_LIST = 1003;

  private static final int WHAT = 200;
  private static String keyword;

  public int cnt = 0;
  private ImageView preview;
  private GraphicOverlay graphicOverlay;
  private String selectedMode = AUTOML_LABELING;//;OBJECT_DETECTION;
  private String selectedSize = SIZE_SCREEN;

  boolean isLandScape;

  private Uri imageUri;
  private int imageMaxWidth;
  private int imageMaxHeight;
  private VisionImageProcessor imageProcessor;

  static private LinearLayout failMsg;
  static private TextView successMsg;
  static boolean isLoding = false;

  private ImageView logoImg;

  static private LinearLayout successLayout;

  private long backKeyPressedTime = 0;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_still_image);

    failMsg = (LinearLayout)findViewById(R.id.fail_msg);
    failMsg.setVisibility(View.INVISIBLE);

    successMsg = (TextView)findViewById(R.id.success_msg);
//    successMsg.setVisibility(View.INVISIBLE);
    successLayout =(LinearLayout)findViewById(R.id.success_layout);
    successLayout.setVisibility(View.INVISIBLE);

    logoImg = (ImageView)findViewById(R.id.logo_Img);
    logoImg.setVisibility(View.VISIBLE);

    findViewById(R.id.take_picture_button).setOnClickListener(
                    view -> {
                      if(isIsLoding()){
                        return;
                      }
                      startCameraIntentForResult();

                    });
    findViewById(R.id.select_image_button).setOnClickListener(
                    view -> {
                      if(isIsLoding()){
                        return;
                      }
                      startChooseImageIntentForResult();

                    });
    preview = findViewById(R.id.preview);
    graphicOverlay = findViewById(R.id.graphic_overlay);

//    populateFeatureSelector();
//    populateSizeSelector();

    isLandScape =
        (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

    if (savedInstanceState != null) {
      imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
      selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
    }

    View rootView = findViewById(R.id.root);
    rootView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageMaxWidth = rootView.getWidth();
                imageMaxHeight = rootView.getHeight() - findViewById(R.id.control).getHeight();
                if (SIZE_SCREEN.equals(selectedSize)) {
                  tryReloadAndDetectInImage();
                }
              }
            });

  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
//    createImageProcessor();
//    tryReloadAndDetectInImage();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(KEY_IMAGE_URI, imageUri);
    outState.putString(KEY_SELECTED_SIZE, selectedSize);
  }

  private void startCameraIntentForResult() {
    // Clean up last time's image
    imageUri = null;
    preview.setImageBitmap(null);

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      ContentValues values = new ContentValues();
      values.put(MediaStore.Images.Media.TITLE, "New Picture");
      values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
      imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
  }

  private void startChooseImageIntentForResult() {

    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    initMsg();

    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Log.d(TAG, "REQUEST_IMAGE_CAPTURE");

      createImageProcessor();
      tryReloadAndDetectInImage();

    } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
      // In this case, imageUri is returned by the chooser, save it.
      Log.d(TAG, "REQUEST_CHOOSE_IMAGE");
      imageUri = data.getData();

      createImageProcessor();
      tryReloadAndDetectInImage();

    } else {

      imageUri = null;

      tryReloadAndDetectInImage();
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  private void tryReloadAndDetectInImage() {
    Log.d(TAG, "Try reload and detect image");
    try {
      if (imageUri == null) {
        logoImg.setVisibility(View.VISIBLE);
        return;
      }
      logoImg.setVisibility(View.INVISIBLE);

      if (SIZE_SCREEN.equals(selectedSize) && imageMaxWidth == 0) {
        // UI layout has not finished yet, will reload once it's ready.
        return;
      }

      Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
      if (imageBitmap == null) {
        Log.d(TAG, "imageBitmap");
        return;
      }

      // Clear the overlay first
      graphicOverlay.clear();

      // Get the dimensions of the image view
      Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

      // Determine how much to scale down the image
      float scaleFactor =
          Math.max(
              (float) imageBitmap.getWidth() / (float) targetedSize.first,
              (float) imageBitmap.getHeight() / (float) targetedSize.second);

      Bitmap resizedBitmap =
          Bitmap.createScaledBitmap(
              imageBitmap,
              (int) (imageBitmap.getWidth() / scaleFactor),
              (int) (imageBitmap.getHeight() / scaleFactor),
              true);

      preview.setImageBitmap(resizedBitmap);

      if (imageProcessor != null) {
        Log.e(TAG, "LabelDetectorProcessor setImageSourceInfo 전");
        graphicOverlay.setImageSourceInfo(
            resizedBitmap.getWidth(), resizedBitmap.getHeight(), /* isFlipped= */ false);
        Log.e(TAG, "LabelDetectorProcessor processBitmap 전");
        imageProcessor.processBitmap(resizedBitmap, graphicOverlay);

      } else {
        Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error");
      }
    } catch (IOException e) {
      Log.e(TAG, "Error retrieving saved image");
      imageUri = null;
    }
  }

  private Pair<Integer, Integer> getTargetedWidthHeight() {
    int targetWidth;
    int targetHeight;

    switch (selectedSize) {
      case SIZE_SCREEN:
        targetWidth = imageMaxWidth;
        targetHeight = imageMaxHeight;
        break;
      case SIZE_640_480:
        targetWidth = isLandScape ? 640 : 480;
        targetHeight = isLandScape ? 480 : 640;
        break;
      case SIZE_1024_768:
        targetWidth = isLandScape ? 1024 : 768;
        targetHeight = isLandScape ? 768 : 1024;
        break;
      default:
        throw new IllegalStateException("Unknown size");
    }

    return new Pair<>(targetWidth, targetHeight);
  }

  private void createImageProcessor() {
    try {

          Log.i(TAG, "Using AutoML Image Label Detector Processor");
          AutoMLImageLabelerLocalModel autoMLLocalModel =
              new AutoMLImageLabelerLocalModel.Builder()
                      .setAssetFilePath("automl/manifest.json")
                  .build();
          AutoMLImageLabelerOptions autoMLOptions =
              new AutoMLImageLabelerOptions.Builder(autoMLLocalModel)
                  .setConfidenceThreshold(0)
                  .build();
          imageProcessor = new LabelDetectorProcessor(this, autoMLOptions);

    } catch (Exception e) {
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  public static void showFailMsg(){
    failMsg.setVisibility(View.VISIBLE);
    Log.d(TAG, "FailMsg");


    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        failMsg.setVisibility(View.INVISIBLE);
      }},2500);

  }

  public static void showSucessMsg(ImageLabel label){
    isLoding = true;

    keyword = label.getText();
    successLayout.setVisibility(View.VISIBLE);
    successMsg.setText("인식결과 : '"+label.getText()+"' ("+ Float.toString(label.getConfidence() * 100)+"%)");

  }

  void initMsg(){
    isLoding = false;
    failMsg.setVisibility(View.INVISIBLE);
    successLayout.setVisibility(View.INVISIBLE);

  }

  boolean isIsLoding(){
    if(isLoding){
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      vibrator.vibrate(500); // 0.5초간 진동

    }
    return isLoding;
  }

  @Override
  public void onBackPressed() {
    if(isIsLoding()){
      return;
    }
    if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
      failMsg.setVisibility(View.INVISIBLE);
      backKeyPressedTime = System.currentTimeMillis();
      preview.setImageBitmap(null);
      logoImg.setVisibility(View.VISIBLE);
      return;
    }

    if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
      finish();

    }


  }

}
