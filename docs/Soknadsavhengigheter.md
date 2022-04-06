## Avhengiheter for søknadene  

### AAP eksempel

#### Søknad opprettelse og under utfylling
```mermaid
graph TD
    A[soknad] -->|logg inn| B(auth server)
    A --> |opprett og fyll inn| C(sendsoknad)
    B --> |sjekk session| N(redis) 
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
#### Brukernotifikasjoner og muligheter for bruker å stoppe under innsengding
```mermaid
stateDiagram-v2
[*] --> Start_soknad
Start_soknad --> Fortsett_senere
Start_soknad --> Ferdig_fyllt_ut
Start_soknad --> Avbryt
Ferdig_fyllt_ut --> Oppsummering
Sendinn --> [*]
Oppsummering --> Fortsett_senere
Oppsummering --> Ettersend_dokumentasjon
Oppsummering --> Avbryt
Oppsummering --> Sendinn
Ettersend_dokumentasjon --> Brukernotifikasjon
Fortsett_senere --> Brukernotifikasjon
```
##### Brukernotifikasjoner
* En bruker kan velge å fortsette senere under utfylling av selve søknaden eller under oppsummeringen.
* En bruker kan velge å etterende påkrevd dokumentasjon under oppsummeringen.
* En bruker kan velge å avbryte søknad når som helst.
* En bruker får en beskjed på dittnav når den velger å fortsette senere.
* En bruker får en oppgave på dittnav når den velger å ettersende påkrevd dokumentasjon.
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
