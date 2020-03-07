package ca.mcgill.ecse211.test;

import ca.mcgill.ecse211.project.Navigation;
import lejos.hardware.Button;

/**
 * This class tests the launching mechanism by instructing the robot to launch a ball.
 * 
 * @author Christina
 * 
 */
public class LaunchTest {

  public static void main(String args[]) {
    Navigation.doPreLaunch();
    while (Button.waitForAnyPress() == Button.ID_ENTER) {
      Navigation.doBallLaunch();
    }

  }
}
