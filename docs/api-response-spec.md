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
- `message`는 프론트에서 그대로 화면에 노출 가능한 문장이다.
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

## 3. 실패 코드 목록

| HTTP | Code | Message |
|---:|---|---|
| 500 | `COMMON_001` | 서버 내부 오류가 발생했습니다. |
| 400 | `COMMON_002` | 잘못된 입력값입니다. |
| 404 | `COMMON_003` | 요청한 리소스를 찾을 수 없습니다. |
| 401 | `AUTH_001` | 인증 정보가 올바르지 않습니다. |
| 403 | `AUTH_002` | 접근 권한이 없습니다. |
| 401 | `AUTH_003` | 유효하지 않은 토큰입니다. |
| 404 | `USER_001` | 사용자를 찾을 수 없습니다. |
| 404 | `MENU_001` | 메뉴를 찾을 수 없습니다. |
| 404 | `DISH_001` | 요리를 찾을 수 없습니다. |
| 404 | `REVIEW_001` | 리뷰를 찾을 수 없습니다. |
| 403 | `REVIEW_002` | 본인이 작성한 리뷰만 삭제할 수 있습니다. |
| 404 | `KEYWORD_001` | 키워드를 찾을 수 없습니다. |
| 404 | `RATE_001` | 평점 정보를 찾을 수 없습니다. |
| 404 | `STATISTICS_001` | 통계 정보를 찾을 수 없습니다. |
| 404 | `PREDICTION_001` | 해당 시간대의 관측 데이터가 없습니다. |
| 503 | `PREDICTION_002` | 예측 서버가 응답하지 않습니다. |
| 502 | `PREDICTION_003` | 예측 서버 호출에 실패했습니다. |

## 4. API별 명세

### Connection

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/connection/{restaurant}` | `ConnectionResponseDto` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/connection/cache` | `null` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/connection/prediction?restaurant=&observed_at=` | `PredictionResponseDto` | `COMMON_002` 잘못된 입력값입니다. / `PREDICTION_001` 해당 시간대의 관측 데이터가 없습니다. / `PREDICTION_002` 예측 서버가 응답하지 않습니다. / `PREDICTION_003` 예측 서버 호출에 실패했습니다. |
| POST | `/api/connection/local?AUTH_KEY=&restaurant=` | `ConnectionVO` | `AUTH_001` 인증 정보가 올바르지 않습니다. / `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |

### Menu

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/daily-menu/{restaurant}` | `MenuResponseByUserDto[]` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/daily-menu/{restaurant}/{date}` | `MenuResponseByUserDto[]` | `COMMON_002` 잘못된 입력값입니다. |
| GET | `/api/weekly-menu/{restaurant}` | `MenuResponseDto[]` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/weekly-menu` | `MenuResponseByAdminDto[]` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| POST | `/api/menu/local?AUTH_KEY=&restaurant=` | `MenuVO[]` | `AUTH_001` 인증 정보가 올바르지 않습니다. / `COMMON_002` 잘못된 입력값입니다. |
| GET | `/api/week` | `null` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/restaurant1-menu` | `JsonNode` | `COMMON_003` 요청한 리소스를 찾을 수 없습니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |

### Dish

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| PUT | `/api/main-menu` | `"Main Menu reflect Success."` | `DISH_001` 요리를 찾을 수 없습니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/dish/week` | `DishResponseDto[]` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| DELETE | `/api/dish/{id}` | `true` | `DISH_001` 요리를 찾을 수 없습니다. |
| PUT | `/api/dish/name` | `true` 또는 `false` | `COMMON_002` 잘못된 입력값입니다. / `DISH_001` 요리를 찾을 수 없습니다. |

### Review

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/total-review` | `ReviewResponseDto[]` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/top-review/{restaurant}` | `ReviewResponseDto[]` | `COMMON_002` 잘못된 입력값입니다. |
| GET | `/api/review/{restaurant}` | `ReviewResponseDto[]` | `COMMON_002` 잘못된 입력값입니다. |
| PUT | `/api/report/{reviewId}` | `ReportCountResponseDto` | `REVIEW_001` 리뷰를 찾을 수 없습니다. |
| POST | `/api/review` | `reviewId: number` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_003` 요청한 리소스를 찾을 수 없습니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/images/{imgName}` | `image/png` binary | `COMMON_002` 잘못된 입력값입니다. / `COMMON_003` 요청한 리소스를 찾을 수 없습니다. |
| GET | `/api/reviews/{token}` | `ReviewResponseDto[]` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| DELETE | `/api/reviews/{reviewId}` | `ReviewDeleteResponseDto` | `REVIEW_001` 리뷰를 찾을 수 없습니다. / `REVIEW_002` 본인이 작성한 리뷰만 삭제할 수 있습니다. |
| DELETE | `/api/review/report/{reviewId}` | `ReviewDeleteResponseDto` | `REVIEW_001` 리뷰를 찾을 수 없습니다. |

### Like

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| POST | `/api/review/like/{review_id}` | `LikeResponseDto` | `USER_001` 사용자를 찾을 수 없습니다. / `REVIEW_001` 리뷰를 찾을 수 없습니다. |

### Keyword

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/keyword/{user_id}` | `KeywordResponseDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| POST | `/api/keyword` | `KeywordAddResponseDto` | `USER_001` 사용자를 찾을 수 없습니다. |
| DELETE | `/api/keyword` | `KeywordResponseDto` | `USER_001` 사용자를 찾을 수 없습니다. / `KEYWORD_001` 키워드를 찾을 수 없습니다. |

### OAuth

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/oauth/kakao?code=` | `TokenDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/oauth/google?code=` | `TokenDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/oauth/token/reissue` | `TokenDto` | `AUTH_001` 인증 정보가 올바르지 않습니다. / `AUTH_003` 유효하지 않은 토큰입니다. |

### Rate

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/restaurant/{restaurantNumber}/rate/main` | `RestaurantTotalRateResponseDto` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/restaurant/{restaurantNumber}/rate/detail` | `RestaurantDetailRateResponseDto[]` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |

### Statistics

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/statistics/{restaurantNumber}` | `ConnectionsStatisticsResponseDto[]` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |
| GET | `/api/statistics/{year}/{month}/{day}` | `null` | `COMMON_002` 잘못된 입력값입니다. / `COMMON_001` 서버 내부 오류가 발생했습니다. |

### Visitor

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/visitors/count` | `VisitorCountDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |

### User

| Method | Path | 성공 `data` | 실패 코드 및 문구 |
|---|---|---|---|
| GET | `/api/user/nickname/check/{nickname}` | `NicknameCheckResponseDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
| PUT | `/api/user/nickname` | `NicknameUpdateResponseDto` | `USER_001` 사용자를 찾을 수 없습니다. |
| GET | `/api/user/nickname` | `UserResponseDto` | `COMMON_001` 서버 내부 오류가 발생했습니다. |
