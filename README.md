# 🍊 Orange Graph 🍊

Aplicação Spring Boot para gerenciamento de transações e contas, com suporte a MySQL e H2.

## Requisitos

- Java 21+
- Maven 3.8+
- Docker (para rodar o MySQL)
- Navegador web

## Como rodar a aplicação

### 1. Subindo o banco MySQL com Docker

```sh
docker-compose up -d
```

### 2. Configurando o banco

No arquivo src/main/resources/application.yml, descomente a seção de configuração do MySQL e comente a do H2, se desejar usar o MySQL.

### 3. Build e execução

```sh
mvn clean install
mvn spring-boot:run
```

### 4. Acessando a aplicação

- Swagger UI:
  - http://localhost:8080/swagger-ui.html


- H2 Console: http://localhost:8080/h2-console
  - `JDBC URL`: jdbc:h2:mem:orange_graph_db
  - `User`: sa
  - `Password`: password
 
## Testes e cobertura

Após rodar os testes com:
```sh
mvn clean install
```

Abra o relatório de cobertura Jacoco em: `target/site/jacoco/index.html`

Basta abrir esse arquivo no navegador.

<hr></hr>

## Observação:
Para usar o H2, mantenha a configuração padrão do application.yml.
Para usar o MySQL, lembre-se de subir o container Docker e ajustar o application.yml.
