import org.jxmapviewer.viewer.GeoPosition;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryExecuter {

    public static Connection connection;

    public static void connect(String url)
    {
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<GeoPosition> findRoute(ArrayList<GeoPosition> points)
    {
        //SELECT...
        return points;
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

    public static ArrayList<ArrayList<GeoPosition>> findPOIs(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
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

    private static ArrayList<ArrayList<GeoPosition>> convertResultSetToList(ResultSet result)
    {
        //TO DO
        return null;
    }

    private static String createStatementFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String selectFormula = createSelectFormula(POInumber);
        String fromFormula = createFromFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);
        return selectFormula + fromFormula;
    }

    private static String createSelectFormula(int POINumber)
    {
        String formula = "";
        //SELECT
        return formula;
    }

    private static String createFromFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String formula = "";
        //FROM...

        for(int i = 0; i < POInumber - 1; i++) formula += createJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);

        if(POInumber > 0) formula += createLastJoinFormula(POInumber, points, maxDistance, maxTime, distancePOI, timePOI, minDistance, minTime);

        //WHERE

        return formula;
    }

    private static String createJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int timePOI, int minDistance, int minTime)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

    private static String createLastJoinFormula(int POInumber, ArrayList<GeoPosition> points, int maxDistance, int maxTime, int distancePOI, int tTimePOI, int minDistance, int minTime)
    {
        String formula = "";
        //JOIN ...
        return formula;
    }

}
