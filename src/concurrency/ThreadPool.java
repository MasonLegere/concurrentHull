package concurrency;

import concurrency.Lock;
import model.IntegerLattice;
import model.MergeTask;
import model.Task;

import java.util.Queue;

/**
 * This class is a thread pool acting as an environment for the the management of tasks. This
 * management involves synchronization for the addition of new tasks to the queue as well as
 * removing tasks from the queue. Removal of tasks from the queue is nontrivial as in the case where
 * we need to merge to convex hulls.
 */

public class ThreadPool {

    /**
     * threads that are available for the queue to notify in order to complete tasks
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
     */
    private Lock lock;

    /**
     * Integer array used for synchronizing waiting threads 0 := thread is not waiting 1 := thread is
     * waiting 2 := waiting for the addition of a second item (prioritized event)
     */
    private int[] threadStates;


    public ThreadPool(int numThreads, int numLattices) {
        this.numThreads = numThreads;
        this.numLattices = numLattices;
        this.exit = false;
        this.q = null;
        this.lock = new Lock(numThreads + 1);
        threadStates = new int[numThreads];

    }

    /**
     * Initializes the thread pool by creating threads and joining them to the main thread. Also
     * assigns initial tasks to the queue.
     *
     * @param q Initial tasks to be completed.
     */
    public void initPool(Queue<Task> q) {
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
        } catch (InterruptedException e) {
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

        if (exit) {
            for (int i = 0; i < numThreads; i++) {
                threadStates[i] = 0;
            }
        }

        lock.lock(numThreads);
        q.add(task);

        // Wake up a thread. First priority given to threads
        // that are waiting for the addition of the two elements.
        for (int j = 2; j > 0; j--) {
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

    /**
     * Returns the next task inline within the thread pool. In the case where the first two takes in
     * the pool have no interior points, they will be merged together and the result will be added to
     * the queue. In the case where the first element in the queue has no interior points and the
     * second element does, the first element will be pushed back onto the queue and the maximum
     * number of interior points will be removed from the second set. This method may return a null
     * when no tasks are remaining (exit <- true) or in the event where two threads are attempting to
     * schedule elements for the same merge operation.
     *
     * @param threadNum The ID of the thread wanting the next task. Used to obtain the mutex lock
     * @return task The next task to be ran
     */
    private Task getTask(int threadNum) {
        Task task = null;

        // if the queue is empty, wait for an element to be added
        if (!exit && q.isEmpty()) {
            threadStates[threadNum] = 1;
            while (!exit && threadStates[threadNum] == 1) {
                // Busy waiting
            }
        }

     /*
     obtain mutex lock, if the first set has interior points, pull it off and continue,
     else release the lock and busy wait until at least two elements are on the queue. It is
     essential to drop the lock before busy waiting to allow concurrent activity and placement in
     the queue - deadlock will ensue otherwise. Once two items have been added check whether
     they can be merged. Null checks are needed in the case where two threads exit busy waiting
     at roughly the same time due to separate placements at the end of the queue.
     */

        lock.lock(threadNum);
        if (!exit && q.peek() != null && !q.peek().isHull()) {
            task = q.poll();
        } else {
            lock.unlock(threadNum);
            if (!exit && q.size() < 2) {

                threadStates[threadNum] = 2;
                while (!exit && threadStates[threadNum] == 2) {
                    // Busy wait
                }
            }

            lock.lock(threadNum);
            task = q.poll();

            if (task != null) {
                if (!exit && !q.isEmpty()) {
                    assert q.peek() != null;
                    if (q.peek().isHull()) {

                        task = new MergeTask(task, q.poll());
                        numLattices--;

                        if (numLattices == 1) {
                            exit = true;
                        }

                    } else {
                        q.add(task);
                        task = q.poll();
                    }
                } else {
                    q.add(task);
                    task = null;
                }
            }
        }
        lock.unlock(threadNum);

        return task;
    }

    private class PoolThread extends Thread {

        private int threadNum;

        public PoolThread(int threadNum) {
            this.threadNum = threadNum;
        }

        /**
         * Pulls tasks off the queue and calls the corresponding functions to either find the convex
         * hull of the set merge two tasks that have been pulled off.
         */

        @Override
        public void run() {
            // While there is still more than one lattice remaining unmerged...
            while (!exit) {
                Task task = null;

                task = getTask(threadNum);

                // null check outside of the try statement for performance
                if (task != null) {
                    try {
                        task.run();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
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
