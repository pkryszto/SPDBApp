import java.sql.*;
import java.util.Properties;

public class Main {

    public static  void main(String[] args) {

        QueryExecuter queryExecuter = new QueryExecuter();
        AppWindow app = new AppWindow(queryExecuter);
        app.setVisible(true);


    }
}
