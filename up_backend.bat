@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

for %%I in ("%~dp0.") do set "ROOT_DIR=%%~fI"
set "BACKEND_DIR=%ROOT_DIR%\backend"
set "RESOURCE_DIR=%BACKEND_DIR%\src\main\resources"
set "COMPOSE_FILE=%ROOT_DIR%\docker-compose.backend.local.yml"

if not defined FRONTEND_ENV_FILE set "FRONTEND_ENV_FILE=%ROOT_DIR%\frontend\.env"
if not defined COMPOSE_PROJECT_NAME set "COMPOSE_PROJECT_NAME=seeandyougo-local-backend"

cd /d "%ROOT_DIR%" || exit /b 1

if "%~1"=="--self-test" (
  echo up_backend.bat 자체 테스트 통과.
  exit /b 0
)

call :detect_runtime || exit /b 1

echo(컨테이너 런타임: %CONTAINER_CMD%
echo(Compose 명령: %COMPOSE_CMD%
echo(Compose 프로젝트: %COMPOSE_PROJECT_NAME%
call :check_container_runtime_access || exit /b 1

if not exist "%RESOURCE_DIR%\key.yml" (
  if exist "%RESOURCE_DIR%\key.yaml" (
    echo backend/src/main/resources/key.yml이 없어 Spring classpath import용으로 key.yaml을 key.yml에 복사합니다.
    copy /Y "%RESOURCE_DIR%\key.yaml" "%RESOURCE_DIR%\key.yml" >nul || exit /b 1
  ) else (
    echo 오류: 백엔드를 시작하려면 backend/src/main/resources/key.yml이 필요합니다. 1>&2
    echo 로컬 시크릿 파일 이름이 key.yaml이라면 backend/src/main/resources/key.yaml에 배치한 뒤 다시 실행하세요. 1>&2
    exit /b 1
  )
)

call :check_oauth_redirect_urls || exit /b 1
call :check_local_ddl_auto_update || exit /b 1

echo 로컬 포트 사용 가능 여부를 확인합니다...
call :check_port_available 3306 MySQL seeandyougo-local-mysql || exit /b 1
call :check_port_available 6379 Redis seeandyougo-local-redis || exit /b 1
call :check_port_available 8080 "SeeAndYouGo backend" seeandyougo-local-backend || exit /b 1

call :check_container_name_available seeandyougo-local-mysql || exit /b 1
call :check_container_name_available seeandyougo-local-redis || exit /b 1
call :check_container_name_available seeandyougo-local-backend || exit /b 1

echo 최신 백엔드 jar를 빌드합니다...
pushd "%BACKEND_DIR%" || exit /b 1
if exist ".\gradlew.bat" (
  call .\gradlew.bat clean bootJar -x test
) else (
  where gradle >nul 2>nul
  if errorlevel 1 (
    popd
    echo 오류: Gradle wrapper 또는 gradle 명령이 필요합니다. 1>&2
    exit /b 1
  )
  gradle clean bootJar -x test
)
set "BUILD_EXIT=%ERRORLEVEL%"
popd
if not "%BUILD_EXIT%"=="0" exit /b %BUILD_EXIT%

echo 로컬 백엔드 스택(MySQL + Redis + SeeAndYouGo)을 시작합니다...
%COMPOSE_CMD% -f "%COMPOSE_FILE%" up -d --build mysql redis seeandyougo
if errorlevel 1 exit /b %ERRORLEVEL%

call :monitor_backend_startup || exit /b 1

echo.
echo 로컬 백엔드 스택 준비 완료.
echo - 백엔드 API: http://localhost:8080
echo - Swagger UI: http://localhost:8080/swagger-ui/index.html
echo - API 문서: http://localhost:8080/v3/api-docs
echo - MySQL: localhost:3306 (데이터베이스: seeandyougo, 사용자: root, 비밀번호: 없음)
echo - Redis: localhost:6379
echo - 프론트엔드 API 기본 URL: http://localhost:8080
echo - JPA ddl-auto: update
echo.
echo 유용한 명령어:
echo - 로그 확인: set "COMPOSE_PROJECT_NAME=%COMPOSE_PROJECT_NAME%" ^&^& %COMPOSE_CMD% -f docker-compose.backend.local.yml logs -f seeandyougo
echo - 종료: set "COMPOSE_PROJECT_NAME=%COMPOSE_PROJECT_NAME%" ^&^& %COMPOSE_CMD% -f docker-compose.backend.local.yml down
echo.
echo [검증 완료]
echo - Docker/Compose 확인 완료
echo - key.yml 확인 완료
echo - OAuth redirect URI 확인 완료
echo - JPA ddl-auto=update 확인 완료
echo - 포트 충돌 확인 완료
echo - Gradle bootJar 완료
echo - MySQL/Redis/backend 컨테이너 기동 완료
echo - 백엔드 HTTP readiness 확인 완료
exit /b 0

:detect_runtime
where docker >nul 2>nul
if not errorlevel 1 (
  set "CONTAINER_CMD=docker"
  docker compose version >nul 2>nul
  if not errorlevel 1 (
    set "COMPOSE_CMD=docker compose"
    exit /b 0
  )
  where docker-compose >nul 2>nul
  if not errorlevel 1 (
    set "COMPOSE_CMD=docker-compose"
    exit /b 0
  )
  echo 오류: docker compose 플러그인 또는 docker-compose가 필요합니다. 1>&2
  exit /b 1
)

where podman >nul 2>nul
if not errorlevel 1 (
  set "CONTAINER_CMD=podman"
  podman compose version >nul 2>nul
  if not errorlevel 1 (
    set "COMPOSE_CMD=podman compose"
    exit /b 0
  )
  where podman-compose >nul 2>nul
  if not errorlevel 1 (
    set "COMPOSE_CMD=podman-compose"
    exit /b 0
  )
  echo 오류: docker를 사용할 수 없으며 podman compose 또는 podman-compose가 필요합니다. 1>&2
  exit /b 1
)

echo 오류: docker 명령이 필요합니다. docker를 사용할 수 없으면 대체 수단으로 podman이 필요합니다. 1>&2
exit /b 1

:check_container_runtime_access
%CONTAINER_CMD% ps >nul 2>nul
if errorlevel 1 (
  echo(오류: %CONTAINER_CMD% 컨테이너 목록을 조회할 수 없습니다. 1>&2
  echo(Docker Desktop/daemon 실행 상태와 현재 터미널의 Docker 접근 권한을 확인한 뒤 다시 실행하세요. 1>&2
  exit /b 1
)
exit /b 0

:check_oauth_redirect_urls
if not exist "%FRONTEND_ENV_FILE%" (
  echo 오류: OAuth redirect URI 검증에 frontend env 파일이 필요합니다: %FRONTEND_ENV_FILE% 1>&2
  echo frontend env 파일 위치가 다르면 FRONTEND_ENV_FILE=C:\path\to\.env로 지정하세요. 1>&2
  exit /b 1
)

echo OAuth redirect URI 일치 여부를 확인합니다...
set "OAUTH_FAILED=0"
set "OAUTH_MISMATCH=0"

call :check_redirect_uri_match Kakao kakao REACT_APP_KAKAO_REDIRECT_URI
set "OAUTH_CHECK_RESULT=%ERRORLEVEL%"
if "%OAUTH_CHECK_RESULT%"=="2" (
  set "OAUTH_MISMATCH=1"
)
if not "%OAUTH_CHECK_RESULT%"=="0" if not "%OAUTH_CHECK_RESULT%"=="2" (
  set "OAUTH_FAILED=1"
)

call :check_redirect_uri_match Google google REACT_APP_GOOGLE_REDIRECT_URI
set "OAUTH_CHECK_RESULT=%ERRORLEVEL%"
if "%OAUTH_CHECK_RESULT%"=="2" (
  set "OAUTH_MISMATCH=1"
)
if not "%OAUTH_CHECK_RESULT%"=="0" if not "%OAUTH_CHECK_RESULT%"=="2" (
  set "OAUTH_FAILED=1"
)

if "%OAUTH_FAILED%"=="1" exit /b 1
if "%OAUTH_MISMATCH%"=="1" (
  call :prompt_sync_oauth_redirect_urls
  if errorlevel 1 exit /b 1
)
exit /b 0

:check_redirect_uri_match
set "LABEL=%~1"
set "KEY_SECTION=%~2"
set "FRONTEND_KEY=%~3"

call :read_key_redirect_uri "%KEY_SECTION%"
set "BACKEND_REDIRECT=%KEY_RESULT%"
call :read_frontend_env_value "%FRONTEND_KEY%"
set "FRONTEND_REDIRECT=%ENV_RESULT%"

if not defined BACKEND_REDIRECT (
  echo 오류: backend/src/main/resources/key.yml에 %LABEL% REDIRECT_URI가 없습니다. 1>&2
  exit /b 1
)

if not defined FRONTEND_REDIRECT (
  echo 오류: %FRONTEND_ENV_FILE%에 %FRONTEND_KEY% 값이 없거나 비어 있습니다. 1>&2
  exit /b 1
)

if not "%BACKEND_REDIRECT%"=="%FRONTEND_REDIRECT%" (
  echo 오류: key.yml과 %FRONTEND_ENV_FILE%의 %LABEL% redirect URI가 일치하지 않습니다^(%FRONTEND_KEY%^). 1>&2
  exit /b 2
)

echo %LABEL% redirect URI가 frontend .env와 일치합니다.
exit /b 0

:prompt_sync_oauth_redirect_urls
choice /c YN /n /m "frontend .env 기준으로 key.yml의 OAuth redirect URI를 동기화할까요? [y/N] "
if errorlevel 2 (
  echo 오류: OAuth redirect URI 불일치가 수정되지 않았습니다. 1>&2
  exit /b 1
)

call :read_frontend_env_value REACT_APP_KAKAO_REDIRECT_URI
set "KAKAO_REDIRECT=%ENV_RESULT%"
call :read_frontend_env_value REACT_APP_GOOGLE_REDIRECT_URI
set "GOOGLE_REDIRECT=%ENV_RESULT%"

call :update_key_redirect_uri kakao "%KAKAO_REDIRECT%"
if errorlevel 1 exit /b 1
call :update_key_redirect_uri google "%GOOGLE_REDIRECT%"
if errorlevel 1 exit /b 1
echo frontend .env 기준으로 key.yml OAuth redirect URI를 업데이트했습니다.
call :check_redirect_uri_match Kakao kakao REACT_APP_KAKAO_REDIRECT_URI
if errorlevel 1 exit /b 1
call :check_redirect_uri_match Google google REACT_APP_GOOGLE_REDIRECT_URI
if errorlevel 1 exit /b 1
exit /b 0

:update_key_redirect_uri
setlocal DisableDelayedExpansion
set "TARGET_SECTION=%~1"
set "TARGET_REDIRECT=%~2"
set "KEY_TMP=%TEMP%\key-%RANDOM%-%RANDOM%.yml"
set "CURRENT_SECTION="

type nul > "%KEY_TMP%"
for /f "usebackq tokens=1,* delims=]" %%A in (`find /n /v "" "%RESOURCE_DIR%\key.yml"`) do call :rewrite_key_redirect_line "%%B" >> "%KEY_TMP%"
move /Y "%KEY_TMP%" "%RESOURCE_DIR%\key.yml" >nul
endlocal
exit /b %ERRORLEVEL%

:rewrite_key_redirect_line
set "RAW_LINE=%~1"
set "NO_SPACE=%RAW_LINE: =%"
if "%RAW_LINE%"=="" goto :rewrite_key_redirect_line_after_section
if "%RAW_LINE:~0,1%"==" " goto :rewrite_key_redirect_line_after_section
if "%RAW_LINE:~-1%"==":" set "CURRENT_SECTION=%RAW_LINE:~0,-1%"

:rewrite_key_redirect_line_after_section
if /I "%CURRENT_SECTION%"=="%TARGET_SECTION%" goto :rewrite_key_redirect_line_section_match
goto :rewrite_key_redirect_line_raw

:rewrite_key_redirect_line_section_match
if /I "%NO_SPACE:~0,13%"=="REDIRECT_URI:" goto :rewrite_key_redirect_line_replace
goto :rewrite_key_redirect_line_raw

:rewrite_key_redirect_line_replace
echo(  REDIRECT_URI: %TARGET_REDIRECT%
exit /b 0

:rewrite_key_redirect_line_raw
echo(%RAW_LINE%
exit /b 0

:read_key_redirect_uri
set "KEY_RESULT="
set "CURRENT_SECTION="
for /f "usebackq delims=" %%L in ("%RESOURCE_DIR%\key.yml") do (
  set "LINE=%%L"
  set "WORK=!LINE!"
  call :trim WORK
  if not "!WORK!"=="" (
    if not "!WORK:~0,1!"=="#" (
      if "!WORK:~-1!"==":" (
        set "CURRENT_SECTION=!WORK:~0,-1!"
      )
      if not "!WORK:~-1!"==":" if /I "!CURRENT_SECTION!"=="%~1" (
        if /I "!WORK:~0,13!"=="REDIRECT_URI:" (
          set "VALUE=!WORK:*REDIRECT_URI:=!"
          call :trim VALUE
          set "KEY_RESULT=!VALUE!"
          exit /b 0
        )
      )
    )
  )
)
exit /b 0

:read_frontend_env_value
set "ENV_RESULT="
for /f "usebackq tokens=1* delims==" %%A in ("%FRONTEND_ENV_FILE%") do (
  set "NAME=%%A"
  set "VALUE=%%B"
  call :trim NAME
  if /I "!NAME:~0,7!"=="export " set "NAME=!NAME:~7!"
  call :trim NAME
  if not "!NAME!"=="" (
    if not "!NAME:~0,1!"=="#" (
      if /I "!NAME!"=="%~1" (
        call :trim VALUE
        set "ENV_RESULT=!VALUE!"
        exit /b 0
      )
    )
  )
)
exit /b 0

:trim
set "TRIM_VALUE=!%~1!"
for /f "tokens=* delims= " %%T in ("!TRIM_VALUE!") do set "TRIM_VALUE=%%T"
:trim_tail
if "!TRIM_VALUE:~-1!"==" " (
  set "TRIM_VALUE=!TRIM_VALUE:~0,-1!"
  goto :trim_tail
)
set "TRIM_VALUE=!TRIM_VALUE:"=!"
set "%~1=!TRIM_VALUE!"
exit /b 0

:check_local_ddl_auto_update
call :read_compose_ddl_auto
set "COMPOSE_DDL_AUTO=%COMPOSE_DDL_RESULT%"
call :read_application_local_ddl_auto
set "APPLICATION_DDL_AUTO=%APPLICATION_DDL_RESULT%"

if "%COMPOSE_DDL_AUTO%"=="update" (
  echo docker-compose.backend.local.yml의 SPRING_JPA_HIBERNATE_DDL_AUTO 설정은 update입니다.
) else (
  call :confirm_non_update_setting "docker-compose.backend.local.yml" "SPRING_JPA_HIBERNATE_DDL_AUTO" "%COMPOSE_DDL_AUTO%"
  if errorlevel 1 exit /b 1
)

if "%APPLICATION_DDL_AUTO%"=="${SPRING_JPA_HIBERNATE_DDL_AUTO:update}" (
  echo backend/src/main/resources/application-local.yml의 ddl-auto 기본값은 update입니다.
) else (
  call :confirm_non_update_setting "backend/src/main/resources/application-local.yml" "ddl-auto 기본값" "%APPLICATION_DDL_AUTO%"
  if errorlevel 1 exit /b 1
)

exit /b 0

:read_compose_ddl_auto
set "COMPOSE_DDL_RESULT="
for /f "usebackq tokens=1* delims=:" %%A in ("%COMPOSE_FILE%") do (
  set "DDL_NAME=%%A"
  set "DDL_VALUE=%%B"
  call :trim DDL_NAME
  if /I "!DDL_NAME!"=="SPRING_JPA_HIBERNATE_DDL_AUTO" (
    call :trim DDL_VALUE
    set "COMPOSE_DDL_RESULT=!DDL_VALUE!"
    exit /b 0
  )
)
exit /b 0

:read_application_local_ddl_auto
set "APPLICATION_DDL_RESULT="
for /f "usebackq tokens=1* delims=:" %%A in ("%RESOURCE_DIR%\application-local.yml") do (
  set "DDL_NAME=%%A"
  set "DDL_VALUE=%%B"
  call :trim DDL_NAME
  if /I "!DDL_NAME!"=="ddl-auto" (
    call :trim DDL_VALUE
    set "APPLICATION_DDL_RESULT=!DDL_VALUE!"
    exit /b 0
  )
)
exit /b 0

:confirm_non_update_setting
set "SETTING_FILE=%~1"
set "SETTING_LABEL=%~2"
set "SETTING_VALUE=%~3"
if not defined SETTING_VALUE set "SETTING_VALUE=없음"
choice /c YN /n /m "%SETTING_FILE%의 %SETTING_LABEL% 설정은 %SETTING_VALUE%입니다. 진행하시겠습니까? (권장: update) [y/N] "
if errorlevel 2 (
  echo 오류: %SETTING_FILE%의 %SETTING_LABEL% 설정을 update로 맞춘 뒤 다시 실행하세요. 1>&2
  exit /b 1
)
echo %SETTING_FILE%의 %SETTING_LABEL% 설정이 update가 아니지만 계속 진행합니다.
exit /b 0

:check_port_available
set "PORT=%~1"
set "SERVICE=%~2"
set "ALLOWED_CONTAINER=%~3"
set "ALLOWED_STATE="

for /f "usebackq delims=" %%S in (`%CONTAINER_CMD% inspect -f "{{.State.Status}}" "%ALLOWED_CONTAINER%" 2^>nul`) do set "ALLOWED_STATE=%%S"

if /I "%ALLOWED_STATE%"=="running" (
  if "%SERVICE%"=="SeeAndYouGo backend" (
    echo 백엔드 포트 %PORT%는 이미 %ALLOWED_CONTAINER%에서 사용 중입니다. 필요한 경우 compose가 백엔드를 다시 빌드하고 재시작합니다.
  ) else (
    echo %SERVICE% 포트 %PORT%는 이미 %ALLOWED_CONTAINER%에서 사용 중입니다. 기존 로컬 %SERVICE% 컨테이너를 유지합니다.
  )
  exit /b 0
)

netstat -ano -p tcp | findstr /r /c:":%PORT% .*LISTENING" >nul
if not errorlevel 1 (
  echo 오류: 포트 %PORT%는 이미 사용 중이라 %SERVICE%를 시작할 수 없습니다. 1>&2
  netstat -ano -p tcp ^| findstr /r /c:":%PORT% .*LISTENING" 1>&2
  exit /b 1
)
exit /b 0

:check_container_name_available
set "CONTAINER_NAME=%~1"
set "CONTAINER_STATE=unknown"
for /f "usebackq delims=" %%S in (`%CONTAINER_CMD% inspect -f "{{.State.Status}}" "%CONTAINER_NAME%" 2^>nul`) do set "CONTAINER_STATE=%%S"

if "%CONTAINER_STATE%"=="unknown" exit /b 0

if "%CONTAINER_STATE%"=="restarting" (
  echo 컨테이너 %CONTAINER_NAME%이 재시작 중입니다. compose가 재생성 또는 재시작을 시도합니다.
  exit /b 0
)

if "%CONTAINER_STATE%"=="running" exit /b 0
if "%CONTAINER_STATE%"=="exited" exit /b 0
if "%CONTAINER_STATE%"=="created" exit /b 0

echo 오류: 컨테이너 이름 %CONTAINER_NAME%이 예상하지 못한 상태로 이미 존재합니다: %CONTAINER_STATE% 1>&2
exit /b 1

:monitor_backend_startup
if not defined BACKEND_READY_ATTEMPTS set "BACKEND_READY_ATTEMPTS=45"
if not defined BACKEND_READY_INTERVAL_SECONDS set "BACKEND_READY_INTERVAL_SECONDS=2"
set /a READY_TIMEOUT=BACKEND_READY_ATTEMPTS * BACKEND_READY_INTERVAL_SECONDS
set "LOG_FILE=%TEMP%\seeandyougo-backend-logs-%RANDOM%.txt"
set "STATE_FILE=%TEMP%\seeandyougo-backend-state-%RANDOM%.txt"
set "HTTP_READY=0"
set "SPRING_STARTED=0"
set "INITIAL_SETUP_DONE=0"
set "REPORTED_LOCAL_PROFILE=0"
set "REPORTED_SPRING_STARTED=0"
set "REPORTED_INITIAL_SETUP_DONE=0"
set "REPORTED_HTTP_READY=0"

echo.
echo 백엔드 기동 상태를 확인합니다.
echo - local 프로필, Spring 시작, DataLoader 초기세팅, HTTP 응답을 순서대로 확인합니다.
echo - 준비 확인은 최대 %READY_TIMEOUT%초 동안 진행합니다.

for /l %%I in (1,1,%BACKEND_READY_ATTEMPTS%) do (
  %COMPOSE_CMD% -f "%COMPOSE_FILE%" logs --tail=220 seeandyougo > "!LOG_FILE!" 2>&1
  %CONTAINER_CMD% inspect -f "{{.State.Status}} {{.State.Restarting}}" seeandyougo-local-backend > "!STATE_FILE!" 2>nul

  findstr /c:"profile is active" "!LOG_FILE!" >nul
  if not errorlevel 1 if "!REPORTED_LOCAL_PROFILE!"=="0" (
    echo - local 프로필 확인 완료
    set "REPORTED_LOCAL_PROFILE=1"
  )

  findstr /c:"Started SeeAndYouGoApplication" /c:"Tomcat started on port" "!LOG_FILE!" >nul
  if not errorlevel 1 (
    set "SPRING_STARTED=1"
    if "!REPORTED_SPRING_STARTED!"=="0" (
      echo - Spring/Tomcat 시작 확인 완료
      set "REPORTED_SPRING_STARTED=1"
    )
  )

  findstr /c:"초기세팅 완료" "!LOG_FILE!" >nul
  if not errorlevel 1 (
    set "INITIAL_SETUP_DONE=1"
    if "!REPORTED_INITIAL_SETUP_DONE!"=="0" (
      echo - DataLoader 초기세팅 완료 확인
      set "REPORTED_INITIAL_SETUP_DONE=1"
    )
  )

  curl.exe -fsS http://localhost:8080/v3/api-docs >nul 2>nul
  if not errorlevel 1 (
    set "HTTP_READY=1"
    if "!REPORTED_HTTP_READY!"=="0" (
      echo - HTTP /v3/api-docs 응답 확인 완료
      set "REPORTED_HTTP_READY=1"
    )
  )

  if "!HTTP_READY!"=="1" if "!SPRING_STARTED!"=="1" if "!INITIAL_SETUP_DONE!"=="1" (
    del "!LOG_FILE!" "!STATE_FILE!" >nul 2>nul
    echo 백엔드 기동 확인 완료.
    exit /b 0
  )

  findstr /c:"Application run failed" /c:"Failed to execute CommandLineRunner" "!LOG_FILE!" >nul
  if not errorlevel 1 (
    echo 오류: 백엔드 애플리케이션 기동 실패 로그가 감지되었습니다. 1>&2
    echo.
    echo 최근 백엔드 로그:
    type "!LOG_FILE!"
    call :classify_backend_failure "!LOG_FILE!"
    del "!LOG_FILE!" "!STATE_FILE!" >nul 2>nul
    exit /b 1
  )

  findstr /c:"exited" /c:"true" "!STATE_FILE!" >nul 2>nul
  if not errorlevel 1 (
    echo 오류: 백엔드 컨테이너 상태가 비정상입니다. 1>&2
    echo.
    echo 최근 백엔드 로그:
    type "!LOG_FILE!"
    call :classify_backend_failure "!LOG_FILE!"
    del "!LOG_FILE!" "!STATE_FILE!" >nul 2>nul
    exit /b 1
  )

  if "%%I"=="1" echo - 백엔드 준비 대기 중...
  set /a MOD=%%I %% 5
  if "!MOD!"=="0" echo - 백엔드 준비 대기 중...
  timeout /t %BACKEND_READY_INTERVAL_SECONDS% /nobreak >nul
)

echo 오류: 제한 시간 안에 백엔드 준비 상태를 확인하지 못했습니다. 1>&2
echo.
echo 최근 백엔드 로그:
type "%LOG_FILE%"
call :classify_backend_failure "%LOG_FILE%"
del "%LOG_FILE%" "%STATE_FILE%" >nul 2>nul
exit /b 1

:classify_backend_failure
set "CLASSIFY_LOG=%~1"
echo.
echo 백엔드 기동 실패 원인 분류:

findstr /i /c:"key.yml" /c:"Jasypt" /c:"encryptor" /c:"Could not resolve placeholder" /c:"BindException" "%CLASSIFY_LOG%" >nul
if not errorlevel 1 (
  echo - 설정/시크릿 문제 가능성이 큽니다.
  echo - key.yml, application-local.yml, 환경변수 값을 먼저 확인하세요.
  exit /b 0
)

findstr /i /c:"Communications link failure" /c:"Access denied" /c:"Unknown database" /c:"HikariPool" /c:"Connection refused" "%CLASSIFY_LOG%" >nul
if not errorlevel 1 (
  echo - MySQL 연결 또는 초기화 문제 가능성이 큽니다.
  echo - MySQL 컨테이너 상태, datasource URL, 포트, 계정을 먼저 확인하세요.
  exit /b 0
)

findstr /i /c:"Table " /c:"Unknown column" /c:"Duplicate column" /c:"Schema-validation" /c:"SchemaManagementException" /c:"Duplicate entry" /c:"Data truncation" /c:"constraint" "%CLASSIFY_LOG%" >nul
if not errorlevel 1 (
  echo - DB 스키마/데이터와 현재 코드가 충돌하는 문제일 수 있습니다.
  echo - MySQL이 healthy인데 같은 에러가 반복되면 DB 볼륨 리셋 후보입니다.
  echo - 자동으로 볼륨 삭제는 하지 않습니다.
  exit /b 0
)

findstr /i /c:"Failed to execute CommandLineRunner" /c:"NoSuchFileException" /c:"menuOfRestaurant1.json" /c:"Failed to parse" "%CLASSIFY_LOG%" >nul
if not errorlevel 1 (
  echo - 애플리케이션 초기 데이터 로딩 또는 리소스 파일 문제 가능성이 큽니다.
  echo - 누락 파일, DataLoader, 외부 API 응답을 먼저 확인하세요.
  exit /b 0
)

findstr /i /c:"RedisConnectionFailureException" /c:"Unable to connect to Redis" "%CLASSIFY_LOG%" >nul
if not errorlevel 1 (
  echo - Redis 연결 문제 가능성이 큽니다.
  echo - Redis 컨테이너 상태와 SPRING_DATA_REDIS_* 설정을 확인하세요.
  exit /b 0
)

echo - 알려진 패턴으로 분류되지 않았습니다.
echo - 위 최근 로그를 기준으로 원인을 확인하세요.
exit /b 0
