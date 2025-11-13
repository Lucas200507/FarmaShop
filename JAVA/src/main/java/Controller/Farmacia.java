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
        System.out.println("1. Inserir nova farmácia (Fluxo Completo)");
        System.out.println("2. Atualizar farmácia");
        System.out.println("3. Desativar farmácia (via usuário)");
        System.out.println("4. Sair da aba farmácias");

        int opcao = 0;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            // Deixa 'opcao' como 0, o 'default' do switch tratará como inválido
        }

        switch (opcao) {
            case 1:
                // Chama o novo fluxo de inserção
                inserirFarmacia(sc);
                break;
            case 2:
                // Se é o ADM, ele passa 0, forçando o método a perguntar o ID.
                atualizarFarmacia(sc, 0);
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
     * Processo de inserir uma nova farmácia (NOVO FLUXO CORRIGIDO).
     * Guia o ADM na criação do Usuário, Endereço e Farmácia em um passo-a-passo.
     * @param sc Scanner
     */
    public static void inserirFarmacia(Scanner sc) {
        System.out.println("\n=== INSERIR NOVA FARMÁCIA (Passo-a-passo) ===");

        // Instâncias dos outros controllers para chamar seus métodos
        Usuario u = new Usuario();
        Endereco e = new Endereco();

        int novoUsuarioId = 0;
        String novoEnderecoId = "0";

        // --- ETAPA 1: Criar o Usuário ---
        System.out.println("--- Etapa 1: Dados de Login (Usuário) ---");
        // Chama o 'inserirUsuario' do controller Usuario, forçando o grupo 3 (farmacia)
        novoUsuarioId = u.inserirUsuario(sc, 3);

        if (novoUsuarioId == 0) {
            System.out.println("ERRO: Falha ao criar o usuário. Abortando cadastro da farmácia.");
            return;
        }
        System.out.println("Usuário (ID: " + novoUsuarioId + ") criado com sucesso.");

        // --- ETAPA 2: Criar o Endereço ---
        System.out.println("\n--- Etapa 2: Dados de Endereço ---");
        // Chama o 'inserirEndereco' do controller Endereco
        novoEnderecoId = e.inserirEndereco(sc);

        if (novoEnderecoId.equals("0")) {
            System.out.println("ERRO: Falha ao criar o endereço. Abortando cadastro da farmácia.");
            // (Opcional: deletar o usuário criado na Etapa 1 para não deixar "lixo")
            return;
        }
        System.out.println("Endereço (ID: " + novoEnderecoId + ") criado com sucesso.");


        // --- ETAPA 3: Criar a Farmácia (com validação) ---
        try (Connection con = Conexao.getConnection()) {
            System.out.println("\n--- Etapa 3: Dados da Farmácia ---");

            String nomeJuridico;
            do {
                System.out.println("Digite o nome jurídico (razão social):");
                nomeJuridico = validarApenasLetras(sc.nextLine());
                if (nomeJuridico == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (nomeJuridico == null);

            String nomeFantasia;
            do {
                System.out.println("Digite o nome fantasia:");
                nomeFantasia = validarApenasLetras(sc.nextLine());
                if (nomeFantasia == null) System.out.println("ERRO: Nome fantasia inválido. Digite apenas letras e espaços.");
            } while (nomeFantasia == null);


            String cnpjValidado;
            do {
                System.out.println("Digite o CNPJ (14 dígitos): ");
                cnpjValidado = validarNumero(sc.nextLine(), 14, 14);
                if (cnpjValidado == null) System.out.println("CNPJ inválido. Digite 14 números.");
                // (Opcional: adicionar validação de CNPJ duplicado)
            } while (cnpjValidado == null);

            String alvara;
            do {
                System.out.println("Digite o alvará sanitário (apenas números, 4-50 dígitos): ");
                alvara = validarNumero(sc.nextLine(), 4, 50);
                if (alvara == null) System.out.println("ERRO: Alvará inválido. Digite de 4 a 50 números.");
            } while (alvara == null);

            String responsavel;
            do {
                System.out.println("Digite o nome do responsável técnico (farmacêutico):");
                responsavel = validarApenasLetras(sc.nextLine());
                if (responsavel == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (responsavel == null);

            String crf;
            do {
                System.out.println("Digite o CRF do responsável (apenas números, 1 a 5 dígitos):");
                crf = validarNumero(sc.nextLine(), 1, 5); // Corrigido
                if (crf == null) System.out.println("ERRO: CRF inválido. Digite de 1 a 5 números.");
            } while (crf == null);

            String telefoneValidado;
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos):");
                telefoneValidado = validarNumero(sc.nextLine(), 10, 11);
                if (telefoneValidado == null) System.out.println("Telefone inválido (DDD + Número).");
                // (Opcional: adicionar validação de telefone duplicado)
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
                stmt.setString(8, novoEnderecoId); // ID do Endereço da Etapa 2
                stmt.setInt(9, novoUsuarioId); // ID do Usuário da Etapa 1
                stmt.executeUpdate();

                System.out.println("\n==============================================");
                System.out.println("FARMÁCIA CADASTRADA COM SUCESSO!");
                System.out.println("==============================================");
            }
        } catch (SQLException ex) {
            System.out.println("ERRO CRÍTICO ao inserir farmácia: " + ex.getMessage());
            // (Opcional: deletar o usuário e endereço criados)
        }
    }

    /**
     * Atualiza uma farmácia.
     * @param sc Scanner
     * @param farmaciaIdLogada O ID da farmácia logada (do perfilId).
     * Se for 0, o método entende que é o ADM
     * e PERGUNTA qual ID deve ser atualizado.
     */
    public static void atualizarFarmacia(Scanner sc, int farmaciaIdLogada) {
        // Inicializa a variável para 0
        int idParaAtualizar = 0;

        try (Connection con = Conexao.getConnection()) {

            if (farmaciaIdLogada == 0) {
                // --- FLUXO DO ADM ---
                // Se o ID é 0, é o ADM. Precisamos perguntar o ID.
                boolean idValido = false;
                do {
                    System.out.println("Digite o ID da farmácia que deseja atualizar (Visão ADM): ");
                    String idInput = sc.nextLine();
                    try {
                        idParaAtualizar = Integer.parseInt(idInput); // Tenta converter
                        if (idParaAtualizar > 0) {
                            idValido = true; // Sucesso!
                        } else {
                            System.out.println("ERRO: O ID deve ser um número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ERRO: Entrada inválida. Digite apenas números.");
                    }
                } while (!idValido);
            } else {
                // --- FLUXO DA FARMÁCIA LOGADA ---
                // O ID já foi fornecido, pula a pergunta.
                idParaAtualizar = farmaciaIdLogada;
                System.out.println("Atualizando dados da sua farmácia (ID: " + idParaAtualizar + ")");
            }


            String sel = "SELECT * FROM farmacias WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setInt(1, idParaAtualizar); // Usa o ID (perguntado ou do login)
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
                    nomeFantasia = validarApenasLetras(nomeFantasia);
                    if (nomeFantasia == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
                } while (nomeFantasia == null);

                String cnpj;
                do {
                    System.out.println("Digite o novo CNPJ (14 dígitos) ou Enter para manter: " + cnpjAtual);
                    String cnpjDigitado = sc.nextLine();
                    if (cnpjDigitado.isEmpty()) { cnpj = ""; break; } // Permite pular
                    cnpj = validarNumero(cnpjDigitado, 14, 14);
                    if (cnpj == null) System.out.println("CNPJ inválido (14 números).");
                    // (Opcional: validar unicidade se cnpj != cnpjAtual)
                } while (cnpj == null);

                String alvara;
                do {
                    System.out.println("Digite o novo alvará (apenas números, 4-50 dígitos) (ou Enter para manter: " + alvaraAtual + "):");
                    String alvaraDigitado = sc.nextLine();
                    if (alvaraDigitado.isEmpty()) { alvara = ""; break; } // Permite pular
                    alvara = validarNumero(alvaraDigitado, 4, 50);
                    if (alvara == null) System.out.println("ERRO: Alvará inválido. Digite de 4 a 50 números.");
                } while (alvara == null);

                String responsavel;
                do {
                    System.out.println("Digite o novo responsável técnico (ou Enter para manter: " + responsavelAtual + "):");
                    responsavel = sc.nextLine();
                    if (responsavel.isEmpty()) break;
                    responsavel = validarApenasLetras(responsavel);
                    if (responsavel == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
                } while (responsavel == null);

                String crf;
                do {
                    System.out.println("Digite o novo CRF (apenas números, 1-5 dígitos) (ou Enter para manter: " + crfAtual + "):");
                    String crfDigitado = sc.nextLine();
                    if (crfDigitado.isEmpty()) { crf = ""; break; } // Permite pular
                    crf = validarNumero(crfDigitado, 1, 5); // Corrigido
                    if (crf == null) System.out.println("ERRO: CRF inválido. Digite de 1 a 5 números.");
                } while (crf == null);

                String telefone;
                do {
                    System.out.println("Digite o novo telefone (10 ou 11 dígitos) ou Enter para manter: " + telefoneAtual);
                    String telDigitado = sc.nextLine();
                    if (telDigitado.isEmpty()) { telefone = ""; break; } // Permite pular
                    telefone = validarNumero(telDigitado, 10, 11);
                    if (telefone == null) System.out.println("Telefone inválido (10 ou 11 números).");
                    // (Opcional: validar unicidade se telefone != telefoneAtual)
                } while (telefone == null);

                System.out.println("Digite novo endereco_id (ou Enter para manter: " + enderecoAtual + "):");
                String enderecoId = sc.nextLine();
                // (Opcional: validar se o novo ID de endereço existe na tabela 'enderecos')

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
                params.add(idParaAtualizar); // Adiciona o 'id' (INT) no final

                try (PreparedStatement upd = con.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        Object p = params.get(i);
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