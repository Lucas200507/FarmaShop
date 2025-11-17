package org.example;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FarmaShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(FarmaShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner logDataSource(DataSource dataSource) {
        return args -> {
            System.out.println("✅ Conectado ao banco: " + dataSource.getConnection().getCatalog());
        };
    }
}
