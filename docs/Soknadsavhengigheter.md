## Avhengiheter for søknadene  

### AAP eksempel

#### Søknad opprettelse og under utfylling
```mermaid
graph TD
       A[soknad] -->|logg inn| B(auth server)
    A --> |opprett og full inn| C(sendsoknad)
    B --> |difi innlogging| D[difi]
    C -->|Hent persondata| E[PDL]
    C -->|Hent kodeverk| F[kodeverk]
    C -->|Hent status på borger| G[Arena]
    C -->|Oppsummering og kvittering| H(soknadinnsending)
    C -->|lagre filer| I[soknadsfillager]
    C -->|Send metadata| J[soknadsmottaker]
    J -->|Publiser brukernotifikasjon| M[dittnav]    
```
#### Søknad arkiveres
```mermaid
graph TD
    A(soknadinnsending)-->|send inn søknad| B(sendsoknad)
    B -->|Send metadata| C[soknadsmottaker]
    B -->|Lagre filer| D[soknadsfillager]
    C -->|Publiser metadata| E[Kafka]
    F[soknadsarkiverer]-->|Les melding| E
    F -->|Hent filer for søknad| D
    F -->|Arkiver journalpost| G[Joark]
```
#### Brukernotifikasjoner og muligheter for bruker å stoppe under innsending
```mermaid
stateDiagram-v2
[*] --> Start_soknad
Start_soknad --> [*]
Start_soknad --> Fortsett_senere
Start_soknad --> Ferdig_fyllt_ut
Fortsett_senere --> Brukernotifikasjon
Ferdig_fyllt_ut --> Oppsummering
Oppsummering --> Fortsett_senere
Oppsummering --> Ettersend_dokumentasjon
Ettersend_dokumentasjon --> Brukernotifikasjon
Oppsummering --> Sendinn
Sendinn --> [*]
Brukernotifikasjon --> Beskjed
Beskjed --> Start_soknad
Brukernotifikasjon --> Oppgave
Oppgave --> Oppsummering
```
##### Brukernotifikasjoner
* En bruker kan velge å fortsette senere under utfylling av selve søknaden eller under oppsummeringen
* En bruker kan velge å etterende påkrevd dokumentasjon under oppsummeringen.
```mermaid
stateDiagram-v2
    [*] --> Start_soknad
    Start_soknad --> Fortsett_senere
    Fortsett_senere --> Beskjed
    Oppsummering --> Fortsett_senere
    Oppsummering --> Ettersend_dokumentasjon
    Ettersend_dokumentasjon --> Oppgave
    Beskjed --> Start_soknad
    Beskjed --> Oppsummering
    Oppgave --> Oppsummering
```