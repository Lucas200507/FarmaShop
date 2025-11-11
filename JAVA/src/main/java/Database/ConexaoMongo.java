package Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class ConexaoMongo {
    private static final String uri = "mongodb+srv://root:TmUutSZz7KVo3ojr@bancofarmashop.60oksqj.mongodb.net/";

    public static MongoDatabase getDatabase(String nomeBanco) {
        MongoClient mongoClient = MongoClients.create(uri);
        return mongoClient.getDatabase(nomeBanco);
    }
}