# spring-parking-control

Pratica de Spring Boot desenvolvida a partir [desse tutorial](https://www.youtube.com/watch?v=LXRU-Z36GEU&t=3325s) da mestre [Micheli Brito](https://www.michellibrito.com/)

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?logo=spring&logoColor=white&style=for-the-badge)
![Postgres](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

## Para rodar:

1. Instalar JDK
2. Configurar variáveis do banco em `src/main/resources/application/application.properties`
3. Rodar `./gradlew bootRun` na raiz do projeto

## Conceitos aprendidos

1. [Spring / Spring Boot](#intro)
2. [Inversão de Controle](#ioc)
3. [Injeção de Dependência](#depdendency-injection)
4. [Beans](#beans)
5. [Models](#models)
6. [Repositories / JPA](#repositories)
7. [Services](#services)
8. [Controllers](#controllers)
9. [Outros](#other)

### <a name="intro"></a>Spring Boot intro

O ecossistema Spring é um conjunto de ferramentas, frameworks e bibliotecas que facilitam o desenvolvimento de aplicações Java

Spring Boot é um framework que facilita a criação de aplicações Spring

Ele tras varias configuracoes predefinidas e um servidor Tomcat imputido, trazendo assim uma aplicação pronta pra rodar

A [ documentacao oficial ](https://spring.io/guides/gs/spring-boot/) é otima

### <a name="ioc"></a>Inversao de Controle (IoC)

Inversao de controle é um padrão onde a responsabilidade de instanciação de dependencias é delegada ao framework

Objetos apenas `definem suas dependencias, sem instancia-las` manualmente

A tarefa de instanciação das dependencias é então delegada a algum `IoC container` contido no framework

Isto esta relacionado ao conceito de:

### <a name="depdendency-injection"></a>Injeção de Dependencia

Injeção de dependencia é a implementação do conceito de Inversao de Controle

Por exemplo, um `UserService` depende de uma instancia do objeto `UserRepository`

```java
/*src/main/java/com/api/projeto/service/UserService.java*/

private final UserRepository userRepository;

//Em vez de instanciar o repository manuamente
UserService(){
    this.userRepository = new UserRepository()
}

//Apenas definimos que o service depende de um objeto UserRepository
UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

```

O `IoC container` é responsavel por instanciar o `UserRepository` e injeta-lo no `UserService`

O Spring Boot usa o `ApplicationContext` como IoC container para gerenciar as dependencias

### <a name="beans"></a>Beans

Bean é uma unidade fundamental no framework Spring

Bean é um objeto instanciado e gerenciado pelo IoC container atraves de injeção de dependência

Beans são declarados em classes anotadas por `decorators` do Spring, como: `@Component`, `@Service`, `@Repository`, `@RestController`

### <a name="models"></a>Models

Modelos são classes Java que representam entidades e definem seu modelamente ao banco de dados

Eles podem ser definidos anotando classes com decoradores `@Entity` e `@Table(name = "table_name")` do pacote `javax.persistence`

```java
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "parking_spot")
public class ParkingSpotModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id // identificador autoincrementavel
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10, name = "parking_spot_number")
    private String parkingSpotNumber;

    @Column(nullable = false, name = "registration_datec")
    private LocalDateTime registrationDate;
}
```

### <a name="repositories"></a>Repositories / JPA

`Java Persistence API` é uma especificação que define um padrão para implementação de ORMs no Java EE

`Spring Data JPA` é uma biblioteca do Spring que implementa o padrão JPA

Repositories sao interfaces anotadas com `@Repository`, que extendem a interface `JpaRepository<MODELO, IDENTIFICADOR>`

A interface `JpaRepository` possui varios metodos de ORM, como `save`, `delete`, `findAll`, `findById`, `count`, `existsById`, que podem ser invocados pelo repository instanciado

O repository não será instanciado manualmente, mas sim pelo framework, por injeção de dependência

```java
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.api.parkingcontrol.models.ParkingSpotModel;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpotModel, UUID> {
    //Metodos extras podem ser declarados, e estes podem ser invocados em instancias dessa interface
    ParkingSpotModel findByLicensePlateCar(String licencePlateCar);
    ParkingSpotModel findByLicensePlateCarIsNotNull(String licencePlateCar);
}
```

### <a name="services"></a>Services

Services sao responsaveis por implementar as regras de negocio da aplicação

No Spring, são classes anotadas com `@Service` com que podem ser `injetadas` em controllers e outros services

```java
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.repositories.ParkingSpotRepository;

@Service
public class ParkingSpotService {

    final ParkingSpotRepository parkingSpotRepository;

    //Injecao de dependencia areaves do construtor
    ParkingSpotService(ParkingSpotRepository parkingSpotRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
    }

    @Transactional //Decorador protege a operacao no banco, realizando rollback em caso de falha
    public ParkingSpotModel save(ParkingSpotDto parkingSpotDto) {
        return parkingSpotRepository.save(parkingSpotDto);
    }
}
```

### <a name="controllers"></a>Controllers

Podemos definir controllers anotando classes com o decorator `@RestController`

Rotas e handlers podem ser definidos anotando os metodos dessa classe com `@GetMapping | @PostMapping ...`

O corpo da requisição pode ser mapeado para um DTO com o decorator `@RequestBody`

Por baixo dos panos o Spring faz o roteamendo utilizando um `Dispatcher Servlet`

```java
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    //@Valid valida o objeto recebido baseado no DTO, e retorna erro 400 caso nao seja valido
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }
}
```

### <a name="other"></a>Outros

- **Lombok** é uma biblioteca que facilita o desenvolviemnto, e evita a necessidade de escrever um monte de boilerplate

  - Ele pode gerar automatimante getters, setters, construtores, e afins para classes Java

- **Maven** e **Gradle** são ferramentas de build e gerenciamente de dependencias de aplicações Java

  - Esse projeto utiliza o Gradle, e pela experimentação me parece que ele é mais rápido que o Maven
