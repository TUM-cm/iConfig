import java.util.ArrayList;

import static java.lang.StrictMath.*;
import static java.lang.StrictMath.min;

public class SamplingGraph extends VisibilityGraph {
    private ArrayList<Edge> visibleEdges = new ArrayList<Edge>();
    private ArrayList<Point> points;
    private ArrayList<Point> beacons;
    private Point start;


    public SamplingGraph(ArrayList<MapEntry> data, Point start){
        super();
        this.start = start;
        build(data,start);
    }



    public void build(ArrayList<MapEntry> data, Point start) {
        SampledEnvironment env = new SampledEnvironment(data, start);
        this.points = env.getPoints();
        this.beacons = env.getBeacons();
        visibleEdges = computeVisibleEdges(env.getPoints(), env.getWalls(), env.getCenterOfGravityX(), env.getCenterOfGravityY());
    }

    private ArrayList<Edge> computeVisibleEdges(ArrayList<Point> points, ArrayList<Edge> walls, Double centerOfGravityX, Double centerOfGravityY) {
        for(Point point1: points) {
            ArrayList<Point> visiblePoints = getVisiblePoints(point1, points, walls,centerOfGravityX,centerOfGravityY);
            for(Point point2: visiblePoints) {
                visibleEdges.add(new Edge(point1,point2));
            }
        }
        return visibleEdges;
    }

    private ArrayList<Point> getVisiblePoints(Point point1, ArrayList<Point> points, ArrayList<Edge> walls, Double centerOfGravityX, Double centerOfGravityY) {
        ArrayList<Point> visiblePoints = new ArrayList<>();
        for(Point p: points) {
            if (!p.equals(point1)){
                Boolean isVisible;

                isVisible = !(edgeIntersection(point1, p, walls, centerOfGravityX, centerOfGravityY));

                if (isVisible) {
                    visiblePoints.add(p);
                }
            }
        }
        return visiblePoints;
    }

/*    private ArrayList<Point> getVisiblePoints(Point point1, ArrayList<Point> points, ArrayList<Edge> walls, Double centerOfGravityX, Double centerOfGravityY) {
        ArrayList<Point> visiblePoints = new ArrayList<>();
        for(Point p: points) {
            if (!p.equals(point1)){
                if (sqrt(pow(point1.getX()-p.getX(),2)+pow(point1.getY()-p.getY(),2)) < 5) {
                    visiblePoints.add(p);
                }
            }
        }
        return visiblePoints;
    }*/

    private boolean edgeIntersection(Point point1, Point p, ArrayList<Edge> walls, Double centerOfGravityX, Double centerOfGravityY) {
        Double offset1x = (centerOfGravityX - point1.getX())/(sqrt(pow(centerOfGravityX - point1.getX(),2d) + pow(centerOfGravityY - point1.getY(), 2d)));
        Double offset1y = (centerOfGravityY - point1.getY())/(sqrt(pow(centerOfGravityX - point1.getX(),2d) + pow(centerOfGravityY - point1.getY(), 2d)));
        Double offset2x = (centerOfGravityX - p.getX())/(sqrt(pow(centerOfGravityX - p.getX(),2d) + pow(centerOfGravityY - p.getY(), 2d)));
        Double offset2y = (centerOfGravityY - p.getY())/(sqrt(pow(centerOfGravityX - p.getX(),2d) + pow(centerOfGravityY - p.getY(), 2d)));
        Point p1 = new Point(-1,point1.getX() + offset1x * 0.1, point1.getY() + offset1y * 0.1);
        Point p2 = new Point(-1,p.getX() + offset2x * 0.1,p.getY() + offset2y * 0.1);
        for (Edge e: walls){
            if (intersetionTest(p1,p2, e)) {
                return true;
            }

        }
        return false;
    }

    private boolean intersetionTest(Point p1, Point p2, Edge e) {

        Point q1 = e.getPoint1();
        Point q2 = e.getPoint2();
        int o1 = collinearityCheck(p1, p2, q1);
        int o2 = collinearityCheck(p1, p2, q2);
        int o3 = collinearityCheck(q1, q2, p1);
        int o4 = collinearityCheck(q1, q2, p2);


        if ((o1 != o2) && (o3 != o4)) return true;

        if ((o1 == 0) && (onSegment(p1, q1, p2))) return true;

        if ((o2 == 0) && (onSegment(p1, q2, p2))) return true;

        if ((o3 == 0) && (onSegment(q1, p1, q2))) return true;

        if ((o4 == 0) && (onSegment(q1, p2, q2))) return true;

        return false;
    }


    // check if p2 lies on a segment consisting of p1-p3
    private boolean onSegment(Point p1, Point p2, Point p3) {
        if ((p2.getX() <= max(p1.getX(), p3.getX())) && (p2.getX() >= min(p1.getX(), p3.getX()))){
            if ((p2.getY() <= max(p1.getY(), p3.getY())) && (p2.getY() >= min(p1.getY(), p3.getY()))){
                return true;
            }
        }
        return false;
    }

    // returns true if this three points are collinear
    private int collinearityCheck(Point point1, Point previous, Point p) {
        double temp = ((previous.getY() - point1.getY()) * (p.getX() - previous.getX()) - (previous.getX() - point1.getX()) * (p.getY() - previous.getY()));
        int space = (int) temp;
        if (temp == 0) {
            return 0;
        }
        else if (temp > 0) {
            return 1;
        }
        else {
            return -1;
        }
    }

    public ArrayList<Edge> getVisibleEdges() {
        return visibleEdges;
    }

    public Point getStart() {
        return start;
    }

    public ArrayList<Point> getBeacons() {
        return beacons;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }
}
