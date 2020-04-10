import java.util.ArrayList;

/**
 * Simple class that has the additional functionality of storing the leftmost point and rightmost
 * point in the array.
 */

public class IntegerLattice {

  /**
   * list of points (x,y) that make up the integer lattice
   */
  private ArrayList<Point> points;
  /**
   * the point with the smallest x value
   */
  private int leftMostPoint;
  /**
   * the point with the largest x value
   */
  private int rightMostPoint;

  public IntegerLattice(int leftMostPoint, int rightMostPoint, ArrayList<Point> points) {
    this.leftMostPoint = leftMostPoint;
    this.rightMostPoint = rightMostPoint;
    this.points = points;
  }

  public ArrayList<Point> getLattice() {
    return this.points;
  }

  public int getLeftPoint() {
    return leftMostPoint;
  }

  public int getRightPoint() {
    return rightMostPoint;
  }

  public void setLeftPoint(int i) {
    this.leftMostPoint = i;
  }

  public void setRightPoint(int i) {
    this.rightMostPoint = i;
  }

}
