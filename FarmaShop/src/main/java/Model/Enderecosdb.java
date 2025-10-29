package Model;

import Database.ConexaoAws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Enderecosdb {
    public static void criarTabelas() {
        String sql = """
            CREATE TABLE IF NOT EXISTS enderecos (
                idEndereco INT PRIMARY KEY AUTO_INCREMENT,
                cep VARCHAR(9) UNIQUE NOT NULL, 
                estado ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO') NOT NULL,
                cidade VARCHAR(60) NOT NULL,
                rua VARCHAR(60) NOT NULL,
                numero INT,
                bairro VARCHAR(60),
                complemento TEXT   
            );
            """;

        try (Connection con = ConexaoAws.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.execute(); // usar execute() em CREATE TABLE
            System.out.println("Tabela 'endereços' criada com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}