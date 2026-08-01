# SeeAndYouGo API Response Spec

## 1. 공통 응답 형식

모든 JSON API 응답은 아래 envelope 형식을 사용한다.

### 성공

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

- HTTP Status: `200 OK`
- `data`에는 API별 실제 응답 결과가 들어간다.
- 목록 API는 `data: []`, 단건 API는 `data: {}`, 값 응답은 `data: true`, 응답 본문이 없는 API는 `data: null`을 사용한다.

### 실패

```json
{
  "success": false,
  "code": "COMMON_002",
  "message": "잘못된 입력값입니다.",
  "data": null
}
```

- HTTP Status: 실패 종류에 맞는 4xx/5xx
- `message`는 프론트에서 그대로 화면에 노출 가능한 문장이다. 프론트는 별도로 문구를 재작성하지 않고 `message`를 그대로 사용자에게 보여준다.
- `code`는 화면 분기(재로그인 유도, 재시도 버튼 등)에만 사용한다.
- 프론트는 실패 시 `error.response.data.message`를 사용자에게 표시하면 된다.

### 예외

`GET /api/images/{imgName}` 성공 응답은 이미지 렌더링을 위해 기존처럼 `image/png` binary를 반환한다. 실패 응답은 공통 실패 JSON을 따른다.

## 2. 프론트 처리 규칙

```js
// 성공
const payload = response.data.data;

// 실패
const message = error.response?.data?.message ?? "알 수 없는 오류가 발생했습니다.";
```

## 3. 에러 코드 전체 목록 (발생 조건별 실제 메시지)

실제로 코드에서 던져지고 있는 에러 코드만 정리했다 (정의만 되어 있고 실제 발생하지 않는 코드는 제외). 하나의 `code`를 여러 API/상황이 공유할 때, 실제로 응답에 담기는 메시지가 조건마다 다르면 행을 나눠 Message 열에 그 실제 문구를 직접 적었다. 문구가 조건과 무관하게 항상 같으면 한 행에 조건을 모아 적었다.

호출 지점별로 다른 문구가 필요할 때는 `ApiException(errorCode, 커스텀메시지)` 또는 `EntityNotFoundException(errorCode, 로그용 detail, 사용자용 userMessage)`로 넘긴다. DB 내부 id/email 같은 원본 식별자는 `detail`로만 서버 로그에 남기고, 사용자에게는 항상 상황을 설명하는 자연어 문구만 노출한다(PII·내부 식별자 미노출).

### Common — 도메인 무관 공통 에러

| HTTP | Code | Message | 발생 조건 |
|---:|---|---|---|
| 500 | `COMMON_001` | 서버 내부 오류가 발생했습니다. | 예상치 못한 모든 예외의 catch-all. DB 오류, NPE, 외부 파일(1학생회관 메뉴 JSON) 파싱 실패(`IOException`) 등. |
| 400 | `COMMON_002` | 존재하지 않는 식당입니다: {입력값} | `Restaurant.parseName()` 실패 — 식당 번호/이름을 받는 대부분의 API 공통 경로 (`/api/connection/{restaurant}`, `/api/daily-menu/{restaurant}`, `/api/weekly-menu/{restaurant}`, `/api/restaurant/{n}/rate/main`, `/api/restaurant/{n}/rate/detail`, `/api/statistics/{n}`, `POST /api/review`, `/api/connection/local`, `/api/menu/local`) |
| 400 | `COMMON_002` | 알 수 없는 식당입니다: {restaurant 파라미터} | `GET /api/connection/prediction` — 식당 파라미터가 유효하지 않은 경우. |
| 400 | `COMMON_002` | observed_at은 yyyy-MM-dd HH:mm:ss 형식이어야 합니다. | `GET /api/connection/prediction` — `observed_at` 형식 오류. |
| 400 | `COMMON_002` | 메뉴별 개별 평점을 관리하는 식당만 지원하는 기능입니다. | `GET /api/restaurant/{n}/rate/detail` — 세부 평점 미지원 식당 요청. |
| 400 | `COMMON_002` | 새로운 요리명은 비어있을 수 없습니다. | `PUT /api/dish/name` — 새 요리명이 빈 문자열. |
| 400 | `COMMON_002` | 날짜형식이 일치하지 않습니다.(yyyy-MM-dd) | `GET /api/daily-menu/{restaurant}/{date}` — `date` 형식 오류. |
| 400 | `COMMON_002` | 올바르지 않은 이미지 파일명입니다. | `GET /api/images/{imgName}` — 경로 조작 문자(`..`, `/`, `\`) 포함 (path traversal 방어). |
| 400 | `COMMON_002` | 잘못된 입력값입니다. (기본값) | 커스텀 문구 없는 나머지: Bean Validation 실패, path/query 타입 불일치, 필수 파라미터 누락, JSON 파싱 불가, 그 외 커스텀 메시지 없이 던져진 `IllegalArgumentException`. |
| 404 | `COMMON_003` | 요청한 리소스를 찾을 수 없습니다. | 이미지 파일 없음 / 존재하지 않는 URL / JPA 엔티티 조회 실패. |

> `IllegalArgumentException`은 JDK/외부 라이브러리를 포함해 어디서든 던져질 수 있어, `GlobalExceptionHandler`는 이 예외의 `getMessage()`를 사용자에게 절대 그대로 전달하지 않는다(항상 `COMMON_002` 기본 메시지로 고정). 위 표의 구체적인 문구들은 전부 해당 지점에서 `IllegalArgumentException` 대신 `ApiException(ErrorCode.INVALID_INPUT_VALUE, "...")`을 명시적으로 던지도록 고쳤기 때문에 노출된다.

### Auth — 인증/인가

| HTTP | Code | Message | 발생 조건 |
|---:|---|---|---|
| 401 | `AUTH_001` | 인증 정보가 올바르지 않습니다. | ① 로그인 필요 API에 자격 증명 없이(또는 만료된 access token으로) 접근 ② `GET /api/oauth/token/reissue`에서 refresh token 자체가 만료 |
| 403 | `AUTH_002` | 접근 권한이 없습니다. | 인증은 되었으나 해당 리소스에 대한 권한이 없는 경우 (`AccessDeniedException`). |
| 401 | `AUTH_003` | 유효하지 않은 토큰입니다. | ① `@ValidateToken` AOP 검증 실패 ② `GET /api/oauth/token/reissue`에서 refresh token JWT 서명 위조 ③ `GET /api/oauth/token/reissue`에서 refresh token 값이 서버 저장값과 불일치(탈취/변조 의심) |

### 도메인별 Not Found

| HTTP | Code | Message | 발생 조건 |
|---:|---|---|---|
| 404 | `USER_001` | 닉네임 변경 이력을 조회할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요. | `PUT /api/user/nickname` |
| 404 | `USER_001` | 키워드를 등록할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요. | `POST /api/keyword` |
| 404 | `USER_001` | 키워드를 삭제할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요. | `DELETE /api/keyword` |
| 404 | `USER_001` | 좋아요를 처리할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요. | `POST /api/review/like/{review_id}` |
| 404 | `USER_001` | 토큰을 재발급할 사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요. | `GET /api/oauth/token/reissue` |
| 404 | `DISH_001` | {요리명}에 해당하는 요리를 찾을 수 없습니다. | `PUT /api/main-menu` |
| 404 | `DISH_001` | ID {id}에 해당하는 요리를 찾을 수 없습니다. | `DELETE /api/dish/{id}`, `PUT /api/dish/name` |
| 404 | `REVIEW_001` | 좋아요를 처리할 리뷰를 찾을 수 없습니다. | `POST /api/review/like/{review_id}` |
| 404 | `REVIEW_001` | 신고하려는 리뷰를 찾을 수 없습니다. | `PUT /api/report/{reviewId}` |
| 404 | `REVIEW_001` | 삭제하려는 리뷰를 찾을 수 없습니다. | `DELETE /api/reviews/{reviewId}` |
| 404 | `REVIEW_001` | 삭제하려는 신고 리뷰를 찾을 수 없습니다. | `DELETE /api/review/report/{reviewId}` |
| 403 | `REVIEW_002` | 본인이 작성한 리뷰만 삭제할 수 있습니다. | `DELETE /api/reviews/{reviewId}` — 요청자와 작성자가 다름. |
| 404 | `KEYWORD_001` | 삭제하려는 키워드를 찾을 수 없습니다. | `DELETE /api/keyword` |

### Prediction — 혼잡도 예측 서버 연동

| HTTP | Code | Message | 발생 조건 |
|---:|---|---|---|
| 404 | `PREDICTION_001` | 해당 시간대의 관측 데이터가 없습니다. | `observed_at` ±5분 이내 관측 기록 없음. |
| 503 | `PREDICTION_002` | 예측 서버가 응답하지 않습니다. | 예측 서버 헬스체크 실패. |
| 502 | `PREDICTION_003` | 예측 서버 호출에 실패했습니다. | 헬스체크 통과 후 실제 요청 중 `RestClientException`. |

## 4. API별 실패 코드 매핑

### Connection

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/connection/{restaurant}` | `ConnectionResponseDto` | `COMMON_002` / `COMMON_001` |
| GET | `/api/connection/cache` | `null` | `COMMON_001` |
| GET | `/api/connection/prediction?restaurant=&observed_at=` | `PredictionResponseDto` | `COMMON_002` / `PREDICTION_001` / `PREDICTION_002` / `PREDICTION_003` |
| POST | `/api/connection/local?AUTH_KEY=&restaurant=` | `ConnectionVO` | `AUTH_001` / `COMMON_002` / `COMMON_001` |

### Menu

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/daily-menu/{restaurant}` | `MenuResponseByUserDto[]` | `COMMON_002` / `COMMON_001` |
| GET | `/api/daily-menu/{restaurant}/{date}` | `MenuResponseByUserDto[]` | `COMMON_002` |
| GET | `/api/weekly-menu/{restaurant}` | `MenuResponseDto[]` | `COMMON_002` / `COMMON_001` |
| GET | `/api/weekly-menu` | `MenuResponseByAdminDto[]` | `COMMON_001` |
| POST | `/api/menu/local?AUTH_KEY=&restaurant=` | `MenuVO[]` | `AUTH_001` / `COMMON_002` |
| GET | `/api/week` | `null` | `COMMON_001` |
| GET | `/api/restaurant1-menu` | `JsonNode` | `COMMON_003` / `COMMON_001` |

### Dish

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| PUT | `/api/main-menu` | `"Main Menu reflect Success."` | `DISH_001` / `COMMON_001` |
| GET | `/api/dish/week` | `DishResponseDto[]` | `COMMON_001` |
| DELETE | `/api/dish/{id}` | `true` | `DISH_001` |
| PUT | `/api/dish/name` | `true` 또는 `false` | `COMMON_002` / `DISH_001` |

### Review

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/total-review` | `ReviewResponseDto[]` | `COMMON_001` |
| GET | `/api/top-review/{restaurant}` | `ReviewResponseDto[]` | `COMMON_002` |
| GET | `/api/review/{restaurant}` | `ReviewResponseDto[]` | `COMMON_002` |
| PUT | `/api/report/{reviewId}` | `ReportCountResponseDto` | `REVIEW_001` |
| POST | `/api/review` | `reviewId: number` | `COMMON_002` / `COMMON_003` / `COMMON_001` |
| GET | `/api/images/{imgName}` | `image/png` binary | `COMMON_002` / `COMMON_003` |
| GET | `/api/reviews/{token}` | `ReviewResponseDto[]` | `COMMON_001` |
| DELETE | `/api/reviews/{reviewId}` | `ReviewDeleteResponseDto` | `REVIEW_001` / `REVIEW_002` |
| DELETE | `/api/review/report/{reviewId}` | `ReviewDeleteResponseDto` | `REVIEW_001` |

### Like

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| POST | `/api/review/like/{review_id}` | `LikeResponseDto` | `USER_001` / `REVIEW_001` |

### Keyword

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/keyword/{user_id}` | `KeywordResponseDto` | `COMMON_001` |
| POST | `/api/keyword` | `KeywordAddResponseDto` | `USER_001` |
| DELETE | `/api/keyword` | `KeywordResponseDto` | `USER_001` / `KEYWORD_001` |

### OAuth

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/oauth/kakao?code=` | `TokenDto` | `COMMON_001` |
| GET | `/api/oauth/google?code=` | `TokenDto` | `COMMON_001` |
| GET | `/api/oauth/token/reissue` | `TokenDto` | `AUTH_001`(만료) / `AUTH_003`(서명 위조·값 불일치) / `USER_001` |

### Rate

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/restaurant/{restaurantNumber}/rate/main` | `RestaurantTotalRateResponseDto` | `COMMON_002` / `COMMON_001` |
| GET | `/api/restaurant/{restaurantNumber}/rate/detail` | `RestaurantDetailRateResponseDto[]` | `COMMON_002` / `COMMON_001` |

### Statistics

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/statistics/{restaurantNumber}` | `ConnectionsStatisticsResponseDto[]` | `COMMON_002` / `COMMON_001` |
| GET | `/api/statistics/{year}/{month}/{day}` | `null` | `COMMON_002` / `COMMON_001` |

### Visitor

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/visitors/count` | `VisitorCountDto` | `COMMON_001` |

### User

| Method | Path | 성공 `data` | 실패 코드 |
|---|---|---|---|
| GET | `/api/user/nickname/check/{nickname}` | `NicknameCheckResponseDto` | `COMMON_001` |
| PUT | `/api/user/nickname` | `NicknameUpdateResponseDto` | `USER_001` |
| GET | `/api/user/nickname` | `UserResponseDto` | `COMMON_001` |
