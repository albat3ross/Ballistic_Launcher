package ca.mcgill.ecse211.test;

import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * This class is an integrated test for Phase 1 and Phase 2 of the robot's trajectory together. Phase 1 consists of
 * localization, navigation to the tunnel, and passing through the tunnel.
 * 
 * @author Mike Wang
 *
 */

public class Phase1and2Test {
  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();
    MapRouter.tillPassTunnel(); // start phase 1
    MapRouter.tillLaunch_OA(); // start phase 2
  }
}
