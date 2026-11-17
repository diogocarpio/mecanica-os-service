#!/usr/bin/env bash
# E2E real: OS Service + Billing Service + Execution Service via RabbitMQ
# Uso: ./e2e/run-e2e.sh [--keep] [--bdd]
#   --keep  mantém a infraestrutura no ar ao final
#   --bdd   em vez do fluxo shell, executa o BDD E2E (Cucumber) contra os serviços reais
set -euo pipefail

OS_DIR="$(cd "$(dirname "$0")/.." && pwd)"
E2E_DIR="$OS_DIR/e2e"
BILLING_DIR="${BILLING_DIR:-/tmp/mecanica-e2e/billing}"
EXECUTION_DIR="${EXECUTION_DIR:-/tmp/mecanica-e2e/execution}"

OS_URL="http://localhost:8081"
BILLING_URL="http://localhost:8080"
EXECUTION_URL="http://localhost:8082"

PIDS=()
KEEP_INFRA=false
BDD_MODE=false
for arg in "$@"; do
  case "$arg" in
    --keep) KEEP_INFRA=true ;;
    --bdd)  BDD_MODE=true ;;
  esac
done

log()  { echo -e "\033[1;34m[E2E]\033[0m $*"; }
ok()   { echo -e "\033[1;32m[OK]\033[0m $*"; }
fail() { echo -e "\033[1;31m[FAIL]\033[0m $*"; exit 1; }

json_get() { python3 -c "import sys,json; d=json.load(sys.stdin); print(d$1)" 2>/dev/null; }

wait_http() {
  local url="$1" name="$2" timeout="${3:-120}"
  log "Aguardando $name ($url) ..."
  for i in $(seq 1 "$timeout"); do
    if curl -sf "$url" >/dev/null 2>&1; then ok "$name pronto"; return 0; fi
    sleep 2
  done
  fail "$name não respondeu em ${timeout}x2s"
}

os_status() {
  curl -sf "$OS_URL/api/ordens-servico/$1/status" | json_get "['status']"
}

wait_os_status() {
  local os_id="$1" expected="$2" timeout="${3:-90}"
  log "Aguardando OS $os_id -> $expected ..."
  for i in $(seq 1 "$timeout"); do
    local st
    st=$(os_status "$os_id" || echo "ERRO")
    if [[ "$st" == "$expected" ]]; then ok "OS $os_id = $expected"; return 0; fi
    sleep 2
  done
  fail "OS $os_id não chegou a $expected (último: $(os_status "$os_id"))"
}

cleanup() {
  log "Encerrando serviços..."
  for pid in "${PIDS[@]:-}"; do kill "$pid" 2>/dev/null || true; done
  if [[ "$KEEP_INFRA" == false ]]; then
    docker compose -f "$E2E_DIR/docker-compose.e2e.yml" down -v >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

# ---------- 1. Infra ----------
log "Subindo infraestrutura (RabbitMQ, Postgres, Mongo rs0)..."
docker compose -f "$E2E_DIR/docker-compose.e2e.yml" up -d
sleep 5
# replica set do mongo: aguarda PRIMARY (myState==1)
log "Aguardando Mongo replica set PRIMARY..."
for i in $(seq 1 60); do
  STATE=$(docker exec e2e-mongo mongosh --quiet --eval "try { rs.status().myState } catch(e) { 0 }" 2>/dev/null | tr -d '[:space:]')
  [[ "$STATE" == "1" ]] && { ok "Mongo rs0 PRIMARY"; break; }
  sleep 2
done
[[ "${STATE:-0}" == "1" ]] || fail "Mongo replica set não ficou PRIMARY"

# ---------- 2. Build dos jars ----------
if [[ "${SKIP_BUILD:-0}" == "1" ]]; then
  log "SKIP_BUILD=1 — reutilizando jars existentes"
else
  log "Build dos serviços (skip tests)..."
  ( cd "$OS_DIR" && mvn -q -DskipTests package )
  ( cd "$BILLING_DIR" && mvn -q -DskipTests package )
  ( cd "$EXECUTION_DIR" && mvn -q -DskipTests package )
fi

OS_JAR=$(ls "$OS_DIR"/target/*.jar | grep -v original | head -1)
BILLING_JAR=$(ls "$BILLING_DIR"/target/*.jar | grep -v original | head -1)
EXECUTION_JAR=$(ls "$EXECUTION_DIR"/target/*.jar | grep -v original | head -1)
[[ -f "$OS_JAR" && -f "$BILLING_JAR" && -f "$EXECUTION_JAR" ]] || fail "Jars não encontrados"

# ---------- 3. Start dos serviços ----------
log "Subindo OS Service (8081), Billing (8080), Execution (8082)..."
java -jar "$OS_JAR" > /tmp/e2e-os.log 2>&1 & PIDS+=($!)
SERVER_PORT=8080 MONGODB_URI="mongodb://localhost:27017/billing?replicaSet=rs0" \
  java -jar "$BILLING_JAR" > /tmp/e2e-billing.log 2>&1 & PIDS+=($!)
SERVER_PORT=8082 BROKER_ENABLED=true SPRING_DATA_MONGODB_UUID_REPRESENTATION=JAVA_LEGACY \
  MONGODB_URI="mongodb://localhost:27017/execution?replicaSet=rs0" \
  java -jar "$EXECUTION_JAR" > /tmp/e2e-execution.log 2>&1 & PIDS+=($!)

wait_http "$OS_URL/actuator/health" "OS Service"
wait_http "$BILLING_URL/actuator/health" "Billing Service"
wait_http "$EXECUTION_URL/actuator/health" "Execution Service"

# ---------- 4. Fluxo E2E ----------
if [[ "$BDD_MODE" == "true" ]]; then
  log "Executando BDD E2E (Cucumber) contra os serviços reais..."
  ( cd "$OS_DIR" && mvn test -Dtest=RunCucumberE2ETest )
  ok "BDD E2E concluído — cenários do Saga validados via filas reais"
  exit 0
fi

log "Criando OS..."
OS_HTTP=$(curl -s -o /tmp/e2e-os-resp.json -w "%{http_code}" -X POST "$OS_URL/api/ordens-servico" \
  -H "Content-Type: application/json" \
  -d '{"cpfCnpjCliente":"52998224792","placaVeiculo":"ABC1234","servicosIds":["1","2"],"pecasIds":["10"]}')
OS_RESP=$(cat /tmp/e2e-os-resp.json)
[[ "$OS_HTTP" == "200" || "$OS_HTTP" == "201" ]] || fail "POST OS retornou HTTP $OS_HTTP: $OS_RESP"
OS_ID=$(echo "$OS_RESP" | json_get "['id']")
[[ -n "$OS_ID" && "$OS_ID" != "None" ]] || fail "OS não criada: $OS_RESP"
ok "OS criada: id=$OS_ID"

wait_os_status "$OS_ID" "AGUARDANDO_APROVACAO"

log "Buscando orçamento no Billing (osId=$OS_ID)..."
QUOTE_RESP=$(curl -sf "$BILLING_URL/api/v1/quotes?osId=$OS_ID")
QUOTE_ID=$(echo "$QUOTE_RESP" | json_get "['id']")
[[ -n "$QUOTE_ID" && "$QUOTE_ID" != "None" ]] || fail "Quote não encontrada: $QUOTE_RESP"
ok "Quote encontrada: id=$QUOTE_ID"

log "Aprovando orçamento no Billing..."
curl -sf -X POST "$BILLING_URL/api/v1/quotes/$QUOTE_ID/approve" >/dev/null

wait_os_status "$OS_ID" "EM_EXECUCAO"

log "Buscando execução no Execution..."
EXEC_ID=""
for i in $(seq 1 30); do
  EXEC_ID=$(curl -sf "$EXECUTION_URL/api/executions/queue" \
    | python3 -c "import sys,json; q=json.load(sys.stdin); print(next((e['executionId'] for e in q if str(e['osId'])=='$OS_ID'), ''))" 2>/dev/null || true)
  [[ -n "$EXEC_ID" ]] && break
  sleep 2
done
[[ -n "$EXEC_ID" ]] || fail "Execução não encontrada para osId=$OS_ID"
ok "Execução encontrada: id=$EXEC_ID"

log "Completando execução..."
curl -sf -X POST "$EXECUTION_URL/api/executions/$EXEC_ID/complete" >/dev/null

wait_os_status "$OS_ID" "FINALIZADA" 120

echo ""
echo -e "\033[1;32m========================================\033[0m"
echo -e "\033[1;32m E2E PASSOU — Saga completa via filas reais\033[0m"
echo -e "\033[1;32m========================================\033[0m"
echo "Fluxo: OS $OS_ID -> AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA"
