
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfree.data.xy.XYSeries;

/** 
 * This class consists exclusively of static methods that operate IntegerLattices or 
 * are helper functions used for the development of the initial data. It functional 
 * objective is to find the convex hull of either user specified points or an arbitary
 * amount of user specified points. This is done through first sorting the list of 
 * lattice points with respect to the X-Coordinate and dividing the list into sublists. 
 * 
 * The convex hull of each sublist is found separately using the Jarvis March algorithm. 
 * These disjoint lattice sets are then merged together in order to create the total convex
 * hull. 
 * */

public class ConvexHull {

  public static void main(String[] args) throws IOException {

    /* 
     * line  
     *  - buffer used for user input 
     * numThreads[i]    
     *  - number of threads to use for ith  convex hull 
     *    if the program is ran as a simulation and not from user input. 
     * pool   
     *  - the thread pool used for synchronization  
     * numPoints 
     *  - number of points to be created for the simulation 
     * numThread 
     *  - number of threads to be used for provided user data
     *   
     * */
    
    String line;      
    int[] numThreads;   
    int numPoints, numThread;
    ThreadPool pool = null;
    Scanner scanner = new Scanner(System.in);

    System.out
        .println(" > Would you like to find the convex hull of points specified in a file (Y/N)");
    line = scanner.nextLine().toUpperCase();

    if (line.equals("Y")) {

      System.out.println(" > Please enter the relative path of the file");

      line = scanner.nextLine();
      ArrayList<Point> points = readFile(line);

      System.out
          .println(" > Enter a positive integer to specifiy the number of threads to be used.");
      line = scanner.nextLine();
      numThread = Integer.parseInt(line);

      pool = new ThreadPool(numThread, 2 * numThread);
      pool.initPool(partitionLattice(points, 2 * numThread, pool));
      
      points = pool.getResult().getLattice();

      for (int i = 0; i < points.size(); i++) {
        System.out.println(points.get(i));
      }


    }

    else if (line.equals("N")) {

      System.out.println(" > Enter a postive integer to specify number of points");
      line = scanner.nextLine();
      numPoints = Integer.parseInt(line);

      System.out.println(
          " > Enter a comma delimited list of postive integers to specifiy the number of threads to be used.");
      line = scanner.nextLine();
      numThreads = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
      scanner.close();
      XYSeries times = runSimulation(numPoints, numThreads);
      LinePlotDisplay plot = new LinePlotDisplay(times);
      plot.setVisible(true);

    } else {

      System.out.println(" > Inavlid input - program terminating");
      System.exit(0);

    }
// test commit

  }

  /**
   * 
   *  @param numPoints  number of lattice points to be created
   *  @param numThreads number of threads to use for each calculation 
   *  
   * */
  private static XYSeries runSimulation(int numPoints, int[] numThreads) {

    XYSeries times = new XYSeries("Running Times");
    long initialTime, finalTime, timeElapsed;
    ThreadPool pool = null;

    ArrayList<Integer> pointsX = new ArrayList<Integer>();
    ArrayList<Integer> pointsY = new ArrayList<Integer>();
    ArrayList<Point> points = new ArrayList<Point>();

    for (int i = 0; i < numPoints; i++) {
      pointsX.add(new Integer(i));
    }

    for (int i = 0; i < numPoints; i++) {
      pointsY.add(new Integer(i));
    }

    Collections.shuffle(pointsX);
    Collections.shuffle(pointsX);

    for (int i = 0; i < numPoints; i++) {
      points.add(new Point(pointsX.get(i), pointsY.get(i)));
    }

    for (int i : numThreads) {
      initialTime = System.currentTimeMillis();

      pool = new ThreadPool(i, 2 * i);
      pool.initPool(partitionLattice(points, 2 * i, pool));

      pool.getResult().getLattice();
      finalTime = System.currentTimeMillis();
      timeElapsed = finalTime - initialTime;
      System.out.println("Number of Threads: " + i + "  ---- Time Taken: " + timeElapsed + " (ms)");
      times.add(i, timeElapsed);
    }

    return times;
  }

  /**
   *    <p>
   *    Takes in an initial arbitrary set of lattice points and separtes it 
   *    into a specified number of disjoint sublist such that they are compatible for 
   *    a DoC approach for convex hull implementation. 
   *    </p>
   *    
   *    @param points The inital lattice points to be partitioned 
   *    @param numPartitions the number of subLattices to split the original lattice
   *            into.
   *    @return The queue containing tasks, where each task contains a sublattice.
   *    @see Task
   *    @see IntegerLattice
   * 
   * */
  private static LinkedBlockingQueue<Task> partitionLattice(ArrayList<Point> points,
      int numPartitions, ThreadPool pool) {
    
    int intervalSize = points.size() / numPartitions;
    IntegerLattice tempLattice;
    ArrayList<Point> tempList;
    int lowerBound = 0, upperBound = 0;
    LinkedBlockingQueue<Task> q = new LinkedBlockingQueue<Task>();

    /* The convex hull of a set of less than four points is not defined.
     * This is not a *good* code design but it works.
     * */
    if (intervalSize < 3) {
      System.out.println("Not possible to find the convex hull using the required specifications");
      System.exit(0);
    }

    points.sort(Comparator.comparing(Point::getX)); // sort array list by X-values

    // Break the array into subarrays of points 
    // May will have minimum MLE for mean of interval sizes.
    for (int i = 0; i < numPartitions - 1; i++) {
      lowerBound = i * intervalSize;
      upperBound = (i + 1) * intervalSize;
      tempList = new ArrayList<Point>(points.subList(lowerBound, upperBound));
      tempLattice = new IntegerLattice(points.get(lowerBound).getX(),
          points.get(upperBound - 1).getX(), tempList);
      q.add(new ConvexTask(tempLattice, pool, false)); // add new task to the queue
     
    }

    tempList = new ArrayList<Point>(points.subList(upperBound, points.size()));
    tempLattice = new IntegerLattice(points.get(upperBound).getX(),
        points.get(points.size() - 1).getX(), tempList);
    q.add(new ConvexTask(tempLattice, pool, false));

    return q;

  }

  /**
   *    <p>
   *    Reads in a file specified by the relative fileName to get
   *    user specified points. Assumes that the input of the file 
   *    is of the form: 
   *        x_1 y_1 \n 
   *        x_2 y_2 \n 
   *        .
   *        .
   *        .
   *        x_n y_n
   *    </p>
   *    
   *    @param fileName relative path of the file 
   *    @return List of (x,y) points read from the file
   *    @throws IOException in the case where there is difficulty opening
   *            the file
   * */
  private static ArrayList<Point> readFile(String fileName) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    ArrayList<Point> points = new ArrayList<Point>();

    String[] arr;

    String input = reader.readLine();

    while (input != null) {
      arr = input.split(" ");
      points.add(new Point(arr[0], arr[1]));
      input = reader.readLine();
    }

    reader.close();
    return points;
  }

  
  /** 
   *    <p> This function takes two convex hulls and combines them together. 
   *        For algorithm description see:
   *        <a href="https://www.geeksforgeeks.org/convex-hull-using-divide-and-conquer-algorithm/">Merge Algorithm</a>       
   *    </p>
   *    
   *    @param leftLattice Set of integer lattice points representing previously found convex hull 
   *                       such that the x-coordinate of each point is less than each x-coordinate 
   *                       in rightLattice.
   *    @param rightLattice Set of integer lattice points representing previously found convex hull 
   *    @return The combined integer lattice that represents the convex hull of the original combined
   *            points
   * */
  public static IntegerLattice combineHull(IntegerLattice leftLattice,
      IntegerLattice rightLattice) {


    ArrayList<Point> left = leftLattice.getLattice();
    ArrayList<Point> right = rightLattice.getLattice();

    if (leftLattice.getRightPoint() > rightLattice.getLeftPoint()) {
      right = leftLattice.getLattice();
      left = rightLattice.getLattice();
    }

    int leftMost = 0, rightMost = 0;
    int leftTp, rightTp;
    int upperLeft, lowerLeft;
    int upperRight, lowerRight;
    boolean flag = false;
    ArrayList<Point> mergedHull = new ArrayList<Point>();

    // Move through all left points in the hull and select the left most pivot element
    for (int i = 1; i < left.size(); i++) {
      if (left.get(i).getX() > left.get(rightMost).getX()) {
        rightMost = i;
      }
    }

    for (int i = 1; i < right.size(); i++) {
      if (right.get(i).getX() < right.get(leftMost).getX()) {
        leftMost = i;
      }
    }

    leftTp = leftMost;
    rightTp = rightMost;

    // Move through all the elements in the right hull and find the right most pivot element
    while (!flag) {
      flag = true;

      while (orient(right.get(leftTp), left.get(rightTp),
          left.get((rightTp + 1) % left.size())) >= 0) {
        rightTp = (rightTp + 1) % left.size();
      }

      while (orient(left.get(rightTp), right.get(leftTp),
          right.get((leftTp - 1 + right.size()) % right.size())) <= 0) {
        leftTp = (right.size() + leftTp - 1) % right.size();
        flag = false;
      }
    }

    upperLeft = rightTp;
    upperRight = leftTp;
    leftTp = leftMost;
    rightTp = rightMost;
    flag = false;

    // Using the pivots, select all elements that do not align in the pivot
    while (!flag) {

      flag = true;

      while (orient(left.get(rightTp), right.get(leftTp),
          right.get((leftTp + 1) % right.size())) >= 0) {
        leftTp = (leftTp + 1) % right.size();
      }

      while (orient(right.get(leftTp), left.get(rightTp),
          left.get((left.size() + rightTp - 1) % left.size())) <= 0) {
        rightTp = (left.size() + rightTp - 1) % left.size();
        flag = false;
      }
    }

    lowerLeft = rightTp;
    lowerRight = leftTp;

    mergedHull.add(left.get(upperLeft));
    leftTp = upperLeft;

    while (leftTp != lowerLeft) {
      leftTp = (leftTp + 1) % left.size();
      mergedHull.add(left.get(leftTp));
    }

    leftTp = lowerRight;
    mergedHull.add(right.get(lowerRight));

    while (leftTp != upperRight) {
      leftTp = (leftTp + 1) % right.size();
      mergedHull.add(right.get(leftTp));
    }

    return new IntegerLattice(leftLattice.getLeftPoint(), rightLattice.getRightPoint(), mergedHull);

  }

  /**
   *    Find the convex hull of a set of 2 dimensional points. Assumes
   *    that more than 3 points are provided to the algorithm. If less 
   *    than three points are specified the program will terminate. 
   *    
   *    Jarvis's algorithm is used to find the convex hull in O(nh) time complexity 
   *    where n is the number of points in the lattice and h is the number of points 
   *    in the resultant convex hull. That is, the runtime is a function of the output size. 
   *    
   *    @param lattice The integer lattice for which we want to find the convex hull
   *    @return The convex hull of the provided lattice 
   * 
   * */
  public static IntegerLattice convexHull(IntegerLattice lattice) {
    

    ArrayList<Point> points = lattice.getLattice();
    if (points.size() < 3)
      return null;

    ArrayList<Point> hull = new ArrayList<Point>();

    int l = 0;
    for (int i = 1; i < points.size(); i++)
      if (points.get(i).getX() < points.get(l).getX())
        l = i;

    // Selecting left pivot shuffle selecting "optimal" outside points 
    // those is, points that minimal orientations with pivots 
    int p = l, q;
    do {
      hull.add(points.get(p));
      q = (p + 1) % points.size();
      for (int i = 0; i < points.size(); i++) {

        if (orient(points.get(p), points.get(i), points.get(q)) < 0)

          q = i;
      }

      p = q;

    } while (p != l);

    return new IntegerLattice(lattice.getLeftPoint(), lattice.getRightPoint(), hull);
  }

 /** 
  *  Provide the orientation of three points in space from point p 
  *  to point r pivoting around q. 
  *  
  *  @param p Point (x,y) in space 
  *  @param q Point (x,y) in space 
  *  @param r Point (x,y) in space 
  *  @return integer value representing the resulting orientation in space. 
  * */
  // Really the cross product in R^2
  public static int orient(Point p, Point q, Point r) {
    return (q.getY() - p.getY()) * (r.getX() - q.getX())
        - (q.getX() - p.getX()) * (r.getY() - q.getY());
  }



}
