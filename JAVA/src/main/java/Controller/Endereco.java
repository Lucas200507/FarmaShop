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
import java.sql.Statement;

public class Endereco {


    /**
     * Busca o ID do endereço (VARCHAR(7)) associado a um ID de usuário.
     * Ele procura primeiro em clientes, depois em farmácias.
     * @param con Conexão com o banco
     * @param idUsuario O ID da tabela 'usuarios'
     * @return O ID do endereço (ex: "a1b2c3d") ou null se não encontrar.
     * @throws SQLException
     */
    private static String findEnderecoIdFromUsuario(Connection con, int idUsuario) throws SQLException {
        String enderecoId = null;

        // 1. Procura na tabela CLIENTES
        String sqlCliente = "SELECT endereco_id FROM clientes WHERE usuario_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(sqlCliente)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                enderecoId = rs.getString("endereco_id");
            }
        }

        // 2. Se não achou, procura na tabela FARMACIAS
        if (enderecoId == null) {
            String sqlFarmacia = "SELECT endereco_id FROM farmacias WHERE usuario_id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sqlFarmacia)) {
                stmt.setInt(1, idUsuario);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    enderecoId = rs.getString("endereco_id");
                }
            }
        }

        return enderecoId; // Retorna o ID (ou null se não achou em nenhuma)
    }


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
            if (tipo != null && tipo.equals("adm")){ // <-- Corrigido (null check)
                System.out.println("\nDigite a opção que preferir:");
                System.out.println("1. Inserir novo endereço");
                System.out.println("2. Atualizar endereço");
                System.out.println("3. Deletar endereço");
                System.out.println("4. Sair da aba endereço");
                int opcao = sc.nextInt();
                sc.nextLine();
                Endereco e = new Endereco();
                // (O loop 'do-while' foi removido, pois o menu já está em loop no Main.java)
                switch (opcao) {
                    case 1:
                        String idEndereco = e.inserirEndereco(sc);
                        break;
                    case 2:
                        atualizarEndereco(0); // 0 significa que é um ADM
                        break;
                    case 3:
                        deletarEndereco(0); // 0 significa que é um ADM
                        break;
                    case 4:
                        System.out.println("Saindo...");
                        break;
                    default:
                        System.out.println("Opção inválida");
                        break;
                }
            }

            // (As chamadas rs.close(), stmt.close(), con.close() são desnecessárias
            //  dentro de um try-with-resources)

        } catch (SQLException e) {
            System.out.println("Erro em exibir Endereços: " + e.getMessage());
        }
    }

    public String inserirEndereco(Scanner sc) {
        String idEndereco = "0";
        try (Connection con = Conexao.getConnection()) {
            // CEP
            String cep = "";
            while (true) { // Loop infinito até que seja válido ou vazio
                System.out.println("Digite um CEP (somente números, 8 dígitos): ");
                cep = sc.nextLine().trim();
                if (cep.length() == 8 && cep.matches("^\\d+$")) {
                    break; // Válido
                } else {
                    System.out.println("CEP inválido. Você deve digitar 8 caracteres numéricos.");
                }
            }

            // Formata CEP com hífen
            String rcep = cep.substring(0, 5) + "-" + cep.substring(5);

            // Estado
            String[] estados = {
                    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO",
                    "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI",
                    "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
            };
            List<String> listaEstados = Arrays.asList(estados);
            String estado = "";
            while (true) {
                System.out.println("Digite a sigla do estado (ex: SP, GO, AC): ");
                estado = sc.nextLine().trim().toUpperCase();
                if (listaEstados.contains(estado)) {
                    break; // Válido
                } else {
                    System.out.println("Sigla de estado inválida!");
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
                System.out.println("Digite o número do endereço (ou Enter para pular): ");
                String n = sc.nextLine().trim();

                if (n.isEmpty()) {
                    break; // opcional, sai do loop
                }

                try {
                    numero = Integer.parseInt(n);
                    if (numero > 0) {
                        break; // Válido
                    } else {
                        System.out.println("O número deve ser maior que zero.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Digite apenas números positivos ou deixe em branco.");
                }
            }

            // Bairro
            System.out.println("Digite o bairro: ");
            String bairro = sc.nextLine().trim();

            // Complemento
            System.out.println("Digite o complemento (ex: ponto de referência): ");
            String complemento = sc.nextLine().trim();

            // Inserção no banco
            // (O ID é gerado pela Trigger 'trg_gerar_idEndereco')
            String sql = "INSERT INTO enderecos (cep, estado, cidade, rua, numero, bairro, complemento) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, rcep);
                stmt.setString(2, estado);
                stmt.setString(3, cidade);
                stmt.setString(4, rua);

                if(numero == 0) {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(5, numero);
                }

                stmt.setString(6, bairro);
                stmt.setString(7, complemento);
                stmt.executeUpdate();
            }

            System.out.println("Endereço criado com sucesso!");

            // Busca o ID do endereço que acabou de ser criado
            String sql2 = "SELECT id FROM enderecos WHERE cep = ? ORDER BY id DESC LIMIT 1";
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
        String id = "";
        try (Connection con = Conexao.getConnection()) {

            // =================================================================
            // CORREÇÃO: Buscar o endereco_id (String) nas tabelas filhas
            // =================================================================
            if (idUsuario != 0) { // Se for Cliente ou Farmácia
                id = findEnderecoIdFromUsuario(con, idUsuario);
                if (id == null) {
                    System.out.println("Erro: Nenhum endereço encontrado para este usuário.");
                    return;
                }
                System.out.println("Editando endereço (ID: " + id + ") vinculado ao seu usuário.");
            } else { // Se for ADM
                System.out.println("Digite o ID do endereço que deseja atualizar: ");
                id = sc.nextLine();
            }
            // =================================================================

            String sel = "SELECT * FROM enderecos WHERE id = ?";
            try (PreparedStatement selStmt = con.prepareStatement(sel)) {
                selStmt.setString(1, id);
                ResultSet rs = selStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Endereço não encontrado.");
                    return;
                }

                // ... (lógica de buscar dados atuais e pedir novos - seu código original) ...
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

                // ... (Lógica de Estado, Cidade, Rua, etc. - seu código original) ...
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
                Integer numero = numeroStr.isEmpty() ? null : Integer.parseInt(numeroStr); // Null se vazio

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
            } catch (NumberFormatException e) {
                System.out.println("ERRO: O número do endereço deve ser um valor numérico.");
            }

        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
        }
    }

    private static void deletarEndereco(int idUsuario) {
        Scanner  sc = new Scanner(System.in);
        String id = "";

        try (Connection con = Conexao.getConnection()) {

            // =================================================================
            // CORREÇÃO: Buscar o endereco_id (String) nas tabelas filhas
            // =================================================================
            if (idUsuario != 0) { // Se for Cliente ou Farmácia
                id = findEnderecoIdFromUsuario(con, idUsuario);
                if (id == null) {
                    System.out.println("Erro: Nenhum endereço encontrado para este usuário.");
                    return;
                }
                System.out.println("AVISO: Esta ação irá deletar o endereço (ID: " + id + ") do banco.");
                System.out.println("O seu perfil (cliente/farmácia) ficará SEM endereço associado.");
            } else { // Se for ADM
                System.out.println("Digite o ID do Endereço que deseja deletar: ");
                id = sc.nextLine();
            }
            // =================================================================

            PreparedStatement stmtCheck = con.prepareStatement("SELECT * FROM enderecos WHERE id = ?");
            stmtCheck.setString(1, id);
            ResultSet rsCheck = stmtCheck.executeQuery();

            if (!rsCheck.next()) {
                System.out.println("Endereço não encontrado, ID: " + id);
                return;
            }

            System.out.println("Tem certeza que deseja DELETAR o endereço ID: "+id+" ? (S/N)");
            String op = sc.nextLine().toUpperCase();

            if (op.equals("S")) {

                // 1. ANTES de deletar o endereço, precisamos desvincular
                //    o endereço das tabelas 'clientes' e 'farmacias'
                //    (Seu SQL não usa ON DELETE SET NULL, então fazemos manualmente)

                String sqlUnlinkC = "UPDATE clientes SET endereco_id = NULL WHERE endereco_id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sqlUnlinkC)) {
                    stmt.setString(1, id);
                    stmt.executeUpdate(); // Desvincula de clientes
                }

                String sqlUnlinkF = "UPDATE farmacias SET endereco_id = NULL WHERE endereco_id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sqlUnlinkF)) {
                    stmt.setString(1, id);
                    stmt.executeUpdate(); // Desvincula de farmácias
                }

                // 2. Agora sim, deletamos o endereço
                String sqlDelete = "DELETE FROM enderecos WHERE id = ?";
                try(PreparedStatement stmt3 = con.prepareStatement(sqlDelete)) {
                    stmt3.setString(1, id);
                    int rows = stmt3.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Endereço id = "+id+" Deletado com sucesso!!");
                    } else {
                        System.out.println("Erro: Endereço não foi deletado.");
                    }
                }
            } else {
                System.out.println("Exclusão cancelada.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar endereço: " + e.getMessage());
            System.out.println("Verifique se o ID está correto.");
        }
    }
}