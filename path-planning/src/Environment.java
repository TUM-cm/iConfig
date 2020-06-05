import java.util.ArrayList;
import java.util.List;

public class Environment {
    private ArrayList<Edge> edges;
    private ArrayList<Edge> walls;
    private ArrayList<Point> points;
    private ArrayList<Point> beacons;
    private Double centerOfGravityY;
    private Double centerOfGravityX;


    public Environment(List<MapEntry> data, Point start) {
        super();
        this.walls = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.points = new ArrayList<>();
        this.beacons = new ArrayList<>();
        centerOfGravityX = 0d;
        centerOfGravityY = 0d;


        points.add(start);
        int i = 1;
        for(MapEntry mapEntry: data) {
            switch(mapEntry.getClassCategory()) {
                case 1:
                    walls.add(new Edge(new Point(-1,mapEntry.getLocationx1(),mapEntry.getLocationy1()),new Point(-1,mapEntry.getLocationx2(),mapEntry.getLocationy2())));
                    centerOfGravityX = centerOfGravityX + mapEntry.getLocationx1() + mapEntry.getLocationx2();
                    centerOfGravityY = centerOfGravityY + mapEntry.getLocationy1() + mapEntry.getLocationy2();

                    Point point = new Point(i,mapEntry.getLocationx1(),mapEntry.getLocationy1());
                    i++;
                    if (!isDoublePoint(point,points)) {
                        points.add(point);
                    }
                    for(MapEntry complement_point: data) {
                        Point otherPoint = new Point(-1,complement_point.getLocationx1(),complement_point.getLocationy1());
                        if((point.getX() != otherPoint.getX()) || (point.getY() != otherPoint.getY())){
                            Edge edge = new Edge(point,otherPoint);
                            edges.add(edge);
                        }
                        Point otherPoint2 = new Point(-1,complement_point.getLocationx2(),complement_point.getLocationy2());
                        if((point.getX() != otherPoint2.getX()) || (point.getY() != otherPoint2.getY())){
                            Edge edge = new Edge(point,otherPoint2);
                            edges.add(edge);
                        }
                    }


                    Point point2 = new Point(i,mapEntry.getLocationx2(),mapEntry.getLocationy2());
                    i++;
                    if (!isDoublePoint(point2,points)) {
                        points.add(point2);
                    }
                    for(MapEntry complement_point: data) {
                        Point otherPoint = new Point(-1,complement_point.getLocationx1(),complement_point.getLocationy1());
                        if((point2.getX() != otherPoint.getX()) || (point2.getY() != otherPoint.getY())){
                            Edge edge = new Edge(point2,otherPoint);
                            edges.add(edge);
                        }
                        Point otherPoint2 = new Point(-1,complement_point.getLocationx2(),complement_point.getLocationy2());
                        if((point2.getX() != otherPoint2.getX()) || (point2.getY() != otherPoint2.getY())){
                            Edge edge = new Edge(point2,otherPoint2);
                            edges.add(edge);
                        }
                    }
                    break;

                case 3:

                    Point point3 = new Point(i,mapEntry.getLocationx1(),mapEntry.getLocationy1());
                    i++;
                    if (!isDoublePoint(point3,points)) {
                        points.add(point3);
                    }
                    point3.setRange(mapEntry.getRange());
                    beacons.add(point3);

                    for(MapEntry complement_point: data) {
                        Point otherPoint = new Point(-1,complement_point.getLocationx1(),complement_point.getLocationy1());
                        if((point3.getX() != otherPoint.getX()) || (point3.getY() != otherPoint.getY())){
                            Edge edge = new Edge(point3,otherPoint);
                            edges.add(edge);
                        }
                    }
                    break;

            }}
        centerOfGravityY = centerOfGravityY / (data.size()*2);
        centerOfGravityX = centerOfGravityX / (data.size()*2);
    }

    private boolean isDoublePoint(Point point, ArrayList<Point> points) {
        for (Point p: points){
            if ((p.getX()==point.getX()) && (p.getY()==point.getY())){
                return true;
            }
        }
        return false;
    }


    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public ArrayList<Edge> getWalls() {
        return walls;
    }

    public ArrayList<Point> getBeacons() {
        return beacons;
    }

    public Double getCenterOfGravityX() {
        return centerOfGravityX;
    }

    public Double getCenterOfGravityY() {
        return centerOfGravityY;
    }
}
