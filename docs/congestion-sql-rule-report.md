# SQL 기반 혼잡도 예측 보고서

작성일: 2026-03-28

## 1. 질문에 대한 짧은 답

가능하다.

현재 데이터 기준으로는 머신러닝 모델 없이도 `SQL + 집계 규칙 + 고정 가중치`만으로 충분히 운영 가능한 혼잡도 예측을 만들 수 있다.

실험 결과를 요약하면 다음과 같다.

- 개장 전 예측의 최적 조합: `이전 주 같은 요일 같은 시간대 값` 중심
- 영업 중 예측의 최적 조합: `직전 5분 값` 단독
- 메뉴 exact match는 이번 테스트 구간에서는 최적 가중치가 `0`으로 나왔다

즉, SQL만으로도 1차 버전은 충분히 가능하다.

## 2. 이번 실험이 무엇을 비교했는가

튜닝 실험 코드는 다음 파일에 구현했다.

- [CongestionSqlRuleTuning.java](/D:/SeeAndYouGo-2/backend/tools/CongestionSqlRuleTuning.java)

이 코드는 다음 조건으로 SQL 기반 규칙을 튜닝했다.

- 테스트 구간: `2026-03-01 ~ 2026-03-27`
- 단위: 식당별 5분 슬롯
- 범위: `07:30 ~ 19:30`
- 제외: 이상일 `2025-06-28`, `2025-09-29`, `2025-12-22`, `2026-01-13`
- 유효일 기준: 슬롯 수 `120` 이상

## 3. SQL로 만들 수 있는 예측 구성요소

모델 없이 SQL만으로 만들 수 있는 예측 구성요소는 생각보다 많다.

### 3.1 개장 전 예측에서 쓸 수 있는 값

1. `weekday_slot_mean`
   - 같은 식당
   - 같은 요일
   - 같은 5분 슬롯 평균

2. `recent_8week_weekday_slot_mean`
   - 같은 식당
   - 같은 요일
   - 같은 5분 슬롯
   - 최근 8주 평균

3. `menu_mean`
   - 같은 식당
   - 같은 요일
   - 같은 5분 슬롯
   - 같은 식사타입
   - 같은 메뉴 signature 평균

4. `prev_week_same_slot`
   - 이전 주 같은 요일
   - 같은 5분 슬롯의 실제 값

### 3.2 영업 중 예측에서 쓸 수 있는 값

1. `lag1`
   - 직전 5분 값

2. `lag_avg3`
   - 최근 3개 슬롯 평균

3. `weekday_slot_mean`
   - 역사 평균 baseline

4. `recent_8week_weekday_slot_mean`
   - 최근 8주 baseline

이 값들은 전부 SQL로 계산 가능하다.

## 4. 튜닝한 파라미터

이번 실험에서 최적화한 파라미터는 `가중치`다.

### 4.1 개장 전 예측 가중치

가중치 순서는 아래와 같다.

`[weekday_slot_mean, recent_8week_weekday_slot_mean, menu_mean, prev_week_same_slot]`

탐색 범위:

- 각 가중치 `0.0 ~ 1.0`
- 간격 `0.1`
- 전수 비교

추가로 메뉴 exact match 최소 이력 건수도 비교했다.

- `menu_min_history = 2, 3, 4, 5`

### 4.2 영업 중 예측 가중치

가중치 순서는 아래와 같다.

`[lag1, lag_avg3, weekday_slot_mean, recent_8week_weekday_slot_mean]`

탐색 범위:

- 각 가중치 `0.0 ~ 1.0`
- 간격 `0.1`
- 전수 비교

## 5. 튜닝 결과

## 5.1 개장 전 예측 최적 가중치

상위 결과는 다음과 같았다.

| 순위 | 가중치 | MAE | WAPE | RMSE |
|---|---|---:|---:|---:|
| 1 | `[0.0, 0.1, 0.0, 1.0]` | 21.995 | 0.306 | 57.078 |
| 2 | `[0.0, 0.1, 0.0, 0.9]` | 21.995 | 0.306 | 57.048 |
| 3 | `[0.0, 0.1, 0.0, 0.8]` | 21.996 | 0.306 | 57.014 |

### 해석

이 결과는 사실상 다음 뜻이다.

- `prev_week_same_slot`이 핵심이다
- `recent_8week_weekday_slot_mean`은 아주 약하게만 보조로 쓰면 된다
- `weekday_slot_mean`은 최적 조합에서 사실상 필요하지 않았다
- `menu_mean`은 이번 테스트 구간에서 최적 가중치가 `0`이었다

즉, 개장 전 예측의 최적 SQL 규칙은 거의 다음과 같다.

`예측값 = 90% 이상 지난주 같은 요일 같은 시간대 값 + 10% 미만 최근 8주 같은 요일 같은 시간대 평균`

실제로는 가중치 `[0.0, 0.1, 0.0, 1.0]`를 정규화해서 쓰면 된다.

정규화 후 해석:

- `recent_8week_weekday_slot_mean`: 약 `9.1%`
- `prev_week_same_slot`: 약 `90.9%`

## 5.2 메뉴 threshold 민감도

같은 가중치로 `menu_min_history`를 바꿔도 결과가 동일했다.

| menu_min_history | MAE | WAPE | RMSE |
|---:|---:|---:|---:|
| 2 | 21.995 | 0.306 | 57.078 |
| 3 | 21.995 | 0.306 | 57.078 |
| 4 | 21.995 | 0.306 | 57.078 |
| 5 | 21.995 | 0.306 | 57.078 |

### 해석

이번 최적 조합은 메뉴 가중치가 0이기 때문에 threshold를 바꿔도 결과가 변하지 않았다.

즉, 이번 구간에서는 “메뉴를 안 쓰는 것이 최적”이었다.

## 5.3 영업 중 예측 최적 가중치

상위 결과는 다음과 같았다.

| 순위 | 가중치 | MAE | WAPE | RMSE |
|---|---|---:|---:|---:|
| 1 | `[0.1, 0.0, 0.0, 0.0]` | 4.574 | 0.064 | 10.173 |
| 2 | `[0.2, 0.0, 0.0, 0.0]` | 4.574 | 0.064 | 10.173 |
| 3 | `[0.3, 0.0, 0.0, 0.0]` | 4.574 | 0.064 | 10.173 |

### 해석

이건 완전히 명확하다.

- `lag1`만 쓰는 방식이 최적이다
- `lag_avg3`, `weekday_slot_mean`, `recent_8week_weekday_slot_mean`은 오히려 성능을 떨어뜨렸다

즉, 영업 중 SQL 예측은 사실상 다음 한 줄이면 된다.

`예측값 = 직전 5분 값`

## 6. SQL만으로 구현할 때 판단 기준은 무엇인가

실제 SQL 예측 로직은 다음 판단 순서로 가는 것이 좋다.

## 6.1 개장 전 판단 기준

### 1순위

`prev_week_same_slot`

- 지난주 같은 요일
- 같은 시간대
- 같은 식당

이 값이 존재하면 가장 신뢰한다.

### 2순위

`recent_8week_weekday_slot_mean`

- 지난주 값이 없거나
- 지난주 값이 비정상으로 판단되면
- 최근 8주 같은 요일 같은 시간대 평균을 본다.

### 3순위

`weekday_slot_mean`

- 최근 8주 데이터가 충분하지 않으면
- 전체 이력 기준 같은 요일 같은 시간대 평균으로 fallback 한다.

### 4순위

`slot_mean`

- 위 값들이 모두 없으면
- 같은 식당 같은 시간대 평균으로 fallback 한다.

### 메뉴는?

현재 테스트 결과상 메뉴 exact match는 기본 규칙으로 넣을 필요가 없었다.

다만 다음 경우에는 보조 조건으로 남길 수 있다.

- 긴 연휴 직후
- 학사 일정이 크게 변하는 주
- 반복 메뉴 식당(예: 제1학생회관)
- 지난주 데이터가 없거나 이상치일 때

즉, 메뉴는 `기본축`이 아니라 `예외 보정용`으로 두는 것이 맞다.

## 6.2 영업 중 판단 기준

### 1순위

`lag1`

- 직전 5분 값이 있으면 그대로 사용

### 2순위

개장 전 예측값

- 아직 첫 슬롯이라 직전값이 없으면
- 개장 전 SQL baseline 사용

즉, 영업 중 판단 로직은 매우 단순하다.

## 7. 추천 SQL 공식

## 7.1 개장 전 추천 공식

실험 기준 추천 공식은 다음과 같다.

```text
pred_preopen =
  (0.909 * prev_week_same_slot + 0.091 * recent_8week_weekday_slot_mean)
  / (available_weight_sum)
```

사용 규칙:

- `prev_week_same_slot`가 있으면 가장 우선
- `recent_8week_weekday_slot_mean`은 약하게 보정
- 둘 다 없으면 `weekday_slot_mean`
- 그래도 없으면 `slot_mean`

## 7.2 영업 중 추천 공식

```text
pred_live = lag1
```

사용 규칙:

- 직전값이 있으면 그대로 사용
- 직전값이 없으면 `pred_preopen`

## 8. SQL 형태로 만들면 어떤 모습인가

개장 전 SQL은 대략 아래 구조로 만들 수 있다.

```sql
with target_slots as (
  select ...
),
weekday_hist as (
  select restaurant, dow, slot_5m, avg(connected) as weekday_slot_mean
  from cleaned_connection
  group by restaurant, dow, slot_5m
),
recent_hist as (
  select restaurant, dow, slot_5m, target_date,
         avg(connected) as recent_8week_weekday_slot_mean
  from cleaned_connection
  join target_slots ...
  where hist_date between target_date - interval 56 day and target_date - interval 1 day
  group by restaurant, dow, slot_5m, target_date
),
prev_week as (
  select t.restaurant, t.target_date, t.slot_5m,
         h.connected as prev_week_same_slot
  from target_slots t
  left join cleaned_connection h
    on h.restaurant = t.restaurant
   and h.slot_5m = t.slot_5m
   and h.date = t.target_date - interval 7 day
)
select
  case
    when prev_week_same_slot is not null and recent_8week_weekday_slot_mean is not null
      then (0.909 * prev_week_same_slot + 0.091 * recent_8week_weekday_slot_mean)
    when prev_week_same_slot is not null
      then prev_week_same_slot
    when recent_8week_weekday_slot_mean is not null
      then recent_8week_weekday_slot_mean
    when weekday_slot_mean is not null
      then weekday_slot_mean
    else slot_mean
  end as pred_preopen
from ...
```

영업 중 SQL은 더 단순하다.

```sql
select
  coalesce(lag1_connected, pred_preopen) as pred_live
from ...
```

## 9. 왜 모델 없이도 가능한가

이번 데이터에서는 다음 구조가 매우 강하다.

1. 같은 요일 같은 시간대 패턴이 강함
2. 주간 반복성이 강함
3. 영업 중에는 직전 5분과 다음 5분의 연속성이 매우 큼

즉, 패턴 자체가 이미 강해서 복잡한 모델 없이도 SQL 집계와 간단한 가중 평균만으로 상당히 잘 맞는다.

## 10. 그럼 모델은 언제 필요한가

다음 상황이 오면 모델 도입 가치가 커진다.

1. 30분, 60분 ahead 예측을 더 잘하고 싶을 때
2. 날씨, 행사, 시험기간 같은 외부 변수까지 넣고 싶을 때
3. 학생생활관처럼 특이 패턴 식당을 정교하게 맞추고 싶을 때
4. 단순 주간 반복이 깨지는 구간을 더 잘 처리하고 싶을 때

하지만 지금 단계에서는 SQL 규칙부터 구현하는 것이 더 효율적이다.

## 11. 최종 권장안

현재 기준 최종 권장안은 다음과 같다.

### 개장 전

- 기본 공식: `0.909 * prev_week_same_slot + 0.091 * recent_8week_weekday_slot_mean`
- fallback: `weekday_slot_mean -> slot_mean`
- 메뉴: 기본 가중치는 0, 필요 시 보조 예외 규칙으로만 사용

### 영업 중

- 기본 공식: `lag1`
- fallback: 개장 전 예측값

## 12. 결론

질문에 대한 최종 답은 다음과 같다.

### 1. 모델 없이 쿼리만으로 가능한가

가능하다.

### 2. 판단 기준은 무엇인가

- 개장 전: 지난주 같은 요일 같은 시간대 값이 최우선
- 영업 중: 직전 5분 값이 최우선
- 메뉴 exact match는 현재 기준 보조 요소

### 3. 최적 가중치는 무엇인가

- 개장 전: `prev_week_same_slot 90.9%`, `recent_8week_weekday_slot_mean 9.1%`
- 영업 중: `lag1 100%`

### 4. 지금 무엇을 만드는 게 맞는가

먼저 SQL 기반 1차 예측 로직을 만들고, 이후 필요하면 모델을 얹는 순서가 맞다.
