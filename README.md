# AutoDviratis

Dviračių skelbimų agregatorius: skelbimai iš kelių šaltinių (Skelbiu.lt, Facebook) sujungiami į vieną sąrašą su filtrais ir rūšiavimu. VilniusTech kurso „Programų sistemų projektavimas“ II užduoties veikiantis prototipas.

Realizuotas vienas pilnas scenarijus – **skelbimų normalizavimo ir filtravimo modulis**:

1. Organizacija (kliento „tenant“) paspaudžia „Atnaujinti skelbimus“.
2. Šaltiniai grąžina „žalius“ skelbimus (prototipe – iš testinių JSON failų).
3. Ne dviračiai atmetami.
4. Iš pavadinimo ir aprašymo pagal taisykles ištraukiami parametrai (tipas, ratų dydis, rėmo dydis, stabdžiai, pavaros, rėmo medžiaga). Jei pavadinimas ir aprašymas prieštarauja – reikšmė „nenurodyta“ (niekada nespėjama).
5. Dublikatai tarp šaltinių sujungiami į vieną įrašą.
6. Rezultatas išsaugomas to kliento saugykloje.
7. Naudotojas filtruoja ir rūšiuoja sąrašą.

Jei vienas šaltinis neveikia, sistema nesustoja: rodo kitų šaltinių naujus skelbimus ir neveikiančio šaltinio ankstesnius duomenis su aiškiu pranešimu, kada jie paskutinį kartą atnaujinti.

## Įrankiai ir versijos

| Įrankis | Versija |
|---|---|
| JDK | 25 (kodas kompiliuojamas su `release` 21) |
| Maven | per wrapper (`mvnw.cmd`), Maven 3.9.11 |
| Spring Boot | 3.5.7 (web + Thymeleaf + data-jpa, t. y. Hibernate/JPA) |
| Duomenų bazė | H2, atmintyje |
| Testai | JUnit 5 (per `spring-boot-starter-test`) |

Papildomų bibliotekų (Lombok, Docker ir pan.) projektas nenaudoja.

## Konfigūracija

- Duomenų bazės diegti nereikia: H2 pasileidžia kartu su programa, atmintyje (`jdbc:h2:mem:autodviratis`).
- Programa veikia prievade **8080**: <http://localhost:8080>
- Nustatymai yra `src/main/resources/application.properties`.
- **Windows PKIX / SSL klaida** (Maven negali parsisiųsti priklausomybių): prieš paleidžiant nustatykite
  - `cmd`: `set MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`
  - PowerShell: `$env:MAVEN_OPTS="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`

## Paleidimas ir testai

```
mvnw.cmd spring-boot:run     (paleidimas, tada http://localhost:8080)
mvnw.cmd test                (visi testai)
```

Testai – JUnit 5, be Spring konteksto, be duomenų bazės, failų ir tinklo: viskas atmintyje su netikrais (fake) šaltiniais ir saugykla. Jie apima taisykles, dublikatų jungimą, filtravimą, servisą (įskaitant klaidos ir tenant izoliacijos scenarijus) ir šaltinių jungiklį.

## Pavyzdiniai duomenys ir klientai (tenant)

Programai startuojant `DataSeeder` sukuria du klientus ir kiekvienam padaro pradinį atnaujinimą:

| Tenant id | Pavadinimas | Adresas |
|---|---|---|
| `siauliu-dviraciai` | Šiaulių dviračių salonas | `/t/siauliu-dviraciai/listings` |
| `vilniaus-ratai` | Vilniaus dviračių klubas | `/t/vilniaus-ratai/listings` |

Šaltinių duomenys yra JSON failai `src/main/resources/data/{tenantId}/{šaltinis}.json`:

```
data/siauliu-dviraciai/skelbiu.json   data/siauliu-dviraciai/facebook.json
data/vilniaus-ratai/skelbiu.json      data/vilniaus-ratai/facebook.json
```

**Šiaulių klientas**
- Skelbiu.lt: Trek Marlin 7 29" M – 450 €, Kross Level 3 27,5" S – 380 €, Specialized Rockhopper 29" L – 620 €.
- Facebook: Trek Marlin 7 29" dydis M – 440 € (tas pats miestas ir `photoHash` kaip Skelbiu.lt Trek – dublikatas), „Dviračio padangos 29"“ (ne dviratis – atmetamas), Giant Talon (pavadinime 29", aprašyme 27,5" – prieštaravimas, ratai „nenurodyta“).
- Po atnaujinimo sąraše turi būti 4 skelbimai.

**Vilniaus klientas** – kiti skelbimai, tuose pačiuose šaltiniuose, pagal 3 iš kiekvieno (iš viso 6, be dublikatų): Cannondale Trail 5, Merida Scultura 400 (plento), Kellys Spider, Giant Defy Advanced (plento), Cube Aim, Pelago (miesto).

## Demonstravimo žingsniai

Paleiskite programą ir atidarykite <http://localhost:8080> – pasirinkite klientą.

**1. Pagrindinis scenarijus** (Šiaulių klientas)
1. Viršuje matysite šaltinių pranešimus, pvz. „Skelbiu.lt: duomenys atnaujinti … (3 skelbimai)“ (pradinis atnaujinimas įvyksta programos starto metu). „Atnaujinti skelbimus“ galima paspausti dar kartą.
2. Filtre pasirinkite: tipas **Kalnų**, ratai **29**, kaina iki **500**, miestas **Šiauliai** → „Filtruoti“.
3. Lieka tik **Trek**. Kross (27,5"), Specialized (620 €) ir Giant (ratai „nenurodyta“) neįtraukiami.

**2. Dublikatas**
- Šiaulių sąraše Trek yra vienas įrašas, nors yra dviejuose šaltiniuose: kaina **440 €** (mažesnė iš 450 € ir 440 €) ir **2 šaltinių nuorodos** (Skelbiu.lt ir Facebook).

**3. Šaltinio klaida**
1. Šaltinių sąraše prie Skelbiu.lt paspauskite „Išjungti (demo)“, tada „Atnaujinti skelbimus“.
2. Sąrašas lieka (Skelbiu.lt skelbimai rodomi iš ankstesnių duomenų), o viršuje paryškintu tekstu rodoma: „**Skelbiu.lt nepasiekiamas. Rodomi ankstesni duomenys, paskutinį kartą atnaujinti YYYY-MM-DD HH:mm.**“ (data ir laikas – paskutinio sėkmingo atnaujinimo).
3. Jungiklis veikia atskirai kiekvienam klientui: Vilniaus kliente Skelbiu.lt lieka įjungtas.
4. Paspaudus „Įjungti“ ir „Atnaujinti skelbimus“, pranešimas vėl rodo „duomenys atnaujinti“.

**4. Tenant atskyrimas**
1. Atidarykite `/t/vilniaus-ratai/listings` – matote tik Vilniaus skelbimus, Šiaulių skelbimų nėra (ir atvirkščiai).
2. Svetimo skelbimo slėpimas (naršyklėje tokio mygtuko nėra, todėl siunčiama `POST` užklausa ranka):
   1. Vilniaus kliento puslapyje (`/t/vilniaus-ratai/listings`) skelbimų lentelės pirmame stulpelyje **ID** raskite bet kurio Vilniaus skelbimo numerį. Pavyzdžiui, jei tai **5**.
   2. Naujame terminale (PowerShell arba `cmd`) išsiųskite tą ID su **Šiaulių** kliento adresu. Rašykite `curl.exe`, ne `curl`, nes PowerShell `curl` yra kitos komandos pavadinimas:
      ```
      curl.exe -i -X POST http://localhost:8080/t/siauliu-dviraciai/listings/5/hide
      ```
      (vietoje `5` įrašykite savo rastą ID).
   3. Rezultatas – `HTTP/1.1 404` ir puslapis su tekstu „Skelbimas nerastas: jis nepriklauso šiam klientui arba jau paslėptas.“
   4. Atnaujinkite `/t/vilniaus-ratai/listings` – to skelbimo eilutė lieka nepakeista.
3. Nežinomas klientas (pvz. `/t/nera/listings`) – 404 „Klientas nerastas“.

**Kiti dalykai, kuriuos galima parodyti**
- Rūšiavimas pagal kainą didėjančia / mažėjančia.
- „Nenurodyta“ filtruojamame lauke skelbimas neįtraukiamas; pažymėjus „Įtraukti skelbimus su nenurodytomis reikšmėmis“ – įtraukiamas.
- Neegzistuojant jokiems duomenims (visi šaltiniai nepasiekiami ir nėra ankstesnių) rodoma „Duomenų gauti nepavyko“; jei filtras nieko nerado – „Skelbimų nerasta“.

## Projekto struktūra

Paketas `lt.vilniustech.autodviratis`:

| Paketas | Paskirtis |
|---|---|
| `model` | Entities (`Tenant`, `Listing`, `SourceLink`, `StoredSnapshot`), `RawListing` (skelbimas iš šaltinio) ir enum'ai (`BikeType`, `BrakeType`, `FrameMaterial`, `SortOrder` su `NOT_SPECIFIED`) |
| `rules` | Parametrų taisyklės (`ParameterRule<T>`: ratai, rėmo dydis, stabdžiai, pavaros, rėmo medžiaga, tipas) ir `ConflictResolver` (pavadinimas prieštarauja aprašymui → nenurodyta). Gryna Java |
| `service` | Pagrindinė logika, gryna Java: `ListingNormalizer`, `DuplicateMerger`, `ListingFilter`, `ListingService` (atnaujinti, ieškoti, slėpti, šaltinių būsenos), `TenantService`, `SourceStatus` (pranešimų tekstai), sąsajos `ListingSource`, `ListingRepository`, `TenantRepository`, išimtys |
| `sources` | `ListingSource` realizacijos: `JsonFileListingSource` (skaito JSON), `SwitchableListingSource` (demo jungiklis kiekvienam klientui) |
| `hibernate` | `GenericHibernate` (bendras CRUD), `CustomHibernate` (paieška su `CriteriaBuilder`, visada su tenant sąlyga), `HibernateListingRepository`, `HibernateTenantRepository` |
| `utils` | `JsonFileReader`, `JsonListing` |
| `web` | `ListingController` (Thymeleaf) ir puslapių pagalbinės klasės |
| `config` | `AppConfig` (visi objektai surenkami per konstruktorius), `DataSeeder` |

Kiti failai: `src/main/resources/templates` (`index.html`, `listings.html`, `error.html`), `static/style.css`, `data/` (JSON), `src/test/java` (testai). Pilnas projekto planas – `PLAN.md`.

Pagrindiniai principai: `service` priklauso tik nuo sąsajų (`ListingSource`, `ListingRepository`, `TenantRepository`), Spring anotacijos naudojamos tik `web` ir `config`, o kiekvienas `ListingRepository` metodas turi `Tenant` parametrą (kliento duomenų izoliacija).

## Parametrų atpažinimas

- **Ratai:** skaičius su `"` arba „colių“ (pvz. 29", 27,5 colių); leidžiama 12–29, kitaip „nenurodyta“.
- **Rėmo dydis:** XS / S / M / L / XL arba „54 cm“ šalia „rėmas“ / „dydis“.
- **Stabdžiai:** „diskiniai“ → diskiniai, „hidrauliniai“ → hidrauliniai, „V-brake“ → V-brake.
- **Pavaros:** skaičius šalia „pavarų“ / „bėgių“.
- **Rėmo medžiaga:** aliuminis, plienas, karbonas.
- **Tipas:** kalnų, plento, miesto, vaikiškas.
- **Ne dviratis:** skelbimas atmetamas, jei tekste nėra „dviratis“ / „dviračiai“ ir nerastas joks dviračio tipas, arba pavadinime yra dalių žodžiai (padangos, šalmas, laikiklis, pedalai, krepšys ir pan.) ir nėra pagrindinio daiktavardžio „dviratis“ / „dviračiai“ (vardininko).

## Dublikatų taisyklė

Slenksčiai yra konstantos klasėje `DuplicateMerger`. Du skelbimai laikomi dublikatais, jei **to paties kliento**, **skirtingų šaltinių** ir **tas pats miestas** (be didžiųjų/mažųjų raidžių skirtumo; tuščias miestas niekada nelaikomas tuo pačiu), ir:

1. **Pagrindinė taisyklė** – abu turi `photoHash`: dublikatas, jei `photoHash` vienodas (skirtingas → ne dublikatas).
2. **Atsarginė taisyklė** – bent vienas neturi `photoHash`: dublikatas, jei
   - pavadinimų panašumas (Jaccard: bendri žodžiai / visi skirtingi žodžiai, po mažųjų raidžių, be skyrybos ir be žodžių „dviratis“, „parduodu“) **≥ 0,80**, IR
   - kainų skirtumas **≤ 10 %** mažesnės kainos: |k1 − k2| / min(k1, k2).

**Sujungto įrašo** kaina – **mažesnė** iš kainų; išsaugomos visos šaltinių nuorodos. Kiti laukai imami iš pirmo skelbimo (skelbimai jungiami šaltinių pavadinimų abėcėlės tvarka, todėl Šiaulių Trek pavadinimas rodomas iš Facebook skelbimo); jei pirmo skelbimo laukas „nenurodyta“, o kito turi reikšmę – imama turinti reikšmė.

Kiekvienas atnaujinimas sujungtą sąrašą sudaro iš nulio, iš visų šaltinių paskutinių duomenų kopijų, todėl pakartotinis atnaujinimas dublikatų nekaupia ir nelieka senos kainos.

## Filtravimas

Skelbimas turi atitikti **visus** pasirinktus filtrus (tipas, ratai, kaina nuo / iki, miestas). Kainos ribos – imtinai. Skelbimas su „nenurodyta“ filtruojamame lauke neįtraukiamas, nebent pažymėta „Įtraukti skelbimus su nenurodytomis reikšmėmis“. Miestas lyginamas be didžiųjų/mažųjų raidžių skirtumo.

## Žinomi apribojimai

- Šaltiniai – **testiniai JSON failai**, tikro skelbimų skaitymo (scraping) nėra; nuotraukų `photoHash` – tiesiog laukas duomenyse.
- **Paslėpimas galioja iki kito atnaujinimo**: „Paslėpti“ pašalina skelbimą iš dabartinio sąrašo, bet po „Atnaujinti skelbimus“ jis grįžta.
- **Duomenys laikomi atmintyje** (H2): paleidus programą iš naujo viskas sukuriama iš nulio, o šaltinių pranešimai nuo pradinio atnaujinimo atsiranda iš naujo.
- Šaltinio demo jungiklis taip pat grįžta į „įjungta“ paleidus iš naujo.
- Svetimo skelbimo slėpimą naršyklėje parodyti galima tik rankiniu `POST` užklausos siuntimu (pvz. `curl.exe`), nes puslapis siūlo slėpti tik to kliento skelbimus.
- Filtras po „Paslėpti“ ir šaltinio perjungimo išsivalo (nukreipiama į sąrašo adresą be filtro parametrų).
- **Login, rolės, registracija** neįgyvendinti – paliekami III etapui. Klientas parenkamas pagal adresą (`/t/{tenantId}/...`).
