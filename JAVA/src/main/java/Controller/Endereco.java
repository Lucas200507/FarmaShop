package Controller;

import Database.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Endereco {
    public static void exibirEnderecos(String tipo){
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
        if (tipo == "adm"){
            System.out.println("\nDigite a opção que preferir:");
            System.out.println("1. Inserir novo endereço");
            System.out.println("2. Atualizar endereço");
            System.out.println("3. Deletar endereço");
            System.out.println("4. Sair da aba endereço");
            int opcao = sc.nextInt();
            sc.nextLine();
            Endereco e = new Endereco();
            do {
                switch (opcao) {
                    case 1:
                        String idEndereco = e.inserirEndereco(sc);
                        break;
                    case 2:
                        atualizarEndereco(0);
                        break;
                    case 3:
                        deletarEndereco(0);
                        break;
                    case 4:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida");
                        break;
                }
            } while (opcao!=1&&opcao!=2&&opcao!=3&&opcao!=4);
        }

            con.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Erro em exibir Endereços: " + e.getMessage());
        }
    }

    public String inserirEndereco(Scanner sc) {
        String idEndereco = "0";
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
            String sql2 = "SELECT id FROM enderecos WHERE cep = ?";
            try(PreparedStatement stmt2 = con.prepareStatement(sql2)){
                stmt2.setString(1, rcep);
                ResultSet rs = stmt2.executeQuery();
                if (rs.next()) {
                    idEndereco = rs.getString("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao criar o endereço: " + e.getMessage());
        }
        return idEndereco;
    }

    public static void atualizarEndereco(int idUsuario) {
        Scanner sc = new Scanner(System.in);
        String sel = "", id = "";
        try (Connection con = Conexao.getConnection()) {
            if (idUsuario != 0) {
                String sql5 = "SELECT endereco_id FROM usuarios WHERE id = ?";
                PreparedStatement stmt5 = con.prepareStatement(sql5);
                stmt5.setInt(1, idUsuario);
                ResultSet rs5 = stmt5.executeQuery();
                if (rs5.next()) {
                    id = rs5.getString("endereco_id");
                }
            } else {
                System.out.println("Digite o ID do endereço que deseja atualizar: ");
                id = sc.nextLine();
            }
                sel = "SELECT * FROM enderecos WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setString(1, id);
                ResultSet rs = selStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Endereço não encontrado.");
                    return;
                }

                String estadoAtual = rs.getString("estado");
                String cidadeAtual = rs.getString("cidade");
                String ruaAtual = rs.getString("rua");
                int numeroAtual = rs.getInt("numero");
                String bairroAtual = rs.getString("bairro");
                String complementoAtual = rs.getString("complemento");
                String cepAtual = rs.getString("cep");

                System.out.println("Digite o novo CEP (8 dígitos, apenas números) ou Enter para manter: " + cepAtual);
                String cepDigitado = sc.nextLine().trim();
                String cep = "";
                if (cepDigitado.isEmpty()) {
                    cep = "";
                } else {
                    while (!cepDigitado.matches("^\\d{8}$")) {
                        System.out.println("CEP inválido. Digite novamente (8 dígitos numéricos): ");
                        cepDigitado = sc.nextLine().trim();
                    }
                    cep = cepDigitado.substring(0, 5) + "-" + cepDigitado.substring(5);
                }

                // Estado
                String[] estados = {
                        "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                        "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                        "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
                };
                List<String> listaEstados = Arrays.asList(estados);
                System.out.println("Digite o novo estado (ex: SP, GO, AC) ou Enter para manter: " + estadoAtual);
                String estado = sc.nextLine().trim().toUpperCase();
                if (!estado.isEmpty() && !listaEstados.contains(estado)) {
                    while (!listaEstados.contains(estado)) {
                        System.out.println("Estado inválido. Digite novamente (ex: SP, GO, AC): ");
                        estado = sc.nextLine().trim().toUpperCase();
                    }
                }

                System.out.println("Digite a nova cidade (ou Enter para manter: " + cidadeAtual + "): ");
                String cidade = sc.nextLine().trim();

                System.out.println("Digite a nova rua (ou Enter para manter: " + ruaAtual + "): ");
                String rua = sc.nextLine().trim();

                System.out.println("Digite o novo número (ou Enter para manter: " + numeroAtual + "): ");
                String numeroStr = sc.nextLine().trim();
                Integer numero = numeroStr.isEmpty() ? null : Integer.parseInt(numeroStr);

                System.out.println("Digite o novo bairro (ou Enter para manter: " + bairroAtual + "): ");
                String bairro = sc.nextLine().trim();

                System.out.println("Digite o novo complemento (ou Enter para manter: " + complementoAtual + "): ");
                String complemento = sc.nextLine().trim();

                // Montagem dinâmica do SQL
                StringBuilder sql = new StringBuilder("UPDATE enderecos SET ");
                List<Object> params = new ArrayList<>();
                boolean first = true;

                if (!cep.isEmpty()) { if (!first) sql.append(", "); sql.append("cep = ?"); params.add(cep); first = false; }
                if (!estado.isEmpty()) { if (!first) sql.append(", "); sql.append("estado = ?"); params.add(estado); first = false; }
                if (!cidade.isEmpty()) { if (!first) sql.append(", "); sql.append("cidade = ?"); params.add(cidade); first = false; }
                if (!rua.isEmpty()) { if (!first) sql.append(", "); sql.append("rua = ?"); params.add(rua); first = false; }
                if (numero != null) { if (!first) sql.append(", "); sql.append("numero = ?"); params.add(numero); first = false; }
                if (!bairro.isEmpty()) { if (!first) sql.append(", "); sql.append("bairro = ?"); params.add(bairro); first = false; }
                if (!complemento.isEmpty()) { if (!first) sql.append(", "); sql.append("complemento = ?"); params.add(complemento); first = false; }

                if (params.isEmpty()) {
                    System.out.println("Nenhum campo foi alterado.");
                    return;
                }

                sql.append(" WHERE id = ?");
                params.add(id);

                try (PreparedStatement upd = con.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        Object p = params.get(i);
                        if (p instanceof Integer) upd.setInt(i + 1, (Integer) p);
                        else upd.setString(i + 1, p.toString());
                    }
                    upd.executeUpdate();
                    System.out.println("Endereço atualizado com sucesso!");
                }

            } catch (SQLException e) {
                System.out.println("Erro ao buscar endereço: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

    private static void deletarEndereco(int idUsuario) throws SQLException {
        Scanner  sc = new Scanner(System.in);
        boolean erro = true, cancelado = false, deletado = false;
        String id = "";
        do {
            if (idUsuario == 0) {
                System.out.println("Digite o id do Endereço que deseja deletar: ");
                id = sc.nextLine();
            } else {
                String sql = "SELECT endereco_id FROM usuarios WHERE id = ?";
                try {
                    Connection con = Conexao.getConnection();
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setInt(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        id = rs.getString("endereco_id");
                        erro = false;
                    } else {
                        System.out.println("Endereço não encontrado.");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            try{
                Connection con = Conexao.getConnection();
                PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM enderecos WHERE id = ?");
                stmt2.setString(1, id);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) {
                    System.out.println("Deseja deletar o endereço ? (S/N)");
                    String op = sc.nextLine().toUpperCase();
                    if (op.equals("S")) {
                        PreparedStatement stmt3 = con.prepareStatement("DELETE FROM enderecos WHERE id = ?");
                        stmt3.setString(1, id);
                        ResultSet rs3 = stmt3.executeQuery();
                        if (rs3.next()) {
                            System.out.println("Endereço id = "+id+" Deletado com sucesso!!");
                            deletado = true;
                        } else {
                            System.out.println("Erro em deletar endereço");
                        }
                    } else {
                        System.out.println("Exclusão cancelada.");
                        cancelado = true;
                    }
                } else {
                    System.out.println("Endereço não encontrado, digite um id válido");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


        }  while (!erro || !deletado || cancelado);

    }

}
