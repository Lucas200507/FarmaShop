package Model;

import Database.ConexaoAws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Usuariosdb {
        public static void criarTabelas() {
            String sql = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INT AUTO_INCREMENT PRIMARY KEY,
                tipo ENUM('cliente', 'farmacia') DEFAULT 'cliente',
                situacao ENUM('ativo', 'inativo') DEFAULT 'ativo',
                email VARCHAR(100) UNIQUE NOT NULL,
                senha VARCHAR(255) NOT NULL,
                data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            """;

            try (Connection con = ConexaoAws.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.execute(); // usar execute() em CREATE TABLE
                System.out.println("Tabela 'usuarios' criada com sucesso!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
}
