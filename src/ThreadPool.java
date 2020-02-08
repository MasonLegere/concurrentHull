import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
	private final int numThreads; 
	private final PoolThread[] threads; 
	private final LinkedBlockingQueue q;
	
	public ThreadPool(int numThreads) {
		this.numThreads = numThreads; 
		threads = new PoolThread[numThreads]; 
		q = new LinkedBlockingQueue();
		
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new PoolThread(); 
			threads[i].start();
		}
	}
	
	public void runTask(Runnable task) {
		synchronized(q){
			if (!q.isEmpty()) {
				q.add(task); 
				q.notify();
			}
		}
	}
	
	private class PoolThread extends Thread {
		Runnable task;
		public void run() {
			while(true) {
				synchronized (q) {
					while(q.isEmpty()) {
						try {
							q.wait();
						} catch (InterruptedException e) {
							System.out.println(e.getMessage());
						}
					}
					
					task = (Runnable) q.poll();
					
				}
				
				try {
					task.run(); 
				}
				catch (RuntimeException e) {
					System.out.println(e.getMessage());
				}
				
			}
		}
	}
}
