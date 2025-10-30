package App.Model.Dominio;
public class Farmacia{
    private String nome_juridico;
    private String nome_fantasia;
    private String cnpj;
    private String alvara_sanitario;
    private String responsavel_tecnico;
    private String crf;
    private String telefone;
    private String email;
    private Endereco endereco;
    private Usuario usuario;

    // ENCAPSULAMENTO
    public String getNome_juridico(){
        return nome_juridico;
    }
    public void setNome_juridico(String nome_juridico){
        this.nome_juridico = nome_juridico;
    }
    public String getNome_fantasia(){
        return nome_fantasia;
    }
    public void setNome_fantasia(String nome_fantasia){
        this.nome_fantasia = nome_fantasia;
    }
    public String getCnpj(){
        return cnpj;
    }
    public void setCnpj(String cnpj){
        this.cnpj = cnpj;
    }
    public String getAlvara_sanitario(){
        return alvara_sanitario;
    }
    public void setAlvara_sanitario(String alvara_sanitario){
        this.alvara_sanitario = alvara_sanitario;
    }
    public String getResponsavel_tecnico(){
        return responsavel_tecnico;
    }
    public void setResponsavel_tecnico(String responsavel_tecnico){
        this.responsavel_tecnico = responsavel_tecnico;
    }
    public String getCrf(){
        return crf;
    }
    public void setCrf(String crf){
        this.crf = crf;
    }
    public String getTelefone(){
        return telefone;
    }
    public void setTelefone(String telefone){
        this.telefone = telefone;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
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