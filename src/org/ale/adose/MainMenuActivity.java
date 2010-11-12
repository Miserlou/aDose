package org.ale.adose;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
//              Intent km = new Intent(MainMenuActivity.this, TextMenuActivity.class);
//              startActivity(km);
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	
    	MenuItem mi = menu.add(0,0,0,"Shell");
    	mi.setIcon(android.R.drawable.ic_menu_manage);

    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case 0:
//    			startActivity(new Intent(this, ShellActivity.class));
    			return(true);
    	}
    	return(super.onOptionsItemSelected(item));
    }

}