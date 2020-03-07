package ca.mcgill.ecse211.test;



import ca.mcgill.ecse211.project.*;
import static ca.mcgill.ecse211.project.Resources.*;


/**
 * This class tests the ultrasonic localization of the robot by performing the falling edge localization routine.
 * 
 * @author Han
 */
public class USLocalizationTest {

  public static void main(String[] args) {
    new Thread(odometer).start();
    new Thread(new Display()).start();
    Localization.fallingEdge();
  }

}
