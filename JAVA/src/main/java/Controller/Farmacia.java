package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Farmacia {

    /**
     * Valida se a string contém apenas números e tem o tamanho correto.
     * @param valor A string de entrada.
     * @param TamMin Tamanho mínimo.
     * @param TamMax Tamanho máximo.
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

    /**
     * Exibe o menu de gerenciamento de farmácias para o ADM.
     */
    public static void exibirFarmacias() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Farmácias ===");

        // Usa a VIEW (definida no seu SQL) para mostrar apenas farmácias ativas
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

        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir nova farmácia (requer usuário e endereço pré-cadastrados)");
        System.out.println("2. Atualizar farmácia");
        System.out.println("3. Desativar farmácia (via usuário)");
        System.out.println("4. Sair da aba farmácias");

        int opcao = 0;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            // Deixa 'opcao' como 0, o 'default' do switch tratará como inválido
        }

        Farmacia f = new Farmacia();
        switch (opcao) {
            case 1:
                int idFarmacia = f.inserirFarmacia(sc, 0, "0");
                break;
            case 2:
                atualizarFarmacia(sc);
                break;
            case 3:
                desativarFarmacia(sc); // Soft delete
                break;
            case 4:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida");
                break;
        }
    }

    /**
     * Processo de inserir uma nova farmácia.
     * @param sc Scanner
     * @param idUsuario ID do usuário (se 0, pedirá ao usuário)
     * @param idEndereco ID do endereço (se "0", pedirá ao usuário)
     * @return O ID da nova farmácia criada
     */
    public int inserirFarmacia(Scanner sc, int idUsuario, String idEndereco) {
        int idFarmacia = 0;

        // --- 1. Seleção de Usuário ---
        if (idUsuario == 0) {
            boolean valido = false;
            System.out.println("--- Seleção de Usuário ---");
            System.out.println("Listando usuários do tipo 'farmacia' disponíveis (que não estão em 'farmacias'):");

            // Query para achar usuários 'farmacia' (grupo_id = 3) que NÃO ESTÃO na tabela 'farmacias'
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
                try {
                    idUsuario = Integer.parseInt(sc.nextLine());
                    // (Idealmente, verificar se o ID escolhido é válido e da lista)
                    if (idUsuario > 0) valido = true;
                } catch (NumberFormatException e) {
                    System.out.println("Valor inválido! Não é um número inteiro.");
                }
            } while(!valido);
        }

        // --- 2. Seleção de Endereço ---
        if (idEndereco.equals("0")) {
            boolean valido = false;
            System.out.println("\n--- Seleção de Endereço ---");
            Endereco.exibirEnderecos(null); // Apenas lista os endereços
            do{
                System.out.println("Escolha um ID de endereço válido (ex: 4a2b3c1):");
                idEndereco = sc.nextLine();

                String sql = "SELECT id FROM enderecos WHERE id = ?";
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

        // --- 3. Coleta de Dados da Farmácia (com validação) ---
        try (Connection con = Conexao.getConnection()) {
            System.out.println("\n--- Dados da Farmácia ---");

            String nomeJuridico;
            do {
                System.out.println("Digite o nome jurídico (razão social):");
                nomeJuridico = validarApenasLetras(sc.nextLine());
                if (nomeJuridico == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (nomeJuridico == null);

            String nomeFantasia;
            do {
                System.out.println("Digite o nome fantasia:");
                // Nota: Nomes fantasia podem ter números (ex: "Drogaria 24h").
                // Vamos usar uma validação mais permissiva ou apenas checar se não está vazio.
                // Por agora, mantendo o validarApenasLetras que você pediu:
                nomeFantasia = validarApenasLetras(sc.nextLine());
                if (nomeFantasia == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (nomeFantasia == null);


            String cnpjValidado;
            do {
                System.out.println("Digite o CNPJ (14 dígitos): ");
                cnpjValidado = validarNumero(sc.nextLine(), 14, 14);
                if (cnpjValidado == null) System.out.println("CNPJ inválido. Digite 14 números.");
            } while (cnpjValidado == null);

            System.out.println("Digite o alvará sanitário: ");
            String alvara = sc.nextLine();

            String responsavel;
            do {
                System.out.println("Digite o nome do responsável técnico (farmacêutico):");
                responsavel = validarApenasLetras(sc.nextLine());
                if (responsavel == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (responsavel == null);

            System.out.println("Digite o CRF do responsável:");
            String crf = sc.nextLine();

            String telefoneValidado;
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos):");
                telefoneValidado = validarNumero(sc.nextLine(), 10, 11);
                if (telefoneValidado == null) System.out.println("Telefone inválido (DDD + Número).");
            } while (telefoneValidado == null);

            String sql = "INSERT INTO farmacias (nome_juridico, nome_fantasia, cnpj, alvara_sanitario, responsavel_tecnico, crf, telefone, endereco_id, usuario_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nomeJuridico);
                stmt.setString(2, nomeFantasia);
                stmt.setString(3, cnpjValidado);
                stmt.setString(4, alvara);
                stmt.setString(5, responsavel);
                stmt.setString(6, crf);
                stmt.setString(7, telefoneValidado);
                stmt.setString(8, idEndereco);
                stmt.setInt(9, idUsuario);
                stmt.executeUpdate();
                System.out.println("Farmácia inserida com sucesso!");

                // Pega o ID da farmácia recém-criada (baseado no usuario_id, que é UNIQUE)
                String sql4 = "SELECT id FROM farmacias WHERE usuario_id = ?";
                try (PreparedStatement stmt4 = con.prepareStatement(sql4)) {
                    stmt4.setInt(1, idUsuario);
                    ResultSet rs = stmt4.executeQuery();
                    if (rs.next()){
                        idFarmacia = rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir farmácia: " + e.getMessage());
        }
        return idFarmacia;
    }

    /**
     * Atualiza uma farmácia. Agora é PÚBLICO para ser chamado pelo Main.
     */
    public static void atualizarFarmacia(Scanner sc) {
        int id = 0; // Inicializa o ID

        try (Connection con = Conexao.getConnection()) {

            // --- CORREÇÃO: Validação de ID (NumberFormatException) ---
            boolean idValido = false;
            do {
                System.out.println("Digite o ID da farmácia que deseja atualizar: ");
                String idInput = sc.nextLine();

                try {
                    id = Integer.parseInt(idInput); // Tenta converter
                    if (id > 0) {
                        idValido = true; // Sucesso!
                    } else {
                        System.out.println("ERRO: O ID deve ser um número positivo.");
                    }
                } catch (NumberFormatException e) {
                    // Se o 'parseInt' falhar (ex: string vazia ou "abc")
                    System.out.println("ERRO: Entrada inválida. Digite apenas números.");
                }
            } while (!idValido);
            // --- FIM DA CORREÇÃO ---

            String sel = "SELECT * FROM farmacias WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setInt(1, id); // 'id' agora é garantido como um int válido
                ResultSet rs = selStmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Farmácia não encontrada.");
                    return;
                }

                // --- Coleta de Dados Atuais (com correção no endereco_id) ---
                String nomeJuridicoAtual = rs.getNString("nome_juridico");
                String nomeFantasiaAtual = rs.getString("nome_fantasia");
                String cnpjAtual = rs.getString("cnpj");
                String alvaraAtual = rs.getString("alvara_sanitario");
                String responsavelAtual = rs.getString("responsavel_tecnico");
                String crfAtual = rs.getString("crf");
                String telefoneAtual = rs.getString("telefone");
                // CORRIGIDO: endereco_id é String (VARCHAR(7))
                String enderecoAtual = rs.getString("endereco_id");

                // --- Coleta de Novos Dados (com validação e skip "Enter") ---
                String nomeJuridico;
                do {
                    System.out.println("Digite o novo nome juridico (ou Enter para manter: " + nomeJuridicoAtual + "):");
                    nomeJuridico = sc.nextLine();
                    if (nomeJuridico.isEmpty()) break; // Permite pular
                    nomeJuridico = validarApenasLetras(nomeJuridico);
                    if (nomeJuridico == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
                } while (nomeJuridico == null);

                String nomeFantasia;
                do {
                    System.out.println("Digite o novo nome fantasia (ou Enter para manter: " + nomeFantasiaAtual + "):");
                    nomeFantasia = sc.nextLine();
                    if (nomeFantasia.isEmpty()) break;
                    nomeFantasia = validarApenasLetras(nomeFantasia); // Mantendo sua restrição
                    if (nomeFantasia == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
                } while (nomeFantasia == null);

                String cnpj;
                do {
                    System.out.println("Digite o novo CNPJ (14 dígitos) ou Enter para manter: " + cnpjAtual);
                    String cnpjDigitado = sc.nextLine();
                    if (cnpjDigitado.isEmpty()) { cnpj = ""; break; } // Permite pular
                    cnpj = validarNumero(cnpjDigitado, 14, 14);
                    if (cnpj == null) System.out.println("CNPJ inválido (14 números).");
                } while (cnpj == null);

                System.out.println("Digite o novo alvará (ou Enter para manter: " + alvaraAtual + "):");
                String alvara = sc.nextLine();

                String responsavel;
                do {
                    System.out.println("Digite o novo responsável técnico (ou Enter para manter: " + responsavelAtual + "):");
                    responsavel = sc.nextLine();
                    if (responsavel.isEmpty()) break;
                    responsavel = validarApenasLetras(responsavel);
                    if (responsavel == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
                } while (responsavel == null);

                System.out.println("Digite o novo CRF (ou Enter para manter: " + crfAtual + "):");
                String crf = sc.nextLine();

                String telefone;
                do {
                    System.out.println("Digite o novo telefone (10 ou 11 dígitos) ou Enter para manter: " + telefoneAtual);
                    String telDigitado = sc.nextLine();
                    if (telDigitado.isEmpty()) { telefone = ""; break; } // Permite pular
                    telefone = validarNumero(telDigitado, 10, 11);
                    if (telefone == null) System.out.println("Telefone inválido (10 ou 11 números).");
                } while (telefone == null);

                // CORRIGIDO: endereco_id é String
                System.out.println("Digite novo endereco_id (ou Enter para manter: " + enderecoAtual + "):");
                String enderecoId = sc.nextLine();

                // --- Montagem do SQL Dinâmico ---
                StringBuilder sql = new StringBuilder("UPDATE farmacias SET ");
                List<Object> params = new java.util.ArrayList<>();
                boolean first = true;

                if (!nomeJuridico.isEmpty()) { if(!first) sql.append(",");sql.append("nome_juridico = ?"); params.add (nomeJuridico); first = false;}
                if (!nomeFantasia.isEmpty()) { if(!first) sql.append(", "); sql.append("nome_fantasia = ?"); params.add(nomeFantasia); first = false; }
                if (!cnpj.isEmpty()) { if(!first) sql.append(", "); sql.append("cnpj = ?"); params.add(cnpj); first = false; }
                if (!alvara.isEmpty()) { if(!first) sql.append(", "); sql.append("alvara_sanitario = ?"); params.add(alvara); first = false; }
                if (!responsavel.isEmpty()) { if(!first) sql.append(", "); sql.append("responsavel_tecnico = ?"); params.add(responsavel); first = false; }
                if (!crf.isEmpty()) { if(!first) sql.append(", "); sql.append("crf = ?"); params.add(crf); first = false; }
                if (!telefone.isEmpty()) { if(!first) sql.append(", "); sql.append("telefone = ?"); params.add(telefone); first = false; }
                if (!enderecoId.isEmpty()) { if(!first) sql.append(", "); sql.append("endereco_id = ?"); params.add(enderecoId); first = false; }

                if (params.isEmpty()) {
                    System.out.println("Nenhum campo para atualizar.");
                    return;
                }

                sql.append(" WHERE id = ?");
                params.add(id); // Adiciona o 'id' (INT) no final

                try (PreparedStatement upd = con.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        Object p = params.get(i);
                        // O 'id' (último) é Int, 'enderecoId' (se houver) é String
                        if (p instanceof Integer) {
                            upd.setInt(i + 1, (Integer) p);
                        } else {
                            upd.setString(i + 1, p.toString());
                        }
                    }
                    upd.executeUpdate();
                    System.out.println("Farmácia atualizada com sucesso!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar/atualizar farmácia: " + e.getMessage());
        }
    }

    /**
     * Desativa uma farmácia (Soft Delete) ao desativar o usuário associado.
     * Agora é PÚBLICO para ser chamado pelo Main.
     */
    public static void desativarFarmacia(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {

            int id = 0;
            boolean idValido = false;
            do {
                System.out.println("Digite o ID da farmácia a DESATIVAR:");
                String idInput = sc.nextLine();
                try {
                    id = Integer.parseInt(idInput);
                    if (id > 0) idValido = true;
                    else System.out.println("ERRO: O ID deve ser um número positivo.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Entrada inválida. Digite apenas números.");
                }
            } while (!idValido);


            System.out.println("Tem certeza que deseja desativar a farmácia ID " + id + "? (S/N)");
            System.out.println("Isso irá desativar o usuário associado e todos os seus produtos deixarão de ser exibidos.");
            String conf = sc.nextLine();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            // 1. Achar o usuario_id da farmácia
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

            // 2. Desativar o usuário (Soft Delete)
            if (usuarioId > 0) {
                String sqlUpdate = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setInt(1, usuarioId);
                    int rows = stmtUpdate.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Farmácia (e seu usuário associado) desativada com sucesso!");
                    } else {
                        System.out.println("Erro: Usuário associado (ID: " + usuarioId + ") não foi encontrado para desativar.");
                    }
                }
            } else {
                System.out.println("Erro: A farmácia " + id + " não possui um usuário associado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao desativar farmácia: " + e.getMessage());
        }
    }
}