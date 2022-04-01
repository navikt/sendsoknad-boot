## Avhengiheter for søknadene


### I dag
<!--- #my-section --->
dagens arkitektur
```mermaid
classDiagram
    aap --> sendsoknad
    aaputland --> sendsoknad
    Tillegstonader --> sendsoknad
    tiltaksstonader --> sendsoknad
    bil --> sendsoknad
    sendsoknad --> soknad_fss_proxy
    soknad_fss_proxy --> arena
    soknad_fss_proxy --> TPS
    soknad_fss_proxy --> Felles Kodeverk
    sendsoknad --> Søknadsveiviser
    sendsoknad --> henvendelse
    henvendelse --> soknadmottaker
    henvendelse --> soknadfillager
    soknadmottaker --> soknadarkiverer
    soknadarkiverer --> joark
```
<!--- #my-section --->
#### Transisjonsarkitektur
- [soknad_ffs-proxy](https://github.com/navikt/soknad-fss-proxy) er tatt i bruk som midlertidig proxy mot eldre soaptjenster, tjenstene skal erstattes av tilbudte resttjenster.
- *henvendelse* skal saneres og sendsøknad skal gå direkte mot [soknadsfillager](https://github.com/navikt/soknadsfillager) og [soknadsmottaker](https://github.com/navikt/soknadsmottaker)

### To be
```mermaid
classDiagram
    aap --> sendsoknad
    aaputland --> sendsoknad
    Tillegstonader --> sendsoknad
    tiltaksstonader --> sendsoknad
    bil --> sendsoknad
    sendsoknad --> arena
    sendsoknad --> PDL
    sendsoknad --> soknadmottaker
    sendsoknad --> soknadsfillager
    soknadmottaker --> soknadarkiverer
    soknadarkiverer --> soknadsfillager
    soknadarkiverer --> joark
```
Alternativ fremstsilling 
```mermaid
flowchart TD
    A[aap] --> F[sendsoknad]
    B[aap-utland] --> F
    C[Tillegstonader] --> F
    D[tiltaksstonader] --> F
    E[bilstonad] --> F
    F[sendsoknad]-- hent status -->G[Arena]
    F-- hent persondata -->H[PDL]
    F-- hent kodeverk -->I[Felles Kodeverk]
    F-- send inn metadata om soknad -->J[Soknadsmottkater]
    F-- lagre filer for søknad -->K[Soknadsfillager]
    F-- oppsummering -->L[Soknadinnsending]
    L--> F
    J -- publiser -->M[Brukernotifikasjon]
    M-->L
    N[soknadsarkiverer]
    J -- publiser -->O[kafka]
    N -- Hent melding --> O
    N -- Hent filer --> K 
    N -- arkiver --> P[Joark]
```