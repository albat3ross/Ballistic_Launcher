package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import lejos.hardware.Sound;

public class Localization {
  /**
   * Filter count.
   */
  public static int filter = 0;

  /**
   * Filtering limitation.
   */
  public static final int FILTER_OUT = 30;

  /**
   * Threshold distance for an unsafe distance that must be checked for obstacles before moving to it.
   */
  public static final int THRESHOLD_DISTANCE = (int) TILE_SIZE;

  /**
   * Period for which to sleep the localization thread.
   */
  public static final int SLEEP = 20;

  /**
   * Array to store the ultrasonic readings.
   */
  private static float[] usData = new float[usSensor.sampleSize()];

  /**
   * Variable to store the current ultrasonic reading.
   */
  static int distance;

  /**
   * Localizes using the falling edge routine.
   */
  public static void loc() {
    fallingEdge();
    odometer.setXYT(TILE_SIZE / 2, TILE_SIZE / 2, 0);
    MapRouter.goToTile_s(1, 0, false);
    Navigation.turnTo(90.0);
    MapRouter.goToTile_s(1, 1, false);
    boolean isRed = (redTeam == 10);
    int corner = (isRed) ? redCorner : greenCorner;
    switch (corner) {
      case 0:
        odometer.setTile(1, 1, 90.0);
        break;
      case 1:
        odometer.setTile(13, 1, 180.0);
        break;
      case 2:
        odometer.setTile(13, 7, 270.0);
        break;
      case 3:
        odometer.setTile(1, 7, 0.0);
        break;

      default:
        System.exit(0);
    }
    Sound.beep();
    Sound.beep();
    Sound.beep();
  }

  /**
   * Performs the falling edge routine.
   */
  public static void fallingEdge() {
    isfacingwall();
    turnOut(true);
    turnIn(true); // rotate till can see bottom wall
    odometer.setTheta(0);
    Navigation.directTurn(90.0);
    turnIn(false); // rotate back till can see left wall
    double dt = odometer.getT();
    double ntheta = (dt / 2.0);
    Navigation.turnTo(ntheta);
    odometer.setTheta(45.0);
    Navigation.turnTo(0);
  }

  /**
   * Turns the robot till it cannot see the wall
   * 
   * @param clockwise, if true instructs the robot to turn clockwise
   */
  private static void turnOut(boolean clockwise) {
    isfacingwall();
    Navigation.modeTurn(clockwise);
    while (true) {
      pollDist();
      if (distance > THRESHOLD_DISTANCE * 1.6) {
        break;
      }
      try {
        Thread.sleep(SLEEP);
      } catch (InterruptedException e) {
      }
    }
    Navigation.setSpeeds(0, 0);
  }

  /**
   * TurnS the robot till it can see the wall
   * 
   * @param clockwise, if true instructs the robot to turn clockwise
   */
  private static void turnIn(boolean clockwise) {
    isfacingwall();
    Navigation.modeTurn(clockwise);
    while (true) {
      pollDist();
      if (distance <= THRESHOLD_DISTANCE) {
        break;
      }
      try {
        Thread.sleep(SLEEP);
      } catch (InterruptedException e) {
      }
    }
    Navigation.setSpeeds(0, 0);
  }

  // helper functions

  /**
   * Checks if the robot is facing the wall and continues checking to ensure it reaches the filter out
   * THRESHOLD_DISTANCE
   * 
   * @return c, true if the robot is facing the wall
   */
  public static boolean isfacingwall() {
    int c = 0;
    for (int i = 0; i < FILTER_OUT + 2; i++) {
      if (pollDist() <= THRESHOLD_DISTANCE) {
        c++;
      }
      try {
        Thread.sleep(SLEEP);
      } catch (InterruptedException e) {
      }
    }
    return (c >= 10);
  }

  /**
   * Gets the distance reading from the ultrasonic sensor and does not change the distance field if it did not pass the
   * FILTER_OUT threshold.
   * 
   * @return the distance detected in cm
   */
  public static int pollDist() {
    usSensor.fetchSample(usData, 0); // acquire data
    int dist = (int) (usData[0] * 100.0); // extract from buffer, convert to cm int
    if (dist >= THRESHOLD_DISTANCE) {
      if (filter >= FILTER_OUT) {
        distance = dist;
      } else {
        filter++;
      }
    } else {
      distance = dist;
      filter = 0;
    }
    return dist;
  }

}
