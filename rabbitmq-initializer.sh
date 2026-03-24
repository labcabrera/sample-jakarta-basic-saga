#!/usr/bin/env bash
set -euo pipefail

RABBIT_HOST="${RABBIT_HOST:-localhost}"
RABBIT_PORT="${RABBIT_PORT:-15672}"
RABBIT_USER="${RABBIT_USER:-admin}"
RABBIT_PASS="${RABBIT_PASS:-admin}"
VHOST="${VHOST:-/}"

EXCHANGE_NAME="${EXCHANGE_NAME:-expedientes}"
EXCHANGE_TYPE="${EXCHANGE_TYPE:-direct}"
EXCHANGE_DURABLE="${EXCHANGE_DURABLE:-true}"

# Mapa cola:routingKey
BINDINGS=(
  "creacion-expediente-cola:creacion"
  "creacion-expediente-ok-cola:creacion-ok"
  "creacion-expediente-ko-cola:creacion-ko"
)

urlencode() {
  python3 -c "import urllib.parse, sys; print(urllib.parse.quote(sys.argv[1], safe=''))" "$1"
}

api_put() {
  local path="$1"
  local data="$2"

  curl -v -sS -u "${RABBIT_USER}:${RABBIT_PASS}" \
    -H "content-type: application/json" \
    -X PUT \
    "http://${RABBIT_HOST}:${RABBIT_PORT}/api/${path}" \
    -d "${data}"
}

api_post() {
  local path="$1"
  local data="$2"

  curl -v -sS -u "${RABBIT_USER}:${RABBIT_PASS}" \
    -H "content-type: application/json" \
    -X POST \
    "http://${RABBIT_HOST}:${RABBIT_PORT}/api/${path}" \
    -d "${data}"
}

VHOST_ENCODED="$(urlencode "$VHOST")"
EXCHANGE_ENCODED="$(urlencode "$EXCHANGE_NAME")"

echo "Creando exchange '${EXCHANGE_NAME}'..."
api_put "exchanges/${VHOST_ENCODED}/${EXCHANGE_ENCODED}" \
  "{\"type\":\"${EXCHANGE_TYPE}\",\"durable\":${EXCHANGE_DURABLE},\"auto_delete\":false,\"internal\":false,\"arguments\":{}}"

for binding in "${BINDINGS[@]}"; do
  QUEUE_NAME="${binding%%:*}"
  ROUTING_KEY="${binding#*:}"

  QUEUE_ENCODED="$(urlencode "$QUEUE_NAME")"

  echo "Creando queue '${QUEUE_NAME}'..."
  api_put "queues/${VHOST_ENCODED}/${QUEUE_ENCODED}" \
    '{"durable":true,"auto_delete":false,"arguments":{}}'

  echo "Asociando queue '${QUEUE_NAME}' al exchange '${EXCHANGE_NAME}' con routing key '${ROUTING_KEY}'..."
  api_post "bindings/${VHOST_ENCODED}/e/${EXCHANGE_ENCODED}/q/${QUEUE_ENCODED}" \
    "{\"routing_key\":\"${ROUTING_KEY}\",\"arguments\":{}}"
done
