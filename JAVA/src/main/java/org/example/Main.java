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
import Controller.FormaPagamento;
import Database.Conexao;
import org.example.Login;

public class Main {

    /**
     * MÉTODO 1: Ponto de entrada principal do aplicativo.
     * Controla o loop de login.
     */
    public static void main(String[] args) {
        Login l = new Login();

        // Loop principal do aplicativo
        while (true) {

            if (l.logar()) { // Chama a tela de Login/Cadastro
                // Se o login for bem-sucedido...
                String grupo = l.getGrupo();
                String usuario = l.getUsuario();
                int usuarioId = l.getId();

                // Busca o ID do perfil (cliente_id ou farmacia_id)
                int perfilId = getPerfilId(grupo, usuarioId);

                System.out.println("Bem vindo ao Sistema, " + usuario + "\n============================");

                // Mostra o menu principal (do adm, cliente ou farmacia)
                mostrarMenu(grupo, usuarioId, perfilId);

                // Quando 'mostrarMenu' terminar (usuário fez logout), o loop 'while(true)'
                // recomeça, voltando para a tela de login (l.logar()).

            } else {
                // Se l.logar() retornar false (usuário escolheu "4. Sair" no login)
                System.out.println("Encerrando FarmaShop. Volte sempre!");
                break; // Quebra o loop principal e encerra o app.
            }
        }
    } // --- FIM do método main ---


    /**
     * MÉTODO 2: Busca o ID do perfil (cliente ou farmácia)
     * com base no ID do usuário logado.
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
                // System.out.println("Aviso: Usuário logado mas sem perfil (cliente/farmácia) associado.");
            }

        } catch (SQLException e) {
            System.out.println("Erro crítico ao buscar perfil: " + e.getMessage());
        }
        return id;
    } // --- FIM do método getPerfilId ---


    /**
     * MÉTODO 3: Exibe o menu principal (ADM, Cliente ou Farmácia)
     * após o login ser bem-sucedido.
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
            // Loop do Cliente. Termina quando 'opcao' for 5
            while (opcao != 5) {
                System.out.println("\nMENU CLIENTE:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Atualizar dados Pessoais");
                System.out.println("2. Atualizar meu Endereço");
                System.out.println("3. Ver Produtos (e Favoritos)");
                System.out.println("4. Gerenciar Formas de Pagamento");
                System.out.println("5. Sair (Voltar à tela inicial)");

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }

                switch (opcao) {
                    case 1:
                        // CORREÇÃO: Passando (sc, usuarioId)
                        Cliente.atualizarCliente(sc, usuarioId);
                        break;
                    case 2:
                        Endereco.atualizarEndereco(usuarioId);
                        break;
                    case 3:
                        Produtos.exibirProdutos(sc, grupo, perfilId);
                        break;
                    case 4:
                        FormaPagamento.gerenciarFormasPagamento(sc, perfilId);
                        break;
                    case 5:
                        System.out.println("Fazendo logout...");
                        break; // Quebra o 'while' e retorna ao Main
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
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
                        System.out.println("Opção inválida");
                }
            }
        }
    }

}