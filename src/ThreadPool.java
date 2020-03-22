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
  private PoolThread[] threads;

  /**
   * number of threads using for the computation 
   */
  private final int numThreads; 
  
  /**
   * Tasks added and removed from the queue in a synchronized manner
   */
  private LinkedBlockingQueue<Task> q;

  /**
   * Number of lattices initially on the queue during initialization
   */
  private int numLattices;
  /**
   * Flag to represent if there is any work let to do.
   */
  private volatile boolean exit;

  public ThreadPool(int numThreads, int numLattices) {
    this.numThreads = numThreads;
    this.numLattices = numLattices;
    this.exit = false;
    this.q = null;
    
  }

   
  public void initPool(LinkedBlockingQueue<Task> q) {
    this.q = q;
    threads = new PoolThread[this.numThreads];
    try {
      // start and join all threads.
      for (int i = 0; i < this.numThreads; i++) {
        threads[i] = new PoolThread();
        threads[i].start();
      }
  
      for (int i = 0; i < this.numThreads; i++) {
        threads[i].join();
      }
    }
    catch (InterruptedException e) {
      System.out.println(e.getMessage());
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
          
          System.out.println("here1");
          while (!exit && q.isEmpty()) {
            try {
              q.wait();
            } catch (InterruptedException e) {
              System.out.println(e.getMessage());
            }

          }

          assert q.peek() != null;
          if (!q.peek().isHull()) {
            System.out.println("here2");
            task = q.poll();     
          }   
          else {
            System.out.println("here3");
            while (!exit && q.size() < 2) {
              try {
                q.wait();
              } catch (InterruptedException e) {
                System.out.println(e.getMessage());
              }
            }

            task = q.poll();
            System.out.println("here4");
            if (!exit) {
              if (q.peek().isHull) {
                task = new MergeTask(task,q.poll());
                numLattices--;
              } else {
                q.add(task);
                task = q.poll();
              }
            } else {
              q.add(task);
            }

          } 
          System.out.println("here5");
          if (numLattices == 1) {
            exit = true;
            q.notifyAll();
          }
          
          System.out.println(q.size());
        }
        
        try {
          if (!exit) {
            task.run();
          }
        } catch (RuntimeException e) {
          System.out.println(e.getMessage());
        }

        // If there is only on lattice left then all lattices have been combined
        // set the flag to true to exit and notify all the threads. 

      }

    }

  }

  /**
   * Returns the lattice from the last
   */
  public IntegerLattice getResult() {
    return q.poll().getLattice();
  }


}
