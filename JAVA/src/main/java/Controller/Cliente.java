package Controller;

import Database.Conexao;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Cliente {
    private static String validarNumero(String valor, int TamMin, int TamMax) {
        if (valor == null) return null;
        String valorLimpo = valor.replaceAll("[^0-9]", "");
        if (valorLimpo.length() >= TamMin && valorLimpo.length() <= TamMax) return valorLimpo;
        return null;
    }

    private static boolean validarUnico(String valor, String tipo) {
        // Query segura (usa PreparedStatement)
        String sql = "SELECT 1 FROM clientes WHERE " + tipo + " = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, valor);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Se rs.next() for true, significa que o valor JÁ EXISTE
                System.out.println("ERRO: Este " + tipo + " (" + valor + ") já está cadastrado no sistema.");
                return false; // Inválido
            } else {
                return true; // Válido (não existe)
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar " + tipo + ": " + e.getMessage());
            return false; // Assume inválido em caso de erro
        }
    }


    /**
     * Valida se a string contém apenas letras, acentos e espaços.
     */
    private static String validarApenasLetras(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null; // Nulo ou vazio é inválido
        }
        String valorLimpo = valor.trim();

        if (valorLimpo.matches("^[a-zA-ZÀ-ú\\s]+$")) {
            return valorLimpo; // Válido
        } else {
            return null; // Inválido
        }
    }

    /**
     * Valida a data de nascimento (Formato AAAA-MM-DD e não ser futura).
     */
    private static LocalDate validarDataNascimento(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate dataNascimento = LocalDate.parse(dataStr, formatter);
            if (dataNascimento.isAfter(LocalDate.now())) {
                System.out.println("ERRO: A data de nascimento não pode ser uma data futura.");
                return null;
            }
            return dataNascimento; // Data válida
        } catch (DateTimeParseException e) {
            System.out.println("ERRO: Formato de data inválido. Use o padrão AAAA-MM-DD (ex: 1990-05-15).");
            return null;
        }
    }

    /**
     * Exibe o menu de gerenciamento de clientes para o ADM.
     */
    public static void exibirClientes() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== CLIENTES (Visão ADM) ===");

        // Mostra clientes ativos e inativos
        String sql = "SELECT c.*, u.situacao FROM clientes c JOIN usuarios u ON c.usuario_id = u.id ORDER BY c.nome";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean existe = false;
            while (rs.next()) {
                existe = true;
                System.out.println("ID Cliente: " + rs.getInt("id"));
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("CPF: " + rs.getString("cpf"));
                System.out.println("Telefone: " + rs.getString("telefone"));
                System.out.println("Situação Usuário: " + rs.getString("situacao"));
                System.out.println("============================================");
            }
            if (!existe) System.out.println("Nenhum cliente cadastrado.");

        } catch (SQLException e) {
            System.out.println("Erro ao exibir clientes: " + e.getMessage());
        }

        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir novo cliente (Fluxo Completo)");
        System.out.println("2. Atualizar cliente");
        System.out.println("3. Desativar cliente (Recomendado)");
        System.out.println("4. Sair da aba clientes");

        int opcao = 0;
        try {
            opcao = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) { /* Opção continua 0 */ }

        switch (opcao) {
            case 1:
                inserirCliente(sc); // Chama o novo fluxo
                break;
            case 2:
                // O método 'atualizarCliente' do ADM pede o ID do cliente (passando 0)
                atualizarCliente(sc, 0);
                break;
            case 3:
                desativarCliente(sc); // Corrigido para desativar
                break;
            case 4:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }


    public static void inserirCliente(Scanner sc) {
        System.out.println("\n=== CRIAR NOVA CONTA DE CLIENTE (Passo-a-passo) ===");

        // Instâncias dos outros controllers
        Usuario u = new Usuario();
        Endereco e = new Endereco();

        int novoUsuarioId = 0;
        String novoEnderecoId = "0";

        // --- ETAPA 1: Criar o Usuário ---
        System.out.println("--- Etapa 1: Dados de Login (Usuário) ---");
        // Chama o 'inserirUsuario' do controller Usuario, forçando o grupo 2 (cliente)
        novoUsuarioId = u.inserirUsuario(sc, 2);

        if (novoUsuarioId == 0) {
            System.out.println("ERRO: Falha ao criar o usuário. Abortando cadastro.");
            return;
        }
        System.out.println("Usuário (ID: " + novoUsuarioId + ") criado com sucesso.");

        // --- ETAPA 2: Criar o Endereço ---
        System.out.println("\n--- Etapa 2: Dados de Endereço ---");
        // Chama o 'inserirEndereco' do controller Endereco
        novoEnderecoId = e.inserirEndereco(sc);

        if (novoEnderecoId.equals("0")) {
            System.out.println("ERRO: Falha ao criar o endereço. Abortando cadastro.");
            // (Opcional: deletar o usuário criado na Etapa 1)
            return;
        }
        System.out.println("Endereço (ID: " + novoEnderecoId + ") criado com sucesso.");

        // --- ETAPA 3: Criar o Cliente (com validação) ---
        try (Connection con = Conexao.getConnection()) {
            System.out.println("\n--- Etapa 3: Dados Pessoais do Cliente ---");

            String nome;
            do {
                System.out.println("Digite o nome do cliente:");
                nome = validarApenasLetras(sc.nextLine());
                if (nome == null) System.out.println("ERRO: Nome inválido. Digite apenas letras e espaços.");
            } while (nome == null);


            String cpfValidado;
            do {
                System.out.println("Digite o CPF (11 dígitos, apenas números):");
                String cpfDigitado = sc.nextLine();
                cpfValidado = validarNumero(cpfDigitado, 11, 11);
                if (cpfValidado == null) {
                    System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos.");
                } else {
                    // Se o formato é válido, checa se é único
                    if (!validarUnico(cpfValidado, "cpf")) {
                        cpfValidado = null; // Falhou na validação de duplicado
                    }
                }
            } while (cpfValidado == null);

            String telefoneValidado;
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos, DDD + Número):");
                String telDigitado = sc.nextLine();
                telefoneValidado = validarNumero(telDigitado, 10, 11);
                if (telefoneValidado == null) {
                    System.out.println("ERRO: Telefone inválido (10 ou 11 dígitos).");
                } else {
                    // Se o formato é válido, checa se é único
                    if (!validarUnico(telefoneValidado, "telefone")) {
                        telefoneValidado = null; // Falhou na validação de duplicado
                    }
                }
            } while (telefoneValidado == null);

            LocalDate dataNascimentoValida = null;
            do {
                System.out.println("Digite a Data de nascimento (AAAA-MM-DD):");
                String dataNascimentoDigitada = sc.nextLine();
                dataNascimentoValida = validarDataNascimento(dataNascimentoDigitada);
                // O método validarDataNascimento já imprime o erro
            } while (dataNascimentoValida == null);

            // --- Inserção no Banco de Dados ---
            String sql = "INSERT INTO clientes (nome, cpf, telefone, data_nascimento, usuario_id, endereco_id) VALUES (?, ?, ?, ?, ?, ?)";
            // Pedimos o ID gerado (Auto_Increment)
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpfValidado);
                stmt.setString(3, telefoneValidado);
                stmt.setDate(4, Date.valueOf(dataNascimentoValida));
                stmt.setInt(5, novoUsuarioId); // ID da Etapa 1
                stmt.setString(6, novoEnderecoId); // ID da Etapa 2

                stmt.executeUpdate();
                System.out.println("\n==============================================");
                System.out.println("CLIENTE CADASTRADO COM SUCESSO!");
                System.out.println("==============================================");
            }

        } catch (SQLException ex) {
            System.out.println("ERRO CRÍTICO ao inserir cliente: " + ex.getMessage());
            // (Opcional: deletar usuário e endereço)
        }
    }


    public static void atualizarCliente(Scanner sc, int usuarioId) {

        try (Connection con = Conexao.getConnection()) {

            int idClienteParaAtualizar; // Este é o ID da tabela 'clientes'

            // --- Determina o cliente a ser atualizado ---
            if (usuarioId == 0) { // Se for 0, é o ADM, então pergunta
                System.out.print("Digite o ID do CLIENTE (da tabela clientes) que deseja atualizar: ");
                try {
                    idClienteParaAtualizar = Integer.parseInt(sc.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("ID inválido.");
                    return;
                }
            } else { // Se for > 0, é o próprio cliente logado
                // Precisamos achar o ID do cliente baseado no usuario_id
                String sqlFind = "SELECT id FROM clientes WHERE usuario_id = ?";
                try(PreparedStatement stmtFind = con.prepareStatement(sqlFind)) {
                    stmtFind.setInt(1, usuarioId);
                    ResultSet rsFind = stmtFind.executeQuery();
                    if(rsFind.next()) {
                        idClienteParaAtualizar = rsFind.getInt("id");
                    } else {
                        System.out.println("Erro: Nenhum perfil de cliente encontrado para seu usuário.");
                        return;
                    }
                }
            }

            // --- Busca os dados atuais ---
            String sqlSelect = "SELECT * FROM clientes WHERE id = ?";
            try (PreparedStatement stmtSelect = con.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, idClienteParaAtualizar);
                ResultSet rs = stmtSelect.executeQuery();

                if (!rs.next()) {
                    System.out.println("Cliente com ID " + idClienteParaAtualizar + " não encontrado.");
                    return;
                }

                // Dados atuais
                String nomeAtual = rs.getString("nome");
                String cpfAtual = rs.getString("cpf");
                String telefoneAtual = rs.getString("telefone");
                String dataNascimentoAtual = rs.getString("data_nascimento");

                // --- Entradas atualizadas ---
                System.out.println("Deixe em branco (aperte Enter) para manter o valor atual.");

                String nome;
                do {
                    System.out.print("Digite o novo nome (Atual: " + nomeAtual + "): ");
                    nome = sc.nextLine();
                    if (nome.isEmpty()) break;
                    nome = validarApenasLetras(nome);
                    if (nome == null) System.out.println("ERRO: Nome inválido. Apenas letras e espaços.");
                } while (nome == null);

                String cpf;
                do {
                    System.out.print("Digite o novo CPF (Atual: " + cpfAtual + "): ");
                    String cpfInput = sc.nextLine();
                    if (cpfInput.isEmpty()) { cpf = ""; break; }
                    cpf = validarNumero(cpfInput, 11, 11);
                    if (cpf == null) {
                        System.out.println("ERRO: CPF inválido (11 números).");
                    } else {
                        // Só checa duplicidade se o CPF for NOVO
                        if (!cpf.equals(cpfAtual) && !validarUnico(cpf, "cpf")) {
                            cpf = null; // Falhou
                        }
                    }
                } while (cpf == null);

                String telefone;
                do {
                    System.out.print("Digite o novo telefone (Atual: " + telefoneAtual + "): ");
                    String telInput = sc.nextLine();
                    if (telInput.isEmpty()) { telefone = ""; break; }
                    telefone = validarNumero(telInput, 10, 11);
                    if (telefone == null) {
                        System.out.println("ERRO: Telefone inválido (10 ou 11 números).");
                    } else {
                        // Só checa duplicidade se o Telefone for NOVO
                        if (!telefone.equals(telefoneAtual) && !validarUnico(telefone, "telefone")) {
                            telefone = null; // Falhou
                        }
                    }
                } while (telefone == null);

                String dataNascimento;
                do {
                    System.out.print("Digite a nova Data de nascimento (AAAA-MM-DD) (Atual: " + dataNascimentoAtual + "): ");
                    dataNascimento = sc.nextLine();
                    if (dataNascimento.isEmpty()) break;
                    if (validarDataNascimento(dataNascimento) == null) {
                        dataNascimento = null; // Força repetição
                    }
                } while (dataNascimento == null);

                // --- Monta o SQL dinamicamente ---
                StringBuilder sqlUpdate = new StringBuilder("UPDATE clientes SET ");
                List<Object> params = new ArrayList<>();
                boolean first = true;

                if (!nome.isEmpty()) { sqlUpdate.append("nome = ?"); params.add(nome); first = false; }
                if (!cpf.isEmpty()) { if (!first) sqlUpdate.append(", "); sqlUpdate.append("cpf = ?"); params.add(cpf); first = false; }
                if (!telefone.isEmpty()) { if (!first) sqlUpdate.append(", "); sqlUpdate.append("telefone = ?"); params.add(telefone); first = false; }
                if (dataNascimento != null && !dataNascimento.isEmpty()) { if (!first) sqlUpdate.append(", "); sqlUpdate.append("data_nascimento = ?"); params.add(dataNascimento); first = false; }

                if (params.isEmpty()) {
                    System.out.println("Nenhum campo foi alterado.");
                    return;
                }

                sqlUpdate.append(" WHERE id = ?");
                params.add(idClienteParaAtualizar);

                // Evita Injeção de SQL usando PreparedStatement
                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate.toString())) {
                    for(int i = 0; i < params.size(); i++) {
                        stmtUpdate.setObject(i + 1, params.get(i));
                    }
                    stmtUpdate.executeUpdate();
                    System.out.println("Cliente atualizado com sucesso!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar cliente: " + e.getMessage());
        }
    }

    private static void desativarCliente(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {

            int idCliente = 0;
            boolean idValido = false;
            do {
                System.out.println("Digite o ID do CLIENTE (da tabela clientes) a DESATIVAR:");
                String idInput = sc.nextLine();
                try {
                    idCliente = Integer.parseInt(idInput);
                    if (idCliente > 0) idValido = true;
                    else System.out.println("ERRO: O ID deve ser um número positivo.");
                } catch (NumberFormatException e) {
                    System.out.println("ERRO: Entrada inválida. Digite apenas números.");
                }
            } while (!idValido);


            System.out.println("Tem certeza que deseja desativar o cliente ID " + idCliente + "? (S/N)");
            System.out.println("Isso irá desativar o usuário associado (login).");
            String confirmacao = sc.nextLine();

            if (confirmacao.equalsIgnoreCase("S")) {

                // 1. Achar o usuario_id do cliente
                int usuarioId = 0;
                String sqlFind = "SELECT usuario_id FROM clientes WHERE id = ?";
                try (PreparedStatement stmtFind = con.prepareStatement(sqlFind)) {
                    stmtFind.setInt(1, idCliente);
                    ResultSet rs = stmtFind.executeQuery();
                    if (rs.next()) {
                        usuarioId = rs.getInt("usuario_id");
                    } else {
                        System.out.println("Erro: Cliente com ID " + idCliente + " não encontrado.");
                        return;
                    }
                }

                // 2. Desativar o usuário (Soft Delete)
                if (usuarioId > 0) {
                    String sqlUpdate = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
                    try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate)) {
                        stmtUpdate.setInt(1, usuarioId);
                        stmtUpdate.executeUpdate();
                        System.out.println("Cliente (e seu usuário associado) desativado com sucesso!");
                    }
                }

            } else {
                System.out.println("Operação cancelada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao desativar cliente: " + e.getMessage());
        }
    }



}
