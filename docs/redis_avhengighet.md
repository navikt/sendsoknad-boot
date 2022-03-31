## Søknad avhengiheter
Søknader har en egen avhengighet til [redis]
```mermaid
classDiagram
    aap-->redis
    aap-->sendsoknad
```