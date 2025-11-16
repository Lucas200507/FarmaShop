package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Farmacia {

    /**
     * Valida se a string é puramente numérica e está dentro do range de tamanho.
     * @param valor String de entrada.
     * @param TamMin Comprimento mínimo.
     * @param TamMax Comprimento máximo.
     * @return A string de números se for válida, ou null se for inválida.
     */
    private static String validarNumero(String valor, int TamMin, int TamMax) {
        if (valor == null) return null;
        String valorLimpo = valor.replaceAll("[^0-9]", "");
        if (valorLimpo.length() >= TamMin && valorLimpo.length() <= TamMax) return valorLimpo;
        return null;
    }

    /**
     * Valida se a string contém apenas letras, acentos e espaços.
     * @param valor A string de entrada.
     * @return A string tratada se for válida, ou null se for inválida.
     */
    private static String validarApenasLetras(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null; // Nulo ou vazio é inválido
        }
        String valorLimpo = valor.trim();

        // Regex que permite letras, acentos comuns e espaços
        if (valorLimpo.matches("^[a-zA-ZÀ-ú\\s]+$")) {
            return valorLimpo; // Válido
        } else {
            return null; // Inválido (contém números ou símbolos)
        }
    }


    public static void exibirFarmacias() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Farmácias ===");

        // Usando a View 'vw_farmacias_ativas' (do seu SQL)
        String sql = "SELECT * FROM vw_farmacias_ativas;";
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean existe = false;
            while (rs.next()) {
                existe = true;
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nome fantasia: " + rs.getString("nome_fantasia"));
                System.out.println("CNPJ: " + rs.getString("cnpj"));
                System.out.println("Telefone: " + rs.getString("telefone"));
                System.out.println("Endereço: " + rs.getString("rua") + ", " + rs.getInt("numero"));
                System.out.println("========================================================");
            }
            if (!existe) {
                System.out.println("Nenhuma farmácia ativa encontrada.");
            }

        } catch (SQLException e){
            System.out.println("Erro ao exibir farmácias: " + e.getMessage());
        }

        // Menu de admin
        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir nova farmácia (requer usuário e endereço pré-cadastrados)");
        System.out.println("2. Atualizar farmácia");
        System.out.println("3. Desativar farmácia (via usuário)");
        System.out.println("4. Sair da aba farmácias");

        int opcao;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (Exception e) { opcao = 0; }

        Farmacia f = new Farmacia();
        switch (opcao) {
            case 1:
                int idFarmacia = f.inserirFarmacia(sc, 0, "0");
                break;
            case 2:
                atualizarFarmacia(sc, 0);
                break;
            case 3:
                desativarFarmacia(sc); // Usando soft-delete
                break;
            case 4:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida");
                break;
        }
    }

    public int inserirFarmacia(Scanner sc, int idUsuario, String idEndereco) {
        int idFarmacia = 0;

        if (idUsuario == 0) {
            boolean valido = false;
            System.out.println("--- Seleção de Usuário ---");
            System.out.println("Listando usuários do tipo 'farmacia' disponíveis (que não são farmácias):");

            // Lista usuários do grupo 'farmacia' (id 3) que não estão em 'farmacias'
            // (Seu SQL tornou farmacias.usuario_id UNIQUE, então isso é crucial)
            String sqlUsers = """
                SELECT u.id, u.email FROM usuarios u 
                JOIN usuarioGrupo ug ON u.id = ug.usuario_id 
                WHERE ug.grupo_id = 3 AND u.situacao = 'ativo'
                AND u.id NOT IN (SELECT f.usuario_id FROM farmacias f)
            """;

            try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sqlUsers)) {
                ResultSet rs = stmt.executeQuery();
                boolean userDisponivel = false;
                while(rs.next()){
                    userDisponivel = true;
                    System.out.println("ID: " + rs.getInt("id") + " | Email: " + rs.getString("email"));
                }
                if (!userDisponivel){
                    System.out.println("Nenhum usuário 'farmacia' disponível. Crie um usuário 'farmacia' primeiro.");
                    return 0; // Aborta a inserção
                }
            } catch (SQLException e) {
                System.out.println("Erro ao listar usuários: " + e.getMessage());
                return 0;
            }

            do{
                System.out.println("Escolha um ID de usuário válido da lista acima:");
                if (sc.hasNextInt()) {
                    idUsuario = sc.nextInt();
                    sc.nextLine(); // Limpa buffer
                    if (idUsuario > 0) valido = true; // (Idealmente, verificar se o ID é da lista)
                } else {
                    System.out.println("Valor inválido! Não é um número inteiro.");
                    sc.nextLine(); // limpa o buffer
                }
            } while(!valido);
        }

        if (idEndereco.equals("0")) {
            boolean valido = false;
            System.out.println("\n--- Seleção de Endereço ---");
            Endereco.exibirEnderecos(null, 0); // Apenas lista os endereços
            do{
                System.out.println("Escolha um ID de endereço válido (ex: 4a2b3c1):");
                idEndereco = sc.nextLine();

                String sql = "SELECT * FROM enderecos WHERE id = ?";
                try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, idEndereco);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()){
                        valido = true;
                    } else {
                        System.out.println("ID de endereço não encontrado. Tente novamente.");
                        idEndereco = "0";
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao validar endereço: " + e.getMessage());
                }
            } while(!valido);
        }

        // --- Coleta de Dados da Farmácia ---
        try (Connection con = Conexao.getConnection()) {
            System.out.println("\n--- Dados da Farmácia ---");

            String nomeJuridico;
            do {
                System.out.println("Digite o nome jurídico (razão social):");
                nomeJuridico = validarApenasLetras(sc.nextLine());
                if (nomeJuridico == null) {
                    System.out.println("ERRO: Nome inválido. Digite apenas letras, acentos e espaços.");
                }
            } while (nomeJuridico == null);

            String nomeFantasia;
            do {
                System.out.println("Digite o nome fantasia:");
                nomeFantasia = validarApenasLetras(sc.nextLine()); // Reutilizando a validação
                if (nomeFantasia == null) {
                    System.out.println("ERRO: Nome inválido. Digite apenas letras, acentos e espaços.");
                }
            } while (nomeFantasia == null);


            String cnpjValidado;
            do {
                System.out.println("Digite o CNPJ (14 dígitos): ");
                String cnpj = sc.nextLine();
                cnpjValidado = validarNumero(cnpj, 14, 14);
                if (cnpjValidado == null) System.out.println("CNPJ inválido. Digite novamente.");
            } while (cnpjValidado == null);

            String alvaraValidado;
            do {
                System.out.println("Digite o alvará sanitário: ");
                String alvara = sc.nextLine();

                while (!alvara.matches("\\d+")) {
                    System.out.println("Entrada inválida! Digite apenas números no alvará sanitário.");
                    System.out.print("Digite novamente o alvará sanitário: ");
                    alvara = sc.nextLine();
                }

                alvaraValidado = alvara; // guarda o valor válido
                System.out.println("Alvará sanitário válido: " + alvaraValidado);
            } while (false);

            String responsavel;
            do {
                System.out.println("Digite o nome do responsável técnico (farmacêutico):");
                responsavel = validarApenasLetras(sc.nextLine());
                if (responsavel == null) {
                    System.out.println("ERRO: Nome inválido. Digite apenas letras, acentos e espaços.");
                }
            } while (responsavel == null);

            String crfValidado;
            do {
                System.out.println("Digite o CRF do responsável:");
                String crf = sc.nextLine();
                crfValidado = validarNumero(crf, 5, 5);
                if (crfValidado == null) System.out.println("CRF inválido.");
            } while (crfValidado == null);

            String telefoneValidado;
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos):");
                String telefone = sc.nextLine();
                telefoneValidado = validarNumero(telefone, 10, 11);
                if (telefoneValidado == null) System.out.println("Telefone inválido.");
            } while (telefoneValidado == null);

            String sql = "INSERT INTO farmacias (nome_juridico, nome_fantasia, cnpj, alvara_sanitario, responsavel_tecnico, crf, telefone, endereco_id, usuario_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nomeJuridico);
                stmt.setString(2, nomeFantasia);
                stmt.setString(3, cnpjValidado);
                stmt.setString(4, alvaraValidado);
                stmt.setString(5, responsavel);
                stmt.setString(6, crfValidado);
                stmt.setString(7, telefoneValidado);
                stmt.setString(8, idEndereco);
                stmt.setInt(9, idUsuario);
                stmt.executeUpdate();
                System.out.println("Farmácia inserida com sucesso!");

                String sql4 = "SELECT id FROM farmacias WHERE usuario_id = ?";
                try (PreparedStatement stmt4 = con.prepareStatement(sql4)) {
                    stmt4.setInt(1, idUsuario);
                    ResultSet rs = stmt4.executeQuery();
                    if (rs.next()){
                        idFarmacia = rs.getInt("id");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao inserir farmácia: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }

        return idFarmacia;
    }

    public static void atualizarFarmacia(Scanner sc, int farmaciaId) {

        try (Connection con = Conexao.getConnection()) {

            // ============================================================
            // 1) SE farmáciaId == 0 → LISTA FARMÁCIAS PARA ESCOLHER
            // ============================================================
            if (farmaciaId == 0) {

                System.out.println("--- Seleção de Farmácia ---");

                String sqlList = "SELECT id, nome_fantasia FROM farmacias";

                try (PreparedStatement stmt = con.prepareStatement(sqlList);
                     ResultSet rs = stmt.executeQuery()) {

                    boolean existe = false;

                    while (rs.next()) {
                        existe = true;
                        System.out.println("ID: " + rs.getInt("id") +
                                " | Nome Fantasia: " + rs.getString("nome_fantasia"));
                    }

                    if (!existe) {
                        System.out.println("Nenhuma farmácia encontrada.");
                        return;
                    }

                }

                boolean valido = false;
                do {
                    System.out.println("Digite o ID da farmácia que deseja atualizar:");
                    if (sc.hasNextInt()) {
                        farmaciaId = sc.nextInt();
                        sc.nextLine();
                        valido = true;
                    } else {
                        System.out.println("Valor inválido!");
                        sc.nextLine();
                    }
                } while (!valido);
            }

            // ============================================================
            // 2) CARREGAR DADOS DA FARMÁCIA (sem fechar ResultSet antes!)
            // ============================================================
            String sqlSel = "SELECT * FROM farmacias WHERE id = ?";
            String nomeJuridicoAtual = null;
            String nomeFantasiaAtual = null;
            String cnpjAtual = null;
            String alvaraAtual = null;
            String responsavelAtual = null;
            String crfAtual = null;
            String telefoneAtual = null;
            String enderecoAtual = null;
            int usuarioAtual = 0;

            try (PreparedStatement stmt = con.prepareStatement(sqlSel)) {
                stmt.setInt(1, farmaciaId);

                try (ResultSet rs = stmt.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("Farmácia não encontrada.");
                        return;
                    }

                    // Carrega TUDO em variáveis (AGORA PODE FECHAR o ResultSet)
                    nomeJuridicoAtual = rs.getString("nome_juridico");
                    nomeFantasiaAtual = rs.getString("nome_fantasia");
                    cnpjAtual = rs.getString("cnpj");
                    alvaraAtual = rs.getString("alvara_sanitario");
                    responsavelAtual = rs.getString("responsavel_tecnico");
                    crfAtual = rs.getString("crf");
                    telefoneAtual = rs.getString("telefone");
                    enderecoAtual = rs.getString("endereco_id");
                    usuarioAtual = rs.getInt("usuario_id");
                }
            }

            // ============================================================
            // 3) SOLICITAR NOVOS DADOS AO USUÁRIO
            // ============================================================

            System.out.println("Novo nome jurídico (Enter para manter: " + nomeJuridicoAtual + "):");
            String nomeJuridico = sc.nextLine();
            if (nomeJuridico.isEmpty()) nomeJuridico = nomeJuridicoAtual;

            System.out.println("Novo nome fantasia (Enter para manter: " + nomeFantasiaAtual + "):");
            String nomeFantasia = sc.nextLine();
            if (nomeFantasia.isEmpty()) nomeFantasia = nomeFantasiaAtual;

            System.out.println("Novo CNPJ (Enter para manter: " + cnpjAtual + "):");
            String cnpj = sc.nextLine();
            if (cnpj.isEmpty()) cnpj = cnpjAtual;

            System.out.println("Novo alvará (Enter para manter: " + alvaraAtual + "):");
            String alvara = sc.nextLine();
            if (alvara.isEmpty()) alvara = alvaraAtual;

            System.out.println("Novo responsável técnico (Enter para manter: " + responsavelAtual + "):");
            String responsavel = sc.nextLine();
            if (responsavel.isEmpty()) responsavel = responsavelAtual;

            System.out.println("Novo CRF (Enter para manter: " + crfAtual + "):");
            String crf = sc.nextLine();
            if (crf.isEmpty()) crf = crfAtual;

            System.out.println("Novo telefone (Enter para manter: " + telefoneAtual + "):");
            String telefone = sc.nextLine();
            if (telefone.isEmpty()) telefone = telefoneAtual;

            // ============================================================
            // 4) UPDATE FINAL
            // ============================================================

            String sqlUp =
                    "UPDATE farmacias SET nome_juridico=?, nome_fantasia=?, cnpj=?, alvara_sanitario=?, " +
                            "responsavel_tecnico=?, crf=?, telefone=? WHERE id=?";

            try (PreparedStatement upd = con.prepareStatement(sqlUp)) {

                upd.setString(1, nomeJuridico);
                upd.setString(2, nomeFantasia);
                upd.setString(3, cnpj);
                upd.setString(4, alvara);
                upd.setString(5, responsavel);
                upd.setString(6, crf);
                upd.setString(7, telefone);
                upd.setInt(8, farmaciaId);

                upd.executeUpdate();
            }

            System.out.println("Farmácia atualizada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }



    public static void desativarFarmacia(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID da farmácia a DESATIVAR:");
            int id = Integer.parseInt(sc.nextLine());

            System.out.println("Tem certeza que deseja desativar a farmácia ID " + id + "? (S/N)");
            System.out.println("Isso irá desativar o usuário associado e todos os seus produtos deixarão de ser exibidos.");
            String conf = sc.nextLine();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            int usuarioId = 0;
            String sqlFind = "SELECT usuario_id FROM farmacias WHERE id = ?";
            try (PreparedStatement stmtFind = con.prepareStatement(sqlFind)) {
                stmtFind.setInt(1, id);
                ResultSet rs = stmtFind.executeQuery();
                if (rs.next()) {
                    usuarioId = rs.getInt("usuario_id");
                } else {
                    System.out.println("Erro: Farmácia com ID " + id + " não encontrada.");
                    return;
                }
            }

            if (usuarioId > 0) {
                String sqlUpdate = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setInt(1, usuarioId);
                    int rows = stmtUpdate.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Farmácia (e seu usuário associado) desativada com sucesso!");
                    } else {
                        System.out.println("Erro: Usuário associado (ID: " + usuarioId + ") não foi encontrado.");
                    }
                }
            } else {
                System.out.println("Erro: A farmácia " + id + " não possui um usuário associado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao desativar farmácia: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }
}