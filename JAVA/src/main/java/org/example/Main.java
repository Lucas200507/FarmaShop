package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

// Imports atualizados
import Controller.Cliente;
import Controller.Farmacia;
import Controller.Endereco;
import Controller.Usuario;
import Controller.Produtos;
import Controller.FormaPagamento; // <-- Já estava aqui, ótimo!
import Database.Conexao;
import org.example.Login;

public class Main {
    public static void main(String[] args) {
        Login l = new Login();

        while (true) {

            if (l.logar()) {
                String grupo = l.getGrupo();
                String usuario = l.getUsuario();
                int usuarioId = l.getId();
                int perfilId = getPerfilId(grupo, usuarioId);

                System.out.println("Bem vindo ao Sistema, " + usuario + "\n============================");
                mostrarMenu(grupo, usuarioId, perfilId);

            } else {
                System.out.println("Encerrando FarmaShop. Volte sempre!");
                break;
            }
        }
    }

    /**
     * Busca o ID da tabela 'clientes' ou 'farmacias' com base no usuario_id.
     * @param grupo O grupo do usuário ("cliente" ou "farmacia")
     * @param usuarioId O ID da tabela 'usuarios'
     * @return O ID do perfil (cliente_id ou farmacia_id), ou 0 se for ADM ou não encontrado.
     */
    public static int getPerfilId(String grupo, int usuarioId) {
        String sql;
        int id = 0;
        if (grupo.equals("cliente")) {
            sql = "SELECT id FROM clientes WHERE usuario_id = ?";
        } else if (grupo.equals("farmacia")) {
            sql = "SELECT id FROM farmacias WHERE usuario_id = ?";
        } else {
            return 0; // ADM não tem perfil
        }

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id");
            } else  {
                // Não é um erro, ADM não tem perfil, e novos usuários podem não ter
                // System.out.println("Aviso: Usuário logado mas sem perfil (cliente/farmácia) associado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro crítico ao buscar perfil: " + e.getMessage());
        }
        return id;
    }

    /**
     * Exibe o menu principal com base no perfil do usuário.
     * @param grupo O nome do grupo ("adm", "cliente", "farmacia")
     * @param usuarioId O ID da tabela 'usuarios' (para atualizar dados de usuário)
     * @param perfilId O ID da tabela 'clientes' ou 'farmacias' (para ações de perfil)
     */
    public static void mostrarMenu(String grupo, int usuarioId, int perfilId){
        Scanner sc = new Scanner(System.in);
        int opcao = 0;

        if (grupo.equals("adm")){
            // Loop do ADM. Termina quando 'opcao' for 6
            while(opcao != 6){
                System.out.println("\nMENU ADMINISTRADOR:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Gerenciar Usuários");
                System.out.println("2. Gerenciar Clientes");
                System.out.println("3. Gerenciar Farmácias");
                System.out.println("4. Gerenciar Endereços");
                System.out.println("5. Gerenciar Produtos (Visão ADM)");
                System.out.println("6. Sair (Voltar à tela inicial)"); // "Sair"

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }


                switch (opcao) {
                    case 1:
                        Usuario.exibirUsuarios("todos");
                        break;
                    case 2:
                        Cliente.exibirClientes();
                        break;
                    case 3:
                        Farmacia.exibirFarmacias();
                        break;
                    case 4:
                        Endereco.exibirEnderecos("adm");
                        break;
                    case 5:
                        Produtos.exibirProdutos(sc, grupo, 0); // ADM passa 0 como perfilId
                        break;
                    case 6:
                        System.out.println("Fazendo logout...");
                        break; // Quebra o 'while(opcao != 6)' e retorna ao Main
                    default:
                        System.out.println("Opção inválida.");
                }
            }
        } else if (grupo.equals("cliente")) {
            // =================================================================
            // CORREÇÃO AQUI
            // =================================================================
            // Loop do Cliente. Termina quando 'opcao' for 5
            while (opcao != 5) { // <-- MUDADO DE 4 PARA 5
                System.out.println("\nMENU CLIENTE:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Atualizar dados Pessoais");
                System.out.println("2. Atualizar meu Endereço");
                System.out.println("3. Ver Produtos (e Favoritos)");
                System.out.println("4. Gerenciar Formas de Pagamento"); // <-- ADICIONADO
                System.out.println("5. Sair (Voltar à tela inicial)"); // <-- MUDADO DE 4 PARA 5

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }

                switch (opcao) {
                    case 1:
                        Cliente.atualizarCliente(usuarioId);
                        break;
                    case 2:
                        Endereco.atualizarEndereco(usuarioId);
                        break;
                    case 3:
                        Produtos.exibirProdutos(sc, grupo, perfilId);
                        break;
                    case 4: // <-- ADICIONADO
                        // Chama o controller de pagamento, passando o ID do cliente (perfilId)
                        FormaPagamento.gerenciarFormasPagamento(sc, perfilId);
                        break;
                    case 5: // <-- MUDADO DE 4 PARA 5
                        System.out.println("Fazendo logout...");
                        break; // Quebra o 'while' e retorna ao Main
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
            // =================================================================
            // FIM DA CORREÇÃO
            // =================================================================
        } else if (grupo.equals("farmacia")) {
            // Loop da Farmácia. Termina quando 'opcao' for 4
            while (opcao != 4) {
                System.out.println("\nMENU FARMÁCIA:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Gerenciar Meus Produtos");
                System.out.println("2. Atualizar dados da Farmácia");
                System.out.println("3. Atualizar Endereço da Farmácia");
                System.out.println("4. Sair (Voltar à tela inicial)"); // "Sair"

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }

                switch (opcao) {
                    case 1:
                        Produtos.exibirProdutos(sc, grupo, perfilId);
                        break;
                    case 2:
                        Farmacia.atualizarFarmacia(sc);
                        break;
                    case 3:
                        Endereco.atualizarEndereco(usuarioId);
                        break;
                    case 4:
                        System.out.println("Fazendo logout...");
                        break; // Quebra o 'while(opcao != 4)' e retorna ao Main
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
        }
    }
}