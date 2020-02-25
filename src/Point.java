
/**
 * This class simply represents a lattice points under the Euclidean basis of R^2. 
 * In other words a data type for the points (x,y).
 * */

public class Point {

  private int x;
  private int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(String x, String y) {
    this.x = Integer.parseInt(x);
    this.y = Integer.parseInt(y);
  }

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public void setPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }
sychronized {
  
}
  @Override
  public String toString() {
    return "(" + this.x + "," + this.y + ")";
  }

}
