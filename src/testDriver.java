import java.util.ArrayList;

public class testDriver {

    public static void main(String[] args) throws InterruptedException {

        ArrayList<TestClass> threads = new ArrayList<TestClass>();

        for (int i = 0; i < 3; i++){
            TestClass thread = new TestClass(i);
            threads.add(thread);
            thread.start();
        }

        for (TestClass thread : threads){
            thread.join();
        }

    }

}
