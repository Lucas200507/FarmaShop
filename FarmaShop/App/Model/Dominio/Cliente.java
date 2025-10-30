package App.Model.Dominio;
public class Cliente{
    private String nome;
    private String cpf;
    private String telefone;
    private Date data_nascimento;
    private Endereco endereco;
    private Usuario usuario;

    // ENCAPSULAMENTO
    public String getNome(){
        return nome;
    }
    public void setNome(String nome){
        this.nome = nome;
    }
    public String getCpf(){
        return cpf;
    }
    public void setCpf(String cpf){
        this.cpf = cpf;
    }
    public String getTelefone(){
        return telefone;
    }
    public void setTelefone(String telefone){
        this.telefone = telefone;
    }
    public Date getData_nascimento(){
        return data_nascimento;
    }
    public void setData_nascimento(Date data_nascimento){
        this.data_nascimento = data_nascimento;
    }
    public Endereco getEndereco(){
        return endereco;
    }
    public void setEndereco(Endereco endereco){
        this.endereco = endereco;
    }
    public Usuario getUsuario(){
        return usuario;
    }
    public void setUsuario(Usuario usuario){
        this.usuario = usuario;
    }
}