### 1. YLEISKUVAUS JA ARKKITEHTUURI

Sovellus tarjoaa minimaalisen REST rajapinnan kirjojen (Book) luomiseen ja hakemiseen.

* Backend: Java 21 ja Spring Boot
* Tietokanta: PostgreSQL
* ORM: Spring Data JPA
* Infrastruktuuri: Docker, Docker Compose, GitHub Actions ja CSC cPouta pilvipalvelu

Tämän projektin pääasiallinen tarkoitus on demonstroida modernia ohjelmistotuotantoa, saumatonta automaatiota ja CI/CD toimintaa oikeassa pilviympäristössä. Tästä syystä itse ohjelman liiketoimintalogiikka on pidetty tarkoituksella erittäin kevyenä ja selkeänä, jotta huomio kiinnittyy infrastruktuurin ja julkaisuputken arkkitehtuuriin.


Itse ohjelman sisäinen toiminta on jaettu kolmeen selkeään komponenttiin:

* **Tietomalli:** Järjestelmän pohjana toimii Book luokka. Se on määritelty entiteetiksi, joka vastaa suoraan tietokannan taulua. Jokaisella kirjalla on automaattisesti generoituva id tunnus, otsikko ja kirjailija.
* **Tietokantakerros:** BookRepository rajapinta hoitaa kaiken kommunikaation tietokannan kanssa. Koska se laajentaa JpaRepository luokkaa, sovellus osaa automaattisesti tallentaa ja etsiä tietoa ilman ainuttakaan manuaalisesti kirjoitettua SQL komentoa.
* **Rajapinta:** BookController luokka tarjoaa päätepisteet ulkomaailmalle osoitteessa /api/books. Se ottaa vastaan GET pyyntöjä kirjojen listaamiseksi ja POST pyyntöjä uusien kirjojen luomiseksi. Kontrolleri delegoi näiden pyyntöjen käsittelyn suoraan repositoryn vastuulle.

### 2. PAIKALLINEN KEHITYS (LOCAL DEVELOPMENT)

Näillä ohjeilla saat projektin pyörimään omalle koneellesi välittömästi.

### Esivaatimukset
Koneellasi tulee olla asennettuna seuraavat työkalut:
* **Git** (versionhallintaan)
* **Docker Desktop** (konttien ajamiseen)
* **Java 21** (paikalliseen koodaukseen)

---

### A. Kloonaa repositorio
Lataa projekti omalle koneellesi ja siirry projektikansioon:
```bash
git clone https://github.com/fransVuori/csc-cicd.git
cd csc-cicd
```

### B. KÄYNNISTÄ KEHITYSYMPÄRISTÖ:
Paikallinen ympäristö käynnistää sekä Spring Boot sovelluksen että PostgreSQL
tietokannan valmiiksi kytkettynä.
```bash
docker compose -f docker-compose.dev.yml up -d --build
```

### C. TESTAA RAJAPINTAA: 
* **API vastaa nyt paikallisesti osoitteessa:**
http://localhost:8080/api/books

Voit kokeilla tietojen lisäämistä komentoriviltä **PowerShellillä** tai jollain API testaus työkalulla (esim. Postman):
```bash
Invoke-RestMethod -Uri http://localhost:8080/api/books -Method Post -Body '{"title":"Uusi kirja","author":"Kehittäjä"}' -ContentType "application/json"
```

### D. SAMMUTA YMPÄRISTÖ: 
```bash
docker compose -f docker-compose.dev.yml down
```

### 3. KONFIGURAATIOPROFIILIT (SPRING PROFILES)

Projektissa on käytössä kolme erillistä ympäristöprofiilia. Näin varmistamme,
että kehityksen, testauksen ja tuotannon tietokannat ja asetukset pysyvät
tiukasti erillään.

* **`dev` (application-dev.yml):**
Kehitysprofiili. Yhdistää Dockerin sisäiseen dev-db tietokantaan. Tämä aktivoituu
automaattisesti docker-compose.dev.yml tiedostossa ympäristömuuttujalla
SPRING_PROFILES_ACTIVE=dev. Hibernate luo ja päivittää tietokannan taulut
automaattisesti lennosta.

* **`test` (application-test.yml):**
Automaattisten testien profiili. Yhdistää paikallisen koneen porttiin 5433.
Tietokanta luodaan tyhjästä ja tuhotaan jokaisen testiajon yhteydessä.

* **`prod` (application-prod.yml):**
Tuotantoprofiili. Yhdistää tuotantopalvelimen db konttiin. Aktivoituu tuotannon
docker-compose.yml tiedostossa. Tietokannan tunnukset ja salasanat luetaan
turvallisesti GitHub Actionsin salaisuuksista (Secrets) ja injektoidaan
ympäristömuuttujina julkaisuputken kautta.

### 4. TESTAUS JA TESTIPROFIILI

Automaattiset testit ajetaan aina erillistä testitietokantaa vasten. Erillinen testiprofiili on ohjelmistotuotannossa kriittisen tärkeä: sen ansiosta testiajo voi vapaasti luoda ja poistaa testidataa ilman vaaraa siitä, että kehittäjän oma paikallinen data ylikirjoittuu.

### Miten testaus toimii tässä projektissa

Projektin testaus on toteutettu Spring kehyksen sisäänrakennetuilla työkaluilla. Pääasiallinen testiluokka on BookControllerTest, joka varmistaa rajapinnan toiminnan päästä päähän.

* **Kontekstin lataus:** Annotaatio `@SpringBootTest` käynnistää sovelluksen taustajärjestelmän testiajoa varten.
* **Oikea ympäristö:** Annotaatio `@ActiveProfiles("test")` pakottaa sovelluksen käyttämään testiprofiilin asetuksia ja yhdistämään paikalliseen testikantaan.
* **Rajapinnan simulointi:** Työkalu nimeltä `MockMvc` mahdollistaa HTTP pyyntöjen simuloinnin ohjelmallisesti.

### Nykyisen testin kulku

Testimetodi `shouldCreateAndReturnBook` suorittaa kaksi tärkeää vaihetta peräkkäin:

1. Se lähettää POST pyynnön ja JSON muotoisen kirjan datan rajapintaan. Testi tarkistaa heti perään, että palvelin vastaa tilakoodilla 200 OK ja palauttaa juuri luodun kirjan tiedot oikein.
2. Tämän jälkeen se lähettää GET pyynnön hakeakseen kaikki kirjat ja varmistaa, että äsken lisätty kirja löytyy listan ensimmäisenä.

Näin ajat testit paikallisesti:

### A. Käynnistä erillinen testitietokanta taustalle:
```bash
docker compose -f docker-compose.test.yml up -d
```
### B. Aja testit Mavenilla testiprofiilia käyttäen:
```bash
./mvnw test "-Dspring.profiles.active=test"
```
### C. Sammuta testikanta, kun olet valmis:
```bash
docker compose -f docker-compose.test.yml down
```
### 5. CI/CD PUTKI JA TUOTANTODEPLOY

Projektissa on täysin automatisoitu julkaisuputki, joka on toteutettu
GitHub Actionsilla (deploy.yml).

PUTKEN TOIMINTALOGIIKKA:

Koodin uusi versio pusketaan main haaraan.

Putki luo tilapäisen tietokannan ja ajaa automaattiset testit.

Kun testit menevät läpi, sovelluksesta rakennetaan uusi Docker image.

Image julkaistaan Docker Hub container registryyn.

Putki ottaa suojatun SSH yhteyden CSC cPouta virtuaalikoneelle, siirtää sinne
tuotannon docker-compose.yml tiedoston, lataa uusimman imagen ja käynnistää
palvelun päivitetyllä versiolla.

### 6. TUOTANTOYMPÄRISTÖ (CSC CPOUTA)

Tuotannossa sovellus ja tietokanta pyörivät virtuaalikoneella.
Rajapinta on julkisesti saatavilla osoitteessa:

http://195.148.23.182:8080/api/books

### Rajapinnan testaaminen tuotannossa (PowerShell):

Tällä komennolla voit lisätä uuden kirjan tuotantokantaan suoraan omalta koneeltasi. Komento on muotoiltu tukemaan myös skandinaavisia erikoismerkkejä.

```bash
$body = @{
    title = "Nimi"
    author = "Tekijä"
} | ConvertTo-Json -Compress

Invoke-RestMethod -Uri http://195.148.23.182:8080/api/books -Method Post -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -ContentType "application/json; charset=utf-8"
```

## Sovelluslogiikka ja testausstrategia

Tämä luku syventyy sovelluksen sisäiseen ohjelmointimalliin sekä siihen, miten automaattinen laadunvarmistus on toteutettu Spring-kehyksen sisällä.

### Spring Boot -arkkitehtuuri ja riippuvuuksien hallinta

Ohjelma noudattaa kerrosarkkitehtuuria, jossa vastuut on jaettu selkeästi eri komponenttien välille. Sovellus hyödyntää Springin ydintoimintoihin kuuluvaa riippuvuuksien injektointia (Dependency Injection). `BookController` käyttää modernia konstruktori-injektiota, mikä takaa sen, että sovellus on helpommin testattavissa ja riippuvuudet ovat muuttumattomia (immutable). Kun pyyntö saapuu rajapinnalle, Spring Bootin sisäinen `Jackson`-kirjasto muuntaa saapuvan JSON-datan automaattisesti Java-olioiksi `@RequestBody`-annotaation avulla.


### Tietokantatransaktiot ja JPA

Tiedon tallennus on toteutettu abstraktiotasolla, joka erottaa sovelluslogiikan ja SQL-kyselyt toisistaan. `BookRepository` toimii rajapintana, joka tarjoaa sovellukselle pääsyn tietokantaan ilman, että kehittäjän tarvitsee kirjoittaa manuaalisia tietokantahakuja. Spring Data JPA hallitsee entiteettien elinkaarta ja varmistaa, että tietokantayhteydet avataan ja suljetaan oikeaoppisesti. Rajapinnassa käytetty `ResponseEntity`-luokka mahdollistaa tarkan kontrollin HTTP-vastauskoodeihin, mikä tekee rajapinnasta ennustettavan ja helposti kulutettavan muille sovelluksille.

### Automaattinen testaus Spring-ympäristössä

Testaus on integroitu osaksi Spring-kontekstia, mikä mahdollistaa sovelluksen toiminnan todentamisen mahdollisimman lähellä aitoa käyttötilannetta. Testeissä hyödynnetään seuraavia periaatteita:

* **Integraatiotestaus**: Sovellus käynnistää osittaisen tai täyden Spring-kontekstin, jolloin voidaan varmistaa, että kontrollerit ja tietokantakerros kommunikoivat keskenään virheettömästi.
* **Testiprofiilin eristäminen**: Testiajon aikana käytetään `application-test.yml` -tiedoston asetuksia, mikä ohjaa sovelluksen käyttämään erillistä testitietokantaa.
* **Tietokannan hallinta testeissä**: Hibernate on määritetty tilaan `create-drop`, mikä tarkoittaa, että tietokantataulut luodaan puhtaalta pöydältä ennen testejä ja hävitetään heti niiden jälkeen. Tämä takaa sen, että jokainen testi on riippumaton muista eikä vanha data vääristä tuloksia.
* **REST-rajapintojen validointi**: Testit simuloivat HTTP-pyyntöjä ja tarkistavat, että palvelin palauttaa oikeat otsakkeet, tilakoodit sekä odotetun JSON-rakenteen.

Tämä lähestymistapa varmistaa, että koodiin tehtävät muutokset eivät rikkoo olemassa olevia toiminnallisuuksia ja että sovellus käyttäytyy loogisesti myös poikkeustilanteissa.