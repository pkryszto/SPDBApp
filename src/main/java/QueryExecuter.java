import org.jxmapviewer.viewer.GeoPosition;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryExecuter {

    private  Connection connection;

    public  QueryExecuter()
    {
        String url = "jdbc:postgresql://spdb.ckvqlgxx5bxn.us-east-2.rds.amazonaws.com/SPDB?user=postgres&password=Lofciamspdb1";
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<GeoPosition> findRoute(ArrayList<GeoPosition> points) throws SQLException {
        ArrayList<GeoPosition> route = new ArrayList<GeoPosition>();

        double startLat =  points.get(0).getLatitude();
        double starLng =  points.get(0).getLongitude();

        double endLat =  points.get(1).getLatitude();
        double endLng = points.get(1).getLongitude();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM pgr_astar(\n" +
                "    'select * from opolskie_routing',\n" +
                "    (SELECT source FROM hh_2po_4pgr\n" +
                "    ORDER BY ST_Distance(\n" +
                "        ST_StartPoint(geom_way),\n" +
                "        ST_SetSRID(ST_MakePoint("+starLng+","+startLat+"), 4326),\n" +
                "        true\n" +
                "   ) ASC limit 1),\n" +
                "\t(SELECT source FROM hh_2po_4pgr\n" +
                "    ORDER BY ST_Distance(\n" +
                "        ST_StartPoint(geom_way),\n" +
                "        ST_SetSRID(ST_MakePoint("+endLng+","+endLat+" ), 4326),\n" +
                "        true\n" +
                "   ) ASC limit 1),\n" +
                "\ttrue)\n" +
                "\t\tas waypoints\n" +
                "JOIN hh_2po_4pgr rd ON waypoints.edge = rd.id;\n");



        while (rs.next()) {
            System.out.println(rs.getDouble("x1") +" "+ rs.getDouble("y1"));
            route.add(new GeoPosition(rs.getDouble("x1"), rs.getDouble("y1")));
        }

        return route;
    }

    public static int getDistanceOfRoute(ArrayList<GeoPosition> points)
    {
        //SELECT...
        return 2137;
    }

    public static int getTimeOfRoute(ArrayList<GeoPosition> points)
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
