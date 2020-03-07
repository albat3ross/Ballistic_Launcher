package ca.mcgill.ecse211.test;



import static ca.mcgill.ecse211.project.Resources.usSensor;

/**
 * This class tests the accuracy of the ultrasonic sensor and prints data to the screen.
 * 
 * @author George Kandalaft
 *
 */

public class UltrasonicSensorTest implements Runnable {

  // arrays to hold ultrasonic sensor readings
  private float[] usData;
  private float[] usData1;
  private float[] usData2;
  
  // initializing variable to hold detected object distance
  public static int objectDistance = 0;

  public UltrasonicSensorTest() {
    usData = new float[usSensor.sampleSize()];
    usData1 = new float[usSensor.sampleSize()];
    usData2 = new float[usSensor.sampleSize()];

  }

  public void run() {

    int distance = 0;
    while (true) {
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
      System.out.println(distance);
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      } // Poor man's timed sampling
    }

  }

  public static void main(String args[]) {
    UltrasonicSensorTest u = new UltrasonicSensorTest();
    new Thread(u).start();
  }

}

