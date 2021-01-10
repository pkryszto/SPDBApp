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

    public Route findRoute(ArrayList<GeoPosition> points) throws SQLException {
        ArrayList<ArrayList<GeoPosition>> route= new ArrayList<>();
        double distance = 0;
        double time = 0;
        double startLat =  points.get(0).getLatitude();
        double starLng =  points.get(0).getLongitude();

        double endLat =  points.get(1).getLatitude();
        double endLng = points.get(1).getLongitude();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ST_AsText(geom_way) as waypoint, km as distance, kmh as speed FROM pgr_astar(\n" +
                "    'select * from routing2 where (Select ST_Distance(\n" +
                "\t\t\tST_Transform(geom_way,26986),\n" +
                "\t\t\tST_Transform(ST_SetSRID(ST_MakePoint("+starLng+","+startLat+"), 4326),26986)\n" +
                "\t\t)) < 10000 \n" +
                "\t\tOR\n" +
                "\t\t(Select ST_Distance(\n" +
                "\t\t\tST_Transform(geom_way,26986),\n" +
                "\t\t\tST_Transform(ST_SetSRID(ST_MakePoint("+endLng+","+endLat+"), 4326),26986)\n" +
                "\t\t)) < 10000 \n" +
                "\t\tOR clazz < 20  \n" +
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

    public int getDistanceOfRoute(ArrayList<ArrayList<GeoPosition>> points)
    {
        //SELECT...
        return 2137;
    }

    public int getTimeOfRoute(ArrayList<ArrayList<GeoPosition>> points)
    {
        //SELECT...
        return 1297;
    }

    public static ArrayList<String> getPOICategories()
    {
        //SELECT...
        ArrayList<String> categories = new ArrayList<String>(Arrays.asList(
                "Night club",
                "Restaurant",
                "Gas station"));
        return categories;
    }

    public  ArrayList<ArrayList<GeoPosition>> findPOIs(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String formula = createStatementFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);
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

    private  String createStatementFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String selectFormula = createSelectFormula(POInumber);
        String fromFormula = createFromFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);
        return selectFormula + fromFormula;
    }

    private  String createSelectFormula(int POINumber)
    {
        String formula = "";
        //SELECT
        return formula;
    }

    private  String createFromFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String formula = "";
        //FROM...

        for(int i = 0; i < POInumber - 1; i++) formula += createJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);

        if(POInumber > 0) formula += createLastJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);

        //WHERE

        return formula;
    }

    private  String createJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

    private  String createLastJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int tTimePOI, int minDistance, int minTime)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

}
