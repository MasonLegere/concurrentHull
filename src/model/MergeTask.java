package model;


import main.ConvexHull;

public class MergeTask extends Task {

    private final IntegerLattice rightLattice;


    public MergeTask(Task left, Task right) {
        super(left.getLattice(), left.pool, false);
        this.rightLattice = right.getLattice();

        assert left.pool.equals(right.pool);
    }

    @Override
    public void run() {
        Task task = new ConvexTask(ConvexHull.combineHull(leftLattice, rightLattice), this.pool, true);
        this.pool.addTask(task);
    }


}
