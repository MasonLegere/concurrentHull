import java.util.ArrayList;

public class Task implements Runnable {
	
	ArrayList<Point> groupOne;
	ThreadPool pool;
	
	public Task(ArrayList<Point> groupOne, ThreadPool pool) {
		this.groupOne = groupOne; 
		this.pool = pool;
	}
	
	public void run() {
		// Java is strict so this is thread safe
		pool.runTask(new Task(ConvexHull.convexHull(groupOne),pool));
	}
}
