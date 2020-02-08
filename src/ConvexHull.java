import java.awt.List;
import java.util.ArrayList;

public class ConvexHull {

	public static void main(String[] args) {
		

	}
	
	private static ArrayList<Point> combineHull(ArrayList<Point> leftPoints, ArrayList<Point> rightPoints){
		
		Point leftMostPoint = rightPoints.get((0));
		Point rightMostPoint = leftPoints.get((0));
		int leftMostIndex = 0, rightMostIndex = 0; 
		Point leftTemp, rightTemp;
		Point prevPointp, prevPointq;
		
		
		for (int i = 1; i < leftPoints.size(); i++) {
			if (leftPoints.get(i).getX() >= rightMostPoint.getX()) {
				rightMostPoint = leftPoints.get(i);
				rightMostIndex = i;
			}
		}
		
		for (int i = 1; i < rightPoints.size(); i++) {
			if (rightPoints.get(i).getX() <= leftMostPoint.getX()) {
				leftMostPoint = rightPoints.get(i);
				leftMostIndex = i;
			}
		}
		
		
		while(true) {
			prevPointp = leftMostPoint
		}
		
		
		return null; 
		
	}
	
	private static ArrayList<Point> convexHull(ArrayList<Point> points){
		
		ArrayList<Point> hull = new ArrayList<Point>();
		
		int l = 0; 
        for (int i = 1; i < points.size(); i++) 
            if (points.get(i).getX() < points.get(l).getX()) 
                l = i; 
		
        int p = l, q; 
        do
        { 
            // Add current point to result 
            hull.add(points.get(p)); 
       
            // Search for a point 'q' such that  
            // orientation(p, x, q) is counterclockwise  
            // for all points 'x'. The idea is to keep  
            // track of last visited most counterclock- 
            // wise point in q. If any point 'i' is more  
            // counterclock-wise than q, then update q. 
            q = (p + 1) % points.size(); 
              
            for (int i = 0; i < points.size(); i++) 
            { 
               // If i is more counterclockwise than  
               // current q, then update q 
               if (orientation(points.get(p), points.get(i), points.get(q)) 
                                                   == 2) 
                   q = i; 
            } 
       
            // Now q is the most counterclockwise with 
            // respect to p. Set p as q for next iteration,  
            // so that q is added to result 'hull' 
            p = q; 
       
        } while (p != l);
        
		return hull;
	}
	
	// 0 --> p, q and r are colinear 
	// 1 --> Clockwise 
	// 2 --> Counterclockwise 
	public static int orientation(Point p, Point q, Point r) 
    { 
        int dir = (q.getY() - p.getY()) * (r.getX() - q.getX()) - 
                  (q.getX() - p.getX()) * (r.getY() - q.getY()); 
       
        if (dir == 0) {
        	return 0; 
        }
        else if (dir > 0) {
        	return 1;
        }
        else {
        	return 2;
        }
    } 

}
