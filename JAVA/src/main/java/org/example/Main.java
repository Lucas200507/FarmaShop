package org.example;
import java.util.Scanner;

import Controller.*;

public class Main {    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcao = 0;
        while(opcao != 7){
            System.out.println("MENU:");
            System.out.println("Escolha uma das opções: \n1.Usuários\n2.Clientes\n3.Farmácias\n4.Endereços\n5.Produtos\n6.Categorias\n7.Sair");
            opcao = sc.nextInt();

            switch (opcao) {
                case 1:
                    Usuario.exibirUsuarios("todos");
                    break;
                case 2:
                    Cliente.exibirClientes();
                    break;
                case 3:
                    Farmacia.exibirFarmacias();
                case 4:
                    Endereco.exibirEnderecos();
                    break;
                case 5: Produtos.exibirProdutos();
                    break;
                case 6: CategoriaProduto.exibirCategorias();
                    break;
            }
        }
    }
}