package App.Model.Dominio;
public Class Endereco{
    private String cep;
    private String estado;
    private String cidade;
    private String rua;
    private int numero;
    private String bairro;
    private String complemento;

    // ENCAPSULAMENTO
    public String getCep(){
        return cep;
    }
    public void setCep(String cep){
        this.cep = cep;
    }
    public String getEstado(){
        return estado;
    }
    public void setEstado(String estado){
        this.estado = estado;
    }
    public String getCidade(){
        return cidade;
    }
    public void setCidade(String cidade){
        this.cidade = cidade;
    }
    public String getRua(){
        return rua;
    }
    public void setRua(String rua){
        this.rua = rua;
    }
    public int getNumero(){
        return numero;
    }
    public void setNumero(int numero){
        this.numero = numero;
    }
    public String getBairro(){
        return bairro;
    }
    public void setBairro(String bairro){
        this.bairro = bairro;
    }
    public String getComplemento(){
        return complemento;
    }
    public void setComplemento(String complemento){
        this.complemento = complemento;
    }
}