
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public class ConvexHull {

	
	public static void main(String[] args) throws IOException {
		
		ArrayList<Point> points = readFile("ConvexHullInput.txt");
		points.sort(Comparator.comparing(Point:: getX));
		ArrayList<Point> points1 =  new ArrayList<Point>( points.subList(0, points.size()/2)); 
		ArrayList<Point> points2 =  new ArrayList<Point>( points.subList(points.size()/2 + 1,points.size())); 
		
//		points1 = convexHull(points1);
//		points2 = convexHull(points2);
//		points = combineHull(points1, points2); 
//		
		for (int i = 0; i < points.size(); i++) {
			System.out.println(points.get(i)); 
		}
		
	}
	
	private static ArrayList<Point> readFile(String fileName) throws IOException{
			
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<Point> points = new ArrayList<Point>();
		
		String[] arr;
		
		String input = reader.readLine();
		
		while(input != null) {
			arr = input.split(" ");
			points.add(new Point(arr[0],arr[1]));	
			input = reader.readLine();
		}
		
		return points;
	}
	
	public static IntegerLattice combineHull(IntegerLattice leftLattice, IntegerLattice rightLattice){
		
		ArrayList<Point> left = leftLattice.getLattice();
		ArrayList<Point> right = rightLattice.getLattice();
		
		int leftMost= 0, rightMost = 0; 
		int leftTp, rightTp;
		int upperLeft, lowerLeft;
		int upperRight, lowerRight; 
		boolean flag =  false;
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
		
		leftTp = leftMost; rightTp = rightMost;
		
		while(!flag) {
			flag = true;
			
			while (orient(right.get(leftTp), left.get(rightTp), left.get((rightTp + 1) % left.size())) >= 0) {
				rightTp = (rightTp + 1) % left.size();
			}
			
			while (orient(left.get(rightTp), right.get(leftTp), right.get((leftTp - 1 + right.size()) % right.size())) <= 0) {
				leftTp = (right.size() + leftTp - 1) % right.size();
				flag = false;
			}			
		}
		
		upperLeft = rightTp;
		upperRight = leftTp;
		leftTp = leftMost; rightTp = rightMost;	
		flag = false;
		
		while(!flag) {
			
			flag = true;
			
			while (orient(left.get(rightTp), right.get(leftTp), right.get((leftTp + 1) % right.size())) >= 0) {
				leftTp = (leftTp + 1) % right.size();
			}
			
			while (orient(right.get(leftTp), left.get(rightTp), left.get( (left.size() + rightTp - 1) % left.size())) <= 0) {
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
		
		while(leftTp != upperRight) {
			leftTp = (leftTp + 1) % right.size();
			mergedHull.add(right.get(leftTp));
		}
		
		return new IntegerLattice(leftLattice.getLeftPoint(), rightLattice.getRightPoint(), mergedHull); 
		
	}
	
	public static IntegerLattice convexHull(IntegerLattice lattice){
		
		ArrayList<Point> points = lattice.getLattice();
		if (points.size() < 3) return null; 
		
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
        
		return new IntegerLattice(lattice.getLeftPoint(), lattice.getRightPoint(), points);
	}
	
	// 0 --> p, q and r are colinear 
	// 1 --> Clockwise 
	// 2 --> Counterclockwise 
	public static int orient(Point p, Point q, Point r) 
    { 
       return (q.getY() - p.getY()) * (r.getX() - q.getX()) - 
                  (q.getX() - p.getX()) * (r.getY() - q.getY());       
    } 
	

}
