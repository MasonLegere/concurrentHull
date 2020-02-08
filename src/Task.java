
public class Task implements Runnable {
	
	private final IntegerLattice left, right;
	private final ThreadPool pool;
	
	public Task(IntegerLattice left, ThreadPool pool) {
		this.left = left; 
		this.right = null;
		this.pool = pool;
	}
	
	private Task(IntegerLattice left, IntegerLattice right, ThreadPool pool) {
		this.left = left; 
		this.right = right;
		this.pool = pool;
	}
	
	
	@Override
	public void run() {
		// Java is strict so this is thread safe
		if (right == null) {
			pool.runTask(new Task(ConvexHull.combineHull(left, right), pool));
	
		} else {
			pool.runTask(new Task(ConvexHull.convexHull(left),pool));
		}
		
		
	}
	
	public IntegerLattice getLattice() {
		return this.left;
	}
	
	public Task combineTasks(Task t) {
		int X = this.left.getLeftPoint(); 
		
		if (X < t.getLattice().getLeftPoint()) {
			return new Task(this.getLattice(), t.getLattice(), this.pool);
		}
		else {
			return new Task(t.getLattice(), this.getLattice(), this.pool); 
		}
	}
}
