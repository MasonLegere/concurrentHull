

/** 
 *  Task represents the two fundamental operations within the algorithm. 
 *  Namely, finding the convex hull of a set of points 
 *  and merging two convex hulls
 * */
public class Task implements Runnable {

  private final IntegerLattice left, right;
  private ThreadPool pool;
  private boolean isHull;

  public Task(IntegerLattice left) {
    this.left = left;
    this.right = null;
    this.isHull = false;
  }

  private Task(IntegerLattice left, ThreadPool pool, boolean isHull) {
    this.left = left;
    this.right = null;
    this.pool = pool;
    this.isHull = isHull;
  }

  private Task(IntegerLattice left, IntegerLattice right, ThreadPool pool) {
    this.left = left;
    this.right = right;
    this.pool = pool;
    this.isHull = true;
  }

  /** 
   *  Performs the computations depending on whether the input
   *  is a convex hull. Adds the result 
   * */
  @Override
  public void run() {

    if (isHull) {
      pool.runTask(new Task(ConvexHull.combineHull(left, right), pool, true));

    } else {
      pool.runTask(new Task(ConvexHull.convexHull(left), pool, true));
    }

  }

  public void setPool(ThreadPool pool) {
    this.pool = pool;
  }

  public IntegerLattice getLattice() {
    return this.left;
  }

  public IntegerLattice getLatticeR() {
    return this.right;
  }

  /** 
   *  Creates a new task that represents the merging of two hulls
   *  
   *  @param task to be set at the right lattice 
   *  @return task representing the operation of merging the two hulls
   * */ 
  public Task combineTasks(Task t) {
    return new Task(this.getLattice(), t.getLattice(), this.pool);
  }

  public boolean isHull() {
    return this.isHull;
  }
}
