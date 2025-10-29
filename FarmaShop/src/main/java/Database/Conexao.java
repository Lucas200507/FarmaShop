package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {
    public static void main(String[] args){
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/farmashop",
                    "root",
                    ""
    
            );
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from usuarios");

            while(rs.next()){
                System.out.println(rs.getString(1)); // tipo
                System.out.println(rs.getString(2)); // situacao
                System.out.println(rs.getString(3)); // email
                System.out.println(rs.getString(4)); // senha
                System.out.println(rs.getString(5)); // data_alteracao

            }
            rs.close();
            stmt.close();
            con.close();
        } catch (SQLException e) {
            System.out.println("Erro na conexão: "+e.getMessage()  );
        }
    }
}
