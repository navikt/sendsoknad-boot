# Systemdokumentasjon for søknadsdialoger
Søknadsdialog tilbyr funksjonalitet for utfylling av en søknad via et dynamisk skjema, samt opplasting av påkrevde og frivillig opplastede vedlegg. 

Det eksisterer flere dialoger for fagområdene AAP, TSO, TSR og dermed Produktområdene arbeidsavklaring og arbeidsoppfølging. 

## Applikasjoner knyttet til tjensten
| Applikasjons type | Repo | Funskjonelle avhengigheter| Kommentar |
|------------------ | ---- |--------------| --------- |
| Søknad | [soknad-aap-utland](https://github.com/navikt/soknad-aap-utland) | Arena, PDL | Søknaden er den nyeste, kjører på eldre react versjon |
| Søknad | [soknadaap](https://github.com/navikt/soknadaap) | avhengigheter| Bygget på angular |
| Søknad | [soknadbilstonad](https://github.com/navikt/soknadbilstonad)| avhengigheter| Erstattes av Fyll-ut og send inn tjenesten |
| Søknad | [soknadrefusjondagligreise](https://github.com/navikt/soknadrefusjondagligreise)| avhengigheter| Bygget på angular |
| Søknad | [soknadtilleggsstonader](https://github.com/navikt/soknadtilleggsstonader) | avhengigheter| Bygget på angular |
| Søknad | [soknadtiltakspenger](https://github.com/navikt/soknadtiltakspenger) | avhengigheter| Bygget på angular |
| Felles FE bibliotek |[soknad-legacy](https://github.com/navikt/sendsoknad-legacy) | Felles FE bibliotek for Applikasjoner bygget på angular | Dette er et fellesbibliotek som er PT delt med dagpengersøknaden |
| Felles innsending | [soknadinnsending](https://github.com/navikt/soknadinnsending) | avhengigheter| Kommentar |
| Felles Backend | [sendsoknad]() | ARENA, PDL, DKIF, Felles Kodeverk, [skjemautlisting](https://www.nav.no/soknader/api/sanity/skjemautlisting)| Backend som er knyttet til bakendforliggende tjenester, Søknadsbygger, mellomlagring av søknader til de er sendt inn, oppretter brukernotifikasjoner mm. |
| mock for backend | [sendsoknad-mock-server](https://github.com/navikt/sendsoknad-mock-server)| NA | Benyttes for å kjøre opp søknader lokalt. |
| Mottaker|[soknadsmottaker](https://github.com/navikt/soknadsmottaker)|Publisering av brukernotifikasjoner og metadata som knytter søknaden til tema, skjemanummer, person og filer| Forvaltes av Annet team|
|Fillager|[soknadfillager](https://github.com/navikt/soknadsfillager)| midlertidig fillager for opplastede filer| Forvaltes av Annet team|
|Arkiverer|[soknadsarkiverer](https://github.com/navikt/soknadsarkiverer)|Knytter søknadmetadata og filer sammen og arkiverer på vegne av borger.|Forvaltes av Annet team|

## Funskjonell dokumentasjon
Søknadsdialog tilbyr funksjonalitet for utfylling av en søknad via et dynamisk skjema, samt opplasting av påkrevde og frivillig opplastede vedlegg. 
I tilegg er det bygget inn en ettersendingsløsning for å kunne ettersende dokumenter på en allerede innsendt søknad, innenfor de tidsrammene og reglene som er fastsatt. 
Etter bruker har lagt inn alle påkrevde data, lages det en pdf av innsendte data som sammen med vedleggene brukeren har lastet opp, legges i joark og håndteres av normal dokumentløp der.
### tekniske elementer
#### Faktum strukturen
* Søknadene er bygget på det som omtalles som faktumstruktur. 
* De er  satt sammen av bolker der spørsmål er gruppert innen bestemte områder, eksepelvis personlaia, barn mm.

## Arkitektur
Tjenestens søknader har en felles backend som har avhengigheter bakover mot tjenesster for personinformasjon og for arkivering av søknad.
[Se arkitekturtegning her](Arkitektur.md) 

## Teknisk beskrivelse
### Tekniske eksempel tegninger
| Tegning                                                   | Kommentar                        |||
|-----------------------------------------------------------|----------------------------------| ----- | ----- |
| [Sendsøknad modul avhengiheter](./module-dependencies.md) | Illusterer avhengheter overordnet for modulene |||
|| [avhengigheter for søknader](./Soknadsavhengigheter.md)   |                                  ||
|||||
|||||
|||||

## Annen dokumentasjon
Historisk dkumentasjon ligger i confluence [her i confluence](https://confluence.adeo.no/display/TS/Soknadsdialog).
