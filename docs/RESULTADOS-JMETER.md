# Resultados de uma execução real do plano JMeter

> Execução **headless** real de `plano-testes-concorrencia.jmx`
> (`jmeter -n -t plano-testes-concorrencia.jmx -l resultados.jtl -e -o jmeter-report`)
> contra a aplicação rodando em `localhost:8080`.
>
> Configuração do plano: `NUM_THREADS=50`, `LOOPS=2`, depósito `10,00`, saque `5,00`,
> saldo inicial `1000,00`, `app.delay-ms=50`.
> Logo: **100 depósitos + 100 saques** em cada conta (200 requisições por parte).

## Resumo geral (Summary do JMeter)

```
summary = 402 in 00:00:03 = 130.3/s  Avg: 102  Min: 4  Max: 176  Err: 178 (44.28%)
```

| Endpoint                              | Amostras | HTTP 200 | HTTP 409 | Erro % | Avg     |
|---------------------------------------|---------:|---------:|---------:|-------:|--------:|
| RESET /contas/1                       |        1 |        1 |        0 |   0,00 |  63 ms  |
| RESET /contas-versionadas/1           |        1 |        1 |        0 |   0,00 |   4 ms  |
| POST /contas/1/deposito               |      100 |      100 |        0 |   0,00 | 103 ms  |
| POST /contas/1/saque                  |      100 |      100 |        0 |   0,00 | 103 ms  |
| POST /contas-versionadas/1/deposito   |      100 |       12 |       88 |  88,00 | 102 ms  |
| POST /contas-versionadas/1/saque      |      100 |       10 |       90 |  90,00 | 101 ms  |
| **Total**                             |  **402** |  **224** |  **178** |  44,28 | 102 ms  |

## Parte 1 — Conta SEM bloqueio  ❌ INCONSISTENTE

Todas as 200 requisições retornaram **HTTP 200 (sucesso)**. Mesmo assim o saldo final está errado:

```
Saldo inicial ............. 1000,00
Depositos aceitos (200) ... 100 x 10,00 = +1000,00
Saques aceitos (200) ...... 100 x  5,00 =  -500,00
-------------------------------------------------
Saldo ESPERADO ............ 1500,00
Saldo REAL (GET) .......... 1075,00
=> DINHEIRO PERDIDO .......  425,00   (Lost Update)
```

`GET /contas/1` → `{"id":1,"titular":"Conta Sem Bloqueio (Parte 1)","saldo":1075.00}`

> O servidor respondeu "OK" para todas as operações, mas **R$ 425,00 evaporaram**:
> escritas concorrentes sobre o mesmo saldo se sobrescreveram.

## Parte 2 — Conta VERSIONADA (@Version)  ✅ CONSISTENTE

178 requisições conflitantes foram **rejeitadas com HTTP 409** (apenas 22 aplicadas). O saldo bate exatamente:

```
Saldo inicial ............. 1000,00
Depositos aceitos (200) ... 12 x 10,00 = +120,00
Saques aceitos (200) ...... 10 x  5,00 =  -50,00
-------------------------------------------------
Saldo ESPERADO p/ sucessos. 1070,00
Saldo REAL (GET) .......... 1070,00
=> DIFERENCA .............. 0,00     (nada se perdeu)
```

`GET /contas-versionadas/1` → `{"id":1,"titular":"Conta Versionada (Parte 2)","saldo":1070.00,"version":...}`

> O saldo final é **exatamente** a soma das 22 operações aceitas (12 depósitos − 10 saques).
> As 178 colisões viraram 409 — um cliente real simplesmente as repetiria.
> O campo `version` aumenta **uma unidade a cada escrita aceita** (e **nunca** em um 409);
> o valor exibido pelo `GET` reflete o total acumulado de escritas bem-sucedidas naquela
> instância da aplicação.

## Conclusão numérica

| Critério                          | Parte 1 (sem bloqueio) | Parte 2 (@Version) |
|-----------------------------------|------------------------|--------------------|
| Requisições "200 OK"              | 200 / 200              | 22 / 200           |
| Requisições "409 Conflict"        | 0                      | 178                |
| Saldo final                       | 1075,00                | 1070,00            |
| Saldo bate com as operações aceitas? | **NÃO (−425,00)**   | **SIM (0,00)**     |
| Integridade do dinheiro           | ❌ corrompida          | ✅ preservada      |

> Os números variam de execução para execução (a concorrência é não-determinística),
> mas o **padrão** é sempre o mesmo: Parte 1 mente "200 OK" e perde dinheiro; Parte 2
> é honesta — rejeita o conflito com 409 e nunca corrompe o saldo.
