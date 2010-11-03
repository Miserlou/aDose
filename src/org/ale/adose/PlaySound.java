package org.ale.adose;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;

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

    private final byte generatedSnd[] = new byte[4 * numSamples];

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
                
                BrainwaveSequence be = new BrainwaveSequence("meditation.drugs", a);
                be.load();
                be.play();
                
                handler.post(new Runnable() {

                    public void run() {
//                        playSound();
                    }
                });
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
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, numSamples);
        audioTrack.setLoopPoints(0, generatedSnd.length/16, -1);
        audioTrack.play();
    }
}
