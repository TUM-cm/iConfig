public class Line {
    public Double x1;
    public Double y1;
    public Double x2;
    public Double y2;

    public Line(Double x1, Double y1, Double x2, Double y2) {
        super();
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Double getX1() {
        return x1;
    }

    public Double getX2() {
        return x2;
    }

    public Double getY1() {
        return y1;
    }

    public Double getY2() {
        return y2;
    }

    public void setX1(Double x1) {
        this.x1 = x1;
    }

    public void setX2(Double x2) {
        this.x2 = x2;
    }

    public void setY1(Double y1) {
        this.y1 = y1;
    }

    public void setY2(Double y2) {
        this.y2 = y2;
    }
}
