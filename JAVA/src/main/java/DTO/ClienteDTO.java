package DTO;

import java.time.LocalDate;

public class ClienteDTO {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private Integer usuarioId;
    private String enderecoId;

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getEnderecoId() { return enderecoId; }
    public void setEnderecoId(String enderecoId) { this.enderecoId = enderecoId; }
}