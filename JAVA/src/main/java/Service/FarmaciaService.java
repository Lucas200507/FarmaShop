package Service;

import DTO.FarmaciaDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Database.Conexao;

@Service
public class FarmaciaService {

    private boolean validarApenasLetras(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        return valor.trim().matches("^[a-zA-ZÀ-ú\\s]+$");
    }

    private boolean validarNumero(String valor, int tamMin, int tamMax) {
        if (valor == null) return false;
        String valorLimpo = valor.replaceAll("[^0-9]", "");
        return valorLimpo.length() >= tamMin && valorLimpo.length() <= tamMax;
    }

    public List<FarmaciaDTO> listarFarmacias() {
        List<FarmaciaDTO> farmacias = new ArrayList<>();
        String sql = "SELECT * FROM vw_farmacias_ativas";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FarmaciaDTO dto = new FarmaciaDTO();
                dto.setId(rs.getInt("id"));
                dto.setNomeFantasia(rs.getString("nome_fantasia"));
                dto.setCnpj(rs.getString("cnpj"));
                dto.setTelefone(rs.getString("telefone"));
                dto.setNomeJuridico(rs.getString("nome_juridico"));
                dto.setAlvaraSanitario(rs.getString("alvara_sanitario"));
                dto.setResponsavelTecnico(rs.getString("responsavel_tecnico"));
                dto.setCrf(rs.getString("crf"));
                dto.setEnderecoId(rs.getString("endereco_id"));
                dto.setUsuarioId(rs.getInt("usuario_id"));
                farmacias.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return farmacias;
    }

    public Integer inserirFarmacia(FarmaciaDTO dto) {
        int idFarmacia = 0;

        try (Connection con = Conexao.getConnection()) {
            String sql = """
                INSERT INTO farmacias (
                    nome_juridico, nome_fantasia, cnpj, alvara_sanitario, responsavel_tecnico, crf, telefone, endereco_id, usuario_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, dto.getNomeJuridico());
                stmt.setString(2, dto.getNomeFantasia());
                stmt.setString(3, dto.getCnpj());
                stmt.setString(4, dto.getAlvaraSanitario());
                stmt.setString(5, dto.getResponsavelTecnico());
                stmt.setString(6, dto.getCrf());
                stmt.setString(7, dto.getTelefone());
                stmt.setString(8, dto.getEnderecoId());
                stmt.setInt(9, dto.getUsuarioId());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idFarmacia = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idFarmacia;
    }

    public void atualizarFarmacia(Integer id, FarmaciaDTO dto) {
        StringBuilder sql = new StringBuilder("UPDATE farmacias SET ");
        boolean primeiro = true;

        if (dto.getNomeJuridico() != null && !dto.getNomeJuridico().isEmpty()) {
            sql.append("nome_juridico = ?");
            primeiro = false;
        }
        if (dto.getNomeFantasia() != null && !dto.getNomeFantasia().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("nome_fantasia = ?");
            primeiro = false;
        }
        if (dto.getCnpj() != null && !dto.getCnpj().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("cnpj = ?");
            primeiro = false;
        }
        if (dto.getAlvaraSanitario() != null && !dto.getAlvaraSanitario().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("alvara_sanitario = ?");
            primeiro = false;
        }
        if (dto.getResponsavelTecnico() != null && !dto.getResponsavelTecnico().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("responsavel_tecnico = ?");
            primeiro = false;
        }
        if (dto.getCrf() != null && !dto.getCrf().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("crf = ?");
            primeiro = false;
        }
        if (dto.getTelefone() != null && !dto.getTelefone().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("telefone = ?");
            primeiro = false;
        }
        if (dto.getEnderecoId() != null && !dto.getEnderecoId().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("endereco_id = ?");
        }

        sql.append(" WHERE id = ?");

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (dto.getNomeJuridico() != null && !dto.getNomeJuridico().isEmpty()) {
                stmt.setString(index++, dto.getNomeJuridico());
            }
            if (dto.getNomeFantasia() != null && !dto.getNomeFantasia().isEmpty()) {
                stmt.setString(index++, dto.getNomeFantasia());
            }
            if (dto.getCnpj() != null && !dto.getCnpj().isEmpty()) {
                stmt.setString(index++, dto.getCnpj());
            }
            if (dto.getAlvaraSanitario() != null && !dto.getAlvaraSanitario().isEmpty()) {
                stmt.setString(index++, dto.getAlvaraSanitario());
            }
            if (dto.getResponsavelTecnico() != null && !dto.getResponsavelTecnico().isEmpty()) {
                stmt.setString(index++, dto.getResponsavelTecnico());
            }
            if (dto.getCrf() != null && !dto.getCrf().isEmpty()) {
                stmt.setString(index++, dto.getCrf());
            }
            if (dto.getTelefone() != null && !dto.getTelefone().isEmpty()) {
                stmt.setString(index++, dto.getTelefone());
            }
            if (dto.getEnderecoId() != null && !dto.getEnderecoId().isEmpty()) {
                stmt.setString(index++, dto.getEnderecoId());
            }
            stmt.setInt(index, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void desativarFarmacia(Integer id) {
        try (Connection con = Conexao.getConnection()) {
            // Buscar o usuario_id associado à farmácia
            String sqlFind = "SELECT usuario_id FROM farmacias WHERE id = ?";
            int usuarioId = 0;

            try (PreparedStatement stmtFind = con.prepareStatement(sqlFind)) {
                stmtFind.setInt(1, id);
                try (ResultSet rs = stmtFind.executeQuery()) {
                    if (rs.next()) {
                        usuarioId = rs.getInt("usuario_id");
                    } else {
                        System.out.println("Erro: Farmácia com ID " + id + " não encontrada.");
                        return;
                    }
                }
            }

            if (usuarioId > 0) {
                String sqlUpdate = "UPDATE usuarios SET situacao = 'inativo' WHERE id = ?";
                try (PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setInt(1, usuarioId);
                    stmtUpdate.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}