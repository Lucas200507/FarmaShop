package Model;

import Database.ConexaoAws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Clientesdb {
    public static void criarTabelas() {
        String sql = """
            CREATE TABLE IF NOT EXISTS clientes (
                	idCliente INT PRIMARY KEY AUTO_INCREMENT,
                    nome VARCHAR(60) NOT NULL,
                    cpf VARCHAR(12) UNIQUE NOT NULL, -- 000000000-00
                    telefone VARCHAR(14) UNIQUE NOT NULL, -- (00)90000-0000
                    data_nascimento DATE NOT NULL, -- verificar se é de maior
                    endereco_id INT,
                    usuario_id INT NOT NULL,
                    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (endereco_id) REFERENCES enderecos (idEndereco),
                    FOREIGN KEY (usuario_id) REFERENCES usuarios (idUsuario)
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
