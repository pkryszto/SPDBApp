import org.jxmapviewer.viewer.GeoPosition;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryExecuter {

    private Connection connection;
    private Connection locationConnection = null;
    private int sessionNumber;

    public QueryExecuter() {
        // Setup for spatial database connection
        String url = "jdbc:postgresql://spdb.ckvqlgxx5bxn.us-east-2.rds.amazonaws.com/SPDBPolska?user=postgres&password=Lofciamspdb1";
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // Setup for databse with names of locations connection
        url = "jdbc:postgresql://spdb.ckvqlgxx5bxn.us-east-2.rds.amazonaws.com/SPDBLocations?user=postgres&password=Lofciamspdb1";
        try {
            locationConnection = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setSessionNumber(int number) {
        sessionNumber = number;
    }

    public Route findRoute(ArrayList<GeoPosition> points) throws SQLException {
        // Find route between two locations - starting location and destination. Does not include POI.
        ArrayList<ArrayList<GeoPosition>> route = new ArrayList<>();
        double distance = 0;
        double time = 0;
        double startLat = points.get(0).getLatitude();
        double starLng = points.get(0).getLongitude();

        double endLat = points.get(1).getLatitude();
        double endLng = points.get(1).getLongitude();

        //Set distance factor
        double separation = calculateDistance(points.get(0), points.get(1));
        System.out.println(separation);
        int distanceFactor;
        if (separation <= 100) distanceFactor = 1;
        if (separation > 100 && separation <= 200) distanceFactor = 2;
        else if (separation > 200 && separation <= 300) distanceFactor = 3;
        else if (separation > 300 && separation <= 400) distanceFactor = 4;
        else distanceFactor = 6;

        Statement stmt = connection.createStatement();

        stmt.executeUpdate(
                "DROP TABLE IF EXISTS   WAYS_" + sessionNumber + "; CREATE TABLE WAYS_" + sessionNumber + " AS (SELECT * from routing2\n" +
                        "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + starLng + "," + startLat + "), 4326)\n" +
                        "LIMIT 15000)\n" +
                        "UNION (SELECT * from routing2\n" +
                        "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + endLng + "," + endLat + "), 4326)\n" +
                        "LIMIT 15000)\n" +
                        "UNION (select * from fast_ways\n" +
                        "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + (starLng + endLng) / 2 + "," + (startLat + endLat) / 2 + "), 4326)\n" +
                        "LIMIT " + 7000 * distanceFactor + ")\n" +
                        "UNION (select * from fast_ways\n" +
                        "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + (starLng + (starLng + endLng) / 2) / 2 + "," + (startLat + (startLat + endLat) / 2) / 2 + "), 4326)\n" +
                        "LIMIT " + 7000 * distanceFactor + ")\n" +
                        "UNION (select * from fast_ways\n" +
                        "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + ((starLng + endLng) / 2 + endLng) / 2 + "," + ((startLat + endLat) / 2 + endLat) / 2 + "), 4326)\n" +
                        "LIMIT " + 7000 * distanceFactor + ")"
        );

        stmt.executeUpdate(
                "DROP TABLE IF EXISTS   WAYS_" + sessionNumber + "2;CREATE TABLE WAYS_" + sessionNumber + "2 AS " +
                        "(SELECT id, source, target, waypoints.cost, reverse_cost, x1, y1, x2, y2, geom_way,clazz,km,kmh FROM pgr_astar(\n" +
                        "    'select * from WAYS_" + sessionNumber +
                        "\t',\n" +
                        "    (SELECT source FROM ways \n" +
                        "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + starLng + "," + startLat + "), 4326)\n" +
                        "\t\tLIMIT 1),\n" +
                        "\t(SELECT source FROM ways \n" +
                        "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + endLng + "," + endLat + "), 4326)\n" +
                        "\t\tLIMIT 1),\n" +
                        "\ttrue\n" +
                        ") as waypoints\n" +
                        "JOIN ways rd ON waypoints.edge = rd.id);"
        );


        ResultSet rs = stmt.executeQuery("SELECT geom_way,ST_AsText(geom_way) as waypoint, km as distance, kmh as speed FROM WAYS_" + sessionNumber + "2;");


        while (rs.next()) {
            distance += rs.getDouble("distance");
            time += rs.getDouble("distance") / rs.getDouble("speed");

            String line = rs.getString("waypoint");

            line = line.replace("LINESTRING(", "");
            line = line.replace(")", "");


            String[] waypoints = line.split(",");

            ArrayList<GeoPosition> path = new ArrayList<>();

            for (String waypoint : waypoints) {
                String[] coordinates = waypoint.split(" ");
                path.add(new GeoPosition(Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[0])));

            }
            route.add(path);

        }

        return new Route(route, distance, (int) (time * 60));
    }

    public Route findFinalRoute(ArrayList<GeoPosition> points) throws SQLException {
        // Find route with collected all points on the way to the destination. Includes POI.
        ArrayList<ArrayList<GeoPosition>> route = new ArrayList<>();
        double distance = 0;
        double time = 0;
        double startLat = points.get(0).getLatitude();
        double starLng = points.get(0).getLongitude();

        double endLat = points.get(1).getLatitude();
        double endLng = points.get(1).getLongitude();

        Statement stmt = connection.createStatement();


        stmt.executeUpdate(
                "DROP TABLE IF EXISTS   WAYS_" + sessionNumber + "3;CREATE TABLE WAYS_" + sessionNumber + "3 AS " +
                        "(SELECT geom_way,ST_AsText(geom_way) as waypoint, km as distance, kmh as speed FROM pgr_astar(\n" +
                        "    'select * from WAYS_" + sessionNumber +
                        "\t',\n" +
                        "    (SELECT source FROM WAYS_" + sessionNumber + "\n" +
                        "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + starLng + "," + startLat + "), 4326)\n" +
                        "\t\tLIMIT 1),\n" +
                        "\t(SELECT source FROM WAYS_" + sessionNumber + "\n" +
                        "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(" + endLng + "," + endLat + "), 4326)\n" +
                        "\t\tLIMIT 1),\n" +
                        "\ttrue\n" +
                        ") as waypoints\n" +
                        "JOIN ways rd ON waypoints.edge = rd.id);"
        );

        ResultSet rs = stmt.executeQuery("SELECT geom_way,waypoint,distance,speed FROM WAYS_" + sessionNumber + "3;");

        while (rs.next()) {
            distance += rs.getDouble("distance");
            time += rs.getDouble("distance") / rs.getDouble("speed");

            String line = rs.getString("waypoint");

            line = line.replace("LINESTRING(", "");
            line = line.replace(")", "");


            String[] waypoints = line.split(",");

            ArrayList<GeoPosition> path = new ArrayList<>();

            for (String waypoint : waypoints) {
                String[] coordinates = waypoint.split(" ");
                path.add(new GeoPosition(Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[0])));

            }
            route.add(path);

        }

        return new Route(route, distance, (int) (time * 60));
    }


    public ArrayList<Poi> findPOIs(int lastDistance, int lastTime, int distancePOI, int minDistance, int timePOI, int minTime, String POICategory) throws SQLException {
        // Find list of matching POI
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT km as distance, kmh as speed, ST_AsText(geom_way) as waypoint  FROM WAYS_" + sessionNumber + "2;");
        double distance = 0;
        double time = 0;

        double distanceFromLastPoi = 0;
        double timeSinceLastPoi = 0;

        ArrayList<Poi> pois = new ArrayList<>();
        int createdPoints = 0;

        // Save found POI in list depending on number of added POI and distance or time to the POI.
        while (rs.next()) {
            distance += rs.getDouble("distance");
            time += rs.getDouble("distance") / rs.getDouble("speed");
            System.out.println(distance + " " + time);
            // The first POI should be further away than given distance and should be after given time.
            if (createdPoints == 0 && distance > minDistance && time > (((double) (minTime)) / 60)) {
                String line = rs.getString("waypoint");
                line = line.replace("LINESTRING(", "");
                line = line.replace(")", "");
                String[] waypoints = line.split(",");

                String[] coordinates = waypoints[0].split(" ");

                double searchLng = Double.parseDouble(coordinates[0]);
                double searchLat = Double.parseDouble(coordinates[1]);

                pois.add(createPoi(searchLng, searchLat, mapPOICategory(POICategory)));
                createdPoints++;


            } else if (createdPoints > 0) {
                // If this is further POI
                distanceFromLastPoi += rs.getDouble("distance");
                timeSinceLastPoi += rs.getDouble("distance") / rs.getDouble("speed");

                // Add POI if distance from last POI is longer than given distance and time from last POI is longer than given time
                if (distanceFromLastPoi > distancePOI && timeSinceLastPoi > (((double) (timePOI)) / 60)) {
                    String line = rs.getString("waypoint");
                    line = line.replace("LINESTRING(", "");
                    line = line.replace(")", "");
                    String[] waypoints = line.split(",");

                    String[] coordinates = waypoints[0].split(" ");

                    double searchLng = Double.parseDouble(coordinates[0]);
                    double searchLat = Double.parseDouble(coordinates[1]);

                    pois.add(createPoi(searchLng, searchLat, mapPOICategory(POICategory)));
                    createdPoints++;
                    distanceFromLastPoi = 0;
                    timeSinceLastPoi = 0;
                }

            }

        }

        if (createdPoints > 0 && distanceFromLastPoi < lastDistance && timeSinceLastPoi < lastTime) {
            pois.remove(createdPoints - 1);
        }
        System.out.println(pois);

        for (Poi poi : pois) {
            insertRoutesNearPoint(poi.location);
        }
        return pois;
    }

    private Poi createPoi(double searchLng, double searchLat, String POICategory) throws SQLException {
        // Get POI information from db based on geolocation and category.
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name, way,ST_AsText(way) as point FROM pois where amenity='" + POICategory + "'\n" +
                "\t\tORDER BY way <-> ST_SetSRID(ST_MakePoint(" + searchLng + ", " + searchLat + "), 4326)\n" +
                "\t\tLIMIT 1");
        while (rs.next()) {
            String name = rs.getString("name");
            String point = rs.getString("point");
            point = point.replace("POINT(", "");
            point = point.replace(")", "");
            String[] coordinates = point.split(" ");

            Poi poi = new Poi(new GeoPosition(Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[0])), name);
            System.out.println(poi);
            return poi;
        }
        return null;
    }

    private String mapPOICategory(String type) {
        return switch (type) {
            case "fuel" -> "Gas station";
            case "Gas station" -> "fuel";
            case "fast_food" -> "Fast Food";
            case "Fast Food" -> "fast_food";
            case "charging_station" -> "Charging station";
            case "Charging station" -> "charging_station";
            case "restaurant" -> "Restaurant";
            case "Restaurant" -> "restaurant";
            case "Toilet" -> "toilets";
            default -> "Toilet";
        };
    }

    public static ArrayList<String> getPOICategories() {
        ArrayList<String> categories = new ArrayList<String>(Arrays.asList(
                "Toilet",
                "Fast Food",
                "Charging station",
                "Restaurant",
                "Gas station"));
        return categories;
    }

    private static double calculateDistance(GeoPosition g1, GeoPosition g2) {
        // Calculate direct distance between two geolocations.

        double lat1 = g1.getLatitude();
        double lon1 = g1.getLongitude();
        double lat2 = g2.getLatitude();
        double lon2 = g2.getLongitude();

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }

    private void insertRoutesNearPoint(GeoPosition position) throws SQLException {
        // Add geolocation to statement
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO WAYS_"+sessionNumber+"2\n" +
                "                        SELECT * from routing2\n" +
                "                        ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint(?,?), 4326)\n" +
                "                        LIMIT 10000");
        stmt.setDouble(1, position.getLongitude());
        stmt.setDouble(2, position.getLatitude());
        stmt.executeUpdate();
    }


    public String getCityName(GeoPosition position) throws SQLException {
        // Returns name of the city/town/village/hamlet that is nearest given GeoPosition.
        PreparedStatement stmt = locationConnection.prepareStatement("""
                SELECT name FROM cities
                ORDER BY way <-> ST_SetSRID(ST_MakePoint(?,?), 4326)
                LIMIT 1""");
        stmt.setDouble(1, position.getLongitude());
        stmt.setDouble(2, position.getLatitude());
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getString("name");


    }

    public ArrayList<Address> findAddresses(String addressName) throws SQLException {
        // Set given string to title case. All locations in db are in title case.
        String capitAddressName = capitalize(addressName);
        // Get 10 matching locations
        PreparedStatement stmt = locationConnection.prepareStatement("""
                select name, ST_AsText(way) as point, place as category from cities
                where name like '""" +capitAddressName+ """
                ' order by population asc
                limit 10""");

        ResultSet rs = stmt.executeQuery();
        // Create a list of addresses
        ArrayList<Address> addresses = new ArrayList<>();

        // Fill the list with addresses
        while (rs.next()) {
            String category = rs.getString("category");
            String name = rs.getString("name");
            String point = rs.getString("point");
            point = point.replace("POINT(", "");
            point = point.replace(")", "");
            String[] coordinates = point.split(" ");

            // Add an address to the list
            Address address = new Address(new GeoPosition(Double.parseDouble(coordinates[1]), Double.parseDouble(coordinates[0])), name,category);
            System.out.println(address);
            addresses.add(address);
        }

        System.out.println(addresses);

        return addresses;
    }

    private static String capitalize(String str) {
        // function to change given String format to title case
        if (str == null || str.isEmpty())
            return "";
        if (str.length() == 1)
            return str.toUpperCase();
        String[] parts = str.split(" ");
        StringBuilder sb = new StringBuilder(str.length());

        for (String part : parts) {
            if (part.length() > 1)
                sb.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            else
                sb.append(part.toUpperCase());
            sb.append(" ");
        }

        return sb.toString().trim();
    }
}
