package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Usuario {
    public static void exibirUsuarios(String tipo) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== USUÁRIOS ===");

        if (tipo.equals("todos")) {
            String sql = "SELECT u.*, gu.nome AS tipo FROM usuarioGrupo ug LEFT JOIN usuarios u ON u.id = ug.usuario_id LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id WHERE u.situacao = 'ativo';";

            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Tipo: " + rs.getString("tipo"));
                    System.out.println("Situação: " + rs.getString("situacao"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("Data de Alteração: "  + rs.getString("dataAlteracao"));
                    System.out.println("============================================");
                }
                rs.close();
                stmt.close();
                con.close();
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

        } else {
            String sql = "SELECT * FROM usuarios WHERE tipo = ?";
            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)){
                    stmt.setString(1, tipo);
                    try(ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id"));
                        System.out.println("Tipo: " + rs.getString("tipo"));
                        System.out.println("Situação: " + rs.getString("situacao"));
                        System.out.println("Email: " + rs.getString("email"));
                        System.out.println("Data de Alteração: "  + rs.getString("dataAlteracao"));
                        System.out.println("============================================");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao exibir usuários: " + e.getMessage());
            }
        }
    }

    private static void inserirUsuario(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            boolean errado = true;
            int idGrupo = 0, idUsuario = 0;
            do{
                System.out.println("Escolha o tipo de usuário:\n1.Cliente\n2.Farmácia\n3.ADM");
                int op = sc.nextInt();
                sc.nextLine();

                switch (op) {
                    case 1:
                        idGrupo = 2;
                        errado = false;
                        break;
                    case 2:
                        idGrupo = 3;
                        errado = false;
                        break;
                    case 3:
                        idGrupo = 1;
                        errado = false;
                        break;
                    default:
                        System.out.println("Opção inválida");
                }
            } while(errado);


            String email;
            do {
                System.out.println("Digite o email:");
                email = sc.nextLine();
                if (!email.contains("@") || email.isEmpty()) {
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

            String sql = "INSERT INTO usuarios (situacao, email, senha) VALUES ('ativo', ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, senha);
                stmt.executeUpdate();


                String sqlSelect = "SELECT * FROM usuarios WHERE situacao = 'ativo' AND email = ? AND senha = UPPER(MD5(?));";
                try (PreparedStatement stmt2 = con.prepareStatement(sqlSelect)) {
                    stmt2.setString(1, email);
                    stmt2.setString(2, senha);
                    ResultSet rs = stmt2.executeQuery();
                    while (rs.next()) {
                        idUsuario = rs.getInt("id");
                    }

                    String sqlGrupo = "INSERT INTO usuarioGrupo (usuario_id, grupo_id) VALUES (?, ?)";
                    try (PreparedStatement stmt3 = con.prepareStatement(sqlGrupo)) {
                        stmt3.setInt(1, idUsuario);
                        stmt3.setInt(2, idGrupo);
                        stmt3.executeUpdate();
                        System.out.println("Usuário inserido com sucesso!");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário: " + e.getMessage());
        }
    }

    private static void atualizarUsuario(Scanner sc) {
        boolean atualizado = false;
        do {
            try (Connection con = Conexao.getConnection()) {
                System.out.println("Digite o número do ID do usuário: ");
                int id = sc.nextInt();
                sc.nextLine();

                String sql = "SELECT u.*, gu.nome AS tipo, gu.id AS grupoId FROM usuarioGrupo ug LEFT JOIN usuarios u ON u.id = ug.usuario_id LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id WHERE u.id = ? AND u.situacao = 'ativo'";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        System.out.println("Usuário encontrado:\n=================================");
                        System.out.println("Grupo: " + rs.getString("tipo"));
                        System.out.println("Email atual: " + rs.getString("email"));
                        System.out.println("Situação atual: " + rs.getString("situacao"));
                        System.out.println("=================================");
                        int grupoId = rs.getInt("grupoId");

                        System.out.println("Deixe em branco para não alterar o campo.");
                        System.out.println("Deseja alterar o tipo de usuário? (1.Adm | 2.Cliente | 3.Farmacia | Enter para manter)");
                        String tipoInput = sc.nextLine();
                        boolean alterarGrupo = false;
                        if (!tipoInput.isEmpty()) {
                            int op = Integer.parseInt(tipoInput);
                            if (op == 1) { grupoId = 1; alterarGrupo = true; }
                            else if (op == 2) { grupoId = 2; alterarGrupo = true; }
                            else if (op == 3) { grupoId = 3; alterarGrupo = true; }
                            else System.out.println("Opção inválida, tipo não alterado.");
                        }

                        if (alterarGrupo) {
                            String sqlGrupo = "UPDATE usuarioGrupo SET grupo_id = ? WHERE usuario_id = ?";
                            try (PreparedStatement stmt2 = con.prepareStatement(sqlGrupo)) {
                                stmt2.setInt(1, grupoId);
                                stmt2.setInt(2, id);
                                stmt2.executeUpdate();
                            }
                        }

                        System.out.println("Novo email (ou Enter para manter):");
                        String email = sc.nextLine();
                        if (!email.isEmpty() && !email.contains("@")) {
                            System.out.println("Email inválido. Tente novamente.");
                            continue;
                        }

                        System.out.println("Nova senha (ou Enter para manter):");
                        String senha = sc.nextLine();
                        String confSenha = "";
                        if (!senha.isEmpty()) {
                            System.out.println("Confirme a senha:");
                            confSenha = sc.nextLine();
                            if (!senha.equals(confSenha)) {
                                System.out.println("Senhas não coincidem. Tente novamente.");
                                continue;
                            }
                        }

                        List<String> campos = new ArrayList<>();
                        List<Object> valores = new ArrayList<>();

                        if (!email.isEmpty()) {
                            campos.add("email = ?");
                            valores.add(email);
                        }
                        if (!senha.isEmpty()) {
                            campos.add("senha = UPPER(MD5(?))");
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
                            }
                        }

                        if (!alterarGrupo && campos.isEmpty()) {
                            System.out.println("Nenhum campo alterado.");
                        } else {
                            System.out.println("Usuário atualizado com sucesso!");
                        }

                        atualizado = true;
                    } else {
                        System.out.println("ID não encontrado. Tente novamente.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao atualizar usuário: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números para o tipo.");
            }
        } while (!atualizado);
    }

    private static void deletarUsuario(Scanner sc) {
        boolean deletado = false;
        boolean cancelado = false;
        do {
            try (Connection con = Conexao.getConnection()) {
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
                                op = sc.nextLine().toLowerCase();
                                switch (op) {
                                    case "S":
                                        String sql2 = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
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
