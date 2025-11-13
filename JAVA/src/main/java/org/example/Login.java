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
        int opcao;
        boolean logado = false;
        Scanner sc = new Scanner(System.in);

        do{
            System.out.println("\n\n\n=== LOGIN ===");
            System.out.println("Digite:\n 1.Realizar Login\n 2.Criar uma conta Cliente\n 3.Criar uma conta Farmácia\n 4.Sair");

            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                opcao = 0; // Trata entrada inválida (ex: "abc" ou Enter)
            }

            switch (opcao){
                case 1:
                    logado = realizarLogin(sc);
                    break;
                case 2:
                    // Chama o "Fluxo Corrigido" do Cliente.
                    Cliente.inserirCliente(sc);
                    // (O programa voltará ao menu de login após o cadastro)
                    break;
                case 3:
                    // Chama o "Fluxo Corrigido" da Farmácia.
                    Farmacia.inserirFarmacia(sc);
                    // (O programa voltará ao menu de login após o cadastro)
                    break;
                case 4:
                    // Opção para fechar o loop do login e encerrar o app
                    return false; // Sinaliza ao Main.java para encerrar
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
            // O loop continua se não estiver logado E o usuário não escolheu Sair
        } while (!logado && opcao != 4);

        return logado;
    }

    public boolean realizarLogin(Scanner sc) {
        String usuario;
        String senha;
        boolean logado = false;

        System.out.print("Email (Usuário): "); // Mudei "Usuário" para "Email" para ser mais claro
        usuario = sc.nextLine().trim();
        if (usuario.isEmpty()) {
            return false; // Se o usuário apertar Enter, volta ao menu principal
        }

        System.out.print("Senha: ");
        senha = sc.nextLine().trim();
        if (senha.isEmpty()) {
            return false; // Se o usuário apertar Enter, volta ao menu principal
        }

        // A VIEW 'vw_usuarios' (do seu SQL) é perfeita para o login
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

        return logado;
    }
}