package org.ale.adose; 

import org.ale.adose.R;

import android.app.Activity; 
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent; 
import android.net.Uri;
import android.os.Bundle; 
import android.os.Handler; 
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity { 
      
     private final int SPLASH_DISPLAY_LENGHT = 3000; 
     private Handler mHandler; 
     
     /** Called when the activity is first created. */ 
     @Override 
     public void onCreate(Bundle icicle) { 
          super.onCreate(icicle); 
          
          //no title bar
          requestWindowFeature(Window.FEATURE_NO_TITLE);
          getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
          
          setContentView(R.layout.splash); 
           
          mHandler = new Handler();
     } 
     
     public void onStart() {
         super.onStart();
         mHandler.postDelayed(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                Intent mainIntent = new Intent(SplashActivity.this,PlaySound.class); 
                SplashActivity.this.startActivity(mainIntent); 
                SplashActivity.this.finish();
                
            }}, 3000);
 
     }
     
}