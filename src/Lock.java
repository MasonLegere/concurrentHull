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

    public void unlock(int threadNum){
      c[threadNum] = 0;

    }

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
