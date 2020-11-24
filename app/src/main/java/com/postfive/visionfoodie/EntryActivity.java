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

package com.postfive.visionfoodie;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;
import com.postfive.visionfoodie.java.StillImageActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/** Activity demonstrating different image detector features with a still image from camera. */
@KeepName
public final class EntryActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

  private static final String TAG = "EntryActivity";
  private static final int PERMISSION_REQUESTS = 1;
  private List<String> allNeededPermissions = null;
  private final NonLeakHandler mHandler = new NonLeakHandler(this);

  private final int WAIT_TIME = 200;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_vision_entry_choice);

    if (!allPermissionsGranted()) {
      getRuntimePermissions();

    }else{
      run();
    }
  }

  private void run(){
    mHandler.sendEmptyMessageDelayed(WAIT_TIME, 2000);
  }

  // 핸들러 객체 만들기
  private static class NonLeakHandler extends Handler {
    private final WeakReference<EntryActivity> mActivity;
    public NonLeakHandler(EntryActivity activity) {
      mActivity = new WeakReference<EntryActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
      Log.d(TAG,"aaaa");

      EntryActivity activity = mActivity.get();
      if (activity != null) {
        Log.d(TAG,"handlemessage");
        activity.handleMessage(msg);
      }
    }
  }

  private void handleMessage(Message msg){
    switch (msg.what){
      case WAIT_TIME:
        Log.d(TAG,"end");
        Intent intent = new Intent(this, StillImageActivity.class);
        startActivity(intent);
        finish();
        break;
    }
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
              this.getPackageManager()
                      .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
        Log.i(TAG, "aaaa");

      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
              this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
      Log.i(TAG, "kkkkk");

    }
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
    Log.d(TAG, "오호");
    Log.d(TAG, Integer.toString(requestCode));
    for(String tmp : permissions){
      Log.d(TAG, tmp);
    }
    for(int tmp : grandResults){
      Log.d(TAG, Integer.toString(tmp));
    }

    if ( requestCode == PERMISSION_REQUESTS && grandResults.length == allNeededPermissions.size()) {
      // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

      boolean check_result = true;
      // 모든 퍼미션을 허용했는지 체크합니다.
      for (int result : grandResults) {
        if (result != PackageManager.PERMISSION_GRANTED) {
          check_result = false;
          break;
        }
      }
      if(check_result) {
        run();
      }
      else {
        Toast.makeText(getApplicationContext(), "사용하실 수 없습니다.", Toast.LENGTH_LONG).show();
        finish();
      }
    }else{
      Toast.makeText(getApplicationContext(), "사용하실 수 없습니다.", Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
