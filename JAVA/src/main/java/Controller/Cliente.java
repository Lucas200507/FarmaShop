package Controller;

import Database.Conexao;
import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; 

public class Clientes {
    
    /**
     Remove caracteres não numéricos e verifica o tamanho.
     * @param valor String a ser validada (CPF ou Telefone)
     * @param tamanhoMinimo Tamanho mínimo esperado
     * @param tamanhoMaximo Tamanho máximo esperado
     * @return O valor limpo (apenas números) se for válido, ou null se inválido.
     */

     /**
     * Valida se a string está no formato AAAA-MM-DD e se não é uma data futura.
     * @param dataStr String da data de nascimento.
     * @return Um objeto LocalDate se a data for válida, ou null se for inválida.
     */

    private static String validarNumerosETamanho(String valor, int tamanhoMinimo, int tamanhoMaximo) {
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
            String nome, cpfValidado, telefoneValidado;
            LocalDate dataNascimentoValida = null;

            System.out.println("Digite o nome do cliente:");
            nome = sc.nextLine();

            // --- Validação do CPF ---
            do {
                System.out.println("Digite o CPF (11 dígitos, apenas números):");
                String cpfDigitado = sc.nextLine();
                cpfValidado = validarNumerosETamanho(cpfDigitado, 11, 11); 
                if (cpfValidado == null) {
                    System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos.");
                }
            } while (cpfValidado == null);
            
            // --- Validação do Telefone ---
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos, apenas números. Ex: DD + Número):");
                String telDigitado = sc.nextLine();
                telefoneValidado = validarNumerosETamanho(telDigitado, 10, 11);
                if (telefoneValidado == null) {
                    System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos (incluindo o DDD).");
                }
            } while (telefoneValidado == null);
            
            // --- Validação de Data de Nascimento ---
            do {
                System.out.println("Digite a Data de nascimento (AAAA-MM-DD):");
                String dataNascimentoDigitada = sc.nextLine();
                
                dataNascimentoValida = validarDataNascimento(dataNascimentoDigitada);
                // O método já imprime o erro se for nulo
            } while (dataNascimentoValida == null);

            // --- Inserção no Banco de Dados (usando os valores validados) ---
            String sql = "INSERT INTO clientes (nome, cpf, telefone, data_nascimento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpfValidado);
                stmt.setString(3, telefoneValidado);
                stmt.setDate(4, Date.valueOf(dataNascimentoValida)); 
                
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

            String sqlSelect = "SELECT * FROM clientes WHERE id = ?";
            try (PreparedStatement stmtSelect = con.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, id);
                ResultSet rs = stmtSelect.executeQuery();

                if (rs.next()) {
                    String nomeAtual = rs.getString("nome");
                    String cpfAtual = rs.getString("cpf");
                    String telefoneAtual = rs.getString("telefone");
                    String dataNascimentoAtual = rs.getString("data_nascimento"); // Data atual

                    System.out.println("Digite o novo nome (ou pressione Enter para manter: " + nomeAtual + "):");
                    String nome = sc.nextLine();

                    // --- Validação/Captura do novo CPF ---
                    String cpf = "";
                    boolean cpfValido = false;
                    while (!cpfValido) {
                        System.out.println("Digite o novo CPF (11 dígitos numéricos, ou pressione Enter para manter: " + cpfAtual + "):");
                        String cpfDigitado = sc.nextLine();

                        if (cpfDigitado.isEmpty()) {
                            cpf = ""; 
                            cpfValido = true;
                        } else {
                            cpf = validarNumerosETamanho(cpfDigitado, 11, 11);
                            if (cpf == null) {
                                System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos.");
                            } else {
                                cpfValido = true;
                            }
                        }
                    }

                    // --- Validação/Captura do novo Telefone ---
                    String telefone = "";
                    boolean telValido = false;
                    while (!telValido) {
                        System.out.println("Digite o novo telefone (10 ou 11 dígitos, ou pressione Enter para manter: " + telefoneAtual + "):");
                        String telDigitado = sc.nextLine();
                        
                        if (telDigitado.isEmpty()) {
                            telefone = ""; 
                            telValido = true;
                        } else {
                            telefone = validarNumerosETamanho(telDigitado, 10, 11);
                            if (telefone == null) {
                                System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos.");
                            } else {
                                telValido = true;
                            }
                        }
                    }
                    
                    // --- Validação/Captura da nova Data de Nascimento ---
                    String dataNascimento = "";
                    boolean dataValida = false;
                    while (!dataValida) {
                        System.out.println("Digite a nova Data de nascimento (AAAA-MM-DD, ou pressione Enter para manter: " + dataNascimentoAtual + "):");
                        String dataDigitada = sc.nextLine();

                        if (dataDigitada.isEmpty()) {
                            dataNascimento = ""; 
                            dataValida = true;
                        } else {
                            LocalDate dataNascimentoValida = validarDataNascimento(dataDigitada);
                            if (dataNascimentoValida != null) {
                                // Converte para String no formato correto para o SQL
                                dataNascimento = dataNascimentoValida.toString(); 
                                dataValida = true;
                            }
                            // Se for nulo, o método validarDataNascimento já imprime o erro
                        }
                    }
                    
                    // --- Construção do SQL UPDATE ---
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

                    sqlUpdate.append(" WHERE id = ").append(id);

                    if (primeiro) {
                         System.out.println("Nenhum campo foi alterado. Operação cancelada.");
                         return;
                    }
                    
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
