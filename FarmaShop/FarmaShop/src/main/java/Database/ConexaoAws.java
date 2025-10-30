package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConexaoAws {
    private static String URL = "jdbc:mysql://farmashop-db.creo202ouamq.sa-east-1.rds.amazonaws.com:3306/Farmashop?useSSL=true";
    private static String user = "admin";
    private static String pass = "ZmxhbWVuZ28=";

    public static Connection getConnection() throws SQLException{
        return  DriverManager.getConnection(URL,user,pass);
    }

}
