package Database;

import com.mongodb.client.MongoDatabase;

public class TesteConexao {
    public static void main(String[] args) {
        try {
            MongoDatabase db = ConexaoMongo.getDatabase("FarmaShop");
            System.out.println("Conexão bem-sucedida com o banco: " + db.getName());
        } catch (Exception e) {
            System.out.println("Erro ao conectar ao MongoDB:");
            e.printStackTrace();
        }
    }
}
