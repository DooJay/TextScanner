package geniussoft.subodh.sudeep.textscanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import codeville.rashmi.sudeep.paperscan.R;

/**
 * Created by GUJAR on 10/10/2015.
 */
public class Splash extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);  //before thread here was s
        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(Splash.this, OCRActivity.class));
                finish();
            }
        }, secondsDelayed * 3000);
    }
}
