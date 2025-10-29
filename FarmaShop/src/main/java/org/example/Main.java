package org.example;
import Controller.Endereco;
import Controller.Usuario;


import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Model.Enderecosdb.criarTabelas();
        Scanner sc = new Scanner(System.in);
        // instanciando Objetos
        Usuario u = new Usuario();
        Endereco e = new Endereco();

        int opcao = 0;
        while(opcao != 5){
            System.out.println("MENU:");
            System.out.println("Escolha uma das opções: \n1.Usuários\n2.Clientes\n3.Farmácias\n4.Endereços\n5.Sair");
            opcao = sc.nextInt();

            switch (opcao) {
                case 1:
                    u.exibirUsuarios();
                    break;
                case 4:
                    e.exibirEnderecos();
            }
        }
    }
}