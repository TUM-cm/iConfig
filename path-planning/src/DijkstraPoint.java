public class DijkstraPoint {
    final private String id;
    final private String name;
    final private Double x;
    final private Double y;
    final private Double range;

    public DijkstraPoint(String id, String name, Double x, Double y, Double range) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.range = range;
    }
    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getRange() {
        return range;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DijkstraPoint other = (DijkstraPoint) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
