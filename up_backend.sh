#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
RESOURCE_DIR="$BACKEND_DIR/src/main/resources"
COMPOSE_FILE="$ROOT_DIR/docker-compose.backend.local.yml"

cd "$ROOT_DIR"

check_port_available() {
  local port="$1"
  local service="$2"
  local allowed_container="$3"

  if docker ps --format '{{.Names}}' | grep -Fxq "$allowed_container"; then
    echo "Port $port is already owned by $allowed_container; reusing the existing local $service container."
    return
  fi

  if command -v lsof >/dev/null 2>&1; then
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      echo "ERROR: port $port is already in use; cannot start $service." >&2
      lsof -nP -iTCP:"$port" -sTCP:LISTEN >&2 || true
      exit 1
    fi
  elif command -v ss >/dev/null 2>&1; then
    if ss -ltn "sport = :$port" | awk 'NR > 1 { found = 1 } END { exit !found }'; then
      echo "ERROR: port $port is already in use; cannot start $service." >&2
      ss -ltnp "sport = :$port" >&2 || true
      exit 1
    fi
  elif command -v netstat >/dev/null 2>&1; then
    if netstat -ltn 2>/dev/null | awk -v port=":$port" '$4 ~ port"$" { found = 1 } END { exit !found }'; then
      echo "ERROR: port $port is already in use; cannot start $service." >&2
      netstat -ltnp 2>/dev/null | awk -v port=":$port" '$4 ~ port"$"' >&2 || true
      exit 1
    fi
  else
    echo "WARN: lsof, ss, and netstat are unavailable; skipping local port check for $service ($port)." >&2
  fi
}

check_container_name_available() {
  local container_name="$1"

  if docker ps -a --format '{{.Names}}' | grep -Fxq "$container_name"; then
    local state
    state="$(docker inspect -f '{{.State.Status}}' "$container_name" 2>/dev/null || echo unknown)"
    if [[ "$state" != "running" && "$state" != "exited" && "$state" != "created" ]]; then
      echo "ERROR: container name $container_name already exists in unexpected state: $state" >&2
      exit 1
    fi
  fi
}

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker command is required." >&2
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE=(docker-compose)
else
  echo "ERROR: docker compose plugin or docker-compose is required." >&2
  exit 1
fi

if [[ ! -f "$RESOURCE_DIR/key.yml" ]]; then
  if [[ -f "$RESOURCE_DIR/key.yaml" ]]; then
    echo "backend/src/main/resources/key.yml not found; copying key.yaml to key.yml for the Spring classpath import."
    cp "$RESOURCE_DIR/key.yaml" "$RESOURCE_DIR/key.yml"
  else
    cat >&2 <<'MSG'
ERROR: backend/src/main/resources/key.yml is required before starting the backend.
If your local secret file is named key.yaml, place it at backend/src/main/resources/key.yaml
and rerun this script; it will copy it to key.yml for the current Spring configuration.
MSG
    exit 1
  fi
fi

echo "Checking local port availability..."
check_port_available 3306 "MySQL" "seeandyougo-local-mysql"
check_port_available 6379 "Redis" "seeandyougo-local-redis"
check_port_available 8080 "SeeAndYouGo backend" "seeandyougo-local-backend"

check_container_name_available "seeandyougo-local-mysql"
check_container_name_available "seeandyougo-local-redis"
check_container_name_available "seeandyougo-local-backend"

echo "Building latest backend jar..."
(cd "$BACKEND_DIR" && ./gradlew clean bootJar -x test)

echo "Starting local backend stack (MySQL + Redis + SeeAndYouGo)..."
"${COMPOSE[@]}" -f "$COMPOSE_FILE" up -d --build mysql redis seeandyougo

cat <<'MSG'

Local backend stack is starting.
- Backend API: http://localhost:8080
- MySQL: localhost:3306 (database: seeandyougo, user: root, password: empty)
- Redis: localhost:6379

Useful commands:
- Logs: docker compose -f docker-compose.backend.local.yml logs -f seeandyougo
- Stop: docker compose -f docker-compose.backend.local.yml down
MSG
