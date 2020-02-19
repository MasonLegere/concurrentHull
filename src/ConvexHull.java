
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import org.jfree.data.xy.XYSeries;

public class ConvexHull {

	public static void main(String[] args) throws IOException {

		String line; // buffer for user input
		int[] numThreads; // numThreads[i] <- number of threads to use for ith convex hull
		int numPoints, numThread;
		ThreadPool pool = null;
		Scanner scanner = new Scanner(System.in);

		System.out.println(" > Would you like to find the convex hull of points specified in a file (Y/N)");
		line = scanner.nextLine().toUpperCase();

		if (line.equals("Y")) {
			
			System.out.println(" > Please enter the relative path of the file");

			line = scanner.nextLine();
			ArrayList<Point> points = readFile(line);
			
			System.out.println(
					" > Enter a positive integer to specifiy the number of threads to be used.");
			line = scanner.nextLine();
			numThread = Integer.parseInt(line);
			try {
				pool = new ThreadPool(numThread, 2 * numThread, partitionLattice(points, 2 * numThread));
			}
			catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			
			points = pool.getResult().getLattice();
			
			for (int i = 0; i < points.size(); i++) {
				System.out.println(points.get(i));
			}
			 
			
		}

		else if (line.equals("N")) {
			
			System.out.println(" > Enter a postive integer to specify number of points");
			line = scanner.nextLine();
			numPoints = Integer.parseInt(line);

			System.out.println(
					" > Enter a comma delimited list of postive integers to specifiy the number of threads to be used.");
			line = scanner.nextLine();
			numThreads = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
			scanner.close();
			XYSeries times = runSimulation(numPoints, numThreads);
			LinePlotDisplay plot = new LinePlotDisplay(times);
			plot.setVisible(true);
			
		} else {
			
			System.out.println(" > Inavlid input - program terminating");
			System.exit(1);
			
		}


	}

	private static XYSeries runSimulation(int numPoints, int[] numThreads) {

		XYSeries times = new XYSeries("Running Times");
		long initialTime, finalTime, timeElapsed;
		ThreadPool pool = null;

		ArrayList<Integer> pointsX = new ArrayList<Integer>();
		ArrayList<Integer> pointsY = new ArrayList<Integer>();
		ArrayList<Point> points = new ArrayList<Point>();

		for (int i = 0; i < numPoints; i++) {
			pointsX.add(new Integer(i));
		}

		for (int i = 0; i < numPoints; i++) {
			pointsY.add(new Integer(i));
		}

		Collections.shuffle(pointsX);
		Collections.shuffle(pointsX);

		for (int i = 0; i < numPoints; i++) {
			points.add(new Point(pointsX.get(i), pointsY.get(i)));
		}

		for (int i : numThreads) {
			initialTime = System.currentTimeMillis();

			try {
				pool = new ThreadPool(i, 2 * i, partitionLattice(points, 2 * i));
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}

			pool.getResult().getLattice();
			finalTime = System.currentTimeMillis();
			timeElapsed = finalTime - initialTime;
			System.out.println("Number of Threads: " + i + "  ---- Time Taken: " + timeElapsed + " (ms)");
			times.add(i, timeElapsed);
		}

		return times;
	}

	private static LinkedBlockingQueue<Task> partitionLattice(ArrayList<Point> points, int numPartitions) {
		int intervalSize = points.size() / numPartitions;
		IntegerLattice tempLattice;
		ArrayList<Point> tempList;
		int lowerBound = 0, upperBound = 0;
		LinkedBlockingQueue<Task> q = new LinkedBlockingQueue<Task>();

		if (intervalSize < 3) {
			System.out.println("Not possible to find the convex hull using the required specifications");
			System.exit(0);
		}

		points.sort(Comparator.comparing(Point::getX)); // sort array list by X-values

		for (int i = 0; i < numPartitions - 1; i++) {
			lowerBound = i * intervalSize;
			upperBound = (i + 1) * intervalSize;
			tempList = new ArrayList<Point>(points.subList(lowerBound, upperBound));
			tempLattice = new IntegerLattice(points.get(lowerBound).getX(), points.get(upperBound - 1).getX(),
					tempList);
			q.add(new Task(tempLattice));

		}

		tempList = new ArrayList<Point>(points.subList(upperBound, points.size()));
		tempLattice = new IntegerLattice(points.get(upperBound).getX(), points.get(points.size() - 1).getX(), tempList);
		q.add(new Task(tempLattice));

		return q;

	}

	private static ArrayList<Point> readFile(String fileName) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<Point> points = new ArrayList<Point>();

		String[] arr;

		String input = reader.readLine();

		while (input != null) {
			arr = input.split(" ");
			points.add(new Point(arr[0], arr[1]));
			input = reader.readLine();
		}

		reader.close();
		return points;
	}

	public static IntegerLattice combineHull(IntegerLattice leftLattice, IntegerLattice rightLattice) {

		ArrayList<Point> left = leftLattice.getLattice();
		ArrayList<Point> right = rightLattice.getLattice();

		if (leftLattice.getRightPoint() > rightLattice.getLeftPoint()) {
			right = leftLattice.getLattice();
			left = rightLattice.getLattice();
		}

		int leftMost = 0, rightMost = 0;
		int leftTp, rightTp;
		int upperLeft, lowerLeft;
		int upperRight, lowerRight;
		boolean flag = false;
		ArrayList<Point> mergedHull = new ArrayList<Point>();

		for (int i = 1; i < left.size(); i++) {
			if (left.get(i).getX() > left.get(rightMost).getX()) {
				rightMost = i;
			}
		}

		for (int i = 1; i < right.size(); i++) {
			if (right.get(i).getX() < right.get(leftMost).getX()) {
				leftMost = i;
			}
		}

		leftTp = leftMost;
		rightTp = rightMost;

		while (!flag) {
			flag = true;

			while (orient(right.get(leftTp), left.get(rightTp), left.get((rightTp + 1) % left.size())) >= 0) {
				rightTp = (rightTp + 1) % left.size();
			}

			while (orient(left.get(rightTp), right.get(leftTp),
					right.get((leftTp - 1 + right.size()) % right.size())) <= 0) {
				leftTp = (right.size() + leftTp - 1) % right.size();
				flag = false;
			}
		}

		upperLeft = rightTp;
		upperRight = leftTp;
		leftTp = leftMost;
		rightTp = rightMost;
		flag = false;

		while (!flag) {

			flag = true;

			while (orient(left.get(rightTp), right.get(leftTp), right.get((leftTp + 1) % right.size())) >= 0) {
				leftTp = (leftTp + 1) % right.size();
			}

			while (orient(right.get(leftTp), left.get(rightTp),
					left.get((left.size() + rightTp - 1) % left.size())) <= 0) {
				rightTp = (left.size() + rightTp - 1) % left.size();
				flag = false;
			}
		}

		lowerLeft = rightTp;
		lowerRight = leftTp;

		mergedHull.add(left.get(upperLeft));
		leftTp = upperLeft;

		while (leftTp != lowerLeft) {
			leftTp = (leftTp + 1) % left.size();
			mergedHull.add(left.get(leftTp));
		}

		leftTp = lowerRight;
		mergedHull.add(right.get(lowerRight));

		while (leftTp != upperRight) {
			leftTp = (leftTp + 1) % right.size();
			mergedHull.add(right.get(leftTp));
		}

		return new IntegerLattice(leftLattice.getLeftPoint(), rightLattice.getRightPoint(), mergedHull);

	}

	public static IntegerLattice convexHull(IntegerLattice lattice) {

		ArrayList<Point> points = lattice.getLattice();
		if (points.size() < 3)
			return null;

		ArrayList<Point> hull = new ArrayList<Point>();

		int l = 0;
		for (int i = 1; i < points.size(); i++)
			if (points.get(i).getX() < points.get(l).getX())
				l = i;

		int p = l, q;
		do {
			hull.add(points.get(p));
			q = (p + 1) % points.size();

			for (int i = 0; i < points.size(); i++) {

				if (orient(points.get(p), points.get(i), points.get(q)) < 0)

					q = i;
			}

			p = q;

		} while (p != l);

		return new IntegerLattice(lattice.getLeftPoint(), lattice.getRightPoint(), hull);
	}

	// 0 --> p, q and r are colinear
	// 1 --> Clockwise
	// 2 --> Counterclockwise
	public static int orient(Point p, Point q, Point r) {
		return (q.getY() - p.getY()) * (r.getX() - q.getX()) - (q.getX() - p.getX()) * (r.getY() - q.getY());
	}
	
	

}
