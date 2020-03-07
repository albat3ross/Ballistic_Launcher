package ca.mcgill.ecse211.test;


import lejos.hardware.Button;
import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * This a class to test basic movement of the robot. The robot should be able to travel to the position indicated in the
 * travelTo method.
 * 
 * @author George Kandalaft
 * @author Han Zhou
 */

public class BasicMovementTest {

  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();

    // seting the odometer to (0,0,90)

    odometer.setXYT(TILE_SIZE * 1.5, TILE_SIZE * 1.5, 90);
    Navigation.turnTo(2, 1);
    Navigation.turnTo(180);



    while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
      sleepFor(500);
    }
    System.exit(0);
  }

  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
    }
  }

}
