public class Edge {
    private Point point1;
    private Point point2;


    public Edge(Point point1, Point point2) {
        super();
        this.point1 = point1;
        this.point2 = point2;
    }

    public Point getNeighbourPoint(Point point) {
        if (point == point1){
            return point2;
        }
        else {
            return point1;
        }
    }

    public Point getPoint1() {
        return point1;
    }

    public Point getPoint2() {
        return point2;
    }
}
