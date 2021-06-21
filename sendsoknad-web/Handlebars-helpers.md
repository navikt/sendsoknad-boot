# Handlebars-helpers
> Denne Markdown-filen genereres automatisk og endringer her vil overskrives.
> Oppdater Handlebars-helpers.hbs dersom du ønsker permanente endringer

For å generere oppsummeringerdokumenter som HTML, PDF eller XML har vi over tid bygget og registert en rekke
helpers til Handlebars som gjør det enklere å jobbe med innholdet i søknaden.
Denne fila innehoder info dersom man vil lage en ny helper til Handlebars eller lurer på 
hvilke helpers som er registert i dag.

#### Ny versjon av dokumentasjon i Handlebars-helpers.md

I testfila `RegistryAwareHelperTest` finnes det en testmetode `skrivRegisterteHelpersTilReadme` 
som kjører genererer denne fila på nytt og tar med eventuelle nye helpers. Denne kan kjøres individuelt, 
men kjører også automatisk sammen med det vanlige testoppsettet.

#### Ny helper

* Opprette en Spring-annotert klasse (`@Component`) `HelpernavnHelper.java` i pakken `no.nav.sbl.dialogarena.service.helpers`
* La klassen arve fra `RegistryAwareHelper<T>`
* Implementer de abstrakte metodene
    * Navnet på helperen må returneres fra `getNavn`
    * Beskrivelsen fra `getBeskrivelse` vil havne i denne fila som dokumentasjon
* Lag et eksempel på bruk under `/readme/helpernavn.hbs`. Denne vil også bli inkludert i dokumentasjonen under.
* Legg til tester

På dette formatet er det superklassen `RegistryAwareHelper` som vil registere helperen som er opprettet på
Handlebars-instansen som brukes for å generere oppsummeringsdokumenter.

## Eksisterende helpers

Tidlgere ble helpers laget som metoder rett i `HandleBarKjoerer.java` hvor de ble
registert inn eksplisitt via `handlebars.registerHelper("helpernavn", helpermetode())`,.

#### Statisk liste over helpers på gammelt registeringsformat
 
* formatterFodelsDato (deprecated og erstattet av formatterKortDato og formatterFnrTilKortDato)
* skalViseRotasjonTurnusSporsmaal

#### Helpers på nytt registreringsformat

* concat - Legger sammen alle parametrene til tekststring
* fnrTilKortDato - Formatterer et gyldig fødselnummer til dato på formatet dd.mm.aaaa
* forBarnefakta - Itererer over alle fakta som har den gitte keyen og parentfaktum satt til nærmeste faktum oppover i context.
* forFakta - Finner alle fakta med en gitt key og setter hvert faktum som aktiv context etter tur. Har inverse ved ingen fakta.
* forFaktaMedPropertySattTilTrue - Finner alle fakta med gitt key som har gitt property satt til true
* forFaktum - Finner et faktum og setter det som aktiv context. Har også inverse om faktum ikke finnes. 
* forFaktumHvisSant - Sjekker om faktumet til den innsendte keyen er sant eller ikke, setter faktumet som context
* forFaktumMedId - Returnerer et faktum med den gitte ID-en
* forFaktumTilknyttetBarn - Returnerer faktumet tilknyttet barnet i parent-context.
* forIkkeInnsendteVedlegg - Itererer over vedlegg som ikke er sendt inn
* forInfotekst - Itererer over alle infotekster med gyldig constraint på faktumstrukturen
* forInnsendteVedlegg - Itererer over innsendte vedlegg på søknaden
* forSortertProperties - Itererer over alle properties sortert
* forVedlegg - Lar en iterere over alle påkrevde vedlegg på en søknad
* formaterDato - Formaterer en innsendt dato på et gitt format som også sendes inn
* formaterLangDato - Gjør en datostreng om til langt, norsk format. F. eks. '17. januar 2015'
* harBarnetInntekt - Henter summen hvis barnet har inntekt. Må brukes innenfor en #forFaktum eller #forFakta helper. 
* hentFaktumValue - Returnerer verdien til et faktum tilhørende keyen som sendes inn
* hentLand - Henter land fra Kodeverk basert på landkode.
* hentMiljovariabel - Finner miljovariabel fra key
* hentPoststed - Henter poststed for et postnummer fra kodeverk
* hentSkjemanummer - Setter inn søknadens skjemanummer
* hentTekst - Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.
* hentTekstMedFaktumParameter - Henter tekst fra cms for en gitt key, med verdien til et faktum som parameter. Faktumet hentes basert på key
* hvisFaktumstrukturHarInfotekster - Sjekker om man har definert infotekster på faktumstrukturen for faktum på context
* hvisFlereErTrue - Finner alle fakta med key som begynner med teksten som sendes inn og teller om antallet med verdien true er større enn tallet som sendes inn.
* hvisHarDiskresjonskode - Viser innhold avhengig av om personalia indikerer diskresjonskode 6 (fortrolig) eller 7 (strengt fortrolig)
* hvisHarIkkeInnsendteDokumenter - Sjekker om søknaden har ikke-innsendte vedlegg
* hvisHarInnsendteDokumenter - Sjekker om søknaden har ett eller flere innsendte dokumenter
* hvisIkkeTom - Dersom variabelen ikke er tom vil innholdet vises
* hvisIngenSynligeBarneFakta - For bruk i generisk oppsummering, undersøker innsendt liste over fakta og ser om alle er skjult.
* hvisIngenSynligeBarneFaktaForGruppe - For gruppe-template brukt for sosialhjelp, der vi ønsker utvidet definisjon av hva som er synlige barnefakta mtp utvidet søknad
* hvisKunStudent - Sjekker om brukeren har en annen status enn student (f.eks sykmeldt, i arbeid osv.)
* hvisLik - Sjekker om to strenger er like
* hvisMer - Evaluerer en string til double og sjekker om verdien er mer enn grenseverdien gitt ved andre inputparameter
* hvisMindre - Evaluerer en string til integer og sjekker om verdien er mindre enn andre inputparameter
* hvisNoenAvkryssetBarneFakta - For bruk i generisk oppsummering, undersøker innsendt liste over fakta og ser om noen er avkrysset.
* hvisSant - Dersom variabelen er "true" vil innholdet vises
* hvisTekstFinnes - Henter tekst fra cms, prøver med søknadens prefix + key, før den prøver med bare keyen. Kan sende inn parametere.
* kortDato - Formatterer en datostreng på formatet yyyy-mm-dd til dd.mm.aaaa
* lagKjorelisteUker - Bygger en nestet liste over uker for et betalingsvedtak, der ukene inneholder dager det er søkt for refusjon.
* property - Returnerer verdien til gitt property på modellen i context, gitt at den er propertyaware
* sendtInnInfo - Tilgjengeliggjør informasjon om søknaden (innsendte vedlegg, påkrevde vedlegg og dato)
* toCapitalized - Gjør om en tekst til at alle ord starter med store bokstaver
* toLowerCase - Gjør om en tekst til kun små bokstaver
* variabel - Lager en variabel med en bestemt verdi som kun er tilgjengelig innenfor helperen
* vedleggCmsNokkel - Henter teksten for et vedlegg
* visCheckbox - hvis value er "true" eller key.false-teksten finnes


#### Eksempler

##### concat

```
{{ concat "a" "b" "c" "d" }}
```


##### fnrTilKortDato

```
{{fnrTilKortDato "10108000398"}}
```


##### forBarnefakta

```
må ha et faktum i context, f. eks. via
{{#forFaktum "parentFaktumKey"}}

    {{#forBarnefakta "key"}}
        itererer over fakta her, {{value}}
    {{else}}
        ingen fakta matchet
    {{/forBarnefakta}}

{{/forFaktum}}
```


##### forFakta

```
{{#forFakta "faktumKey"}}
   Faktum {{index}} med key "faktumKey" har value {{value}}
{{else}}
   Faktalisten er tom, det finnes ingen faktum med key "faktumKey".
{{/forFakta}}
```


##### forFaktaMedPropertySattTilTrue

```
{{#forFaktaMedPropertySattTilTrue "faktumnavn" "propertyKey"}}
    Faktumet "faktumnavn" har har propertien "propertyKey" og den satt til true.
{{else}}
    Faktumet har ikke property satt til true (enten false eller ikke noe).
{{/forFaktaMedPropertySattTilTrue}}
```


##### forFaktum

```
{{#forFaktum "faktumNavn"}}
    Faktum med key {{key}} finnes og kan aksesseres. {{value}} skriver f.eks ut verdien på faktumet. se Faktum klassen.
{{else}}
    faktum med key "faktumNavn" er ikke satt
{{/forFaktum}}
```


##### forFaktumHvisSant

```
{{#forFaktumHvisSant "key2"}}
    sant, har faktumet som context
{{else}}
    ikke sant, har faktumet som context
    ELLER
    faktum eller value fantes ikke
{{/forFaktumHvisSant}}
```


##### forFaktumMedId

```
{{#forFaktumMedId "faktumId"}}
    Faktum med id {{faktumId}} finnes og kan aksesseres. {{value}} skriver f.eks. ut verdien på faktumet. Se Faktum-klassen.
{{else}}
    Faktum med id "faktumId" finnes ikke.
{{/forFaktumMedId}}
```


##### forFaktumTilknyttetBarn

```
{{#forFaktum "barn"}}
    {{#forFaktumTilknyttetBarn "faktumNavn"}}
        Her har du faktumet (med gitt key) som er tilknyttet barnet gitt i parent-context (forFaktum).
        Se forFaktum for å vite med om hvordan faktumobjektet fungerer.
    {{else}}
        Om det ikke finnet noe faktum som er tilknyttet barnet vil den gå inn i else-contexten.
    {{/forFaktumTilknyttetBarn}}
{{/forFaktum}}
```


##### forIkkeInnsendteVedlegg

```
{{#forIkkeInnsendteVedlegg}}
    ikke innsendt: {{navn}}
{{else}}
    Ingen ikke-innsendte vedlegg
{{/forIkkeInnsendteVedlegg}}
```


##### forInfotekst

```
{{ forInfotekst }}
    {{{ hentTekst key }}}
{{ forInfotekst }}

```


##### forInnsendteVedlegg

```
{{#forInnsendteVedlegg}}
    innsendt: {{navn}}
{{else}}
    Ingen innsendte vedlegg
{{/forInnsendteVedlegg}}
```


##### forSortertProperties

```
{{#forSortertProperties faktum}}
    {{key}}: {{values}}
{{else}}
    Ingen properties
{{/forSortertProperties}}
```


##### forVedlegg

```
{{#forVedlegg}}
    vedlegg: {{navn}}

    {{#hvisLik innsendingsvalg "LastetOpp"}}
        lastet opp
    {{ else }}
        ikke lastet opp
    {{/hvisLik}}
    + andre verdier som ligger på vedleggene
{{else}}
    Ingen vedlegg
{{/forVedlegg}}
```


##### formaterDato

```
{{formaterDato "2015-09-16" "EEEE"}}
{{formaterDato variabel "d. MMMM YYYY"}}
```


##### formaterLangDato

```
{{formaterLangDato "2015-09-16"}}
{{formaterLangDato variabel}}
```


##### harBarnetInntekt

```
{{#forFaktum "faktumNavn"}}
    {{#harBarnetInntekt}
        Gitt at wrapper-faktumet "faktumNavn" har to barnefaktum: "barnet.harinntekt" hvor verdi er "true", og
        "barnet.inntekt". Sistnevnte er tilgjengelig her slik at {{value}} skriver ut inntekten.
    {{else}}
        Barnet har ikke inntekt.
    {{/harBarnetInntekt}}
{{/forFaktum}}

```


##### hentFaktumValue

```
{{hentFaktumValue "faktum.key"}}
```


##### hentLand

```
{{hentLand "NOR"}}
{{hentLand variabel}}
```


##### hentMiljovariabel

```
 {{hentTekst "personalia.intro" (hentMiljovariabel "soknad.brukerprofil.url")}}

 {{hentMiljovariabel "soknad.brukerprofil.url"}}
```


##### hentPoststed

```
{{hentPoststed "2233"}}
{{hentPoststed variabel}}
```


##### hentSkjemanummer

```
{{hentSkjemanummer}}
```


##### hentTekst

```
{{hentTekst "min.key" "param1" "param2"}}
{{hentTekst "min.key.uten.params"}}
```


##### hentTekstMedFaktumParameter

```
{{hentTekstMedFaktumParameter "cms.key" "faktum.key"}}
```


##### hvisFaktumstrukturHarInfotekster

```
{{#forFaktum "faktumNavn"}}
    {{#hvisFaktumstrukturHarInfotekster}}
        Hvis faktumstrukturen definerer en infotekst kommer man inn i denne blokken
    {{else}}
        Hvis ikke kommer vi inn hit
    {{/hvisFaktumstrukturHarInfotekster}}
{{/forFaktum}}
```


##### hvisFlereErTrue

```
{{#hvisFlereErTrue "min.key" "2" }}
    flere enn 2 er true
{{else}}
    ikke nok true
{{/hvisFlereErTrue}}
```


##### hvisHarDiskresjonskode

```
{{#hvisHarDiskresjonskode}}
    Jeg har diskresjonskode
    {{else}}
    jeg har IKKE noen diskresjonskode
{{/hvisHarDiskresjonskode}}
```


##### hvisHarIkkeInnsendteDokumenter

```
{{#hvisHarIkkeInnsendteDokumenter}}
    har ikke-innsendte dokumenter
{{else}}
    alt er innsendt
{{/hvisHarIkkeInnsendteDokumenter}}
```


##### hvisHarInnsendteDokumenter

```
{{#hvisHarInnsendteDokumenter}}
    har ett eller flere innsendte dokumenter
{{else}}
    har ikke sendt inn noen dokumenter
{{/hvisHarInnsendteDokumenter}}
```


##### hvisIkkeTom

```
{{#hvisIkkeTom "verdi"}}
    Verdien er ikke tom
{{else}}
    Verdien er tom
{{/hvisIkkeTom}}
```


##### hvisIngenSynligeBarneFakta

```
{{#hvisIngenSynligeBarneFakta fakta utvidetSoknad}}
    Ingen synlige fakta
{{/hvisIngenSynligeBarneFakta}}

```


##### hvisIngenSynligeBarneFaktaForGruppe

```
{{#hvisIngenSynligeBarneFaktaForGruppe fakta}}
    Ingen synlige fakta
{{/hvisIngenSynligeBarneFaktaForGruppe}}

```


##### hvisKunStudent

```
{{#hvisKunStudent}}
    Bare student
    {{else}}
        Ikke bare student
{{/hvisKunStudent}}
```


##### hvisLik

```
{{#hvisLik "verdi 1" "verdi 2"}}
    Verdiene er like
    {{else}}
    Verdiene er ikke like
{{/hvisLik}}
```


##### hvisMer

```
{{#hvisMer verdi "1"}}
    Verdi er mer enn 1
    {{else}}
    Verdi er lik eller mindre enn 1
{{/hvisMer}}
```


##### hvisMindre

```
{{#hvisMindre verdi "50"}}
    Verdi er mindre enn 50
    {{else}}
    Verdi er lik eller større enn 50
{{/hvisMindre}}
```


##### hvisNoenAvkryssetBarneFakta

```
{{#hvisNoenAvkryssetBarneFakta barneFakta}}
    Avkrysset fakta
{{/hvisNoenAvkryssetBarneFakta}}
```


##### hvisSant

```
{{#hvisSant booleanString}}
    Gitt "true"
    {{else}}
    Gitt alt annet enn "true"
{{/hvisSant}}
```


##### hvisTekstFinnes

```
{{#hvisTekstFinnes "cms.key"}}
    true
{{else}}
    false
{{/hvisTekstFinnes}}
```


##### kortDato

```
{{kortDato "2015-11-03"}}
```


##### lagKjorelisteUker

```
{{#lagKjorelisteUker properties}}
    uke: {{ukeNr}}
    {{#each dager}}
        {{dato}}: {{parkering}}
    {{/each}}
{{/lagKjorelisteUker}}
```


##### property

```
Leser fra model på context
{{property 'navnPåProperty'}}
{{property 'fom'}}
```


##### sendtInnInfo

```
{{#sendtInnInfo}}
    innsendte: {{sendtInn}}
    påkrevde: {{ikkeSendtInn}}
    innsendt dato: {{innsendtDato}}
{{/sendtInnInfo}}
```


##### toCapitalized

```
{{toCapitalized variabel}}
{{toCapitalized "mASSe caSe"}}
```


##### toLowerCase

```
{{toLowerCase variabel}}
{{toLowerCase "MaSSe Case"}}
```


##### variabel

```
{{#variabel "minvariabel" "verdi1"}}
    Forventer verdi1: {{minvariabel}}
{{/variabel}}
```


##### vedleggCmsNokkel

```
{{#forVedlegg}}
    {{{hentTekst (vedleggCmsNokkel this)}}}
{{/forVedlegg}}
```


##### visCheckbox

```
{{#visCheckbox "value" "en.nokkel"}}
   Value er "true" eller teksten "en.nokkel.false" finnes
{{/visCheckbox}}
```

