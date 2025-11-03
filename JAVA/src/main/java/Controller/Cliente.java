package Controller;

import Database.Conexao;
import Controller.Usuario;
import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException; 

public class Cliente {
    private static String validarNumerosETamanho(String valor, int tamanhoMinimo, int tamanhoMaximo) {
        if (valor == null) {
            return null;
        }
        String valorLimpo = valor.replaceAll("[^0-9]", "");
        if (valorLimpo.length() >= tamanhoMinimo && valorLimpo.length() <= tamanhoMaximo) {
            return valorLimpo;
        } else {
            return null;
        }
    }
    
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
            return dataNascimento;
        } catch (DateTimeParseException e) {
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
                System.out.println("ID: " + rs.getString("id")); 
                System.out.println("Nome: " + rs.getString("nome"));
                System.out.println("CPF: " + rs.getString("cpf"));
                System.out.println("Telefone: " + rs.getString("telefone"));
                System.out.println("============================================");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao exibir clientes: " + e.getMessage());
        }

        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir novo cliente");
        System.out.println("2. Atualizar cliente");
        System.out.println("3. Deletar cliente");
        System.out.println("4. Sair da aba clientes");

        // Captura a entrada do usuário
        String opcaoStr = sc.nextLine();
        int opcao = 0;
        try {
            opcao = Integer.parseInt(opcaoStr);
        } catch (NumberFormatException e) {
        }

        switch (opcao) {
            case 1 -> inserirCliente(sc);
            case 2 -> atualizarCliente(sc);
            case 3 -> deletarCliente(sc);
            case 4 -> System.out.println("Saindo...");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void inserirCliente(Scanner sc) {
        Usuario.exibirUsuarios("cliente");
        
        try (Connection con = Conexao.getConnection()) {
            String nome, cpfValidado, telefoneValidado, idUsuario;
            LocalDate dataNascimentoValida = null;
            
            System.out.println("Digite o ID do usuário (UUID):");
            idUsuario = sc.nextLine();
            
            // --- Validação do ID do Usuário ---
            do {
                String sqlVerificaId = "SELECT COUNT(*) AS total FROM usuarios u " +
                                       "JOIN usuarioGrupo ug ON u.id = ug.usuario_id " +
                                       "JOIN gruposUsuarios gu ON ug.grupo_id = gu.id " +
                                       "WHERE u.id = ? AND gu.nome = 'cliente'";
                
                try (PreparedStatement stmtVerifica = con.prepareStatement(sqlVerificaId)) {
                    stmtVerifica.setString(1, idUsuario); // setString para UUID
                    try (ResultSet rsVerifica = stmtVerifica.executeQuery()) {
                        if (rsVerifica.next() && rsVerifica.getInt("total") > 0) {
                            // Verifica se o cliente já está cadastrado
                            if (clienteJaCadastrado(idUsuario)) {
                                System.out.println("ERRO: Este usuário (ID: " + idUsuario + ") já possui cadastro como cliente.");
                            } else {
                                break; // ID válido 
                            }
                        } else {
                            System.out.println("ERRO: ID de usuário inválido. Tente novamente.");
                        }
                    }
                }
                System.out.println("Digite o ID do usuário (UUID):");
                idUsuario = sc.nextLine();
            } while (true);
            
            System.out.println("Digite o nome do cliente:");
            nome = sc.nextLine();
            
            // ... (Validação de CPF)
            do {
                System.out.println("Digite o CPF (11 dígitos, apenas números):");
                String cpfDigitado = sc.nextLine();
                cpfValidado = validarNumerosETamanho(cpfDigitado, 11, 11); 
                if (cpfValidado == null) {
                    System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos.");
                }
            } while (cpfValidado == null);
            
            // ... (Validação de Telefone)
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos, apenas números. Ex: DD + Número):");
                String telDigitado = sc.nextLine();
                telefoneValidado = validarNumerosETamanho(telDigitado, 10, 11);
                if (telefoneValidado == null) {
                    System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos (incluindo o DDD).");
                }
            } while (telefoneValidado == null);
            
            // ... (Validação de Data de Nascimento)
            do {
                System.out.println("Digite a Data de nascimento (AAAA-MM-DD):");
                String dataNascimentoDigitada = sc.nextLine();
                dataNascimentoValida = validarDataNascimento(dataNascimentoDigitada);
            } while (dataNascimentoValida == null);

            String sql = "INSERT INTO clientes (nome, cpf, telefone, data_nascimento, usuario_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, cpfValidado);
                stmt.setString(3, telefoneValidado);
                stmt.setDate(4, Date.valueOf(dataNascimentoValida)); 
                stmt.setString(5, idUsuario); 
                
                stmt.executeUpdate();
                System.out.println("Cliente inserido com sucesso!");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir cliente: " + e.getMessage());
        }
    }
    
    // Método auxiliar para verificar se o cliente já existe
    private static boolean clienteJaCadastrado(String idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE usuario_id = ?";
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private static void atualizarCliente(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID do cliente (UUID) que deseja atualizar:");
            String id = sc.nextLine(); 

            String sqlSelect = "SELECT * FROM clientes WHERE id = ?";
            try (PreparedStatement stmtSelect = con.prepareStatement(sqlSelect)) {
                stmtSelect.setString(1, id); 
                ResultSet rs = stmtSelect.executeQuery();

                if (rs.next()) {
                    String nomeAtual = rs.getString("nome");
                    String cpfAtual = rs.getString("cpf");
                    String telefoneAtual = rs.getString("telefone");
                    String dataNascimentoAtual = rs.getString("data_nascimento"); 
                    
                    String usuarioIdAtual = rs.getString("usuario_id"); 
                    System.out.println("ID de Usuário Associado: " + usuarioIdAtual);
                    System.out.println("=========================================");

                    // ... (Captura e validação das novas informações)
                    System.out.println("Digite o novo nome (ou pressione Enter para manter: " + nomeAtual + "):");
                    String nome = sc.nextLine();
                    
                    String cpf = "";
                    boolean cpfValido = false;
                    while (!cpfValido) {
                        System.out.println("Digite o novo CPF (11 dígitos numéricos, ou pressione Enter para manter: " + cpfAtual + "):");
                        String cpfDigitado = sc.nextLine();
                        if (cpfDigitado.isEmpty()) { cpf = ""; cpfValido = true; } 
                        else {
                            cpf = validarNumerosETamanho(cpfDigitado, 11, 11);
                            if (cpf == null) { System.out.println("ERRO: CPF inválido. Digite 11 dígitos numéricos."); } 
                            else { cpfValido = true; }
                        }
                    }

                    String telefone = "";
                    boolean telValido = false;
                    while (!telValido) {
                        System.out.println("Digite o novo telefone (10 ou 11 dígitos, ou pressione Enter para manter: " + telefoneAtual + "):");
                        String telDigitado = sc.nextLine();
                        if (telDigitado.isEmpty()) { telefone = ""; telValido = true; } 
                        else {
                            telefone = validarNumerosETamanho(telDigitado, 10, 11);
                            if (telefone == null) { System.out.println("ERRO: Telefone inválido. Digite 10 ou 11 dígitos numéricos."); } 
                            else { telValido = true; }
                        }
                    }
                    
                    String dataNascimento = "";
                    boolean dataValida = false;
                    while (!dataValida) {
                        System.out.println("Digite a nova Data de nascimento (AAAA-MM-DD, ou pressione Enter para manter: " + dataNascimentoAtual + "):");
                        String dataDigitada = sc.nextLine();
                        if (dataDigitada.isEmpty()) { dataNascimento = ""; dataValida = true; } 
                        else {
                            LocalDate dataNascimentoValida = validarDataNascimento(dataDigitada);
                            if (dataNascimentoValida != null) {
                                dataNascimento = dataNascimentoValida.toString(); 
                                dataValida = true;
                            }
                        }
                    }

                    StringBuilder sqlUpdate = new StringBuilder("UPDATE clientes SET ");
                    boolean primeiro = true;
                    
                    int paramIndex = 1;
                    
                    if (!nome.isEmpty()) {
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("nome = ?");
                        primeiro = false;
                    }
                    if (!cpf.isEmpty()) {
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("cpf = ?"); 
                        primeiro = false;
                    }
                    if (!telefone.isEmpty()) {
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("telefone = ?");
                        primeiro = false;
                    }
                    if (!dataNascimento.isEmpty()) { 
                        if (!primeiro) sqlUpdate.append(", ");
                        sqlUpdate.append("data_nascimento = ?");
                        primeiro = false;
                    }

                    if (primeiro) {
                        System.out.println("Nenhum campo foi alterado. Operação cancelada.");
                        return;
                    }
                    
                    sqlUpdate.append(" WHERE id = ?");

                    try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate.toString())) {
                        
                        if (!nome.isEmpty()) stmtUpdate.setString(paramIndex++, nome);
                        if (!cpf.isEmpty()) stmtUpdate.setString(paramIndex++, cpf);
                        if (!telefone.isEmpty()) stmtUpdate.setString(paramIndex++, telefone);
                        if (!dataNascimento.isEmpty()) stmtUpdate.setDate(paramIndex++, Date.valueOf(dataNascimento));
                        
                        stmtUpdate.setString(paramIndex, id); 
                        
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
            System.out.println("Digite o ID do cliente (UUID) que deseja deletar:");
            String id = sc.nextLine(); 

            System.out.println("Tem certeza que deseja deletar o cliente ID " + id + "? (S/N)");
            String confirmacao = sc.nextLine();

            if (confirmacao.equalsIgnoreCase("S")) {
                String sql = "DELETE FROM clientes WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    
                    stmt.setString(1, id); 
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Cliente deletado com sucesso!");
                    } else {
                        System.out.println("Nenhum cliente encontrado com o ID " + id + ".");
                    }
                }
            } else {
                System.out.println("Operação cancelada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cliente: " + e.getMessage());
        }
    }
}