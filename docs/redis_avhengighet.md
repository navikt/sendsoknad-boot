## Søknad avhengiheter
Søknader har en egen avhengighet til [redis]
```mermaid
classDiagram
    aap-->authServer
    aap-->sendsoknad
    authServer-->redis
```
