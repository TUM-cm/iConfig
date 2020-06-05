public class DijkstraEdge {
    private final String id;
    private final DijkstraPoint source;
    private final DijkstraPoint destination;
    private final int weight;

    public DijkstraEdge(String id, DijkstraPoint source, DijkstraPoint destination, int weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }
    public DijkstraPoint getDestination() {
        return destination;
    }

    public DijkstraPoint getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }
}
