
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
	
	
	@Override
	public void run() { 
		
		if (isHull) {
			pool.runTask(new Task(ConvexHull.combineHull(left, right), pool, true));
	
		} else {
			pool.runTask(new Task(ConvexHull.convexHull(left),pool, true));
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
	
	public Task combineTasks(Task t) {
		int X = this.left.getRightPoint(); 
		
		// Differentiate which is the left and right lattice 
		// Note that (t.getLattice().getLeftPoint() == X) == false for all lattices
		if (X < t.getLattice().getLeftPoint()) {
			return new Task(this.getLattice(), t.getLattice(), this.pool);
		}
		else {
			return new Task(t.getLattice(), this.getLattice(), this.pool); 
		}
	}
	
	public boolean isHull() {
		return this.isHull;
	}
}
