-- 1. CRIAÇÃO DO BANCO DE DADOS E FUNÇÃO DE GERAÇÃO DE ID
CREATE DATABASE FarmaShop;
USE FarmaShop;
-- FAÇA ESTA FUNÇÃO PARA GERAR CÓDIGOS DO PRODUTO COM 7 DÍGITOS
-- FUNÇÃO OBRIGATÓRIA: Regra Própria para Geração de IDs (UUID/GUID)
DELIMITER //
CREATE FUNCTION fn_gerar_id() 
RETURNS CHAR(7)
NOT DETERMINISTIC
BEGIN
    RETURN SUBSTRING(REPLACE(UUID(), '-', ''), 1, 7);
END //
DELIMITER ;


-- =================================================================
-- 2. TABELAS OBRIGATÓRIAS E ESTRUTURA BASE 
-- =================================================================


-- Tabela Grupos de Usuários 
CREATE TABLE gruposUsuarios (
id INT PRIMARY KEY,
nome VARCHAR(50) UNIQUE NOT NULL,
descricao VARCHAR(255)
);
INSERT INTO gruposUsuarios (id, nome, descricao) VALUES (2, 'cliente', 'acesso a páginas de clientes'),(3, 'farmacia' ,'acesso a todas as páginas, exceto de cartão'),(1,'adm' ,'acesso a todas as páginas');
SELECT * FROM gruposUsuarios;

-- Tabela Usuários
CREATE TABLE usuarios(
id INT PRIMARY KEY AUTO_INCREMENT,
situacao ENUM('ativo', 'inativo') DEFAULT 'ativo',
email VARCHAR(60) NOT NULL UNIQUE,
senha VARCHAR(255) NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- CRIPTOGRAFIA DA SENHA
DELIMITER //
CREATE TRIGGER senha_login
BEFORE INSERT 
ON usuarios FOR EACH ROW 
BEGIN
SET NEW.senha = UPPER(MD5(NEW.senha));
END 
//
DELIMITER ;

INSERT INTO usuarios (email,senha) VALUES ('adm@','321'), ('cliente@','123'), ('farmacia@', '123');
SELECT * FROM usuarios;
-- Tabela de Relacionamento 
CREATE TABLE usuarioGrupo (
usuario_id INT NOT NULL, 
grupo_id INT NOT NULL,   
PRIMARY KEY (usuario_Id, grupo_id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
FOREIGN KEY (grupo_id) REFERENCES gruposUsuarios (id)
);
INSERT INTO usuarioGrupo (usuario_id, grupo_id) VALUES (1, 1), (2, 2), (3, 3);
SELECT * FROM usuarioGrupo;

-- Tabela para log de auditoria de segurança 
CREATE TABLE logAlteracoesSenha (
id VARCHAR(7) PRIMARY KEY,
usuarioId INT NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ipOrigem VARCHAR(50), 
FOREIGN KEY (usuarioId) REFERENCES usuarios (id)
);
SELECT * FROM logAlteracoesSenha;

-- Tabela Endereços
CREATE TABLE enderecos(
id VARCHAR(7) PRIMARY KEY,
cep VARCHAR(9) UNIQUE NOT NULL,
estado ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO') NOT NULL,
cidade VARCHAR(60) NOT NULL,
rua VARCHAR(60) NOT NULL,
numero INT,
bairro VARCHAR(60),
complemento TEXT
);
SELECT * FROM enderecos;

INSERT INTO enderecos (cep, estado, cidade, rua, numero, bairro, complemento) VALUES
('01001-000', 'SP', 'São Paulo', 'Rua da Saúde', 100, 'Centro', 'Próximo à estação de metrô'),
('20031-050', 'RJ', 'Rio de Janeiro', 'Avenida das Farmácias', 250, 'Copacabana', 'Em frente à praça principal');

-- Tabela Clientes
CREATE TABLE clientes(
id INT PRIMARY KEY AUTO_INCREMENT,
nome VARCHAR(60) NOT NULL,
cpf VARCHAR(12) UNIQUE NOT NULL,
telefone VARCHAR(14) UNIQUE NOT NULL,
data_nascimento DATE NOT NULL, 
endereco_id VARCHAR(7),  
usuario_id INT NOT NULL,  
data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
FOREIGN KEY (endereco_id) REFERENCES enderecos (id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
SELECT * FROM clientes;

-- Tabela Farmácias
CREATE TABLE farmacias(
id INT PRIMARY KEY AUTO_INCREMENT,
nome_juridico VARCHAR(150) NOT NULL,   
nome_fantasia VARCHAR(150) NOT NULL,   
cnpj VARCHAR(14) UNIQUE NOT NULL,
alvara_sanitario VARCHAR(50) NOT NULL, 
responsavel_tecnico VARCHAR(60) NOT NULL, 
crf VARCHAR(20) NOT NULL,
telefone VARCHAR(20) UNIQUE NOT NULL,
endereco_id VARCHAR(7) NOT NULL,
usuario_id INT NOT NULL,
dataCadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (endereco_id) REFERENCES enderecos (id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
INSERT INTO farmacias (nome_juridico,nome_fantasia,cnpj,alvara_sanitario,responsavel_tecnico,crf,telefone,endereco_id, usuario_id) VALUES ('Farmácia Saúde Total LTDA','Farmácia Saúde Total','12345678000199','ALV-2025-0001','João da Silva','CRF-SP 12345','11999990000','0b870', 3);

SELECT * FROM farmacias;

-- Tabela Categoria Produtos
CREATE TABLE categoria_produtos(
id INT PRIMARY KEY,
nome VARCHAR(100) NOT NULL
);

INSERT INTO categoria_produtos(id, nome) VALUES (1, 'Cosméticos'), (2, 'Medicamento'), (3, 'Prod. Beleza'), (4, 'Prod. Higiene'), (5, 'Prod. Infantil'), (6, 'Prod. Saúde');
SELECT * FROM categoria_produtos;

-- Tabela Produtos
CREATE TABLE produtos(
COD VARCHAR(7) primary key, -- Tem que gerar o código com a trigger
nome VARCHAR(100) NOT NULL,
descricao TEXT NOT NULL,
estoque INT NOT NULL,
promocao BOOLEAN DEFAULT FALSE,
preco DECIMAL(10, 2) NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
dataCadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
categoria_id INT NOT NULL, 
farmacia_id INT NOT NULL, 
FOREIGN KEY (categoria_id) REFERENCES categoria_produtos (id),
FOREIGN KEY (farmacia_id) REFERENCES farmacias (id)
);

INSERT INTO produtos (nome, descricao, estoque, promocao, preco, categoria_id, farmacia_id) VALUES
('Dipirona Sódica 500mg', 'Analgésico e antipirético em comprimidos.', 100, FALSE, 12.90, 2, 3),
('Paracetamol 750mg', 'Analgésico e antipirético em comprimidos.', 80, TRUE, 15.50, 2, 3),
('Shampoo Anticaspa 200ml', 'Shampoo para controle de caspa.', 50, FALSE, 22.90, 1, 3),
('Sabonete Líquido Neutro 250ml', 'Sabonete líquido para uso diário.', 70, FALSE, 14.75, 4, 3),
('Protetor Solar FPS 50 120ml', 'Protetor solar para todos os tipos de pele.', 40, TRUE, 49.90, 3, 3),
('Vitamina C 500mg', 'Suplemento vitamínico em comprimidos.', 60, FALSE, 29.90, 6, 3),
('Fralda Infantil Tamanho M', 'Pacote com 30 unidades.', 35, FALSE, 39.99, 5, 3),
('Álcool 70% 500ml', 'Álcool etílico 70% para assepsia.', 90, TRUE, 9.99, 6, 3),
('Escova Dental Macia', 'Escova dental com cerdas macias.', 120, FALSE, 7.50, 4, 3),
('Creme Hidratante Corporal 200ml', 'Hidratante corporal para pele seca.', 55, FALSE, 24.90, 1, 3);

SELECT * FROM produtos;

-- Tabela de Favoritos
CREATE TABLE prod_favoritos(
cliente_id INT NOT NULL,
produto_cod VARCHAR(7) NOT NULL,
FOREIGN KEY (cliente_id) REFERENCES clientes(id),
FOREIGN KEY (produto_cod) REFERENCES produtos(COD)
);

SELECT * FROM prod_favoritos;

-- Tabela Carrinho
CREATE TABLE  carrinho (
id INT PRIMARY KEY AUTO_INCREMENT, 
cliente_id INT NOT NULL,
produto_cod CHAR(7) NOT NULL,
data_adicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE CASCADE,
FOREIGN KEY (produto_cod) REFERENCES produtos (COD) ON DELETE CASCADE
);
SELECT * FROM carrinho;

-- Trigger para Produtos
DELIMITER //
CREATE TRIGGER trg_gerar_idProdutos
BEFORE INSERT ON produtos
FOR EACH ROW
BEGIN
IF NEW.COD IS NULL OR NEW.COD = '' THEN
SET NEW.COD = fn_gerar_id();
END IF;
END //
DELIMITER ;

-- Trigger para Cartão
DELIMITER //
CREATE TRIGGER trg_gerar_idCartao
BEFORE INSERT ON cartaos
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_id();
END IF;
END //
DELIMITER ;

-- Trigger para Endereços
DELIMITER //
CREATE TRIGGER trg_gerar_idEndereco
BEFORE INSERT ON enderecos
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_id();
END IF;
END //
DELIMITER ;

-- Trigger para log de alteração de senha
DELIMITER //
CREATE TRIGGER trg_gerar_idLog
BEFORE INSERT ON logAlteracoesSenha
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_id();
END IF;
END //
DELIMITER ;

-- 4. ÍNDICES (PARA DESEMPENHO)

-- Índices ajustados para camelCase
ALTER TABLE clientes ADD INDEX idx_cliente_endereco (endereco_id);
ALTER TABLE farmacias ADD INDEX idx_farmacia_endereco (endereco_id);
ALTER TABLE usuarios ADD INDEX idx_usuario_situacao (situacao);
ALTER TABLE produtos ADD INDEX idx_produto_categoria_farmacia (categoria_id, farmacia_id);
ALTER TABLE produtos ADD FULLTEXT INDEX ftidx_produto_nome (nome);

-- 5. TRIGGERS ADICIONAIS (LÓGICA DE NEGÓCIO E AUDITORIA)

-- TRIGGER 5: trg_validar_maioridade 
DELIMITER //
CREATE TRIGGER trg_validar_maioridade
BEFORE INSERT ON clientes
FOR EACH ROW
BEGIN
IF TIMESTAMPDIFF(YEAR, NEW.data_nascimento, CURDATE()) < 18 THEN
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERRO: O cadastro de clientes exige a maioridade (18 anos).';
END IF;
END //
DELIMITER ;

-- TRIGGER 6: trg_log_senha_usuario 
DELIMITER //
CREATE TRIGGER trg_log_senha_usuario
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
IF OLD.senha <> NEW.senha THEN
INSERT INTO logAlteracoesSenha (usuario_id, ipOrigem)
VALUES (NEW.id, 'DESCONHECIDO_VIA_TRIGGER');
END IF;
END //
DELIMITER ;

-- 6. PROCEDURES E VIEWS

-- PROCEDURE: sp_atualizar_estoque
DELIMITER //
CREATE PROCEDURE sp_atualizar_estoque (
IN p_produto_id CHAR(36),
IN p_quantidadeVendida INT
)
BEGIN
IF (SELECT estoque FROM produtos WHERE id = p_produto_id) >= p_quantidadeVendida THEN
UPDATE produtos
SET estoque = estoque - p_quantidadeVendida
WHERE id = p_produto_id;
ELSE
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERRO: Estoque insuficiente para o produto.';
END IF;
END //
DELIMITER ;

-- VIEW 1: vw_farmacias_ativas 
DROP VIEW IF EXISTS vw_farmacias_ativas;
CREATE VIEW vw_farmacias_ativas AS
SELECT
f.id, f.nome_fantasia, f.cnpj, f.telefone,
e.rua, e.numero, e.cidade, e.estado
FROM
farmacias f
JOIN enderecos e ON f.endereco_id = e.id
JOIN usuarios u ON f.usuario_id = u.id
WHERE u.situacao = 'ativo';

SELECT *FROM vw_farmacias_ativas;

-- VIEW 2: Calcular o Total do Carrinho 
DROP VIEW IF EXISTS vw_total_carrinho;
CREATE VIEW vw_total_carrinho AS
SELECT
c.cliente_id,
p.nome AS produto_nome,
COUNT(c.id) AS quantidade,
p.preco,
SUM(p.preco) AS valor_total_item
FROM
carrinho c
JOIN
produtos p ON c.produto_cod = p.COD
GROUP BY
c.cliente_id, c.produto_cod, p.nome, p.preco;

SELECT * FROM vw_total_carrinho;


-- VIEW 3: vw_produtos_em_promocao 
DROP VIEW IF EXISTS vw_produtos_em_promocao;
CREATE VIEW vw_produtos_em_promocao AS
SELECT
p.COD, p.nome AS produtoNome, p.preco,
f.nome_fantasia AS farmaciaNome
FROM
produtos p
JOIN farmacias f ON p.farmacia_id = f.id
WHERE p.promocao = TRUE;

SELECT * FROM vw_produtos_em_promocao;

-- VIEW 4: vw_usuarios
DROP VIEW IF EXISTS vw_usuarios;
CREATE VIEW vw_usuarios AS 
SELECT 
u.id,
u.email,
u.senha,
g.nome AS grupo,
u.situacao
FROM usuarioGrupo ug 
LEFT JOIN usuarios u ON u.id = ug.usuario_id
LEFT JOIN gruposUsuarios g ON g.id = ug.grupo_id;

SELECT * FROM vw_usuarios;

-- VIEW 5
DROP VIEW  IF EXISTS vw_enderecos;
CREATE VIEW vw_enderecos AS 
SELECT 
e.*,
c.id AS cliente_id
FROM clientes c
LEFT JOIN enderecos e ON c.endereco_id = e.id;

SELECT * FROM vw_enderecos;

-- VIEW 6
CREATE VIEW vw_favoritos AS
SELECT 
    pf.cliente_id, 
    c.nome AS clienteNome,
    p.COD, 
    p.nome AS produtoNome, 
    p.preco, 
    f.nome_fantasia AS farmaciaNome 
FROM prod_favoritos pf
JOIN produtos p ON pf.produto_cod = p.COD
JOIN farmacias f ON p.farmacia_id = f.id
JOIN clientes c ON pf.cliente_id = c.id;

-- 7. SEGURANÇA: CRIAÇÃO DE USUÁRIOS E CONTROLE DE ACESSO

-- Nível 1: Administrador (DBA)
DROP USER IF EXISTS 'admin_farma'@'localhost';
CREATE USER 'admin_farma'@'localhost' IDENTIFIED BY 'SenhaForteAdminFarma2025';
GRANT ALL PRIVILEGES ON FarmaShop.* TO 'admin_farma'@'localhost' WITH GRANT OPTION;

-- Nível 2: Aplicativo Web
DROP USER IF EXISTS 'app_web'@'%';
CREATE USER 'app_web'@'%' IDENTIFIED BY 'SenhaSeguraParaAplicacao789';
GRANT SELECT, INSERT, UPDATE, DELETE ON FarmaShop.* TO 'app_web'@'%';
GRANT EXECUTE ON *.* TO 'app_web'@'%';

-- Nível 3: Relatórios (BI)
DROP USER IF EXISTS 'relatorio_user'@'%';
CREATE USER 'relatorio_user'@'%' IDENTIFIED BY 'SenhaRelatorioSomenteLeitura101';
GRANT SELECT ON FarmaShop.* TO 'relatorio_user'@'%';

FLUSH PRIVILEGES;

