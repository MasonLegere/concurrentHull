import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is a thread pool acting as an environment for the the management of tasks. This
 * management involves synchonronization for the addition of new tasks to the queue as well as
 * removing tasks from the queue. Removal of tasks from the queue is nontrivial as in the case where
 * we need to merge to convex hulls.
 */

public class ThreadPool {
  /**
   * threads that are avaliable for the queue to notify in order to complete tasks
   * 
   */
  private final PoolThread[] threads;

  /**
   * Tasks added and removed from the queue in a synchronized manner
   */
  private final LinkedBlockingQueue<Task> q;

  /**
   * Number of lattices initially on the queue during initialization
   */
  private int numLattices;
  /**
   * Flag to represent if there is any work let to do.
   */
  private volatile boolean exit;

  public ThreadPool(int numThreads, int numLattices, LinkedBlockingQueue<Task> q)
      throws InterruptedException {

    this.numLattices = numLattices;
    this.exit = false;
    this.q = q;

    threads = new PoolThread[numThreads];

    // start and join all threads.
    for (int i = 0; i < numThreads; i++) {
      threads[i] = new PoolThread();
      threads[i].start();
    }

    for (int i = 0; i < numThreads; i++) {
      threads[i].join();
    }

  }

  /**
   * Adds the task provided to the queue and notifys a thread that a new task has been added to the
   * queue.
   * 
   * @param task The task to be added to the queue
   */
  public void runTask(Task task) {
    synchronized (q) {
      q.add(task);
      q.notify();
    }
  }

  private class PoolThread extends Thread {

    /**
     * Pulls tasks off the queue and calls the corresponding functions to either find the convex
     * hull of the set merge two tasks that have been pulled off.
     * 
     */
    @Override
    public void run() {
      // While there is still more than one lattice remaining unmerged...
      while (!exit) {
        Task task = null;
        synchronized (q) {
          // If the queue of tasks is empty, wait
          while (!exit && q.isEmpty()) {
            try {
              q.wait();
            } catch (InterruptedException e) {
              System.out.println(e.getMessage());
            }

          }

          /*
           * If the first element on the
           */
          if (q.peek().isHull()) {
            while (!exit && q.size() < 2) {
              try {
                q.wait();
              } catch (InterruptedException e) {
                System.out.println(e.getMessage());
              }
            }


            task = q.poll();
            /*
             * If the first element on the queue is already a convex hull then wait until a new
             * element is added. If the need element that was added is also a convex hull then merge
             * them. If not, add the convex hull back to the queue and find the convex hull of the
             * new element.
             */
            if (!exit) {
              if (q.peek().isHull()) {
                task = task.combineTasks(q.poll());
                numLattices--;
              } else {
                q.add(task);
                task = q.poll();
                task.setPool(getPool());
              }
            } else {
              q.add(task);
            }

          } else {
            task = q.poll();
            task.setPool(getPool());
          }


        }
        try {
          if (!exit && task != null) {
            task.run();
          }
        } catch (RuntimeException e) {
          System.out.println(e.getMessage());
        }

        // If there is only on lattice left then all lattices have been combined
        // set the flag to true to exit and notify all the threads.
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

  /**
   * Returns the lattice from the last
   */
  public IntegerLattice getResult() {
    return q.poll().getLattice();
  }


}
