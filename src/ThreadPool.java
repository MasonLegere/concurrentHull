import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
	private final PoolThread[] threads; 
	private final LinkedBlockingQueue<Task> q;
	private int numLattices; 
	private volatile boolean exit;
	private volatile boolean flag;
	
	public ThreadPool(int numThreads, int numLattices, LinkedBlockingQueue<Task> q) throws InterruptedException {
		
		this.numLattices = numLattices;
		this.exit = false;
		this.q = q;
		
		threads = new PoolThread[numThreads]; 
		q = new LinkedBlockingQueue<Task>();
		
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new PoolThread(); 
			threads[i].setName(i+"");
			threads[i].start();
		}
		
		for (int i = 0; i < numThreads; i++) {
			threads[i].join();
		}

		
	}
	
	public void runTask(Task task) {
		synchronized(q){
				System.out.println("Added something to a Queue: "+task); 
				System.out.println(q);
				q.add(task); 
				System.out.println(q);
				q.notify();			
		}
	}
	
	private class PoolThread extends Thread {
		Task task;
		boolean flag = true;
		
		@Override
		public void run() {
			while (!exit){
				task = null;
				synchronized (q) {
					System.out.println(q.size());
						while(!exit && q.isEmpty()) {
							flag = false;
							try {
								System.out.println("waiting 1:"+ this.toString());
								System.out.println(numLattices);
								System.out.println(q.size());
								q.wait();
							} catch (InterruptedException e) {
								System.out.println(e.getMessage());
							}
							System.out.println("No longer waiting:" + this.toString());
						}
						
						
						if (q.peek().isHull()) {
//							if (flag) {
								while (!exit && q.size() < 2) {
									try {
										
										System.out.println("waiting 2 :"+ this.toString());
										System.out.println(numLattices);
										System.out.println(q.size());
										q.wait();
									} catch (InterruptedException e) {
										System.out.println(e.getMessage());
									}
								}
								
								if (!exit) {
									task = q.poll().combineTasks(q.poll());
									numLattices--;
								}
//							} else {
//								
//							}
						} 
						else {
							task = q.poll(); 
							task.setPool(getPool());				
						}										
						
					}		
				
							try {
								if (task != null) {
									task.run(); 
								}	
							}
							catch (RuntimeException e) {
								System.out.println(e.getMessage());
							}
						
				
					if (numLattices == 1) {
						exit = true;
						synchronized (q) {
							q.notifyAll();
						}

					}
					
		}
				
			
		}
		
		
		public ThreadPool getPool() {
			return ThreadPool.this;
		}
	}
	
	public IntegerLattice getResult() {
		
		for (int i = 0; i < threads.length; i++) {
			if (threads[i].isAlive()) {
				System.out.println("Something is wrong");
				System.exit(0);
			}
		}
		
		return q.poll().getLattice();
	}
}
