package org.ale.adose;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class PlaySound extends Activity {

    public Panel panel;
    public BrainwaveSequence bs;
    public String seqPath;
    
    Button loaded_button;
    Button resume_button;
    ProgressBar pBar;
    Boolean dShowing = true;

    Handler handler; 
    boolean loaded = false;
    Thread thread;
    Thread thread2;
    
    Dialog dialoog;
    Dialog pauseDialog;
    AsyncTask aTask1;
    
    ArrayList<Oscillator> osc = new ArrayList<Oscillator>();
    ArrayList<Integer> times = new ArrayList<Integer>();
    Oscillator current;
    boolean paused = false;
    boolean running = false;
    boolean nuked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        panel = new Panel(this);
        panel.setKeepScreenOn(true);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        setContentView(panel);
        seqPath = getIntent().getStringExtra("sequence");
        
        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 70, 0);
        
        final Activity a = this;
        
        if(aTask1 == null) {
            aTask1 = new AsyncTask() {
    
                @Override
                protected Object doInBackground(Object... arg0) {
                    if(seqPath == null) {
                        finish();
                        return null;
                    }
                    bs = new BrainwaveSequence(seqPath, a);
                    bs.load();
                    loaded = true;
                    return null;
                }
                @Override
                protected void onPostExecute(Object arg0) {
                    makeLoadingButton();
                }
            
            };
        
            aTask1.execute(null);
            makeLoadingDialog();
        }
    }
    
    public void onPause() {
        super.onPause();
    }

    public void makeLoadingButton() {
        if(loaded_button != null) {
            loaded_button.setVisibility(View.VISIBLE);
            pBar.setVisibility(View.GONE);
        }
    }
    
    public void onDestroy() {
        super.onDestroy();
//        stopSequence();
        System.out.println("On destroying!");
    }
    
    public void onStop() {
        super.onStop();
        stopSequence();
        panel = null;
        bs = null;
        seqPath = null;
        
        loaded_button = null;
        resume_button = null;
        pBar = null;
        dShowing = true;

        handler = null; 
        loaded = false;
        thread = null;
        thread2 = null;
        
        dialoog = null;
        pauseDialog = null;
        aTask1 = null;
        
        ArrayList<Oscillator> osc = new ArrayList<Oscillator>();
        ArrayList<Integer> times = new ArrayList<Integer>();
        current = null;
        paused = false;
        running = false;
        nuked = true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(dShowing) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH) {
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK){
            if(!paused) 
            {
                stopSequence();
                paused = true;
                makePausedDialog();
            }
            else {
                finish();
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_HOME){
            if(!paused) 
            {
                stopSequence();
                paused = true;
            }
            else {
                finish();
            }
        }
        return false;
    }

    private void stopSequence() {
        if(!nuked) {
            if(current != null) {
                System.out.println("pausing..");
                current.pauseSample();
                current.pauseFlashing();
            }
            if(osc != null) {
                Iterator i = osc.iterator();
                while(i.hasNext()) {
                    System.out.println("Removing callback");
                    if(handler != null) {
                        handler.removeCallbacks((Runnable) i.next());
                    }
                    else{
                        return;
                    }
                }
            }
        }
    }
    
    public void makeLoadingDialog() {
        
        dialoog = new Dialog(this, R.style.CustomDialogTheme);
        dialoog.setContentView(R.layout.dialog);
        
        final LayoutInflater factory = getLayoutInflater();
        final View cView = factory.inflate(R.layout.dialog, null);
        loaded_button = (Button) cView.findViewById(R.id.loaded);
        
        System.out.println("Making loading dialog");
        loaded_button.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                  thread2 = new Thread(new Runnable() {
                  public void run() {
                      System.out.println("thread2runnig");
                      BrainwaveElement be;
                      Oscillator o = null;
                      int toDur = 0;
                      
                      for(int i=0; i < bs.sequence.size(); i++) {
                          be = bs.sequence.get(i);
                          System.out.println("Making Oscillator");
                          o = new Oscillator(o);
                          o.setHz((long) (Math.abs(be.leftFreq - be.rightFreq)));
                          o.setDuration(be.duration);
                          o.setColors(be.leftOffColor, be.leftOnColor, be.rightOffColor, be.rightOnColor);
                          o.setFreqs(new Double(be.leftFreq).toString(), new Double(be.rightFreq).toString());
                          if(handler == null) {
                              return;
                          }
                          if(i==0) {
                              current = o;
                          }
                          osc.add(o);
                          times.add(toDur);
                          handler.postDelayed(o, toDur);
                          toDur = toDur + be.duration;
                      }
                      o = new Oscillator(o);
                      o.setHz((long) (1));
                      o.setDuration(5);
                      o.setColors("#ffffff", "#ffffff", "#ffffff", "#ffffff");
                      o.setFreqs(new Double(1).toString(), new Double(1).toString());
                      o.finisher = true;
                      osc.add(o);
                      times.add(toDur);
                      handler.postDelayed(o, toDur);
                  }
              });
              if(loaded & !running) {
                  new Exception().printStackTrace();
                  dialoog.hide();
                  dShowing = false;
                  System.out.println("Starting t2!!!");
                  thread2.start();
                  running = true;
              }
                return false;
            }});
        dialoog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                finish();
            }
            
        });
        
        pBar = (ProgressBar)cView.findViewById(R.id.progressbar);
        
        dialoog.setContentView(cView);
        dialoog.show();
    }
    
public void makePausedDialog() {
        
        pauseDialog = new Dialog(this, R.style.CustomDialogTheme);
        pauseDialog.setContentView(R.layout.paused);
        
        final LayoutInflater factory = getLayoutInflater();
        final View cView = factory.inflate(R.layout.paused, null);
        resume_button = (Button) cView.findViewById(R.id.resume);
        
        resume_button.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int diff = times.size() - osc.size();
                try {
                    int timeDiff = times.get(diff+1);
                    System.out.println("Time diff..");
                    System.out.println(timeDiff);
                    for(int i=1; i< osc.size(); i++) {
                        handler.postDelayed(osc.get(i), (times.get(i+diff)-timeDiff));
                    }
                    paused = false;
                    pauseDialog.hide();
                    
                    return paused;
                }
                catch(Exception e) {
                    paused = false;
                    pauseDialog.hide();
                    return false;
                }
                }
        });
        pauseDialog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                finish();
            }
            
        });
        
        pauseDialog.setContentView(cView);
        pauseDialog.show();
    }


    public class Oscillator implements Runnable {
        
        long hz;
        int count=0;
        int times;
        int duration;
        
        private String lPaintOn;
        private String lPaintOff;
        private String rPaintOn;
        private String rPaintOff;
        
        private String lFreq;
        private String rFreq;
        
        boolean hasSpawned = false;
        boolean paused = false;
        
        Oscillator previous;
        
        public boolean finisher = false;
        
        public Oscillator(Oscillator pre) {
            if(pre != null) {
                previous = pre;
            }
        }

        public void run() {
            
            if(previous != null) {
                previous.pauseSample();
                handler.removeCallbacks(previous);
                previous = null;
                current = this;
                if(osc.contains(this)) {
                    osc.remove(this);
                }
            }
            
            if(finisher) {
                if(thread2 != null) {
                    thread2.stop();
                }
                thread2=null;
                bs.stop();
                handler.removeCallbacks(this);
                dialoog.dismiss();
                handler = null;
                endSequence();
                System.out.println("Finishing..?");
                return;
            }
            
            if(!paused) {
                if(!hasSpawned) {
                    panel.lPaintOn.setColor(Color.parseColor(lPaintOn));
                    panel.lPaintOff.setColor(Color.parseColor(lPaintOff));
                    panel.rPaintOn.setColor(Color.parseColor(rPaintOn));
                    panel.rPaintOff.setColor(Color.parseColor(rPaintOff)); 
                    
                    if(bs != null) {
                        bs.playSample(lFreq+rFreq, duration);
                    }
                }
                
                panel.flipStatus();
                panel.invalidate();
                count += (990/hz);
                if(count<duration) {
                    hasSpawned = true;
                    handler.postDelayed(this, 990/hz);
                }
                else {
                    handler.removeCallbacks(this);
                }
            }
        }
        
        public void setHz(long d) {
            hz = d;
        }
        
        public void setTimes(int t) {
            times = t;
        }
        
        public void setDuration(int t) {
            duration = t;
        }
        
        public void setFreqs(String l, String r) {
            lFreq = l;
            rFreq = r;
        }
        
        public void setColors(String lColorOn, String lColorOff, String rColorOff, String rColorOn) {
            lPaintOn = lColorOn;
            rPaintOn = rColorOn;
            lPaintOff = lColorOff;
            rPaintOff = rColorOff;

        }
        
        public void pauseSample() {
            System.out.println("Pausing sample..");
            bs.pauseSample();
        }
        
        public void pauseFlashing() {
            paused = true;
        }
        
        public void endSequence() {
            System.out.println("Sequence finished!");
            finish();
        }
    }
    
    class Panel extends View {

        public Panel(Context context) {
            super(context);
            
            lPaintOn = new Paint();
            rPaintOn = new Paint();
            lPaintOff = new Paint();
            rPaintOff = new Paint();
        }
        
        private Canvas canvas;
        private Bitmap bitmap;
        private Paint lPaintOn;
        private Paint lPaintOff;
        private Paint rPaintOn;
        private Paint rPaintOff;
        
        public boolean on = true;
        public boolean supad = false;

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            canvas= new Canvas();
//            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//            canvas.setBitmap(bitmap);
        }
        public void destroy() {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        public void onDraw(Canvas c) {
            if(!supad) {
                c.drawColor(Color.BLUE);
                supad=true;
                super.onDraw(c);
                return;
            }

            c.drawRect(0, 0, getWidth()/2, getHeight(), lPaintOn);
            c.drawRect(getWidth()/2, 0, getWidth(), getHeight(), rPaintOff);

        }
        
        public void setColors(String lColorOn, String lColorOff, String rColorOn, String rColorOff) {

            lPaintOn.setColor(Color.parseColor(lColorOn));
            lPaintOff.setColor(Color.parseColor(lColorOff));
            rPaintOn.setColor(Color.parseColor(rColorOff));
            rPaintOff.setColor(Color.parseColor(rColorOn));
             
        }
        
        public void flipStatus() {
            Paint temp;
            temp = lPaintOn;
            lPaintOn = lPaintOff;
            lPaintOff = temp;
            
            temp = rPaintOn;
            rPaintOn = rPaintOff;
            rPaintOff = temp;
            if(on) {
                on = false;
            }
            else {
                on = true;
            }
        }
    }
}
