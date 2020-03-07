package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import lejos.hardware.Sound;

/**
 * The OdometerCorrector class finds out if it is safe for the robot to move to the next tile, uses the light sensors to
 * correct the odometer.
 * 
 * @author Han Zhou
 * @author George Kandalaft
 *
 */

public class OdometerCorrector implements Runnable {

  /**
   * The period for which to sleep the odometer corrector thread in milliseconds.
   */
  private static final int SLEEP_PERIOD = 20;

  /**
   * The light intensity differential threshold to detect a black line.
   */
  private static final double LS_THRESHOLD = 0.15;

  /**
   * Size of list to store intensity reading data.
   */
  private static final int LS_LISTSIZE = 10;

  /**
   * X coordinate of the destination point.
   */
  private double destx;

  /**
   * Y coordinate of the destination point.
   */
  private double desty;

  /**
   * Array to store the 1st light sensor intensity readings.
   */
  public float[] colordata_1 = new float[colorSensor_1.sampleSize()];

  /**
   * Array to store the 2nd light sensor intensity readings.
   */
  public float[] colordata_2 = new float[colorSensor_1.sampleSize()];


  /**
   * Array to store the specified number of intensity readings for the 1st color sensor.
   */
  private float[] datalist_1 = new float[LS_LISTSIZE];

  /**
   * Array to store the specified number of intensity readings for the 2nd color sensor.
   */
  private float[] datalist_2 = new float[LS_LISTSIZE];

  /**
   * List counter that loops through the list of old intensity readings for the 1st color sensor and deletes the old
   * data.
   */
  private int listcount_1 = 0;

  /**
   * List counter that loops through the list of old intensity readings for the 2nd color sensor and deletes the old
   * data.
   */
  private int listcount_2 = 0;

  /**
   * Odometer corrector constructor.
   * 
   * @param destx x-coordinate of the destination point in cm
   * @param desty y-coordinate of the destination point in cm
   */
  public OdometerCorrector(double destx, double desty) {
    this.destx = destx;
    this.desty = desty;
  }

  @Override
  public void run() {
    if (MapRouter.isTravelingStraight) {
      doLightSensorCorrection_s();
    } else if (MapRouter.isCentering) {
      doTileCorrection();
    }

  }

  /**
   * This method uses the 2 light sensors to correct the robot's odometer while the robot is moving in right angles.
   */
  private void doLightSensorCorrection_s() {
    long updateStart, updateEnd;
    boolean leftdone = false;
    boolean rightdone = false;

    Navigation.modeTravel(true);
    while (MapRouter.isTravelingStraight) {
      updateStart = System.currentTimeMillis();
      int i = pollLight();

      if (!leftdone) {
        leftdone = (i == 1 || i == 3);
        if (leftdone) {
          leftMotor.setSpeed(0);
        }
      }
      if (!rightdone) {
        rightdone = (i == 2 || i == 3);
        if (rightdone) {
          rightMotor.setSpeed(0);
        }
      }
      if (leftdone & rightdone)
        break;

      updateEnd = System.currentTimeMillis();
      if ((updateEnd - updateStart) < SLEEP_PERIOD) {
        sleepFor(SLEEP_PERIOD - (updateEnd - updateStart));
      }
    }
    Navigation.directDrive(TILE_SIZE * 0.5 - BODY);
    odometer.setX(destx);
    odometer.setY(desty);
    odometer.setTheta((getHeading()) * 90);

  }

  /**
   * Performs odometry correction when centering the robot in a tile.
   */
  private void doTileCorrection() {
    long updateStart, updateEnd;
    boolean leftdone = false;
    boolean rightdone = false;

    Navigation.turnTo(getHeading() * 90);
    Navigation.modeTravel(false);
    while (true) {
      updateStart = System.currentTimeMillis();

      int i = pollLight();

      if (!leftdone) {
        leftdone = (i == 1 || i == 3);
        if (leftdone) {
          leftMotor.setSpeed(0);
        }
      }
      if (!rightdone) {
        rightdone = (i == 2 || i == 3);
        if (rightdone) {
          rightMotor.setSpeed(0);
        }
      }
      if (leftdone & rightdone)
        break;

      updateEnd = System.currentTimeMillis();
      if ((updateEnd - updateStart) < SLEEP_PERIOD) {
        sleepFor(SLEEP_PERIOD - (updateEnd - updateStart));
      }
    }

    Navigation.directDrive(TILE_SIZE * 0.5 - BODY);
    double[] pos = odometer.getXYT();
    double currx = (((int) (pos[0] / TILE_SIZE)) + 0.5) * TILE_SIZE;
    double curry = (((int) (pos[1] / TILE_SIZE)) + 0.5) * TILE_SIZE;
    odometer.setXYT(currx, curry, (getHeading()) * 90);

  }



  /**
   * Gets the heading of the robot
   * 
   * @return 0 if heading east (x+) 1 if heading north (y-) 2 if heading west (x-) 3 if heading south (y+)
   */
  public static int getHeading() {
    double headingAngle = odometer.getT();
    int result = (((int) headingAngle / 45) + 1) / 2;
    if (result == 4)
      result = 0;
    return result;
  }

  /**
   * Gets the light readings from the colorSensor
   * 
   * @return 0 if no line is detected, 1 if only colorsensor_1 detected line, 2 if only colorsensor_2 detected line, 3
   *         if both.
   */
  public int pollLight() {
    colorSensor_1.getRedMode().fetchSample(colordata_1, 0);
    colorSensor_2.getRedMode().fetchSample(colordata_2, 0);
    boolean sensor_1_detect_line = addData_1(colordata_1[0]);
    boolean sensor_2_detect_line = addData_2(colordata_2[0]);
    int encoded_result = 0;
    if (sensor_1_detect_line) {
      encoded_result += 1;
    }
    if (sensor_2_detect_line) {
      encoded_result += 2;
    }
    return encoded_result;
  }

  /**
   * Adds data to lightdata_1 array, which is meant to store the readings of the 1st color sensor.
   * 
   * @param d the reading from the polled color sensor
   * @return true if black line detected
   */
  private boolean addData_1(float d) {
    // replace the oldest data with new data
    datalist_1[listcount_1] = d;
    listcount_1 = (listcount_1 + 1) % LS_LISTSIZE;
    double max = Double.MIN_VALUE;
    // get the max data in the array
    for (int i = 0; i < LS_LISTSIZE; i++) {
      if (datalist_1[i] >= max)
        max = datalist_1[i];
    }
    // get the difference between max and the new data
    double diff = d - max;
    // if the difference pass the threshold, blackline is detected
    if (diff <= -LS_THRESHOLD) {
      for (int i = 0; i < LS_LISTSIZE; i++) {
        // clean the array with new data
        datalist_1[i] = d;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add data to lightdata_2 array, which is meant to store the readings of the 2nd color sensor.
   * 
   * @param d the reading from the polled color sensor
   * @return true if black line detected
   */
  private boolean addData_2(float d) {
    // replace the oldest data with new data
    datalist_2[listcount_2] = d;
    listcount_2 = (listcount_2 + 1) % LS_LISTSIZE;
    double max = Double.MIN_VALUE;
    // get the max data in the array
    for (int i = 0; i < LS_LISTSIZE; i++) {
      if (datalist_2[i] >= max)
        max = datalist_2[i];
    }
    // get the difference between max and the new data
    double diff = d - max;
    // if the difference pass the threshold, blackline is detected
    if (diff <= -LS_THRESHOLD) {
      for (int i = 0; i < LS_LISTSIZE; i++) {
        // clean the array with new data
        datalist_2[i] = d;
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method sets the odometer corrector thread to sleep for the specified duration.
   * 
   * @param duration of sleep
   */
  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
    }
  }


}
