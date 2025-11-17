package org.example.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.DTO.FavoritoDTO;
import org.example.DTO.ProdutoDTO;
import org.springframework.stereotype.Service;

import Database.Conexao;

@Service
public class ProdutoService {

    private boolean validarNomeProduto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        return valor.trim().matches("^[a-zA-ZÀ-ú0-9\\s.,'\\\"()%-]+$");
    }

    public List<ProdutoDTO> listarProdutos(String grupoNome, Integer perfilId) {
        List<ProdutoDTO> produtos = new ArrayList<>();
        String sql = null;

        if ("cliente".equals(grupoNome)) {
            sql = "SELECT * FROM produtos WHERE estoque > 0";
        } else if ("farmacia".equals(grupoNome)) {
            sql = "SELECT * FROM produtos WHERE farmacia_id = ?";
        } else {
            sql = "SELECT * FROM produtos";
        }

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            if ("farmacia".equals(grupoNome)) {
                stmt.setInt(1, perfilId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProdutoDTO dto = new ProdutoDTO();
                    dto.setCod(rs.getString("COD"));
                    dto.setNome(rs.getString("nome"));
                    dto.setDescricao(rs.getString("descricao"));
                    dto.setPreco(rs.getDouble("preco"));
                    dto.setEstoque(rs.getInt("estoque"));
                    dto.setCategoriaId(rs.getInt("categoria_id"));
                    dto.setFarmaciaId(rs.getInt("farmacia_id"));
                    produtos.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produtos;
    }

    public Integer inserirProduto(ProdutoDTO dto) {
        int idProduto = 0;

        try (Connection con = Conexao.getConnection()) {
            String sql = """
                INSERT INTO produtos (nome, descricao, preco, estoque, categoria_id, farmacia_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, dto.getNome());
                stmt.setString(2, dto.getDescricao());
                stmt.setDouble(3, dto.getPreco());
                stmt.setInt(4, dto.getEstoque());
                stmt.setInt(5, dto.getCategoriaId());
                stmt.setInt(6, dto.getFarmaciaId());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            idProduto = rs.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idProduto;
    }

    public void atualizarProduto(String cod, ProdutoDTO dto) {
        StringBuilder sql = new StringBuilder("UPDATE produtos SET ");
        boolean primeiro = true;

        if (dto.getNome() != null && !dto.getNome().isEmpty()) {
            sql.append("nome = ?");
            primeiro = false;
        }
        if (dto.getDescricao() != null && !dto.getDescricao().isEmpty()) {
            if (!primeiro) sql.append(", ");
            sql.append("descricao = ?");
            primeiro = false;
        }
        if (dto.getPreco() != null) {
            if (!primeiro) sql.append(", ");
            sql.append("preco = ?");
            primeiro = false;
        }
        if (dto.getEstoque() != null) {
            if (!primeiro) sql.append(", ");
            sql.append("estoque = ?");
        }

        sql.append(" WHERE COD = ?");

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (dto.getNome() != null && !dto.getNome().isEmpty()) {
                stmt.setString(index++, dto.getNome());
            }
            if (dto.getDescricao() != null && !dto.getDescricao().isEmpty()) {
                stmt.setString(index++, dto.getDescricao());
            }
            if (dto.getPreco() != null) {
                stmt.setDouble(index++, dto.getPreco());
            }
            if (dto.getEstoque() != null) {
                stmt.setInt(index++, dto.getEstoque());
            }
            stmt.setString(index, cod);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletarProduto(String cod) {
        String sql = "DELETE FROM produtos WHERE COD = ?";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, cod);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void adicionarFavorito(FavoritoDTO dto) {
        String sql = "INSERT INTO prod_favoritos (cliente_id, produto_cod) VALUES (?, ?)";

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, dto.getClienteId());
            stmt.setString(2, dto.getProdutoCod());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ProdutoDTO> listarFavoritos(Integer clienteId) {
        List<ProdutoDTO> favoritos = new ArrayList<>();
        String sql = """
            SELECT p.COD, p.nome, p.descricao, p.preco, p.estoque, p.categoria_id, p.farmacia_id
            FROM produtos p
            JOIN prod_favoritos pf ON p.COD = pf.produto_cod
            WHERE pf.cliente_id = ?
            """;

        try (Connection con = Conexao.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProdutoDTO dto = new ProdutoDTO();
                    dto.setCod(rs.getString("COD"));
                    dto.setNome(rs.getString("nome"));
                    dto.setDescricao(rs.getString("descricao"));
                    dto.setPreco(rs.getDouble("preco"));
                    dto.setEstoque(rs.getInt("estoque"));
                    dto.setCategoriaId(rs.getInt("categoria_id"));
                    dto.setFarmaciaId(rs.getInt("farmacia_id"));
                    favoritos.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return favoritos;
    }
}