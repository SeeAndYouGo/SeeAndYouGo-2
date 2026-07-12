#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
RESOURCE_DIR="$BACKEND_DIR/src/main/resources"
COMPOSE_FILE="$ROOT_DIR/docker-compose.backend.local.yml"
FRONTEND_ENV_FILE="${FRONTEND_ENV_FILE:-$ROOT_DIR/frontend/.env}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-seeandyougo-local-backend}"
CONTAINER=()
COMPOSE=()

cd "$ROOT_DIR"

check_port_available() {
  local port="$1"
  local service="$2"
  local allowed_container="$3"

  if "${CONTAINER[@]}" ps --format '{{.Names}}' | grep -Fxq "$allowed_container"; then
    case "$service" in
      "MySQL"|"Redis")
        echo "$service port $port is already used by $allowed_container; keeping the existing local $service container."
        ;;
      "SeeAndYouGo backend")
        echo "Backend port $port is already used by $allowed_container; compose will rebuild and restart the backend if needed."
        ;;
      *)
        echo "Port $port is already used by $allowed_container; continuing with the existing local container."
        ;;
    esac
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

  if "${CONTAINER[@]}" ps -a --format '{{.Names}}' | grep -Fxq "$container_name"; then
    local state
    state="$("${CONTAINER[@]}" inspect -f '{{.State.Status}}' "$container_name" 2>/dev/null || echo unknown)"
    if [[ "$state" == "restarting" ]]; then
      echo "Container $container_name is restarting; compose will attempt to recreate or restart it."
    elif [[ "$state" != "running" && "$state" != "exited" && "$state" != "created" ]]; then
      echo "ERROR: container name $container_name already exists in unexpected state: $state" >&2
      exit 1
    fi
  fi
}

read_key_redirect_uri() {
  local section="$1"

  awk -v section="$section" '
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    /^[[:alnum:]_-]+:[[:space:]]*$/ {
      current = $1
      sub(/:$/, "", current)
      next
    }
    current == section && /^[[:space:]]*REDIRECT_URI:[[:space:]]*/ {
      line = $0
      sub(/^[[:space:]]*REDIRECT_URI:[[:space:]]*/, "", line)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
      gsub(/^"|"$/, "", line)
      print line
      exit
    }
  ' "$RESOURCE_DIR/key.yml"
}

read_frontend_env_value() {
  local key="$1"

  awk -v key="$key" '
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    {
      line = $0
      sub(/\r$/, "", line)
      sub(/^[[:space:]]*export[[:space:]]+/, "", line)
      name = line
      sub(/=.*/, "", name)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", name)
      if (name == key) {
        sub(/^[^=]*=/, "", line)
        gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
        gsub(/^"|"$/, "", line)
        print line
        exit
      }
    }
  ' "$FRONTEND_ENV_FILE"
}

update_key_redirect_uri() {
  local section="$1"
  local redirect_uri="$2"
  local tmp_file

  tmp_file="$(mktemp "${TMPDIR:-/tmp}/key.yml.XXXXXX")"
  awk -v section="$section" -v redirect_uri="$redirect_uri" '
    /^[[:alnum:]_-]+:[[:space:]]*$/ {
      current = $1
      sub(/:$/, "", current)
      print
      next
    }
    current == section && /^[[:space:]]*REDIRECT_URI:[[:space:]]*/ {
      sub(/REDIRECT_URI:.*/, "REDIRECT_URI: " redirect_uri)
      print
      next
    }
    { print }
  ' "$RESOURCE_DIR/key.yml" > "$tmp_file"
  mv "$tmp_file" "$RESOURCE_DIR/key.yml"
}

check_redirect_uri_match() {
  local label="$1"
  local key_section="$2"
  local frontend_key="$3"
  local backend_redirect
  local frontend_redirect

  backend_redirect="$(read_key_redirect_uri "$key_section")"
  frontend_redirect="$(read_frontend_env_value "$frontend_key")"

  if [[ -z "$backend_redirect" ]]; then
    echo "ERROR: $label REDIRECT_URI is missing in backend/src/main/resources/key.yml." >&2
    return 1
  fi

  if [[ -z "$frontend_redirect" ]]; then
    echo "ERROR: $frontend_key is missing or empty in $FRONTEND_ENV_FILE." >&2
    return 1
  fi

  if [[ "$backend_redirect" != "$frontend_redirect" ]]; then
    echo "ERROR: $label redirect URI mismatch between key.yml and $FRONTEND_ENV_FILE ($frontend_key)." >&2
    return 2
  fi

  echo "$label redirect URI matches frontend .env."
  return 0
}

check_oauth_redirect_urls() {
  if [[ ! -f "$FRONTEND_ENV_FILE" ]]; then
    echo "ERROR: frontend env file is required for OAuth redirect URI validation: $FRONTEND_ENV_FILE" >&2
    echo "Set FRONTEND_ENV_FILE=/path/to/.env if your frontend env file is elsewhere." >&2
    exit 1
  fi

  echo "Checking OAuth redirect URI consistency..."
  local failed=0
  local mismatch=0

  check_redirect_uri_match "Kakao" "kakao" "REACT_APP_KAKAO_REDIRECT_URI" || {
    if [[ "$?" -eq 2 ]]; then
      mismatch=1
    else
      failed=1
    fi
  }

  check_redirect_uri_match "Google" "google" "REACT_APP_GOOGLE_REDIRECT_URI" || {
    if [[ "$?" -eq 2 ]]; then
      mismatch=1
    else
      failed=1
    fi
  }

  if [[ "$failed" -ne 0 ]]; then
    exit 1
  fi

  if [[ "$mismatch" -ne 0 ]]; then
    if [[ ! -t 0 ]]; then
      echo "ERROR: OAuth redirect URI mismatch detected. Rerun in a terminal and choose whether to sync key.yml from frontend .env." >&2
      exit 1
    fi

    local answer
    read -r -p "Sync key.yml OAuth redirect URIs from frontend .env? [y/N] " answer
    case "$answer" in
      [yY]|[yY][eE][sS])
        update_key_redirect_uri "kakao" "$(read_frontend_env_value "REACT_APP_KAKAO_REDIRECT_URI")"
        update_key_redirect_uri "google" "$(read_frontend_env_value "REACT_APP_GOOGLE_REDIRECT_URI")"
        echo "Updated key.yml OAuth redirect URIs from frontend .env."
        check_redirect_uri_match "Kakao" "kakao" "REACT_APP_KAKAO_REDIRECT_URI" || exit 1
        check_redirect_uri_match "Google" "google" "REACT_APP_GOOGLE_REDIRECT_URI" || exit 1
        ;;
      *)
        echo "ERROR: OAuth redirect URI mismatch was not fixed." >&2
        exit 1
        ;;
    esac
  fi
}

check_local_ddl_auto_update() {
  if ! grep -Eq 'SPRING_JPA_HIBERNATE_DDL_AUTO:[[:space:]]*update' "$COMPOSE_FILE"; then
    prompt_fix_compose_ddl_auto
  fi

  if ! grep -Eq 'ddl-auto:[[:space:]]*\$\{SPRING_JPA_HIBERNATE_DDL_AUTO:update\}' "$RESOURCE_DIR/application-local.yml"; then
    prompt_fix_application_local_ddl_auto
  fi

  echo "Local JPA ddl-auto is configured as update."
}

prompt_fix_compose_ddl_auto() {
  if [[ ! -t 0 ]]; then
    echo "ERROR: docker-compose.backend.local.yml must set SPRING_JPA_HIBERNATE_DDL_AUTO: update." >&2
    echo "Rerun in a terminal to choose whether to update it automatically." >&2
    exit 1
  fi

  local answer
  read -r -p "docker-compose.backend.local.yml의 SPRING_JPA_HIBERNATE_DDL_AUTO를 update로 변경할까요? [y/N] " answer
  case "$answer" in
    [yY]|[yY][eE][sS])
      set_compose_ddl_auto_update
      echo "docker-compose.backend.local.yml의 ddl-auto 설정을 update로 맞췄습니다."
      ;;
    *)
      echo "ERROR: local backend requires SPRING_JPA_HIBERNATE_DDL_AUTO: update." >&2
      exit 1
      ;;
  esac
}

set_compose_ddl_auto_update() {
  local tmp_file
  tmp_file="$(mktemp "${TMPDIR:-/tmp}/docker-compose.backend.local.yml.XXXXXX")"

  awk '
    /^[[:space:]]*SPRING_JPA_HIBERNATE_DDL_AUTO:[[:space:]]*/ {
      sub(/SPRING_JPA_HIBERNATE_DDL_AUTO:.*/, "SPRING_JPA_HIBERNATE_DDL_AUTO: update")
      found = 1
      print
      next
    }
    /^[[:space:]]*SPRING_DATA_REDIS_PORT:[[:space:]]*/ {
      print
      if (!found) {
        indent = $0
        sub(/SPRING_DATA_REDIS_PORT:.*/, "", indent)
        print indent "SPRING_JPA_HIBERNATE_DDL_AUTO: update"
        found = 1
      }
      next
    }
    { print }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "$COMPOSE_FILE" > "$tmp_file" || {
    rm -f "$tmp_file"
    echo "ERROR: could not update SPRING_JPA_HIBERNATE_DDL_AUTO automatically." >&2
    exit 1
  }

  mv "$tmp_file" "$COMPOSE_FILE"
}

prompt_fix_application_local_ddl_auto() {
  if [[ ! -t 0 ]]; then
    echo "ERROR: application-local.yml must default spring.jpa.hibernate.ddl-auto to update." >&2
    echo "Rerun in a terminal to choose whether to update it automatically." >&2
    exit 1
  fi

  local answer
  read -r -p "application-local.yml의 ddl-auto 기본값을 update로 변경할까요? [y/N] " answer
  case "$answer" in
    [yY]|[yY][eE][sS])
      set_application_local_ddl_auto_update
      echo "application-local.yml의 ddl-auto 기본값을 update로 맞췄습니다."
      ;;
    *)
      echo "ERROR: local profile requires spring.jpa.hibernate.ddl-auto default update." >&2
      exit 1
      ;;
  esac
}

set_application_local_ddl_auto_update() {
  local tmp_file
  tmp_file="$(mktemp "${TMPDIR:-/tmp}/application-local.yml.XXXXXX")"

  awk '
    /^[[:space:]]*ddl-auto:[[:space:]]*/ {
      sub(/ddl-auto:.*/, "ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}")
      found = 1
      print
      next
    }
    { print }
    END {
      if (!found) {
        exit 1
      }
    }
  ' "$RESOURCE_DIR/application-local.yml" > "$tmp_file" || {
    rm -f "$tmp_file"
    echo "ERROR: could not update application-local.yml ddl-auto automatically." >&2
    exit 1
  }

  mv "$tmp_file" "$RESOURCE_DIR/application-local.yml"
}

run_compose() {
  COMPOSE_PROJECT_NAME="$COMPOSE_PROJECT_NAME" "${COMPOSE[@]}" -f "$COMPOSE_FILE" "$@"
}

backend_logs_tail() {
  local lines="${1:-160}"

  run_compose logs --tail="$lines" seeandyougo 2>&1 || true
}

log_contains() {
  local logs="$1"
  local pattern="$2"

  printf '%s\n' "$logs" | grep -Eiq "$pattern"
}

classify_backend_failure() {
  local logs="$1"

  echo
  echo "백엔드 기동 실패 원인 분류:"

  if log_contains "$logs" "key\\.yml|Jasypt|encryptor|Could not resolve placeholder|BindException"; then
    echo "- 설정/secret 문제 가능성이 큽니다."
    echo "- key.yml, application-local.yml, 환경변수 값을 먼저 확인하세요."
    echo "- DB 볼륨 삭제 대상이 아닙니다."
    return
  fi

  if log_contains "$logs" "Communications link failure|Access denied|Unknown database|HikariPool.*Exception|Connection refused.*mysql"; then
    echo "- MySQL 연결 또는 초기화 문제 가능성이 큽니다."
    echo "- MySQL 컨테이너 상태, datasource URL, 포트, 계정을 먼저 확인하세요."
    echo "- 바로 DB 볼륨 삭제로 가지 말고 원인 확인이 먼저입니다."
    return
  fi

  if log_contains "$logs" "Table .* doesn't exist|Unknown column|Duplicate column|Schema-validation|SchemaManagementException|Duplicate entry|Data truncation|constraint.*fails"; then
    echo "- DB 스키마/데이터와 현재 코드가 충돌하는 문제일 수 있습니다."
    echo "- MySQL이 healthy인데 같은 에러가 반복되면 DB 볼륨 리셋 후보입니다."
    echo "- 자동으로 볼륨 삭제는 하지 않습니다."
    return
  fi

  if log_contains "$logs" "Failed to execute CommandLineRunner|NoSuchFileException|menuOfRestaurant1\\.json|Failed to parse"; then
    echo "- 애플리케이션 초기 데이터 로딩 또는 리소스 파일 문제 가능성이 큽니다."
    echo "- 누락 파일, DataLoader, 외부 API 응답을 먼저 확인하세요."
    echo "- DB 볼륨 삭제로 해결될 가능성은 낮습니다."
    return
  fi

  if log_contains "$logs" "RedisConnectionFailureException|Unable to connect to Redis|Connection refused.*redis"; then
    echo "- Redis 연결 문제 가능성이 큽니다."
    echo "- Redis 컨테이너 상태와 SPRING_DATA_REDIS_* 설정을 확인하세요."
    echo "- DB 볼륨 삭제 대상이 아닙니다."
    return
  fi

  echo "- 알려진 패턴으로 분류되지 않았습니다."
  echo "- 아래 최근 로그를 기준으로 원인을 확인하세요."
  echo "- DB 볼륨 삭제는 스키마/데이터 충돌이 확인될 때만 고려하세요."
}

monitor_backend_startup() {
  local attempts="${BACKEND_READY_ATTEMPTS:-45}"
  local interval="${BACKEND_READY_INTERVAL_SECONDS:-2}"
  local logs=""
  local state=""
  local http_ready=0
  local spring_started=0
  local local_profile=0
  local initial_setup_done=0
  local reported_http_ready=0
  local reported_spring_started=0
  local reported_local_profile=0
  local reported_initial_setup_done=0

  echo
  echo "백엔드 기동 상태를 확인합니다."
  echo "- local profile, Spring 시작, DataLoader 초기세팅, HTTP 응답을 순서대로 확인합니다."
  echo "- 준비 확인은 최대 $((attempts * interval))초 동안 진행합니다."

  for ((i = 1; i <= attempts; i++)); do
    logs="$(backend_logs_tail 220)"
    state="$("${CONTAINER[@]}" inspect -f '{{.State.Status}} {{.State.Restarting}}' seeandyougo-local-backend 2>/dev/null || true)"

    if log_contains "$logs" 'The following 1 profile is active: "local"'; then
      local_profile=1
      if [[ "$reported_local_profile" -eq 0 ]]; then
        echo "- local profile 확인 완료"
        reported_local_profile=1
      fi
    fi

    if log_contains "$logs" "Started SeeAndYouGoApplication|Tomcat started on port\\(s\\): 8080"; then
      spring_started=1
      if [[ "$reported_spring_started" -eq 0 ]]; then
        echo "- Spring/Tomcat 시작 확인 완료"
        reported_spring_started=1
      fi
    fi

    if log_contains "$logs" "DataLoader - 초기세팅 완료|초기세팅 완료"; then
      initial_setup_done=1
      if [[ "$reported_initial_setup_done" -eq 0 ]]; then
        echo "- DataLoader 초기세팅 완료 확인"
        reported_initial_setup_done=1
      fi
    fi

    if command -v curl >/dev/null 2>&1 && curl -fsS http://localhost:8080/v3/api-docs >/dev/null 2>&1; then
      http_ready=1
      if [[ "$reported_http_ready" -eq 0 ]]; then
        echo "- HTTP /v3/api-docs 응답 확인 완료"
        reported_http_ready=1
      fi
    fi

    if [[ "$http_ready" -eq 1 && "$spring_started" -eq 1 && "$initial_setup_done" -eq 1 ]]; then
      echo "백엔드 기동 확인 완료."
      return 0
    fi

    if log_contains "$logs" "Application run failed|Failed to execute CommandLineRunner"; then
      echo "ERROR: 백엔드 애플리케이션 기동 실패 로그가 감지되었습니다." >&2
      echo
      echo "최근 백엔드 로그:"
      printf '%s\n' "$logs"
      classify_backend_failure "$logs"
      return 1
    fi

    if [[ "$state" == exited* || "$state" == *"true" ]]; then
      echo "ERROR: 백엔드 컨테이너 상태가 비정상입니다: ${state:-unknown}" >&2
      echo
      echo "최근 백엔드 로그:"
      printf '%s\n' "$logs"
      classify_backend_failure "$logs"
      return 1
    fi

    if [[ "$i" -eq 1 || $((i % 5)) -eq 0 ]]; then
      echo "- 백엔드 준비 대기 중..."
    fi
    sleep "$interval"
  done

  logs="$(backend_logs_tail 220)"
  echo "ERROR: 제한 시간 안에 백엔드 준비 상태를 확인하지 못했습니다." >&2
  echo
  echo "최근 백엔드 로그:"
  printf '%s\n' "$logs"
  classify_backend_failure "$logs"
  return 1
}

if command -v docker >/dev/null 2>&1; then
  CONTAINER=(docker)
  if docker compose version >/dev/null 2>&1; then
    COMPOSE=(docker compose)
  elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE=(docker-compose)
  else
    echo "ERROR: docker compose plugin or docker-compose is required." >&2
    exit 1
  fi
elif command -v podman >/dev/null 2>&1; then
  CONTAINER=(podman)
  if podman compose version >/dev/null 2>&1; then
    COMPOSE=(podman compose)
  elif command -v podman-compose >/dev/null 2>&1; then
    COMPOSE=(podman-compose)
  else
    echo "ERROR: docker is unavailable, and podman compose or podman-compose is required." >&2
    exit 1
  fi
else
  echo "ERROR: docker command is required. If Docker is unavailable, podman is required as a fallback." >&2
  exit 1
fi

echo "Using container runtime: ${CONTAINER[*]}"
echo "Using compose command: ${COMPOSE[*]}"
echo "Using compose project: $COMPOSE_PROJECT_NAME"

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

check_oauth_redirect_urls
check_local_ddl_auto_update

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
run_compose up -d --build mysql redis seeandyougo

monitor_backend_startup

cat <<MSG

로컬 백엔드 스택 준비 완료.
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs
- MySQL: localhost:3306 (database: seeandyougo, user: root, password: empty)
- Redis: localhost:6379
- 프론트 API base URL: http://localhost:8080
- JPA ddl-auto: update

Useful commands:
- Logs: COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME ${COMPOSE[*]} -f docker-compose.backend.local.yml logs -f seeandyougo
- Stop: COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME ${COMPOSE[*]} -f docker-compose.backend.local.yml down

[완료]
- Docker/Compose 확인 완료
- key.yml 확인 완료
- OAuth redirect URI 확인 완료
- JPA ddl-auto=update 확인 완료
- 포트 충돌 확인 완료
- Gradle bootJar 완료
- MySQL/Redis/backend 컨테이너 기동 완료
- 백엔드 HTTP readiness 확인 완료
MSG
