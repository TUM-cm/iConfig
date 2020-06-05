import java.util.*;

public class DijkstraPath {
    private final List<DijkstraPoint> nodes;
    private final List<DijkstraEdge> edges;
    private Set<DijkstraPoint> settledNodes;
    private Set<DijkstraPoint> unSettledNodes;
    private Map<DijkstraPoint, DijkstraPoint> predecessors;
    private Map<DijkstraPoint, Integer> distance;

    public DijkstraPath(DijkstraGraph graph) {
        this.nodes = new ArrayList<DijkstraPoint>(graph.getPoints().values());
        this.edges = new ArrayList<DijkstraEdge>(graph.getEdges());
    }

    public void execute(DijkstraPoint source) {
        settledNodes = new HashSet<DijkstraPoint>();
        unSettledNodes = new HashSet<DijkstraPoint>();
        distance = new HashMap<DijkstraPoint, Integer>();
        predecessors = new HashMap<DijkstraPoint, DijkstraPoint>();
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            DijkstraPoint node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(DijkstraPoint node) {
        List<DijkstraPoint> adjacentNodes = getNeighbors(node);
        for (DijkstraPoint target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private int getDistance(DijkstraPoint node, DijkstraPoint target) {
        for (DijkstraEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Error getting distance");
    }

    private List<DijkstraPoint> getNeighbors(DijkstraPoint node) {
        List<DijkstraPoint> neighbors = new ArrayList<DijkstraPoint>();
        for (DijkstraEdge edge : edges) {
            if (edge.getSource().equals(node)
                    && !isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private DijkstraPoint getMinimum(Set<DijkstraPoint> vertices) {
        DijkstraPoint minimum = null;
        for (DijkstraPoint vertex : vertices) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(DijkstraPoint vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(DijkstraPoint destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }


    public LinkedList<DijkstraPoint> getPath(DijkstraPoint target) {
        LinkedList<DijkstraPoint> path = new LinkedList<DijkstraPoint>();
        DijkstraPoint step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }

        Collections.reverse(path);
        return path;
    }
}
