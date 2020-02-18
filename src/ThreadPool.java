import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
	private final PoolThread[] threads;
	private final LinkedBlockingQueue<Task> q;
	private int numLattices;
	private volatile boolean exit;

	public ThreadPool(int numThreads, int numLattices, LinkedBlockingQueue<Task> q) throws InterruptedException {

		this.numLattices = numLattices;
		this.exit = false;
		this.q = q;

		threads = new PoolThread[numThreads];
		q = new LinkedBlockingQueue<Task>();

		for (int i = 0; i < numThreads; i++) {
			threads[i] = new PoolThread();
			threads[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			threads[i].join();
		}

	}

	public void runTask(Task task) {
		synchronized (q) {
			q.add(task);
			q.notify();
		}
	}

	private class PoolThread extends Thread {
		Task task;
		boolean flag = true;

		@Override
		public void run() {
			while (!exit) {
				task = null;
				synchronized (q) {
					System.out.println(q.size());
					while (!exit && q.isEmpty()) {
						flag = false;
						try {
							q.wait();
						} catch (InterruptedException e) {
							System.out.println(e.getMessage());
						}

					}

					if (q.peek().isHull()) {
						while (!exit && q.size() < 2) {
							try {
								q.wait();
							} catch (InterruptedException e) {
								System.out.println(e.getMessage());
							}
						}

						if (!exit) {
							task = q.poll().combineTasks(q.poll());
							numLattices--;
						}
					} else {
						task = q.poll();
						task.setPool(getPool());
					}

				}

				try {
					if (task != null) {
						task.run();
					}
				} catch (RuntimeException e) {
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

		return q.poll().getLattice();
	}
}
