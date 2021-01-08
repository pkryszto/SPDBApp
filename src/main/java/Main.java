import java.sql.*;
import java.util.Properties;

public class Main {

    public static  void main(String[] args) throws SQLException {
        AppWindow app = new AppWindow();
        app.setVisible(true);

        // trasa z lotniska do centrum wypisane w printlnie

        Connection conn =  connect();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT *\n" +
                "FROM pgr_dijkstra(\n" +
                "   'SELECT gid as id, source, target, st_length(the_geom, true) as cost FROM ways',\n" +
                "   (SELECT source FROM ways\n" +
                "    ORDER BY ST_Distance(\n" +
                "        ST_StartPoint(the_geom),\n" +
                "        ST_SetSRID(ST_MakePoint(20.983455, 52.231909), 4326),\n" +
                "        true\n" +
                "   ) ASC\n" +
                "   LIMIT 1),\n" +
                "   (SELECT source FROM ways\n" +
                "    ORDER BY ST_Distance(\n" +
                "        ST_StartPoint(the_geom),\n" +
                "        ST_SetSRID(ST_MakePoint(20.973400, 52.169647), 4326),\n" +
                "        true\n" +
                "   ) ASC\n" +
                "   LIMIT 1)\n" +
                ") as pt\n" +
                "JOIN ways rd ON pt.edge = rd.gid;");

        while (rs.next()) {
            String x1 = rs.getString("x1");
            String y1 = rs.getString("y1");
            String x2 = rs.getString("x2");
            String y2 = rs.getString("y2");

            System.out.println("Start node "+x1+" + "+y1 + ", End node"+x2+" "+y2);
        }

    }

    public static Connection connect() {
        Connection conn = null;
        try {
            String url = "jdbc:postgresql://spdb.ckvqlgxx5bxn.us-east-2.rds.amazonaws.com/SPDB?user=postgres&password=Lofciamspdb1";
            conn = DriverManager.getConnection(url);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}
