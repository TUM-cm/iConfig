import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

public class DijkstraGraph {
    private HashMap<Integer,DijkstraPoint> points = new HashMap<Integer,DijkstraPoint>();
    private ArrayList<DijkstraEdge> edges = new ArrayList<DijkstraEdge>();
    private ArrayList<DijkstraPoint> beacons = new ArrayList<DijkstraPoint>();

    public DijkstraGraph(VisibilityGraph visibilityGraph) {

        for (Point p: visibilityGraph.getPoints()){
            points.put(p.getId(),new DijkstraPoint(""+p.getId(),"Node_"+p.getId(),p.getX(),p.getY(),0d));
        }

        for (Point b: visibilityGraph.getBeacons()){
            beacons.add(new DijkstraPoint(""+b.getId(),"Beacon_"+b.getId(),b.getX(),b.getY(),b.getRange()));
        }

        int i = 0;
        for (Edge e: visibilityGraph.getVisibleEdges()) {
            int weight = (int) sqrt(pow(e.getPoint1().getX()-e.getPoint2().getX(),2)+pow(e.getPoint1().getY()-e.getPoint2().getY(),2));
            edges.add(new DijkstraEdge(Integer.toString(i),points.get(e.getPoint1().getId()),points.get(e.getPoint2().getId()),weight));
            i++;
        }


    }

    public HashMap<Integer, DijkstraPoint> getPoints() {
        return points;
    }

    public List<DijkstraPoint> getBeacons() {
        return beacons;
    }

    public List<DijkstraEdge> getEdges() {
        return edges;
    }
}
