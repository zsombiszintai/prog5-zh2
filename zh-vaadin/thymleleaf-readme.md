# A kiegészített ZH megoldása Thymeleaf használatával

Ez az útmutató a `src/README.md` hét feladatát és a jelenlegi állatos alkalmazást követi. A projekt Spring Boot 3.5.6-ot és Java 21-et használ. A fájl átállási és tanulási segédlet: a benne szereplő példák nem kerültek be az alkalmazásba, és együtt futtatva még ellenőrizni kell őket.

**A fő változás:** a Java-ban felépített Vaadin komponensek helyére HTML sablonok kerülnek, amelyeket Spring MVC controllerek töltenek fel adatokkal. A böngésző GET kéréssel kér oldalt, az űrlap POST kéréssel küld adatot. A Thymeleaf a szerveren állítja elő a HTML-t; ehhez a feladathoz nem kell külön REST API vagy JavaScript alkalmazás.

## Mi marad, mi változik?

| Jelenlegi megoldás | Thymeleaf megoldás |
| --- | --- |
| `Pet`, `PetSpecies` | Megmaradnak |
| `PetRepository`, `InMemoryPetRepository`, `DBPetRepository` | Megmaradnak |
| `PetService`, `PetServiceImpl`, `@Qualifier("mock")` | Megmaradnak |
| `SecurityService`, `SecurityServiceImpl` | Megmaradhatnak, már Spring Security kontextust használnak |
| `@EnableMethodSecurity`, `@PreAuthorize`, `@PostFilter` | Megmaradnak |
| `LoginView`, `ListView`, `RegisterView` | MVC controller + HTML sablon |
| Vaadin `@Route` | `@GetMapping`, `@PostMapping` |
| `Grid<Pet>` | HTML táblázat, `th:each` |
| `Binder<Pet>` | Űrlapmodell, `@Valid`, `BindingResult`, `th:field` |
| `MainLayout`, `RouterLink` | Közös HTML fragment, `<a th:href="...">` |
| `AuthenticationContext` | `Authentication`, illetve Thymeleaf `sec:authentication` |
| `HasDynamicTitle`, `getTranslation(...)` | `<title th:text="#{...}">`, `#{...}` |
| `MessagesI18NProvider` | Spring MVC üzenetfeloldás; a `messages.properties` megmarad |
| `VaadinSecurityConfigurer` | Spring Security `authorizeHttpRequests`, `formLogin`, `logout` |

## 0. Függőségek és fájlszerkezet

A `pom.xml`-ből kikerül a `vaadin-spring-boot-starter`, a `vaadin.version` property, a Vaadin BOM-ot importáló `dependencyManagement`, valamint a Vaadin-specifikus `production` profil. A Spring Boot Maven plugin, a security és a tesztfüggőségek megmaradnak. Ezeket kell hozzáadni a `dependencies` részhez; verziójukat a jelenlegi Spring Boot parent kezeli:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

A security extras függőség a példák `sec:*` attribútumaihoz kell. Spring Boot 3 mellett a validáció importjai `jakarta.validation.*` alakúak.

Javasolt új fájlok a `zh-vaadin` projekthez képest:

```text
src/main/java/hu/mik/pte/prog5/zh1vaadin/
  controller/LoginController.java
  controller/PetController.java
  model/PetForm.java
src/main/resources/
  templates/login.html
  templates/pets/list.html
  templates/pets/register.html
  templates/fragments/header.html
  static/css/style.css
  messages.properties                  # meglévő
```

Az átálláskor a Vaadin-függő Java osztályokat is ki kell venni vagy át kell írni: a három nézetet, a `MainLayout`-ot és a `MessagesI18NProvider`-t. Ellenőrizd az alkalmazás indítóosztályát és az `application.properties` Vaadin-beállításait is. A `src/main/frontend` Vaadin fájljai nem kellenek a Thymeleaf oldalakhoz. A Java package neve ettől még maradhat `zh1vaadin`.

## 1. feladat – Bejelentkezés

### Spring Security

A `SecurityConfig` osztályon megmarad a `@Configuration`, az `@EnableWebSecurity` és az `@EnableMethodSecurity`. Kikerül a `VaadinAwareSecurityContextHolderStrategyConfiguration` importja és a hozzá tartozó `@Import`, valamint a `VaadinSecurityConfigurer` és a `LoginView` hivatkozása.

A meglévő `securityFilterChain` bean helyére:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/css/**", "/error").permitAll()
            .requestMatchers("/register", "/register/**").hasRole("REGISTER")
            .anyRequest().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/list", true)
            .failureUrl("/login?error")
            .permitAll())
        .logout(logout -> logout
            .logoutSuccessUrl("/login?logout")
            .permitAll());
    return http.build();
}
```

A `userDetailsManager()` bean a négy felhasználóval változatlanul megtartható. A fenti konfiguráció sikeres belépés után mindig a listára irányít. A `/register` itt **állatregisztráció**, ezért `REGISTER` jogosultságot igényel.

A GET oldalt saját controller adja:

```java
package hu.mik.pte.prog5.zh1vaadin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```

A visszatérő `"login"` a `templates/login.html` sablont jelöli. Ezért `@Controller` kell; a `@RestController` szövegként küldené vissza a nevet. **POST `/login` controllert nem írunk:** az adatokat Spring Security dolgozza fel. A működést a [Spring Security bejelentkezési dokumentációja](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html) részletezi.

### `templates/login.html`

```html
<!DOCTYPE html>
<html lang="hu" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="#{title.login}">Bejelentkezés</title>
</head>
<body>
<h1 th:text="#{title.login}">Bejelentkezés</h1>
<p th:if="${param.error}" th:text="#{login.error.message}"></p>
<p th:if="${param.logout}" th:text="#{login.logout.success}"></p>
<form th:action="@{/login}" method="post">
    <label for="username" th:text="#{login.username}"></label>
    <input id="username" name="username" required>
    <label for="password" th:text="#{login.password}"></label>
    <input id="password" name="password" type="password" required>
    <button type="submit" th:text="#{login.submit}"></button>
</form>
</body>
</html>
```

A `username` és `password` mezőneveket a Spring Security alapbeállítása várja. A CSRF-védelem maradjon bekapcsolva: a Springgel integrált Thymeleaf a `th:action`-t használó POST űrlapokhoz automatikusan hozzáadja a szükséges rejtett tokent. Lásd a [CSRF dokumentációt](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).

## 2. feladat – PetService

Itt nincs szükség frontend miatti módosításra. A controller ugyanúgy a konstruktorban kapja a `PetService` interfészt, ahogy eddig a nézet kapta. A `PetServiceImpl` továbbra is a `PetRepository` interfészt használja, és a `@Qualifier("mock")` választja ki a repositoryt.

Ne kerüljön át a repository közvetlen meghívása a controllerbe. A fajszűrésnek és a mentési jogosultságnak továbbra is a service-ben kell érvényesülnie.

## 3. feladat – Fejléc

A `MainLayout` helyett közös fragment készül. `templates/fragments/header.html`:

```html
<!DOCTYPE html>
<html lang="hu" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<body>
<header th:fragment="header">
    <span th:text="#{header.username}"></span>
    <span sec:authentication="name"></span>
    <a th:href="@{/list}" th:text="#{header.menu.pet.list}"></a>
    <a sec:authorize="hasRole('REGISTER')" th:href="@{/register}"
       th:text="#{header.menu.pet.register}"></a>
    <form th:action="@{/logout}" method="post">
        <button type="submit" th:text="#{header.menu.logout}"></button>
    </form>
</header>
</body>
</html>
```

A lista és a regisztrációs oldal `<body>` elemében ezzel használható:

```html
<header th:replace="~{fragments/header :: header}"></header>
```

A kijelentkezés POST űrlap, a hozzá tartozó végpontot Spring Security kezeli. A felhasználónevet a security dialektus adja, ezért nem kell minden controllerben külön modellattribútumot felvenni. A dialektus használatát a [Thymeleaf security útmutatója](https://www.thymeleaf.org/doc/articles/springsecurity.html) mutatja be; annak régi függőségpéldái helyett itt a fenti `springsecurity6` függőséget használd.

## 4–5. feladat – Lista és állatregisztráció

### Űrlapmodell a Binder helyett

A `Pet` jelenleg nem tartalmaz Bean Validation annotációkat. Külön űrlapmodell használatával nem kell módosítani:

```java
package hu.mik.pte.prog5.zh1vaadin.model;

import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PetForm {
    @NotBlank(message = "{register.name.required}")
    private String name;

    @NotNull(message = "{register.species.required}")
    private PetSpecies species;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PetSpecies getSpecies() { return species; }
    public void setSpecies(PetSpecies species) { this.species = species; }
}
```

A `@NotBlank` a kizárólag szóközöket tartalmazó nevet is elutasítja. A böngésző `required` ellenőrzése mellett a szerveroldali validáció is szükséges.

### `PetController`

```java
package hu.mik.pte.prog5.zh1vaadin.controller;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.model.PetForm;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping({"/", "/list"})
    public String list(Model model) {
        model.addAttribute("pets", petService.listAll());
        return "pets/list";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("petForm", new PetForm());
        model.addAttribute("species", petService.listSpecies());
        return "pets/register";
    }

    @PostMapping("/register")
    public String save(@Valid @ModelAttribute("petForm") PetForm form,
                       BindingResult errors, Model model,
                       RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("species", petService.listSpecies());
            return "pets/register";
        }
        petService.save(new Pet(null, form.getName().trim(), form.getSpecies()));
        redirect.addFlashAttribute("saved", true);
        return "redirect:/register";
    }
}
```

A `BindingResult` közvetlenül az ellenőrzött modellparaméter után áll. Hiba esetén újra fel kell tölteni a fajválasztó listáját; ugyanazt az oldalt adjuk vissza, így a megadott értékek és a hibák megmaradnak. Sikernél átirányítás következik: az új GET kiürített űrlapot ad, a flash attribútum egyszeri sikerjelzést tesz lehetővé. Ez a Post/Redirect/Get minta megakadályozza az egyszerű oldalfrissítés miatti újraküldést.

Az azonosítót továbbra is a repository adja. A listázó metódus nem hívja a `listSpecies()`-t, mert az `REGISTER` szerepkörrel védett, miközben listázni más felhasználó is jogosult.

### `templates/pets/list.html`

```html
<!DOCTYPE html>
<html lang="hu" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <title th:text="#{title.pet.list}"></title>
</head>
<body>
<header th:replace="~{fragments/header :: header}"></header>
<h1 th:text="#{title.pet.list}"></h1>
<table>
    <thead>
    <tr>
        <th sec:authorize="hasRole('ADMIN')" th:text="#{pet.id}"></th>
        <th th:text="#{pet.name}"></th>
        <th th:text="#{pet.species}"></th>
    </tr>
    </thead>
    <tbody>
    <tr th:each="pet : ${pets}">
        <td sec:authorize="hasRole('ADMIN')" th:text="${pet.uid}"></td>
        <td th:text="${pet.name}"></td>
        <td th:text="#{${'pet.species.' + pet.species.name()}}"></td>
    </tr>
    </tbody>
</table>
</body>
</html>
```

Az azonosító fejléce és adatcellái is feltételesek. A `pet.uid` a meglévő modell `getUid()` metódusához tartozik; itt nincs `id` nevű property. A faj fordítási kulcsa például `pet.species.DOG` lesz.

### `templates/pets/register.html`

```html
<!DOCTYPE html>
<html lang="hu" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="#{title.pet.register}"></title>
</head>
<body>
<header th:replace="~{fragments/header :: header}"></header>
<h1 th:text="#{title.pet.register}"></h1>
<p th:if="${saved}" th:text="#{register.success}"></p>
<form th:action="@{/register}" th:object="${petForm}" method="post">
    <p th:if="${#fields.hasAnyErrors()}"
       th:text="#{register.validation.error}"></p>
    <label for="name" th:text="#{pet.name}"></label>
    <input id="name" th:field="*{name}" required>
    <p th:errors="*{name}"></p>
    <label for="species" th:text="#{pet.species}"></label>
    <select id="species" th:field="*{species}" required>
        <option value="" th:text="#{register.species.placeholder}"></option>
        <option th:each="item : ${species}" th:value="${item}"
                th:text="#{${'pet.species.' + item.name()}}"></option>
    </select>
    <p th:errors="*{species}"></p>
    <button type="submit" th:text="#{register.submit}"></button>
</form>
</body>
</html>
```

A `th:object` kiválasztja az űrlap modelljét; a `th:field` összeköti a mezőket annak propertyjeivel. A `th:errors` a szerveroldali hibákat írja ki. A kötést és a hibaüzenetek kezelését a [Thymeleaf Spring integrációs útmutatója](https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.pdf) tárgyalja.

## 6. feladat – Lokalizált címek és feliratok

A meglévő `src/main/resources/messages.properties` használható tovább. A Vaadin `getTranslation("title.login")` megfelelője Thymeleafben `#{title.login}`; ez a `<title>` és a látható fejléc szövegében is működik.

A fenti mintákhoz új kulcsok kellenek a `messages.properties` fájlba:

```properties
login.logout.success=Sikeres kijelentkezés.
register.species.placeholder=Válassz fajt!
typeMismatch.petForm.species=Érvénytelen faj.
```

Az utolsó üzenet hibás enumérték beküldésekor használható. A magyar szövegeket tartalmazó alap `messages.properties` már elegendő ehhez a magyar felülethez. Ha több nyelvet kérnek, jöhet `messages_hu.properties`, `messages_en.properties` és külön locale-választás; a Vaadin `I18NProvider`-e helyett ez Spring MVC beállítás lesz.

## 7. feladat – Jogosultság és fajonkénti szűrés

Három helyen marad ellenőrzés:

1. **HTML:** a `sec:authorize` elrejti a regisztrációs linket és az admin azonosítóoszlopot.
2. **HTTP végpont:** a `SecurityFilterChain` csak `REGISTER` szerepkörrel engedi a GET és POST `/register` kéréseket.
3. **Service:** a meglévő `@PreAuthorize` a mentést és a fajlista lekérését védi, a `@PostFilter` a látható állatokat szűri.

A menüpont elrejtése önmagában nem védi a végpontot. A Vaadin nézetek `@AnonymousAllowed`, `@PermitAll`, `@RolesAllowed` annotációi helyett ebben a mintában a HTTP szabályok határozzák meg az oldalak elérését. A service meglévő Spring Security annotációi megmaradnak; a controller a Spring által injektált service proxyt hívja.

A jelenlegi fajszűrő kifejezés is megmaradhat:

```java
@PreAuthorize("isAuthenticated()")
@PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
public List<Pet> listAll() {
    return petRepository.findAll();
}
```

A repository által visszaadott kollekciónak szűrhetőnek kell lennie, és a szűrés nem törölheti a közös tárolt adatokat. A használt `InMemoryPetRepository.findAll()` módosítható másolatot ad vissza; ezt az átálláskor is őrizd meg. A `DBPetRepository` jelenleg csak váz, metódusai `UnsupportedOperationException` kivételt dobnak.

A jelenlegi felhasználókon az elvárt működés változatlan (mindegyik jelszava `password`):

| Felhasználó | Látható fajok | Állatregisztráció | Azonosítóoszlop |
| --- | --- | --- | --- |
| `admin` | minden faj | igen | látható |
| `dog` | kutya | nem | rejtett |
| `cat` | kutya | igen | rejtett |
| `uncommon` | teknős, papagáj, vadászgörény | nem | rejtett |

A `cat` nevű felhasználó ténylegesen `REGISTER` és `DOG` szerepkört kapott. Az `ADMIN` önmagában nem kerüli meg a fajszűrést: az admin külön kapta meg az összes faj szerepkörét is.

## Mire jó a meglévő `crud-mvc` projekt?

A szomszédos projektben már vannak közeli minták:

- `../crud-mvc/src/main/java/hu/mik/pte/prog5/crud_mvc/controller/CarController.java`: controller, modellátadás, `@Valid`, `BindingResult`, átirányítás. A `create()` hibakezelése jó kiindulás.
- `../crud-mvc/src/main/resources/templates/cars/list.html`: listázás HTML sablonnal.
- `../crud-mvc/src/main/resources/templates/cars/form.html`: űrlap kötése modellhez.
- `../crud-mvc/src/main/resources/templates/fragments/`: közös sablonrészek.

A CRUD projekt felhasználó-regisztrációját ne másold át állatregisztráció helyett: ennél a ZH-nál a `/register` egy új `Pet` mentését jelenti.

## Ellenőrzés az átállás után

A `zh-vaadin` könyvtárból, Java 21-gyel:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

A meglévő `PetServiceSecurityTests` továbbra is releváns. A fenti parancsok itt útmutatók, nem igazolt futási eredmények.

Kézzel ellenőrizd:

1. Kijelentkezve a `/list` és `/register` bejelentkezést kér; hibás belépésnél magyar hibaüzenet látszik.
2. A négy felhasználó a fenti táblázatnak megfelelő listát és menüpontokat kapja.
3. A `dog` és az `uncommon` közvetlen GET `/register` kérése is tiltott. POST-nál érvényes CSRF token mellett is szerepkör szerinti tiltás kell.
4. Üres, csak szóközös nevű vagy faj nélküli űrlap nem menthető. A szerveroldali ellenőrzést a böngésző `required` szabályainak megkerülésével is ellenőrizd.
5. Hibás adatoknál a mezőhibák és a fajválasztó megmaradnak; helyes mentés után az űrlap kiürül és sikerjelzés látszik.
6. A mentett állat a fajának megfelelő jogosultságú felhasználó listáján megjelenik. Az oldalfrissítés nem ment újabb példányt.
7. A három oldal lapfülcíme és a fajnevek magyarul jelennek meg; kijelentkezés után a védett oldalak ismét belépést kérnek.

ZH-n a fő megfeleltetés: **Vaadin nézet → MVC controller + HTML sablon; Binder → űrlapmodell és validáció; MainLayout → fragment; a service és a repository logikája megmarad.**
