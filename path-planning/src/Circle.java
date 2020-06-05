public class Circle {
    private Double radius;
    private Double locationx1;
    private Double locationy1;

    public Circle(Double radius, Double locationX1, Double locationY1) {
        super();
        this.radius = radius;
        this.locationx1 = locationX1;
        this.locationy1 = locationY1;
    }

    public Double getRadius() {
        return radius;
    }

    public Double getLocationy1() {
        return locationy1;
    }

    public Double getLocationx1() {
        return locationx1;
    }
}
