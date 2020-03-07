package ca.mcgill.ecse211.test;

import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * This class tests Phase 1 of the robot's trajectory, which consists of localization, navigation to the tunnel, and
 * passing through the tunnel.
 * 
 * @author Mike Wang
 *
 */
public class Phase1Test {
  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();
    MapRouter.tillPassTunnel(); // start phase 1

  }
}
