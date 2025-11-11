package org.example;
import java.util.Scanner;

import Controller.*;
import org.example.Login;
public class Main {
    public static void main(String[] args) {
        Login l = new Login();

        if (l.logar()) {
            String grupo = l.getGrupo();
            String usuario = l.getUsuario();
            int id = l.getId();
            System.out.println("Bem vindo ao Sistema, " + usuario + "\n============================");
            mostrarMenu(grupo, id);
        }
    }

    public static void mostrarMenu(String grupo, int id){
        Usuario u = new  Usuario();

        Scanner sc = new Scanner(System.in);
        int opcao = 0;
        if (grupo.equals("adm")){
            while(opcao != 5){
                System.out.println("MENU:");
                System.out.println("Escolha uma das opções: \n1.Usuários\n2.Clientes\n3.Farmácias\n4.Endereços\n5.Sair");
                opcao = sc.nextInt();

                switch (opcao) {
                    case 1:
                        u.exibirUsuarios("todos");
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
                }
            }
        } else if (grupo.equals("cliente")) {
            while (opcao != 5) {
                System.out.println("MENU:");
                System.out.println("Escolha uma das opções: \n1.Atualizar dados Pessoais\n2.Cadastrar novo Endereço\n3.Exibir Catálogo de produtos\n4.Carrinho\n5.Sair");
                opcao = sc.nextInt();

                switch (opcao) {
                    case 1:
                        Cliente.atualizarCliente(id);
                        break;
                    case 2:
                        Endereco.atualizarEndereco(id);
                        break;
                    case 3:
                        Produtos.exibirProdutos("cliente");
                        break;
                    case 4:
                        //Produto.exibirCarrinho(id);
                        break;
                    case 5:
                        System.out.println("Saindo..");
                        break;
                    default:
                        System.out.println("Escolha uma opção válida");
                }
            }
        }
    }
}