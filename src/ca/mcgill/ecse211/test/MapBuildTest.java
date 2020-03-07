package ca.mcgill.ecse211.test;

/**
 * This class tests the building of a correct map of the playing field. 
 * The purpose of this class was to be able to generate maps locally in case the wifi class was down. 
 * 
 * @author Han
 *
 */
public class MapBuildTest {

  /**
   * 2D array to store all the tiles ( safe red/green tiles have value of 1, safe island tiles have value of 2)
   */
  public static int map_width = 9;
  public static int[][] TILES = new int[map_width][9];
  public static int[] tunnel = new int[4];

  // creating game parameter variables
  public static int redTeam, redCorner, greenTeam, greenCorner, targetAngle;
  public static Region red, green, island, tnr, tng;
  public static Point bin;
  public static double[] pos;

  public static final double TILE_SIZE = 30.48;
  public static final double LAUNCH_DIST = 135.0;

  public static void main(String[] args) {
    initMapTest();
  }


  private static void getTunnelTest() {
    System.out.print("Tunnel Test starts.\n");
    int[] pos = checkTunnel(true);

    System.out.print("\n" + pos[0] + " " + pos[1] + " " + pos[2] + " " + pos[3] + "\nTunnel test ends.\n");

  }

  private static void initparams1() {
    greenTeam = 10;
    greenCorner = 0;
    green = new Region(0, 0, 3, 3);
    tng = new Region(3, 2, 5, 3);
    tnr = new Region(3, 2, 5, 3);
    bin = new Point(7, 4);
    targetAngle = 90;
    island = new Region(5, 1, 9, 9);
  }

  private static void initparams0() {
    greenTeam = 22;
    redTeam = 10;
    red = new Region(0, 0, 3, 4);
    tnr = new Region(3, 0, 5, 1);
    island = new Region(5, 0, 8, 6);
  }

  private static void initparams2() {
    greenTeam = 10;
    greenCorner = 0;
    green = new Region(0, 0, 3, 3);
    tng = new Region(2, 3, 3, 5);
    tnr = new Region(2, 3, 3, 5);
    bin = new Point(4, 7);
    targetAngle = 90;
    island = new Region(0, 5, 9, 9);
  }

  public static void initMapTest() {
    System.out.print("\ninit Map Test starts.\n");
    initparams1();
    initSafeTiles();
    prtMap();
    System.out.print("\ninit Map Test ends.\n");
  }

  public static void getTargetTileTest() {
    System.out.print("Target Tile Test starts.\n");
    redTeam = 10;
    red = new Region(0, 0, 3, 4);
    tnr = new Region(3, 0, 5, 1);
    pos = new double[] {TILE_SIZE * 1.5, TILE_SIZE * 1.5, 90.0};
    island = new Region(0, 0, 9, 9);
    bin = new Point(-2, 4);

    initSafeTiles();
    prtMap();
    int[] tar = getTargetTile();
    prtMap();
    System.out.print("\n" + tar[0] + " " + tar[1] + "\n");
    System.out.print("Target Tile Test ends.\n");
  }

  private static void prtMap() {
    System.out.print("\n");
    for (int y = 8; y >= 0; y--) {
      System.out.print(y / 10 + "" + y % 10 + " ||");
      for (int x = 0; x < map_width; x++) {
        System.out.print(TILES[x][y] + " ");
      }
      System.out.print('\n');
    }
    System.out.print("    ");
    for (int x = 0; x < map_width; x++) {
      System.out.print("==");
    }
    System.out.print("\n     ");
    for (int x = 0; x < map_width; x++) {
      System.out.print(x / 10 + " ");
    }

    System.out.print("\n     ");
    for (int x = 0; x < map_width; x++) {
      System.out.print(x % 10 + " ");
    }
  }

  /**
   * Computes the closest target tile to launch from.
   * 
   * @return [0] launch tile x coordinate 
   *         [1] launch tile y coordinate
   *         [2] distance of launch
   */
  private static int[] getTargetTile() {
    int t_i = (int) bin.x;
    int t_j = (int) bin.y;
    int[] result = {-1, -1, Integer.MAX_VALUE};
    double offset = TILE_SIZE / 2;
    boolean found = false;
    while (!found) {
      for (int i = (int) island.ll.x; i < (int) island.ur.x; i++) {
        for (int j = (int) island.ll.y; j < (int) island.ur.y; j++) {
          double dist = Math.sqrt(Math.pow((t_i - 0.5 - i) * TILE_SIZE, 2) + Math.pow((t_j - 0.5 - j) * TILE_SIZE, 2));
          if (dist <= LAUNCH_DIST + offset) {
            TILES[i][j] = 9;
            if ((int) dist < result[2]) {
              result[0] = i;
              result[1] = j;
              result[2] = (int) getDestDist((i + 0.5) * TILE_SIZE, (j + 0.5) * TILE_SIZE);
              found = true;
            }
          }
        }
      }
      if (!found) {
        offset += 5;
      }
    }
    return result;
  }

  /**
   * Calculates the distance left to travel to get to the specified destination.
   * 
   * @param x coordinate of destination
   * @param y coordinate of destination
   * @return distance left to travel
   */
  public static double getDestDist(double x, double y) {
    double dist = Math.sqrt(Math.pow(Math.abs(pos[0] - x), 2) + Math.pow(Math.abs(pos[1] - y), 2));
    return dist;
  }

  /**
   * Checks the tunnel and finds out where to go.
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
    for (int i = 0; i < 4; i++) {
      System.out.print("\n" + check_x[i] + " " + check_y[i]);
    }
    int[] result = {-1, -1, -1, -1};
    int id = (isforwarding) ? 1 : 2;

    if ((tn_ur_x - tn_ll_x == 1) && (tn_ur_y - tn_ll_y == 1)) {

      // tunnel = 1*1,one out of four surrounding tiles should be 1
      result[2] = 1;
      for (int i = 0; i < 4; i++) {
        if (TILES[check_x[i]][check_y[i]] == id) {
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
        System.out.print("\nx:" + check_x[i] + " y:" + check_y[i] + " val:" + TILES[check_x[i]][check_y[i]]);
        if (TILES[check_x[i]][check_y[i]] == id) {
          // tile in team zone
          result[0] = check_x[i];
          result[1] = check_y[i];
        }
      }

    }
    return result;
  }

  /**
   * Initializes all the safe tiles. Safe red/green tiles have a value of 1. Safe island tiles have a value
   * of 2.
   */
  public static void initSafeTiles() {
    boolean isRed = (redTeam == 10);
    Region team = (isRed) ? red : green;
    Region tn = (isRed) ? tnr : tng;
    // Team zone = 1
    for (int i = (int) team.ll.x; i < (int) team.ur.x; i++) {
      for (int j = (int) team.ll.y; j < (int) team.ur.y; j++) {
        TILES[i][j] = 1;
      }
    }
    // Island = 2
    for (int i = (int) island.ll.x; i < (int) island.ur.x; i++) {
      for (int j = (int) island.ll.y; j < (int) island.ur.y; j++) {
        TILES[i][j] = 2;
      }
    }

    // Tunnel = 3
    for (int i = (int) tn.ll.x; i < (int) tn.ur.x; i++) {
      for (int j = (int) tn.ll.y; j < (int) tn.ur.y; j++) {
        TILES[i][j] = 3;
      }
    }

    // bin = 4 for demo
    TILES[(int) bin.x][(int) bin.y] = 4;

  }

  /**
   * Represents a region on the competition map grid, delimited by its lower-left and upper-right corners (inclusive).
   * 
   * @author Younes Boubekeur
   */
  public static class Region {

    /** The lower left corner of the region. */
    public Point ll;

    /** The upper right corner of the region. */
    public Point ur;

    /**
     * Constructs a Region.
     * 
     * @param lowerLeft the lower left corner of the region
     * @param upperRight the upper right corner of the region
     */
    public Region(Point lowerLeft, Point upperRight) {
      validateCoordinates(lowerLeft, upperRight);
      ll = lowerLeft;
      ur = upperRight;
    }

    public Region(double ll_x, double ll_y, double ur_x, double ur_y) {
      Point ll = new Point(ll_x, ll_y);
      Point ur = new Point(ur_x, ur_y);
      validateCoordinates(ll, ur);
      this.ll = ll;
      this.ur = ur;
    }

    /**
     * Validates coordinates.
     * 
     * @param lowerLeft the lower left corner of the region
     * @param upperRight the upper right corner of the region
     */
    private void validateCoordinates(Point lowerLeft, Point upperRight) {
      if (lowerLeft.x > upperRight.x || lowerLeft.y > upperRight.y) {
        throw new IllegalArgumentException("Upper right cannot be below or to the left of lower left!");
      }
    }

    public String toString() {
      return "[" + ll + ", " + ur + "]";
    }
  }

  /**
   * Represents a coordinate point on the competition map grid.
   * 
   * @author Younes Boubekeur
   */
  public static class Point {
    /** The x coordinate. */
    public double x;

    /** The y coordinate. */
    public double y;

    /**
     * Constructs a Point.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Point(double x, double y) {
      this.x = x;
      this.y = y;
    }

    public String toString() {
      return "(" + x + ", " + y + ")";
    }

  }



}
