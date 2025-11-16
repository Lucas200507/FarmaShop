package Service;

import DTO.UsuarioDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Database.Conexao;
import Database.ConexaoMongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.types.ObjectId;

@Service
public class UsuarioService {

    public List<UsuarioDTO> listarUsuarios(String tipo) {
        List<UsuarioDTO> usuarios = new ArrayList<>();
        String sql;

        if ("todos".equals(tipo)) {
            sql = "SELECT * FROM vw_usuarios WHERE situacao = 'ativo';";
        } else {
            sql = """
                SELECT u.*, gu.nome AS tipo FROM usuarioGrupo ug
                LEFT JOIN usuarios u ON u.id = ug.usuario_id
                LEFT JOIN gruposUsuarios gu ON gu.id = ug.grupo_id
                WHERE u.situacao = 'ativo' AND gu.nome = ?;
                """;
        }

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            if (!"todos".equals(tipo)) {
                stmt.setString(1, tipo);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UsuarioDTO dto = new UsuarioDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setEmail(rs.getString("email"));
                    dto.setSituacao(rs.getString("situacao"));
                    dto.setGrupo("todos".equals(tipo) ? rs.getString("grupo") : rs.getString("tipo"));
                    usuarios.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    public Integer inserirUsuario(UsuarioDTO dto) {
        int idGrupo = 0;
        switch (dto.getGrupo()) {
            case "adm": idGrupo = 1; break;
            case "cliente": idGrupo = 2; break;
            case "farmacia": idGrupo = 3; break;
            default: return 0;
        }

        if (!validarEmail(dto.getEmail())) {
            System.out.println("Email já cadastrado ou inválido.");
            return 0;
        }

        try (Connection con = Conexao.getConnection()) {
            String sql = "INSERT INTO usuarios (situacao, email, senha) VALUES ('ativo', ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, dto.getEmail());
                stmt.setString(2, dto.getSenha());

                int rowsAfetadas = stmt.executeUpdate();
                if (rowsAfetadas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int idUsuario = rs.getInt(1);

                            // Relacionar grupo
                            String sqlGrupo = "INSERT INTO usuarioGrupo (usuario_id, grupo_id) VALUES (?, ?)";
                            try (PreparedStatement stmt3 = con.prepareStatement(sqlGrupo)) {
                                stmt3.setInt(1, idUsuario);
                                stmt3.setInt(2, idGrupo);
                                stmt3.executeUpdate();
                            }

                            // Sincronizar com MongoDB
                            sincronizarUsuarioMongo(idUsuario, dto.getEmail(), dto.getSenha(), idGrupo, "ativo");

                            return idUsuario;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean validarEmail(String email) {
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT email FROM usuarios WHERE email = ?")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void sincronizarUsuarioMongo(int idUsuario, String email, String senha, int idGrupo, String situacao) {
        try {
            MongoDatabase db = ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");
            MongoCollection<Document> colGrupos = db.getCollection("gruposUsuarios");

            Document grupoDoc = colGrupos.find(eq("id", idGrupo)).first();
            if (grupoDoc != null) {
                ObjectId grupoObjectId = grupoDoc.getObjectId("_id");

                String senhaCriptografada = md5Upper(senha);

                Document novoUsuario = new Document()
                        .append("email", email)
                        .append("senha", senhaCriptografada)
                        .append("situacao", situacao)
                        .append("grupo_id", grupoObjectId)
                        .append("id_mysql", idUsuario);

                colUsuarios.insertOne(novoUsuario);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Adicione este método estático em algum lugar do projeto, talvez em uma classe de utilidade
    private static String md5Upper(String senha) {
        // Implementação do MD5 UPPER (se você não tiver isso em outra classe)
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(senha.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar senha", e);
        }
    }

    public void atualizarUsuario(int id, UsuarioDTO dto) {
        try (Connection con = Conexao.getConnection()) {
            List<String> campos = new ArrayList<>();
            List<Object> valores = new ArrayList<>();

            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                campos.add("email = ?");
                valores.add(dto.getEmail());
            }
            if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
                campos.add("senha = UPPER(MD5(?))");
                valores.add(dto.getSenha());
            }
            if (dto.getSituacao() != null && !dto.getSituacao().isEmpty()) {
                campos.add("situacao = ?");
                valores.add(dto.getSituacao());
            }

            if (!campos.isEmpty()) {
                String sql = "UPDATE usuarios SET " + String.join(", ", campos) + " WHERE id = ?";
                valores.add(id);

                try (PreparedStatement stmt = con.prepareStatement(sql)) {
                    for (int i = 0; i < valores.size(); i++) {
                        stmt.setObject(i + 1, valores.get(i));
                    }
                    stmt.executeUpdate();
                }
            }

            // Sincronizar com MongoDB
            sincronizarUsuarioMongoPorId(id, dto.getEmail(), dto.getSenha(), dto.getSituacao());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sincronizarUsuarioMongoPorId(int id, String email, String senha, String situacao) {
        try {
            MongoDatabase db = ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");

            Document filtro = new Document("id_mysql", id);
            Document setFields = new Document();
            if (email != null) setFields.append("email", email);
            if (senha != null) setFields.append("senha", md5Upper(senha));
            if (situacao != null) setFields.append("situacao", situacao);

            if (!setFields.isEmpty()) {
                colUsuarios.updateOne(filtro, new Document("$set", setFields));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void desativarUsuario(int id) {
        try (Connection con = Conexao.getConnection()) {
            String sql = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // Sincronizar com MongoDB
            desativarUsuarioMongoPorId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void desativarUsuarioMongoPorId(int id) {
        try {
            MongoDatabase db = ConexaoMongo.getDatabase("FarmaShop");
            MongoCollection<Document> colUsuarios = db.getCollection("usuarios");

            Document filtro = new Document("id_mysql", id);
            Document update = new Document("$set", new Document("situacao", "inativo"));
            colUsuarios.updateOne(filtro, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}