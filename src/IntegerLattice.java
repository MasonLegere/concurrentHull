import java.util.ArrayList;

public class IntegerLattice {
	private ArrayList<Point> points;
	private int leftMostPoint; 
	private int rightMostPoint; 
	
	public IntegerLattice(int leftMostPoint, int rightMostPoint, ArrayList<Point> points) {
		this.leftMostPoint = leftMostPoint;
		this.rightMostPoint = rightMostPoint;
		this.points = points;
	}
	
	public ArrayList<Point> getLattice(){
		return this.points;
	}
	
	public int getLeftPoint() {
		return leftMostPoint;
	}
	
	public int getRightPoint() {
		return rightMostPoint; 
	}
	
	public void setLeftPoint(int i) {
		this.leftMostPoint = i;
	}
	
	public void setRightPoint(int i) {
		this.rightMostPoint = i;
	}
	
	
	
}
