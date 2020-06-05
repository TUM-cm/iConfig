import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sqrt;

public class Main {


    private static StringBuilder builder8;
    private static StringBuilder builder9;

    public static void main(String[] args) throws FileNotFoundException {
        VisibilityGraph visibilityGraph;
        SamplingGraph samplingGraph;
        DijkstraPath dijkstraPath;

        ArrayList<MapEntry> data = loadMapData();
        visibilityGraph = new VisibilityGraph(data,new Point(0,1.0,6.0));
        DijkstraGraph graph = new DijkstraGraph(visibilityGraph);
        dijkstraPath = new DijkstraPath(graph);
        HashMap<Integer,DijkstraPoint> dijkstraPoints = graph.getPoints();
        List<DijkstraPoint> beacons = graph.getBeacons();
        dijkstraPath.execute(dijkstraPoints.get(0));





        //use hotspots instead of data beacons --> mode2
        ArrayList<HotSpot> hotspots = computeHotSpots(beacons);
        data = loadHotSpotsIn(data, hotspots);
        visibilityGraph = new VisibilityGraph(data,new Point(0,1.0,6.0));
        graph = new DijkstraGraph(visibilityGraph);
        dijkstraPath = new DijkstraPath(graph);
        dijkstraPoints = graph.getPoints();
        beacons = graph.getBeacons();
        Collections.sort(beacons,new Comparator<DijkstraPoint>(){
            public int compare(DijkstraPoint p1, DijkstraPoint p2){
                double x1 = p1.getX();
                double x2 = p2.getX();
                return (int) (x1-x2);
            }});

        dijkstraPath.execute(dijkstraPoints.get(0));



        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("output.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();
        String ColumnNamesList = "device;time;distance;area;energy";
        builder.append(ColumnNamesList +"\n");


        PrintWriter pw1 = null;
        try {
            pw1 = new PrintWriter(new File("xhotspot.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder1 = new StringBuilder();
        builder1.append("[");

        PrintWriter pw2 = null;
        try {
            pw2 = new PrintWriter(new File("yhotspot.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[");

        for (HotSpot hotspot : hotspots) {
            builder1.append(hotspot.getLocationx1()+", ");
            builder2.append(hotspot.getLocationy1()+", ");
        }
        builder1.append("]");
        builder2.append("]");

        PrintWriter pw8 = new PrintWriter(new File("pathx.txt"));
        builder8 = new StringBuilder();

        builder8.append("[");

        PrintWriter pw9 = new PrintWriter(new File("pathy.txt"));
        builder9 = new StringBuilder();

        builder9.append("[");

        System.out.println("******Visibility Graph with Dijkstra******");
        double time = 0d;
        double totalDistance = 0d;
        double batteryCharge = 100d;
        int i = 1;
        int deviceCounter = 0;
        double area = 0d;
        for (DijkstraPoint beacon: beacons) {
            int devicesOnHotspot = hotspots.get(i-1).getDevicesInReach();
            deviceCounter = deviceCounter + devicesOnHotspot;
            LinkedList<DijkstraPoint> solutionPath = dijkstraPath.getPath(dijkstraPoints.get(Integer.valueOf(beacon.getId())));
            Double deltaTime = computeTime(solutionPath);
            time = time + deltaTime;
            time = time + (((Math.random() * (5) + 5))  * devicesOnHotspot);
            //area = 11.75 * beacon.getX();
            area = ((21.5+(21.5-((17/300)*beacon.getX())))*beacon.getX())/2;
            Double deltaDistance = computeDistance(solutionPath);
            totalDistance = totalDistance + deltaDistance;
            batteryCharge = 100 - 0.194 * time;
            System.out.println("Beacon number "+Integer.toString(deviceCounter)+" configured after " + Double.toString(time) + "s with total travel distance of " + Double.toString(totalDistance) + " meter and remaining battery charge of " + Double.toString(batteryCharge));
            builder.append(Integer.toString(deviceCounter)+";");
            builder.append(Double.toString(time)+";");
            builder.append(Double.toString(totalDistance)+";");
            builder.append(Double.toString(area)+";");
            builder.append(Double.toString(batteryCharge));
            builder.append('\n');
            i++;
            dijkstraPath.execute(dijkstraPoints.get(Integer.valueOf(beacon.getId())));
        }


        builder8.append("******Visibility Graph with A-Star******");
        builder9.append("******Visibility Graph with A-Star******");

        AStarPath aStarPath = new AStarPath(visibilityGraph);
        aStarPath.execute(points.get(0),points.get(1));


        System.out.println("******Visibility Graph with 'A-Star'******");
        time = 0d;
        totalDistance = 0d;
        int s = 1;
        deviceCounter = 0;
        area = 0d;
        for (DijkstraPoint beacon: beacons) {
            int devicesOnHotspot = hotspots.get(s-1).getDevicesInReach();
            deviceCounter = deviceCounter + devicesOnHotspot;
            LinkedList<DijkstraPoint> solutionPath = aStarPath.getPath(points.get(Integer.valueOf(beacon.getId())));
            Double deltaTime = computeTime(solutionPath);
            time = time + deltaTime;
            time = time + (((Math.random() * (5) + 5))  * devicesOnHotspot);
            Double deltaDistance = computeDistance(solutionPath);
            totalDistance = totalDistance + deltaDistance;
            batteryCharge = 100 - 0.194 * time;
            //area = 11.75 * beacon.getX();
            area = ((21.5+(21.5-((17/300)*beacon.getX())))*beacon.getX())/2;
            System.out.println("Beacon number "+Integer.toString(deviceCounter)+" configured after " + Double.toString(time) + "s with total travel distance of " + Double.toString(totalDistance) + " meter and remaining battery charge of " + Double.toString(batteryCharge));
            builder.append(Integer.toString(deviceCounter)+";");
            builder.append(Double.toString(time)+";");
            builder.append(Double.toString(totalDistance)+";");
            builder.append(Double.toString(area)+";");
            builder.append(Double.toString(batteryCharge));
            builder.append('\n');
            s++;
            DijkstraPoint goal = points.getOrDefault(Integer.valueOf(beacon.getId())+1,null);
            aStarPath.execute(points.get(Integer.valueOf(beacon.getId())),goal);
        }


        samplingGraph = new SamplingGraph(data,new Point(0,1.0,6.0));
        DijkstraGraph sampledDijkstraGraph = new DijkstraGraph(samplingGraph);
        dijkstraPath = new DijkstraPath(sampledDijkstraGraph);
        HashMap<Integer,DijkstraPoint> points = sampledDijkstraGraph.getPoints();
        beacons = sampledDijkstraGraph.getBeacons();
        dijkstraPath.execute(points.get(0));

        builder8.append("******Sampling Graph with Dijkstra******");
        builder9.append("******Sampling Graph with Dijkstra******");

        System.out.println("******Sampling Graph with Dijkstra******");
        time = 0d;
        totalDistance = 0d;
        int j = 1;
        deviceCounter = 0;
        area = 0d;
        for (DijkstraPoint beacon: beacons) {
            int devicesOnHotspot = hotspots.get(j-1).getDevicesInReach();
            deviceCounter = deviceCounter + devicesOnHotspot;
            LinkedList<DijkstraPoint> solutionPath = dijkstraPath.getPath(points.get(Integer.valueOf(beacon.getId())));
            Double deltaTime = computeTime(solutionPath);
            time = time + deltaTime;
            time = time + (((Math.random() * (5) + 5))  * devicesOnHotspot);
            Double deltaDistance = computeDistance(solutionPath);
            totalDistance = totalDistance + deltaDistance;
            batteryCharge = 100 - 0.194 * time;
            //area = 11.75 * beacon.getX();
            area = ((21.5+(21.5-((17/300)*beacon.getX())))*beacon.getX())/2;
            System.out.println("Beacon number "+Integer.toString(deviceCounter)+" configured after " + Double.toString(time) + "s with total travel distance of " + Double.toString(totalDistance) + " meter and remaining battery charge of " + Double.toString(batteryCharge));
            builder.append(Integer.toString(deviceCounter)+";");
            builder.append(Double.toString(time)+";");
            builder.append(Double.toString(totalDistance)+";");
            builder.append(Double.toString(area)+";");
            builder.append(Double.toString(batteryCharge));
            builder.append('\n');
            j++;
            dijkstraPath.execute(points.get(Integer.valueOf(beacon.getId())));
        }
        builder8.append("******Sampling Graph with A-Star******");
        builder9.append("******Sampling Graph with A-Star******");

        aStarPath = new AStarPath(sampledDijkstraGraph);
        aStarPath.execute(points.get(0),points.get(1));


        System.out.println("******Sampling Graph with 'A-Star'******");
        time = 0d;
        totalDistance = 0d;
        int k = 1;
        deviceCounter = 0;
        area = 0d;
        for (DijkstraPoint beacon: beacons) {
            int devicesOnHotspot = hotspots.get(k-1).getDevicesInReach();
            deviceCounter = deviceCounter + devicesOnHotspot;
            LinkedList<DijkstraPoint> solutionPath = aStarPath.getPath(points.get(Integer.valueOf(beacon.getId())));
            Double deltaTime = computeTime(solutionPath);
            time = time + deltaTime;
            time = time + (((Math.random() * (5) + 5))  * devicesOnHotspot);
            Double deltaDistance = computeDistance(solutionPath);
            totalDistance = totalDistance + deltaDistance;
            batteryCharge = 100 - 0.194 * time;
            //area = 11.75 * beacon.getX();
            area = ((21.5+(21.5-((17/300)*beacon.getX())))*beacon.getX())/2;
            System.out.println("Beacon number "+Integer.toString(deviceCounter)+" configured after " + Double.toString(time) + "s with total travel distance of " + Double.toString(totalDistance) + " meter and remaining battery charge of " + Double.toString(batteryCharge));
            builder.append(Integer.toString(deviceCounter)+";");
            builder.append(Double.toString(time)+";");
            builder.append(Double.toString(totalDistance)+";");
            builder.append(Double.toString(area)+";");
            builder.append(Double.toString(batteryCharge));
            builder.append('\n');
            k++;
            DijkstraPoint goal = points.getOrDefault(Integer.valueOf(beacon.getId())+1,null);
            aStarPath.execute(points.get(Integer.valueOf(beacon.getId())),goal);
        }


        //close writer

        pw.write(builder.toString());
        pw.close();

        pw1.write(builder1.toString());
        pw1.close();

        pw2.write(builder2.toString());
        pw2.close();

        pw8.write(builder8.toString());
        pw8.close();

        pw9.write(builder9.toString());
        pw9.close();


    }

    private static ArrayList<HotSpot> computeHotSpots(List<DijkstraPoint> beacons) {
        ArrayList<HotSpot> result = new ArrayList<>();
        ArrayList<HotSpot> hotspots = new ArrayList<>();
        ArrayList<Circle> circles = new ArrayList<Circle>();

        PrintWriter pw2 = null;
        try {
            pw2 = new PrintWriter(new File("xbluetooth.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder2 = new StringBuilder();

        PrintWriter pw3 = null;
        try {
            pw3 = new PrintWriter(new File("ybluetooth.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder3 = new StringBuilder();

        PrintWriter pw4 = null;
        try {
            pw4 = new PrintWriter(new File("ranges_bluetooth.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder4 = new StringBuilder();

        PrintWriter pw5 = null;
        try {
            pw5 = new PrintWriter(new File("xwifi.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder5 = new StringBuilder();

        PrintWriter pw6 = null;
        try {
            pw6 = new PrintWriter(new File("ywifi.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder6 = new StringBuilder();

        PrintWriter pw7 = null;
        try {
            pw7 = new PrintWriter(new File("ranges_wifi.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder7 = new StringBuilder();


        builder2.append("[ ");
        builder3.append("[ ");
        builder4.append("[ ");

        builder5.append("[ ");
        builder6.append("[ ");
        builder7.append("[ ");

       for (DijkstraPoint beacon : beacons) {
           Double range = beacon.getRange();
           circles.add(new Circle(range,beacon.getX(),beacon.getY()));
           if  (range < 7) {
               builder2.append(beacon.getX()+", ");
               builder3.append(beacon.getY()+", ");
               builder4.append(range+", ");
           }
           else {
               builder5.append(beacon.getX()+", ");
               builder6.append(beacon.getY()+", ");
               builder7.append(range+", ");
           }
        }

        builder2.append(" ]");
        builder3.append(" ]");
        builder4.append(" ]");

        builder5.append(" ]");
        builder6.append(" ]");
        builder7.append(" ]");

        pw2.write(builder2.toString());
        pw2.close();
        pw3.write(builder3.toString());
        pw3.close();
        pw4.write(builder4.toString());
        pw4.close();
        pw5.write(builder5.toString());
        pw5.close();
        pw6.write(builder6.toString());
        pw6.close();
        pw7.write(builder7.toString());
        pw7.close();

        for (int i = 0; i < circles.size(); i++) {
            for (int j = i + 1; j < circles.size(); j++) {
                Point temp = circleIntersection(circles.get(i),circles.get(j));
                if (temp != null){
                    HotSpot hotSpot = new HotSpot(999,temp.getX(),temp.getY());
                    hotSpot.addParent(i);
                    hotSpot.addParent(j);
                    hotspots.add(hotSpot);
                }
            }
        }

        //remove doubles and hotspots which can be used for the same devices
        for (int i = 0; i < hotspots.size(); i++) {
            for (int j = i + 1; j < hotspots.size(); j++) {
                Double x1 = hotspots.get(i).getLocationx1();
                Double y1 = hotspots.get(i).getLocationy1();
                Double x2 = hotspots.get(j).getLocationx1();
                Double y2 = hotspots.get(j).getLocationy1();
                Double d = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));

                if (d < 4) {
                    HotSpot tempHotspot = hotspots.get(j);
                    for (int parent: tempHotspot.getParentDevices()){
                        hotspots.get(i).addParent(parent);
                    }

                    hotspots.remove(j);
                }
            }
        }

        ArrayList<Integer> tmp = new ArrayList<Integer>();
        //generate result
        for (int i = 0; i<beacons.size(); i++) {
            if (tmp.contains(i)){
                continue;
            }
            for (HotSpot hotspot: hotspots){
                if (hotspot.getParentDevices().contains(i)) {
                    int devicesInReach = 0;
                    for (int devicesToBeConfigured: hotspot.getParentDevices()){
                        if (devicesToBeConfigured >= i && (!tmp.contains(devicesToBeConfigured))){
                            devicesInReach++;
                        }
                    }

                    result.add(new HotSpot(devicesInReach,hotspot.getLocationx1(),hotspot.getLocationy1()));

                    for (int j : hotspot.getParentDevices()) {
                        if (!tmp.contains(j)){
                            tmp.add(j);
                        }
                    }

                    hotspots.remove(hotspot);
                    break;
                }
            }

            // no hotspot available
            if (!tmp.contains(i)) {
                result.add(new HotSpot(1,beacons.get(i).getX(),beacons.get(i).getY()));
            }

        }

        return result;
    }

    private static Point circleIntersection(Circle circle1, Circle circle2) {
        Double x1 = circle1.getLocationx1();
        Double y1 = circle1.getLocationy1();
        Double x2 = circle2.getLocationx1();
        Double y2 =circle2.getLocationy1();
        Double d = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
        Double r1 = circle1.getRadius();
        Double r2 = circle2.getRadius();

        // too far or circles overlap completely
        if ((d <= Math.abs(r1 - r2)) || (d >= (r1 + r2))) {
            return null;
        }

        Double a = (r1 * r1 - r2 * r2 + d * d) / (2 * d);
        Double h = Math.sqrt(r1 * r1 - a * a);
        Double x0 = x1 + a * (x2 - x1) / d;
        Double y0 = y1 + a * (y2 - y1) / d;
        Double rx = -(y2 - y1) * (h / d);
        Double ry = -(x2 - x1) * (h / d);

        Double resX1 = x0 + rx;
        Double resY1 = y0 - ry;
        Double resX2 = x0 - rx;
        Double resY2 = y0 + ry;

        if (resX1 < 0) {
            resX1 = 0d;
        }

        if (resY1 < 0) {
            resY1 = 0d;
        }

        if (resX2 < 0) {
            resX2 = 0d;
        }

        if (resY2 < 0) {
            resY2 = 0d;
        }


        if (resX1 > 48) {
            resX1 = 47.8;
        }

        if (resY1 > 11) {
            resY1 = 10.93;
        }

        if (resX2 > 48) {
            resX2 = 47.8;
        }

        if (resY2 > 11) {
            resY2 = 10.93;
        }

        Double centerOfGravityX = 24d;
        Double centerOfGravityY = 5.875;

/*
        if (resX1 > 150) {
            resX1 = 149.3645698745;
        }

        if (resY1 > 13) {
            resY1 = 12.675477664;
        }

        if (resX2 > 150) {
            resX2 = 149.23463467;
        }

        if (resY2 > 13) {
            resY2 = 12.675456464;
        }

        Double centerOfGravityX = 75d;
        Double centerOfGravityY = 8d;
*/

        Double candidate1 = sqrt(pow(resX1 - centerOfGravityX, 2) + pow(resY1 - centerOfGravityY, 2));
        Double candidate2 = sqrt(pow(resX2 - centerOfGravityX, 2) + pow(resY2 - centerOfGravityY, 2));

        if (candidate1 <= candidate2){
            return new Point(99999999,resX1,resY1);
        }
        else{
            return new Point(99999999,resX2,resY2);
        }
    }

    private static Double computeTime(LinkedList<DijkstraPoint> solutionPath) {
        Double time = 0d;
        if (solutionPath != null) {
            Double x1 = solutionPath.getFirst().getX();
            Double y1 = solutionPath.getFirst().getY();

            Iterator<DijkstraPoint> iterator = solutionPath.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                DijkstraPoint point = iterator.next();
                Double x2 = point.getX();
                Double y2 = point.getY();
                Double distance = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
                time = time + (distance / 0.902); //Halle 1.4846m/s Tight Environment 0.902m/s
                x1 = x2;
                y1 = y2;
            }
            // random configuration time from 5-15 seconds
            time = time + ((Math.random() * (4) + 4));
        }
        return time;
    }

    private static Double computeDistance(LinkedList<DijkstraPoint> solutionPath) {
        Double distance = 0d;
        if (solutionPath != null) {
            Double x1 = solutionPath.getFirst().getX();
            Double y1 = solutionPath.getFirst().getY();

            Iterator<DijkstraPoint> iterator = solutionPath.iterator();
            iterator.next();
            while (iterator.hasNext()) {
                DijkstraPoint point = iterator.next();
                Double x2 = point.getX();
                Double y2 = point.getY();
                Double deltaDistance = sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2));
                distance = distance + deltaDistance;
                builder8.append(Double.toString(x1)+", ");
                builder9.append(Double.toString(y1)+", ");
                System.out.println("Travelled across following coordinates: "+x1+" "+y1);
                x1 = x2;
                y1 = y2;
            }
            System.out.println("Travelled across following coordinates: "+x1+" "+y1);
            builder8.append(Double.toString(x1)+", ");
            builder9.append(Double.toString(y1)+", ");
        }
        return distance;
    }

    private static Connection connect() {
        // SQLite connection string
        File dbfile=new File("");
        String url="jdbc:sqlite:C:\\Users\\Jan\\Desktop\\evaluation\\pathplanning\\versions\\2\\sparse.db";
        System.out.println(url);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }


    private static ArrayList<MapEntry> loadHotSpotsIn(ArrayList<MapEntry> data, ArrayList<HotSpot> hotspots) {
        ArrayList<MapEntry> result = new ArrayList<MapEntry>();
        for (MapEntry mapData : data) {
                if (mapData.getClassCategory() == 1){
                    result.add(mapData);
                }
            }

        for (HotSpot hotspot : hotspots) {
            result.add(new MapEntry(3,hotspot.getLocationx1(),hotspot.getLocationy1(),0d,0d,"", 0d));
        }
            return  result;
    }

    private static ArrayList<MapEntry> loadMapData() {
        String sql = "SELECT * FROM map";
        ArrayList<MapEntry> mapEntries = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt  = conn.prepareStatement(sql)){
            //
            ResultSet result  = stmt.executeQuery();
            MapEntry entry = null;

            System.out.println("Loading Data");
            // loop through the result set
            while (result.next()) {
                entry = new MapEntry(result.getInt("class"),result.getDouble("locationx1"),
                        result.getDouble("locationy1"),result.getDouble("locationx2"),
                        result.getDouble("locationy2"), result.getString("mac"), result.getDouble("range"));
                mapEntries.add(entry);

                System.out.println(result.getInt("id") +  "\t" +
                        result.getInt("class") + "\t" +
                        result.getDouble("locationx1") + "\t" +
                        result.getDouble("locationy1") + "\t" +
                        result.getDouble("locationx2") + "\t" +
                        result.getDouble("locationy2") + "\t" +
                        result.getString("mac") + "\t" +
                        result.getDouble("range"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return mapEntries;
    }
}
