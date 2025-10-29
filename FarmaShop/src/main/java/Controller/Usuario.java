package Controller;

import Database.ConexaoAws;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Usuario {
    public static void exibirUsuarios() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== USUÁRIOS ===");

        String sql = "SELECT * FROM usuarios;";

        try (Connection con = ConexaoAws.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Tipo: " + rs.getString("tipo"));
                System.out.println("Situação: " + rs.getString("situacao"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Data de Alteração: "  + rs.getString("data_alteracao"));
                System.out.println("============================================");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao exibir usuários: " + e.getMessage());
        }

        // Menu de opções
        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir novo usuário");
        System.out.println("2. Atualizar usuário");
        System.out.println("3. Deletar usuário");
        System.out.println("4. Sair da aba usuários");
        int opcao = sc.nextInt();
        sc.nextLine();

        switch (opcao) {
            case 1 -> inserirUsuario(sc);
            case 2 -> atualizarUsuario(sc);
            case 3 -> deletarUsuario(sc);
            case 4 -> System.out.println("Saindo...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void inserirUsuario(Scanner sc) {
        try (Connection con = ConexaoAws.getConnection()) {

            System.out.println("Escolha o tipo de usuário:\n1.Cliente\n2.Farmácia");
            int op = sc.nextInt();
            sc.nextLine();
            String tipo = (op == 1) ? "cliente" : "farmacia";

            String email;
            do {
                System.out.println("Digite o email:");
                email = sc.nextLine();
                if (!email.contains("@") && email.isEmpty()) {
                    System.out.println("Email inválido");
                }
            } while (!email.contains("@"));

            String senha, confSenha;
            do {
                System.out.println("Digite a senha:");
                senha = sc.nextLine();
                System.out.println("Confirme a senha:");
                confSenha = sc.nextLine();
                if (!senha.equals(confSenha)) {
                    System.out.println("Senhas não coincidem. Tente novamente.");
                }
            } while (!senha.equals(confSenha));

            String sql = "INSERT INTO usuarios (tipo, situacao, email, senha) VALUES (?, 'ativo', ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, tipo);
                stmt.setString(2, email);
                stmt.setString(3, senha);
                stmt.executeUpdate();
                System.out.println("Usuário inserido com sucesso!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário: " + e.getMessage());
        }
    }

    private static void atualizarUsuario(Scanner sc) {
        boolean atualizado = false;
        do {
            try (Connection con = ConexaoAws.getConnection()) {
                System.out.println("Digite o número do ID do usuário: ");
                int id = sc.nextInt();
                String sql = "SELECT * FROM usuarios WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        System.out.println("Digite suas alteraçõs");
                        int op;
                        String tipo;
                        do {
                            System.out.println("Escolha o tipo de usuário:\n1.Cliente\n2.Farmácia");
                            op = sc.nextInt();
                            sc.nextLine();
                            tipo = (op == 1) ? "cliente" : "farmacia";
                        } while (op != 1 && op != 2);

                        String email;
                        do {
                            System.out.println("Digite o email:");
                            email = sc.nextLine();
                            if (!email.isEmpty() && !email.contains("@")) {
                                System.out.println("Email inválido. Tente novamente.");
                            }
                        } while (!email.isEmpty() && !email.contains("@"));
                        String senha = "", confSenha = "";
                        do {
                            System.out.println("Digite a senha:");
                            senha = sc.nextLine();
                            if (!senha.isEmpty()) {
                                System.out.println("Confirme a senha:");
                                confSenha = sc.nextLine();
                                if (!senha.equals(confSenha)) {
                                    System.out.println("Senhas não coincidem. Tente novamente.");
                                }
                            }
                        } while (!senha.equals(confSenha) && !senha.isEmpty());
                        List<String> campos = new ArrayList<>();
                        List<Object> valores = new ArrayList<>();

                        if (!tipo.isEmpty()) {
                            campos.add("tipo = ?");
                            valores.add(tipo);
                        }
                        if (!email.isEmpty()) {
                            campos.add("email = ?");
                            valores.add(email);
                        }
                        if (!senha.isEmpty()) {
                            campos.add("senha = ?");
                            valores.add(senha);
                        }

                        if (!campos.isEmpty()) {
                            String sql2 = "UPDATE usuarios SET " + String.join(", ", campos) + " WHERE id = ?";
                            try (PreparedStatement stmt2 = con.prepareStatement(sql2)) {
                                for (int i = 0; i < valores.size(); i++) {
                                    stmt2.setObject(i + 1, valores.get(i));
                                }
                                stmt2.setInt(valores.size() + 1, id);
                                stmt2.executeUpdate();
                                atualizado = true;
                            }
                        }
                    } else {
                        System.out.println("Digite o id de um usuário válido.");
                    }
                } catch (SQLException e) {
                    System.out.println("ID inválido, digite um id de um usuário válido");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }while(!atualizado);
    }

    private static void deletarUsuario(Scanner sc) {
        boolean deletado = false;
        boolean cancelado = false;
        do {
            try (Connection con = ConexaoAws.getConnection()) {
                System.out.println("Digite o número do ID do usuário: ");
                int id = sc.nextInt();
                sc.nextLine();
                String sql = "SELECT * FROM usuarios WHERE id = ?";
                    try (PreparedStatement stmt = con.prepareStatement(sql)) {
                        stmt.setInt(1, id);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()){
                            String op;
                            do{
                                System.out.println("Tem certeza que deseja deletar o usuário ID: "+id+" ? (S / N)");
                                op = sc.nextLine();
                                switch (op) {
                                    case "S":
                                        String sql2 = "DELETE FROM usuarios WHERE id = ?";
                                        PreparedStatement stmt2 = con.prepareStatement(sql2);
                                        stmt2.setInt(1, id);
                                        stmt2.executeUpdate();
                                        System.out.println("Usuário deletado com sucesso!");
                                        deletado = true;
                                        break;
                                    case "N":
                                        System.out.println("Exclusão de usuário cancelado!");
                                        cancelado = true;
                                        break;
                                    default:
                                        System.out.println("Digite uma opção válida!");
                                }
                            } while (!op.equals("S") && !op.equals("N"));
                        }
                    }
            } catch (SQLException e) {
                System.out.println("ID inválido, digite um id de um usuário válido");
            }
        }while(!deletado && !cancelado);
    }
}
