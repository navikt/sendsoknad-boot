## Avhengiheter for søknadene  

```mermaid
classDiagram
    aap --> redis
    aap --> Legacy_frontend
    aap --> sendsoknad
    aap-utland --> redis
    aap-utland --> sendsoknad -->arena
    Tillegstonader --> redis
    Tillegstonader --> Legacy_frontend
    Tillegstonader --> sendsoknad --> arena
    tiltaksstonader--> redis
    tiltaksstonader --> Legacy_frontend
    tiltaksstonader --> sendsoknad
    bil--> redis
    bil --> Legacy_frontend
    bil --> sendsoknad   
```