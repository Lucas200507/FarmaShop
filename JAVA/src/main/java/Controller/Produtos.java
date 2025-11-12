package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.Scanner;


public class Produtos {

    public static void exibirProdutos() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== PRODUTOS ===");

        String sql = "SELECT p.id, p.codigo, p.nome, p.descricao, p.estoque, p.promocao, p.preco, p.categoria_id, p.farmacia_id, p.data_alteracao " +
                "FROM produtos p ORDER BY p.id;";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Código: " + rs.getString("codigo"));
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("Descrição: " + rs.getString("descricao"));
                System.out.println("Estoque: " + rs.getInt("estoque"));
                System.out.println("Promoção: " + (rs.getBoolean("promocao") ? "Sim" : "Não"));
                System.out.printf("Preço: R$ %.2f%n", rs.getDouble("preco"));
                System.out.println("Categoria ID: " + rs.getInt("categoria_id"));
                System.out.println("Farmácia ID: " + rs.getInt("farmacia_id"));
                System.out.println("Data alteração: " + rs.getTimestamp("data_alteracao"));
                System.out.println("----------------------------------------------------");
            }
            if (!any) System.out.println("Nenhum produto encontrado.");

        } catch (SQLException e) {
            System.out.println("Erro ao exibir produtos: " + e.getMessage());
            return;
        }

        // Menu
        System.out.println("\nEscolha uma opção:");
        System.out.println("1. Inserir novo produto");
        System.out.println("2. Atualizar produto");
        System.out.println("3. Deletar produto");
        System.out.println("4. Voltar");

        int opcao;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (Exception ex) {
            System.out.println("Opção inválida.");
            return;
        }

        switch (opcao) {
            case 1 -> inserirProduto(sc);
            case 2 -> atualizarProduto(sc);
            case 3 -> deletarProduto(sc);
            case 4 -> System.out.println("Voltando...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void inserirProduto(Scanner sc) {
        System.out.println("=== INSERIR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {

            System.out.print("Código (ex: P1): ");
            String codigo = sc.nextLine().trim();

            System.out.print("Nome: ");
            String nome = sc.nextLine().trim();

            System.out.print("Descrição: ");
            String descricao = sc.nextLine().trim();

            int estoque;
            while (true) {
                System.out.print("Estoque (número inteiro): ");
                try {
                    estoque = Integer.parseInt(sc.nextLine());
                    if (estoque < 0) throw new NumberFormatException();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Estoque inválido. Digite um número inteiro >= 0.");
                }
            }

            double preco;
            while (true) {
                System.out.print("Preço (ex: 12.90): ");
                try {
                    preco = Double.parseDouble(sc.nextLine().replace(",", "."));
                    if (preco < 0) throw new NumberFormatException();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Preço inválido. Digite um número válido.");
                }
            }

            System.out.print("Promoção? (S/N): ");
            String promoInput = sc.nextLine().trim();
            boolean promocao = promoInput.equalsIgnoreCase("S");

            int categoriaId;
            while (true) {
                System.out.print("Categoria ID: ");
                try {
                    categoriaId = Integer.parseInt(sc.nextLine());
                    if (!existeRegistro(con, "categoria_produtos", categoriaId)) {
                        System.out.println("Categoria não encontrada. Cadastre uma categoria ou informe outro ID.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("ID inválido.");
                }
            }

            int farmaciaId;
            while (true) {
                System.out.print("Farmácia ID: ");
                try {
                    farmaciaId = Integer.parseInt(sc.nextLine());
                    if (!existeRegistro(con, "farmacias", farmaciaId)) {
                        System.out.println("Farmácia não encontrada. Informe um ID válido.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("ID inválido.");
                }
            }

            String sql = "INSERT INTO produtos (codigo, nome, descricao, estoque, promocao, preco, categoria_id, farmacia_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, codigo);
                stmt.setString(2, nome);
                stmt.setString(3, descricao);
                stmt.setInt(4, estoque);
                stmt.setBoolean(5, promocao);
                stmt.setDouble(6, preco);
                stmt.setInt(7, categoriaId);
                stmt.setInt(8, farmaciaId);
                stmt.executeUpdate();
                System.out.println("Produto inserido com sucesso!");
            } catch (SQLException e) {
                System.out.println("Erro ao inserir produto: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

    private static void atualizarProduto(Scanner sc) {
        System.out.println("=== ATUALIZAR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {

            System.out.print("Digite o ID do produto a ser atualizado: ");
            int id = Integer.parseInt(sc.nextLine());

            // Verifica existência
            String sel = "SELECT * FROM produtos WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setInt(1, id);
                try (ResultSet rs = selStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Produto não encontrado.");
                        return;
                    }
                }
            }

            System.out.println("Deixe em branco para manter o valor atual.");
            System.out.print("Novo nome: ");
            String nome = sc.nextLine().trim();

            System.out.print("Nova descrição: ");
            String descricao = sc.nextLine().trim();

            System.out.print("Novo estoque (ou vazio): ");
            String estoqueStr = sc.nextLine().trim();

            System.out.print("Novo preço (ou vazio): ");
            String precoStr = sc.nextLine().trim();

            System.out.print("Promoção? (S/N ou vazio): ");
            String promoStr = sc.nextLine().trim();

            System.out.print("Nova categoria ID (ou vazio): ");
            String categoriaStr = sc.nextLine().trim();

            System.out.print("Nova farmácia ID (ou vazio): ");
            String farmaciaStr = sc.nextLine().trim();

            // Monta SQL dinâmico com parâmetros (evita concatenação insegura)
            StringBuilder sql = new StringBuilder("UPDATE produtos SET ");
            java.util.List<Object> params = new java.util.ArrayList<>();
            boolean first = true;

            if (!nome.isEmpty()) { if (!first) sql.append(", "); sql.append("nome = ?"); params.add(nome); first = false; }
            if (!descricao.isEmpty()) { if (!first) sql.append(", "); sql.append("descricao = ?"); params.add(descricao); first = false; }
            if (!estoqueStr.isEmpty()) {
                try {
                    int estoque = Integer.parseInt(estoqueStr);
                    if (!first) sql.append(", "); sql.append("estoque = ?"); params.add(estoque); first = false;
                } catch (NumberFormatException e) { System.out.println("Estoque inválido. Atualização pulada para estoque."); }
            }
            if (!precoStr.isEmpty()) {
                try {
                    double preco = Double.parseDouble(precoStr.replace(",", "."));
                    if (!first) sql.append(", "); sql.append("preco = ?"); params.add(preco); first = false;
                } catch (NumberFormatException e) { System.out.println("Preço inválido. Atualização pulada para preço."); }
            }
            if (!promoStr.isEmpty()) {
                boolean promo = promoStr.equalsIgnoreCase("S");
                if (!first) sql.append(", "); sql.append("promocao = ?"); params.add(promo); first = false;
            }
            if (!categoriaStr.isEmpty()) {
                try {
                    int categoriaId = Integer.parseInt(categoriaStr);
                    if (!existeRegistro(con, "categoria_produtos", categoriaId)) {
                        System.out.println("Categoria não encontrada. Campo categoria será ignorado.");
                    } else {
                        if (!first) sql.append(", "); sql.append("categoria_id = ?"); params.add(categoriaId); first = false;
                    }
                } catch (NumberFormatException e) { System.out.println("Categoria inválida. Ignorando."); }
            }
            if (!farmaciaStr.isEmpty()) {
                try {
                    int farmaciaId = Integer.parseInt(farmaciaStr);
                    if (!existeRegistro(con, "farmacias", farmaciaId)) {
                        System.out.println("Farmácia não encontrada. Campo farmácia será ignorado.");
                    } else {
                        if (!first) sql.append(", "); sql.append("farmacia_id = ?"); params.add(farmaciaId); first = false;
                    }
                } catch (NumberFormatException e) { System.out.println("Farmácia inválida. Ignorando."); }
            }

            if (params.isEmpty()) {
                System.out.println("Nenhum campo para atualizar.");
                return;
            }

            sql.append(" WHERE id = ?");
            params.add(id);

            try (PreparedStatement upd = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    Object p = params.get(i);
                    if (p instanceof Integer) upd.setInt(i+1, (Integer)p);
                    else if (p instanceof Double) upd.setDouble(i+1, (Double)p);
                    else if (p instanceof Boolean) upd.setBoolean(i+1, (Boolean)p);
                    else upd.setString(i+1, p.toString());
                }
                int affected = upd.executeUpdate();
                if (affected > 0) System.out.println("Produto atualizado com sucesso!");
                else System.out.println("Nenhuma linha alterada.");
            } catch (SQLException e) {
                System.out.println("Erro ao atualizar produto: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private static void deletarProduto(Scanner sc) {
        System.out.println("=== DELETAR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {
            System.out.print("Digite o ID do produto a deletar: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Tem certeza que deseja deletar o produto ID " + id + " ? (S/N): ");
            String conf = sc.nextLine().trim();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            String sql = "DELETE FROM produtos WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int affected = stmt.executeUpdate();
                if (affected > 0) System.out.println("Produto deletado com sucesso!");
                else System.out.println("Produto não encontrado.");
            } catch (SQLException e) {
                System.out.println("Erro ao deletar produto: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }


    private static boolean existeRegistro(Connection con, String tabela, int id) {
        String sql = "SELECT 1 FROM " + tabela + " WHERE id = ? LIMIT 1";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {

            return false;
        }
    }
}
