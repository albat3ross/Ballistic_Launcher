package ca.mcgill.ecse211.test;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * This class tests the accuracy and consistency of the color sensor in detecting black lines while running the robot 
 * in a straight line across 3 black lines.
 * 
 * @author Christina
 *
 */
public class ColorSensorTest implements Runnable {

  /**
   * The speed with which the motors move forward (deg/sec).
   */
  public static final int FWD_SPEED = 100;

  /**
   * The left motor.
   */
  public static final EV3LargeRegulatedMotor LEFT_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

  /**
   * The right motor.
   */
  public static final EV3LargeRegulatedMotor RIGHT_MOTOR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  /**
   * The wheel radius.
   */
  public static final double WHEEL_RAD = 2.2;

  /**
   * Initializing color sensor.
   */
  public static final EV3ColorSensor COLOR_SENSOR = new EV3ColorSensor(SensorPort.S1);

  /**
   * Specified mode of color sensor to detect light intensity.
   */
  private SampleProvider iSampleProvider = COLOR_SENSOR.getMode("Red");

  /**
   * Array to store color sensor samples.
   */
  private float[] iData = new float[iSampleProvider.sampleSize()];


  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) {
    return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
  }

  /**
   * Variable to store measured intensity in previous loop.
   */
  private static float prevIntensity = 0;

  /**
   * Variable to store measured intensity in current loop.
   */
  private static float nowIntensity = 0;

  /**
   * Variable to compute the difference between the current intensity reading and the previous one.
   */
  private static float intensityDifference = 0;


  public void run() {
    iSampleProvider.fetchSample(iData, 0);
    prevIntensity = iData[0];
    int numSamples = 0;

    while (numSamples <= 50) {

      iSampleProvider.fetchSample(iData, 0);
      numSamples++;
      nowIntensity = iData[0];
      intensityDifference = nowIntensity - prevIntensity;
      System.out.println(intensityDifference);
      if (intensityDifference <= -0.045) {
        Sound.beep();
      }
      prevIntensity = nowIntensity;
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }

  public static void main(String args[]) {
    ColorSensorTest c = new ColorSensorTest();
    new Thread(c).start();
    LEFT_MOTOR.setSpeed(FWD_SPEED);
    RIGHT_MOTOR.setSpeed(FWD_SPEED);

    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();

  }

}
