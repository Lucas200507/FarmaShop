package org.example;

import Controller.Cliente;
import Controller.Endereco;
import Controller.Farmacia;
import Controller.Usuario;
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
        String idEndereco;
        int opcao, idUsuario, idCliente, idFarmacia;
        boolean logado = false, valido = false;
        Scanner sc = new Scanner(System.in);
        Usuario u = new Usuario();
        Endereco e = new Endereco();
        Farmacia f = new Farmacia();
        Cliente c =  new Cliente();

        do{
            System.out.println("\n\n\n=== LOGIN ===");
            System.out.println("Digite:\n 1.Realizar Login\n 2.Criar uma conta Cliente\n 3.Criar uma conta Farmácia");
            opcao = sc.nextInt();
            sc.nextLine();
            switch (opcao){
                case 1:
                    logado = realizarLogin(sc);
                    break;
                case 2:
                    idUsuario = u.inserirUsuario(sc, 2); // grupo 2 = cliente
                    idEndereco = e.inserirEndereco(sc);
                    idCliente = c.inserirCliente(sc, idUsuario, idEndereco);
                    break;
                case 3:
                    idUsuario = u.inserirUsuario(sc, 3);
                    idEndereco = e.inserirEndereco(sc);
                    idFarmacia = f.inserirFarmacia(sc, idUsuario, idEndereco);
                    break;

            }
        } while (!logado);
        return logado;
    }

    public boolean realizarLogin(Scanner sc) {
        String usuario;
        String senha;
        boolean logado = false;
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