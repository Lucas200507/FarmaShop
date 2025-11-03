-- #################################################################
-- 1. CRIAÇÃO DO BANCO DE DADOS E FUNÇÃO DE GERAÇÃO DE ID
-- (Nomes de tabelas e colunas ajustados para camelCase)
-- #################################################################

CREATE DATABASE FarmaShop;
USE FarmaShop;

-- FUNÇÃO OBRIGATÓRIA: Regra Própria para Geração de IDs (UUID/GUID)
DELIMITER $$
CREATE FUNCTION fn_gerar_uuid() RETURNS CHAR(36)
DETERMINISTIC
BEGIN
RETURN UUID();
END$$
DELIMITER ;

-- =================================================================
-- 2. TABELAS OBRIGATÓRIAS E ESTRUTURA BASE 
-- =================================================================

-- Tabela Grupos de Usuários (gruposUsuarios)
CREATE TABLE gruposUsuarios (
id INT PRIMARY KEY,
nome VARCHAR(50) UNIQUE NOT NULL,
descricao VARCHAR(255)
);

-- Tabela Usuários
CREATE TABLE usuarios(
id CHAR(36) PRIMARY KEY,
situacao ENUM('ativo', 'inativo') DEFAULT 'ativo',
email VARCHAR(60) NOT NULL UNIQUE,
senha VARCHAR(255) NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabela de Relacionamento 
CREATE TABLE usuarioGrupo (
usuario_id CHAR(36) NOT NULL, 
grupo_id INT NOT NULL,   
PRIMARY KEY (usuario_Id, grupo_id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
FOREIGN KEY (grupo_id) REFERENCES gruposUsuarios (id)
);

-- Tabela para log de auditoria de segurança 
CREATE TABLE logAlteracoesSenha (
id INT PRIMARY KEY AUTO_INCREMENT,
usuarioId CHAR(36) NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ipOrigem VARCHAR(50), 
FOREIGN KEY (usuarioId) REFERENCES usuarios (id)
);

-- Tabela Endereços
CREATE TABLE enderecos(
id INT PRIMARY KEY AUTO_INCREMENT,
cep VARCHAR(9) UNIQUE NOT NULL,
estado ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO') NOT NULL,
cidade VARCHAR(60) NOT NULL,
rua VARCHAR(60) NOT NULL,
numero INT,
bairro VARCHAR(60),
complemento TEXT
);

-- Tabela Clientes
CREATE TABLE clientes(
id CHAR(36) PRIMARY KEY,
nome VARCHAR(60) NOT NULL,
cpf VARCHAR(12) UNIQUE NOT NULL,
telefone VARCHAR(14) UNIQUE NOT NULL,
data_nascimento DATE NOT NULL, 
endereco_id INT,  
usuario_id CHAR(36) NOT NULL,  
data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
FOREIGN KEY (endereco_id) REFERENCES enderecos (id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- Tabela Cartão
CREATE TABLE cartao(
id INT PRIMARY KEY AUTO_INCREMENT,
clienteId CHAR(36) NOT NULL, 
nome_titular VARCHAR(100) NOT NULL, 
bandeira ENUM('Visa','MasterCard','Elo','Amex','Hipercard','Outros') NOT NULL,
ultimos_digitos CHAR(4) NOT NULL, 
validade_mes CHAR(2),
validade_ano CHAR(4) NOT NULL, 
token_pagamento VARCHAR(255) NOT NULL, 
dataCadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (clienteId) REFERENCES clientes(id)
);

-- Tabela Farmácias
CREATE TABLE farmacias(
id CHAR(36) PRIMARY KEY,
nome_juridico VARCHAR(150) NOT NULL,   
nome_fantasia VARCHAR(150) NOT NULL,   
cnpj VARCHAR(14) UNIQUE NOT NULL,
alvara_sanitario VARCHAR(50) NOT NULL, 
responsavel_tecnico VARCHAR(60) NOT NULL, 
crf VARCHAR(20) NOT NULL,
telefone VARCHAR(20) UNIQUE NOT NULL,
email VARCHAR(60) UNIQUE NOT NULL,
endereco_id INT NOT NULL,
usuario_id CHAR(36) NOT NULL,
dataCadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (endereco_id) REFERENCES enderecos (id),
FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

-- Tabela Categoria Produtos
CREATE TABLE categoria_produtos(
id INT PRIMARY KEY AUTO_INCREMENT,
nome VARCHAR(100) NOT NULL
);

-- Tabela Produtos
CREATE TABLE produtos(
id CHAR(36) PRIMARY KEY,
codigo VARCHAR(7) NOT NULL,
nome VARCHAR(100) NOT NULL,
descricao TEXT NOT NULL,
estoque INT NOT NULL,
promocao BOOLEAN DEFAULT FALSE,
preco DECIMAL(10, 2) NOT NULL,
dataAlteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
dataCadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
categoria_id INT NOT NULL, 
farmacia_id CHAR(36) NOT NULL, 
FOREIGN KEY (categoria_id) REFERENCES categoria_produtos (id),
FOREIGN KEY (farmacia_id) REFERENCES farmacias (id)
);

-- Tabela Imagem Produtos 
CREATE TABLE imagem_produtos(
id INT PRIMARY KEY AUTO_INCREMENT,
produto_id CHAR(36) NOT NULL, 
url VARCHAR(255) NOT NULL,
pricipal BOOLEAN DEFAULT FALSE,
ordem INT DEFAULT 0,
FOREIGN KEY (produto_id) REFERENCES produtos (id)
);

-- 3. TRIGGERS PARA GERAÇÃO DE ID 

-- Trigger para Usuarios
DELIMITER $$
CREATE TRIGGER trg_gerar_uuid_usuarios
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_uuid();
END IF;
END$$
DELIMITER ;

-- Trigger para Clientes
DELIMITER $$
CREATE TRIGGER trg_gerar_uuid_clientes
BEFORE INSERT ON clientes
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_uuid();
END IF;
END$$
DELIMITER ;

-- Trigger para Farmácias
DELIMITER $$
CREATE TRIGGER trg_gerar_uuid_farmacias
BEFORE INSERT ON farmacias
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_uuid();
END IF;
END$$
DELIMITER ;

-- Trigger para Produtos
DELIMITER $$
CREATE TRIGGER trg_gerar_uuid_produtos
BEFORE INSERT ON produtos
FOR EACH ROW
BEGIN
IF NEW.id IS NULL OR NEW.id = '' THEN
SET NEW.id = fn_gerar_uuid();
END IF;
END$$
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
DELIMITER $$
CREATE TRIGGER trg_validar_maioridade
BEFORE INSERT ON clientes
FOR EACH ROW
BEGIN
IF TIMESTAMPDIFF(YEAR, NEW.data_nascimento, CURDATE()) < 18 THEN
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERRO: O cadastro de clientes exige a maioridade (18 anos).';
END IF;
END$$
DELIMITER ;

-- TRIGGER 6: trg_log_senha_usuario 
DELIMITER $$
CREATE TRIGGER trg_log_senha_usuario
AFTER UPDATE ON usuarios
FOR EACH ROW
BEGIN
IF OLD.senha <> NEW.senha THEN
INSERT INTO logAlteracoesSenha (usuario_id, ipOrigem)
VALUES (NEW.id, 'DESCONHECIDO_VIA_TRIGGER');
END IF;
END$$
DELIMITER ;

-- 6. PROCEDURES E VIEWS (ajustados para camelCase)

-- PROCEDURE: sp_atualizar_estoque
DELIMITER $$
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
END$$
DELIMITER ;

-- VIEW 1: vw_farmacias_ativas 
CREATE VIEW vw_farmacias_ativas AS
SELECT
f.id, f.nome_fantasia, f.cnpj, f.telefone,
e.rua, e.numero, e.cidade, e.estado
FROM
farmacias f
JOIN enderecos e ON f.endereco_id = e.id
JOIN usuarios u ON f.usuario_id = u.id
WHERE u.situacao = 'ativo';

-- VIEW 2: vw_produtos_em_promocao (usa farmaciaId, nomeFantasia)
CREATE VIEW vw_produtos_em_promocao AS
SELECT
p.id AS produto_id, p.nome AS produtoNome, p.preco,
f.nome_fantasia AS farmaciaNome
FROM
produtos p
JOIN farmacias f ON p.farmacia_id = f.id
WHERE p.promocao = TRUE;

-- 7. SEGURANÇA: CRIAÇÃO DE USUÁRIOS E CONTROLE DE ACESSO

-- Nível 1: Administrador (DBA)
CREATE USER 'admin_farma'@'localhost' IDENTIFIED BY 'SenhaForteAdminFarma2025';
GRANT ALL PRIVILEGES ON FarmaShop.* TO 'admin_farma'@'localhost' WITH GRANT OPTION;

-- Nível 2: Aplicativo Web
CREATE USER 'app_web'@'%' IDENTIFIED BY 'SenhaSeguraParaAplicacao789';
GRANT SELECT, INSERT, UPDATE, DELETE ON FarmaShop.* TO 'app_web'@'%';
GRANT EXECUTE ON *.* TO 'app_web'@'%';

-- Nível 3: Relatórios (BI)
CREATE USER 'relatorio_user'@'%' IDENTIFIED BY 'SenhaRelatorioSomenteLeitura101';
GRANT SELECT ON FarmaShop.* TO 'relatorio_user'@'%';

FLUSH PRIVILEGES;



