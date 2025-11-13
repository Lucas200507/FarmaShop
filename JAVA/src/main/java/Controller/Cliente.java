package Controller;

import Database.Conexao;
import Controller.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class Cliente {



    private static String validarNumerosETamanho(String valor, int tamanhoMinimo, int tamanhoMaximo, String tipo) {
        // Fazer a verificação de telefone e cpf unicos
        String sql = "SELECT * FROM clientes WHERE " + tipo + " = ?";
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, valor);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (tipo.equals("cpf")) {
                        System.out.println("CPF já cadastrado no sistema.\n CPF: " + valor);
                    } else if (tipo.equals("telefone")) {
                        System.out.println("Telefone já cadastrado no sistema.\n Telefone: " + valor);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar duplicidade de " + tipo + ": " + e.getMessage(), e);
        }

        if (valor == null) {
            return null;
        }

        // Remove todos os caracteres que não são dígitos (0-9)
        String valorLimpo = valor.replaceAll("[^0-9]", "");

        if (valorLimpo.length() >= tamanhoMinimo && valorLimpo.length() <= tamanhoMaximo) {
            return valorLimpo; // Válido
        } else {
            return null; // Inválido
        }
    }

    private static LocalDate validarDataNascimento(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }

        // Define o formato esperado (ISO)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            LocalDate dataNascimento = LocalDate.parse(dataStr, formatter);

            // Validação 2: Não pode ser uma data futura
            if (dataNascimento.isAfter(LocalDate.now())) {
                System.out.println("ERRO: A data de nascimento não pode ser uma data futura.");
                return null;
            }

            return dataNascimento; // Data válida

        } catch (DateTimeParseException e) {
            // Validação 1: Formato inválido ou data inexistente (ex: 2000-02-30)
            System.out.println("ERRO: Formato de data inválido. Use o padrão AAAA-MM-DD (ex: 1990-05-15).");
            return null;
        }
    }


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
        Cliente c = new Cliente();
        switch (opcao) {
            case 1:
                int idCliente = c.inserirCliente(sc ,0, "0");
                break;
            case 2:
                atualizarCliente(0);
                break;
            case 3:
                deletarCliente(sc);
                break;
            case 4:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida.");
                break;
        }
    }

    public int inserirCliente(Scanner sc, int idUsuario, String idEndereco) {
        int usuario_id = 0;
        String endereco_id = "0";

        if (idUsuario == 0) {
            System.out.println("--- Cadastro de Usuário (Conta) ---");
            usuario_id = new Usuario().inserirUsuario(sc, 2);

            if (usuario_id == 0) {
                System.out.println("Falha ao criar o Usuário. Cadastro de Cliente cancelado.");
                return 0;
            }
        } else {
            usuario_id = idUsuario;
        }

        if (idEndereco.equals("0")) {
            boolean valido = false;
            do {
                System.out.println("Escolha o ID do Endereço (ou digite um ID válido, Endereco.exibirEnderecos deve estar funcionando):");
                endereco_id = sc.nextLine();
                String sql = "SELECT id FROM enderecos WHERE id = ?";
                try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, endereco_id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            valido = true;
                        } else {
                            System.out.println("ID de endereço inválido. Tente novamente.");
                            endereco_id = "0";
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao validar endereço: " + e.getMessage());
                    endereco_id = "0";
                }
            } while (!valido);
        } else {
            endereco_id = idEndereco;
        }


        String nome, cpfValidado, telefoneValidado;
        LocalDate dataNascimentoValida = null;

        System.out.println("--- Dados Pessoais do Cliente ---");

        System.out.println("Digite o nome completo do cliente:");
        nome = sc.nextLine();

        do {
            System.out.println("Digite o CPF (11 dígitos, apenas números):");
            String cpfDigitado = sc.nextLine();
            cpfValidado = validarNumerosETamanho(cpfDigitado, 11, 11, "cpf");
            if (cpfValidado == null) {
                System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos e verifique se já está cadastrado.");
            }
        } while (cpfValidado == null);

        do {
            System.out.println("Digite o telefone (10 ou 11 dígitos, apenas números. Ex: DD + Número):");
            String telDigitado = sc.nextLine();
            telefoneValidado = validarNumerosETamanho(telDigitado, 10, 11, "telefone");
            if (telefoneValidado == null) {
                System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos (incluindo o DDD) e verifique se já está cadastrado.");
            }
        } while (telefoneValidado == null);

        do {
            System.out.println("Digite a Data de nascimento (AAAA-MM-DD):");
            String dataNascimentoDigitada = sc.nextLine();
            dataNascimentoValida = validarDataNascimento(dataNascimentoDigitada);
        } while (dataNascimentoValida == null);

        try (Connection con = Conexao.getConnection()) {
            // Usando PreparedStatement com RETURN_GENERATED_KEYS para capturar o ID auto_increment
            String sql = "INSERT INTO clientes (nome, cpf, telefone, data_nascimento, usuario_id, endereco_id) VALUES (?, ?, ?, ?, ?, ?)";

            // Adiciona Statement.RETURN_GENERATED_KEYS para obter o ID gerado
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpfValidado);
                stmt.setString(3, telefoneValidado);
                stmt.setDate(4, Date.valueOf(dataNascimentoValida));
                stmt.setInt(5, usuario_id);

                // CORREÇÃO: Usando setString para o ID de endereço VARCHAR
                stmt.setString(6, endereco_id);

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    // Captura o ID gerado (auto_increment)
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int idCliente = generatedKeys.getInt(1);
                            System.out.println("Cliente inserido com sucesso! ID do Cliente: " + idCliente);
                            return idCliente;
                        } else {
                            System.out.println("Cliente inserido, mas falha ao obter o ID gerado.");
                            return 0;
                        }
                    }
                } else {
                    System.out.println("Falha ao inserir o cliente.");
                    return 0;
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir cliente: " + e.getMessage());
            return 0;
        } catch (NumberFormatException e) {
            // Este catch é menos provável de ser ativado agora, mas mantemos por segurança.
            System.out.println("Erro de formato ao processar ID de endereço: " + e.getMessage());
            return 0;
        }
    }


    private static String lerCampoOpcional(Scanner sc, String label, String atual) {
        System.out.print("Digite o " + label + " (ou Enter para manter: " + atual + "): ");
        String valor = sc.nextLine();
        return valor.isEmpty() ? "" : valor;
    }

    private static String lerCampoNumerico(Scanner sc, String label, String atual, int min, int max) {
        while (true) {
            System.out.print("Digite o " + label + " (" + min + " a " + max + " dígitos, ou Enter para manter: " + atual + "): ");
            String entrada = sc.nextLine();

            if (entrada.isEmpty()) return "";

            String valorLimpo = entrada.replaceAll("[^0-9]", "");

            if (valorLimpo.length() >= min && valorLimpo.length() <= max) {
                // Se a entrada não estava vazia, retornamos o valor limpo (apenas números)
                return valorLimpo;
            } else {
                System.out.println("ERRO: valor inválido. Digite apenas números com " + min + " a " + max + " dígitos.");
            }
        }
    }

    private static String lerCampoData(Scanner sc, String label, String atual) {
        while (true) {
            System.out.print("Digite a " + label + " (AAAA-MM-DD, ou Enter para manter: " + atual + "): ");
            String entrada = sc.nextLine();

            if (entrada.isEmpty()) return "";

            try {
                LocalDate dataValida = validarDataNascimento(entrada);
                if (dataValida != null) {
                    return entrada;
                }
            } catch (Exception e) {
                System.out.println("ERRO: data inválida. Formato correto: AAAA-MM-DD.");
            }
        }
    }

    public static void atualizarCliente(Integer idUsuario) {
        Scanner sc = new Scanner(System.in);

        try (Connection con = Conexao.getConnection()) {

            // --- Determina o cliente a ser atualizado ---
            int idBusca;
            String campoBusca;
            if (idUsuario == null || idUsuario == 0) {
                System.out.print("Digite o ID do cliente que deseja atualizar: ");
                idBusca = sc.nextInt();
                sc.nextLine(); // limpa o buffer
                campoBusca = "id";
            } else {
                idBusca = idUsuario;
                campoBusca = "usuario_id";
            }

            // --- Busca os dados atuais ---
            String sqlSelect = "SELECT * FROM clientes WHERE " + campoBusca + " = ?";

            try (PreparedStatement stmtSelect = con.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, idBusca);
                ResultSet rs = stmtSelect.executeQuery();

                if (!rs.next()) {
                    System.out.println("Cliente não encontrado.");
                    return;
                }

                // ID do Cliente
                int idClienteAtualizar = rs.getInt("id");

                // Dados atuais
                String nomeAtual = rs.getString("nome");
                String cpfAtual = rs.getString("cpf");
                String telefoneAtual = rs.getString("telefone");
                String dataNascimentoAtual = rs.getString("data_nascimento");

                // --- Entradas atualizadas ---
                System.out.println("--- Dados Atuais do Cliente (ID: " + idClienteAtualizar + ") ---");
                String nome = lerCampoOpcional(sc, "novo nome", nomeAtual);
                String cpf = lerCampoNumerico(sc, "novo CPF", cpfAtual, 11, 11);
                String telefone = lerCampoNumerico(sc, "novo telefone", telefoneAtual, 10, 11);
                String dataNascimento = lerCampoData(sc, "nova Data de nascimento", dataNascimentoAtual);

                // Validação de unicidade para CPF e Telefone (apenas se alterados)
                if (!cpf.isEmpty()) {
                    String cpfValidado = validarNumerosETamanho(cpf, 11, 11, "cpf");
                    if (cpfValidado == null && !cpf.equals(cpfAtual)) { // Checa se é diferente do atual
                        System.out.println("ERRO: Novo CPF inválido ou já cadastrado. Operação cancelada.");
                        return;
                    }
                    cpf = cpfValidado;
                }
                if (!telefone.isEmpty()) {
                    String telefoneValidado = validarNumerosETamanho(telefone, 10, 11, "telefone");
                    if (telefoneValidado == null && !telefone.equals(telefoneAtual)) { // Checa se é diferente do atual
                        System.out.println("ERRO: Novo Telefone inválido ou já cadastrado. Operação cancelada.");
                        return;
                    }
                    telefone = telefoneValidado;
                }


                // --- Monta o SQL dinamicamente (Usando PreparedStatement) ---
                List<String> campos = new java.util.ArrayList<>();
                List<Object> valores = new java.util.ArrayList<>();


                if (!nome.isEmpty()) {
                    campos.add("nome = ?");
                    valores.add(nome);
                }
                if (!cpf.isEmpty()) {
                    campos.add("cpf = ?");
                    valores.add(cpf);
                }
                if (!telefone.isEmpty()) {
                    campos.add("telefone = ?");
                    valores.add(telefone);
                }
                if (!dataNascimento.isEmpty()) {
                    campos.add("data_nascimento = ?");
                    valores.add(Date.valueOf(dataNascimento));
                }

                if (campos.isEmpty()) {
                    System.out.println("Nenhum campo foi alterado. Operação cancelada.");
                    return;
                }

                StringBuilder sqlUpdate = new StringBuilder("UPDATE clientes SET ");
                sqlUpdate.append(String.join(", ", campos));
                sqlUpdate.append(" WHERE id = ?");

                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate.toString())) {
                    int paramIndex = 1;
                    for (Object valor : valores) {
                        stmtUpdate.setObject(paramIndex++, valor);
                    }
                    stmtUpdate.setInt(paramIndex, idClienteAtualizar); // ID do Cliente para o WHERE

                    stmtUpdate.executeUpdate();
                    System.out.println("Cliente atualizado com sucesso!");
                }

            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar cliente: " + e.getMessage());
        } catch (java.util.InputMismatchException e) {
            System.out.println("Entrada inválida. Por favor, insira um número para o ID do cliente.");
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
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("Cliente deletado com sucesso!");
                    } else {
                        System.out.println("Cliente não encontrado (ID " + id + ").");
                    }
                }
            } else {
                System.out.println("Operação cancelada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cliente: " + e.getMessage());
        }
    }

    /**
     * Converte o ID de um usuário (vindo do login) para o ID do cliente correspondente.
     * @param usuarioId O ID da tabela 'usuarios'.
     * @return O ID da tabela 'clientes', ou 0 se não for encontrado.
     */
    public static int getClienteIdByUsuarioId(int usuarioId) {
        String sql = "SELECT id FROM clientes WHERE usuario_id = ?";
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); // Retorna o ID da tabela CLIENTES
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cliente ID por usuário ID: " + e.getMessage());
        }
        return 0; // Cliente não encontrado
    }
}