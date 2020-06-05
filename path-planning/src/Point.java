public class Point {
    private Integer id;
    private Double xCoordinate;
    private Double yCoordinate;
    private Double range;

    public Point(Integer id, Double xCoord, Double yCoord) {
        super();
        this.id = id;
        this.xCoordinate = xCoord;
        this.yCoordinate = yCoord;
    }

    public double getX() {
        return xCoordinate;
    }

    public double getY() {
        return yCoordinate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }
}
