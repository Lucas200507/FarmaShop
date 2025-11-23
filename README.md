# Como rodar o programa:
- Na sua IDE de mysql, rode o script presente em DATABASE.sql e execute o banco com suas tabelas, triggers, funções e views. <br>
- Baixar os arquivos do main JAVA, favor, ignorar os outros arquivos fora deste diretório<br>

## Conectar ao Banco: 
- No intelliJ, vá em File > Project Structure > Modules > Add '+' > Jar or Diretories > Selecione o arquivo  mysql-connector-j-9.5.0, a qual estará em FarmaShop > Java > lib.<br>
- Em Database Conexao, altere as variáveis, url, user e pss:<br>
  EX: <br>
  private static final String url = "jdbc:mysql://HOST:PORT/FarmaShop";<br>
    private static final String user = "USER";<br>
    private static final String pss = "SENHA";<br><br>
# Fluxo do programa:
O programa se inicia com a tela de login, onde temos a opção de sair do programa, cadastrar farmácia, cliente ou realizar login:
<br>
## LOGIN
Digite:<br>
 1.Realizar Login<br>
 2.Criar uma conta Cliente<br>
 3.Criar uma conta Farmácia<br>
 4.Sair<br>

UTLIZE ESSES USUÁRIOS PARA TESTE:<br>  usuário: cliente@gmail | senha: 123<br>  usuário: farmacia@gmail | senha: 123<br>
Crie outros tipos de usuários no login.

OBS: Não foi criado uma classe para favoritos, contudo em Produtos, possui esta funcionalidade.
