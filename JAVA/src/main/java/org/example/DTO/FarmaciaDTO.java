package org.example.DTO;

public class FarmaciaDTO {
    private Integer id;
    private String nomeJuridico;
    private String nomeFantasia;
    private String cnpj;
    private String alvaraSanitario;
    private String responsavelTecnico;
    private String crf;
    private String telefone;
    private String enderecoId;
    private Integer usuarioId;

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeJuridico() { return nomeJuridico; }
    public void setNomeJuridico(String nomeJuridico) { this.nomeJuridico = nomeJuridico; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getAlvaraSanitario() { return alvaraSanitario; }
    public void setAlvaraSanitario(String alvaraSanitario) { this.alvaraSanitario = alvaraSanitario; }

    public String getResponsavelTecnico() { return responsavelTecnico; }
    public void setResponsavelTecnico(String responsavelTecnico) { this.responsavelTecnico = responsavelTecnico; }

    public String getCrf() { return crf; }
    public void setCrf(String crf) { this.crf = crf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEnderecoId() { return enderecoId; }
    public void setEnderecoId(String enderecoId) { this.enderecoId = enderecoId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
}