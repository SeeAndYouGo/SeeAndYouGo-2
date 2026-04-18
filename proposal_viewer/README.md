# 학생식당 혼잡도 예측 튜닝 뷰어

기존 SeeAndYouGo `backend`와 `frontend`를 건드리지 않고, 별도 화면에서 학생식당 혼잡도 예측식을 튜닝하고 날짜별 결과를 확인하기 위한 독립 데모 프로젝트입니다.

이 화면은 이제 `2025 검증 전용`이 아니라, DB에 들어 있는 `2026년까지의 누적 데이터`를 바탕으로 고정 운영식을 먼저 정한 뒤 특정 날짜를 확인하는 방식으로 동작합니다.

중요:
선택 날짜 화면은 `확인용`입니다.
즉, 해당 날짜를 독립 검증 셋으로 떼어 놓고 점수를 낸 화면이 아니라, 전체 누적 데이터로 정한 고정식을 그 날짜에 그대로 적용해 보는 화면입니다.

## 실행 방법

```bash
cd proposal_viewer
copy .env.example .env
npm install
npm start
```

브라우저에서 `http://localhost:4321`로 접속합니다.

주의:
`public/index.html` 파일을 직접 열면 안 됩니다.
반드시 `proposal_viewer`에서 `npm start`로 Express 서버를 띄운 뒤 `http://localhost:4321`로 접속해야 합니다.
직접 열거나 다른 서버에서 열면 `/api/history`가 HTML을 응답해서 `Unexpected token '<'` 또는 "HTML 문서가 응답됐습니다" 오류가 날 수 있습니다.

## 파일 구조

- `server.js`: DB 조회, 전체 데이터 튜닝, 날짜 비교 API
- `public/index.html`: 날짜 선택, 추천 반영률, 설명 영역
- `public/app.js`: 메타데이터 로딩, 카드/그래프/상세 비교 렌더링
- `public/styles.css`: 화면 스타일

## 기본식

기본식은 다음과 같습니다.

```text
예측값 = 목표 시점 평소값 + (현재값 - 현재 시점 평소값) × 반영률
```

용어:

- `현재값`: 기준 시각의 실제 혼잡도
- `현재 시점 평소값`: 같은 식당, 같은 요일, 같은 시간대의 평균
- `목표 시점 평소값`: 30/60/90분 뒤 같은 식당, 같은 요일, 같은 시간대의 평균
- `반영률`: 현재 차이를 얼마나 끌고 갈지 정하는 가중치

예를 들어:

```text
목표 시점 평소값 45
현재값 80
현재 시점 평소값 60
반영률 0.7
```

이면,

```text
45 + (80 - 60) × 0.7 = 59
```

이 됩니다.

## 최적화식

이제 이 화면의 조정식은 `피크 압력식(pressureTrend)` 하나로 통일되어 있습니다.
즉, 여러 식 종류 중 하나를 고르는 구조가 아니라 `같은 식을 쓰되 그룹마다 계수만 다시 학습`합니다.

피크 압력식은 다음 특징들을 조합한 선형식입니다.

- `intercept`
- `currentValue`
- `targetBaseline`
- `pressureGap`
- `baselineShiftPos`
- `rise15`, `rise30`
- `gapPos`
- `surge30`

운영 구조는 2가지로 볼 수 있습니다.

- `식당×분 고정식`: 식당별 30/60/90분만 나누는 기본 운영안
- `시간대별 고정식`: 식당별 30/60/90분과 목표 시간대까지 나누는 상세 운영안

기본 화면은 `식당×분 고정식`을 기본값으로 사용합니다.

점심 진입과 점심 피크 구간은 단순 MAE만 보지 않고,
`상위 혼잡 구간 과소예측`에 추가 벌점을 주어 피크를 늦게 따라가는 식이 선택되지 않도록 했습니다.

특히 제3학생회관처럼 피크가 빠르게 올라가는 식당은 이 벌점이 중요합니다.

계수 학습 방식:

```text
β = argmin Σ(actual - predicted)^2 + λΣβ_j^2
```

- `λ = 18`
- 절편(`intercept`)은 다른 계수와 같이 학습하지만 규제 항에서는 제외
- 즉, 절편은 손으로 정하는 값이 아니라 데이터에서 자동으로 추정되는 시작점

## 데이터 기준

튜닝 범위:

- 쿼리는 `2027-01-01 00:00:00` 미만 전체 데이터를 읽습니다.
- 실제 화면과 API에는 DB에서 관측된 `최소 시각 ~ 최대 시각`을 따로 표시합니다.
- 현재 기준 실제 관측 범위는 `/api/restaurants`의 `optimizedSummary.tuningWindow`가 원본입니다.

평소값 기준:

- 학생생활관 제외 식당은 `같은 시즌 평균 우선`
- 학생생활관은 `전 기간 평균`
- 날짜 확인 시에도 전체 튜닝 범위의 누적 데이터를 기준으로 평소값을 계산

시즌 구간:

- `1~2월`: 겨울 비수기
- `3~6월`: 1학기
- `7~8월`: 여름 비수기
- `9~12월`: 2학기

## 데이터 품질 필터

튜닝과 날짜 비교에서 다음 값들을 제외합니다.

- 고립된 `0` 또는 거의 `0` 값
- 한 칸만 튀었다가 바로 되돌아오는 급반등/급락 값
- 실제로 거의 운영되지 않는 비활성 시간대

즉, `잘못 들어간 센서값`이나 `의미 없는 시간대` 때문에 식이 흔들리지 않도록 한 상태입니다.

## 추천 반영률

추천 반영률은 더 이상 고정 상수가 아니라 `server.js`가 전체 누적 데이터를 보고 직접 다시 계산합니다.

화면에서:

- `공통 추천값`: 모든 식당에 같은 반영률 적용
- `식당별 최적값`: 식당별로 다시 튜닝한 반영률 적용
- `직접 조정`: 사용자가 수동 입력

최신 추천값은 `/api/restaurants` 응답의 `recommendedWeights`가 기준입니다.

## API

### 식당/튜닝 메타데이터

```http
GET /api/restaurants
```

주요 응답:

- `restaurants`
- `recommendedWeights`
- `optimizedSummary.tuningWindow`
- `optimizedValidation`

### 날짜 비교

```http
GET /api/history?restaurant=제3학생회관&date=2026-04-01&mode=restaurant&rowsLimit=72
```

주요 응답:

- `restaurant`, `date`, `baselineMode`, `season`
- `parameters`
- `quality`
- `comparisonSummary`
- `horizons`

`parameters` 안에는 다음이 들어 있습니다.

- `weights`
- `trainingBaseline`
- `tuningWindow`
- `searchNote`
- `selectionNote`
- `qualityNote`
- `validationNote`
- `inspectionNote`

`summaryOnly=true`를 주면 요약 지표만 받고,
`rowsLimit`를 주면 지표 계산은 그대로 유지하면서 응답에 포함되는 상세 rows만 줄일 수 있습니다.

## 보안 메모

DB 접속 정보는 브라우저 코드에 넣지 않고 `server.js`에서만 사용합니다.
실제 운영 시에는 `.env`를 커밋하지 말고, 서버 환경변수나 Secret Manager로 분리하는 편이 안전합니다.
