package model;

import concurrency.ThreadPool;
import main.ConvexHull;

public class ConvexTask extends Task {

    public ConvexTask(IntegerLattice leftLattice, ThreadPool pool) {
        super(leftLattice, pool, false);
        // When initially making a task the convex hull has not be found.
    }

    public ConvexTask(IntegerLattice leftLattice, ThreadPool pool, boolean isHull) {
        super(leftLattice, pool, isHull);
    }

    @Override
    public void run() {
        Task task = new ConvexTask(ConvexHull.convexHull(leftLattice), this.pool, true);
        this.pool.addTask(task);
    }


}
