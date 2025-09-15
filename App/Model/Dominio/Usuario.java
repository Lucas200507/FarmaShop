package App.Model.Dominio;
public class Usuario{
    private String tipo;
    private String situacao;
    private String email;
    private String senha;

    // ENCAPSULAMENTO
    public String getTipo(){
        return tipo;
    }

    public void setTipo(String tipo){
        this.tipo = tipo;
    }

    public String getSituacao(){
        return situacao;
    }

    public void setSituacao(String situacao){
        this.situacao = situacao;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getSenha(){
        return senha;
    }

    public void setSenha(String senha){
        this.senha = senha;
    }

}