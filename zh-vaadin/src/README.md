# 1. feladat – Bejelentkező felület

## Elvégzett módosítások

- A `LoginView` a Vaadin beépített `LoginForm` komponensét használja, és a `/login` útvonalon érhető el. A `@AnonymousAllowed` annotáció biztosítja, hogy bejelentkezés nélkül is megnyitható legyen.
- A nézet a `VerticalLayout` osztályból származik, a bejelentkező űrlap középre igazítva jelenik meg.
- A `login.setAction("login")` beállítás a Spring Security bejelentkezési végpontjára küldi a felhasználónevet és a jelszót.
- A `beforeEnter()` ellenőrzi az URL paramétereit. Ha az URL tartalmazza az `error` paramétert (például `/login?error`), a `login.setError(true)` bekapcsolja a hibaüzenetet. Hiba nélküli navigációnál a hibajelzés kikapcsol.
- A feliratok, a hibaüzenet és a böngészőlap címe I18n segítségével, a `messages.properties` resource bundle-ből érkeznek. Ezt a `MessagesI18NProvider` biztosítja, amely az `I18NProvider` interfészt valósítja meg.
- Az elfelejtett jelszó gomb rejtve marad, mert a projektben nincs hozzá megvalósított funkció.
- A `SecurityConfig` megkapta a `@Configuration` és `@EnableWebSecurity` annotációkat. A `SecurityFilterChain` bean a `VaadinSecurityConfigurer` segítségével a `LoginView` osztályt állítja be bejelentkező oldalként.
- A felhasználókat szolgáltató metódus `@Bean` annotációt kapott. Az `InMemoryUserDetailsManager` most már tartalmazza az előkészített `admin`, `dog`, `cat` és `uncommon` felhasználókat. A metódus visszatérési típusa a `UserDetailsManager` interfész.

## Érintett fájlok

- `main/java/hu/mik/pte/prog5/zh1vaadin/ui/view/LoginView.java`
- `main/java/hu/mik/pte/prog5/zh1vaadin/config/SecurityConfig.java`
- `main/java/hu/mik/pte/prog5/zh1vaadin/config/MessagesI18NProvider.java` (új fájl)
- `main/resources/messages.properties` (új fájl, a meglévő fordításokkal és az új bejelentkezési szövegekkel)

Az útvonalak ennek a README-nek a könyvtárához (`src`) képest értendők.

## Kipróbálás

1. Java 21 használatával indítsuk el az alkalmazást a projekt gyökeréből: `./mvnw.cmd spring-boot:run`.
2. Nyissuk meg a `http://localhost:8080/login` címet.
3. A bejelentkezés például az `admin` felhasználónévvel és a `password` jelszóval próbálható ki. A többi előkészített felhasználó jelszava is `password`.
4. Hibás felhasználónév vagy jelszó esetén a bejelentkező oldalon magyar hibaüzenetnek kell megjelennie.

## Ellenőrzés és jelenlegi korlátok

A teljes buildet, a tesztek sikeres futását és az alkalmazás indulását ebben a környezetben nem sikerült igazolni. A helyi Maven-tárból szükséges függőségek hiányoztak, az alternatív fordítási kísérlet pedig fordítói erőforrás-kezelési hibával zárult. A projekt Java 21-et igényel; az alapértelmezett Java 17 helyett a megtalált Java 21 JDK-val történtek az ellenőrzési kísérletek.

A sikeres bejelentkezés utáni kezdőoldal, valamint a felületi kijelentkezési lehetőség nem készült el ebben a lépésben. A módosítások az elsőként megadott részfeladatra, a bejelentkező felületre és annak biztonsági bekötésére terjednek ki.

# 2. feladat – PetService

A `PetServiceImpl` osztály megvalósítja a `PetService` interfészt. A `@Service` annotációval a Spring automatikusan regisztrálja.

- A konstruktor `PetRepository` interfész típusú paramétert fogad. A `@Qualifier("mock")` annotáció választja ki a két repository közül a `mock` nevű beant. Az implementáció nem importálja és közvetlenül nem hivatkozza az `InMemoryPetRepository` osztályt.
- A `listAll()` a repository `findAll()` metódusát hívja, és annak eredményét adja vissza.
- A `save(Pet pet)` a repository `save(pet)` metódusának adja át a mentendő állatot, és visszaadja a mentés eredményét.
- A `listSpecies()` a `PetSpecies` enum összes értékét adja vissza listaként. Ehhez a repository interfészben nincs külön metódus.

Új fájl: `main/java/hu/mik/pte/prog5/zh1vaadin/service/PetServiceImpl.java`.

Ellenőrzés: a szolgáltatás és a hozzá szükséges saját osztályok fordítását Java 21-gyel, közvetlen `javac` hívással is megpróbáltam. A fordító a JAR-erőforrások lezárásakor `AccessDeniedException` hibával állt le, ezért sikeres fordítás és teljes alkalmazásindulás továbbra sem igazolt.

# 3. feladat – Fejléc

A `MainLayout` az `AppLayout` navigációs sávjában megjeleníti a bejelentkezett felhasználó nevét, a listázás és a regisztráció `RouterLink` menüpontjait, valamint a kijelentkezés gombot. A felhasználó lekérése és a kijelentkeztetés a Vaadin `AuthenticationContext` segítségével történik. A fejléc feliratai a `messages.properties` fájlból származnak.

# 4. feladat – Listázás

- A `ListView` a `/list` útvonalon érhető el a `MainLayout` keretében. A gyökérútvonal (`/`) is erre a nézetre mutat, így sikeres bejelentkezés után megjelenhet a lista.
- A `Grid<Pet>` az azonosítót, a nevet és a magyarra fordított fajt jeleníti meg. Az adatokat a konstruktorban injektált `PetService` szolgáltatja.
- Az azonosító oszlop csak akkor látható, ha a `SecurityService.getRolesAsString()` eredménye tartalmazza az `ADMIN` szerepkört.
- A `SecurityServiceImpl` a meglévő `SecurityService` interfész alapértelmezett metódusát teszi elérhetővé Spring szolgáltatásként. A nézet az interfészt használja a dependency injection során.

# 5. feladat – Állatregisztráció

- A `RegisterView` a `/register` útvonalon, a `MainLayout` keretében jelenik meg. A listanézet és a közös layout bejelentkezett felhasználó számára érhető el (`@PermitAll`); a regisztrációs nézetet a 7. feladat óta `@RolesAllowed("REGISTER")` védi.
- Az űrlapon névmező, fajválasztó és regisztrációs gomb található. A fajokat a `PetService.listSpecies()` adja; a megjelenő fajnevek I18n segítségével fordítódnak le.
- A `Binder<Pet>` kapcsolja össze a név- és fajmezőt a modell getter/setter metódusaival. Mindkét mező kötelező; a kizárólag szóközökből álló név is érvénytelen.
- Mentéskor a `writeBeanIfValid()` ellenőrzi a mezőket. Hibás adatok esetén magyar mezőhibák és figyelmeztető értesítés jelenik meg, a repository mentése nem fut le.
- Érvényes adatok esetén a `PetService.save()` elmenti az új állatot. A név elejéről és végéről eltávolítjuk a szóközöket. Sikeres mentés után az űrlap kiürül és visszajelzés jelenik meg. A listanézet újbóli megnyitásakor az új állat is megjelenik.
- Minden új felirat és validációs üzenet a `messages.properties` resource bundle-ben található. A nézetek böngészőlapcíme is fordításból érkezik.

## Kézi ellenőrzés a 3–5. feladathoz

1. Jelentkezzünk be `admin` / `password` adatokkal. A fejlécben az `admin` név, a listában az azonosító oszlop is jelenjen meg.
2. Jelentkezzünk ki a fejléc gombjával, majd lépjünk be `dog` / `password` adatokkal. Az azonosító oszlop ne jelenjen meg.
3. Lépjünk vissza az `admin` felhasználóval (a 7. feladat óta a regisztráció `REGISTER` szerepkört igényel). A regisztrációs oldalon próbáljunk üres űrlapot menteni, majd csak névvel, illetve csak fajjal menteni. Egyik esetben se keletkezzen új állat, és jelenjen meg figyelmeztetés.
4. Kizárólag szóközökből álló névvel és kiválasztott fajjal se lehessen menteni.
5. Adjunk meg érvényes nevet és fajt. Mentés után ürüljenek ki a mezők; a listára navigálva jelenjen meg az új állat.
6. Kijelentkezés után a `/list` és `/register` útvonal megnyitása ismét bejelentkezést igényeljen.

A fenti lépések ellenőrzési útmutatót jelentenek, nem igazolt teszteredményeket. A Maven fordítási kísérlet továbbra is a helyi JAR-erőforrások hozzáférési hibájával zárult; a teljes build és a böngészős működés ellenőrzése még szükséges.

# 6. feladat – Képernyők lokalizált címe

Mindhárom képernyőn már a korábbi feladatok során beállítottuk a lokalizált címet: a `LoginView`, a `ListView` és a `RegisterView` megvalósítja a `HasDynamicTitle` interfészt. A Vaadin a `getPageTitle()` visszatérési értékét használja a böngészőlap címeként. A metódus minden nézetben `getTranslation(...)` hívással, az aktuális lokalizáció alapján kéri le a címet a `messages` resource bundle-ből.

| Képernyő | Fordítási kulcs | Magyar cím |
| --- | --- | --- |
| `/login` | `title.login` | Bejelentkezés |
| `/list` és `/` | `title.pet.list` | Állat lista |
| `/register` | `title.pet.register` | Állat regisztrálás |

Példa a `ListView` megoldásából:

```java
@Override
public String getPageTitle() {
    return getTranslation("title.pet.list");
}
```

A címet nem rögzített szövegű `@PageTitle` annotáció adja, hanem az I18n fordítás. A `MainLayout` közös keret, nem önálló képernyő; az aktuális nézet határozza meg a címet. A jelenlegi fordítási szolgáltató magyar nyelvet támogat.

Ellenőrzés: a három nézetben a `HasDynamicTitle` megvalósítását és a hozzájuk tartozó fordítási kulcsok meglétét forráskód alapján ellenőriztem. Ehhez a feladathoz további Java-módosításra nem volt szükség. A böngészőben az egyes útvonalak megnyitásakor a fenti címeknek kell megjelenniük a lapfülön; ezt a futásidejű ellenőrzést még el kell végezni.

# 7. feladat – Jogosultság

## Global method security

A `SecurityConfig` osztályra felkerült az `@EnableMethodSecurity` annotáció. Ez a projekt Spring Security verziójában a global method security engedélyezésének korszerű módja; alapértelmezetten bekapcsolja a `@PreAuthorize` és `@PostFilter` annotációk feldolgozását. A szolgáltatást Spring dependency injection útján, a `PetService` interfészen keresztül kell használni, hogy a jogosultságellenőrzést végző proxy érvényesüljön.

## Regisztráció védelme

- A `PetServiceImpl.save()` és `listSpecies()` metódusait `@PreAuthorize("hasRole('REGISTER')")` védi. A Spring Security a `REGISTER` szerepkörhöz a `ROLE_REGISTER` authorityt ellenőrzi. A mentés és a regisztrációs fajlista jogosulatlan hívása hozzáférési hibát eredményez.
- A `MainLayout` a konstruktorban injektált `SecurityService` interfészen keresztül ellenőrzi a szerepköröket. A regisztrációs menüpont csak akkor látható, ha a `getRolesAsString()` eredménye tartalmazza a `REGISTER` szöveget.
- A `RegisterView` `@RolesAllowed("REGISTER")` annotációt kapott, így a `/register` cím közvetlen megnyitása is védett. A menüpont elrejtése mellett tehát az útvonal és a szolgáltatási metódusok is ellenőrzik a jogosultságot.

## Fajonként szűrt lista – bónusz

A `PetServiceImpl.listAll()` csak bejelentkezett felhasználónak hívható, és az alábbi annotáció szűri a visszaadott állatokat:

```java
@PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
```

A `securityService` a `SecurityServiceImpl` explicit beanneve, amelyen keresztül a kifejezés a `SecurityService.getRolesAsString()` metódust hívja. A `filterObject` az éppen vizsgált állat. A `species` enumot a `name()` alakítja Stringgé (például `CAT`), így a szerepköröket tartalmazó `List<String>` elemeivel azonos típust hasonlítunk össze.

A szűrés a repository által visszaadott lista másolatán történik; a más szerepkörű felhasználóknak megmaradnak az állatok. Az `ADMIN` önmagában nem ad hozzáférést minden fajhoz: a beállított admin felhasználónak külön megvan az összes fajhoz szükséges szerepköre.

Az induló felhasználói beállításokat megtartva az elvárt működés:

| Felhasználó | Látható fajok | Regisztrálhat? |
| --- | --- | --- |
| `admin` | minden faj | igen |
| `dog` | kutya | nem |
| `cat` | kutya | igen |
| `uncommon` | teknős, papagáj, vadászgörény | nem |

A `cat` felhasználónak az induló konfiguráció szerint `REGISTER` és `DOG` szerepköre van; a név nem határozza meg a jogosultságokat.

## Ellenőrzés

A `PetServiceSecurityTests` valódi Spring metódusbiztonsági proxyval vizsgálja a fajszűrést, több szerepkör együttes kezelését, a repository tartalmának megőrzését, a regisztrációs metódusok tiltását és engedélyezését. Külön eset ellenőrzi, hogy az `ADMIN` fajszerepkörök nélkül üres listát kap.

Futtatás Java 21-gyel: `./mvnw.cmd -Dtest=PetServiceSecurityTests test`.

A környezetben megkísérelt tesztfuttatás a fordítás előtt megállt: offline módban hiányzott a `plexus-java:1.4.0` Maven-függőség. A tesztek sikeres futása és a böngészős jogosultságellenőrzés ezért még nincs igazolva. Kézzel a fenti felhasználókkal ellenőrizhető a lista, a regisztrációs menüpont láthatósága és a `/register` útvonal védelme.
