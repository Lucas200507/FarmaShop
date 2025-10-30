package App.Model.Dominio;
public class Produto{
    private String codigo;
    private String nome;
    private String descricao;
    private int estoque;
    private boolean promocao;
    private double preco;
    private Farmacia farmacia;

    // ENCAPSULAMENTO
    public String getCodigo(){
        return codigo;
    }
    public void setCodigo(String codigo){
        this.codigo = codigo;
    }
    public String getNome(){
        return nome;
    }
    public void setNome(String nome){
        this.nome = nome;
    }
    public String getDescricao(){
        return descricao;
    }
    public void setDescricao(String descricao){
        this.descricao = descricao;
    }
    public int getEstoque(){
        return estoque;
    }
    public void setEstoque(int estoque){
        this.estoque = estoque;
    }
    public boolean isPromocao(){
        return promocao;
    }
    public void setPromocao(boolean promocao){
        this.promocao = promocao;
    }
    public double getPreco(){
        return preco;
    }
    public void setPreco(double preco){
        this.preco = preco;
    }
    public Farmacia getFarmacia(){
        return farmacia;
    }
    public void setFarmacia(Farmacia farmacia){
        this.farmacia = farmacia;
    }    
}