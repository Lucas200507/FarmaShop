CREATE DATABASE FarmaShop;
USE FarmaShop;
-- Por enquanto, 2 tipos de usuários Cliente e Fornecedor (Farmácia)
CREATE TABLE usuarios(
	idUsuario INT PRIMARY KEY AUTO_INCREMENT,
    tipo ENUM('cliente', 'farmacia') DEFAULT 'cliente',
    situacao ENUM('ativo', 'inativo') DEFAULT 'ativo',
    email VARCHAR(60) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL, -- CRIPTOGRAFIA COM MD5
    data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE enderecos(
	idEndereco INT PRIMARY KEY AUTO_INCREMENT,
	cep VARCHAR(9) UNIQUE NOT NULL, -- 00000-000
    estado ENUM('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO') NOT NULL,
    cidade VARCHAR(60) NOT NULL,
    rua VARCHAR(60) NOT NULL,
    numero INT,
    bairro VARCHAR(60),
    complemento TEXT    
);

CREATE TABLE clientes(
	idCliente INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(60) NOT NULL,
    cpf VARCHAR(12) UNIQUE NOT NULL, -- 000000000-00
    telefone VARCHAR(14) UNIQUE NOT NULL, -- (00)90000-0000
    data_nascimento DATE NOT NULL, -- verificar se é de maior
    endereco_id INT,
    usuario_id INT NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (endereco_id) REFERENCES enderecos (idEndereco),
    FOREIGN KEY (usuario_id) REFERENCES usuarios (idUsuario)
);

CREATE TABLE cartao(
	idCartao INT PRIMARY KEY AUTO_INCREMENT,
	cliente_id INT NOT NULL,
    nome_titular VARCHAR(100) NOT NULL,
    bandeira ENUM('Visa','MasterCard','Elo','Amex','Hipercard','Outros') NOT NULL,
    ultimos_digitos CHAR(4) NOT NULL,
    validade_mes CHAR(2),
    validade_ano CHAR(4) NOT NULL,
    token_pagamento VARCHAR(255) NOT NULL, -- gerado pelo gateway de pagamento
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(idCliente)
);

CREATE TABLE farmacias(
	idFarmacia INT PRIMARY KEY AUTO_INCREMENT,
    nome_juridico VARCHAR(150) NOT NULL, -- razao social
	nome_fantasia VARCHAR(150) NOT NULL, -- nome comercial
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    alvara_sanitario VARCHAR(50) NOT NULL, -- Registro obrigatório na Anvisa/Vigilância
    responsavel_tecnico VARCHAR(60) NOT NULL, -- Nome do farmacêutico responsável
    crf VARCHAR(20) NOT NULL, -- Registro do farmacêutico no Conselho de Farmácia
    telefone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(60) UNIQUE NOT NULL,
    endereco_id INT NOT NULL,
	usuario_id INT NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (endereco_id) REFERENCES enderecos (idEndereco),
    FOREIGN KEY (usuario_id) REFERENCES usuarios (idUsuario)
);

CREATE TABLE categoria_produtos(
	idCategoria_produto INT PRIMARY KEY AUTO_INCREMENT,
	nome VARCHAR(100) NOT NULL
);

CREATE TABLE produtos(
	idProduto INT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(7) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    estoque INT NOT NULL,
    promocao BOOLEAN DEFAULT FALSE,
    preco DECIMAL(10, 2) NOT NULL,
    data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	categoria_id INT NOT NULL,
    farmacia_id INT NOT NULL,
	FOREIGN KEY (categoria_id) REFERENCES catedoria_produtos (idCategoria_produto),
    FOREIGN KEY (farmacia_id) REFERENCES farmacias (idFarmacia)
);

CREATE TABLE imagem_produtos(
	idImagem_produto INT PRIMARY KEY AUTO_INCREMENT,
    produto_id INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    pricipal BOOLEAN DEFAULT FALSE,
    ordem INT DEFAULT 0,
    FOREIGN KEY (produto_id) REFERENCES produtos (idProduto)
);
