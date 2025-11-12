package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Produtos {

    /**
     * Valida o nome de um produto.
     * Permite letras, números, espaços e alguns símbolos comuns em produtos.
     * @param valor A string de entrada.
     * @return A string tratada se for válida, ou null se for inválida.
     */
    private static String validarNomeProduto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null; // Nulo ou vazio é inválido
        }
        String valorLimpo = valor.trim();

        // Regex atualizada para nomes de produtos (permite letras, números, ml, mg, (), etc.)
        if (valorLimpo.matches("^[a-zA-ZÀ-ú0-9\\s.,'\\\"()%-]+$")) {
            return valorLimpo; // Válido
        } else {
            System.out.println("ERRO: Nome contém símbolos inválidos.");
            return null; // Inválido
        }
    }

    /**
     * Exibe produtos. O menu de ações muda com base no tipo de usuário.
     * @param sc Scanner
     * @param grupoNome O nome do grupo do usuário logado ("cliente", "farmacia", "adm")
     * @param perfilId O ID do Cliente ou da Farmácia logada
     */
    public static void exibirProdutos(Scanner sc, String grupoNome, int perfilId) {
        System.out.println("=== CATÁLOGO DE PRODUTOS ===");

        // Query que só mostra produtos de farmácias ativas
        String sql = """
            SELECT p.COD, p.nome, p.descricao, p.preco, p.estoque, 
                   c.nome AS categoria, f.nome_fantasia AS farmacia, p.dataAlteracao
            FROM produtos p
            JOIN categoria_produtos c ON p.categoria_id = c.id
            JOIN farmacias f ON p.farmacia_id = f.id
            JOIN usuarios u ON f.usuario_id = u.id
            WHERE u.situacao = 'ativo' 
            ORDER BY p.nome;
        """;

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean existe = false;
            while (rs.next()) {
                existe = true;
                System.out.println("ID (COD): " + rs.getString("COD"));
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("Descrição: " + rs.getString("descricao"));
                System.out.println("Preço: R$ " + rs.getDouble("preco"));
                System.out.println("Estoque: " + rs.getInt("estoque"));
                System.out.println("Categoria: " + rs.getString("categoria"));
                System.out.println("Farmácia: " + rs.getString("farmacia"));
                System.out.println("Última Alteração: " + rs.getTimestamp("dataAlteracao"));
                System.out.println("============================================");
            }
            if (!existe) System.out.println("Nenhum produto cadastrado.");

        } catch (SQLException e) {
            System.out.println("Erro ao exibir produtos: " + e.getMessage());
            return;
        }

        System.out.println("\nDigite a opção que preferir:");

        // --- MENU DINÂMICO BASEADO NO GRUPO ---
        if (grupoNome.equals("farmacia") || grupoNome.equals("adm")) {
            System.out.println("1. Inserir novo produto");
            System.out.println("2. Atualizar produto");
            System.out.println("3. Deletar produto");
        }

        if (grupoNome.equals("cliente")) {
            System.out.println("5. Adicionar produto aos favoritos");
            System.out.println("6. Ver meus favoritos (usando a VIEW)");
        }

        System.out.println("4. Voltar ao menu principal");

        int opcao;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Opção inválida.");
            return;
        }

        switch (opcao) {
            // Opções da Farmácia/ADM
            case 1:
                if (grupoNome.equals("farmacia") || grupoNome.equals("adm")) {
                    int farmaciaId = (grupoNome.equals("adm")) ? 0 : perfilId;
                    inserirProduto(sc, farmaciaId);
                } else {
                    System.out.println("Acesso negado.");
                }
                break;
            case 2:
                if (grupoNome.equals("farmacia") || grupoNome.equals("adm")) {
                    atualizarProduto(sc, perfilId, grupoNome);
                } else {
                    System.out.println("Acesso negado.");
                }
                break;
            case 3:
                if (grupoNome.equals("farmacia") || grupoNome.equals("adm")) {
                    deletarProduto(sc, perfilId, grupoNome);
                } else {
                    System.out.println("Acesso negado.");
                }
                break;
            case 4:
                System.out.println("Voltando...");
                break;

            // Opções do Cliente
            case 5:
                if (grupoNome.equals("cliente")) {
                    adicionarFavorito(sc, perfilId);
                } else {
                    System.out.println("Acesso negado.");
                }
                break;
            case 6:
                if (grupoNome.equals("cliente")) {
                    exibirFavoritos(perfilId);
                } else {
                    System.out.println("Acesso negado.");
                }
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    /**
     * Insere um produto.
     */
    private static void inserirProduto(Scanner sc, int farmaciaId) {
        System.out.println("=== INSERIR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {

            String nome;
            do {
                System.out.print("Nome: ");
                nome = validarNomeProduto(sc.nextLine());
            } while (nome == null);

            System.out.print("Descrição: ");
            String descricao = sc.nextLine();

            double preco = 0;
            do {
                System.out.print("Preço: ");
                try {
                    preco = Double.parseDouble(sc.nextLine().replace(",", "."));
                    if (preco <= 0) System.out.println("ERRO: O preço deve ser maior que zero.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Valor inválido. Use números (ex: 10.99).");
                    preco = 0;
                }
            } while (preco <= 0);

            int estoque = -1; // Usar -1 para diferenciar do 0 válido
            do {
                System.out.print("Estoque: ");
                try {
                    estoque = Integer.parseInt(sc.nextLine());
                    if (estoque < 0) System.out.println("ERRO: O estoque não pode ser negativo.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Valor inválido. Use apenas números inteiros.");
                    estoque = -1;
                }
            } while (estoque < 0);

            int categoriaId = 0;
            do {
                System.out.print("ID da Categoria (1-Cosméticos, 2-Medicamento, ...): ");
                try {
                    categoriaId = Integer.parseInt(sc.nextLine());
                    if (categoriaId <= 0) System.out.println("ERRO: ID da categoria deve ser positivo.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Valor inválido. Use apenas números inteiros.");
                    categoriaId = 0;
                }
            } while (categoriaId <= 0);


            if (farmaciaId == 0) {
                do {
                    System.out.print("ID da Farmácia (ADMIN): ");
                    try {
                        farmaciaId = Integer.parseInt(sc.nextLine());
                        if (farmaciaId <= 0) System.out.println("ERRO: ID da farmácia deve ser positivo.");
                    } catch (NumberFormatException e) {
                        System.out.println("ERRO: Valor inválido. Use apenas números inteiros.");
                        farmaciaId = 0;
                    }
                } while (farmaciaId <= 0);
            }

            // O 'COD' não é inserido, pois é gerado pela Trigger 'trg_gerar_idProdutos'
            String sql = "INSERT INTO produtos (nome, descricao, preco, estoque, categoria_id, farmacia_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, descricao);
                stmt.setDouble(3, preco);
                stmt.setInt(4, estoque);
                stmt.setInt(5, categoriaId);
                stmt.setInt(6, farmaciaId);
                stmt.executeUpdate();
                System.out.println("Produto inserido com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir produto: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido. Preço, estoque, categoria e farmácia devem ser números.");
        }
    }

    /**
     * Atualiza um produto.
     */
    private static void atualizarProduto(Scanner sc, int farmaciaId, String grupoNome) {
        System.out.println("=== ATUALIZAR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {

            System.out.print("Digite o COD do produto: ");
            String cod = sc.nextLine();

            // Validação de Permissão (se não for ADM)
            if (!grupoNome.equals("adm")) {
                String sqlCheck = "SELECT farmacia_id FROM produtos WHERE COD = ?";
                try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, cod);
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next()) {
                        if (rsCheck.getInt("farmacia_id") != farmaciaId) {
                            System.out.println("Erro: Você não tem permissão para alterar este produto.");
                            return;
                        }
                    } else {
                        System.out.println("Produto não encontrado.");
                        return;
                    }
                }
            }

            // Se for ADM e o produto não foi encontrado, avisa
            if (grupoNome.equals("adm")) {
                String sqlCheck = "SELECT COUNT(*) FROM produtos WHERE COD = ?";
                try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, cod);
                    ResultSet rsCheck = psCheck.executeQuery();
                    rsCheck.next();
                    if (rsCheck.getInt(1) == 0) {
                        System.out.println("Produto não encontrado.");
                        return;
                    }
                }
            }

            String nome;
            do {
                System.out.print("Novo nome (vazio mantém o atual): ");
                nome = sc.nextLine();
                if (nome.isEmpty()) break; // Permite pular
                nome = validarNomeProduto(nome);
            } while (nome == null);

            System.out.print("Nova descrição (vazio mantém o atual): ");
            String descricao = sc.nextLine();

            String precoStr;
            Double preco = null;
            do {
                System.out.print("Novo preço (vazio mantém o atual): ");
                precoStr = sc.nextLine();
                if (precoStr.isEmpty()) break;
                try {
                    preco = Double.parseDouble(precoStr.replace(",", "."));
                    if (preco <= 0) System.out.println("ERRO: O preço deve ser maior que zero.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Valor inválido. Use números (ex: 10.99).");
                    preco = null;
                }
            } while (preco != null && preco <= 0); // Só repete se o número for inválido (<=0)

            String estoqueStr;
            Integer estoque = null;
            do {
                System.out.print("Novo estoque (vazio mantém o atual): ");
                estoqueStr = sc.nextLine();
                if (estoqueStr.isEmpty()) break;
                try {
                    estoque = Integer.parseInt(estoqueStr);
                    if (estoque < 0) System.out.println("ERRO: O estoque não pode ser negativo.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Valor inválido. Use apenas números inteiros.");
                    estoque = null;
                }
            } while (estoque != null && estoque < 0);


            StringBuilder sql = new StringBuilder("UPDATE produtos SET ");
            List<Object> params = new ArrayList<>();
            boolean first = true;

            if (!nome.isEmpty()) { if(!first) sql.append(","); sql.append("nome = ?"); params.add(nome); first = false; }
            if (!descricao.isEmpty()) { if(!first) sql.append(", "); sql.append("descricao = ?"); params.add(descricao); first = false; }
            if (preco != null) { if(!first) sql.append(", "); sql.append("preco = ?"); params.add(preco); first = false; }
            if (estoque != null) { if(!first) sql.append(", "); sql.append("estoque = ?"); params.add(estoque); first = false; }

            if (params.isEmpty()) {
                System.out.println("Nenhum campo foi alterado.");
                return;
            }

            sql.append(" WHERE COD = ?");
            params.add(cod); // Adiciona o COD no final

            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                stmt.executeUpdate();
                System.out.println("Produto atualizado com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar produto: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
        }
    }

    /**
     * Deleta um produto.
     */
    private static void deletarProduto(Scanner sc, int farmaciaId, String grupoNome) {
        System.out.println("=== DELETAR PRODUTO ===");
        try (Connection con = Conexao.getConnection()) {

            System.out.print("Digite o COD do produto: ");
            String cod = sc.nextLine();

            // Validação de Permissão (se não for ADM)
            if (!grupoNome.equals("adm")) {
                String sqlCheck = "SELECT farmacia_id FROM produtos WHERE COD = ?";
                try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, cod);
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next()) {
                        if (rsCheck.getInt("farmacia_id") != farmaciaId) {
                            System.out.println("Erro: Você não tem permissão para deletar este produto.");
                            return;
                        }
                    } else {
                        System.out.println("Produto não encontrado.");
                        return;
                    }
                }
            }

            System.out.print("Tem certeza que deseja deletar? (S/N): ");
            String conf = sc.nextLine();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            // O SQL agora usa 'ON DELETE CASCADE' para 'prod_favoritos' e 'imagem_produtos'
            // então não precisamos deletar deles manualmente.
            // Apenas o delete principal é necessário.

            String sqlProd = "DELETE FROM produtos WHERE COD = ?";
            try (PreparedStatement stmtProd = con.prepareStatement(sqlProd)) {
                stmtProd.setString(1, cod);
                int rows = stmtProd.executeUpdate();
                if (rows > 0) System.out.println("Produto deletado com sucesso!");
                else System.out.println("Produto não encontrado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar produto: " + e.getMessage());
            System.out.println("Verifique se este produto não está associado a outras tabelas (ex: pedidos).");
        }
    }

    // --- MÉTODOS DE FAVORITOS (NOVOS) ---

    /**
     * Adiciona um produto à lista de favoritos de um cliente.
     */
    public static void adicionarFavorito(Scanner sc, int clienteId) {
        System.out.println("=== ADICIONAR FAVORITO ===");
        System.out.print("Digite o COD do produto que deseja favoritar: ");
        String produtoCod = sc.nextLine();

        // O SQL agora tem uma Primary Key (cliente_id, produto_cod)
        String sql = "INSERT INTO prod_favoritos (cliente_id, produto_cod) VALUES (?, ?)";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            stmt.setString(2, produtoCod);
            stmt.executeUpdate();

            System.out.println("Produto " + produtoCod + " adicionado aos favoritos!");

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código de erro para "Duplicate entry"
                System.out.println("Erro: Este produto já está nos seus favoritos.");
            } else if (e.getErrorCode() == 1452) { // Código de erro para "Foreign key constraint fails"
                System.out.println("Erro: Produto com COD '" + produtoCod + "' não encontrado.");
            } else {
                System.out.println("Erro ao adicionar favorito: " + e.getMessage());
            }
        }
    }

    /**
     * Exibe os produtos favoritados por um cliente (USANDO A VIEW).
     */
    public static void exibirFavoritos(int clienteId) {
        System.out.println("=== MEUS FAVORITOS ===");

        // Usando a VIEW 'vw_favoritos' (que está no seu SQL)
        String sql = "SELECT * FROM vw_favoritos WHERE cliente_id = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();

            boolean existe = false;
            while (rs.next()) {
                existe = true;
                System.out.println("---------------------------------");
                // CORREÇÃO: A coluna na vw_favoritos chama-se 'COD'
                System.out.println("COD: " + rs.getString("COD"));
                System.out.println("Nome: " + rs.getString("produtoNome"));
                System.out.println("Preço: R$ " + rs.getDouble("preco"));
                System.out.println("Vendido por: " + rs.getString("farmaciaNome"));
            }
            System.out.println("---------------------------------");

            if (!existe) {
                System.out.println("Você ainda não tem produtos favoritos.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao exibir favoritos: " + e.getMessage());
        }
    }
}