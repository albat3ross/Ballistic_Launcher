package ca.mcgill.ecse211.test;

import static ca.mcgill.ecse211.project.Resources.odometer;
import static ca.mcgill.ecse211.project.Resources.*;
import ca.mcgill.ecse211.project.Display;
import ca.mcgill.ecse211.project.MapRouter;

/**
 * This class tests the odometer correction done by the two light sensors by running the robot in a straight line
 * towards tile (4,4) and correcting the odometer at each line encountered.
 * 
 * @author Han Zhou
 * 
 */
public class LSCorrectionTest {

  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();

    // start from the middle of the first tile
    odometer.update(TILE_SIZE, TILE_SIZE, 0);
    MapRouter.chargeToTile(4, 4, true, false);
  }
}
