package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;

/**
 * The Main class runs the program.
 * 
 * @author George Kandalaft
 * @author Han Zhou
 * 
 */
public class Main {

  /**
   * The main entry point that starts all the needed threads.
   * 
   * @param args
   */
  public static void main(String[] args) {

    new Thread(odometer).start();
    // new Thread(new Display()).start();

    MapRouter.tillPassTunnel();
    MapRouter.tillLaunch_OA();
    MapRouter.finishRest();
    System.exit(0);
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
