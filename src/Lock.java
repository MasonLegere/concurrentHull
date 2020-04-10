/**
 * Implements a mutex lock using Lamport's Bakery Algorithm. This code has been rued from my
 * operating systems assignment.
 */


public class Lock {

    private volatile int c[];
    private volatile boolean tk[];

    public Lock(int numThreads){

        c = new int[numThreads];
        tk = new boolean[numThreads];

        for (int i = 0; i < numThreads; i++){
            c[i] = 0;
            tk[i] = false;
        }

    }

    /**
     * Obtains the mutex lock to thread with unique ID threadNum - may have to wait for other
     * threads to finish their work in the critical section.
     * @param threadNum The thread asking for the mutex lock
     */
    public void lock(int threadNum){

        tk[threadNum] = true;

        c[threadNum] = maxID() + 1;
        tk[threadNum] = false;

        for (int i = 0; i < c.length; i++){

            if (i == threadNum) {
                continue;
            }

            while (tk[i]) {

            }

            while (c[i] != 0 && (c[threadNum] > c[i] || (c[threadNum] == c[i] && threadNum > i))) {

            }
        }

    }

    /**
     * Drops the lock held by the thread with unique ID theadNum - done instantly.
     * @param threadNum The thread asking for the mutex lock
     */
    public void unlock(int threadNum){
      c[threadNum] = 0;

    }

    /*
    *  Returns the lowest priority index within the currently waiting threads. That is, the maximum
    *  index. Newly created threads will be assigned this index plus one.
    * */
    public int maxID(){
        int max = c[0];

        for (int val : c){
            if (val > max){
                max = val;
            }
        }
        return max;
    }

}
