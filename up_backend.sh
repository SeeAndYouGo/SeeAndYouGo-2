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
        echo "${service} 포트 ${port}는 이미 ${allowed_container}에서 사용 중입니다. 기존 로컬 ${service} 컨테이너를 유지합니다."
        ;;
      "SeeAndYouGo backend")
        echo "백엔드 포트 ${port}는 이미 ${allowed_container}에서 사용 중입니다. 필요한 경우 compose가 백엔드를 다시 빌드하고 재시작합니다."
        ;;
      *)
        echo "포트 ${port}는 이미 ${allowed_container}에서 사용 중입니다. 기존 로컬 컨테이너를 유지하고 계속 진행합니다."
        ;;
    esac
    return
  fi

  if command -v lsof >/dev/null 2>&1; then
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      echo "오류: 포트 ${port}는 이미 사용 중이라 ${service}를 시작할 수 없습니다." >&2
      lsof -nP -iTCP:"$port" -sTCP:LISTEN >&2 || true
      exit 1
    fi
  elif command -v ss >/dev/null 2>&1; then
    if ss -ltn "sport = :$port" | awk 'NR > 1 { found = 1 } END { exit !found }'; then
      echo "오류: 포트 ${port}는 이미 사용 중이라 ${service}를 시작할 수 없습니다." >&2
      ss -ltnp "sport = :$port" >&2 || true
      exit 1
    fi
  elif command -v netstat >/dev/null 2>&1; then
    if netstat -ltn 2>/dev/null | awk -v port=":$port" '$4 ~ port"$" { found = 1 } END { exit !found }'; then
      echo "오류: 포트 ${port}는 이미 사용 중이라 ${service}를 시작할 수 없습니다." >&2
      netstat -ltnp 2>/dev/null | awk -v port=":$port" '$4 ~ port"$"' >&2 || true
      exit 1
    fi
  else
    echo "경고: lsof, ss, netstat을 사용할 수 없어 ${service}(${port}) 로컬 포트 검사를 건너뜁니다." >&2
  fi
}

check_container_name_available() {
  local container_name="$1"

  if "${CONTAINER[@]}" ps -a --format '{{.Names}}' | grep -Fxq "$container_name"; then
    local state
    state="$("${CONTAINER[@]}" inspect -f '{{.State.Status}}' "$container_name" 2>/dev/null || echo unknown)"
    if [[ "$state" == "restarting" ]]; then
      echo "컨테이너 ${container_name}이 재시작 중입니다. compose가 재생성 또는 재시작을 시도합니다."
    elif [[ "$state" != "running" && "$state" != "exited" && "$state" != "created" ]]; then
      echo "오류: 컨테이너 이름 ${container_name}이 예상하지 못한 상태로 이미 존재합니다: ${state}" >&2
      exit 1
    fi
  fi
}

check_container_runtime_access() {
  if ! "${CONTAINER[@]}" ps >/dev/null 2>&1; then
    echo "오류: ${CONTAINER[*]} 컨테이너 목록을 조회할 수 없습니다." >&2
    echo "Docker Desktop/daemon 실행 상태와 현재 터미널의 Docker 접근 권한을 확인한 뒤 다시 실행하세요." >&2
    exit 1
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
    echo "오류: backend/src/main/resources/key.yml에 $label REDIRECT_URI가 없습니다." >&2
    return 1
  fi

  if [[ -z "$frontend_redirect" ]]; then
    echo "오류: ${FRONTEND_ENV_FILE}에 ${frontend_key} 값이 없거나 비어 있습니다." >&2
    return 1
  fi

  if [[ "$backend_redirect" != "$frontend_redirect" ]]; then
    echo "오류: key.yml과 ${FRONTEND_ENV_FILE}의 ${label} redirect URI가 일치하지 않습니다(${frontend_key})." >&2
    return 2
  fi

  echo "$label redirect URI가 frontend .env와 일치합니다."
  return 0
}

check_oauth_redirect_urls() {
  if [[ ! -f "$FRONTEND_ENV_FILE" ]]; then
    echo "오류: OAuth redirect URI 검증에 frontend env 파일이 필요합니다: $FRONTEND_ENV_FILE" >&2
    echo "frontend env 파일 위치가 다르면 FRONTEND_ENV_FILE=/path/to/.env로 지정하세요." >&2
    exit 1
  fi

  echo "OAuth redirect URI 일치 여부를 확인합니다..."
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
      echo "오류: OAuth redirect URI 불일치가 감지되었습니다. key.yml의 REDIRECT_URI를 frontend .env 값과 맞춘 뒤 다시 실행하세요." >&2
      exit 1
    fi

    local answer
    read -r -p "frontend .env 기준으로 key.yml의 OAuth redirect URI를 동기화할까요? [y/N] " answer
    case "$answer" in
      [yY]|[yY][eE][sS])
        update_key_redirect_uri "kakao" "$(read_frontend_env_value "REACT_APP_KAKAO_REDIRECT_URI")"
        update_key_redirect_uri "google" "$(read_frontend_env_value "REACT_APP_GOOGLE_REDIRECT_URI")"
        echo "frontend .env 기준으로 key.yml OAuth redirect URI를 업데이트했습니다."
        check_redirect_uri_match "Kakao" "kakao" "REACT_APP_KAKAO_REDIRECT_URI" || exit 1
        check_redirect_uri_match "Google" "google" "REACT_APP_GOOGLE_REDIRECT_URI" || exit 1
        ;;
      *)
        echo "오류: OAuth redirect URI 불일치가 수정되지 않았습니다." >&2
        exit 1
        ;;
    esac
  fi
}

read_compose_ddl_auto() {
  awk '
    /^[[:space:]]*SPRING_JPA_HIBERNATE_DDL_AUTO:[[:space:]]*/ {
      line = $0
      sub(/^[[:space:]]*SPRING_JPA_HIBERNATE_DDL_AUTO:[[:space:]]*/, "", line)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
      print line
      exit
    }
  ' "$COMPOSE_FILE"
}

read_application_local_ddl_auto() {
  awk '
    /^[[:space:]]*ddl-auto:[[:space:]]*/ {
      line = $0
      sub(/^[[:space:]]*ddl-auto:[[:space:]]*/, "", line)
      gsub(/^[[:space:]]+|[[:space:]]+$/, "", line)
      print line
      exit
    }
  ' "$RESOURCE_DIR/application-local.yml"
}

confirm_non_update_setting() {
  local file_path="$1"
  local label="$2"
  local current_value="$3"

  if [[ ! -t 0 ]]; then
    echo "오류: ${file_path}의 ${label} 설정은 ${current_value:-없음}입니다." >&2
    echo "권장값은 update입니다. 진행 여부를 선택하려면 대화형 터미널에서 다시 실행하세요." >&2
    exit 1
  fi

  local answer
  read -r -p "${file_path}의 ${label} 설정은 ${current_value:-없음}입니다. 진행하시겠습니까? (권장: update) [y/N] " answer
  case "$answer" in
    [yY]|[yY][eE][sS])
      echo "${file_path}의 ${label} 설정이 update가 아니지만 계속 진행합니다."
      ;;
    *)
      echo "오류: ${file_path}의 ${label} 설정을 update로 맞춘 뒤 다시 실행하세요." >&2
      exit 1
      ;;
  esac
}

check_local_ddl_auto_update() {
  local compose_ddl_auto
  local application_ddl_auto

  compose_ddl_auto="$(read_compose_ddl_auto)"
  application_ddl_auto="$(read_application_local_ddl_auto)"

  if [[ "$compose_ddl_auto" == "update" ]]; then
    echo "docker-compose.backend.local.yml의 SPRING_JPA_HIBERNATE_DDL_AUTO 설정은 update입니다."
  else
    confirm_non_update_setting "docker-compose.backend.local.yml" "SPRING_JPA_HIBERNATE_DDL_AUTO" "$compose_ddl_auto"
  fi

  if [[ "$application_ddl_auto" == '${SPRING_JPA_HIBERNATE_DDL_AUTO:update}' ]]; then
    echo "backend/src/main/resources/application-local.yml의 ddl-auto 기본값은 update입니다."
  else
    confirm_non_update_setting "backend/src/main/resources/application-local.yml" "ddl-auto 기본값" "$application_ddl_auto"
  fi
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
    echo "- 설정/시크릿 문제 가능성이 큽니다."
    echo "- key.yml, application-local.yml, 환경변수 값을 먼저 확인하세요."
    return
  fi

  if log_contains "$logs" "Communications link failure|Access denied|Unknown database|HikariPool.*Exception|Connection refused.*mysql"; then
    echo "- MySQL 연결 또는 초기화 문제 가능성이 큽니다."
    echo "- MySQL 컨테이너 상태, datasource URL, 포트, 계정을 먼저 확인하세요."
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
    return
  fi

  if log_contains "$logs" "RedisConnectionFailureException|Unable to connect to Redis|Connection refused.*redis"; then
    echo "- Redis 연결 문제 가능성이 큽니다."
    echo "- Redis 컨테이너 상태와 SPRING_DATA_REDIS_* 설정을 확인하세요."
    return
  fi

  echo "- 알려진 패턴으로 분류되지 않았습니다."
  echo "- 아래 최근 로그를 기준으로 원인을 확인하세요."
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
  echo "- local 프로필, Spring 시작, DataLoader 초기세팅, HTTP 응답을 순서대로 확인합니다."
  echo "- 준비 확인은 최대 $((attempts * interval))초 동안 진행합니다."

  for ((i = 1; i <= attempts; i++)); do
    logs="$(backend_logs_tail 220)"
    state="$("${CONTAINER[@]}" inspect -f '{{.State.Status}} {{.State.Restarting}}' seeandyougo-local-backend 2>/dev/null || true)"

    if log_contains "$logs" 'The following 1 profile is active: "local"'; then
      local_profile=1
      if [[ "$reported_local_profile" -eq 0 ]]; then
        echo "- local 프로필 확인 완료"
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
      echo "오류: 백엔드 애플리케이션 기동 실패 로그가 감지되었습니다." >&2
      echo
      echo "최근 백엔드 로그:"
      printf '%s\n' "$logs"
      classify_backend_failure "$logs"
      return 1
    fi

    if [[ "$state" == exited* || "$state" == *"true" ]]; then
      echo "오류: 백엔드 컨테이너 상태가 비정상입니다: ${state:-unknown}" >&2
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
  echo "오류: 제한 시간 안에 백엔드 준비 상태를 확인하지 못했습니다." >&2
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
    echo "오류: docker compose 플러그인 또는 docker-compose가 필요합니다." >&2
    exit 1
  fi
elif command -v podman >/dev/null 2>&1; then
  CONTAINER=(podman)
  if podman compose version >/dev/null 2>&1; then
    COMPOSE=(podman compose)
  elif command -v podman-compose >/dev/null 2>&1; then
    COMPOSE=(podman-compose)
  else
    echo "오류: docker를 사용할 수 없으며 podman compose 또는 podman-compose가 필요합니다." >&2
    exit 1
  fi
else
  echo "오류: docker 명령이 필요합니다. docker를 사용할 수 없으면 대체 수단으로 podman이 필요합니다." >&2
  exit 1
fi

echo "컨테이너 런타임: ${CONTAINER[*]}"
echo "Compose 명령: ${COMPOSE[*]}"
echo "Compose 프로젝트: $COMPOSE_PROJECT_NAME"
check_container_runtime_access

if [[ ! -f "$RESOURCE_DIR/key.yml" ]]; then
  if [[ -f "$RESOURCE_DIR/key.yaml" ]]; then
    echo "backend/src/main/resources/key.yml이 없어 Spring classpath import용으로 key.yaml을 key.yml에 복사합니다."
    cp "$RESOURCE_DIR/key.yaml" "$RESOURCE_DIR/key.yml"
  else
    cat >&2 <<'MSG'
오류: 백엔드를 시작하려면 backend/src/main/resources/key.yml이 필요합니다.
로컬 시크릿 파일 이름이 key.yaml이라면 backend/src/main/resources/key.yaml에 배치한 뒤
이 스크립트를 다시 실행하세요. 현재 Spring 설정에 맞춰 key.yml로 복사합니다.
MSG
    exit 1
  fi
fi

check_oauth_redirect_urls
check_local_ddl_auto_update

echo "로컬 포트 사용 가능 여부를 확인합니다..."
check_port_available 3306 "MySQL" "seeandyougo-local-mysql"
check_port_available 6379 "Redis" "seeandyougo-local-redis"
check_port_available 8080 "SeeAndYouGo backend" "seeandyougo-local-backend"

check_container_name_available "seeandyougo-local-mysql"
check_container_name_available "seeandyougo-local-redis"
check_container_name_available "seeandyougo-local-backend"

echo "최신 백엔드 jar를 빌드합니다..."
(cd "$BACKEND_DIR" && ./gradlew clean bootJar -x test)

echo "로컬 백엔드 스택(MySQL + Redis + SeeAndYouGo)을 시작합니다..."
run_compose up -d --build mysql redis seeandyougo

monitor_backend_startup

cat <<MSG

로컬 백엔드 스택 준비 완료.
- 백엔드 API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API 문서: http://localhost:8080/v3/api-docs
- MySQL: localhost:3306 (데이터베이스: seeandyougo, 사용자: root, 비밀번호: 없음)
- Redis: localhost:6379
- 프론트엔드 API 기본 URL: http://localhost:8080
- JPA ddl-auto: update

유용한 명령어:
- 로그 확인: COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME ${COMPOSE[*]} -f docker-compose.backend.local.yml logs -f seeandyougo
- 종료: COMPOSE_PROJECT_NAME=$COMPOSE_PROJECT_NAME ${COMPOSE[*]} -f docker-compose.backend.local.yml down

[검증 완료]
- Docker/Compose 확인 완료
- key.yml 확인 완료
- OAuth redirect URI 확인 완료
- JPA ddl-auto=update 확인 완료
- 포트 충돌 확인 완료
- Gradle bootJar 완료
- MySQL/Redis/backend 컨테이너 기동 완료
- 백엔드 HTTP readiness 확인 완료
MSG
