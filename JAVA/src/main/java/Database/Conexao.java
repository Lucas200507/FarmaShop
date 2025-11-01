package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {
    // lucas

    private static final String url = "jdbc:mysql://127.0.0.1:3306/FarmaShop";
    private static final String user = "root";
    private static final String pss = "";

/*
    // catolica
    private static final String url = "jdbc:mysql://localhost:3307/FarmaShop";
    private static final String user = "root";
    private static final String pss = "catolica";
*/
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pss);
    }
}
