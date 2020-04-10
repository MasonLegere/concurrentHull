

/** 
 *  Task represents the two fundamental operations within the algorithm. 
 *  Namely, finding the convex hull of a set of points 
 *  and merging two convex hulls
 * */
public abstract class Task implements Runnable{

  protected IntegerLattice leftLattice; 
  protected ThreadPool pool; 
  protected boolean isHull;


  
  public Task(IntegerLattice leftLattice, ThreadPool pool, boolean isHull) {
      this.leftLattice = leftLattice;
      this.pool = pool;
      this.isHull = isHull;
  }
  
  public IntegerLattice getLattice() {
    return this.leftLattice;
  }
  
  public boolean isHull() {
    return this.isHull;
  }

  public abstract void run();

}
