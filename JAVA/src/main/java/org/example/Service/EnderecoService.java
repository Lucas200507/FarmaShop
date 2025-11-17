package org.example.Service;

import org.example.DTO.EnderecoDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Database.Conexao;

@Service
public class EnderecoService {

    public List<EnderecoDTO> listarEnderecos() {
        List<EnderecoDTO> enderecos = new ArrayList<>();
        String sql = "SELECT * FROM enderecos";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EnderecoDTO dto = new EnderecoDTO();
                dto.setId(rs.getString("id"));
                dto.setCep(rs.getString("cep"));
                dto.setEstado(rs.getString("estado"));
                dto.setCidade(rs.getString("cidade"));
                dto.setRua(rs.getString("rua"));
                dto.setNumero(rs.getInt("numero"));
                dto.setBairro(rs.getString("bairro"));
                dto.setComplemento(rs.getString("complemento"));
                enderecos.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enderecos;
    }

    public String inserirEndereco(EnderecoDTO dto) {
        String idEndereco = "0";
        try (Connection con = Conexao.getConnection()) {
            // Formata o CEP com hífen
            String cepFormatado = dto.getCep().substring(0, 5) + "-" + dto.getCep().substring(5);

            String sql = """
                INSERT INTO enderecos (cep, estado, cidade, rua, numero, bairro, complemento)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, cepFormatado);
                stmt.setString(2, dto.getEstado());
                stmt.setString(3, dto.getCidade());
                stmt.setString(4, dto.getRua());
                stmt.setInt(5, dto.getNumero() != null ? dto.getNumero() : 0);
                stmt.setString(6, dto.getBairro());
                stmt.setString(7, dto.getComplemento());
                stmt.executeUpdate();
            }

            // Recupera o ID do endereço inserido
            String sql2 = "SELECT id FROM enderecos WHERE cep = ?";
            try (PreparedStatement stmt2 = con.prepareStatement(sql2)) {
                stmt2.setString(1, cepFormatado);
                try (ResultSet rs = stmt2.executeQuery()) {
                    if (rs.next()) {
                        idEndereco = rs.getString("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idEndereco;
    }

    public void atualizarEndereco(String id, EnderecoDTO dto) {
        StringBuilder sql = new StringBuilder("UPDATE enderecos SET ");
        boolean primeiro = true;

        if (dto.getCep() != null && !dto.getCep().isEmpty()) {
            sql.append("cep = ?");
            primeiro = false;
        }
        if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("estado = ?");
            primeiro = false;
        }
        if (dto.getCidade() != null && !dto.getCidade().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("cidade = ?");
            primeiro = false;
        }
        if (dto.getRua() != null && !dto.getRua().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("rua = ?");
            primeiro = false;
        }
        if (dto.getNumero() != null) {
            if (!primeiro) sql.append(", ");
            sql.append("numero = ?");
            primeiro = false;
        }
        if (dto.getBairro() != null && !dto.getBairro().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("bairro = ?");
            primeiro = false;
        }
        if (dto.getComplemento() != null && !dto.getComplemento().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("complemento = ?");
        }

        sql.append(" WHERE id = ?");

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (dto.getCep() != null && !dto.getCep().isEmpty()) {
                stmt.setString(index++, dto.getCep());
            }
            if (dto.getEstado() != null && !dto.getEstado().isEmpty()) {
                stmt.setString(index++, dto.getEstado());
            }
            if (dto.getCidade() != null && !dto.getCidade().isEmpty()) {
                stmt.setString(index++, dto.getCidade());
            }
            if (dto.getRua() != null && !dto.getRua().isEmpty()) {
                stmt.setString(index++, dto.getRua());
            }
            if (dto.getNumero() != null) {
                stmt.setInt(index++, dto.getNumero());
            }
            if (dto.getBairro() != null && !dto.getBairro().isEmpty()) {
                stmt.setString(index++, dto.getBairro());
            }
            if (dto.getComplemento() != null && !dto.getComplemento().isEmpty()) {
                stmt.setString(index++, dto.getComplemento());
            }
            stmt.setString(index, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletarEndereco(String id) {
        String sql = "DELETE FROM enderecos WHERE id = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}