import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
	private final PoolThread[] threads; 
	private final LinkedBlockingQueue<Task> q;
	private int numLattices; 
	
	public ThreadPool(int numThreads, int numLattices) throws InterruptedException {
		threads = new PoolThread[numThreads]; 
		q = new LinkedBlockingQueue<Task>();
		this.numLattices = numLattices;
		
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new PoolThread(); 
			threads[i].start();
		}
		
		for (int i = 0; i < numThreads; i++) {
			threads[i].join();
		}
	}
	
	public void runTask(Task task) {
		synchronized(q){			
				q.add(task); 
				q.notify();			
		}
	}
	
	private class PoolThread extends Thread {
		Runnable task;
		
		@Override
		public void run() {
			while(numLattices > 1) {
				synchronized (q) {
					while(q.size() < 2) {
						try {
							q.wait();
						} catch (InterruptedException e) {
							System.out.println(e.getMessage());
						}
					}
					
					// Could be a better solution using take()
					task = q.poll().combineTasks(q.poll());
					// Each time two sets are combined the total number of lattice points decreases yuj
					numLattices--; 
					
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
