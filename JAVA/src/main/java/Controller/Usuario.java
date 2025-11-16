package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static org.example.Login.md5Upper;

import org.bson.Document;

// Imports do BANCO
import Database.Conexao;
import Database.ConexaoMongo;
import org.bson.types.ObjectId;

public class Usuario {

    private static boolean validarEmail(String email){
        try {
            Connection con = Conexao.getConnection();
            String sql = "SELECT email FROM usuarios WHERE email = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Email já cadastrado.");
                return false;
            } else {
                // Email não existe
                return true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (email == null) return  false;
        email = email.trim();
        return !email.isEmpty() && email.contains("@") && email.indexOf('@') != 0 && email.indexOf('@') != email.length() - 1;
    }

    public static void exibirUsuarios(String tipo) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== USUÁRIOS ===");

        if (tipo.equals("todos")) {
            // Ajustado para usar a VIEW (como no seu SQL)
            String sql = "SELECT * FROM vw_usuarios WHERE situacao = 'ativo';";

            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Tipo: " + rs.getString("grupo"));
                    System.out.println("Situação: " + rs.getString("situacao"));
                    System.out.println("Email: " + rs.getString("email"));
                    System.out.println("============================================");
                }
            } catch (SQLException e) {
                System.out.println("Erro ao exibir usuários: " + e.getMessage());
            }

            // Menu de opções
            System.out.println("\nDigite a opção que preferir:");
            System.out.println("1. Inserir novo usuário (ADM)");
            System.out.println("2. Atualizar usuário");
            System.out.println("3. Desativar usuário");
            System.out.println("4. Sair da aba usuários");

            int opcao = 0;
            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) { /* Fica 0 */ }

            Usuario u = new Usuario();

            switch (opcao) {
                case 1:
                    int idUsuario = u.inserirUsuario(sc, 0); // 0 = ADM escolhe o grupo
                    break;
                case 2:
                    atualizarUsuario(sc);
                    break;
                case 3:
                    deletarUsuario(sc); // Renomeado para 'desativar'
                    break;
                case 4:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }

        } else {
            // Listagem para Farmacia.java e Cliente.java (sem menu)
            String sql = "SELECT u.*, gu.nome AS tipo FROM usuarioGrupo ug LEFT JOIN usuarios u ON u.id = ug.usuario_id LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id WHERE u.situacao = 'ativo' AND gu.nome = ?;";
            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)){
                stmt.setString(1, tipo);
                try(ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id"));
                        System.out.println("Tipo: " + rs.getString("tipo"));
                        System.out.println("Situação: " + rs.getString("situacao"));
                        System.out.println("Email: " + rs.getString("email"));
                        System.out.println("Data de Alteração: "  + rs.getTimestamp("dataAlteracao"));
                        System.out.println("============================================");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao exibir usuários: " + e.getMessage());
            }
        }
    }

    public int inserirUsuario(Scanner sc, int idGrupo) {
        int idUsuario = 0; // 0 é o sinal de falha

        if (idGrupo == 0){ // Se 0, é o ADM no menu "Gerenciar Usuários"
            boolean errado = true;
            do{
                System.out.println("Escolha o tipo de usuário:\n1.Adm\n2.Cliente\n3.Farmácia");
                try {
                    int op = Integer.parseInt(sc.nextLine());
                    switch (op) {
                        case 1: idGrupo = 1; errado = false; break;
                        case 2: idGrupo = 2; errado = false; break;
                        case 3: idGrupo = 3; errado = false; break;
                        default: System.out.println("Opção inválida");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Opção inválida. Digite 1, 2 ou 3.");
                }
            } while(errado);
        }

        String email;
        do {
            System.out.println("Digite o email:");
            email = sc.nextLine();
            if (!validarEmail(email)) System.out.println("Email inválido.");
        } while (!validarEmail(email));

        String senha, confSenha;
        do {
            System.out.println("Digite a senha:");
            senha = sc.nextLine();
            System.out.println("Confirme a senha:");
            confSenha = sc.nextLine();
            if (!senha.equals(confSenha)) {
                System.out.println("Senhas não coincidem. Tente novamente.");
            }
        } while (!senha.equals(confSenha));

        try (Connection con = Conexao.getConnection()) {

            String sql = "INSERT INTO usuarios (situacao, email, senha) VALUES ('ativo', ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, email);
                stmt.setString(2, senha); // Trigger 'senha_login' criptografa (UPPER(MD5))
                int rowsAfetadas = stmt.executeUpdate();

                if (rowsAfetadas > 0) {

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idUsuario = rs.getInt(1); // ID AUTO_INCREMENT do MySQL
                        }
                    }

                    if (idUsuario == 0) {
                        throw new SQLException("Falha ao obter o ID do usuário (Auto_Increment).");
                    }

                    // Relacionamento na tabela usuarioGrupo
                    String sqlGrupo = "INSERT INTO usuarioGrupo (usuario_id, grupo_id) VALUES (?, ?)";
                    try (PreparedStatement stmt3 = con.prepareStatement(sqlGrupo)) {
                        stmt3.setInt(1, idUsuario);
                        stmt3.setInt(2, idGrupo);
                        stmt3.executeUpdate();
                    }

                    System.out.println("Usuário inserido com sucesso no MySQL! ID: " + idUsuario);
                }

            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário no MySQL: " + e.getMessage());
            return 0; // falhou no MySQL, nem tenta o Mongo
        }

        try {
            MongoDatabase db = Database.ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");
            MongoCollection<Document> colGrupos   = db.getCollection("gruposUsuarios");

            Document grupoDoc = colGrupos.find(eq("id", idGrupo)).first();
            if (grupoDoc == null) {
                System.out.println("Aviso: grupo id=" + idGrupo + " não encontrado no Mongo. Usuário não será inserido no Mongo.");
                return idUsuario;
            }

            ObjectId grupoObjectId = grupoDoc.getObjectId("_id");

            String senhaCriptografada = md5Upper(senha);

            Document novoUsuario = new Document()
                    .append("email", email)
                    .append("senha", senhaCriptografada)
                    .append("situacao", "ativo")
                    .append("grupo_id", grupoObjectId)
                    .append("id_mysql", idUsuario); // opcional: referência cruzada

            colUsuarios.insertOne(novoUsuario);

            System.out.println("Usuário replicado com sucesso no MongoDB! _id: " + novoUsuario.getObjectId("_id"));

        } catch (Exception e) {
            System.out.println("Erro ao inserir usuário no MongoDB (mas no MySQL já foi): " + e.getMessage());
        }

        return idUsuario;
    }

    private static void atualizarUsuario(Scanner sc) {
        boolean atualizado = false;
        do {
            String emailAtual = ""; // Para log e para localizar no Mongo
            int idUsuario = 0;      // ID MySQL

            try (Connection con = Conexao.getConnection()) { // Conexão MySQL

                boolean idValido = false;
                do {
                    System.out.println("Digite o número do ID do usuário: ");
                    try {
                        idUsuario = Integer.parseInt(sc.nextLine());
                        if (idUsuario > 0) idValido = true;
                        else System.out.println("ID deve ser positivo.");
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida. Digite apenas números.");
                    }
                } while (!idValido);

                String sql = "SELECT u.*, gu.nome AS tipo, gu.id AS grupoId " +
                        "FROM usuarioGrupo ug " +
                        "LEFT JOIN usuarios u ON u.id = ug.usuario_id " +
                        "LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id " +
                        "WHERE u.id = ? AND u.situacao = 'ativo'";

                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        System.out.println("Usuário encontrado:\n=================================");
                        System.out.println("Grupo: " + rs.getString("tipo"));
                        emailAtual = rs.getString("email"); // Guarda o email para o log e para localizar no Mongo
                        System.out.println("Email atual: " + emailAtual);
                        System.out.println("Situação atual: " + rs.getString("situacao"));
                        System.out.println("=================================");
                        int grupoId = rs.getInt("grupoId");

                        System.out.println("Deixe em branco para não alterar o campo.");
                        System.out.println("Deseja alterar o tipo de usuário? (1.Adm | 2.Cliente | 3.Farmacia | Enter para manter)");
                        String tipoInput = sc.nextLine();
                        boolean alterarGrupo = false;
                        if (!tipoInput.isEmpty()) {
                            try {
                                int op = Integer.parseInt(tipoInput);
                                if (op == 1) { grupoId = 1; alterarGrupo = true; }
                                else if (op == 2) { grupoId = 2; alterarGrupo = true; }
                                else if (op == 3) { grupoId = 3; alterarGrupo = true; }
                                else System.out.println("Opção inválida, tipo não alterado.");
                            } catch (NumberFormatException e) {
                                System.out.println("Opção inválida, tipo não alterado.");
                            }
                        }

                        if (alterarGrupo) {
                            String sqlGrupo = "UPDATE usuarioGrupo SET grupo_id = ? WHERE usuario_id = ?";
                            try (PreparedStatement stmt2 = con.prepareStatement(sqlGrupo)) {
                                stmt2.setInt(1, grupoId);
                                stmt2.setInt(2, idUsuario);
                                stmt2.executeUpdate();
                            }
                        }

                        System.out.println("Novo email (ou Enter para manter):");
                        String novoEmail = sc.nextLine();
                        if (!novoEmail.isEmpty() && !validarEmail(novoEmail)) {
                            System.out.println("Email inválido. Tente novamente.");
                            continue;
                        }

                        System.out.println("Nova senha (ou Enter para manter):");
                        String novaSenha = sc.nextLine();
                        String confSenha = "";
                        boolean senhaMudou = false; // Flag para o log / Mongo

                        if (!novaSenha.isEmpty()) {
                            System.out.println("Confirme a senha:");
                            confSenha = sc.nextLine();
                            if (!novaSenha.equals(confSenha)) {
                                System.out.println("Senhas não coincidem. Tente novamente.");
                                continue;
                            }
                            senhaMudou = true;
                        }

                        List<String> campos = new ArrayList<>();
                        List<Object> valores = new ArrayList<>();

                        if (!novoEmail.isEmpty()) {
                            campos.add("email = ?");
                            valores.add(novoEmail);
                        }
                        if (!novaSenha.isEmpty()) {
                            // A Trigger só funciona no INSERT, não no UPDATE
                            // Então o UPDATE precisa criptografar
                            campos.add("senha = UPPER(MD5(?))");
                            valores.add(novaSenha);
                        }

                        if (!campos.isEmpty()) {
                            String sql2 = "UPDATE usuarios SET " + String.join(", ", campos) + " WHERE id = ?";
                            try (PreparedStatement stmt2 = con.prepareStatement(sql2)) {
                                for (int i = 0; i < valores.size(); i++) {
                                    stmt2.setObject(i + 1, valores.get(i));
                                }
                                stmt2.setInt(valores.size() + 1, idUsuario);
                                stmt2.executeUpdate();
                            }
                        }

                        if (!alterarGrupo && campos.isEmpty() && !senhaMudou) {
                            System.out.println("Nenhum campo alterado.");
                        } else {
                            System.out.println("Usuário atualizado com sucesso no MySQL!");
                        }

                        // ===========================
                        // SINCRONIZAR COM MONGODB
                        // ===========================
                        sincronizarUsuarioMongo(emailAtual, novoEmail, novaSenha, grupoId, senhaMudou);

                        // ======================================================
                        // JÁ TINHA: log no Mongo se senha mudou
                        // ======================================================
                        if (senhaMudou) {
                            System.out.println("Atualizando senha no MySQL... OK.");
                            System.out.println("Enviando log para o MongoDB...");
                            logarAlteracaoSenhaMongo(idUsuario, emailAtual);
                        }
                        // ======================================================

                        atualizado = true;
                    } else {
                        System.out.println("ID não encontrado. Tente novamente.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro (MySQL) ao atualizar usuário: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números para o tipo.");
            }
        } while (!atualizado);
    }

    private static void sincronizarUsuarioMongo(String emailAtual, String novoEmail, String novaSenha, int novoGrupoId, boolean senhaMudou) {
        try {
            MongoDatabase db = Database.ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");
            MongoCollection<Document> colGrupos   = db.getCollection("gruposUsuarios");

            // Documento de filtro (localizar pelo email antigo)
            Document filtro = new Document("email", emailAtual);

            // Monta os campos que serão atualizados
            Document setFields = new Document();

            // Se o email mudou e não está vazio
            if (novoEmail != null && !novoEmail.isEmpty()) {
                setFields.append("email", novoEmail);
            }

            // Se a senha mudou
            if (senhaMudou && novaSenha != null && !novaSenha.isEmpty()) {
                String senhaCriptografada = md5Upper(novaSenha);
                setFields.append("senha", senhaCriptografada);
            }

            // Atualizar grupo também no Mongo (grupo_id), se quiser manter 100% igual
            // Busca o grupo no Mongo pela coluna "id" (1,2,3)
            Document grupoDoc = colGrupos.find(eq("id", novoGrupoId)).first();
            if (grupoDoc != null) {
                ObjectId grupoObjectId = grupoDoc.getObjectId("_id");
                setFields.append("grupo_id", grupoObjectId);
            }

            if (setFields.isEmpty()) {
                System.out.println("Nada para atualizar no MongoDB.");
                return;
            }

            Document update = new Document("$set", setFields);

            var result = colUsuarios.updateOne(filtro, update);

            if (result.getMatchedCount() == 0) {
                System.out.println("Aviso: usuário não encontrado no MongoDB para o email: " + emailAtual);
            } else {
                System.out.println("Usuário atualizado com sucesso no MongoDB!");
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar usuário no MongoDB: " + e.getMessage());
        }
    }

    private static void logarAlteracaoSenhaMongo(int usuarioId, String email) {
        try {
            // 1. Conecta ao MongoDB
            MongoDatabase db = ConexaoMongo.getDatabase("FarmaShop");

            // 2. Cria um novo "Documento" (equivalente ao JSON)
            Document logDoc = new Document();
            logDoc.append("usuarioId_sql", usuarioId); // Guarda a referência do ID do MySQL
            logDoc.append("email", email);
            logDoc.append("dataAlteracao", new java.util.Date()); // Data atual
            logDoc.append("ipOrigem", "App_Java_CLI"); // Fonte do log
            logDoc.append("motivo", "Atualização via app Java");

            // 3. Insere o documento na coleção "logAlteracoesSenha"
            db.getCollection("logAlteracoesSenha").insertOne(logDoc);

            System.out.println("Log de segurança salvo no MongoDB com sucesso.");

        } catch (Exception e) {
            // Se o MongoDB falhar, não quebra o app, apenas avisa.
            System.out.println("AVISO: Erro ao salvar log no MongoDB: " + e.getMessage());
        }
    }

    private static void deletarUsuario(Scanner sc) { // na prática: desativarUsuario
        boolean desativado = false;
        boolean cancelado = false;

        do {
            String emailUsuario = null; // vamos buscar pelo ID no MySQL e pegar o email

            try (Connection con = Conexao.getConnection()) {

                int id = 0;
                boolean idValido = false;
                do {
                    System.out.println("Digite o número do ID do usuário a DESATIVAR: ");
                    try {
                        id = Integer.parseInt(sc.nextLine());
                        if (id > 0) idValido = true;
                        else System.out.println("ID deve ser positivo.");
                    } catch (NumberFormatException e) {
                        System.out.println("Entrada inválida.");
                    }
                } while (!idValido);


                String sql = "SELECT * FROM usuarios WHERE id = ?";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()){
                        emailUsuario = rs.getString("email"); // pegar email para sincronizar com o Mongo

                        String op;
                        do{
                            System.out.println("Tem certeza que deseja desativar o usuário ID: "+id+" ("+emailUsuario+") ? (S / N)");
                            op = sc.nextLine().toLowerCase();
                            switch (op) {
                                case "s":
                                    // 1) Atualiza no MySQL
                                    String sql2 = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
                                    try (PreparedStatement stmt2 = con.prepareStatement(sql2)) {
                                        stmt2.setInt(1, id);
                                        stmt2.executeUpdate();
                                    }
                                    System.out.println("Usuário desativado no MySQL com sucesso!");

                                    // 2) Atualiza no MongoDB (situacao = 'inativo' pelo email)
                                    desativarUsuarioMongoPorEmail(emailUsuario);

                                    desativado = true;
                                    break;

                                case "n":
                                    System.out.println("Operação cancelada!");
                                    cancelado = true;
                                    break;

                                default:
                                    System.out.println("Digite uma opção válida (S ou N)!");
                            }
                        } while (!op.equals("s") && !op.equals("n"));
                    } else {
                        System.out.println("ID não encontrado.");
                        cancelado = true; // Força a saída do loop
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro ao desativar usuário (MySQL): " + e.getMessage());
            }
        } while(!desativado && !cancelado);
    }

    private static void desativarUsuarioMongoPorEmail(String emailUsuario) {
        try {
            MongoDatabase db = Database.ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");

            Document filtro = new Document("email", emailUsuario);
            Document atualizacao = new Document("$set", new Document("situacao", "inativo"));

            var result = colUsuarios.updateOne(filtro, atualizacao);

            if (result.getMatchedCount() == 0) {
                System.out.println("Aviso: usuário não encontrado no MongoDB para o email: " + emailUsuario);
            } else {
                System.out.println("Usuário desativado no MongoDB com sucesso!");
            }
        } catch (Exception e) {
            System.out.println("Erro ao desativar usuário no MongoDB: " + e.getMessage());
        }
    }
}