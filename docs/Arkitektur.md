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
- 