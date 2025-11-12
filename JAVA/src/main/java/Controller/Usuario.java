package Controller;

// Imports do MySQL (SQL)
import Database.Conexao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// --- Imports do MongoDB (NoSQL) ---
import Database.ConexaoMongo;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
// ------------------------------------

public class Usuario {

    // (O método validarEmail(...) continua igual)
    private static boolean validarEmail(String email){
        // ... (código existente) ...
        if (email == null) return  false;
        email = email.trim();
        return !email.isEmpty() && email.contains("@") && email.indexOf('@') != 0 && email.indexOf('@') != email.length() - 1;
    }

    // (O método exibirUsuarios(...) continua igual)
    public static void exibirUsuarios(String tipo) {
        // ... (código existente) ...
    }

    // (O método inserirUsuario(...) continua igual)
    public int inserirUsuario(Scanner sc, int idGrupo) {
        // ... (código existente) ...
        return 0; // (retorno de exemplo)
    }

    // --- MÉTODO ATUALIZADO ---
    // (Este método agora usa OS DOIS bancos de dados)
    private static void atualizarUsuario(Scanner sc) {
        boolean atualizado = false;
        do {
            String emailAtual = ""; // Para o log
            int idUsuario = 0; // Para o log

            try (Connection con = Conexao.getConnection()) { // Conexão MySQL
                System.out.println("Digite o número do ID do usuário: ");
                idUsuario = sc.nextInt();
                sc.nextLine();

                String sql = "SELECT u.*, gu.nome AS tipo, gu.id AS grupoId FROM usuarioGrupo ug LEFT JOIN usuarios u ON u.id = ug.usuario_id LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id WHERE u.id = ? AND u.situacao = 'ativo'";
                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    stmt.setInt(1, idUsuario);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        System.out.println("Usuário encontrado:\n=================================");
                        System.out.println("Grupo: " + rs.getString("tipo"));
                        emailAtual = rs.getString("email"); // Guarda o email para o log
                        System.out.println("Email atual: " + emailAtual);
                        // ... (resto do código de busca) ...

                        // ... (lógica de alterar grupo) ...

                        // ... (lógica de alterar email) ...

                        System.out.println("Nova senha (ou Enter para manter):");
                        String senha = sc.nextLine();
                        String confSenha = "";
                        boolean senhaMudou = false; // Flag para o log

                        if (!senha.isEmpty()) {
                            // ... (lógica de confirmar senha) ...
                            senhaMudou = true;
                        }

                        // ... (lógica de montar query dinâmica) ...
                        List<String> campos = new ArrayList<>();
                        List<Object> valores = new ArrayList<>();
                        // ... (if email) ...
                        if (!senha.isEmpty()) {
                            campos.add("senha = UPPER(MD5(?))");
                            valores.add(senha);
                        }

                        if (!campos.isEmpty()) {
                            String sql2 = "UPDATE usuarios SET " + String.join(", ", campos) + " WHERE id = ?";
                            try (PreparedStatement stmt2 = con.prepareStatement(sql2)) {
                                // ... (setar valores) ...
                                stmt2.executeUpdate();
                            }
                        }

                        // ... (lógica de 'nenhum campo alterado') ...

                        // ======================================================
                        // INTEGRAÇÃO: Se a senha mudou, loga no MongoDB
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

    // --- NOVO MÉTODO (MONGODB) ---
    /**
     * Insere um documento de log na coleção 'logAlteracoesSenha' no MongoDB.
     * @param usuarioId O ID do usuário (do banco MySQL)
     * @param email O email do usuário
     */
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

    // (O método deletarUsuario(...) continua igual)
    private static void deletarUsuario(Scanner sc) {
        // ... (código existente) ...
    }
}