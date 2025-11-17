package org.example.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.DTO.LoginRequest;
import org.example.DTO.LoginResponse;
import org.springframework.stereotype.Service;

import Database.Conexao;

@Service
public class LoginService {

    public LoginResponse autenticar(LoginRequest request) {
        String sql = "SELECT * FROM vw_usuarios WHERE email = ? AND senha = UPPER(MD5(?)) AND situacao = 'ativo'";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, request.getEmail());
            stmt.setString(2, request.getSenha());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String grupo = rs.getString("grupo");
                int id = rs.getInt("id");
                return new LoginResponse(true, "Login realizado com sucesso.", grupo, id);
            } else {
                return new LoginResponse(false, "Usuário ou senha incorretos.", null, null);
            }

        } catch (SQLException e) {
            return new LoginResponse(false, "Erro ao verificar login: " + e.getMessage(), null, null);
        }
    }
}