package org.ale.adose;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class PlaySound extends Activity {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    // then by Rich Jones <rich@anomos.info>
    private final int duration = 60; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double lSample[] = new double[numSamples];
    private final double rSample[] = new double[numSamples];
    private final double freqOfTone = 60; // hz
    private final double lFreqOfTone = 400; // hz
    private final double rFreqOfTone = 72; // hz
    public Panel panel;

    private final byte generatedSnd[] = new byte[4 * numSamples];
    public BrainwaveSequence bs;

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        panel = new Panel(this);
        panel.setKeepScreenOn(true);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        setContentView(panel);
        }

    @Override
    protected void onResume() {
        super.onResume();
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, 70, 0);

        
        final Activity a = this;
        
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                
//                bs = new BrainwaveSequence("meditation.drugs", a);
//                bs = new BrainwaveSequence("short.drugs", a);
                bs = new BrainwaveSequence("smooth.drugs", a);
                bs.load();
//                be.play();
                
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
                    handler.postDelayed(o, toDur);
                    toDur = toDur + be.duration;
                }
            }
        });
        thread.start();
    }

    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }
        for (int i = 0; i < numSamples; ++i) {
            lSample[i] = Math.sin(2 * Math.PI * i / (sampleRate/lFreqOfTone));
        }
        for (int i = 0; i < numSamples; ++i) {
            rSample[i] = Math.sin(-2 * Math.PI * i / (sampleRate/rFreqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for(int i=0; i<sample.length; i++) {
            final double dVal = lSample[i];
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            
            final double dVal2 = rSample[i];
            final short val2 = (short) ((dVal2 * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val2 & 0x00ff);
            generatedSnd[idx++] = (byte) ((val2 & 0xff00) >>> 8);
        }
//        
//        for (final double dVal : sample) {
//            // scale to maximum amplitude
//            final short val = (short) ((dVal * 32767));
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//
//        }
    }

    void playSound(){
//        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
//                AudioFormat.ENCODING_PCM_16BIT, numSamples,
//                AudioTrack.MODE_STATIC);
//        audioTrack.write(generatedSnd, 0, numSamples);
//        audioTrack.setLoopPoints(0, generatedSnd.length/16, -1);
//        audioTrack.play();
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
        
        Oscillator previous;
        
        public Oscillator(Oscillator pre) {
            if(pre != null) {
                previous = pre;
            }
        }

        public void run() {
            
            if(previous != null) {
                System.out.println("Removing.");
                previous.pause();
                handler.removeCallbacks(previous);
                previous = null;
            }
            
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
            count += (1000/hz);
            if(count<duration) {
                hasSpawned = true;
                handler.postDelayed(this, 1000/hz);
            }
            else {
                System.out.println("Removin!");
                handler.removeCallbacks(this);
                bs.pauseSample(lFreq+rFreq);
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
        
        public void pause() {
            bs.pauseSample(lFreq+rFreq);
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
