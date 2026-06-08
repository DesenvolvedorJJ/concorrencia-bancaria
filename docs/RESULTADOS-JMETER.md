# Resultados de uma execução real do plano JMeter

> Execução **headless** real de `plano-testes-concorrencia.jmx`
> (`jmeter -n -t plano-testes-concorrencia.jmx -l resultados.jtl -e -o jmeter-report`)
> contra a aplicação rodando em `localhost:8080`.
>
> Configuração do plano: `NUM_THREADS=50`, `LOOPS=2`, depósito `10,00`, saque `5,00`,
> saldo inicial `100000,00`, `app.delay-ms=50`.
> Logo: **100 depósitos + 100 saques** em cada conta (200 requisições por parte).

## Resumo geral (Summary do JMeter)

```
summary = 402 in 00:00:03 = 127.0/s  Avg: 112  Min: 11  Max: 215  Err: 178 (44.28%)
```

| Endpoint                              | Amostras | HTTP 200 | HTTP 409 | Erro % | Avg     |
|---------------------------------------|---------:|---------:|---------:|-------:|--------:|
| RESET /contas/1                       |        1 |        1 |        0 |   0,00 |  65 ms  |
| RESET /contas-versionadas/1           |        1 |        1 |        0 |   0,00 |  11 ms  |
| POST /contas/1/deposito               |      100 |      100 |        0 |   0,00 | 109 ms  |
| POST /contas/1/saque                  |      100 |      100 |        0 |   0,00 | 109 ms  |
| POST /contas-versionadas/1/deposito   |      100 |       13 |       87 |  87,00 | 116 ms  |
| POST /contas-versionadas/1/saque      |      100 |        9 |       91 |  91,00 | 117 ms  |
| **Total**                             |  **402** |  **224** |  **178** |  44,28 | 112 ms  |

## Parte 1 — Conta SEM bloqueio  ❌ INCONSISTENTE

Todas as 200 requisições retornaram **HTTP 200 (sucesso)**. Mesmo assim o saldo final está errado:

```
Saldo inicial ............. 100000,00
Depositos aceitos (200) ... 100 x 10,00 = +1000,00
Saques aceitos (200) ...... 100 x  5,00 =  -500,00
-------------------------------------------------
Saldo ESPERADO ............ 100500,00
Saldo REAL (GET) .......... 100070,00
=> DINHEIRO PERDIDO ....... 430,00   (Lost Update)
```

`GET /contas/1` → `{"id":1,"titular":"Conta Sem Bloqueio (Parte 1)","saldo":100070.00}`

> O servidor respondeu "OK" para todas as operações, mas **R$ 430,00 evaporaram**:
> escritas concorrentes sobre o mesmo saldo se sobrescreveram.

## Parte 2 — Conta VERSIONADA (@Version)  ✅ CONSISTENTE

178 requisições conflitantes foram **rejeitadas com HTTP 409** (apenas 22 aplicadas). O saldo bate exatamente:

```
Saldo inicial ............. 100000,00
Depositos aceitos (200) ... 13 x 10,00 = +130,00
Saques aceitos (200) ......  9 x  5,00 =  -45,00
-------------------------------------------------
Saldo ESPERADO p/ sucessos. 100085,00
Saldo REAL (GET) .......... 100085,00
=> DIFERENCA .............. 0,00     (nada se perdeu)
```

`GET /contas-versionadas/1` → `{"id":1,"titular":"Conta Versionada (Parte 2)","saldo":100085.00,"version":23}`

> `version = 23 = 1 (reset) + 22 (movimentos aceitos)`. O número de escritas
> bem-sucedidas é rastreável e o saldo é exatamente a soma delas. As 178 colisões
> viraram 409 — um cliente real simplesmente as repetiria.

## Conclusão numérica

| Critério                          | Parte 1 (sem bloqueio) | Parte 2 (@Version) |
|-----------------------------------|------------------------|--------------------|
| Requisições "200 OK"              | 200 / 200              | 22 / 200           |
| Requisições "409 Conflict"        | 0                      | 178                |
| Saldo final                       | 100070,00              | 100085,00          |
| Saldo bate com as operações aceitas? | **NÃO (−430,00)**   | **SIM (0,00)**     |
| Integridade do dinheiro           | ❌ corrompida          | ✅ preservada      |

> Os números variam de execução para execução (a concorrência é não-determinística),
> mas o **padrão** é sempre o mesmo: Parte 1 mente "200 OK" e perde dinheiro; Parte 2
> é honesta — rejeita o conflito com 409 e nunca corrompe o saldo.
