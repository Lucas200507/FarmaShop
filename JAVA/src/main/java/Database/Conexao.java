package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    // Suas credenciais (Marilia)
    /*
    private static final String url = "jdbc:mysql://127.0.0.1:3306/FarmaShop";
    private static final String user = "root";
    private static final String pss = "25112006";
    */
    /*
     * Bloco estático para carregar o driver do MySQL (o "motor a diesel")
     * Isso corrige o erro 'ClassNotFoundException'.
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver MySQL (mysql-connector-j) não encontrado! Verifique seu pom.xml", e);
        }
    }

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