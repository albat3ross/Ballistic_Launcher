package ca.mcgill.ecse211.test;


import lejos.hardware.Button;
import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * 
 * This a class to test basic movement of the robot (SQUARE DRIVE). The robot should be able to travel in a square
 * 
 * @author George Kandalaft
 * @author Han Zhou
 *
 * 
 */

public class SquareDriver {

  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();

    odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);

    Navigation.travelTo(4 * TILE_SIZE, TILE_SIZE);
    Navigation.travelTo(4 * TILE_SIZE, 4 * TILE_SIZE);
    Navigation.travelTo(TILE_SIZE, 4 * TILE_SIZE);
    Navigation.travelTo(TILE_SIZE, TILE_SIZE);


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
