package org.example.DTO;

public class ProdutoDTO {
    private String cod;
    private String nome;
    private String descricao;
    private Double preco;
    private Integer estoque;
    private Integer categoriaId;
    private Integer farmaciaId;

    // getters e setters
    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public Integer getFarmaciaId() { return farmaciaId; }
    public void setFarmaciaId(Integer farmaciaId) { this.farmaciaId = farmaciaId; }
}
