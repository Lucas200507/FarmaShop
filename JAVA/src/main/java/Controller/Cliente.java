package Controller;

import Database.Conexao;
import Controller.Usuario;
import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Cliente {

    private static String validarNumerosETamanho(String valor, int tamanhoMinimo, int tamanhoMaximo, String tipo) {
        // Fazer a verificação de telefone e cpf unicos
        String sql = "SELECT ? FROM clientes WHERE ? = ?";
        try {
            Connection con = Conexao.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, tipo);
            stmt.setString(2, tipo);
            stmt.setString(3, valor);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if(tipo.equals("cpf")){
                    System.out.println("CPF já cadastrado no sistema.\n CPF: "+valor);
                } else if (tipo.equals("telefone")){
                    System.out.println("Telefone já cadastrado no sistema.\n Telefone: "+valor);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
        int usuario_id  = 0, idCliente = 0;
        String endereco_id = "0";
        if (idUsuario == 0) {
            boolean valido = false;
            Usuario.exibirUsuarios("cliente");
            do{
                System.out.println("Escolha um id válido:");
                if (sc.hasNextInt()) {
                    usuario_id = sc.nextInt();
                    if (usuario_id > 0){
                        String sql = "SELECT * FROM usuarios WHERE id = ?";
                        try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                            stmt.setInt(1, usuario_id);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                valido = true;
                            } else {
                                usuario_id = 0;
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    System.out.println("Valor inválido! Não é um número inteiro.");
                    sc.nextLine(); // limpa o buffer
                }
            } while(!valido);
        }

        if (idEndereco.equals("0")) {
            boolean valido = false;
            Endereco.exibirEnderecos("cliente");
            do{
                System.out.println("Escolha um id válido:");
                endereco_id = sc.nextLine();
                String sql = "SELECT * FROM enderecos WHERE id = ?";
                try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, endereco_id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()){
                        valido = true;
                    } else {
                        endereco_id = "0";
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } while(!valido);
        }


        try (Connection con = Conexao.getConnection()) {
            String nome, cpfValidado, telefoneValidado;
            LocalDate dataNascimentoValida = null;

            System.out.println("Digite o nome do cliente:");
            nome = sc.nextLine();

            // --- Validação do CPF ---
            do {
                System.out.println("Digite o CPF (11 dígitos, apenas números):");
                String cpfDigitado = sc.nextLine();
                cpfValidado = validarNumerosETamanho(cpfDigitado, 11, 11, "cpf");
                if (cpfValidado == null) {
                    System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos.");
                }
            } while (cpfValidado == null);

            // --- Validação do Telefone ---
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos, apenas números. Ex: DD + Número):");
                String telDigitado = sc.nextLine();
                telefoneValidado = validarNumerosETamanho(telDigitado, 10, 11, "telefone");
                if (telefoneValidado == null) {
                    System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos (incluindo o DDD).");
                }
            } while (telefoneValidado == null);

            // --- Validação de Data de Nascimento ---
            do {
                System.out.println("Digite a Data de nascimento (AAAA-MM-DD):");
                String dataNascimentoDigitada = sc.nextLine();

                dataNascimentoValida = validarDataNascimento(dataNascimentoDigitada);

            } while (dataNascimentoValida == null);

            // --- Inserção no Banco de Dados (usando os valores validados) ---

            String sql = "INSERT INTO clientes (nome, cpf, telefone, data_nascimento, usuario_id, endereco_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpfValidado);
                stmt.setString(3, telefoneValidado);
                stmt.setDate(4, Date.valueOf(dataNascimentoValida));
                if(idUsuario == 0) {stmt.setInt(5, usuario_id);}else{stmt.setInt(5, idUsuario);}
                if(idEndereco.equals("0")) {stmt.setString(6, endereco_id);}else{stmt.setString(6, idEndereco);}

                stmt.executeUpdate();
                System.out.println("Cliente inserido com sucesso!");
                String sql2 = "SELECT id FROM clientes ORDER BY id DESC LIMIT 1";
                try(PreparedStatement stmt2 = con.prepareStatement(sql2)){
                    ResultSet rs = stmt2.executeQuery();
                    if (rs.next()){
                        idCliente = rs.getInt("id");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir cliente: " + e.getMessage());
        }
        return  idCliente;
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

            if (entrada.matches("\\d{" + min + "," + max + "}")) {
                return entrada;
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
                LocalDate.parse(entrada);
                return entrada;
            } catch (Exception e) {
                System.out.println("ERRO: data inválida. Formato correto: AAAA-MM-DD.");
            }
        }
    }

    public static void atualizarCliente(Integer idUsuario) {
        Scanner sc = new Scanner(System.in);

        try (Connection con = Conexao.getConnection()) {

            // --- Determina o cliente a ser atualizado ---
            int idCliente;
            if (idUsuario == null || idUsuario == 0) {
                System.out.print("Digite o ID do cliente que deseja atualizar: ");
                idCliente = sc.nextInt();
                sc.nextLine(); // limpa o buffer
            } else {
                idCliente = idUsuario;
            }

            // --- Busca os dados atuais ---
            String sqlSelect = (idUsuario == null || idUsuario == 0)
                    ? "SELECT * FROM clientes WHERE id = ?"
                    : "SELECT * FROM clientes WHERE usuario_id = ?";

            try (PreparedStatement stmtSelect = con.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, idCliente);
                ResultSet rs = stmtSelect.executeQuery();

                if (!rs.next()) {
                    System.out.println("Cliente não encontrado.");
                    return;
                }

                // Dados atuais
                String nomeAtual = rs.getString("nome");
                String cpfAtual = rs.getString("cpf");
                String telefoneAtual = rs.getString("telefone");
                String dataNascimentoAtual = rs.getString("data_nascimento");

                // --- Entradas atualizadas ---
                String nome = lerCampoOpcional(sc, "novo nome", nomeAtual);
                String cpf = lerCampoNumerico(sc, "novo CPF", cpfAtual, 11, 11);
                String telefone = lerCampoNumerico(sc, "novo telefone", telefoneAtual, 10, 11);
                String dataNascimento = lerCampoData(sc, "nova Data de nascimento", dataNascimentoAtual);

                // --- Monta o SQL dinamicamente ---
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
                    primeiro = false;
                }
                if (!dataNascimento.isEmpty()) {
                    if (!primeiro) sqlUpdate.append(", ");
                    sqlUpdate.append("data_nascimento = '").append(dataNascimento).append("'");
                }

                if (primeiro) {
                    System.out.println("Nenhum campo foi alterado. Operação cancelada.");
                    return;
                }

                sqlUpdate.append((idUsuario == null || idUsuario == 0)
                        ? " WHERE id = " + idCliente
                        : " WHERE usuario_id = " + idCliente);

                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate.toString())) {
                    stmtUpdate.executeUpdate();
                    System.out.println("Cliente atualizado com sucesso!");
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