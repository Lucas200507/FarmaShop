package org.example;

import Database.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;


public class Login {
    private String grupo;
    private String email;
    private int id;

    public String getGrupo(){
        return grupo;
    }
    public String getUsuario(){
        return email;
    }
    public int getId(){
        return id;
    }

    public boolean logar() {
        String usuario;
        String senha;
        boolean logado = false;

        System.out.println("=== LOGIN ===");
        Scanner sc = new Scanner(System.in);

        do {
            System.out.print("Usuário: ");
            usuario = sc.nextLine();

            System.out.print("Senha: ");
            senha = sc.nextLine();

            String sql = "SELECT * FROM vw_usuarios WHERE email = ? AND senha = UPPER(MD5(?)) AND situacao = 'ativo'";

            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setString(1, usuario);
                stmt.setString(2, senha);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    grupo = rs.getString("grupo");
                    email = rs.getString("email");
                    id = rs.getInt("id");
                    logado = true;
                } else {
                    System.out.println("Usuário ou senha incorretos, digite novamente.");
                }

            } catch (SQLException e) {
                System.out.println("Erro ao verificar login: " + e.getMessage());
            }

        } while (!logado);

        return logado;
    }
}
