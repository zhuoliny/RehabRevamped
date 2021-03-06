package com.example.matthew.rehabrevamped.UserWorkouts;

import android.content.Context;
import android.text.format.Time;


import com.example.matthew.rehabrevamped.Utilities.GripAnalysis;
import com.example.matthew.rehabrevamped.Utilities.JerkScoreAnalysis;
import com.example.matthew.rehabrevamped.Utilities.SampleAverage;

import java.util.ArrayList;

/**
 * Created by Matthew on 7/10/2016.
 */
public class PickUpCount implements WorkoutSession {

    int getPickupCountMax = 10;
    SampleAverage sampleAverage = new SampleAverage();
    float countPickupLastVal = 10;
    float countAccuracyLastVal = 8;
    int pickupCount = 0;
    Time startTime = new Time();
    String whatToSay = "";
    boolean shouldITalk = false;
    double lastSlope = 0;
    double collisionNumber = 0;
    float a = 0;
    boolean imOnLowerSurface = true;
    long StartTime = System.currentTimeMillis();
    ArrayList<String> stringsIHaveSaid = new ArrayList<>();
    double totaldatas = 0;
    boolean startedWork = false;
    GripAnalysis gripAnalysis = new GripAnalysis();
    boolean inMotion = false;
    boolean hasStarted = false;
    long startOfWorkoutForGrade = System.currentTimeMillis();

    //Jerk Stuff
    JerkScoreAnalysis jerkScoreAnalysis = new JerkScoreAnalysis(3);
    long jerkStartTime = System.currentTimeMillis();

    public PickUpCount() {
        startTime.setToNow();
    }

    public void dataIn(float accX, float accY, float accZ, float gravX, float gravY, float gravZ, int walkingCount, Context context) {

        jerkScoreAnalysis.jerkAdd(accX, accY, accZ);
        float differenceVAL = Math.abs(accY - countPickupLastVal);
        a = differenceVAL;
        countPickupLastVal = accY;
        sampleAverage.addSmoothAverage(differenceVAL);
        Time nowTime = new Time();
        nowTime.setToNow();
        holdAccuracy(accX, accY, accZ);
        long differenceTime = Math.abs(nowTime.toMillis(true) - startTime.toMillis(true));
        if (sampleAverage.getMedianAverage() < .22 && differenceTime > 2000 && inMotion) {
            startTime.setToNow();
            shouldITalk = true;
            pickupCount++;
            whatToSay = "" + pickupCount;
            jerkScoreAnalysis.jerkCompute(Math.abs(System.currentTimeMillis() - jerkStartTime));
            jerkStartTime = System.currentTimeMillis();
            inMotion = false;
            imOnLowerSurface = !imOnLowerSurface;
        } else if (sampleAverage.getMedianAverage() > .5 && !inMotion) {
            inMotion = true;
        }
    }


    @Override
    public int getGrade() {
        return jerkScoreAnalysis.getJerkAverage().intValue();
    }

    @Override
    public void holdAccuracy(float accX, float accY, float accZ) {
        float accTotal = (float) Math.pow((Math.pow(accX, 2) + Math.pow(accY, 2) + Math.pow(accZ, 2)), .5);
        double slope = accTotal - countAccuracyLastVal;
        if (((slope > 0 && lastSlope < 0) || (lastSlope > 0 && slope < 0)) && countAccuracyLastVal <= 8.6) {
            collisionNumber++;
        }
        countAccuracyLastVal = accTotal;
        lastSlope = slope;
    }


    @Override
    public boolean workoutFinished() {
        if (pickupCount == getPickupCountMax) {
            return true;
        }
        return false;
    }

    @Override
    public String result() {

        return "\n\n\nPut away " + pickupCount + " time(s).";
    }


    @Override
    public boolean shouldISaySomething() {
        return shouldITalk;
    }

    @Override
    public float getJerkScore() {
        return jerkScoreAnalysis.getJerkAverage();
    }

    @Override
    public String whatToSay() {
        shouldITalk = false;
        return whatToSay;
    }

    @Override
    public String saveData() {
        String returnMe = "\n" +
                "---Watch Positional Average---\n" +
                "-X: " + gripAnalysis.getGripXGravMean() + "-\n" +
                "-Y: " + gripAnalysis.getGripYGravMean() + "-\n" +
                "-Z: " + gripAnalysis.getGripZGravMean() + "-\n" +
                "-------------------------------";
        return returnMe;
    }

    @Override
    public String getWorkoutName() {
        return "Pick Up Put Down";
    }

    @Override
    public String stringOut() {
        return "Pick Up Count: " + pickupCount;
    }

    @Override
    public Float dataOut() {
        return null;
    }

    @Override
    public void addTouche(float x, float y) {

    }

    @Override
    public float[][] getHoldParamaters() {
        return new float[][]{{0.7f,.3f,-5f,9.5f ,10f ,4.5f},{1f,10f ,-7.5f ,1.5f}};
    }



    @Override
    public String sayHowToHoldCup() {
        return "In this workout you will put the cup above your head and back onto the table. Be sure to let it sit on the table and when I count pick up the cup again.";
    }

}
