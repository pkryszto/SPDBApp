import org.jxmapviewer.viewer.GeoPosition;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryExecuter {

    private  Connection connection;

    public  QueryExecuter()
    {
        String url = "jdbc:postgresql://spdb.ckvqlgxx5bxn.us-east-2.rds.amazonaws.com/SPDBPolska?user=postgres&password=Lofciamspdb1";
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Route findRoute(ArrayList<GeoPosition> points, int tableNumber) throws SQLException {
        ArrayList<ArrayList<GeoPosition>> route= new ArrayList<>();
        double distance = 0;
        double time = 0;
        double startLat =  points.get(0).getLatitude();
        double starLng =  points.get(0).getLongitude();

        double endLat =  points.get(1).getLatitude();
        double endLng = points.get(1).getLongitude();

        Statement stmt = connection.createStatement();

        stmt.executeUpdate(
        "CREATE TEMPORARY TABLE WAYS_"+ tableNumber + " AS (SELECT * from routing2\n" +
                "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint("+starLng+","+startLat+"), 4326)\n" +
                "LIMIT 20000)\n" +
                "UNION (SELECT * from routing2\n" +
                "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint("+endLng+","+endLat+"), 4326)\n" +
                "LIMIT 20000)\n" +
                "UNION (select * from fast_ways\n" +
                "ORDER BY geom_way <-> ST_SetSRID(ST_MakePoint("+(starLng+endLng)/2 +","+(startLat+endLat)/2 +"), 4326)\n" +
                "LIMIT 30000)"
        );

        ResultSet rs = stmt.executeQuery("SELECT ST_AsText(geom_way) as waypoint, km as distance, kmh as speed FROM pgr_astar(\n" +
                "    'select * from WAYS_"+ tableNumber +
                "\t',\n" +
                "    (SELECT source FROM ways \n" +
                "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint("+starLng+","+startLat+"), 4326)\n" +
                "\t\tLIMIT 1),\n" +
                "\t(SELECT source FROM ways \n" +
                "\t\tORDER BY geom_way <-> ST_SetSRID(ST_MakePoint("+endLng+","+endLat+"), 4326)\n" +
                "\t\tLIMIT 1),\n" +
                "\ttrue\n" +
                ") as waypoints\n" +
                "JOIN ways rd ON waypoints.edge = rd.id;");


        while (rs.next()) {
            distance += rs.getDouble("distance");
            time +=  rs.getDouble("distance")/rs.getDouble("speed");

            String line = rs.getString("waypoint");

            line = line.replace("LINESTRING(", "");
            line = line.replace(")", "");


            String[] waypoints = line.split(",");

            ArrayList<GeoPosition> path = new ArrayList<>();

            for (String waypoint : waypoints){
                String[] coordinates = waypoint.split(" ");
                path.add(new GeoPosition(Double.parseDouble(coordinates[1]) , Double.parseDouble(coordinates[0])));

            }
            route.add(path);

        }


        return new Route(route,distance,(int)(time*60));
    }


    public static ArrayList<String> getPOICategories()
    {
        ArrayList<String> categories = new ArrayList<String>(Arrays.asList(
                "Toilet",
                "Fast Food",
                "Charging station",
                "Restaurant",
                "Gas station"));
        return categories;
    }

    public  ArrayList<ArrayList<GeoPosition>> findPOIs(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime, String POICategory)
    {
        String formula = createStatementFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime, POICategory);
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(formula);
            return convertResultSetToList(result);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    private  ArrayList<ArrayList<GeoPosition>> convertResultSetToList(ResultSet result)
    {
        //TO DO
        return null;
    }

    private  String createStatementFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime, String POICategory)
    {
        String selectFormula = createSelectFormula(POInumber);
        String fromFormula = createFromFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime, POICategory);
        return selectFormula + fromFormula;
    }

    private  String createSelectFormula(int POINumber)
    {
        String formula = "";
        //SELECT
        return formula;
    }

    private  String createFromFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime, String POICategory)
    {
        String formula = "";
        //FROM...

        for(int i = 0; i < POInumber - 1; i++) formula += createJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime, POICategory);

        if(POInumber > 0) formula += createLastJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime, POICategory);

        //WHERE

        return formula;
    }

    private  String createJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime, String POICategory)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

    private  String createLastJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int tTimePOI, int minDistance, int minTime, String POICategory)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

    private String mapPOICategory(String type){
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

}
