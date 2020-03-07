package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static java.lang.Math.atan2;

/**
 * Navigation class contains most of the movement methods for the robot.
 * 
 * @author George Kandalaft
 * @author Han Zhou
 * 
 */

public class Navigation {

  /**
   * Sets the speeds for two wheels. Supports negative values.
   * 
   * @param leftSpeed speed of the left motor
   * @param rightSpeed speed of the right motor
   */
  public static void setSpeeds(float leftSpeed, float rightSpeed) {
    leftMotor.setSpeed(leftSpeed);
    rightMotor.setSpeed(rightSpeed);
    if (leftSpeed < 0) {
      leftMotor.backward();
    } else {
      leftMotor.forward();
    }
    if (rightSpeed < 0) {
      rightMotor.backward();
    } else {
      rightMotor.forward();
    }
  }

  /**
   * Turns to the degrees with regards to the odometer. It is the most reliable function.
   * 
   * @param angle
   */
  public static void turnTo(double angle) {
    double error = angle - odometer.getT();
    error = error % 360;
    if (error >= 180) {
      error = error - 360;
    } else if (error <= -180) {
      error = error + 360;
    }
    directTurn(error);
  }

  /**
   * Turns the robot to the specified tile regards to the odometer. 
   * 
   * @param x x coordinate of the target tile we want to turn to 
   * @param y y coordinate of the target tile we want to turn to 
   */
  public static void turnTo(double x, double y) {
    turnTo(getDestAngle(x, y));
  }

  /**
   * Turns the robot to the specified tile and converts tile coordinates into cm. 
   * 
   * @param x_tile x coordinate of the target tile we want to turn to 
   * @param y_tile y coordinate of the target tile we want to turn to 
   */
  public static void turnTo(int x_tile, int y_tile) {
    turnTo(getDestAngle((x_tile + 0.5) * TILE_SIZE, (y_tile + 0.5) * TILE_SIZE));
  }

  /**
   * Travels to a destination point while continuously adjusting the robot's heading during travel.
   * 
   * @param x destination x
   * @param y destination y
   */
  public static void travelTo(double x, double y) {
    turnTo(getDestAngle(x, y));
    directDrive(getDestDist(x, y) * travelTo_SCALER);

  }

  /**
   * Drives the robot without interruption.
   * 
   * @param dist
   */
  public static void directDrive(double dist) {
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);

    leftMotor.rotate(convertDistance(dist), true);
    rightMotor.rotate(convertDistance(dist), false);
  }

  /**
   * Turns the robot without interruption by the specified angle.
   * 
   * @param angle by which to turn the robot
   */
  public static void directTurn(double angle) {
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);

    leftMotor.rotate(-convertAngle(angle), true);
    rightMotor.rotate(convertAngle(angle), false);
  }

  /**
   * Turns the robot in one direction. Will turn clockwise if clockwise is true.
   * 
   * @param clockwise
   */
  public static void modeTurn(boolean clockwise) {
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    if (clockwise) {
      leftMotor.forward();
      rightMotor.backward();
    } else {
      leftMotor.backward();
      rightMotor.forward();
    }
  }

  /**
   * Makes the robot keep going in one direction.
   * 
   * @param forward if true, indicates that the robot should move forward
   */
  public static void modeTravel(boolean forward) {
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    if (forward) {
      leftMotor.forward();
      rightMotor.forward();
    } else {
      leftMotor.backward();
      rightMotor.backward();
    }


  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover that distance.
   * 
   * @param distance
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) {
    return (int) ((180.0 * distance) / (Math.PI * WHEEL_RAD));
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the robot by that angle.
   * 
   * @param angle
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) {
    return convertDistance(Math.PI * TRACK * angle / 360.0);
  }

  /**
   * Calculates the distance left to travel to get to the specified destination.
   * 
   * @param x coordinate of destination
   * @param y coordinate of destination
   * @return distance left to travel
   */
  public static double getDestDist(double x, double y) {
    double[] pos = odometer.getXYT();
    double dist = Math.sqrt(Math.pow(Math.abs(pos[0] - x), 2) + Math.pow(Math.abs(pos[1] - y), 2));
    return dist;
  }

  /**
   * Calculates the minimal angle from position to destination.
   * 
   * @param x coordinate of destination
   * @param y coordinate of destination
   * @return minAng the minimal angle by which to turn the robot
   */
  public static double getDestAngle(double x, double y) {
    double minAng = Math.toDegrees((atan2(y - odometer.getXYT()[1], x - odometer.getXYT()[0])));
    return minAng;
  }

  /**
   * The speed with which to rotate the motors back to prepare for launch (in deg/sec).
   */
  private static final int BACKWARDLAUNCH_SPD = 120;

  /**
   * The speed with which to rotate the motors forward to launch the ball (in deg/sec).
   */
  public static final int LAUNCH_SPD = 700;

  /**
   * The angle by which to rotate the motors forward to launch the ball (deg).
   */
  public static final int LAUNCH_ANGLE = 90;

  /**
   * Launches the ball.
   * 
   * @param void
   * @return ball launch
   */
  public static void doBallLaunch() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }
    launchMotor.setSpeed(LAUNCH_SPD);
    launchMotor.setAcceleration(5000);
    launchMotor.rotate(-5, false);
    launchMotor.rotate(-LAUNCH_ANGLE, false);
    launchMotor.setSpeed(BACKWARDLAUNCH_SPD);
    launchMotor.rotate(LAUNCH_ANGLE - 20, false);

  }

  /**
   * Prepares the motor for launch by first unfolding the arm down.
   */
  public static void doPreLaunch() {
    launchMotor.setAcceleration(25); // it was 5000
    launchMotor.setSpeed(10);
//    launchMotor.rotate(, false);
    launchMotor.rotate(10, false);
    launchMotor.rotate(10, false);

    launchMotor.flt(true);
  }

  /**
   * Positions the robot at a proper launch point from the target.
   * 
   * @param x_tile x coordinate of the target tile
   * @param y_tile y coordinate of the target tile
   */
  public static void positionTo(int x_tile, int y_tile) {
    turnTo((x_tile) * TILE_SIZE, (y_tile) * TILE_SIZE);
    double dist = getDestDist(x_tile * TILE_SIZE, y_tile * TILE_SIZE);
    double disterror = dist - LAUNCH_DIST;
    if (disterror > TILE_SIZE / 2)
      disterror = TILE_SIZE / 2;
    if (disterror < -TILE_SIZE / 2)
      disterror = -TILE_SIZE / 2;

    directDrive(disterror);
  }

  /**
   * Returns the robot back to the center of the target tile after launching. 
   * 
   * @param x_tile x coordinate of the tile
   * @param y_tile y coordinate of the tile
   */
  public static void recenterTile(int x_tile, int y_tile) {
    double x = (x_tile + 0.5) * TILE_SIZE;
    double y = (y_tile + 0.5) * TILE_SIZE;
    boolean rev = false;
    double angle = getDestAngle(x, y);
    double dist = getDestDist(x, y);
    if (angle > 90) {
      angle -= 180;
      rev = true;
    } else if (angle < -90) {
      angle += 180;
      rev = true;
    }
    turnTo(angle);
    if (rev)
      dist *= -1;
    directDrive(dist);
  }
}

