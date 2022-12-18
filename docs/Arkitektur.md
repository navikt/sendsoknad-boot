## Avhengiheter for søknadene

### I dag
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
    sendsoknad --> soknadsmottaker
    sendsoknad --> soknadsfillager
    soknadsmottaker --> soknadsarkiverer
    soknadsarkiverer --> soknadsfillager
    soknadsarkiverer --> joark
```

#### Transisjonsarkitektur
- [soknad_ffs-proxy](https://github.com/navikt/soknad-fss-proxy) er tatt i bruk som midlertidig proxy mot eldre soaptjenster, tjenstene skal erstattes av tilbudte resttjenster.

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
    sendsoknad --> soknadsmottaker
    sendsoknad --> soknadsfillager
    soknadsmottaker --> soknadsarkiverer
    soknadsarkiverer --> soknadsfillager
    soknadsarkiverer --> joark
```
Alternativ fremstilling
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
    F-- send inn metadata om soknad -->J[soknadsmottaker]
    F-- lagre filer for søknad -->K[soknadsfillager]
    F-- oppsummering -->L[Soknadinnsending]
    L--> F
    J -- publiser -->M[Brukernotifikasjon]
    M-- gjennoppta -->L
    N[soknadsarkiverer]
    J -- publiser -->O[kafka]
    O -- Hent melding --> N
    N -- Hent filer --> K 
    N -- arkiver --> P[Joark]
```
