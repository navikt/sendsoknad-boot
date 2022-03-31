## Avhengiheter for søknadene  

```mermaid
classDiagram
    aap --> redis
    aap --> Legacy_frontend
    aap --> sendsoknad
    aaputland --> redis
    aaputland --> sendsoknad
    Tillegstonader --> redis
    Tillegstonader --> Legacy_frontend
    Tillegstonader --> sendsoknad
    tiltaksstonader--> redis
    tiltaksstonader --> Legacy_frontend
    tiltaksstonader --> sendsoknad
    bil--> redis
    bil --> Legacy_frontend
    bil --> sendsoknad   
```