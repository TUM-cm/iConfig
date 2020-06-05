public class Point {
    public Double x;
    public Double y;
    public String info;

    public Point(Double x, Double y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Point(Double x, Double y, String info) {
        super();
        this.x = x;
        this.y = y;
        this.info = info;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }
}

