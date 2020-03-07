package ca.mcgill.ecse211.test;

import ca.mcgill.ecse211.project.*;
import ca.mcgill.ecse211.project.Resources.Point;
import lejos.hardware.Button;
import static ca.mcgill.ecse211.project.Resources.*;

/** 
 * This class is meant to be a test class for the beta demo in which parameters are generated locally. 
 * @author Han Zhou
 *
 */
public class DemoTest {


  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();
    initparams1();
    MapRouter.tillPassTunnel();

    while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
      sleepFor(500);
    }
    System.exit(0);
  }
  /**
   *08 ||0 0 0 0 0 2 2 2 2 
    07 ||0 0 0 0 0 2 2 2 2 
    06 ||0 0 0 0 0 2 2 2 2 
    05 ||0 0 0 0 0 2 2 2 2 
    04 ||0 0 0 0 0 2 2 4 2 
    03 ||0 0 0 0 0 2 2 2 2 
    02 ||1 1 1 3 3 2 2 2 2 
    01 ||1 1 1 0 0 2 2 2 2 
    00 ||1 1 1 0 0 0 0 0 0 
        ==================
         0 0 0 0 0 0 0 0 0 
         0 1 2 3 4 5 6 7 8
   */
  private static void initparams1() {
    greenTeam = 10;
    greenCorner = 0;
    red = new Region(0,4,3,8);
    green = new Region(0,0,6,3);
    tng = new Region(5,3,6,5);
    tnr = new Region(2,225,5,233);
    greenBin = new Point(7,7);
    island = new Region(4,5,8,8);
  }
  /**
   *08 ||2 2 2 2 2 2 2 2 2 
    07 ||2 2 2 2 4 2 2 2 2 
    06 ||2 2 2 2 2 2 2 2 2 
    05 ||2 2 2 2 2 2 2 2 2 
    04 ||0 0 3 0 0 0 0 0 0 
    03 ||0 0 3 0 0 0 0 0 0 
    02 ||1 1 1 0 0 0 0 0 0 
    01 ||1 1 1 0 0 0 0 0 0 
    00 ||1 1 1 0 0 0 0 0 0 
        ==================
         0 0 0 0 0 0 0 0 0 
         0 1 2 3 4 5 6 7 8
   */
  private static void initparams2() {
    greenTeam = 10;
    greenCorner = 0;
    green = new Region(0,0,3,3);
    tng = new Region(2,3,3,5);
    tnr = new Region(2,3,3,5);
    greenBin = new Point(4,7);
    island = new Region(0,5,9,9);
  }


  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
    }
  }

}
