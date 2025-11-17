package org.example.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.DTO.ClienteDTO;
import org.springframework.stereotype.Service;

import Database.Conexao;

@Service
public class ClienteService {

    private boolean cpfOuTelefoneExiste(String valor, String campo) {
        String sql = "SELECT " + campo + " FROM clientes WHERE " + campo + " = ?";
        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, valor);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Se encontrar, retorna true (já existe)
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClienteDTO> listarClientes() {
        List<ClienteDTO> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClienteDTO dto = new ClienteDTO();
                dto.setId(rs.getInt("id"));
                dto.setNome(rs.getString("nome"));
                dto.setCpf(rs.getString("cpf"));
                dto.setTelefone(rs.getString("telefone"));
                dto.setDataNascimento(rs.getDate("data_nascimento") != null ? rs.getDate("data_nascimento").toLocalDate() : null);
                dto.setUsuarioId(rs.getInt("usuario_id"));
                dto.setEnderecoId(rs.getString("endereco_id"));
                clientes.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    public Integer inserirCliente(ClienteDTO dto) {
        // Validações
        if (cpfOuTelefoneExiste(dto.getCpf(), "cpf")) {
            System.out.println("CPF já cadastrado: " + dto.getCpf());
            return 0;
        }
        if (cpfOuTelefoneExiste(dto.getTelefone(), "telefone")) {
            System.out.println("Telefone já cadastrado: " + dto.getTelefone());
            return 0;
        }

        String sql = """
            INSERT INTO clientes (nome, cpf, telefone, data_nascimento, usuario_id, endereco_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, dto.getNome());
            stmt.setString(2, dto.getCpf());
            stmt.setString(3, dto.getTelefone());
            stmt.setDate(4, dto.getDataNascimento() != null ? Date.valueOf(dto.getDataNascimento()) : null);
            stmt.setInt(5, dto.getUsuarioId() != null ? dto.getUsuarioId() : 0);
            stmt.setString(6, dto.getEnderecoId() != null ? dto.getEnderecoId() : "0");

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Retorna o ID do cliente inserido
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void atualizarCliente(Integer id, ClienteDTO dto) {
        StringBuilder sql = new StringBuilder("UPDATE clientes SET ");
        boolean primeiro = true;

        if (dto.getNome() != null && !dto.getNome().isEmpty()) {
            sql.append("nome = ?");
            primeiro = false;
        }
        if (dto.getCpf() != null && !dto.getCpf().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("cpf = ?");
            primeiro = false;
        }
        if (dto.getTelefone() != null && !dto.getTelefone().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("telefone = ?");
            primeiro = false;
        }
        if (dto.getDataNascimento() != null) {
            if (!primeiro) sql.append(", ");
            sql.append("data_nascimento = ?");
        }

        sql.append(" WHERE id = ?");

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (dto.getNome() != null && !dto.getNome().isEmpty()) {
                stmt.setString(index++, dto.getNome());
            }
            if (dto.getCpf() != null && !dto.getCpf().isEmpty()) {
                stmt.setString(index++, dto.getCpf());
            }
            if (dto.getTelefone() != null && !dto.getTelefone().isEmpty()) {
                stmt.setString(index++, dto.getTelefone());
            }
            if (dto.getDataNascimento() != null) {
                stmt.setDate(index++, Date.valueOf(dto.getDataNascimento()));
            }
            stmt.setInt(index, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletarCliente(Integer id) {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}