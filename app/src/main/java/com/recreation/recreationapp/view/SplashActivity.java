package com.recreation.recreationapp.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.recreation.recreationapp.R;
import com.recreation.recreationapp.ReCreationApplication;

/**
 * Created by milanmanvar on 16/03/16.
 */
public class SplashActivity extends Activity {

    private ReCreationApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        application = (ReCreationApplication) this.getApplication();

//        new AsyncSplashLoader().execute();
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.milan.recreationapp",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        showAnimation();

    }

    private void showAnimation() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                if (application.sharedPreferences.getString("club", "").equalsIgnoreCase("")) {
                    startActivity(new Intent(SplashActivity.this, WelcomeScreen.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, HomeScreen.class));
                }
                finish();

            }
        }, 2000);
    }


    /*private class AsyncSplashLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            finish();
            if (((ReCreationApplication) getApplication()).sharedPreferences.getString("club", "").equalsIgnoreCase("")) {
                startActivity(new Intent(SplashActivity.this, WelcomeScreen.class));
            } else {
                startActivity(new Intent(SplashActivity.this, HomeScreen.class));

            }
        }
    }*/

}
