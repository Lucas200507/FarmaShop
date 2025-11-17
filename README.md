# Como rodar o programa:
- Rodar o schema do banco de dados no mysql
## Conectar ao Banco: 
- No intelliJ, vá em File > Project Structure > Modules > Add '+' > Jar or Diretories > Selecione o arquivo  mysql-connector-j-9.5.0, a qual estará em FarmaShop > Java > lib.<br>
- Em Database Conexao, altere as variáveis, url, user e pss:<br>
  EX: <br>
 private static final String url  = "jdbc:mysql://HOST:PORT/FarmaShop";<br>
    private static final String user = "app_web";<br>
    private static final String pss  = "SenhaSeguraParaAplicacao789";
# Fluxo do programa:
O programa se inicia com a tela de login, onde temos a opção de sair do programa, cadastrar farmácia, cliente ou realizar login:
<br>
## LOGIN
Digite:<br>
 1.Realizar Login<br>
 2.Criar uma conta Cliente<br>
 3.Criar uma conta Farmácia<br>
 4.Sair<br>

UTLIZE ESSES USUÁRIOS PARA TESTE:<br>  usuário: adm@ | senha: 321<br>
Crie outros tipos de usuários no login.

## CLIENTE:
Escolha uma das opções: <br>
1. Atualizar dados Pessoais<br>
2. Atualizar meu Endereço<br>
3. Ver Produtos (e Carrinho/Favoritos)<br>
4. Gerenciar Formas de Pagamento<br>
5. Sair (Voltar à tela inicial)<br>
   
## FARMÁCIA:
Escolha uma das opções: <br>
1. Gerenciar Meus Produtos<br>
2. Atualizar dados da Farmácia<br>
3. Atualizar Endereço da Farmácia<br>
4. Sair (Voltar à tela inicial)<br>

ADM: 
Como acessar o ADM:
(Email (Usuário): adm@
Senha: 321)
-
MENU ADMINISTRADOR:
Escolha uma das opções: 
1. Gerenciar Usuários
2. Gerenciar Clientes
3. Gerenciar Farmácias
4. Gerenciar Endereços
5. Gerenciar Produtos (Visão ADM)
6. Sair (Voltar à tela inicial)

[README.pdf](https://github.com/user-attachments/files/22583121/README.pdf)
