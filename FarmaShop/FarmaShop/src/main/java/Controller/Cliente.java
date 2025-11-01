package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Clientes {

    public static void exibirClientes() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CLIENTES ===");

        String sql = "SELECT * FROM clientes;";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("CPF: " + rs.getString("cpf"));
                System.out.println("Telefone: " + rs.getString("telefone"));
                System.out.println("============================================");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao exibir clientes: " + e.getMessage());
        }

        // Menu de opções
        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir novo cliente");
        System.out.println("2. Atualizar cliente");
        System.out.println("3. Deletar cliente");
        System.out.println("4. Sair da aba clientes");

        int opcao = sc.nextInt();
        sc.nextLine();

        switch (opcao) {
            case 1 -> inserirCliente(sc);
            case 2 -> atualizarCliente(sc);
            case 3 -> deletarCliente(sc);
            case 4 -> System.out.println("Saindo...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void inserirCliente(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o nome do cliente:");
            String nome = sc.nextLine();

            System.out.println("Digite o CPF:");
            String cpf = sc.nextLine();

            System.out.println("Digite o telefone:");
            String telefone = sc.nextLine();

            String sql = "INSERT INTO clientes (nome, cpf, telefone) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpf);
                stmt.setString(3, telefone);
                stmt.executeUpdate();
                System.out.println("Cliente inserido com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir cliente: " + e.getMessage());
        }
    }

    private static void atualizarCliente(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID do cliente que deseja atualizar:");
            int id = sc.nextInt();
            sc.nextLine();

            String sql = "SELECT * FROM clientes WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Digite o novo nome (ou pressione Enter para manter):");
                    String nome = sc.nextLine();

                    System.out.println("Digite o novo CPF (ou pressione Enter para manter):");
                    String cpf = sc.nextLine();

                    System.out.println("Digite o novo telefone (ou pressione Enter para manter):");
                    String telefone = sc.nextLine();

                    StringBuilder sqlUpdate = new StringBuilder("UPDATE clientes SET ");
                    boolean primeiro = true;

                    if (!nome.isEmpty()) {
                        sqlUpdate.append("nome = '").append(nome).append("'");
                        primeiro = false;
                    }
                    if (!cpf.isEmpty()) {
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("cpf = '").append(cpf).append("'");
                        primeiro = false;
                    }
                    if (!telefone.isEmpty()) {
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("telefone = '").append(telefone).append("'");
                    }

                    sqlUpdate.append(" WHERE id = ").append(id);

                    try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate.toString())) {
                        stmtUpdate.executeUpdate();
                        System.out.println("Cliente atualizado com sucesso!");
                    }

                } else {
                    System.out.println("Cliente não encontrado.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    private static void deletarCliente(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID do cliente que deseja deletar:");
            int id = sc.nextInt();
            sc.nextLine();

            System.out.println("Tem certeza que deseja deletar o cliente ID " + id + "? (S/N)");
            String confirmacao = sc.nextLine();

            if (confirmacao.equalsIgnoreCase("S")) {
                String sql = "DELETE FROM clientes WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    System.out.println("Cliente deletado com sucesso!");
                }
            } else {
                System.out.println("Operação cancelada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cliente: " + e.getMessage());
        }
    }
}
