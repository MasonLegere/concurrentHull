public class TestClass extends Thread {

    private static Lock lock = new Lock(3);
    private final int ID;

    public TestClass(int ID){
        this.ID = ID;
    }

    @Override
    public void run() {
        System.out.println(ID + " is attempting to get lock");
        lock.lock(ID);
        System.out.println(ID + " has acquired the lock");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(ID + " has released the lock");
        lock.unlock(ID);


    }

}
