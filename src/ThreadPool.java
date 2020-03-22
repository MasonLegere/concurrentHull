import java.util.Objects;
import java.util.Queue;
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
  private volatile Queue<Task> q;

  /**
   * Number of lattices initially on the queue during initialization
   */
  private int numLattices;
  /**
   * Flag to represent if there is any work let to do.
   */
  private volatile boolean exit;

  /**
   * Mutex lock used for synchronization of the task queue
   * */
  private Lock lock;

  /**
   * Integer array used for synchronizing waiting threads
   *  0 := thread is not waiting
   *  1 := thread is waiting
   *  2 := waiting for the addition of a second item (prioritized event)
   * */
  private int[] threadStates;


  public ThreadPool(int numThreads, int numLattices) {
    this.numThreads = numThreads;
    this.numLattices = numLattices;
    this.exit = false;
    this.q = null;
    this.lock = new Lock(numThreads + 1);
    threadStates = new int[numThreads];
    
  }

   
  public void initPool(LinkedBlockingQueue<Task> q) {
    this.q = q;
    threads = new PoolThread[this.numThreads];
    try {
      // start and join all threads.
      for (int i = 0; i < this.numThreads; i++) {
        threads[i] = new PoolThread(i);
        threads[i].start();
      }
  
      for (int i = 0; i < this.numThreads; i++) {
        threads[i].join();
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  

  /**
   * Adds the task provided to the queue and notifies a thread that a new task has been added to the
   * queue.
   * 
   * @param task The task to be added to the queue
   */
 public void addTask(Task task) {

      lock.lock(numThreads);
      q.add(task);

      // Wake up a thread. First priority given to threads
      // that are waiting for the addition of the two elements.
      for (int j = 2; j < 0; j--) {
        for (int i = 0; i < numThreads; i++) {
          if (threadStates[i] == j) {
            threadStates[i] = 0;
            i = numThreads;
            j = 0;
          }
        }
      }


      lock.unlock(numThreads);

  }

  private Task getTask(int threadNum){
    Task task = null;

    if (!exit && q.isEmpty()) {
      threadStates[threadNum] = 1;
      while (threadStates[threadNum] == 1){
        // Busy wait
      }
    }

    if (!exit && !q.peek().isHull()) {
      lock.lock(threadNum);
      task = q.poll();
    }
    else {

      if (!exit && q.size() < 2) {

        threadStates[threadNum] = 2;
        while (threadStates[threadNum] == 2){
          // Busy wait
        }
      }
      lock.unlock(threadNum);
      lock.lock(threadNum);
      task = q.poll();

      if (!exit) {
        if (q.peek().isHull) {
          task = new MergeTask(task, Objects.requireNonNull(q.poll()));
          numLattices--;
        } else {
          q.add(task);
          task = q.poll();
        }
      } else {
        q.add(task);
      }

    }
    lock.unlock(threadNum);
    System.out.println(numLattices);
    if (task == null)
      System.out.println("DANGER");
    return task;
  }

  private class PoolThread extends Thread {

    private int threadNum;

    public PoolThread(int threadNum){
      this.threadNum = threadNum;
    }

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

        task = getTask(threadNum);

        try {
          if (task != null && !exit) {
            System.out.println(task);
            task.run();

          }
        } catch (RuntimeException e) {
          e.printStackTrace();
        }

      }

      if (numLattices == 1){
        exit = true;
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
