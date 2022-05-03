## Module dependency-tree
Avhengigheter mellom modulene som inngår i prosjektet.

```mermaid
classDiagram
    boot --> web
    web --> business
    web --> auth
    business --> innsending
    innsending --> innsending_clients
    business --> pdfutility
    business --> consumer
    consumer --> mock
    consumer --> domain
    consumer --> auth
    mock --> domain
```
