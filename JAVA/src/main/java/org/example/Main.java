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

import Database.Conexao;


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
            }

        } catch (SQLException e) {
            System.out.println("Erro crítico ao buscar perfil: " + e.getMessage());
        }
        return id;
    }

    public static void mostrarMenu(String grupo, int usuarioId, int perfilId){
        Scanner sc = new Scanner(System.in);
        int opcao = 0;

        if (grupo.equals("adm")){
            // Loop do ADM.
            while(opcao != 6){
                System.out.println("\nMENU ADMINISTRADOR:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Gerenciar Usuários");
                System.out.println("2. Gerenciar Clientes");
                System.out.println("3. Gerenciar Farmácias");
                System.out.println("4. Gerenciar Endereços");
                System.out.println("5. Gerenciar Produtos (Visão ADM)");
                System.out.println("6. Sair (Voltar à tela inicial)");

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
                        Endereco.exibirEnderecos("adm", 0);
                        break;
                    case 5:
                        Produtos.exibirProdutos(sc, grupo, 0);
                        break;
                    case 6:
                        System.out.println("Fazendo logout...");
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            }
        } else if (grupo.equals("cliente")) {
            // Loop do Cliente.
            while (opcao != 5) {
                System.out.println("\nMENU CLIENTE:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Atualizar dados Pessoais");
                System.out.println("2. Atualizar meu Endereço");
                System.out.println("3. Ver Produtos (e Adicionar)");
                System.out.println("4. Ver Meu Carrinho");
                //System.out.println("5. Gerenciar Formas de Pagamento");
                System.out.println("5. Sair (Voltar à tela inicial)");

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }

                switch (opcao) {
                    case 1:
                        Cliente.atualizarCliente(sc, usuarioId); // Passa o ID do usuário
                        break;
                    case 2:
                        Endereco.atualizarEndereco(usuarioId);
                        break;
                    case 3:
                        Produtos.exibirProdutos(sc, grupo, perfilId);
                        break;
                    case 4:
                        Produtos.exibirCarrinho(sc, perfilId); // Chama o novo método
                        break;
                        /*
                    case 5:
                        FormaPagamento.gerenciarFormasPagamento(sc, perfilId);
                        break;
                        */
                    case 5:
                        System.out.println("Fazendo logout...");
                        break;
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
        } else if (grupo.equals("farmacia")) {

            // Loop da Farmácia.
            while (opcao != 4) {
                System.out.println("\nMENU FARMÁCIA:");
                System.out.println("Escolha uma das opções: ");
                System.out.println("1. Gerenciar Meus Produtos");
                System.out.println("2. Atualizar dados da Farmácia");
                System.out.println("3. Atualizar Endereço da Farmácia");
                System.out.println("4. Sair (Voltar à tela inicial)");

                try {
                    opcao = Integer.parseInt(sc.nextLine());
                } catch (Exception e) { opcao = 0; }

                switch (opcao) {
                    case 1:
                        Produtos.exibirProdutos(sc, grupo, perfilId);
                        break;
                    case 2:
                        Farmacia.atualizarFarmacia(sc, perfilId);
                        break;
                    case 3:
                        Endereco.atualizarEndereco(usuarioId);
                        break;
                    case 4:
                        System.out.println("Fazendo logout...");
                        break;
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
        }
    }
}