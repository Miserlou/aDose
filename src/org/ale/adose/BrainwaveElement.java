package org.ale.adose;

public class BrainwaveElement {
    
    public String leftOnColor;
    public String leftOffColor;
    public String rightOnColor;
    public String rightOffColor;
    public double leftFreq;
    public double rightFreq;
    public int duration;
    
    public BrainwaveElement(double lfreq, String loff, String lon, double rfreq, String roff, String ron, int d) {
        this.leftFreq = lfreq;
        this.leftOffColor = loff;
        this.leftOnColor = lon;
        this.rightFreq = rfreq;
        this.rightOffColor = roff;
        this.rightOnColor = ron;
        this.duration = d;
    }
    
    public String getLeftOnColor() {
        return leftOnColor;
    }

    public void setLeftOnColor(String leftOnColor) {
        this.leftOnColor = leftOnColor;
    }

    public String getLeftOffColor() {
        return leftOffColor;
    }

    public void setLeftOffColor(String leftOffColor) {
        this.leftOffColor = leftOffColor;
    }

    public String getRightOnColor() {
        return rightOnColor;
    }

    public void setRightOnColor(String rightOnColor) {
        this.rightOnColor = rightOnColor;
    }

    public String getRightOffColor() {
        return rightOffColor;
    }

    public void setRightOffColor(String rightOffColor) {
        this.rightOffColor = rightOffColor;
    }

    public double getLeftFreq() {
        return leftFreq;
    }

    public void setLeftFreq(double leftFreq) {
        this.leftFreq = leftFreq;
    }

    public double getRightFreq() {
        return rightFreq;
    }

    public void setRightFreq(double rightFreq) {
        this.rightFreq = rightFreq;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append(" Left Freqeuency: " + leftFreq + NEW_LINE);
        result.append(" Left Off Color: " + leftOffColor + NEW_LINE);
        result.append(" Left On Color: " + leftOnColor + NEW_LINE);
        result.append(" Right Freqeuency: " + rightFreq + NEW_LINE);
        result.append(" Right Off Color: " + rightOffColor + NEW_LINE);
        result.append(" Right On Color: " + rightOnColor + NEW_LINE);
        result.append(" Duration: " + duration + NEW_LINE);

        return result.toString();
      }


}
