package ca.mcgill.ecse211.project;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.project.Resources.*;

/**
 * The odometer class keeps track of the robot's (x, y, theta) position.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 * @author Younes Boubekeur
 */

public class Odometer implements Runnable {
  private static final double ROTATION_SACLE = 1.0;
  /**
   * The x-axis position in cm.
   */
  private volatile double x;

  /**
   * The y-axis position in cm.
   */
  private volatile double y; // y-axis position

  /**
   * The orientation in degrees.
   */
  private volatile double theta; // Head angle

  /**
   * The (x, y, theta) position as an array
   */
  private double[] position;

  // Thread control tools
  /**
   * Fair lock for concurrent writing
   */
  private static Lock lock = new ReentrantLock(true);

  /**
   * Indicates if a thread is trying to reset any position parameters
   */
  private volatile boolean isResetting = false;

  /**
   * Lets other threads know that a reset operation is over.
   */
  private Condition doneResetting = lock.newCondition();

  private static Odometer odo; // Returned as singleton

  // Motor-related variables
  private static int leftMotorTachoCount = 0;
  private static int rightMotorTachoCount = 0;

  /**
   * The odometer update period in ms.
   */
  private static final long ODOMETER_PERIOD = 25;


  /**
   * This is the default constructor of this class. It initiates all motors and variables once.It cannot be accessed
   * externally.
   */
  private Odometer() {
    setXYT(0, 0, 90.0);
  }

  /**
   * Returns the Odometer Object. Use this method to obtain the instance of Odometer.
   * 
   * @return the Odometer Object
   */
  public synchronized static Odometer getOdometer() {
    if (odo == null) {
      odo = new Odometer();
    }

    return odo;
  }

  /**
   * This method is where the logic for the odometer will run.
   */
  public void run() {
    long updateStart, updateEnd;

    leftMotorTachoCount = leftMotor.getTachoCount();
    rightMotorTachoCount = rightMotor.getTachoCount();

    while (true) {
      updateStart = System.currentTimeMillis();

      int lrot = leftMotor.getTachoCount() - leftMotorTachoCount;
      int rrot = rightMotor.getTachoCount() - rightMotorTachoCount;
      double pos[] = this.getXYT();

      // Calculate new wheel position based on tachometer counts
      double dleft = WHEEL_RAD * Math.PI * lrot / 180.0;
      double dright = WHEEL_RAD * Math.PI * rrot / 180.0;

      // difference between displacement of two wheels
      double dd = dright - dleft;
      // estimated displacement of the center of the robot
      double dh = (dright + dleft) / 2.0;
      // calculate the change in angle
      double dtheta = Math.toDegrees(Math.asin(dd / TRACK));
      double ntheta = pos[2] + dtheta;

      // calculate the change in x and y according to its current position
      double dx = dh * Math.cos(Math.toRadians(ntheta));
      double dy = dh * Math.sin(Math.toRadians(ntheta));

      leftMotorTachoCount = leftMotor.getTachoCount();
      rightMotorTachoCount = rightMotor.getTachoCount();

      // some tricks to ensure odometer works
      dtheta *= ROTATION_SACLE;
      dtheta = (pos[2] + dtheta < 0) ? dtheta + 360 : dtheta;
      dtheta = (pos[2] + dtheta > 360) ? dtheta - 360 : dtheta;

      // update the odometer
      odo.update(dx, dy, dtheta);


      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();

      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done
        }
      }
    }
  }

  /**
   * Returns the Odometer data.
   * <p>
   * Writes the current position and orientation of the robot onto the odoData array. {@code odoData[0] =
   * x, odoData[1] = y; odoData[2] = theta;}
   * 
   * @param position the array to store the odometer data
   * @return the odometer data.
   */
  public double[] getXYT() {
    double[] position = new double[3];
    lock.lock();
    try {
      while (isResetting) { // If a reset operation is being executed, wait until it is over.
        doneResetting.await(); // Using await() is lighter on the CPU than simple busy wait.
      }

      position[0] = x;
      position[1] = y;
      position[2] = theta;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }

    return position;
  }

  /**
   * Adds dx, dy and dtheta to the current values of x, y and theta, respectively. Useful for odometry.
   * 
   * @param dx
   * @param dy
   * @param dtheta
   */
  public void update(double dx, double dy, double dtheta) {
    lock.lock();
    isResetting = true;
    try {
      x += dx;
      y += dy;
      theta = (theta + (dtheta % 360)) % 360; // keeps the updates within 360 degrees
      isResetting = false;
      doneResetting.signalAll(); // Let the other threads know we are done resetting
    } finally {
      lock.unlock();
    }

  }

  /**
   * Overrides the values of x, y and theta. Use for odometry correction.
   * 
   * @param x the value of x
   * @param y the value of y
   * @param theta the value of theta in degrees
   */
  public void setXYT(double x, double y, double theta) {
    lock.lock();
    isResetting = true;
    try {
      this.x = x;
      this.y = y;
      this.theta = theta;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public void setTile(int xt, int yt, double theta) {
    lock.lock();
    isResetting = true;
    try {
      this.x = (xt + 0.5) * TILE_SIZE;
      this.y = (yt + 0.5) * TILE_SIZE;
      this.theta = theta;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites x. Use for odometry correction.
   * 
   * @param x the value of x
   */
  public void setX(double x) {
    lock.lock();
    isResetting = true;
    try {
      this.x = x;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites y. Use for odometry correction.
   * 
   * @param y the value of y
   */
  public void setY(double y) {
    lock.lock();
    isResetting = true;
    try {
      this.y = y;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Overwrites theta. Use for odometry correction.
   * 
   * @param theta the value of theta
   */
  public void setTheta(double theta) {
    lock.lock();
    isResetting = true;
    try {
      this.theta = theta;
      isResetting = false;
      doneResetting.signalAll();
    } finally {
      lock.unlock();
    }
  }

  /**
   * return theta in angles
   * 
   * @return
   */
  public double getT() {
    double result = 0;
    lock.lock();
    try {
      while (isResetting) {
        doneResetting.await();
      }

      result = theta;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }

    return result;
  }


}
