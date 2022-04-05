## Søknad avhengiheter
Søknader har en egen avhengighet til [redis]
```mermaid
classDiagram
    aap-->authServer
    auuthServer-->redis-db
    aap-->sendsoknad
```
