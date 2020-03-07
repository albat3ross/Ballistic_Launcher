package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

public class USGuard implements Runnable {
  /**
   * Sets the sleep period of this thread in milliseconds.
   */
  private static final int US_SLEEP_PERIOD = 250;
  /**
   * The threshold to define the unsafe distance that the ultrasonic sensor must check for obstacles before allowing
   * robot to move further (cm).
   */
  private static final int US_UNSAFE_THRESHOLD = (int) TILE_SIZE;

  // arrays to hold the ultrasonic sensor readings
  private static float[] usData = new float[usSensor.sampleSize()];
  private static float[] usData1 = new float[usSensor.sampleSize()];
  private static float[] usData2 = new float[usSensor.sampleSize()];

  /**
   * Empty constructor.
   */
  public USGuard() {

  }

  @Override
  public void run() {
    if (MapRouter.isTravelingStraight || MapRouter.isTravelingDiagonal) {
      doGuard();
    } else if (MapRouter.isScanning) {
      doScan();
    }

  }

  /**
   * Allows the robot to detect an obstacle in front of it and to take appropriate action based on that.
   */
  private void doGuard() {
    long updateStart, updateEnd;
    int dist;
    while (MapRouter.isTravelingStraight || MapRouter.isTravelingDiagonal) {
      updateStart = System.currentTimeMillis();

      dist = pollDist();

      if (dist <= US_UNSAFE_THRESHOLD && confirmObstacle(US_UNSAFE_THRESHOLD + 5)) {
        MapRouter.isTravelingStraight = false;
        MapRouter.isTravelingDiagonal = false;
        break;
      }

      updateEnd = System.currentTimeMillis();
      if ((updateEnd - updateStart) < US_SLEEP_PERIOD) {
        sleepFor(US_SLEEP_PERIOD - (updateEnd - updateStart));
      }
    }
  }


  /**
   * Allows the robot to scan the front tile and decide if it's safe.
   */
  private void doScan() {
    long updateStart, updateEnd;
    int dist;
    while (MapRouter.isScanning) {
      updateStart = System.currentTimeMillis();

      dist = pollDist();

      if (dist <= US_UNSAFE_THRESHOLD + 5 && confirmObstacle(US_UNSAFE_THRESHOLD + 10)) {
        MapRouter.isScanning = false;
        break;
      }

      updateEnd = System.currentTimeMillis();
      if ((updateEnd - updateStart) < US_SLEEP_PERIOD) {
        sleepFor(US_SLEEP_PERIOD - (updateEnd - updateStart));
      }
    }

  }

  /**
   * Confirms the presence of an obstacle on the robot's path.
   * 
   * @param dist
   * @return true if an obstacle is detected false if no obstacle is detected
   */
  private boolean confirmObstacle(int dist) {
    int confirm = 0;
    int total = 5;
    for (int i = 0; i < total; i++) {
      if (pollDist() < dist)
        confirm++;
      sleepFor(50);
    }
    if (confirm > 2) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets the distance reading from the ultrasonic sensor continuously. Does not change the distance field if it did not
   * pass the FILTER_OUT threshold
   *
   * @return the distance detected in cm.
   */
  public int pollDist() {
    // usSensor.getDistanceMode().fetchSample(usData, 0); // acquire data
    // int dist = (int) (usData[0] * 100.0); // extract from buffer, convert to cm int
    // return dist;
    int distance = 0;
    usSensor.getDistanceMode().fetchSample(usData, 0); // acquire distance1 data in meters
    usSensor.getDistanceMode().fetchSample(usData1, 0); // acquire distance2 data in meters
    usSensor.getDistanceMode().fetchSample(usData2, 0); // acquire distance3 data in meters
    if ((Math.abs(usData[0] - usData2[0]) < Math.abs(usData1[0] - usData2[0]))) {
      // extract from buffer, convert to cm, cast to int
      distance = (int) ((usData[0] + usData2[0]) / 2 * 100.0);
    }
    if ((Math.abs(usData[0] - usData2[0]) >= Math.abs(usData1[0] - usData2[0]))) {
      // extract from buffer, convert to cm, cast to int
      distance = (int) ((usData1[0] + usData2[0]) / 2 * 100.0);
    }

    return distance;

  }

  /**
   * This method sets the USGuard thread to sleep for the specified duration.
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
