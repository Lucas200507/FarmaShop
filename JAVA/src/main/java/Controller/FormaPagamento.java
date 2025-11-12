package Controller;

import Database.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class FormaPagamento {

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
     * Menu principal para o cliente gerenciar suas formas de pagamento.
     * @param sc O Scanner global
     * @param clienteId O ID do cliente que está logado
     */
    public static void gerenciarFormasPagamento(Scanner sc, int clienteId) {
        int opcao;
        do {
            System.out.println("\n=== MINHAS FORMAS DE PAGAMENTO ===");

            // Exibe as formas de pagamento atuais
            exibirMinhasFormas(clienteId);

            System.out.println("\nEscolha uma opção:");
            System.out.println("1. Adicionar nova forma de pagamento");
            System.out.println("2. Remover forma de pagamento");
            System.out.println("3. Voltar");

            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                opcao = 0;
            }

            switch (opcao) {
                case 1:
                    adicionarNovaForma(sc, clienteId);
                    break;
                case 2:
                    removerForma(sc, clienteId);
                    break;
                case 3:
                    System.out.println("Voltando...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 3);
    }

    /**
     * (PRIVADO) Apenas exibe a lista de formas de pagamento salvas do cliente.
     */
    private static void exibirMinhasFormas(int clienteId) {
        String sql = "SELECT id, tipo, apelido, chave_pix, ultimos_digitos, bandeira FROM formas_pagamento_cliente WHERE cliente_id = ?";
        boolean existe = false;

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                existe = true;
                String tipo = rs.getString("tipo");
                System.out.println("---------------------------------");
                System.out.println("ID: " + rs.getString("id"));
                System.out.println("Apelido: " + rs.getString("apelido"));

                if (tipo.equals("PIX")) {
                    System.out.println("Tipo: PIX");
                    System.out.println("Chave: " + rs.getString("chave_pix"));
                } else if (tipo.startsWith("CARTAO")) {
                    System.out.println("Tipo: Cartão");
                    System.out.println("Final: " + rs.getString("ultimos_digitos"));
                    System.out.println("Bandeira: " + rs.getString("bandeira"));
                }
            }

            if (!existe) {
                System.out.println("Nenhuma forma de pagamento cadastrada.");
            }
            System.out.println("---------------------------------");

        } catch (SQLException e) {
            System.out.println("Erro ao listar formas de pagamento: " + e.getMessage());
        }
    }

    /**
     * (PRIVADO) Pergunta o tipo (PIX ou Cartão) e chama o método correspondente.
     */
    private static void adicionarNovaForma(Scanner sc, int clienteId) {
        System.out.println("Qual tipo de forma de pagamento deseja adicionar?");
        System.out.println("1. Chave PIX");
        System.out.println("2. Cartão de Crédito/Débito");
        System.out.println("3. Cancelar");

        int tipo;
        try {
            tipo = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            tipo = 0;
        }

        switch(tipo) {
            case 1:
                adicionarPIX(sc, clienteId);
                break;
            case 2:
                adicionarCartao(sc, clienteId);
                break;
            case 3:
                System.out.println("Cancelado.");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    /**
     * (PRIVADO) Adiciona uma nova Chave PIX.
     */
    private static void adicionarPIX(Scanner sc, int clienteId) {
        System.out.println("--- Adicionar PIX ---");
        System.out.print("Digite um apelido (ex: Meu PIX Celular): ");
        String apelido = sc.nextLine();
        System.out.print("Digite a chave PIX (CPF, e-mail, celular, etc.): ");
        String chave = sc.nextLine();

        String sql = "INSERT INTO formas_pagamento_cliente (cliente_id, tipo, apelido, chave_pix) VALUES (?, 'PIX', ?, ?)";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            stmt.setString(2, apelido);
            stmt.setString(3, chave);
            stmt.executeUpdate();

            System.out.println("Chave PIX adicionada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao salvar PIX: " + e.getMessage());
        }
    }

    /**
     * (PRIVADO) Adiciona um novo Cartão.
     */
    private static void adicionarCartao(Scanner sc, int clienteId) {
        System.out.println("--- Adicionar Cartão ---");
        System.out.print("Digite um apelido (ex: Cartão Nubank): ");
        String apelido = sc.nextLine();

        String titular;
        do {
            System.out.print("Nome do titular (como está no cartão): ");
            titular = validarApenasLetras(sc.nextLine());
            if (titular == null) {
                System.out.println("ERRO: Nome inválido. Digite apenas letras, acentos e espaços.");
            }
        } while (titular == null);

        System.out.print("Bandeira (Visa, MasterCard, Elo, etc.): ");
        String bandeira = sc.nextLine();

        System.out.print("Últimos 4 dígitos: ");
        String digitos = sc.nextLine(); // Idealmente, validar (mas mantendo simples)

        // O SQL define 'token_pagamento' como NULL, então podemos deixar vazio
        // String tokenSimulado = "token_" + digitos + "_" + (titular.hashCode());

        String sql = "INSERT INTO formas_pagamento_cliente (cliente_id, tipo, apelido, nome_titular, bandeira, ultimos_digitos, token_pagamento) " +
                "VALUES (?, 'CARTAO_CREDITO', ?, ?, ?, ?, NULL)"; // Usando NULL para token

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            stmt.setString(2, apelido);
            stmt.setString(3, titular);
            stmt.setString(4, bandeira);
            stmt.setString(5, digitos);
            stmt.executeUpdate();

            System.out.println("Cartão adicionado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao salvar Cartão: " + e.getMessage());
        }
    }

    /**
     * (PRIVADO) Remove uma forma de pagamento pelo ID.
     */
    private static void removerForma(Scanner sc, int clienteId) {
        System.out.println("--- Remover Forma de Pagamento ---");
        System.out.print("Digite o ID (ex: 1a2b3c4) da forma de pagamento que deseja remover: ");
        String idParaRemover = sc.nextLine();

        if (idParaRemover == null || idParaRemover.trim().isEmpty()) {
            System.out.println("ID inválido. Operação cancelada.");
            return;
        }

        // O 'ON DELETE CASCADE' no SQL é bom, mas é sempre seguro
        // ter a validação dupla (cliente_id) no app.
        String sql = "DELETE FROM formas_pagamento_cliente WHERE id = ? AND cliente_id = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, idParaRemover);
            stmt.setInt(2, clienteId); // Garante que o cliente só apague o que é dele

            int rowsAfetadas = stmt.executeUpdate();

            if (rowsAfetadas > 0) {
                System.out.println("Forma de pagamento removida com sucesso!");
            } else {
                System.out.println("ID não encontrado ou não pertence a você.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao remover forma de pagamento: " + e.getMessage());
        }
    }
}