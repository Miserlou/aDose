package org.ale.adose;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

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
    int boffset = 0;
    
    private File tempFile;
    public FileOutputStream out;
    
    public AudioTrack audioTrack;
    public byte generatedSnd[] = new byte[4 * numSamples];
    
    public BrainwaveSequence(String f, Activity p) {
        filename = f;
        parent = p;
        tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/brain.pcm");
        System.out.println(tempFile.getPath());
        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
              }
            else{
                tempFile.delete();
                tempFile.createNewFile();
            }
            out = new FileOutputStream(tempFile, true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        

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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public void load() {
        readFile();
//        generatedSnd = new byte[4 * numSamples * (getTotalLength()/1000)];
        
        
        BrainwaveElement be;
        for(int i=0;i<sequence.size();i++) {
            be = sequence.get(i);
            genTone(be.leftFreq, be.rightFreq, be.duration/1000);
            System.out.println("Generated tone!: " + be.leftFreq + " " + be.rightFreq);
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/brain.pcm");
        
//        numSamples = getTotalLength()/1000 * duration * sampleRate;
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STREAM);
    }
    
    void genTone(double lFreqOfTone, double rFreqOfTone, int length){
        
        
        System.out.println("length is..");
        System.out.println(length);
        
        lSample = new double[numSamples];
        rSample = new double[numSamples];
        
        
        for (int i = 0; i < numSamples; ++i) {
            lSample[i] = Math.sin(2 * Math.PI * i / (sampleRate/lFreqOfTone));
        }
        for (int i = 0; i < numSamples; ++i) {
            rSample[i] = Math.sin(-2 * Math.PI * i / (sampleRate/rFreqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        
        generatedSnd = new byte[4 * numSamples];
        
        double dVal, dVal2;
        short val, val2;
        int idx = 0;
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
            
            
            try {
              for(int j=0;j<length;j++) {
                  out.write(generatedSnd);
              }
              System.out.println("Writing snd " + generatedSnd.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

    }

    public void play(){
//        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
//                AudioFormat.ENCODING_PCM_16BIT, numSamples,
//                AudioTrack.MODE_STATIC);
        
        
//        audioTrack.play();
//        System.out.println(getBytesFromFile().length);
//        int len = getBytesFromFile().length;
//        audioTrack.write(getBytesFromFile(), 0, len);
        
//        for(int i=0;i<(tempFile.length()/8000);i++) {
//            audioTrack.write(getSomeBytesFromFile(8000), 0, 8000);
//        }
        
//        while(audioTrack.write(getSomeBytesFromFile(numSamples), 0, numSamples)>1) {
//            System.out.println("Writing!");
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        
//        MediaPlayer mp = MediaPlayer.create(parent.getBaseContext(), tempFile.toURI());
        MediaPlayer mp = new MediaPlayer();
        try {
            FileInputStream ffis = new FileInputStream(tempFile);
            mp.setDataSource(ffis.getFD());
//            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            System.out.println(tempFile.getAbsolutePath());
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Playing?!");
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
    
    public byte[] getBytesFromFile() {
        InputStream fis;
        
        // Get the size of the file
        long length = tempFile.length();
        
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            System.out.println("too big");
        }
        
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
        
        try {
            fis = new FileInputStream(tempFile);
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=fis.read(bytes, offset, bytes.length-offset)) >= 0) {
                    offset += numRead;
            }
    
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
               System.out.println("Could not completely read file "+tempFile.getName());
            }
    
            // Close the input stream and return bytes
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return bytes;
        }
    
    public byte[] getSomeBytesFromFile(int amount) {
        InputStream fis;
       
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)amount];
        
        try {
            fis = new FileInputStream(tempFile);
            // Read in the bytes
            
            int numRead = 0;
            while (boffset < bytes.length && (numRead=fis.read(bytes, boffset, amount-boffset)) >= 0) {
                    boffset += numRead;
            }
    
            // Close the input stream and return bytes
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return bytes;
        }

        
}

