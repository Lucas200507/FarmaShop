package Controller;

import Database.Conexao;
import Controller.Usuario;
import java.sql.*;
import java.util.List;
import java.util.Scanner;


public class Farmacia {

    private static String validarNumero(String valor, int TamMin, int TamMax) {
        if (valor == null) return null;
        String valorLimpo = valor.replaceAll("[^0-9]", "");
        if (valorLimpo.length() >= TamMin && valorLimpo.length() <= TamMax) return valorLimpo;
        return null;
    }

    public static void exibirFarmacias() {
        Scanner sc = new Scanner(System.in);
        System.out.println("===Farmácias===");

        String sql = "SELECT * FROM farmacias;";
        try (Connection con = Conexao.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nome fantasia: " + rs.getString("nome_fantasia"));
                System.out.println("CNPJ: " + rs.getString("cnpj"));
                System.out.println("Telefone: " + rs.getString("telefone"));
                System.out.println
                ("========================================================");
            }
        } catch (SQLException e){
            System.out.println("Erro ao exibir farmácias: " + e.getMessage());
        }

        System.out.println("\nDigite a opção que preferir:");
        System.out.println("1. Inserir nova farmácia");
        System.out.println("2. Atualizar farmácia");
        System.out.println("3. Deletar farmácia");
        System.out.println("4. Sair da aba farmácias");

        int opcao = sc.nextInt();
        sc.nextLine();
        Farmacia f = new Farmacia();
        switch (opcao) {
            case 1:
                int idFarmacia = f.inserirFarmacia(sc, 0, "0");
                break;
            case 2:
                atualizarFarmacia(sc);
                break;
            case 3:
                deletarFarmacia(sc);
                break;
            case 4:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida");
                break;

        }
    }

    public int inserirFarmacia(Scanner sc, int idUsuario, String idEndereco) {
        int idFarmacia = 0;

        if (idUsuario == 0) {
            boolean valido = false;
            Usuario.exibirUsuarios("cliente");
            do{
                System.out.println("Escolha um id válido:");
                if (sc.hasNextInt()) {
                    idUsuario = sc.nextInt();
                    if (idUsuario > 0){
                        String sql = "SELECT * FROM usuarios WHERE id = ?";
                        try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                            stmt.setInt(1, idUsuario);
                            ResultSet rs = stmt.executeQuery();
                            if (rs.next()){
                                valido = true;
                            } else {
                                idUsuario = 0;
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    System.out.println("Valor inválido! Não é um número inteiro.");
                    sc.nextLine(); // limpa o buffer
                }
            } while(!valido);
        }

        if (idEndereco.equals("0")) {
            boolean valido = false;
            Endereco.exibirEnderecos("cliente");
            do{
                System.out.println("Escolha um id válido:");
                idEndereco = sc.nextLine();
                String sql = "SELECT * FROM enderecos WHERE id = ?";
                try (Connection con = Conexao.getConnection(); PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setString(1, idEndereco);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()){
                        valido = true;
                    } else {
                        idEndereco = "0";
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } while(!valido);
        }


        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o nome jurídico (razão social):");
            String nomeJuridico = sc.nextLine();

            System.out.println("Digite o nome fantasia:");
            String nomeFantasia = sc.nextLine();

            // Validação de CNPJ (apenas remoção de não dígitos + tamanho 14)
            String cnpjValidado;
            do {
            System.out.println("Digite o CNPJ (14 dígitos): ");
            String cnpj = sc.nextLine();
            cnpjValidado = validarNumero(cnpj, 14, 14);
            if (cnpjValidado == null) System.out.println("CNPJ inválido. Digite novamente.");
            } while (cnpjValidado == null);

            System.out.println("Digite o alvará sanitário: ");
            String alvara = sc.nextLine();

            System.out.println("Digite o nome do responsável técnico (farmacêutico):");
            String responsavel = sc.nextLine();

            System.out.println("Digite o CRF do responsável:");
            String crf = sc.nextLine();

            // Telefone
            String telefoneValidado;
            do {
                System.out.println("Digite o telefone (10 ou 11 dígitos):");
                String telefone = sc.nextLine();
                telefoneValidado = validarNumero(telefone, 10, 11);
                if (telefoneValidado == null) System.out.println("Telefone inválido.");
            } while (telefoneValidado == null);

            String sql = "INSERT INTO farmacias (nome_juridico, nome_fantasia, cnpj, alvara_sanitario, responsavel_tecnico, crf, telefone, endereco_id, usuario_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, nomeJuridico);
                stmt.setString(2, nomeFantasia);
                stmt.setString(3, cnpjValidado);
                stmt.setString(4, alvara);
                stmt.setString(5, responsavel);
                stmt.setString(6, crf);
                stmt.setString(7, telefoneValidado);
                stmt.setString(8, idEndereco);
                stmt.setInt(9, idUsuario);

                stmt.executeUpdate();
                System.out.println("Farmácia inserida com sucesso!");
                String sql4 = "SELECT id FROM farmacias ORDER BY id DESC LIMIT 1";
                try (PreparedStatement stmt4 = con.prepareStatement(sql4)) {
                    ResultSet rs = stmt4.executeQuery();
                    if (rs.next()){
                        idFarmacia = rs.getInt("id");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao inserir farmácia: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }

        return idFarmacia;
    }

    private static void atualizarFarmacia(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID da farmácia que deseja atualizar: ");
            int id = Integer.parseInt(sc.nextLine());

            String sel = "SELECT * FROM farmacias WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
               selStmt.setInt(1, id);
               ResultSet rs = selStmt.executeQuery();
               if (!rs.next()) {
                   System.out.println("Farmácia não encontrada.");
                   return;
               }

               String nomeJuridicoAtual = rs.getNString("nome_juridico");
               String nomeFantasiaAtual = rs.getString("nome_fantasia");
               String cnpjAtual = rs.getString("cnpj");
               String alvaraAtual = rs.getString("alvara_sanitario");
               String responsavelAtual = rs.getString("responsavel_tecnico");
               String crfAtual = rs.getString("crf");
               String telefoneAtual = rs.getString("telefone");
               int enderecoAtual = rs.getInt("endereco_id");
               int usuarioAtual = rs.getInt("usuario_id");

                System.out.println("Digite o novo nome juridico (ou Enter para manter: " + nomeJuridicoAtual + "):");
                String nomeJuridico = sc.nextLine();

                System.out.println("Digite o novo nome fantasia (ou Enter para manter: " + nomeFantasiaAtual + "):");
                String nomeFantasia = sc.nextLine();

                String cnpj = "";
                boolean cnpjValido = false;
                while (!cnpjValido) {
                    System.out.println("Digite o novo CNPJ (14 dígitos) ou Enter para manter: " + cnpjAtual);
                    String cnpjDigitado = sc.nextLine();
                    if (cnpjDigitado.isEmpty()) { cnpj = ""; cnpjValido = true; }
                    else {
                        cnpj = validarNumero(cnpjDigitado, 14, 14);
                        if (cnpj == null) System.out.println("CNPJ inválido.");
                        else cnpjValido = true;
                    }
                }

                System.out.println("Digite o novo alvará (ou Enter para manter: " + alvaraAtual + "):");
                String alvara = sc.nextLine();

                System.out.println("Digite o novo responsável técnico (ou Enter para manter: " + responsavelAtual + "):");
                String responsavel = sc.nextLine();

                System.out.println("Digite o novo CRF (ou Enter para manter: " + crfAtual + "):");
                String crf = sc.nextLine();

                String telefone = "";
                boolean telValido = false;
                while (!telValido) {
                    System.out.println("Digite o novo telefone (10 ou 11 dígitos) ou Enter para manter: " + telefoneAtual);
                    String telDigitado = sc.nextLine();
                    if (telDigitado.isEmpty()) { telefone = ""; telValido = true; }
                    else {
                        telefone = validarNumero(telDigitado, 10, 11);
                        if (telefone == null) System.out.println("Telefone inválido.");
                        else telValido = true;
                    }
                }

                System.out.println("Digite novo endereco_id (ou Enter para manter: " + enderecoAtual + "):");
                String enderecoStr = sc.nextLine();
                Integer enderecoId = enderecoStr.isEmpty() ? null : Integer.parseInt(enderecoStr);

                StringBuilder sql = new StringBuilder("UPDATE farmacias SET ");
                List<Object> params = new java.util.ArrayList<>();
                boolean first = true;

                if (!nomeJuridico.isEmpty()) { if(!first) sql.append(",");sql.append("nome_juridico = ?"); params.add (nomeJuridico); first = false;}
                if (!nomeFantasia.isEmpty()) { if(!first) sql.append(", "); sql.append("nome_fantasia = ?"); params.add(nomeFantasia); first = false; }
                if (!cnpj.isEmpty()) { if(!first) sql.append(", "); sql.append("cnpj = ?"); params.add(cnpj); first = false; }
                if (!alvara.isEmpty()) { if(!first) sql.append(", "); sql.append("alvara_sanitario = ?"); params.add(alvara); first = false; }
                if (!responsavel.isEmpty()) { if(!first) sql.append(", "); sql.append("responsavel_tecnico = ?"); params.add(responsavel); first = false; }
                if (!crf.isEmpty()) { if(!first) sql.append(", "); sql.append("crf = ?"); params.add(crf); first = false; }
                if (!telefone.isEmpty()) { if(!first) sql.append(", "); sql.append("telefone = ?"); params.add(telefone); first = false; }
                if (enderecoId != null) { if(!first) sql.append(", "); sql.append("endereco_id = ?"); params.add(enderecoId); first = false; }

                if (params.isEmpty()) {
                    System.out.println("Nenhum campo para atualizar.");
                    return;
                }

                sql.append(" WHERE id = ?");
                params.add(id);

                try (PreparedStatement upd = con.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        Object p = params.get(i);
                        if (p instanceof Integer) upd.setInt(i+1, (Integer) p);
                        else upd.setString(i+1, p.toString());
                    }
                    upd.executeUpdate();
                    System.out.println("Farmácia atualizada com sucesso!");
                }

            } catch (SQLException e) {
                System.out.println("Erro ao buscar farmácia: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }

    }

    private static void deletarFarmacia(Scanner sc) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Digite o ID da farmácia a deletar:");
            int id = Integer.parseInt(sc.nextLine());

            System.out.println("Tem certeza que deseja deletar a farmácia ID " + id + "? (S/N)");
            String conf = sc.nextLine();
            if (!conf.equalsIgnoreCase("S")) {
                System.out.println("Operação cancelada.");
                return;
            }

            String sql = "DELETE FROM farmacias WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                System.out.println("Farmácia deletada com sucesso!");
            } catch (SQLException e) {
                System.out.println("Erro ao deletar farmácia: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

}

