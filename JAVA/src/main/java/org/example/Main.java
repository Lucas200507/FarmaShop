package org.example;
import java.util.Scanner;

import Controller.Cliente;
import Controller.Endereco;
import Controller.Usuario;

public class Main {    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcao = 0;
        while(opcao != 5){
            System.out.println("MENU:");
            System.out.println("Escolha uma das opções: \n1.Usuários\n2.Clientes\n3.Farmácias\n4.Endereços\n5.Sair");
            opcao = sc.nextInt();

            switch (opcao) {
                case 1:
                    Usuario.exibirUsuarios();
                    break;
                case 2:
                    Cliente.exibirClientes();
                    break;
                case 4:
                    Endereco.exibirEnderecos();
                    break;
            }
        }
    }
}