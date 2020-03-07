package ca.mcgill.ecse211.test;

import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;


/**
 * This class tests phase 2 of the robot's trajectory, which consists of traveling from the tunnel to a suitable
 * launching point while avoiding obstacles in the way to finally get a ball into the target bin.
 * 
 * @author Mike Wang
 *
 */
public class Phase2Test {
  public static void main(String[] args) {
    // initial robot coordinates and orientation depending on the map we are testing
    double startingX = 197.79;
    double startingY = 3.5 * TILE_SIZE;
    double startingTheta = 0;

    new Thread(odometer).start();
    new Thread(new Display()).start();
    MapRouter.initSafeTiles();

    // setting odometer coordinates and orientation to those of the starting
    // point when the robot is out of the tunnel to isolate testing phase 2
    odometer.setXYT(startingX, startingY, startingTheta);

    MapRouter.tillLaunch_OA(); // start phase 2
  }
}
