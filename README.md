# tsn-api

API REST para análise de redes Ethernet TSN usando Network Calculus.

Arquitetura distribuída com Gateway + Workers usando Quarkus 3.34.

## Pré-requisitos

- Docker e Docker Compose

## Como rodar

### 1. Clonar as dependências

O projeto depende do **DNC-EthernetTSN**, que por sua vez depende do **NetCal-DNC**. Ambos não estão em repositórios Maven públicos e precisam ser clonados manualmente:

```bash
# Na raiz do tsn-api, clonar o DNC-EthernetTSN
git clone https://github.com/davidalain/DNC-EthernetTSN.git DNC-EthernetTSN

# Dentro do DNC-EthernetTSN, clonar o NetCal-DNC
cd DNC-EthernetTSN
git clone https://github.com/NetCal/DNC.git DNC
cd ..
```

A estrutura deve ficar:

```
tsn-api/
├── DNC-EthernetTSN/      ← clonado manualmente (gitignored)
│   ├── DNC/              ← clonado manualmente
│   ├── src/
│   └── pom.xml
├── src/
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

### 2. Subir com Docker Compose

```bash
docker-compose up --build
```

Isso builda tudo do source (NetCal-DNC → DNC-EthernetTSN → tsn-api) e sobe:
- **Gateway** em `localhost:8080`
- **2 Workers** (rede interna, não acessíveis externamente)

### 3. Testar

```bash
curl -X POST http://localhost:8080/api/v1/gateway/analyze \
  -H "Content-Type: application/json" \
  -d "[$(cat src/main/resources/samples/dataset1-1.json)]"
```

## Endpoints

### Gateway

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/gateway/analyze` | Envia N redes para análise distribuída |
| PUT | `/api/v1/gateway/manage-worker/add` | Adiciona worker dinamicamente |
| DELETE | `/api/v1/gateway/manage-worker/remove` | Remove worker |
| GET | `/api/v1/gateway/manage-worker/status` | Status de todos os workers |
| GET | `/api/v1/gateway/manage-worker/status/{id}` | Status de um worker |

### Worker (acesso interno)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/analysis/single` | Análise de uma rede individual |

### Infraestrutura

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/q/health/ready` | Readiness probe |
| GET | `/q/health/live` | Liveness probe |
| GET | `/q/metrics` | Métricas Prometheus |

## Desenvolvimento local (sem Docker)

### Pré-requisitos
- JDK 17+
- Maven 3.9+

### Setup

```bash
# 1. Buildar e instalar NetCal-DNC no .m2 local
cd DNC-EthernetTSN/DNC
mvn install -DskipTests

# 2. Buildar e instalar DNC-EthernetTSN no .m2 local
cd ..
mvn install -DskipTests

# 3. Voltar para tsn-api e rodar em modo dev
cd ..
./mvnw quarkus:dev
```

## Configuração

Propriedades configuráveis em `application.properties` ou via variáveis de ambiente:

| Propriedade | Default | Descrição |
|-------------|---------|-----------|
| `tsn.worker.urls` | `http://localhost:8080` | URLs dos workers (separadas por vírgula) |
| `tsn.loadbalancer.health-check-interval-seconds` | `10` | Intervalo do health check |
| `tsn.loadbalancer.request-timeout-seconds` | `30` | Timeout de requisição para workers |
| `tsn.loadbalancer.connection-timeout-seconds` | `3` | Timeout de conexão |
| `tsn.loadbalancer.max-consecutive-failures` | `5` | Falhas consecutivas para auto-remoção |

## Logs

Para acompanhar os logs em tempo real:

```bash
# Todos os serviços
docker-compose logs -f

# Apenas o gateway
docker-compose logs -f tsn-gateway

# Apenas os workers
docker-compose logs -f tsn-worker-1 tsn-worker-2
```
