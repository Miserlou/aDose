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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.android.soundpool.example.MediaPlayerPool;
import com.android.soundpool.example.MediaPlayerPools;
import com.android.soundpool.example.MediaPlayerStream;

import android.R;
import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
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
    private final int sampleRate = 44100;
    private int numSamples = duration * sampleRate;
    private double lSample[];// = new double[numSamples];
    private double rSample[];// = new double[numSamples];
    int boffset = 0;
    
    private File tempFile;
    public FileOutputStream out;
    
    public MediaPlayerPools mediaPlayerPools;
    public HashMap<String, MediaPlayerPool> poolMap = new HashMap<String, MediaPlayerPool>();
    public byte generatedSnd[];// = new byte[4 * numSamples];
    
    public String name = "Name";
    public String description = "Description";
    
    public AssetManager assetManager;
    
    int last = -1;
    MediaPlayerStream lastStream;
    
    public BrainwaveSequence(String f, Activity p) {
        filename = f;
        parent = p;
        tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/brain.pcm");
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
            e.printStackTrace();
        }
        
        mediaPlayerPools = new MediaPlayerPools(p);

    }
    
    public void setNameDesc(String n, String d) {
        name = n;
        description = d;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
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
                  e.printStackTrace(); 
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
        assetManager = parent.getAssets();
        String[] oggs = new String[1];
        try {
            oggs = assetManager.list("oggs");
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
            
        }
        System.out.println("OGGS!");
        System.out.println(oggs);
        ArrayList ogal = new ArrayList();
        for(int ii=0; ii<oggs.length; ii++) {
            ogal.add(oggs[ii]);
            System.out.println(oggs[ii]);
        }
        
        BrainwaveElement be;
        for(int i=0;i<sequence.size();i++) {
            be = sequence.get(i);
            
            // Have we pre-generated this tone? If not, generate it, else just load it.
            
            //Loaded already?
            if(!poolMap.containsKey("" + be.leftFreq + be.rightFreq)) {
                //Pregenerated, load it
                if(ogal.contains("" + be.leftFreq + be.rightFreq + ".ogg")){
                    
                    poolMap.put("" + be.leftFreq + be.rightFreq, mediaPlayerPools.add("oggs/" + be.leftFreq + be.rightFreq + ".ogg", 1, true));
                }
                else {
                    System.out.println("Generating a new tone!!");
                    genTone(be.leftFreq, be.rightFreq, be.duration/10000);
                }
            }
            // We've already loaded this.
            else {
                continue;
            }
        }
  
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/brain.pcm");
        
    }
    
    void genTone(final double lFreqOfTone, final double rFreqOfTone, int length){
        
        
        System.out.println("length is..");
        System.out.println(length);
       
        lSample = new double[sampleRate*2];
        rSample = new double[sampleRate*2];
        
        int i=0;
        for (i = 0; i < lSample.length; ++i) {
            lSample[i] = Math.sin(2 * Math.PI * i / (sampleRate/lFreqOfTone));
            rSample[i] = Math.sin(-2 * Math.PI * i / (sampleRate/rFreqOfTone));
            if((Math.abs((lSample[i] - rSample[i])) < .0001) && (i != 0) && (Math.abs((lSample[i] - lSample[0])) < .00000001) && (Math.abs((rSample[i] - rSample[0])) < .00000001) && i>1000000) {
                System.out.println("Victory!");
                System.out.println(Math.abs((lSample[i] - rSample[i])));
                System.out.println(i);
                break;
            }
        }
        
        double[] lSampleT = new double[i];
        double[] rSampleT = new double[i];
        
        System.arraycopy(lSample, 0, lSampleT, 0, i);
        System.arraycopy(rSample, 0, rSampleT, 0, i);

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        
        generatedSnd = new byte[(4 * lSampleT.length)];
        
        double dVal, dVal2;
        short val, val2;
        int idx = 0;
            for(int j=0; j<rSampleT.length; j++) {
                dVal = lSampleT[j];
                val = (short) ((dVal * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
                
                dVal2 = rSampleT[j];
                val2 = (short) ((dVal2 * 32767));
                // in 16 bit wav PCM, first byte is the low order byte
                generatedSnd[idx++] = (byte) (val2 & 0x00ff);
                generatedSnd[idx++] = (byte) ((val2 & 0xff00) >>> 8);
            }
            
            
            try {
              tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + lFreqOfTone + rFreqOfTone +".pcm");
              
              if(poolMap.containsKey("" + lFreqOfTone + rFreqOfTone)) {
                  return;
              }
              else{
                  System.out.println(poolMap);
              }
              
              if (!tempFile.exists()) {
                  tempFile.createNewFile();
                }
              else{
                  tempFile.delete();
                  tempFile.createNewFile();
              }
              out = new FileOutputStream(tempFile, false);
              out.write(generatedSnd);
              out.flush();
              out.close();
              
              String s = Environment.getExternalStorageDirectory().getPath() + "/" + lFreqOfTone + rFreqOfTone +".pcm";
              String in = Environment.getExternalStorageDirectory().getPath() + "/" + lFreqOfTone + rFreqOfTone +".pcm";
              String out = Environment.getExternalStorageDirectory().getPath() + "/" + lFreqOfTone + rFreqOfTone +".ogg";
              System.out.println(s);
              if(!poolMap.containsKey("" + lFreqOfTone + rFreqOfTone)) {
                  VorbisEncoder.encode(in, out);
                  System.out.println("Loading sample..");
                    poolMap.put("" + lFreqOfTone + rFreqOfTone, mediaPlayerPools.add(out, 1, false));
                  
                  while(!poolMap.containsKey("" + lFreqOfTone + rFreqOfTone)) {
                      continue;}
                  
              }
              
              System.out.println("Writing snd " + generatedSnd.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    
//    public void playSample(String s, int duration) {
//        int t = soundMap.get(s);
//        System.out.println("Attempting to play sample");
//        if(last != -1) {
//            soundPool.pause(last);
//        }
//        try{
//            last = soundPool.play(t, 0.99f, 0.99f, 1, -1, 1);
//        }
//        catch(Exception e) {
//            System.out.println("Fuck");
//            last = soundPool.play(t, 0.99f, 0.99f, 1, -1, 1);
//        }
//    }
    
    public void playSample(String s, int duration) {
        MediaPlayerPool mp = poolMap.get(s);
        System.out.println("Attempting to play sample");
        System.out.println("Playing..");
        lastStream = mp.play(100, 100);
        System.out.println("Playing!");
    }
    
//    public void pauseSample(String s) {
//        int t = soundMap.get(s);
//        soundPool.stop(t);
//    }
    public void pauseSample() {
        if(lastStream != null) {
            System.out.println("Pausing");
            lastStream.pause();
        }
    }
    
    public void stop() {
//        Iterator it;
//        Collection<MediaPlayerPool> c = poolMap.values();
//        it = c.iterator();
//        while(it.hasNext()) {
//            MediaPlayerPool mpp = (MediaPlayerPool) it.next();
//            if(mpp != null) {
//                mpp.dispose();
//            }
//        }
//        mediaPlayerPools.dispose();
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
    
    public static double gcd(double a, double b) {      
        if (b==0)
            return a;
        else
            return gcd(b, a % b);
    }
   
    public static double lcm(double a, double b) {
        return (a / gcd(a, b)) * b;
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
    return Double.valueOf(twoDForm.format(d));
}
    double roundThreeDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
    return Double.valueOf(twoDForm.format(d));
}
    
}

