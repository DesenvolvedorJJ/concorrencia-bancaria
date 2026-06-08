# Prints do JMeter

Coloque aqui os seus screenshots da execução do JMeter (modo GUI) para anexar ao relatório do README.

Sugestão de capturas:

| Arquivo sugerido        | O que capturar                                                        |
|-------------------------|------------------------------------------------------------------------|
| `parte1-summary.png`    | **Summary Report** após rodar a Parte 1 (tudo verde, 0% de erro).      |
| `parte1-saldo.png`      | Resposta do `GET /contas/1` mostrando o saldo final ERRADO.            |
| `parte2-summary.png`    | **Summary Report** da Parte 2 com a coluna *Error %* alta (HTTP 409).  |
| `parte2-view-tree.png`  | **View Results Tree** mostrando um response `409 Conflict` com o JSON. |
| `parte2-saldo.png`      | Resposta do `GET /contas-versionadas/1` com o saldo CONSISTENTE.       |

Depois é só referenciá-los no `README.md`, por exemplo:

```markdown
![Summary Parte 1](docs/prints/parte1-summary.png)
```

> Os números de uma execução real (headless) já estão documentados em
> [`docs/RESULTADOS-JMETER.md`](../RESULTADOS-JMETER.md).
