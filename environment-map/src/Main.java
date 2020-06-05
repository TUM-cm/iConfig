import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Main{
    private static final String TAG = "MainActivity";

    //declare storage for the scan values of drone
    static ArrayList<Point> flightPoints = new ArrayList<Point>();
    static ArrayList<Point> obstacleInteriorPoints = new ArrayList<Point>();
    static ArrayList<Point> obstacleBorderPoints = new ArrayList<Point>();
    static ArrayList<Point> minimumBeaconPoints = new ArrayList<Point>();
    static ArrayList<Point> minimumWifiPoints = new ArrayList<Point>();
    static ArrayList<Line> borderPoints = new ArrayList<>();
    static ArrayList<LogEntry> convexPoints;
    static ArrayList<LogEntry> rectangleHull;
    static ArrayList<String> beaconAlreadySaved = new ArrayList<String>();
    static Integer datasetLimitBT;
    static Integer datasetLimitWiFi;
    static Integer datasetStartBT;
    static Integer datasetStartWiFi;


    public static void main(String[] args) {
        List<LogEntry> logEntries = loadDroneLog();
        createDataPoints(logEntries);
        List<RaspberryEntry> raspberryLog = loadRaspberryLog(logEntries.get(0).getTimestamp().get(Calendar.DAY_OF_MONTH),
                logEntries.get(0).getTimestamp().get(Calendar.MONTH),logEntries.get(0).getTimestamp().get(Calendar.YEAR));
        createWiFiPoints(raspberryLog,logEntries);
        drawBorder(convexPoints);
        //drawBorder(rectangleHull);
        filterBeacons();
        filterWiFis();
        writeReferenceFile();
        writeToTXT();
    }

    private static void filterBeacons() {
        ListIterator<Point> iter = minimumBeaconPoints.listIterator();
        while(iter.hasNext()){
            String mac = iter.next().getInfo();
            if(mac.equals("CA:F7:55:BB:61:DD") || mac.equals("C9:ED:20:D2:6A:5F") || mac.equals("CC:26:77:B5:5A:BD") || mac.equals("EB:26:C9:E4:DC:5B")){
                System.out.println("beacon found");
            }
            else {
                iter.remove();
            }
        }
    }

    private static void filterWiFis() {
        ListIterator<Point> iter = minimumWifiPoints.listIterator();
        while(iter.hasNext()){
            String mac = iter.next().getInfo();
            if(mac.equals("b8:27:eb:1b:54:d5") || mac.equals("b8:27:eb:9b:ff:22") || mac.equals("b8:27:eb:37:08:29") || mac.equals("b8:27:eb:34:ef:c6") || mac.equals("b8:27:eb:2f:33:9d") || mac.equals("b8:27:eb:2b:65:ed")){
                System.out.println("Wi-Fi device found");
            }
            else {
                iter.remove();
            }
        }
    }

    private static void writeReferenceFile() {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("reference.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();


        builder.append("Wi-Fi devices");
        builder.append("\n");

        for (Point p : minimumWifiPoints) {
            builder.append(p.getX() + " " + p.getY() + " " + p.getInfo());
            builder.append("\n");
        }

        builder.append("BT devices");
        builder.append("\n");

        for (Point p : minimumBeaconPoints) {
            builder.append(p.getX() + " " + p.getY() + " " + p.getInfo());
            builder.append("\n");
        }

        builder.append("\n");
        pw.write(builder.toString());
        pw.close();
    }

    private static void writeToTXT() {

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("processedmap.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder();


        builder.append("wifix=[");
        String prefix = "";
        for (Point p : minimumWifiPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append(p.getX());
        }
        builder.append("]");
        builder.append("\n");

        builder.append("wifiy=[");
        prefix = "";
        for (Point p : minimumWifiPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append(p.getY());
        }
        builder.append("]");
        builder.append("\n");

        builder.append("annotationwifi=[");
        prefix = "";
        for (Point p : minimumWifiPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append("'"+p.getInfo()+"'");
        }
        builder.append("]");
        builder.append("\n");

        builder.append("bluetoothx=[");
        prefix = "";
        for (Point p : minimumBeaconPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append(p.getX());
        }
        builder.append("]");
        builder.append("\n");

        builder.append("bluetoothy=[");
        prefix = "";
        for (Point p : minimumBeaconPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append(p.getY());
        }
        builder.append("]");
        builder.append("\n");

        builder.append("annotationbluetooth=[");
        prefix = "";
        for (Point p : minimumBeaconPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append("'"+p.getInfo()+"'");
        }
        builder.append("]");
        builder.append("\n");

        builder.append("border=[");
        prefix = "";
        for (Line l : borderPoints) {
            builder.append(prefix);
            prefix = ", ";
            builder.append("["+l.getX1()+", "+l.getY1()+", "+l.getX2()+", "+l.getY2()+"]");
        }
        builder.append("]");
        builder.append("\n");
        pw.write(builder.toString());
        pw.close();
    }

    private static void createWiFiPoints(List<RaspberryEntry> raspberryLog, List<LogEntry> logEntries) {
        ArrayList<RaspberryEntry> list = new ArrayList<RaspberryEntry>();
        HashMap<String, Integer> minimumList = getMinimumWiFiEntries(raspberryLog);
        for (String device: minimumList.keySet()){
            int rssi = minimumList.get(device);
            for (RaspberryEntry raspberryEntry: raspberryLog){
                if ((raspberryEntry.getRssi() == rssi) && (raspberryEntry.getBssid().equals(device)) && (rssi < 45)) {
                    ArrayList<Double> position = findPoisition(raspberryEntry.getTimestamp(),logEntries);
                    raspberryEntry.setX(position.get(0));
                    raspberryEntry.setY(position.get(1));
                    list.add(raspberryEntry);
                    break;
                }
            }
        }

        //add data to graph set
        for (RaspberryEntry entry: list) {
            minimumWifiPoints.add(new Point(entry.getX(),entry.getY(), entry.getBssid()));
        }
    }

    private static ArrayList<Double> findPoisition(Calendar timestamp, List<LogEntry> logEntries) {
        long minimumTimeDifference = 999999999;
        ArrayList<Double> position = new ArrayList<Double>();
        position.add(0,0.0);
        position.add(1,0.0);
        for (LogEntry logEntry: logEntries){
            long timeDifference;
            if (logEntry.getTimestamp().getTime().getTime() > timestamp.getTime().getTime()){
                timeDifference = logEntry.getTimestamp().getTime().getTime() - timestamp.getTime().getTime();
            }
            else {
                timeDifference = timestamp.getTime().getTime()- logEntry.getTimestamp().getTime().getTime();
            }
            if (timeDifference < minimumTimeDifference) {
                position.set(0,logEntry.getLocationx());
                position.set(1,logEntry.getLocationy());
                minimumTimeDifference = timeDifference;
            }
        }

        return position;
    }

    private static void createDataPoints(List<LogEntry> logEntries) {
        HashMap<String, Integer> minimumBeaconList;
        minimumBeaconList = getMinimumBeaconList(logEntries);
        ArrayList<LogEntry> obstaclePointsApriori = getObstaclePointsApriori(logEntries);
        convexPoints = findConvexHull(obstaclePointsApriori);
        rectangleHull = findMaximumRectangle(convexPoints);
        for(LogEntry logEntry: logEntries) {
            if (logEntry.getObstacle() == 1) {
                if (convexPoints.contains(logEntry)){
                    obstacleBorderPoints.add(new Point(logEntry.getLocationx(),logEntry.getLocationy()));
                }
                else {
                    obstacleInteriorPoints.add(new Point(logEntry.getLocationx(),logEntry.getLocationy()));
                    //*************************************************************************************dbExporter.addPoint(2,logEntry.getLocationx(),logEntry.getLocationy(),0d,0d,"");
                }
            }
            else {
                if (logEntry.getBeaconmac().equals("no beacon found")){
                    flightPoints.add(new Point(logEntry.getLocationx(),logEntry.getLocationy()));
                }
                else{
                    //check if position is a position with lowest rssi to a beacon and not noise (rssi<80)
                    if ((minimumBeaconList.get(logEntry.getBeaconmac()) == logEntry.getRssi())&&(logEntry.getRssi() < 80) && !(beaconAlreadySaved.contains(logEntry.getBeaconmac()))){
                        beaconAlreadySaved.add(logEntry.getBeaconmac());
                        minimumBeaconPoints.add(new Point(logEntry.getLocationx(),logEntry.getLocationy(), logEntry.getBeaconmac()));
                        //************************************************dbExporter.addPoint(3,logEntry.getLocationx(),logEntry.getLocationy(),0d,0d,logEntry.getBeaconmac());
                    }
                    else {
                        flightPoints.add(new Point(logEntry.getLocationx(),logEntry.getLocationy()));
                    }
                }}
        }
    }

    private static void drawBorder(ArrayList<LogEntry> convexPoints) {

        for (int i = 0; i < convexPoints.size() - 1; i++) {
            if (EuclideanDistance(new Point(convexPoints.get(i).getLocationx(),convexPoints.get(i).getLocationy()),
                    new Point(convexPoints.get(i+1).getLocationx(),convexPoints.get(i+1).getLocationy())) < 15) {
                if (convexPoints.get(i).getLocationx() < convexPoints.get(i+1).getLocationx()) {
                    borderPoints.add(new Line(convexPoints.get(i).getLocationx(), convexPoints.get(i).getLocationy(), convexPoints.get(i + 1).getLocationx(), convexPoints.get(i + 1).getLocationy()));
                }
                else {
                    borderPoints.add(new Line(convexPoints.get(i + 1).getLocationx(), convexPoints.get(i + 1).getLocationy(), convexPoints.get(i).getLocationx(), convexPoints.get(i).getLocationy()));
                }
                //***************differently**************************************************didbExporter.addPoint(1,dataPoint1.getX(),dataPoint1.getY(),dataPoint2.getX(),dataPoint2.getY(),"");
            }
        }
        //check last point with very first point as well
        if (EuclideanDistance(new Point(convexPoints.get(convexPoints.size() - 1).getLocationx(),convexPoints.get(convexPoints.size() - 1).getLocationy()),
                new Point(convexPoints.get(0).getLocationx(),convexPoints.get(0).getLocationy())) < 15){
            if (convexPoints.get(convexPoints.size() - 1).getLocationx() < convexPoints.get(0).getLocationx()) {
                borderPoints.add(new Line(convexPoints.get(convexPoints.size() - 1).getLocationx(), convexPoints.get(convexPoints.size() - 1).getLocationy(), convexPoints.get(0).getLocationx(), convexPoints.get(0).getLocationy()));
            }
            else {
                borderPoints.add(new Line(convexPoints.get(0).getLocationx(), convexPoints.get(0).getLocationy(), convexPoints.get(convexPoints.size() - 1).getLocationx(), convexPoints.get(convexPoints.size() - 1).getLocationy()));
            }
            //***************differently**************************************************dbExporter.addPoint(1,dataPoint1.getX(),dataPoint1.getY(),dataPoint2.getX(),dataPoint2.getY(),"");
        }
    }

    private static Double EuclideanDistance(Point point1, Point point2) {
        double x1 = point1.getX();
        double y1 = point1.getY();
        double x2 = point2.getX();
        double y2 = point2.getY();
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }


    private static ArrayList<LogEntry> findConvexHull(ArrayList<LogEntry> obstaclePointsApriori) {
        ArrayList<LogEntry> convexHull = new ArrayList<LogEntry>();
        if (obstaclePointsApriori.size() <3) {
            System.out.print("Less than 3 points in database");
        }

        int minPoint = -1, maxPoint = -1;
        Double minX = 999999d;
        Double maxX = -999999d;
        for(LogEntry logEntry: obstaclePointsApriori) {

            if (logEntry.getLocationx() < minX)
            {
                minX = logEntry.getLocationx();
                minPoint = obstaclePointsApriori.indexOf(logEntry);
            }

            if (logEntry.getLocationx() > maxX)
            {
                maxX = logEntry.getLocationx();
                maxPoint = obstaclePointsApriori.indexOf(logEntry);
            }
        }
        Point A = new Point(obstaclePointsApriori.get(minPoint).getLocationx(),obstaclePointsApriori.get(minPoint).getLocationy());
        Point B = new Point(obstaclePointsApriori.get(maxPoint).getLocationx(),obstaclePointsApriori.get(maxPoint).getLocationy());
        LogEntry logEntryA = obstaclePointsApriori.get(minPoint);
        LogEntry logEntryB = obstaclePointsApriori.get(maxPoint);
        convexHull.add(logEntryA);
        convexHull.add(logEntryB);
        obstaclePointsApriori.remove(logEntryA);
        obstaclePointsApriori.remove(logEntryB);

        ArrayList<LogEntry> leftSite = new ArrayList<LogEntry>();
        ArrayList<LogEntry> rightSite = new ArrayList<LogEntry>();


        for(LogEntry logEntry: obstaclePointsApriori)
        {
            Point p = new Point(logEntry.getLocationx(),logEntry.getLocationy());
            if (locationOfPoint(A, B, p) == -1)
                leftSite.add(logEntry);
            else if (locationOfPoint(A, B, p) == 1)
                rightSite.add(logEntry);
        }
        checkHullSet(logEntryA, logEntryB, rightSite, convexHull);
        checkHullSet(logEntryB, logEntryA, leftSite, convexHull);

        return convexHull;
    }

    private static void checkHullSet(LogEntry A, LogEntry B, ArrayList<LogEntry> set, ArrayList<LogEntry> convexHull) {
        int insertPosition = convexHull.indexOf(B);
        Point a = new Point(A.getLocationx(),A.getLocationy());
        Point b = new Point(B.getLocationx(),B.getLocationy());

        if (set.size() == 0)
            return;

        if (set.size() == 1)
        {
            LogEntry P = set.get(0);
            set.remove(P);
            convexHull.add(insertPosition, P);
            return;
        }

        Double dist = -99999d;
        int furthestPoint = -1;
        for (int i = 0; i < set.size(); i++)
        {
            LogEntry P = set.get(i);
            Point p = new Point(P.getLocationx(),P.getLocationy());
            Double distance = distance(a, b, p);
            if (distance > dist)
            {
                dist = distance;
                furthestPoint = i;
            }
        }

        LogEntry P = set.get(furthestPoint);
        Point p = new Point(P.getLocationx(), P.getLocationy());
        set.remove(furthestPoint);
        convexHull.add(insertPosition, P);


        ArrayList<LogEntry> leftSetPointsA = new ArrayList<LogEntry>();
        for (int i = 0; i < set.size(); i++)
        {
            Point m = new Point(set.get(i).getLocationx(),set.get(i).getLocationy());
            if (locationOfPoint(a, p, m) == 1)
            {
                leftSetPointsA.add(set.get(i));
            }
        }


        ArrayList<LogEntry> leftSetPointsB = new ArrayList<LogEntry>();
        for (int i = 0; i < set.size(); i++)
        {
            Point m = new Point(set.get(i).getLocationx(),set.get(i).getLocationy());
            if (locationOfPoint(p, b, m) == 1)
            {
                leftSetPointsB.add(set.get(i));
            }
        }
        checkHullSet(A, P, leftSetPointsA, convexHull);
        checkHullSet(P, B, leftSetPointsB, convexHull);
    }

    public static int locationOfPoint(Point A, Point B, Point P) {
        Double temp = (B.x - A.x) * (P.y - A.y) - (B.y - A.y) * (P.x - A.x);
        if (temp > 0)
            return 1;
        else if (temp == 0)
            return 0;
        else
            return -1;
    }

    public static Double distance(Point A, Point B, Point C) {
        Double ABx = B.x - A.x;
        Double ABy = B.y - A.y;
        Double num = ABx * (A.y - C.y) - ABy * (A.x - C.x);
        if (num < 0)
        {
            num = -num;
        }
        return num;
    }

    private static ArrayList<LogEntry> getObstaclePointsApriori(List<LogEntry> logEntriesSorted) {
        ArrayList<LogEntry> points = new ArrayList<LogEntry>();
        for(LogEntry logEntry: logEntriesSorted) {
            if (logEntry.getObstacle() == 1) {
                points.add(logEntry);
            }
        }
        return points;
    }

    private static HashMap<String,Integer> getMinimumWiFiEntries(List<RaspberryEntry> raspberryEntries) {
        HashMap<String, Integer> minimumList = new HashMap<String, Integer>();
        for(RaspberryEntry raspberryEntry: raspberryEntries) {
            Integer rssi = raspberryEntry.getRssi();
            String bssid = raspberryEntry.getBssid();
            if (minimumList.containsKey(bssid)){
                //check if hashmap has smallest beacon rssi saved already
                if (rssi < minimumList.get(bssid)){
                    minimumList.put(bssid,rssi);
                }
            }
            else{
                minimumList.put(bssid,rssi);
            }
        }
        return minimumList;
    }

    private static HashMap<String,Integer> getMinimumBeaconList(List<LogEntry> logEntries) {
        HashMap<String, Integer> minimumBeaconList = new HashMap<String, Integer>();
        for(LogEntry logEntry: logEntries) {
            Integer rssi = logEntry.getRssi();
            String beaconMac = logEntry.getBeaconmac();
            if (rssi == 9999){
                continue;
            }
            if (minimumBeaconList.containsKey(beaconMac)){
                //check if hashmap has smallest beacon rssi saved already
                if (rssi < minimumBeaconList.get(beaconMac)){
                    minimumBeaconList.put(beaconMac,rssi);
                }
            }
            else{
                minimumBeaconList.put(beaconMac,rssi);
            }
        }
        return minimumBeaconList;
    }


    private static Connection connect() {
        // SQLite connection string
        File dbfile=new File("");
        String url="jdbc:sqlite:C:\\Users\\Jan\\Desktop\\evaluation\\final\\ituepferl\\random.db";
        datasetStartBT = 0;
        datasetLimitBT = 999999;
        datasetStartWiFi = 0;
        datasetLimitWiFi = 999999;
        System.out.println(url);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static List<LogEntry> loadDroneLog() {
        String sql = "SELECT * FROM flight";
        List<LogEntry> logSeries = new LinkedList<LogEntry>();

        try (Connection conn = connect();
             PreparedStatement stmt  = conn.prepareStatement(sql)){
            //
            ResultSet result  = stmt.executeQuery();
            LogEntry entry = null;

            System.out.println("Loading Data");
            // loop through the result set
            int counter = 0;
            while (result.next()) {
                entry = new LogEntry();
                entry.setId(result.getInt("id"));
                entry.setLocationx(result.getDouble("locationx"));
                entry.setLocationy(result.getDouble("locationy"));
                entry.setObstacle(result.getInt("obstacle"));
                entry.setRssi(result.getInt("rssi"));
                entry.setBeaconmac(result.getString("beaconmac"));
                try {
                    Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",Locale.US).parse(result.getString("timestamp"));
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    entry.setTimestamp(c);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if ((counter < datasetLimitBT)&&(counter >= datasetStartBT)) {
                    logSeries.add(entry);
                }
                counter++;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return logSeries;
    }


    private static List<RaspberryEntry> loadRaspberryLog(int day, int month, int year) {
        String sql = "SELECT * FROM raspberry";
        List<RaspberryEntry> raspberrySeries = new LinkedList<RaspberryEntry>();

        try (Connection conn = connect();
             PreparedStatement stmt  = conn.prepareStatement(sql)){
            //
            ResultSet result  = stmt.executeQuery();
            RaspberryEntry entry = null;
            System.out.println("Loading Data");
            // loop through the result set
            int counter = 0;
            while (result.next()) {
                entry = new RaspberryEntry();
                entry.setId(result.getInt("id"));
                try {
                    Date date = new SimpleDateFormat("HH:mm:ss").parse(result.getString("time"));
                    Calendar c = Calendar.getInstance();
                    c.setTime(date);
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH,day);
                    entry.setTimestamp(c);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                entry.setRssi(result.getInt("RSSI"));
                entry.setBssid(result.getString("BSSID"));
                if ((counter < datasetLimitWiFi)&&(counter >= datasetStartWiFi)){
                raspberrySeries.add(entry);
                }
                counter++;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return raspberrySeries;
    }

    private static ArrayList<LogEntry> findMaximumRectangle(ArrayList<LogEntry> convexHull) {
        ArrayList<LogEntry> rectangleHull = new ArrayList<LogEntry>();
        Double minX = 999999d;
        Double maxX = -999999d;
        Double minY = 999999d;
        Double maxY = -999999d;
        for(LogEntry logEntry: convexHull) {
            Double x = logEntry.getLocationx();
            Double y = logEntry.getLocationy();

            if (x < minX)
            {
                minX = x;
            }

            if (x > maxX)
            {
                maxX = x;
            }

            if (y < minY)
            {
                minY = y;
            }

            if (y > maxY)
            {
                maxY = y;
            }
        }
        LogEntry A = new LogEntry(minX,minY);
        LogEntry B = new LogEntry(minX,maxY);
        LogEntry C = new LogEntry(maxX,maxY);
        LogEntry D = new LogEntry(maxX,minY);

        rectangleHull.add(A);
        rectangleHull.add(B);
        rectangleHull.add(C);
        rectangleHull.add(D);

        return rectangleHull;
    }



}
