package org.ale.adose;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class MainMenuActivity extends Activity {
	
	SharedPreferences prefs;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //no title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.menu);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    }
    
    @Override
    public void onStart(){
    	super.onStart();
        
    	final Button b1 = (Button)findViewById(R.id.seq);
//    	Button b2 = (Button)findViewById(R.id.email);
    	
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("On click");
            	Intent km = new Intent(MainMenuActivity.this, SequenceListActivity.class);
            	startActivity(km);
            }
        });
        
        b1.setOnTouchListener(new View.OnTouchListener() {
            
            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    b1.setBackgroundResource(R.drawable.button_pressed);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    b1.setBackgroundResource(R.drawable.button);
                }
                
                return false;
            }
        });
        
        
        final Button b2 = (Button)findViewById(R.id.about);
        
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                makeAboutDialog();
            }
        });
        b2.setOnTouchListener(new View.OnTouchListener() {
            
            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    b2.setBackgroundResource(R.drawable.button_pressed);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    b2.setBackgroundResource(R.drawable.button);
                }
                
                return false;
            }
        });
    	
    }
    
public void onResume() {
    super.onResume();
    
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
  
    final SharedPreferences.Editor editor2;
    String first = prefs.getString("first_time", "fuck");
    if(first.contains("fuck")){
          new AlertDialog.Builder(this)
          .setMessage("\tWelcome to aDose! Thank your for purchasing and supporting independent developers and creative applications! :) \n\n Before you try aDose, you should really press \'About\' and read the tutorial about how to use it properly.\n\n\tIf you encounter any bugs, please contact us at miserlou@gmail.com so we can fix them for you.\n\nEnjoy! (Also, if you enjoy this application, please remember to give us a five star review! Thanks!)\n" )
          .setPositiveButton("Okay!", null)
          .show();
          editor2 = prefs.edit();
          editor2.putString("first_time", "shitballs");
          editor2.commit();
    }
}
    
public void makeAboutDialog() {
        
        final Dialog pauseDialog = new Dialog(this, R.style.CustomDialogTheme);
        pauseDialog.setContentView(R.layout.about);
        
        final LayoutInflater factory = getLayoutInflater();
        final View cView = factory.inflate(R.layout.about, null);
        Button resume_button = (Button) cView.findViewById(R.id.resume);
        
        resume_button.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                pauseDialog.hide();
                return false;
                }
        });
        
        pauseDialog.setContentView(cView);
        pauseDialog.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem mi = menu.add(0,0,0,"About");
    	mi.setIcon(android.R.drawable.ic_menu_help);

    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case 0:
    		    makeAboutDialog();
    			return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }

}