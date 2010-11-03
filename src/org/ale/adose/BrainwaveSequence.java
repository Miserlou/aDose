package org.ale.adose;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class BrainwaveSequence {
    
    // Audio generation originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    // then by Rich Jones <rich@anomos.info>
    
    public String filename;
    public ArrayList<BrainwaveElement> sequence = new ArrayList<BrainwaveElement>();
    public Activity parent;
    
    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private int numSamples = duration * sampleRate;
    private double lSample[] = new double[numSamples];
    private double rSample[] = new double[numSamples];
    
    public AudioTrack audioTrack;
    private byte generatedSnd[];
    int idx = 0;
    
    public BrainwaveSequence(String f, Activity p) {
        filename = f;
        parent = p;
    }
    
    public void readFile() {
        try {
            DataInputStream in = new DataInputStream(parent.getAssets().open(filename));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String[] vals;
            BrainwaveElement be;
            
            double lFreq;
            double rFreq;
            String lOff;
            String lOn;
            String rOn;
            String rOff;
            int duration;
            
            while ((strLine = br.readLine()) != null)   {
                
              if(strLine.startsWith("#") || strLine.startsWith("\n") || strLine.startsWith(" ")) {
                  continue;
              }
              
              System.out.println(strLine);
              
              try {
                  vals = strLine.split(",");
                  lFreq = new Double(vals[0]).doubleValue();
                  lOff = vals[1];
                  lOn = vals[2];
                  rFreq = new Double(vals[3]).doubleValue();
                  rOff = vals[4];
                  rOn = vals[5];
                  duration = new Integer(vals[6]).intValue();
                  be = new BrainwaveElement(lFreq, lOff, lOn, rFreq, rOff, rOn, duration);
                  System.out.println(be);
                  sequence.add(be);
              }
              catch(Exception e) {
                  System.out.println(e);
                  System.out.println("Oh bugger.");
                  continue;
              }
            }
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public void load() {
        readFile();
        System.out.println("Read file!");
        generatedSnd = new byte[4 * numSamples * sequence.size()];
        BrainwaveElement be;
        System.out.println(sequence.size());
        for(int i=0;i<sequence.size();i++) {
            be = sequence.get(i);
            genTone(be.leftFreq, be.rightFreq);
            System.out.println("Generated tone!");
        }
        numSamples = sequence.size() * duration * sampleRate;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        for(int i=0;i<sequence.size();i++) {
            be = sequence.get(i);
        }
    }
    
    public int getTotalLength() {
        if(sequence == null) {
            return 0;
        }
        int t = 0;
        BrainwaveElement be;
        for(int i=0; i<sequence.size();i++) {
            be = sequence.get(i);
            t = t + be.getDuration();
        }
        
        return t;
    }
    
    void genTone(double lFreqOfTone, double rFreqOfTone){
        for (int i = 0; i < numSamples; ++i) {
            lSample[i] = Math.sin(2 * Math.PI * i / (sampleRate/lFreqOfTone));
        }
        for (int i = 0; i < numSamples; ++i) {
            rSample[i] = Math.sin(-2 * Math.PI * i / (sampleRate/rFreqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        
        double dVal, dVal2;
        short val, val2;
        for(int i=0; i<rSample.length; i++) {
            dVal = lSample[i];
            val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            
            dVal2 = rSample[i];
            val2 = (short) ((dVal2 * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val2 & 0x00ff);
            generatedSnd[idx++] = (byte) ((val2 & 0xff00) >>> 8);
        }    
    }

    public void play(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, numSamples);
        audioTrack.play();
        System.out.println("Playing?!");
    }

}
