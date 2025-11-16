package DTO;

public class LoginResponse {
    private boolean sucesso;
    private String mensagem;
    private String grupo;
    private Integer id;

    public LoginResponse(boolean sucesso, String mensagem, String grupo, Integer id) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.grupo = grupo;
        this.id = id;
    }

    // getters e setters
    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}