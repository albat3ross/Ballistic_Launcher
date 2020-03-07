package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import java.util.Stack;
import lejos.hardware.Sound;

/**
 * The MapRouter class stores a basic idea of the map. It marks the tiles as safe and unsafe based on obstacles.
 * 
 * @author Han Zhou
 * @author George Kandalaft
 */
public class MapRouter {

  /**
   * Size of the memory to store the unsafe tiles that have obstacles.
   */
  private static final int MEMORY_SIZE = 8;

  /**
   * Array to store the x coordinates of the unsafe tiles.
   */
  private static int[] unsafe_tile_x = new int[MEMORY_SIZE];

  /**
   * Array to store the y coordinates of the unsafe tiles.
   */
  private static int[] unsafe_tile_y = new int[MEMORY_SIZE];

  /**
   * List counter that loops through the list of unsafe tiles and cleans the old data.
   */
  private static int listCounter = 0;

  // Memory of the path taken by the robot
  private static Stack<Integer[]> route = new Stack<Integer[]>();

  // mark
  private static final int TEAM_MARK = 1;
  private static final int ISLAND_MARK = 2;

  // state of travel
  public static boolean isTravelingStraight = false;
  public static boolean isTravelingDiagonal = false;
  public static boolean isCentering = false;
  public static boolean isScanning = false;
  public static boolean isExploring = false;
  public static boolean planB = false;

  /**
   * 2D array to store all the tiles ( safe red/green tiles have value of 1, safe island tiles have value of 2)
   */
  public static int[][] TILES = new int[15][9];
  public static int[] tunnel = {-1, -1, -1, -1};


  /**
   * This method completes phase 1 of the competition. Phase 1 consists of: localization at the starting corner,
   * navigation to the tunnel, and passing through the tunnel.
   * 
   */
  public static void tillPassTunnel() {
    LCD.clear();
    initSafeTiles();
    Localization.loc();
    // compute tunnel info
    tunnel = checkTunnel(true);
    // go to tunnel based on the direction of the tunnel
    if (tunnel[0] == -1) {
      Sound.beepSequence();
      System.exit(0);
    }
    chargeToTile(tunnel[0], tunnel[1], (tunnel[3] == 0), false);
    // turn to tunnel
    boolean isRed = (redTeam == 10);
    Region tn = (isRed) ? tnr : tng;

    if ((int) tn.ll.x == 1 || (int) tn.ll.x == 13 || (int) tn.ll.y == 1 || (int) tn.ll.y == 7) {
      Navigation.turnTo((int) tn.ll.x, (int) tn.ll.y);
      Navigation.directTurn(90);
    }
    // center
    centerCurrTile();
    Navigation.turnTo((int) tn.ll.x, (int) tn.ll.y);
    // center
    centerCurrTile();
    // pass the tunnel
    Navigation.directTurn(2);
    travelTunnel();
  }

  /**
   * This method starts phase 2 with obstacle avoidance. Phase 2 consists of navigating through the playing field's
   * island while avoiding obstacles to find a suitable launching point to get a ball into the target bin. During phase
   * 2, the robot stores safe and unsafe tiles. The safe tiles traversed by the robot will be retraced when the robot
   * returns to the starting corner.
   */
  public static void tillLaunch_OA() {
    doTargetApproach();
    launchProcess();
  }

  /**
   * This method completes phase3 of the competition. Phase 3 consists of returning back to the starting corner by
   * retracing the path taken by the robot to get to the launching spot.
   */
  public static void finishRest() {
    double[] position = odometer.getXYT();
    int currx = (int) (position[0] / TILE_SIZE);
    int curry = (int) (position[1] / TILE_SIZE);
    Navigation.recenterTile(currx, curry);
    centerCurrTile();
    Navigation.directTurn(-90);
    centerCurrTile();
    while (!route.isEmpty()) {
      Integer[] next = route.pop();
      if (checkTile(next[0], next[1])) {
        chargeToTile(next[0], next[1], true, false);
      } else {

      }

    }

    boolean isRed = (redTeam == 10);
    Region tn = (isRed) ? tnr : tng;

    tunnel = checkTunnel(false);
    chargeToTile(tunnel[0], tunnel[1], (tunnel[3] == 0), false);

    Navigation.turnTo((int) tn.ll.x, (int) tn.ll.y);

    Navigation.directTurn(90);
    centerCurrTile();
    // turn to tunnel
    Navigation.turnTo((int) tn.ll.x, (int) tn.ll.y);
    // center
    centerCurrTile();
    // pass the tunnel
    travelTunnel();
    centerCurrTile();
    int corner = (isRed) ? redCorner : greenCorner;
    switch (corner) {
      case 0:
        chargeToTile(0, 0, (tunnel[3] == 0), false);
        break;
      case 1:
        chargeToTile(14, 0, (tunnel[3] == 0), false);
        break;
      case 2:
        chargeToTile(14, 8, (tunnel[3] == 0), false);
        break;
      case 3:
        chargeToTile(0, 8, (tunnel[3] == 0), false);
        break;
    }
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Sound.beep();
  }

  /**
   * This method will be called at the start of phase 2, it will calculate a proper point to launch the ball and bring
   * the robot to the proper place.
   */
  public static void doTargetApproach() {
    // initialize parameters
    boolean keeptarget = false;
    double[] target = {0, 0, 0};
    boolean xfirst = false;
    // start exploring, the recorded tile will be used in phase 3.
    isExploring = true;
    // keep looping, each loop the robot will attempt to reach the target
    while (true) {
      // find start point
      double[] position = odometer.getXYT();
      int startx = (int) (position[0] / TILE_SIZE);
      int starty = (int) (position[1] / TILE_SIZE);
      // if from last loop we know the last calc target point is invalid, recalculate the target point.
      if (!keeptarget) {
        keeptarget = true;
        target = getTargetTile();
        if (target[0]+target[1]+target[2]<-2.9) {
          return;
        }
        // if row dist is longer, go row first.
        xfirst = (target[0] - startx > target[1] - starty);
      } else {
        xfirst = checkGetStuck((int) target[0], (int) target[1], xfirst);
      }
      // attempt to get to the point.

      System.out.println("Do target approach:" + (int) target[0] + " " + (int) target[1] + "xfirst: " + xfirst);
      boolean suc = chargeToTile((int) target[0], (int) target[1], xfirst, true);
      // if success, adjust fine tune position and end approach
      if (suc) {
        isExploring = false;
        boolean isRed = (redTeam == 10);
        Point bin = (isRed) ? redBin : greenBin;
        Navigation.positionTo((int) bin.x, (int) bin.y);
        break;
      } else {
        // else, make sure there's an obstacle.
        centerCurrTile();
        // so there is an obstacle
        // record unsafe tile
        int[] pos = recordFrontTile();
        // if the unsafe tile is too close to the launch point, let it go
        if (Math.abs(pos[0] - target[0]) <= 0 && Math.abs(pos[1] - target[1]) <= 0) {
          System.out.println(
              "Discard target: " + target[0] + " " + target[1] + ", due to bad tile: " + pos[0] + " " + pos[1]);
          keeptarget = false;
          // Sound.beepSequenceUp();
          continue;
        }
        // try to find a way to get away with the obstacle.
        xfirst = rebase((int) target[0], (int) target[1]);
        if (planB == true) {
          return;
        }
      }
    }
  }

  /**
   * Checks if the robot is stuck with no way out in both the x and y direction. If confirmed, escape by going to the
   * opposite direction to the one you are currently in and reroute.
   * 
   * @param destx x position of destination in tile
   * @param desty y position of destination in tile
   * @param xfirst the initial xfirst statement
   * @return xfirst the updated xfirst statement
   */
  private static boolean checkGetStuck(int destx, int desty, boolean xfirst) {
    double[] position = odometer.getXYT();
    int currx = (int) (position[0] / TILE_SIZE);
    int curry = (int) (position[1] / TILE_SIZE);
    int dx, dy;
    if (currx == destx) {
      dx = 0;
    } else {
      dx = (destx > currx) ? 1 : -1;
    }
    if (curry == desty) {
      dy = 0;
    } else {
      dy = (desty > curry) ? 1 : -1;
    }
    int checkx = currx + dx;
    int checky = curry + dy;
    if ((!checkTile(checkx, curry)) && (!checkTile(currx, checky))) {
      if (checkTile(currx - dx, curry) && goToTile_s(currx - dx, curry, true)) {
        return false;
      } else {
        centerCurrTile();
        goToTile_s(currx, curry, false);
      }
      if (checkTile(currx, curry - dy) && goToTile_s(currx, curry - dy, true)) {
        return true;
      } else {
        centerCurrTile();
        goToTile_s(currx, curry, false);
      }
      return false;
    } else {
      return xfirst;
    }
  }

  /**
   * This method replaces phase 2 and 3 of the competition by launching right out of the tunnel. This will only be
   * called after several failed demos.
   * 
   */
  public static void planB() {
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Navigation.doPreLaunch();
    Navigation.doBallLaunch();
    Navigation.directTurn(180);
    boolean isRed = (redTeam == 10);
    centerCurrTile();
    travelTunnel();
    int corner = (isRed) ? redCorner : greenCorner;
    switch (corner) {
      case 0:
        chargeToTile(0, 0, (tunnel[3] == 0), false);
        break;
      case 1:
        chargeToTile(14, 0, (tunnel[3] == 0), false);
        break;
      case 2:
        chargeToTile(14, 8, (tunnel[3] == 0), false);
        break;
      case 3:
        chargeToTile(0, 8, (tunnel[3] == 0), false);
        break;
    }
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Sound.beep();

  }

  /**
   * This method allows the robot to decide how to move to avoid the obstacle it is dealing with.
   * 
   * @param destx target x in tile coordinates
   * @param desty target y in tile coordinates
   * @return true if the robot should move by row first false if otherwise
   */
  private static boolean rebase(int destx, int desty) {
    System.out.println("Rebasing to " + destx + " " + desty);
    // get the current heading and position of the robot
    int heading = getHeading();
    double[] position = odometer.getXYT();
    int currx = (int) (position[0] / TILE_SIZE);
    int curry = (int) (position[1] / TILE_SIZE);
    // if the robot is heading horizontally
    if ((heading == 0 || heading == 2)) {
      if (curry == desty) { // if the target is on the same row of the robot
        // decide which direction to pass the object
        int breakheading;
        if (curry == island.ll.y) {
          // if on bottom edge of island, go up
          breakheading = 1;
        } else if (curry == island.ur.y - 1) {
          // go down
          breakheading = 3;
        } else {
          // if in the middle, go to the closest edge
          breakheading = (curry > island.ll.y + (island.ur.y - island.ll.y) / 2) ? 3 : 1;
        }
        // tryToBreak, if failed at the decided direction, go to second direction.
        if (!tryToBreak(heading, breakheading)) {
          if (!tryToBreak(heading, 4 - breakheading)) {
            planB = true;
          }
        }
        return true;
      } else { // if the target is on other rows
        // just make the robot go different direction.
        return false;
      }

    } else { // case the robot is heading vertically
      if (currx == destx) {
        int breakheading;
        if (currx == island.ll.x) {
          breakheading = 0;
        } else if (currx == island.ur.x - 1) {
          breakheading = 2;
        } else {
          breakheading = (currx > island.ll.x + (island.ur.x - island.ll.x) / 2) ? 2 : 0;
        }
        if (!tryToBreak(heading, breakheading)) {
          if (!tryToBreak(heading, 2 - breakheading)) {
            planB = true;
          }
        }
        return false;
      } else {
        return true;
      }

    }

  }

  /**
   * Tries to pass the obstacle that lies between robot and the target point by considering different headings one after
   * the other.
   * 
   * @param heading the robot heading, direction of the target
   * @param breakheading the new direction that is chosen to move in, the robot will move along this direction and find
   *        gap.
   * @return true if the break process is successful false if it failed
   */
  private static boolean tryToBreak(int heading, int breakheading) {
    // get the start position.
    double[] position = odometer.getXYT();
    int startx = (int) (position[0] / TILE_SIZE);
    int starty = (int) (position[1] / TILE_SIZE);
    Navigation.turnTo(90 * breakheading);
    // make a current position record
    int currx = startx;
    int curry = starty;
    // decide dx and dy
    int dx, dy;
    if (breakheading == 0 || breakheading == 2) {
      dx = (breakheading == 0) ? 1 : -1;
      dy = 0;
    } else {
      dx = 0;
      dy = (breakheading == 1) ? 1 : -1;
    }
    // keep looping till the tile is invalid or find the gap
    isExploring = false;
    while (true) {
      // update move point
      currx += dx;
      curry += dy;
      // if the new move point is bad, end this attempt with fail
      if (!checkTile(currx, curry)) {
        // go back to the starting point
        chargeToTile(startx, starty, (heading == 3 || heading == 1), false);
        Navigation.turnTo(heading * 90);
        isExploring = true;
        return false;
      }
      // try to move to the point
      boolean suc = goToTile_s(currx, curry, true);
      if (suc) {
        // if movement is success, turn to wall and see if theres obstacle
        Navigation.turnTo(heading * 90);
        // scan the tile, if true, end attempt with success message
        boolean pass = scanTile();
        if (pass) {
          route.push(new Integer[] {currx, curry});
          isExploring = true;
          return true;
        }
        // if not, keep on
      } else { // if find obstacle in the movement
        // do a desperate check
        centerCurrTile();
        Navigation.turnTo(heading * 90);
        boolean pass = scanTile();
        // if pass, yeah!
        if (pass) {
          route.push(new Integer[] {currx, curry});
          isExploring = true;
          return true;
        } else {
          // if not, too bad
          chargeToTile(startx, starty, (heading == 3 || heading == 1), false);
          Navigation.turnTo(heading * 90);
          isExploring = true;
          return false;
        }
      }
    }
  }

  /**
   * Launches the ball by performing the necessary unfolding of the launching arm before throwing the ball.
   */
  private static void launchProcess() {
    Sound.beep();
    Sound.beep();
    Sound.beep();
    Navigation.doPreLaunch();
    Navigation.doBallLaunch();
  }

  /**
   * Records the front tile as unsafe once an obstacle is detected on it.
   * 
   * @return result, the array holding the coordinates of the tile being recorded
   */
  private static int[] recordFrontTile() {
    int heading = getHeading();
    double[] position = odometer.getXYT();
    int currx = (int) (position[0] / TILE_SIZE);
    int curry = (int) (position[1] / TILE_SIZE);
    switch (heading) {
      case 0:
        // right
        if (currx != 14)
          currx++;
        break;
      case 1:
        // up
        if (curry != 8)
          curry++;
        break;
      case 2:
        // left
        if (currx != 0)
          currx--;
        break;
      case 3:
        // down
        if (curry != 0)
          curry--;
        break;
      default:
        break;
    }
    System.out.println("Record Unsafe tile: " + currx + " " + curry);
    recordtile(currx, curry);
    int[] result = {currx, curry};
    return result;
  }

  /**
   * Navigates the robot to the specified tile tilewisely and using odometer correction.
   * 
   * @param x destination in tile coordinates
   * @param y destination in tile coordinates
   * @param rowfirst, true if the robot should approach the tile by row first
   * @param onGuard, true if the robot should perform obstacle avoidance while moving
   * @return suc, true if the movement to the specified tile was successful false if it was not
   */
  public static boolean chargeToTile(int destx, int desty, boolean rowfirst, boolean onGuard) {
    boolean suc = true;
    double[] position = odometer.getXYT();
    int currx = (int) (position[0] / TILE_SIZE);
    int curry = (int) (position[1] / TILE_SIZE);

    if (rowfirst) {
      suc = moveRow(destx, currx, curry, onGuard);
      if (suc) {
        suc = moveCol(desty, destx, curry, onGuard);
      }
    } else {
      suc = moveCol(desty, currx, curry, onGuard);
      if (suc) {
        suc = moveRow(destx, currx, desty, onGuard);
      }
    }
    return suc;
  }


  /**
   * Moves the robot starting with a column tile by tile.
   * 
   * @param desty destination y in tile coordinates
   * @param currx start x in tile coordinates
   * @param curry start y in tile coordinates
   * @param onGuard, true if the robot should perform obstacle avoidance while moving
   * @return suc, true if the movement to the specified tile was successful false if it was not
   */
  private static boolean moveCol(int desty, int currx, int curry, boolean onGuard) {
    boolean suc = true;
    if (desty > curry) {
      Navigation.turnTo(90);
      if (onGuard && curry < 7 && currx != 0 && currx != 14 && scanTile() == false) {
        return false;
      }
      for (int i = curry + 1; i <= desty; i++) {
        if (!goToTile_s(currx, i, onGuard)) {
          suc = false;
          break;
        }
      }
    } else if (desty < curry) {
      Navigation.turnTo(270);
      if (onGuard && curry > 1 && currx != 0 && currx != 14 && scanTile() == false) {
        return false;
      }
      for (int i = curry - 1; i >= desty; i--) {
        if (!goToTile_s(currx, i, onGuard)) {
          suc = false;
          break;
        }
      }
    } else {
      // do nothing
    }
    return suc;
  }

  /**
   * Moves the robot starting with a row tile by tile.
   * 
   * @param destx destination x in tile coordinates
   * @param currx start x in tile coordinates
   * @param curry start y in tile coordinates
   * @param onGuard, true if the robot should perform obstacle avoidance while moving
   * @return suc, true if the movement to the specified tile was successful false if it was not
   */
  private static boolean moveRow(int destx, int currx, int curry, boolean onGuard) {
    boolean suc = true;
    if (destx > currx) {
      Navigation.turnTo(0);
      if (onGuard && currx < 13 && curry != 0 && curry != 8 && scanTile() == false) {
        return false;
      }
      for (int i = currx + 1; i <= destx; i++) {
        if (!goToTile_s(i, curry, onGuard)) {
          suc = false;
          break;
        }
      }
    } else if (destx < currx) {
      Navigation.turnTo(180);
      if (onGuard && currx > 1 && curry != 0 && curry != 8 && scanTile() == false) {
        return false;
      }
      for (int i = currx - 1; i >= destx; i--) {
        if (!goToTile_s(i, curry, onGuard)) {
          suc = false;
          break;
        }
      }
    } else {
      // do nothing
    }
    return suc;
  }

  /**
   * Centers the robot at current tile.
   */
  public static void centerCurrTile() {
    isCentering = true;
    OdometerCorrector odoc_inside = new OdometerCorrector(-1, -1);
    Thread odoc = new Thread(odoc_inside);
    odoc.start();
    try {
      odoc.join();
    } catch (InterruptedException e) {

    }

    isCentering = false;
  }

  /**
   * Goes to the next tile with odometer correction, 1 tile per call.
   * 
   * @param x destination x in tile
   * @param y destination y in tile
   * @return true if the method ended without interruption by obstacle detection
   */
  public static boolean goToTile_s(int x, int y, boolean onGuard) {
    int heading = getHeading();
    // on edge case, disable the UsGuard
    if ((x <= 1 && heading == 2) || (x >= 13 && heading == 0) || 
        (y <= 1 && heading == 3) || (y >= 7  && heading == 1)) {
      onGuard = false;
    }
    double destx = (x + 0.5) * TILE_SIZE;
    double desty = (y + 0.5) * TILE_SIZE;
    if (Navigation.getDestDist(destx, desty) <= 3.0) {
      return true;
    }
    Navigation.turnTo(destx, desty);
    isTravelingStraight = true;
    Thread odoc = new Thread(new OdometerCorrector(destx, desty));
    Thread uspoller = new Thread(new USGuard());
    odoc.start();
    if (onGuard) {
      uspoller.start();
    }
    try {
      odoc.join();
    } catch (InterruptedException e) {

    }
    if (isTravelingStraight) {
      isTravelingStraight = false;
      if (onGuard) {
        try {
          uspoller.join();
        } catch (InterruptedException e) {
        }
      }
      if (isExploring) {
        route.push(new Integer[] {x, y});
      }
      return true;
    } else {
      return false;
    }

  }

  /**
   * Initializes all the safe tiles: safe red/green tiles from the robots' respective zones have a value of 1 and safe
   * island tiles have value of 2.
   */
  public static void initSafeTiles() {
    boolean isRed = (redTeam == 10);
    Region team = (isRed) ? red : green;
    // Team zone = 1
    for (int i = (int) team.ll.x; i < (int) team.ur.x; i++) {
      for (int j = (int) team.ll.y; j < (int) team.ur.y; j++) {
        TILES[i][j] = TEAM_MARK;
      }
    }
    // Island = 2
    for (int i = (int) island.ll.x; i < (int) island.ur.x; i++) {
      for (int j = (int) island.ll.y; j < (int) island.ur.y; j++) {
        TILES[i][j] = ISLAND_MARK;
      }
    }
  }

  /**
   * Gets the closest target tile to launch from.
   * 
   * @return [0] launch tile x coordinate [1] launch tile y coordinate [ [2] distance of launch in cm
   */
  private static double[] getTargetTile() {
    boolean isRed = (redTeam == 10);
    Point bin = (isRed) ? redBin : greenBin;
    int t_i = (int) bin.x;
    int t_j = (int) bin.y;
    double[] result = {-1, -1, Integer.MAX_VALUE};
    double offset = TILE_SIZE / 2;
    boolean found = false;
    while (!found) {
      for (int i = (int) island.ll.x; i < (int) island.ur.x; i++) {
        for (int j = (int) island.ll.y; j < (int) island.ur.y; j++) {
          double dist = Math.sqrt(Math.pow((t_i - 0.5 - i) * TILE_SIZE, 2) + Math.pow((t_j - 0.5 - j) * TILE_SIZE, 2));
          if (dist <= LAUNCH_DIST + offset && dist > LAUNCH_DIST - TILE_SIZE && checkTile(i, j)) {
            TILES[i][j] = 9;
            double routedist = Navigation.getDestDist((i + 0.5) * TILE_SIZE, (j + 0.5) * TILE_SIZE);
            if (routedist < result[2]) {
              result[0] = i;
              result[1] = j;
              result[2] = routedist;
              found = true;
            }

          }
        }
      }
      if (!found) {
        offset += 5;
        System.out.println("Dist of " + (LAUNCH_DIST + offset) + " is not found. Dist +5.");
        if (offset > 60) {
          result[0] = -1;
          result[1] = -1;
          result[2] = -1;
          return result;
        }
      }
    }
    System.out.println("Target tile get:" + result[0] + " " + result[1] + ",dist: " + result[2]);
    return result;
  }

  /**
   * This method checks the tunnel and finds out where to go.
   * 
   * @param true if checking for the valid tile from the team zone side false if from island
   * @return location x of tile in front of the tunnel location y of tile in front of the tunnel the length of the
   *         tunnel 0/1 if the tunnel is horizontal/vertical
   */
  private static int[] checkTunnel(boolean isforwarding) {
    boolean isRed = (redTeam == 10);
    Region tn = (isRed) ? tnr : tng;
    int tn_ur_x = (int) tn.ur.x;
    int tn_ur_y = (int) tn.ur.y;
    int tn_ll_x = (int) tn.ll.x;
    int tn_ll_y = (int) tn.ll.y;

    // surrounding tiles ||down|| ||left|| ||up|| ||right||
    int[] check_x = {tn_ll_x, tn_ll_x - 1, tn_ur_x - 1, tn_ur_x};
    int[] check_y = {tn_ll_y - 1, tn_ll_y, tn_ur_y, tn_ur_y - 1};

    int[] result = {-1, -1, -1, -1};
    int id = (isforwarding) ? TEAM_MARK : ISLAND_MARK;

    if ((tn_ur_x - tn_ll_x == 1) && (tn_ur_y - tn_ll_y == 1)) {

      // tunnel = 1*1,one out of four surrounding tiles should be 1
      result[2] = 1;
      for (int i = 0; i < 4; i++) {
        if (check_x[i] >= 0 && check_x[i] < 15 && check_y[i] >= 0 && check_y[i] < 9
            && TILES[check_x[i]][check_y[i]] == id) {
          // tile in team zone
          result[0] = check_x[i];
          result[1] = check_y[i];
          result[3] = (i == 0 || i == 2) ? 1 : 0;
        }
      }
    } else {

      // tunnel = 2*1,one out of two surrounding tiles in tunnel direction should be 1
      result[2] = 2;
      boolean isHorizontal = (tn_ur_x - tn_ll_x != 1);
      result[3] = (isHorizontal) ? 0 : 1;
      // horizontal direction, check left&right -> i = 1,3
      // vertical direction, check up&down -> i = 0,2
      for (int i = (isHorizontal) ? 1 : 0; i < 4; i += 2) {
        if (TILES[check_x[i]][check_y[i]] == id) {
          // tile in team zone

          result[0] = check_x[i];
          result[1] = check_y[i];
          System.out.println("found tunnel entrance:" + result[0] + " " + result[1]);
        }
      }
    }
    return result;
  }

  /**
   * Travels through the tunnel and performs light localization afterwards.
   * 
   * @param dist to travel
   */
  public static void travelTunnel() {
    // Navigation.directTurn(-3);
    Navigation.directDrive((tunnel[2] + 1) * TILE_SIZE);
    Navigation.directDrive(TILE_SIZE);
    centerCurrTile();
  }

  /**
   * Scans the current tile with the ultrasonic sensor to determine if it is safe (ie void of obstacles).
   * 
   * @return true if the tile is safe to proceed. false otherwise
   */
  private static boolean scanTile() {
    System.out.println("Scanning");
    isScanning = true;
    USGuard sc_inside = new USGuard();
    Thread sc = new Thread(sc_inside);
    sc.start();
    Navigation.directTurn(10);
    Navigation.directTurn(-20);
    Navigation.directTurn(10);

    if (isScanning != false) {
      isScanning = false;
      try {
        sc.join();
      } catch (InterruptedException e) {
      }
      return true;
    } else {
      return false;
    }

  }

  /**
   * Records a tile as unsafe (ie having an obstacle on it).
   * 
   * @param x destination in tile coordinates
   * @param y destination in tile coordinates
   */
  private static void recordtile(int x, int y) {
    unsafe_tile_x[listCounter] = x;
    unsafe_tile_y[listCounter] = y;
    listCounter = (listCounter + 1) % MEMORY_SIZE;
  }

  /**
   * Checks the tile to see if it is recorded unsafe.
   * 
   * @param x coordinate of the tile to check
   * @param y coordinate of the tile to check
   * @return true if the tile is recorded as safe false otherwise
   */
  private static boolean checkTile(int x, int y) {
    if (x < 0 || x > 14 || y < 0 || y > 8)
      return false;
    if (TILES[x][y] != ISLAND_MARK)
      return false;
    for (int i = 0; i < MEMORY_SIZE; i++) {
      if (unsafe_tile_x[i] == x && unsafe_tile_y[i] == y)
        return false;
    }
    return true;
  }

  /**
   * Get the heading of the robot
   * 
   * @return 0 if heading east (x+) 1 if heading north (y-) 2 if heading west (x-) 3 if heading south (y+)
   */
  public static int getHeading() {
    double headingAngle = odometer.getT();
    int result = ((((int) headingAngle / 45) + 1) % 8) / 2;
    return result;
  }

}


