package org.example;

import Controller.Cliente;
import Controller.Endereco;
import Controller.Farmacia;
import Controller.Usuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import org.bson.Document;

import java.security.MessageDigest;
import java.util.Scanner;

public class Login {
    private String grupo;
    private String email;
    private int id;

    public String getGrupo() {
        return grupo;
    }

    public String getUsuario() {
        return email;
    }

    public int getId() {
        return id;
    }

    public boolean logar() {
        String idEndereco;
        int opcao, idUsuario, idCliente, idFarmacia;
        boolean logado = false, valido = false;
        Scanner sc = new Scanner(System.in);
        Usuario u = new Usuario();
        Endereco e = new Endereco();
        Farmacia f = new Farmacia();
        Cliente c = new Cliente();

        do {
            System.out.println("\n\n\n=== LOGIN ===");
            System.out.println("Digite:\n 1.Realizar Login\n 2.Criar uma conta Cliente\n 3.Criar uma conta Farmácia");
            opcao = sc.nextInt();
            sc.nextLine();
            switch (opcao) {
                case 1:
                    logado = realizarLoginMongo(sc);
                    break;
                case 2:
                    idUsuario = u.inserirUsuario(sc, 2); // grupo 2 = cliente
                    idEndereco = e.inserirEndereco(sc);
                    idCliente = c.inserirCliente(sc, idUsuario, idEndereco);
                    break;
                case 3:
                    idUsuario = u.inserirUsuario(sc, 3);
                    idEndereco = e.inserirEndereco(sc);
                    idFarmacia = f.inserirFarmacia(sc, idUsuario, idEndereco);
                    break;

            }
        } while (!logado);
        return logado;
    }

/*    public boolean realizarLogin(Scanner sc) {
        String usuario;
        String senha;
        boolean logado = false;
        do {

            System.out.print("Usuário: (Aperte Enter para voltar)\n");
            usuario = sc.nextLine().trim();
            if (usuario.isEmpty() || usuario.equals("")) {
                return false;
            }
            System.out.print("Senha: (Aperte Enter para voltar)\n");
            senha = sc.nextLine().trim();
            if (senha.isEmpty() || senha.equals("")) {
                return false;
            }
            String sql = "SELECT * FROM vw_usuarios WHERE email = ? AND senha = UPPER(MD5(?)) AND situacao = 'ativo'";

            try (Connection con = Conexao.getConnection();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setString(1, usuario);
                stmt.setString(2, senha);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    grupo = rs.getString("grupo");
                    email = rs.getString("email");
                    id = rs.getInt("id");
                    logado = true;
                } else {
                    System.out.println("Usuário ou senha incorretos, digite novamente.");
                }

            } catch (SQLException e) {
                System.out.println("Erro ao verificar login: " + e.getMessage());
            }
        } while (!logado);
        return logado;
    }*/

    public boolean realizarLoginMongo(Scanner sc) {
        boolean logado = false;
        MongoDatabase db = Database.ConexaoMongo.getDatabase("FarmaShop");
        MongoCollection<Document> usuarios = db.getCollection("usuarios");
        MongoCollection<Document> grupos = db.getCollection("gruposUsuarios");

        do {
            System.out.print("Usuário: ");
            String usuario = sc.nextLine().trim();
            if (usuario.isEmpty()) return false;

            System.out.print("Senha: ");
            String senha = sc.nextLine().trim();
            if (senha.isEmpty()) return false;

            // Criptografa senha igual ao MySQL: UPPER(MD5(senha))
            String senhaCriptografada = md5Upper(senha);

            // Faz a busca no MongoDB
            Document user = usuarios.find(
                    and(
                            eq("email", usuario),
                            eq("senha", senhaCriptografada),
                            eq("situacao", "ativo")
                    )
            ).first();

            if (user != null) {
                // Busca o grupo do usuário (join manual)
                Document grupoDoc = grupos.find(eq("_id", user.getObjectId("grupo_id"))).first();

                this.email = user.getString("email");
                this.id = user.getObjectId("_id").hashCode(); // só pra ter um id inteiro, se precisar
                this.grupo = grupoDoc != null ? grupoDoc.getString("nome") : "desconhecido";

                System.out.println("Login realizado com sucesso! Grupo: " + this.grupo);
                logado = true;
            } else {
                System.out.println("Usuário ou senha incorretos, tente novamente.");
            }

        } while (!logado);

        return logado;
    }

    // Função auxiliar para gerar MD5 maiúsculo
    public static String md5Upper(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02X", b)); // maiúsculo
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
