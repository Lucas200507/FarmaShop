package Controller;

import Database.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

public class Endereco {
    public static void exibirEnderecos(){
        System.out.println("=== ENDEREÇOS ===");
        Scanner sc = new Scanner(System.in);
        try (Connection con = Conexao.getConnection()){
            String sql = "SELECT * FROM enderecos;";
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("ID: "+rs.getString("id"));
                System.out.println("CEP: "+rs.getString("cep"));
                System.out.println("Estado: "+rs.getString("estado"));
                System.out.println("Cidade: "+rs.getString("cidade"));
                System.out.println("Rua: "+rs.getString("rua"));
                System.out.println("Número: "+rs.getString("numero"));
                System.out.println("Bairro: "+rs.getString("bairro"));
                System.out.println("Complemento: "+rs.getString("complemento"));
                System.out.println("====================================================");
            }

            System.out.println("\nDigite a opção que preferir:");
            System.out.println("1. Inserir novo endereço");
            System.out.println("2. Atualizar endereço");
            System.out.println("3. Deletar endereço");
            System.out.println("4. Sair da aba endereço");
            int opcao = sc.nextInt();
            sc.nextLine();
            do {
                switch (opcao) {
                    case 1:
                        inserirEndereco(sc);
                        break;
                    case 2:
                        atualizarEndereco(0);
                        break;
                    case 3:
                        deletarEndereco(sc);
                        break;
                    case 4:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida");
                        break;
                }
            } while (opcao!=1&&opcao!=2&&opcao!=3&&opcao!=4);
            con.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Erro em exibir Endereços: " + e.getMessage());
        }
    }

    private static void inserirEndereco(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            // CEP
            String cep = "";
            while (!cep.matches("^\\d+$") || cep.length() != 8) {
                System.out.println("Digite um CEP (somente números, 8 dígitos): ");
                cep = sc.nextLine().trim();
                if (cep.length() != 8 || !cep.matches("^\\d+$")) {
                    System.out.println("Você deve digitar 8 caracteres numéricos.");
                }
            }

            // Formata CEP com hífen
            StringBuilder sb = new StringBuilder(cep);
            sb.insert(5, "-");
            String rcep = sb.toString();

            // Estado
            String[] estados = {
                    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                    "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                    "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
            };
            String estado = "";
            while (!Arrays.asList(estados).contains(estado)) {
                System.out.println("Digite a sigla do estado (ex: SP, GO, AC): ");
                estado = sc.nextLine().trim().toUpperCase();
                if (!Arrays.asList(estados).contains(estado)) {
                    System.out.println("Opção inválida!");
                }
            }

            // Cidade
            System.out.println("Digite o nome da cidade: ");
            String cidade = sc.nextLine().trim();

            // Rua
            System.out.println("Digite o nome da rua: ");
            String rua = sc.nextLine().trim();

            // Número (opcional e apenas positivo)
            int numero = 0;
            while (true) {
                System.out.println("Digite o número do endereço (não obrigatório): ");
                String n = sc.nextLine().trim();

                if (n.isEmpty()) {
                    break; // opcional, sai do loop
                }

                if (!n.matches("^\\d+$")) {
                    System.out.println("Digite apenas números positivos ou deixe em branco.");
                    continue;
                }

                numero = Integer.parseInt(n);
                if (numero <= 0) {
                    System.out.println("O número deve ser maior que zero.");
                    continue;
                }
                break; // válido
            }

            // Bairro
            System.out.println("Digite o bairro: ");
            String bairro = sc.nextLine().trim();

            // Complemento
            System.out.println("Digite o complemento (ex: ponto de referência): ");
            String complemento = sc.nextLine().trim();

            // Inserção no banco
            String sql = "INSERT INTO enderecos (cep, estado, cidade, rua, numero, bairro, complemento) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, rcep);
            stmt.setString(2, estado);
            stmt.setString(3, cidade);
            stmt.setString(4, rua);
            stmt.setInt(5, numero);
            stmt.setString(6, bairro);
            stmt.setString(7, complemento);
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Endereço criado com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao criar o endereço: " + e.getMessage());
        }
    }

    public static void atualizarEndereco(Integer idUsuario){
        Scanner  sc = new Scanner(System.in);
        boolean erro;
        try (Connection con = Conexao.getConnection()) {
            do{
                erro = true;
                System.out.println("Digite o id do endereço que gostaria alterar");
                int id = sc.nextInt();
                sc.nextLine();
                try {
                    String sql = "SELECT * FROM  enderecos WHERE id = ?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setInt(1, id);
                    if (stmt.execute()){
                        erro = false;
                        String cep = "";
                        while (!cep.matches("^\\d+$") || cep.length() != 8) {
                            System.out.println("Digite um CEP (somente números, 8 dígitos): ");
                            cep = sc.nextLine().trim();
                            if (cep.length() != 8 || !cep.matches("^\\d+$")) {
                                System.out.println("Você deve digitar 8 caracteres numéricos.");
                            }
                        }

                        // Formata CEP com hífen
                        StringBuilder sb = new StringBuilder(cep);
                        sb.insert(5, "-");
                        String rcep = sb.toString();

                        // Estado
                        String[] estados = {
                                "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                                "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                                "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
                        };
                        String estado = "";
                        while (!Arrays.asList(estados).contains(estado)) {
                            System.out.println("Digite a sigla do estado (ex: SP, GO, AC): ");
                            estado = sc.nextLine().trim().toUpperCase();
                            if (!Arrays.asList(estados).contains(estado)) {
                                System.out.println("Opção inválida!");
                            }
                        }

                        // Cidade
                        System.out.println("Digite o nome da cidade: ");
                        String cidade = sc.nextLine().trim();

                        // Rua
                        System.out.println("Digite o nome da rua: ");
                        String rua = sc.nextLine().trim();

                        // Número (opcional e apenas positivo)
                        int numero = 0;
                        while (true) {
                            System.out.println("Digite o número do endereço (não obrigatório): ");
                            String n = sc.nextLine().trim();

                            if (n.isEmpty()) {
                                break; // opcional, sai do loop
                            }

                            if (!n.matches("^\\d+$")) {
                                System.out.println("Digite apenas números positivos ou deixe em branco.");
                                continue;
                            }

                            numero = Integer.parseInt(n);
                            if (numero <= 0) {
                                System.out.println("O número deve ser maior que zero.");
                                continue;
                            }
                            break; // válido
                        }

                        // Bairro
                        System.out.println("Digite o bairro: ");
                        String bairro = sc.nextLine().trim();

                        // Complemento
                        System.out.println("Digite o complemento (ex: ponto de referência): ");
                        String complemento = sc.nextLine().trim();

                        String sql2 = "UPDATE enderecos SET cep = ?, estado = ?, cidade = ?, rua = ?, numero = ?, bairro = ?, complemento = ? WHERE id = ?";
                        PreparedStatement stmt2 = con.prepareStatement(sql2);
                        stmt2.setString(1, rcep);
                        stmt2.setString(2, estado);
                        stmt2.setString(3, cidade);
                        stmt2.setString(4, rua);
                        stmt2.setInt(5, numero);
                        stmt2.setString(6, bairro);
                        stmt2.setString(7, complemento);
                        stmt2.setInt(8, id);
                        stmt2.executeUpdate();
                        System.out.println("Endereço atualizado com sucesso!");
                        stmt.close();
                        stmt2.close();
                        con.close();

                    } else {
                        System.out.println("Digite um id válido");;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            } while (erro);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deletarEndereco(Scanner sc){
        boolean erro = true, cancelado = false, deletado = false;
        try (Connection con = Conexao.getConnection()) {
            do {
                erro = true;
                System.out.println("Digite o id do endereço que gostaria de alterar: ");
                int id = sc.nextInt();
                sc.nextLine();

                try {
                    String sql = "SELECT * FROM enderecos WHERE id = ?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) { // ← Aqui corrigido: executeQuery() retorna ResultSet
                        erro = false;

                        // ======= CEP =======
                        String cep = "";
                        while (!cep.matches("^\\d+$") || cep.length() != 8) {
                            System.out.println("Digite um CEP (somente números, 8 dígitos): ");
                            cep = sc.nextLine().trim();
                            if (cep.length() != 8 || !cep.matches("^\\d+$")) {
                                System.out.println("Você deve digitar 8 caracteres numéricos.");
                            }
                        }

                        String rcep = cep.substring(0, 5) + "-" + cep.substring(5);

                        // ======= Estado =======
                        String[] estados = {
                                "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                                "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                                "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
                        };
                        String estado = "";
                        while (!Arrays.asList(estados).contains(estado)) {
                            System.out.println("Digite a sigla do estado (ex: SP, GO, AC): ");
                            estado = sc.nextLine().trim().toUpperCase();
                            if (!Arrays.asList(estados).contains(estado)) {
                                System.out.println("Opção inválida!");
                            }
                        }

                        // ======= Cidade =======
                        System.out.println("Digite o nome da cidade: ");
                        String cidade = sc.nextLine().trim();

                        // ======= Rua =======
                        System.out.println("Digite o nome da rua: ");
                        String rua = sc.nextLine().trim();

                        // ======= Número =======
                        Integer numero = null;
                        while (true) {
                            System.out.println("Digite o número do endereço (não obrigatório): ");
                            String n = sc.nextLine().trim();

                            if (n.isEmpty()) {
                                break; // opcional
                            }

                            if (!n.matches("^\\d+$")) {
                                System.out.println("Digite apenas números positivos ou deixe em branco.");
                                continue;
                            }

                            numero = Integer.parseInt(n);
                            if (numero <= 0) {
                                System.out.println("O número deve ser maior que zero.");
                                continue;
                            }
                            break;
                        }

                        // ======= Bairro =======
                        System.out.println("Digite o bairro: ");
                        String bairro = sc.nextLine().trim();

                        // ======= Complemento =======
                        System.out.println("Digite o complemento (ex: ponto de referência): ");
                        String complemento = sc.nextLine().trim();

                        // ======= Atualiza no banco =======
                        String sql2 = "UPDATE enderecos SET cep = ?, estado = ?, cidade = ?, rua = ?, numero = ?, bairro = ?, complemento = ? WHERE id = ?";
                        PreparedStatement stmt2 = con.prepareStatement(sql2);
                        stmt2.setString(1, rcep);
                        stmt2.setString(2, estado);
                        stmt2.setString(3, cidade);
                        stmt2.setString(4, rua);

                        if (numero != null) {
                            stmt2.setInt(5, numero);
                        } else {
                            stmt2.setNull(5, java.sql.Types.INTEGER);
                        }

                        stmt2.setString(6, bairro);
                        stmt2.setString(7, complemento);
                        stmt2.setInt(8, id);
                        stmt2.executeUpdate();

                        System.out.println("✅ Endereço atualizado com sucesso!");

                        stmt2.close();
                        stmt.close();
                        rs.close();

                    } else {
                        System.out.println("❌ ID de endereço não encontrado.");
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            } while (erro);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
