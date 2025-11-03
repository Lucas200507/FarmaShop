package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.Scanner;

public class CategoriaProduto {

    public static void exibirCategorias() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CATEGORIAS DE PRODUTOS ===");

        String sql = "SELECT id, nome FROM categoria_produtos ORDER BY id";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("------------------------------------------------");
            }

            if (!any) System.out.println("Nenhuma categoria encontrada.");

        } catch (SQLException e) {
            System.out.println("Erro ao exibir categorias: " + e.getMessage());
        }

        // Menu
        System.out.println("\nEscolha uma opção:");
        System.out.println("1. Inserir nova categoria");
        System.out.println("2. Atualizar categoria");
        System.out.println("3. Deletar categoria");
        System.out.println("4. Voltar");

        int opcao;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (Exception ex) {
            System.out.println("Opção inválida.");
            return;
        }

        switch (opcao) {
            case 1 -> inserirCategoria(sc);
            case 2 -> atualizarCategoria(sc);
            case 3 -> deletarCategoria(sc);
            case 4 -> System.out.println("Voltando...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void inserirCategoria(Scanner sc) {
        System.out.println("=== INSERIR CATEGORIA ===");
        try (Connection con = Conexao.getConnection()) {
            System.out.print("Nome da categoria: ");
            String nome = sc.nextLine().trim();

            if (nome.isEmpty()) {
                System.out.println("Nome não pode ficar vazio.");
                return;
            }

            String sql = "INSERT INTO categoria_produtos (nome) VALUES (?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.executeUpdate();
                System.out.println("Categoria inserida com sucesso!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir categoria: " + e.getMessage());
        }
    }

    private static void atualizarCategoria(Scanner sc) {
        System.out.println("=== ATUALIZAR CATEGORIA ===");
        try (Connection con = Conexao.getConnection()) {
            System.out.print("Digite o ID da categoria: ");
            int id = Integer.parseInt(sc.nextLine());

            // Verifica existência
            String sel = "SELECT * FROM categoria_produtos WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setInt(1, id);
                try (ResultSet rs = selStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Categoria não encontrada.");
                        return;
                    }
                }
            }

            System.out.print("Novo nome da categoria (ou vazio para manter): ");
            String nome = sc.nextLine().trim();

            if (nome.isEmpty()) {
                System.out.println("Nenhum valor informado. Atualização cancelada.");
                return;
            }

            String sql = "UPDATE categoria_produtos SET nome = ? WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setInt(2, id);
                int affected = stmt.executeUpdate();
                if (affected > 0) System.out.println("Categoria atualizada com sucesso!");
                else System.out.println("Nenhuma linha alterada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar categoria: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private static void deletarCategoria(Scanner sc) {
        System.out.println("=== DELETAR CATEGORIA ===");
        try (Connection con = Conexao.getConnection()) {
            System.out.print("Digite o ID da categoria: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Tem certeza que deseja deletar a categoria ID " + id + " ? (S/N): ");
            String conf = sc.nextLine().trim();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            String sql = "DELETE FROM categoria_produtos WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int affected = stmt.executeUpdate();
                if (affected > 0) System.out.println("Categoria deletada com sucesso!");
                else System.out.println("Categoria não encontrada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar categoria: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }
}
