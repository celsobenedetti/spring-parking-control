# spring-parking-control

Pratica de Spring Boot desenvolvida a partir [desse tutorial](https://www.youtube.com/watch?v=LXRU-Z36GEU) da mestre [Micheli Brito](https://www.michellibrito.com/)

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?logo=spring&logoColor=white&style=for-the-badge)
![Gradle](https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Postgres](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

## Para rodar:

1. Instalar JDK e Gradle
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

## Conceitos para estudar

- Spring Web, Spring Data JPA, Spring Validation
- Beans, Bean life cycle, Core Container

### <a name="intro"></a>Spring Boot intro

O ecossistema Spring é um conjunto de ferramentas, frameworks e bibliotecas para o desenvolvimento de aplicações Java

Spring Boot é um framework que facilita a criação de aplicações Spring

Ele predefine uma serie de configuracoes, e contem um servidor Tomcat imbutido, trazendo assim uma API pronta pra rodar

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

Podemos acessar propriedades do request como body, query, params com decorators `@RequestBody | @PathVariable ...`

Por baixo dos panos o Spring faz o roteamendo utilizando um `Dispatcher Servlet`

```java
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> findAll(
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findOneById(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);
        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }
        return ResponseEntity.ok(parkingSpotOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> createParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto) {
        var message = parkingSpotService.checkSpotRegistered(parkingSpotDto);
        if (message != "") {
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOne(@PathVariable(value = "id") UUID id) {
        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);

        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }

        parkingSpotService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Parking spot delete successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOne(@PathVariable(value = "id") UUID id,
            @RequestBody ParkingSpotDto parkingSpotDto) {

        Optional<ParkingSpotModel> parkingSpotOptional = parkingSpotService.findOneById(id);
        if (!parkingSpotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(parkingSpotService.updateOne(parkingSpotOptional.get(), parkingSpotDto));
    }

}
```

### <a name="other"></a>Outros

- **Lombok** é uma biblioteca que facilita o desenvolviemnto, e evita a necessidade de escrever um monte de boilerplate

  - Ele pode gerar automatimante getters, setters, construtores, e afins para classes Java

- **Maven** e **Gradle** são ferramentas de build e gerenciamente de dependencias de aplicações Java

  - Esse projeto utiliza o Gradle, e pela experimentação me parece que ele é mais rápido que o Maven
