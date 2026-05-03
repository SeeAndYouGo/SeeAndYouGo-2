import express from 'express';
import mysql from 'mysql2/promise';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

loadDotEnv();

const app = express();
const port = Number(process.env.PORT || 4321);

const HORIZONS = [30, 60, 90];
const RESTAURANTS = ['제1학생회관', '제2학생회관', '제3학생회관', '상록회관', '생활과학대', '학생생활관'];
const DEFAULT_MODE = 'common';
const STRATEGY_BANDED = 'banded';
const STRATEGY_RESTAURANT_HORIZON = 'restaurantHorizon';
const DEFAULT_FORMULA_STRATEGY = STRATEGY_RESTAURANT_HORIZON;
const PROFILE_ALL_DATA = 'allData';
const PROFILE_TRAIN_TO_2025 = 'trainTo2025';
const DEFAULT_PROFILE = PROFILE_ALL_DATA;
const MODEL_BASE = 'base';
const MODEL_ADJUSTED = 'adjusted';
const MODEL_RESIDUAL = 'residual';
const TUNING_DATA_END_EXCLUSIVE = '2027-01-01 00:00:00';
const TRAIN_TO_2025_END_EXCLUSIVE = '2026-01-01 00:00:00';
const DETAIL_TOP_ERRORS_LIMIT = 18;
const OPTIMIZED_MIN_SAMPLES = 300;
const OPTIMIZED_BASE_LAMBDA = 12;
const PEAK_FORMULA_LAMBDA = 18;
const PEAK_UNDER_QUANTILE = 0.8;
const PEAK_SCORE_WEIGHTS = Object.freeze({
  preLunch: 0.08,
  lunchPeak: 0.22
});
const FORMULA_BASE = 'base';
const FORMULA_GAP_TREND = 'gapTrend';
const FORMULA_LEVEL_TREND = 'levelTrend';
const FORMULA_PEAK_CHASE = 'peakChase';
const FORMULA_PEAK_BRIDGE = 'peakBridge';
const FORMULA_PRESSURE_TREND = 'pressureTrend';
const FORMULA_BASE_RESIDUAL = 'baseResidual';
const FORCED_OPTIMIZED_FORMULA_KEY = FORMULA_PRESSURE_TREND;
const FORMULA_FAMILIES = Object.freeze({
  [FORMULA_LEVEL_TREND]: {
    label: '수준·추세식',
    shortLabel: 'levelTrend',
    lambda: OPTIMIZED_BASE_LAMBDA,
    featureKeys: ['intercept', 'currentValue', 'targetBaseline', 'trend15', 'trend30'],
    formula: '절편 + 현재값×a + 목표 평소값×b + 15분 추세×c + 30분 추세×d',
    note: '현재 수준과 직전 흐름을 함께 본다.'
  },
  [FORMULA_GAP_TREND]: {
    label: '차이·추세식',
    shortLabel: 'gapTrend',
    lambda: OPTIMIZED_BASE_LAMBDA,
    featureKeys: ['intercept', 'targetBaseline', 'gapPos', 'gapNeg', 'rise15', 'rise30', 'fall15', 'fall30', 'baselineShiftSigned'],
    formula: '절편 + 목표 평소값×a + 초과차이×b + 부족차이×c + 상승추세×d + 하강추세×e + 평소값 이동량×f',
    note: '평소값 대비 차이를 상승/하강으로 나눠 반영한다.'
  },
  [FORMULA_PEAK_CHASE]: {
    label: '피크 추격식',
    shortLabel: 'peakChase',
    lambda: PEAK_FORMULA_LAMBDA,
    featureKeys: ['intercept', 'currentValue', 'targetBaseline', 'pressureGap', 'baselineShiftPos', 'rise15', 'rise30', 'surge15'],
    formula: '절편 + 현재값×a + 목표 평소값×b + 피크압력×c + 상승추세×d + 점심 진입 가속×e',
    note: '피크 직전 상승을 더 빠르게 따라가도록 설계했다.'
  },
  [FORMULA_PEAK_BRIDGE]: {
    label: '피크 연결식',
    shortLabel: 'peakBridge',
    lambda: PEAK_FORMULA_LAMBDA,
    featureKeys: ['intercept', 'currentValue', 'baselineShiftSigned', 'targetBaseline', 'rise15', 'rise30', 'gapPos'],
    formula: '절편 + 현재값×a + 평소값 이동량×b + 목표 평소값×c + 상승추세×d + 초과차이×e',
    note: '현재 수준에서 목표 시간대로 이어 붙이는 식이다.'
  },
  [FORMULA_PRESSURE_TREND]: {
    label: '피크 압력식',
    shortLabel: 'pressureTrend',
    lambda: PEAK_FORMULA_LAMBDA,
    featureKeys: ['intercept', 'targetBaseline', 'currentValue', 'pressureGap', 'baselineShiftPos', 'gapPos', 'surge30'],
    formula: '절편 + 목표 평소값×a + 현재값×b + 피크압력×c + 상승추세×d + 점심 진입 가속×e + 초과차이×f',
    note: '점심 진입/피크 구간의 과소예측을 줄이기 위한 반응형 식이다.'
  },
  [FORMULA_BASE_RESIDUAL]: {
    label: '기본식 + 잔차 보정',
    shortLabel: 'baseResidual',
    lambda: OPTIMIZED_BASE_LAMBDA,
    featureKeys: ['pressureGap', 'baselineShiftPos', 'gapPos', 'gapNeg', 'rise30'],
    formula: '기본식 + a×피크 압력 + b×상승 평소값 이동 + c×현재 초과분 + d×현재 부족분 + e×최근 30분 상승',
    note: '기본식을 기준점으로 두고, 남는 오차만 추가 항목으로 보정합니다.'
  }
});

FORMULA_FAMILIES[FORMULA_PRESSURE_TREND].label = '피크 압력식';
FORMULA_FAMILIES[FORMULA_PRESSURE_TREND].featureKeys = ['targetBaseline', 'currentValue', 'pressureGap', 'baselineShiftPos', 'gapPos', 'surge30'];
FORMULA_FAMILIES[FORMULA_PRESSURE_TREND].formula = '목표 평소값 + 현재값 + 피크 압력 + 상승 평소값 이동 + 현재 초과분 + 30분 가속';
FORMULA_FAMILIES[FORMULA_PRESSURE_TREND].note = '최종 운영식입니다. 절편 없이 6개 항목만 사용하고, 피크 구간 보정에 필요한 값만 남겼습니다.';
const QUALITY_NEAR_ZERO_MAX = 2;
const QUALITY_NEAR_ZERO_NEIGHBOR_MIN = 18;
const QUALITY_ZERO_NEIGHBOR_MIN = 12;
const QUALITY_ZERO_NEIGHBOR_GAP_MAX = 20;
const QUALITY_SPIKE_NEIGHBOR_GAP_MAX = 18;
const QUALITY_SPIKE_DEVIATION_MIN = 30;
const QUALITY_SPIKE_DEVIATION_RATIO = 0.45;
const ACTIVE_SLOT_MIN_COUNT = 8;
const ACTIVE_SLOT_NON_ZERO_RATE = 0.2;
const ACTIVE_SLOT_POSITIVE_AVG = 8;
const FEATURE_PRUNE_OVERALL_MAX_MAE_DELTA = 0.15;
const FEATURE_PRUNE_HORIZON_MAX_MAE_DELTA = 0.1;
const SURGE30_KEEP_OVERALL_MIN_MAE_DELTA = 0.25;
const SURGE30_KEEP_HORIZON_MIN_MAE_DELTA = 0.35;
const EXTERNAL_FEATURE_MIN_IMPROVEMENT = 0.15;
const EXTERNAL_FEATURE_MAX_HORIZON_DEGRADATION = 0.05;
const EXTERNAL_MENU_MIN_COVERAGE = 0.85;
const SAFETY_BLEND_MIN = 0.35;
const SAFETY_GAIN_FULL_EFFECT = 1.8;
const SAFETY_SAMPLE_FULL_EFFECT = 900;
const SAFETY_SIGNAL_FULL_EFFECT = 90;
const SAFETY_POSITIVE_CAP_BASE = 10;
const SAFETY_POSITIVE_CAP_PRESSURE_WEIGHT = 0.55;
const SAFETY_POSITIVE_CAP_SHIFT_WEIGHT = 0.35;
const SAFETY_POSITIVE_CAP_GAP_WEIGHT = 0.15;
const SAFETY_NEGATIVE_CAP_BASE = 8;
const SAFETY_NEGATIVE_CAP_GAP_WEIGHT = 0.3;
const SAFETY_NEGATIVE_CAP_EXCESS_WEIGHT = 0.15;
const EXTERNAL_FEATURE_CANDIDATES = Object.freeze([
  Object.freeze({
    key: 'isHoliday',
    label: '공휴일 여부',
    source: 'holiday',
    coverageKey: null
  }),
  Object.freeze({
    key: 'menuOpenAny',
    label: '메뉴 운영 여부',
    source: 'menu',
    coverageKey: 'menuDataAvailable'
  }),
  Object.freeze({
    key: 'menuOpenCount',
    label: '운영 메뉴 수',
    source: 'menu',
    coverageKey: 'menuDataAvailable'
  }),
  Object.freeze({
    key: 'menuMenuCount',
    label: '등록 메뉴 수',
    source: 'menu',
    coverageKey: 'menuDataAvailable'
  }),
  Object.freeze({
    key: 'menuClosedAll',
    label: '메뉴 전부 마감',
    source: 'menu',
    coverageKey: 'menuDataAvailable'
  })
]);
const DB_RETRY_ATTEMPTS = 2;
const DB_RETRY_DELAY_MS = 250;
const LIBRARY_PROFILES = Object.freeze({
  [PROFILE_ALL_DATA]: Object.freeze({
    key: PROFILE_ALL_DATA,
    label: '전체 누적 데이터',
    shortLabel: '전체',
    trainingEndExclusive: TUNING_DATA_END_EXCLUSIVE,
    description: 'DB에 있는 전체 누적 데이터로 학습한 뒤 선택 날짜를 확인합니다.',
    recommendedDateHint: '전체 기간 확인용'
  }),
  [PROFILE_TRAIN_TO_2025]: Object.freeze({
    key: PROFILE_TRAIN_TO_2025,
    label: '2025까지 학습 → 2026 검증',
    shortLabel: '2025→2026',
    trainingEndExclusive: TRAIN_TO_2025_END_EXCLUSIVE,
    description: '2025-12-31까지의 데이터로만 학습한 고정식을 2026 날짜에 적용해 확인합니다.',
    recommendedDateHint: '2026 날짜 검증용'
  })
});

const TIME_BANDS = Object.freeze([
  { key: 'morning', start: 0, end: 599, label: '오전 07:00-09:59' },
  { key: 'preLunch', start: 600, end: 689, label: '점심 진입 10:00-11:29' },
  { key: 'lunchPeak', start: 690, end: 809, label: '점심 피크 11:30-13:29' },
  { key: 'postLunch', start: 810, end: 929, label: '점심 이후 13:30-15:29' },
  { key: 'afternoon', start: 930, end: 1049, label: '오후 15:30-17:29' },
  { key: 'dinner', start: 1050, end: 1439, label: '저녁 17:30-23:59' }
]);

const WEIGHT_CANDIDATES = Object.freeze(Array.from({ length: 11 }, (_, index) => round1(index / 10)));

const FALLBACK_COMMON_WEIGHTS = freezeWeights({
  30: 0.8,
  60: 0.7,
  90: 0.6
});

const FALLBACK_RESTAURANT_WEIGHTS = Object.freeze({
  제1학생회관: freezeWeights({ 30: 0.9, 60: 0.7, 90: 0.7 }),
  제2학생회관: freezeWeights({ 30: 0.9, 60: 0.7, 90: 0.6 }),
  제3학생회관: freezeWeights({ 30: 0.9, 60: 0.6, 90: 0.6 }),
  상록회관: freezeWeights({ 30: 0.9, 60: 0.8, 90: 0.8 }),
  생활과학대: freezeWeights({ 30: 0.9, 60: 0.7, 90: 0.6 }),
  학생생활관: freezeWeights({ 30: 0.6, 60: 0.4, 90: 0.2 })
});

const pool = mysql.createPool({
  host: process.env.DB_HOST || '219.255.141.135',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'seeandyougo',
  password: process.env.DB_PASSWORD || 'seeandyougo',
  database: process.env.DB_NAME || 'seeandyougo',
  waitForConnections: true,
  connectionLimit: 5,
  enableKeepAlive: true,
  keepAliveInitialDelay: 0
});

const optimizedLibraryPromiseByProfile = new Map();

app.use(express.static(join(process.cwd(), 'public')));

app.get('/api/restaurants', async (req, res) => {
  try {
    const optimizedLibrary = await getOptimizedLibrary(DEFAULT_PROFILE);
    const trainTo2025Library = await getOptimizedLibrary(PROFILE_TRAIN_TO_2025);
    res.json({
      restaurants: RESTAURANTS,
      defaultMode: DEFAULT_MODE,
      defaultFormulaStrategy: DEFAULT_FORMULA_STRATEGY,
      defaultProfile: DEFAULT_PROFILE,
      profiles: {
        [PROFILE_ALL_DATA]: buildLibraryMetadataPayload(optimizedLibrary),
        [PROFILE_TRAIN_TO_2025]: buildLibraryMetadataPayload(trainTo2025Library)
      },
      recommendedWeights: optimizedLibrary.summary.recommendedWeights,
      optimizedSummary: optimizedLibrary.summary,
      optimizedValidation: {
        [STRATEGY_BANDED]: optimizedLibrary.validationByHorizon,
        [STRATEGY_RESTAURANT_HORIZON]: optimizedLibrary.fixedValidationByHorizon
      },
      formulaStrategies: buildFormulaStrategyPayload(optimizedLibrary)
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

app.get('/api/feature-analysis', async (req, res) => {
  try {
    const profile = parseProfile(req.query.profile);
    const optimizedLibrary = await getOptimizedLibrary(profile);
    res.json({
      profile: buildProfilePayload(optimizedLibrary.profile),
      featureAnalysis: optimizedLibrary.featureAnalysis
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.get('/api/predictions', async (req, res) => {
  try {
    const restaurant = parseRestaurant(req.query.restaurant);
    const time = parseDateTime(String(req.query.time || ''));
    const mode = parseWeightMode(req.query.mode);
    const formulaStrategy = parseFormulaStrategy(req.query.strategy);
    const profile = parseProfile(req.query.profile);
    const optimizedLibrary = await getOptimizedLibrary(profile);
    const weights = resolveWeights(req.query, restaurant, mode, optimizedLibrary.summary.recommendedWeights);
    const currentValue = await findLatestConnected(restaurant, time);
    const currentBaseline = await baseline(restaurant, time, optimizedLibrary);
    const recentRows = await loadRecentRows(restaurant, time, 30);
    const trend15 = recentTrendFromRecentRows(recentRows, time, 15, currentValue);
    const trend30 = recentTrendFromRecentRows(recentRows, time, 30, currentValue);

    const predictions = [];
    for (const horizonMinutes of HORIZONS) {
      const targetDate = addMinutes(time, horizonMinutes);
      const targetBaseline = await baseline(restaurant, targetDate, optimizedLibrary);
      const targetBand = timeBandFor(formatTime(targetDate));
      const shared = buildSharedPredictionState({
        time,
        targetDate,
        horizonMinutes,
        currentValue,
        actual: null,
        currentBaseline,
        targetBaseline,
        trend15,
        trend30,
        targetBand
      });

      const baseRow = buildPredictionRow(MODEL_BASE, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy);
      const adjustedRow = buildPredictionRow(MODEL_ADJUSTED, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy);

      predictions.push({
        horizonMinutes,
        targetTime: formatDateTime(targetDate),
        targetBaseline: round1(targetBaseline),
        weight: weights[horizonMinutes],
        targetBand: targetBand.label,
        basePredictedValue: baseRow.predicted,
        adjustedPredictedValue: adjustedRow.predicted,
        baseRule: baseRow.rule,
        adjustedRule: adjustedRow.rule,
        adjustedModelType: adjustedRow.optimizedModelType,
        adjustedAdjustment: adjustedRow.adjustmentTotal
      });
    }

    res.json({
      restaurant,
      time: formatDateTime(time),
      currentValue,
      currentBaseline: round1(currentBaseline),
      baselineMode: baselineModeLabel(restaurant),
      season: seasonLabel(time.getMonth() + 1),
      profile: buildProfilePayload(optimizedLibrary.profile),
      parameters: buildParameterPayload(restaurant, mode, weights, optimizedLibrary, formulaStrategy),
      formulas: formulaPayloadV2(optimizedLibrary),
      formulaStrategies: buildFormulaStrategyPayload(optimizedLibrary),
      fixedModel: buildFixedModelPayload(optimizedLibrary),
      predictions
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.get('/api/history', async (req, res) => {
  try {
    const restaurant = parseRestaurant(req.query.restaurant);
    const date = parseDate(String(req.query.date || ''));
    const referenceDate = parseDateTime(`${date} 12:00:00`);
    const mode = parseWeightMode(req.query.mode);
    const formulaStrategy = parseFormulaStrategy(req.query.strategy);
    const profile = parseProfile(req.query.profile);
    const optimizedLibrary = await getOptimizedLibrary(profile);
    const weights = resolveWeights(req.query, restaurant, mode, optimizedLibrary.summary.recommendedWeights);
    const summaryOnly = String(req.query.summaryOnly || '').toLowerCase() === 'true';
    const rowsLimit = integerParam(req.query.rowsLimit, 0);

    const { rows: dayRows, quality: dayQuality } = await loadActualDay(restaurant, date, optimizedLibrary);
    if (!dayRows.length) {
      throw new Error(`선택한 날짜의 실제 데이터가 없습니다: ${restaurant}, ${date}`);
    }

    const baselineMaps = {
      season: await loadBaselineMap(restaurant, referenceDate, true, optimizedLibrary),
      allYear: await loadBaselineMap(restaurant, referenceDate, false, optimizedLibrary)
    };
    const actualByTime = new Map(dayRows.map((row) => [formatTime(row.date), row.connected]));

    const horizons = HORIZONS.map((horizonMinutes) => {
      const rowsByModel = {
        [MODEL_BASE]: [],
        [MODEL_ADJUSTED]: []
      };

      for (let index = 0; index < dayRows.length; index += 1) {
        const row = dayRows[index];
        const targetDate = addMinutes(row.date, horizonMinutes);
        if (dateOnly(targetDate) !== date) continue;

        const actual = actualByTime.get(formatTime(targetDate));
        if (actual === undefined) continue;

        const currentBaseline = baselineFromMaps(restaurant, row.date, baselineMaps);
        const targetBaseline = baselineFromMaps(restaurant, targetDate, baselineMaps);
        const targetBand = timeBandFor(formatTime(targetDate));
        const shared = buildSharedPredictionState({
          time: row.date,
          targetDate,
          horizonMinutes,
          currentValue: row.connected,
          actual,
          currentBaseline,
          targetBaseline,
          trend15: recentTrendFromRows(dayRows, index, row.date, 15),
          trend30: recentTrendFromRows(dayRows, index, row.date, 30),
          targetBand
        });

        rowsByModel.base.push(buildPredictionRow(MODEL_BASE, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy));
        rowsByModel.adjusted.push(buildPredictionRow(MODEL_ADJUSTED, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy));
      }

      const models = {
        base: serializeModelResult(
          MODEL_BASE,
          '기본식',
          rowsByModel.base,
          summaryOnly,
          rowsLimit,
          ruleLabelBase(restaurant, weights[horizonMinutes])
        ),
        adjusted: serializeModelResult(
          MODEL_ADJUSTED,
          '최적화식',
          rowsByModel.adjusted,
          summaryOnly,
          rowsLimit,
          optimizedRuleLabel(optimizedLibrary, restaurant, horizonMinutes)
        )
      };

      return {
        horizonMinutes,
        weight: weights[horizonMinutes],
        comparison: compareModels(models.base, models.adjusted),
        models
      };
    });

    res.json({
      restaurant,
      date,
      baselineMode: baselineModeLabel(restaurant),
      season: seasonLabel(referenceDate.getMonth() + 1),
      profile: buildProfilePayload(optimizedLibrary.profile),
      formulas: formulaPayloadV2(optimizedLibrary),
      formulaStrategies: buildFormulaStrategyPayload(optimizedLibrary),
      fixedModel: buildFixedModelPayload(optimizedLibrary),
      parameters: buildParameterPayload(restaurant, mode, weights, optimizedLibrary, formulaStrategy),
      quality: dayQuality,
      comparisonSummary: buildComparisonSummary(horizons),
      rowsLimit,
      horizons
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.get('/api/history-compare', async (req, res) => {
  try {
    const restaurant = parseRestaurant(req.query.restaurant);
    const date = parseDate(String(req.query.date || ''));
    const referenceDate = parseDateTime(`${date} 12:00:00`);
    const mode = parseWeightMode(req.query.mode);
    const formulaStrategy = parseFormulaStrategy(req.query.strategy);
    const profile = parseProfile(req.query.profile);
    const optimizedLibrary = await getOptimizedLibrary(profile);
    const weights = resolveWeights(req.query, restaurant, mode, optimizedLibrary.summary.recommendedWeights);
    const summaryOnly = String(req.query.summaryOnly || '').toLowerCase() === 'true';
    const rowsLimit = integerParam(req.query.rowsLimit, 0);

    const { rows: dayRows, quality: dayQuality } = await loadActualDay(restaurant, date, optimizedLibrary);
    if (!dayRows.length) {
      throw new Error(`선택한 날짜의 실제 데이터가 없습니다: ${restaurant}, ${date}`);
    }

    const baselineMaps = {
      season: await loadBaselineMap(restaurant, referenceDate, true, optimizedLibrary),
      allYear: await loadBaselineMap(restaurant, referenceDate, false, optimizedLibrary)
    };
    const actualByTime = new Map(dayRows.map((row) => [formatTime(row.date), row.connected]));

    const horizons = HORIZONS.map((horizonMinutes) => {
      const rowsByModel = {
        [MODEL_BASE]: [],
        [MODEL_ADJUSTED]: [],
        [MODEL_RESIDUAL]: []
      };

      for (let index = 0; index < dayRows.length; index += 1) {
        const row = dayRows[index];
        const targetDate = addMinutes(row.date, horizonMinutes);
        if (dateOnly(targetDate) !== date) continue;

        const actual = actualByTime.get(formatTime(targetDate));
        if (actual === undefined) continue;

        const currentBaseline = baselineFromMaps(restaurant, row.date, baselineMaps);
        const targetBaseline = baselineFromMaps(restaurant, targetDate, baselineMaps);
        const targetBand = timeBandFor(formatTime(targetDate));
        const shared = buildSharedPredictionState({
          time: row.date,
          targetDate,
          horizonMinutes,
          currentValue: row.connected,
          actual,
          currentBaseline,
          targetBaseline,
          trend15: recentTrendFromRows(dayRows, index, row.date, 15),
          trend30: recentTrendFromRows(dayRows, index, row.date, 30),
          targetBand
        });

        rowsByModel[MODEL_BASE].push(
          buildPredictionRow(MODEL_BASE, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy)
        );
        rowsByModel[MODEL_ADJUSTED].push(
          buildPredictionRow(MODEL_ADJUSTED, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy)
        );
        rowsByModel[MODEL_RESIDUAL].push(
          buildPredictionRow(MODEL_RESIDUAL, restaurant, shared, weights[horizonMinutes], optimizedLibrary, formulaStrategy)
        );
      }

      const models = {
        base: serializeModelResult(
          MODEL_BASE,
          '기본식',
          rowsByModel[MODEL_BASE],
          summaryOnly,
          rowsLimit,
          ruleLabelBase(restaurant, weights[horizonMinutes])
        ),
        adjusted: serializeModelResult(
          MODEL_ADJUSTED,
          '현재 운영식',
          rowsByModel[MODEL_ADJUSTED],
          summaryOnly,
          rowsLimit,
          optimizedRuleLabel(optimizedLibrary, restaurant, horizonMinutes)
        ),
        residual: serializeModelResult(
          MODEL_RESIDUAL,
          '제안식',
          rowsByModel[MODEL_RESIDUAL],
          summaryOnly,
          rowsLimit,
          ruleLabelResidual(residualRuleFor(optimizedLibrary, restaurant, horizonMinutes))
        )
      };

      const comparisons = {
        adjustedVsBase: compareModelPair(MODEL_BASE, models.base, MODEL_ADJUSTED, models.adjusted),
        residualVsBase: compareModelPair(MODEL_BASE, models.base, MODEL_RESIDUAL, models.residual),
        residualVsAdjusted: compareModelPair(MODEL_ADJUSTED, models.adjusted, MODEL_RESIDUAL, models.residual)
      };

      return {
        horizonMinutes,
        weight: weights[horizonMinutes],
        bestModelKey: pickBestModelKey(models),
        comparisons,
        methodSpecs: {
          base: buildBaseMethodPayload(weights[horizonMinutes]),
          adjusted: buildAdjustedMethodPayload(optimizedLibrary, restaurant, horizonMinutes),
          residual: buildResidualMethodPayload(optimizedLibrary, restaurant, horizonMinutes)
        },
        models
      };
    });

    res.json({
      restaurant,
      date,
      baselineMode: baselineModeLabel(restaurant),
      season: seasonLabel(referenceDate.getMonth() + 1),
      profile: buildProfilePayload(optimizedLibrary.profile),
      parameters: buildParameterPayload(restaurant, mode, weights, optimizedLibrary, formulaStrategy),
      quality: dayQuality,
      rowsLimit,
      comparisonSummary: buildTripleComparisonSummary(horizons),
      horizons
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.listen(port, () => {
  console.log(`congestion prediction viewer: http://localhost:${port}`);
  void Promise.all([
    getOptimizedLibrary(PROFILE_ALL_DATA),
    getOptimizedLibrary(PROFILE_TRAIN_TO_2025)
  ])
    .then((libraries) => {
      for (const library of libraries) {
        console.log(
          `[${library.profile.shortLabel}] optimized viewer model ready: banded 60m ${library.validationByHorizon[60].optimizedMae}, 90m ${library.validationByHorizon[90].optimizedMae} / simple 60m ${library.fixedValidationByHorizon[60].optimizedMae}, 90m ${library.fixedValidationByHorizon[90].optimizedMae}`
        );
      }
    })
    .catch((error) => {
      console.error('optimized viewer model warmup failed:', error.message);
    });
});

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function isRetryableDbError(error) {
  const code = String(error?.code || '');
  return code === 'ECONNRESET'
    || code === 'PROTOCOL_CONNECTION_LOST'
    || code === 'ETIMEDOUT'
    || code === 'EPIPE'
    || code === 'PROTOCOL_ENQUEUE_AFTER_FATAL_ERROR';
}

async function queryWithRetry(sql, params = [], attempts = DB_RETRY_ATTEMPTS) {
  let lastError = null;

  for (let attempt = 0; attempt <= attempts; attempt += 1) {
    try {
      return await pool.query(sql, params);
    } catch (error) {
      lastError = error;
      if (!isRetryableDbError(error) || attempt >= attempts) throw error;
      await sleep(DB_RETRY_DELAY_MS * (attempt + 1));
    }
  }

  throw lastError;
}

function loadDotEnv() {
  try {
    const env = readFileSync(join(process.cwd(), '.env'), 'utf8');
    env.split(/\r?\n/).forEach((line) => {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('#')) return;

      const index = trimmed.indexOf('=');
      if (index <= 0) return;

      const key = trimmed.slice(0, index).trim();
      const value = trimmed.slice(index + 1).trim();
      if (!process.env[key]) process.env[key] = value;
    });
  } catch {
    // .env is optional.
  }
}

function parseRestaurant(value) {
  const input = String(value || '').trim();
  const restaurant = RESTAURANTS.find((name, index) => input === name || input === String(index + 1) || input.includes(name));
  if (!restaurant) throw new Error(`식당명이 올바르지 않습니다: ${input}`);
  return restaurant;
}

function parseWeightMode(value) {
  const input = String(value || '').trim().toLowerCase();
  if (input === 'restaurant' || input === 'custom') return input;
  return DEFAULT_MODE;
}

function parseFormulaStrategy(value) {
  const input = String(value || '').trim();
  if (input === STRATEGY_RESTAURANT_HORIZON) return input;
  return DEFAULT_FORMULA_STRATEGY;
}

function parseProfile(value) {
  const input = String(value || '').trim();
  if (input === PROFILE_TRAIN_TO_2025) return PROFILE_TRAIN_TO_2025;
  return DEFAULT_PROFILE;
}

function resolveWeights(query, restaurant, mode, recommendationSet) {
  const recommended = recommendedWeights(restaurant, mode, recommendationSet);
  return freezeWeights({
    30: clampWeight(numberParam(query.w30, recommended[30])),
    60: clampWeight(numberParam(query.w60, recommended[60])),
    90: clampWeight(numberParam(query.w90, recommended[90]))
  });
}

function recommendedWeights(restaurant, mode, recommendationSet) {
  const common = recommendationSet?.common || FALLBACK_COMMON_WEIGHTS;
  const restaurantWeights = recommendationSet?.restaurant?.[restaurant] || FALLBACK_RESTAURANT_WEIGHTS[restaurant] || common;
  if (mode === 'restaurant') return restaurantWeights;
  return common;
}

function recommendationPayload(restaurant, optimizedLibrary) {
  const recommendations = optimizedLibrary.summary.recommendedWeights || {
    common: FALLBACK_COMMON_WEIGHTS,
    restaurant: FALLBACK_RESTAURANT_WEIGHTS
  };
  return {
    common: recommendations.common,
    restaurant: recommendations.restaurant[restaurant] || recommendations.common,
    restaurantAll: recommendations.restaurant
  };
}

function formulaStrategyMeta(strategyKey) {
  if (strategyKey === STRATEGY_RESTAURANT_HORIZON) {
    return {
      key: STRATEGY_RESTAURANT_HORIZON,
      label: '식당×분 고정식',
      note: '식당별 30/60/90분만 나누고, 시간대는 구분하지 않는 단순 운영안',
      default: strategyKey === DEFAULT_FORMULA_STRATEGY
    };
  }

  return {
    key: STRATEGY_BANDED,
    label: '시간대별 고정식',
    note: '식당별 30/60/90분과 목표 시간대까지 나누는 상세 운영안',
    default: strategyKey === DEFAULT_FORMULA_STRATEGY
  };
}

function formulaPayload(optimizedLibrary) {
  const pressureFamily = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY);
  return {
    base: '목표 평소값 + (현재값 - 현재 시점 평소값) × 반영률',
    optimized: {
      key: pressureFamily.shortLabel,
      formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
      label: pressureFamily.label,
      shortLabel: pressureFamily.shortLabel,
      formula: pressureFamily.formula,
      note: '이제 모든 조정식은 피크 압력식 하나로 고정하고, 그룹마다 계수만 다시 학습합니다.'
    },
    featureDocs: [
      {
        key: 'intercept',
        label: '절편',
        meaning: '모든 입력 특징이 0이라고 가정했을 때의 출발점',
        detail: '미리 손으로 계산하는 값이 아니라, 학습 데이터에서 오차가 가장 작아지도록 다른 계수와 함께 동시에 추정합니다.'
      },
      {
        key: 'targetBaseline',
        label: '목표 평소값',
        meaning: '30/60/90분 뒤 같은 식당·같은 요일·같은 시간대의 평균 혼잡도',
        detail: '앞으로 원래 어느 정도까지 올라가거나 내려갈 시간대인지 알려주는 기준값입니다.'
      },
      {
        key: 'currentValue',
        label: '현재값',
        meaning: '기준 시각의 실제 혼잡도',
        detail: '지금 이미 얼마나 붐비는지 직접 반영합니다.'
      },
      {
        key: 'pressureGap',
        label: '피크압력',
        meaning: 'max(목표 평소값 - 현재값, 0)',
        detail: '앞으로 더 붐빌 여지가 남아 있을수록 커집니다.'
      },
      {
        key: 'baselineShiftPos',
        label: '평소값 상승분',
        meaning: 'max(목표 평소값 - 현재 평소값, 0)',
        detail: '원래 시간대 흐름상 지금보다 나중이 더 붐비는 구간인지 나타냅니다.'
      },
      {
        key: 'rise15',
        label: '15분 상승량',
        meaning: 'max(최근 15분 변화량, 0)',
        detail: '직전 15분 동안 실제 혼잡도가 올라가는 속도를 반영합니다.'
      },
      {
        key: 'rise30',
        label: '30분 상승량',
        meaning: 'max(최근 30분 변화량, 0)',
        detail: '직전 30분 동안의 좀 더 긴 상승 흐름입니다.'
      },
      {
        key: 'gapPos',
        label: '현재 초과분',
        meaning: 'max(현재값 - 현재 평소값, 0)',
        detail: '지금이 평소보다 얼마나 더 붐비는지 나타냅니다.'
      },
      {
        key: 'surge30',
        label: '30분 가속',
        meaning: '(피크압력 × 30분 상승량) / 50',
        detail: '앞으로 더 올라갈 여지도 크고 최근 상승도 빠를수록 값이 커지는 가속 항입니다.'
      }
    ],
    training: {
      objective: 'Σ(actual - predicted)^2 + λΣβ_j^2',
      lambda: pressureFamily.lambda,
      fitNote: '각 식당 그룹의 튜닝 데이터로 ridge regression을 적용해 계수를 추정합니다.',
      interceptNote: '코드상 절편은 intercept 계수 하나로 포함되며, 규제(λ)는 절편을 제외한 나머지 계수에만 적용합니다.',
      fixedNote: '한 번 학습된 계수는 같은 식당·같은 예측 구간 그룹에서 고정값으로 사용되고, 예측 요청마다 다시 바뀌지 않습니다.',
      dynamicNote: '예측 시점마다 다시 계산되는 것은 currentValue, targetBaseline, pressureGap, rise15 같은 입력 feature 값들입니다.',
      scopeNote: '식당×분 고정식은 식당별 30/60/90분마다 한 세트, 시간대별 고정식은 식당별 30/60/90분·시간대마다 한 세트를 사용합니다.'
    },
    selection: `${tuningWindowLabel(optimizedLibrary.summary.tuningWindow)} 누적 데이터 기준으로 조정식은 피크 압력식 하나로 고정하고, 운영 구조만 식당×분 또는 시간대별 단위로 나눕니다.`
  };
}

function formulaPayloadV2(optimizedLibrary) {
  const pressureFamily = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY);
  return {
    base: '예측값 = 목표 평소값 + (현재값 - 현재 평소값) × 반영률',
    optimized: {
      key: pressureFamily.shortLabel,
      formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
      label: pressureFamily.label,
      shortLabel: pressureFamily.shortLabel,
      formula: '예측값 = 절편 + a×목표 평소값 + b×현재값 + c×피크 압력 + d×상승 평소값 이동 + e×현재 초과분 + f×30분 가속',
      note: '피크 압력식은 식당×30/60/90분마다 계수를 고정하고, 예측 시점마다 입력값만 다시 계산합니다.'
    },
    featureDocs: [
      {
        key: 'intercept',
        label: '절편',
        meaning: '모든 입력값이 0일 때의 시작값',
        detail: '미리 정하는 상수가 아니라, 전체 오차가 가장 작아지도록 데이터에서 함께 추정한 값입니다.'
      },
      {
        key: 'targetBaseline',
        label: '목표 평소값',
        meaning: '30/60/90분 뒤 같은 식당·같은 요일·같은 시각의 평균 혼잡도',
        detail: '앞으로 원래 어느 정도까지 올라가거나 내려가는 시간대인지 보여주는 기준값입니다.'
      },
      {
        key: 'currentValue',
        label: '현재값',
        meaning: '기준 시각의 실제 혼잡도',
        detail: '지금 이 식당이 실제로 얼마나 붐비는지 직접 반영합니다.'
      },
      {
        key: 'pressureGap',
        label: '피크 압력',
        meaning: 'max(목표 평소값 - 현재값, 0)',
        detail: '앞으로 더 붐빌 여지가 남아 있으면 커지는 값입니다.'
      },
      {
        key: 'baselineShiftPos',
        label: '상승 평소값 이동',
        meaning: 'max(목표 평소값 - 현재 평소값, 0)',
        detail: '평소 흐름상 앞으로 더 붐비는 구간인지 보여줍니다.'
      },
      {
        key: 'gapPos',
        label: '현재 초과분',
        meaning: 'max(현재값 - 현재 평소값, 0)',
        detail: '지금이 평소보다 얼마나 더 붐비는지 나타냅니다.'
      },
      {
        key: 'surge30',
        label: '30분 가속',
        meaning: '(피크 압력 × 30분 상승) / 50',
        detail: '앞으로 더 붐빌 압력과 최근 30분 상승이 동시에 크면 추가로 반응하는 값입니다.'
      }
    ],
    training: {
      objective: 'Σ(actual - predicted)^2 + λΣβ_j^2',
      lambda: pressureFamily.lambda,
      fitNote: '각 식당 그룹의 과거 데이터에 ridge regression을 적용해 계수를 추정합니다.',
      interceptNote: '절편은 학습에 포함되지만 규제는 절편을 제외한 나머지 계수에만 적용합니다.',
      fixedNote: '한 번 학습한 계수는 같은 식당×예측 구간 그룹에서 고정값으로 사용하고, 예측 요청마다 다시 바뀌지 않습니다.',
      dynamicNote: '예측 시점마다 다시 계산되는 것은 currentValue, targetBaseline, pressureGap, surge30 같은 입력 feature 값들입니다.',
      scopeNote: '현재 운영 모드는 식당별 30/60/90분 단위로 하나의 고정 세트를 사용합니다.'
    },
    selection: `${tuningWindowLabel(optimizedLibrary.summary.tuningWindow)} 누적 데이터 기준으로 조정식은 피크 압력식 하나로 고정하고, 운영 구조만 식당×30/60/90분 단위로 나눕니다.`
  };
}

function buildProfilePayload(profileConfig) {
  if (!profileConfig) {
    return {
      key: DEFAULT_PROFILE,
      label: '전체 누적 데이터',
      shortLabel: '전체',
      description: 'DB에 있는 전체 누적 데이터로 학습한 뒤 선택 날짜를 확인합니다.',
      recommendedDateHint: '전체 기간 확인용'
    };
  }

  return {
    key: profileConfig.key,
    label: profileConfig.label,
    shortLabel: profileConfig.shortLabel,
    description: profileConfig.description,
    recommendedDateHint: profileConfig.recommendedDateHint
  };
}

function buildLibraryMetadataPayload(optimizedLibrary) {
  return {
    profile: buildProfilePayload(optimizedLibrary.profile),
    recommendedWeights: optimizedLibrary.summary.recommendedWeights,
    optimizedSummary: optimizedLibrary.summary,
    optimizedValidation: {
      [STRATEGY_BANDED]: optimizedLibrary.validationByHorizon,
      [STRATEGY_RESTAURANT_HORIZON]: optimizedLibrary.fixedValidationByHorizon
    },
    formulaStrategies: buildFormulaStrategyPayload(optimizedLibrary)
  };
}

function buildParameterPayload(restaurant, mode, weights, optimizedLibrary, formulaStrategy) {
  const tuningWindow = optimizedLibrary.summary.tuningWindow;
  return {
    profile: buildProfilePayload(optimizedLibrary.profile),
    mode,
    weights,
    formulaStrategy: formulaStrategyMeta(formulaStrategy),
    trainingBaseline: trainingBaselineLabel(optimizedLibrary),
    tuningWindow,
    seasonMode: restaurant === '학생생활관' ? '전 기간 평균' : '같은 시즌 평균 우선',
    recommendations: recommendationPayload(restaurant, optimizedLibrary),
    optimized: {
      formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
      formulaLabel: formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).label,
      featureKeys: formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).featureKeys,
      minSamples: OPTIMIZED_MIN_SAMPLES,
      peakScoreWeights: PEAK_SCORE_WEIGHTS,
      safetyGuard: {
        type: 'blendAndCap',
        minBlendRatio: SAFETY_BLEND_MIN,
        note: '피크 압력식으로 계산한 뒤, 과한 보정은 기본식 쪽으로 일부 되돌리고 보정폭 상한을 둡니다.'
      },
      selectedFormulaGroupCount: optimizedLibrary.summary.selectedFormulaGroupCount,
      baseFallbackGroupCount: optimizedLibrary.summary.baseFallbackGroupCount
    },
    quality: {
      removedRowsTuning: optimizedLibrary.summary.removedRowsTuning,
      activeSlotCount: optimizedLibrary.summary.activeSlotCountByRestaurant[restaurant] || 0,
      activeSlotCounts: optimizedLibrary.summary.activeSlotCountByRestaurant,
      removedRowsByRestaurant: optimizedLibrary.summary.removedRowsByRestaurant
    },
    searchNote: formulaStrategy === STRATEGY_RESTAURANT_HORIZON
      ? `최적화식은 ${tuningWindowLabel(tuningWindow)} 누적 데이터에서 식당별 · 30/60/90분별로 피크 압력식 계수를 다시 학습해, 시간대 구분 없이 식당×분 단위의 고정식을 사용합니다.`
      : `최적화식은 ${tuningWindowLabel(tuningWindow)} 누적 데이터에서 식당별 · 30/60/90분별 · 목표 시간대별로 피크 압력식 계수를 다시 학습해, 운영 시에는 그 구간의 고정식을 그대로 사용합니다.`,
    selectionNote: '이제 조정식 종류는 피크 압력식 하나로 통일했고, 점심 진입과 점심 피크는 과소예측 벌점을 반영해 계수가 피크를 더 빨리 따라가도록 학습합니다. 최종 예측에서는 기본식을 기준점으로 삼는 안전장치를 함께 적용합니다.',
    qualityNote: '데이터 품질 필터로 고립된 0·거의 0 값, 한 칸만 튀었다가 바로 되돌아오는 급반등 값, 실제로 거의 운영되지 않는 비활성 시간대는 학습과 비교에서 제외합니다.',
    validationNote: `${optimizedLibrary.profile.label} 학습 기준 적합 MAE: 30분 ${formatValidationDelta(validationByStrategy(optimizedLibrary, formulaStrategy)[30])}, 60분 ${formatValidationDelta(validationByStrategy(optimizedLibrary, formulaStrategy)[60])}, 90분 ${formatValidationDelta(validationByStrategy(optimizedLibrary, formulaStrategy)[90])}`,
    inspectionNote: optimizedLibrary.profile.key === PROFILE_TRAIN_TO_2025
      ? '이 모드는 2025년까지 학습한 고정식을 선택 날짜에 그대로 적용해 보는 2026 확인용 결과입니다. 독립 홀드아웃 점수 요약은 아니고, 선택 날짜 비교 화면입니다.'
      : '선택 날짜 화면은 고정식을 해당 날짜에 그대로 적용해 보는 확인용 결과입니다. 독립 홀드아웃 검증 점수는 아닙니다.'
  };
}

function formatValidationDelta(item) {
  return `${item.baseMae}→${item.optimizedMae}`;
}

function tuningWindowLabel(window) {
  return window?.label || '전체 데이터';
}

function validationByStrategy(optimizedLibrary, formulaStrategy) {
  if (formulaStrategy === STRATEGY_RESTAURANT_HORIZON) {
    return optimizedLibrary.fixedValidationByHorizon;
  }
  return optimizedLibrary.validationByHorizon;
}

function buildFormulaStrategyPayload(optimizedLibrary) {
  return {
    options: [
      formulaStrategyMeta(STRATEGY_BANDED),
      formulaStrategyMeta(STRATEGY_RESTAURANT_HORIZON)
    ],
    activeDefault: DEFAULT_FORMULA_STRATEGY,
    banded: {
      ...formulaStrategyMeta(STRATEGY_BANDED),
      validationByHorizon: optimizedLibrary.validationByHorizon,
      selectedFamilyCounts: optimizedLibrary.summary.selectedFamilyCounts,
      rows: buildBandedStrategyRows(optimizedLibrary.groups)
    },
    restaurantHorizon: {
      ...formulaStrategyMeta(STRATEGY_RESTAURANT_HORIZON),
      validationByHorizon: optimizedLibrary.fixedValidationByHorizon,
      selectedFamilyCounts: optimizedLibrary.summary.fixedSelectedFamilyCounts,
      rows: buildUnifiedStrategyRows(optimizedLibrary.fixedGroups)
    },
    comparisonByHorizon: HORIZONS.map((horizonMinutes) => ({
      horizonMinutes,
      bandedMae: optimizedLibrary.validationByHorizon[horizonMinutes].optimizedMae,
      restaurantHorizonMae: optimizedLibrary.fixedValidationByHorizon[horizonMinutes].optimizedMae,
      delta: round1(
        optimizedLibrary.fixedValidationByHorizon[horizonMinutes].optimizedMae
        - optimizedLibrary.validationByHorizon[horizonMinutes].optimizedMae
      )
      }))
  };
}

function parseDateTime(value) {
  const normalized = value.trim().replace('T', ' ');
  const match = normalized.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (!match) {
    throw new Error('time은 yyyy-MM-ddTHH:mm 또는 yyyy-MM-dd HH:mm:ss 형식이어야 합니다.');
  }

  return new Date(
    Number(match[1]),
    Number(match[2]) - 1,
    Number(match[3]),
    Number(match[4]),
    Number(match[5]),
    Number(match[6] || 0)
  );
}

function normalizeSqlDate(value) {
  if (value instanceof Date) return new Date(value.getTime());
  return parseDateTime(String(value));
}

function normalizeSqlDay(value) {
  if (value instanceof Date) return dateOnly(value);
  const match = String(value).trim().match(/^(\d{4}-\d{2}-\d{2})/);
  if (!match) throw new Error(`invalid sql date: ${value}`);
  return match[1];
}

function rawRowsToSeries(rows) {
  return rows.map((row) => ({
    date: normalizeSqlDate(row.time),
    connected: Number(row.connected)
  }));
}

function groupRowsByDate(rows) {
  const dayMap = new Map();
  for (const row of rows) {
    const key = dateOnly(row.date);
    const list = dayMap.get(key) || [];
    list.push(row);
    dayMap.set(key, list);
  }
  return dayMap;
}

function markValidDayRows(dayRows) {
  return dayRows.map((row, index) => {
    const prev = index > 0 ? dayRows[index - 1] : null;
    const next = index < dayRows.length - 1 ? dayRows[index + 1] : null;
    let invalidReason = null;

    if (row.connected < 0) {
      invalidReason = 'negative';
    } else if (prev && next) {
      const neighborMin = Math.min(prev.connected, next.connected);
      const neighborGap = Math.abs(prev.connected - next.connected);
      const neighborAverage = (prev.connected + next.connected) / 2;

      if (
        row.connected <= QUALITY_NEAR_ZERO_MAX
        && neighborMin >= QUALITY_NEAR_ZERO_NEIGHBOR_MIN
        && neighborGap <= QUALITY_ZERO_NEIGHBOR_GAP_MAX
      ) {
        invalidReason = 'nearZeroDropout';
      } else if (
        row.connected === 0
        && neighborMin >= QUALITY_ZERO_NEIGHBOR_MIN
        && neighborGap <= QUALITY_ZERO_NEIGHBOR_GAP_MAX
      ) {
        invalidReason = 'isolatedZero';
      } else if (
        Math.abs(row.connected - neighborAverage) >= Math.max(QUALITY_SPIKE_DEVIATION_MIN, neighborAverage * QUALITY_SPIKE_DEVIATION_RATIO)
        && neighborGap <= QUALITY_SPIKE_NEIGHBOR_GAP_MAX
      ) {
        invalidReason = 'localSpike';
      }
    }

    return {
      ...row,
      valid: !invalidReason,
      invalidReason
    };
  });
}

function filterQualityRows(rows) {
  const filteredRows = [];
  const removedReasons = {};

  for (const dayRows of groupRowsByDate(rows).values()) {
    const markedRows = markValidDayRows(dayRows);
    for (const row of markedRows) {
      if (!row.valid) {
        removedReasons[row.invalidReason] = (removedReasons[row.invalidReason] || 0) + 1;
        continue;
      }

      filteredRows.push({
        date: row.date,
        connected: row.connected
      });
    }
  }

  return {
    rows: filteredRows,
    removedCount: rows.length - filteredRows.length,
    removedReasons
  };
}

function buildActiveSlotMap(rows) {
  const stats = new Map();

  for (const row of rows) {
    const time = formatTime(row.date);
    const current = stats.get(time) || { total: 0, nonZero: 0, positiveSum: 0 };
    current.total += 1;
    if (row.connected > 0) {
      current.nonZero += 1;
      current.positiveSum += row.connected;
    }
    stats.set(time, current);
  }

  return new Map(Array.from(stats.entries()).map(([time, item]) => {
    const nonZeroRate = item.total ? item.nonZero / item.total : 0;
    const positiveAvg = item.nonZero ? item.positiveSum / item.nonZero : 0;
    return [time, {
      total: item.total,
      nonZero: item.nonZero,
      nonZeroRate,
      positiveAvg,
      active: item.total >= ACTIVE_SLOT_MIN_COUNT && (nonZeroRate >= ACTIVE_SLOT_NON_ZERO_RATE || positiveAvg >= ACTIVE_SLOT_POSITIVE_AVG)
    }];
  }));
}

function filterRowsByActiveSlots(rows, activeSlotMap) {
  return rows.filter((row) => activeSlotMap.get(formatTime(row.date))?.active);
}

function activeSlotCount(activeSlotMap) {
  return Array.from(activeSlotMap.values()).filter((item) => item.active).length;
}

function activeSlotMetaFor(optimizedLibrary, restaurant, date) {
  const time = typeof date === 'string' ? date : formatTime(date);
  return optimizedLibrary.activeSlots[restaurant]?.get(time) || null;
}

function parseDate(value) {
  const match = value.trim().match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) throw new Error('date는 yyyy-MM-dd 형식이어야 합니다.');
  return value.trim();
}

function numberParam(value, fallback) {
  if (value === undefined || value === null || value === '') return fallback;
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return fallback;
  return parsed;
}

function integerParam(value, fallback) {
  if (value === undefined || value === null || value === '') return fallback;
  const parsed = Number(value);
  if (!Number.isInteger(parsed)) return fallback;
  return parsed;
}

function clampWeight(value) {
  return round1(clampBetween(value, 0, 1));
}

function clampBetween(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

function clampUnit(value) {
  return clampBetween(value, 0, 1);
}

function buildSafetyProfile(sampleCount, baseMae, optimizedMae) {
  const gain = baseMae - optimizedMae;
  const sampleFactor = clampUnit((sampleCount - OPTIMIZED_MIN_SAMPLES) / SAFETY_SAMPLE_FULL_EFFECT);
  const gainFactor = clampUnit((gain + 0.2) / SAFETY_GAIN_FULL_EFFECT);
  const minBlendRatio = clampBetween(SAFETY_BLEND_MIN + sampleFactor * 0.15, SAFETY_BLEND_MIN, 0.9);

  return {
    gain: round3(gain),
    gainFactor: round3(gainFactor),
    sampleFactor: round3(sampleFactor),
    minBlendRatio: round3(minBlendRatio)
  };
}

function safetySignalValues(shared) {
  const pressureGap = shared.pressureGap ?? Math.max((shared.targetBaseline ?? 0) - (shared.currentValue ?? 0), 0);
  const baselineShiftPos = shared.baselineShiftPos ?? Math.max(shared.baselineShiftSigned ?? 0, 0);
  const gapPos = shared.gapPos ?? Math.max(shared.currentGap ?? 0, 0);

  return {
    pressureGap,
    baselineShiftPos,
    gapPos
  };
}

function positiveAdjustmentCap(shared) {
  const { pressureGap, baselineShiftPos, gapPos } = safetySignalValues(shared);
  return SAFETY_POSITIVE_CAP_BASE
    + pressureGap * SAFETY_POSITIVE_CAP_PRESSURE_WEIGHT
    + baselineShiftPos * SAFETY_POSITIVE_CAP_SHIFT_WEIGHT
    + gapPos * SAFETY_POSITIVE_CAP_GAP_WEIGHT;
}

function negativeAdjustmentCap(shared) {
  const { gapPos } = safetySignalValues(shared);
  return SAFETY_NEGATIVE_CAP_BASE
    + Math.abs(shared.currentGap ?? 0) * SAFETY_NEGATIVE_CAP_GAP_WEIGHT
    + gapPos * SAFETY_NEGATIVE_CAP_EXCESS_WEIGHT;
}

function applyPredictionSafetyGuard(basePredictionRaw, formulaRawPrediction, shared, safetyProfile) {
  if (!safetyProfile) {
    return {
      applied: false,
      blendRatio: 1,
      signalFactor: 1,
      reliability: 1,
      positiveCap: round3(positiveAdjustmentCap(shared)),
      negativeCap: round3(negativeAdjustmentCap(shared)),
      formulaAdjustment: round3(formulaRawPrediction - basePredictionRaw),
      finalAdjustment: round3(formulaRawPrediction - basePredictionRaw),
      rawPrediction: round3(formulaRawPrediction),
      reasons: []
    };
  }

  const { pressureGap, baselineShiftPos, gapPos } = safetySignalValues(shared);
  const signalScore = pressureGap + baselineShiftPos + gapPos;
  const signalFactor = clampUnit(signalScore / SAFETY_SIGNAL_FULL_EFFECT);
  const reliability = clampUnit(
    safetyProfile.gainFactor * 0.55
    + safetyProfile.sampleFactor * 0.2
    + signalFactor * 0.25
  );
  const blendRatio = safetyProfile.minBlendRatio + (1 - safetyProfile.minBlendRatio) * reliability;
  const formulaAdjustment = formulaRawPrediction - basePredictionRaw;
  const blendedAdjustment = formulaAdjustment * blendRatio;
  const positiveCap = positiveAdjustmentCap(shared);
  const negativeCap = negativeAdjustmentCap(shared);
  const finalAdjustment = clampBetween(blendedAdjustment, -negativeCap, positiveCap);
  const rawPrediction = basePredictionRaw + finalAdjustment;
  const reasons = [];

  if (blendRatio < 0.995) reasons.push('blend');
  if (Math.abs(finalAdjustment - blendedAdjustment) > 0.05) reasons.push('cap');

  return {
    applied: reasons.length > 0,
    blendRatio: round3(blendRatio),
    signalFactor: round3(signalFactor),
    reliability: round3(reliability),
    positiveCap: round3(positiveCap),
    negativeCap: round3(negativeCap),
    formulaAdjustment: round3(formulaAdjustment),
    finalAdjustment: round3(finalAdjustment),
    rawPrediction: round3(rawPrediction),
    reasons
  };
}

async function findLatestConnected(restaurant, time) {
  const optimizedLibrary = await getOptimizedLibrary();
  const activeSlotMap = optimizedLibrary.activeSlots[restaurant] || new Map();
  const start = addMinutes(time, -12 * 60);
  const [rows] = await queryWithRetry(
    'select time, connected from connection where restaurant = ? and time >= ? and time <= ? order by time',
    [restaurant, formatDateTime(start), formatDateTime(time)]
  );
  const recentRows = filterRowsByActiveSlots(filterQualityRows(rawRowsToSeries(rows)).rows, activeSlotMap);
  const latestValid = recentRows[recentRows.length - 1];

  if (latestValid) return latestValid.connected;
  if (!rows.length) {
    throw new Error(`해당 시각 이전의 실제 데이터가 없습니다: ${restaurant}, ${formatDateTime(time)}`);
  }

  return Number(rows[rows.length - 1].connected);
}

async function loadRecentRows(restaurant, time, minutes) {
  const optimizedLibrary = await getOptimizedLibrary();
  const activeSlotMap = optimizedLibrary.activeSlots[restaurant] || new Map();
  const start = addMinutes(time, -minutes);
  const [rows] = await queryWithRetry(
    'select time, connected from connection where restaurant = ? and time >= ? and time <= ? order by time',
    [restaurant, formatDateTime(start), formatDateTime(time)]
  );

  return filterRowsByActiveSlots(filterQualityRows(rawRowsToSeries(rows)).rows, activeSlotMap);
}

function recentTrendFromRecentRows(rows, currentTime, minutes, currentValue) {
  const cutoff = addMinutes(currentTime, -minutes);
  for (let index = rows.length - 1; index >= 0; index -= 1) {
    if (rows[index].date <= cutoff) return currentValue - rows[index].connected;
  }
  return 0;
}

async function baseline(restaurant, time, optimizedLibrary = null) {
  const library = optimizedLibrary || await getOptimizedLibrary(DEFAULT_PROFILE);
  const window = baselineWindow(library);
  const rows = library.cleanRowsByRestaurant[restaurant] || [];
  const activeSlotMap = library.activeSlots[restaurant] || new Map();
  const timeOfDay = formatTime(time);
  const dayOfWeek = mysqlDayOfWeek(time);

  if (restaurant !== '학생생활관') {
    const seasonAverage = averageConnectedFromRows(rows, activeSlotMap, timeOfDay, dayOfWeek, seasonMonths(time.getMonth() + 1), window);
    if (seasonAverage !== null) return seasonAverage;
  }

  const allYearAverage = averageConnectedFromRows(rows, activeSlotMap, timeOfDay, dayOfWeek, null, window);
  if (allYearAverage !== null) return allYearAverage;

  return averageConnectedFromRows(rows, activeSlotMap, timeOfDay, null, null, window) ?? 0;
}

function averageConnectedFromRows(rows, activeSlotMap, timeOfDay, dayOfWeek, months, window) {
  if (activeSlotMap && !activeSlotMap.get(timeOfDay)?.active) return null;

  let sum = 0;
  let count = 0;
  const monthSet = months && months.length ? new Set(months) : null;

  for (const row of rows) {
    if (row.date < window.startDate || row.date >= window.endDate) continue;
    if (formatTime(row.date) !== timeOfDay) continue;
    if (dayOfWeek !== null && mysqlDayOfWeek(row.date) !== dayOfWeek) continue;
    if (monthSet && !monthSet.has(row.date.getMonth() + 1)) continue;
    sum += row.connected;
    count += 1;
  }

  return count ? sum / count : null;
}

async function loadActualDay(restaurant, date, optimizedLibrary = null) {
  const library = optimizedLibrary || await getOptimizedLibrary(DEFAULT_PROFILE);
  const activeSlotMap = library.activeSlots[restaurant] || new Map();
  const start = parseDateTime(`${date} 00:00:00`);
  const end = addDays(start, 1);
  const [rows] = await queryWithRetry(
    'select time, connected from connection where restaurant = ? and time >= ? and time < ? order by time',
    [restaurant, formatDateTime(start), formatDateTime(end)]
  );
  const rawRows = rawRowsToSeries(rows);
  const qualityFiltered = filterQualityRows(rawRows);
  const activeRows = filterRowsByActiveSlots(qualityFiltered.rows, activeSlotMap);

  return {
    rows: activeRows,
    quality: {
      rawCount: rawRows.length,
      qualityRemovedCount: qualityFiltered.removedCount,
      inactiveSlotRemovedCount: qualityFiltered.rows.length - activeRows.length,
      keptCount: activeRows.length,
      removedReasons: qualityFiltered.removedReasons
    }
  };
}

async function loadBaselineMap(restaurant, referenceDate, seasonOnly, optimizedLibrary = null) {
  const library = optimizedLibrary || await getOptimizedLibrary(DEFAULT_PROFILE);
  const window = baselineWindow(library);
  const rows = library.cleanRowsByRestaurant[restaurant] || [];
  const activeSlotMap = library.activeSlots[restaurant] || new Map();
  const targetDayOfWeek = mysqlDayOfWeek(referenceDate);
  const monthSet = seasonOnly ? new Set(seasonMonths(referenceDate.getMonth() + 1)) : null;
  const aggregates = new Map();

  for (const row of rows) {
    if (row.date < window.startDate || row.date >= window.endDate) continue;
    if (mysqlDayOfWeek(row.date) !== targetDayOfWeek) continue;
    const timeOfDay = formatTime(row.date);
    if (!activeSlotMap.get(timeOfDay)?.active) continue;
    if (monthSet && !monthSet.has(row.date.getMonth() + 1)) continue;

    const current = aggregates.get(timeOfDay) || { sum: 0, count: 0 };
    current.sum += row.connected;
    current.count += 1;
    aggregates.set(timeOfDay, current);
  }

  return new Map(Array.from(aggregates.entries()).map(([timeOfDay, item]) => [timeOfDay, item.sum / item.count]));
}

function baselineFromMaps(restaurant, date, baselineMaps) {
  const time = formatTime(date);
  if (restaurant !== '학생생활관' && baselineMaps.season.has(time)) return baselineMaps.season.get(time);
  if (baselineMaps.allYear.has(time)) return baselineMaps.allYear.get(time);
  return 0;
}

function baselineWindow(optimizedLibrary) {
  return optimizedLibrary.tuningWindow;
}

function trainingBaselineLabel(optimizedLibrary) {
  return `튜닝 범위 전체 (${tuningWindowLabel(optimizedLibrary.summary.tuningWindow)})`;
}

function buildSharedPredictionState({ time, targetDate, horizonMinutes, currentValue, actual, currentBaseline, targetBaseline, trend15, trend30, targetBand }) {
  const currentGap = currentValue - currentBaseline;
  return {
    time: formatTime(time),
    targetTime: formatTime(targetDate),
    horizonMinutes,
    currentValue,
    actual,
    currentBaseline,
    targetBaseline,
    currentGap,
    gapPos: Math.max(currentGap, 0),
    gapNeg: Math.max(-currentGap, 0),
    trend15,
    trend30,
    baselineShiftSigned: targetBaseline - currentBaseline,
    baselineShiftAbs: Math.abs(targetBaseline - currentBaseline),
    targetBand
  };
}

function buildPredictionRow(modelKey, restaurant, shared, weight, optimizedLibrary, formulaStrategy) {
  const basePredictionRaw = shared.targetBaseline + shared.currentGap * weight;
  let rawPrediction = basePredictionRaw;
  let formulaRawPrediction = basePredictionRaw;
  let optimizedModelType = FORMULA_BASE;
  let rule = ruleLabelBase(restaurant, weight);
  let coefficients = null;
  let featureValues = null;
  let contributions = null;
  let safetyGuard = null;
  let modelSampleCount = null;
  let bandBaseMae = null;
  let bandOptimizedMae = null;
  let formulaKey = FORMULA_BASE;
  let formulaLabel = '기본식';
  let formulaShortLabel = 'base';
  let peakUnderPenalty = 0;
  let basePeakUnder = null;
  let optimizedPeakUnder = null;

  if (modelKey === MODEL_RESIDUAL) {
    const spec = residualRuleFor(optimizedLibrary, restaurant, shared.horizonMinutes);
    optimizedModelType = spec.mode;
    modelSampleCount = spec.sampleCount;
    bandBaseMae = spec.baseMae;
    bandOptimizedMae = spec.optimizedMae;
    formulaKey = spec.formulaKey;
    formulaLabel = spec.formulaLabel;
    formulaShortLabel = spec.formulaShortLabel;
    peakUnderPenalty = spec.penaltyWeight;
    basePeakUnder = spec.basePeakUnder;
    optimizedPeakUnder = spec.optimizedPeakUnder;
    rule = ruleLabelResidual(spec);

    if (spec.formulaKey !== FORMULA_BASE && Array.isArray(spec.coefficients)) {
      featureValues = featureValueMapForKeys(shared, spec.featureKeys);
      const residualAdjustment = predictWithCoefficients(spec.coefficients, spec.featureKeys, featureValues);
      formulaRawPrediction = basePredictionRaw + residualAdjustment;
      rawPrediction = formulaRawPrediction;
      coefficients = coefficientMap(spec.featureKeys, spec.coefficients);
      contributions = contributionMap(spec.featureKeys, spec.coefficients, featureValues);
    }
  } else if (modelKey === MODEL_ADJUSTED) {
    const spec = optimizedRuleForStrategy(
      optimizedLibrary,
      restaurant,
      shared.horizonMinutes,
      shared.targetBand.key,
      formulaStrategy
    );
    optimizedModelType = spec.mode;
    modelSampleCount = spec.sampleCount;
    bandBaseMae = spec.baseMae;
    bandOptimizedMae = spec.optimizedMae;
    formulaKey = spec.formulaKey;
    formulaLabel = spec.formulaLabel;
    formulaShortLabel = spec.formulaShortLabel;
    peakUnderPenalty = spec.penaltyWeight;
    basePeakUnder = spec.basePeakUnder;
    optimizedPeakUnder = spec.optimizedPeakUnder;
    rule = ruleLabelAdjusted(spec);

    if (spec.formulaKey !== FORMULA_BASE) {
      featureValues = formulaFeatureValueMap(shared, spec.formulaKey);
      formulaRawPrediction = predictWithCoefficients(spec.coefficients, spec.featureKeys, featureValues);
      safetyGuard = applyPredictionSafetyGuard(
        basePredictionRaw,
        formulaRawPrediction,
        shared,
        spec.safetyProfile
      );
      rawPrediction = safetyGuard.rawPrediction;
      coefficients = coefficientMap(spec.featureKeys, spec.coefficients);
      contributions = contributionMap(spec.featureKeys, spec.coefficients, featureValues);
    }
  }

  const predicted = Math.max(0, Math.round(rawPrediction));
  const signedError = shared.actual === null ? null : predicted - shared.actual;
  const error = shared.actual === null ? null : Math.abs(predicted - shared.actual);

  return {
    time: shared.time,
    targetTime: shared.targetTime,
    targetBandKey: shared.targetBand.key,
    targetBandLabel: shared.targetBand.label,
    currentValue: shared.currentValue,
    actual: shared.actual,
    predicted,
    signedError,
    error,
    weight: round2(weight),
    currentBaseline: round1(shared.currentBaseline),
    targetBaseline: round1(shared.targetBaseline),
    currentGap: round1(shared.currentGap),
    gapPos: round1(shared.gapPos),
    gapNeg: round1(shared.gapNeg),
    trend15: round1(shared.trend15),
    trend30: round1(shared.trend30),
    baselineShiftSigned: round1(shared.baselineShiftSigned),
    baselineShiftAbs: round1(shared.baselineShiftAbs),
    basePrediction: round1(basePredictionRaw),
    formulaRawPrediction: round1(formulaRawPrediction),
    formulaAdjustmentTotal: round1(formulaRawPrediction - basePredictionRaw),
    rawPrediction: round1(rawPrediction),
    adjustmentTotal: round1(rawPrediction - basePredictionRaw),
    optimizedModelType,
    optimizedModelLabel: formulaLabel,
    optimizedFormulaKey: formulaKey,
    optimizedFormulaLabel: formulaLabel,
    optimizedFormulaShortLabel: formulaShortLabel,
    rule,
    modelSampleCount,
    bandBaseMae,
    bandOptimizedMae,
    peakUnderPenalty,
    basePeakUnder,
    optimizedPeakUnder,
    coefficients,
    featureValues,
    contributions,
    safetyGuard: safetyGuard
      ? {
          ...safetyGuard,
          basePrediction: round1(basePredictionRaw),
          formulaRawPrediction: round1(formulaRawPrediction),
          rawPrediction: round1(rawPrediction)
        }
      : null
  };
}

function serializeModelResult(key, label, rows, summaryOnly, rowsLimit, rule) {
  const analysis = analyzeRows(rows);
  const visibleRows = summaryOnly ? [] : limitRows(rows, rowsLimit);

  return {
    key,
    label,
    rule,
    mae: analysis.mae,
    within10Rate: analysis.within10Rate,
    count: analysis.count,
    bias: analysis.bias,
    underPredictRate: analysis.underPredictRate,
    overPredictRate: analysis.overPredictRate,
    averageAdjustment: analysis.averageAdjustment,
    regressionCount: analysis.regressionCount,
    baseFallbackCount: analysis.baseFallbackCount,
    regressionRate: analysis.regressionRate,
    largestErrors: analysis.largestErrors,
    timeBands: analysis.timeBands,
    extremes: analysis.extremes,
    returnedRowCount: visibleRows.length,
    isSampled: !summaryOnly && visibleRows.length < rows.length,
    rows: visibleRows
  };
}

function analyzeRows(rows) {
  if (!rows.length) {
    return {
      mae: 0,
      within10Rate: 0,
      count: 0,
      bias: 0,
      underPredictRate: 0,
      overPredictRate: 0,
      averageAdjustment: 0,
      regressionCount: 0,
      baseFallbackCount: 0,
      regressionRate: 0,
      largestErrors: [],
      timeBands: [],
      extremes: {
        worstUnder: null,
        worstOver: null
      }
    };
  }

  const bias = round1(rows.reduce((sum, row) => sum + row.signedError, 0) / rows.length);
  const underCount = rows.filter((row) => row.signedError < 0).length;
  const overCount = rows.filter((row) => row.signedError > 0).length;
  const regressionCount = rows.filter((row) => row.optimizedFormulaKey && row.optimizedFormulaKey !== FORMULA_BASE).length;
  const baseFallbackCount = rows.filter((row) => !row.optimizedFormulaKey || row.optimizedFormulaKey === FORMULA_BASE).length;

  return {
    mae: mae(rows),
    within10Rate: withinRate(rows, 10),
    count: rows.length,
    bias,
    underPredictRate: round1((underCount * 100) / rows.length),
    overPredictRate: round1((overCount * 100) / rows.length),
    averageAdjustment: round1(rows.reduce((sum, row) => sum + (row.adjustmentTotal || 0), 0) / rows.length),
    regressionCount,
    baseFallbackCount,
    regressionRate: round1((regressionCount * 100) / rows.length),
    largestErrors: rows
      .slice()
      .sort((left, right) => right.error - left.error || left.time.localeCompare(right.time))
      .slice(0, DETAIL_TOP_ERRORS_LIMIT),
    timeBands: summarizeTimeBands(rows),
    extremes: {
      worstUnder: pickExtremeRow(rows, (row) => row.signedError < 0, (left, right) => left.signedError - right.signedError),
      worstOver: pickExtremeRow(rows, (row) => row.signedError > 0, (left, right) => right.signedError - left.signedError)
    }
  };
}

function compareModelPair(leftKey, leftModel, rightKey, rightModel) {
  const maeDelta = round1(rightModel.mae - leftModel.mae);
  const within10RateDelta = round1(rightModel.within10Rate - leftModel.within10Rate);
  const countDelta = rightModel.count - leftModel.count;

  let winner = 'tie';
  if (maeDelta < -0.05) winner = rightKey;
  else if (maeDelta > 0.05) winner = leftKey;

  return {
    leftKey,
    rightKey,
    winner,
    maeDelta,
    within10RateDelta,
    countDelta
  };
}

function compareModels(baseModel, adjustedModel) {
  return compareModelPair(MODEL_BASE, baseModel, MODEL_ADJUSTED, adjustedModel);
}

function pickBestModelKey(models) {
  return Object.entries(models)
    .slice()
    .sort((left, right) => left[1].mae - right[1].mae || right[1].within10Rate - left[1].within10Rate)[0]?.[0] || MODEL_BASE;
}

function buildComparisonSummary(horizons) {
  const adjustedWins = horizons.filter((item) => item.comparison.winner === MODEL_ADJUSTED).length;
  const baseWins = horizons.filter((item) => item.comparison.winner === MODEL_BASE).length;
  const ties = horizons.length - adjustedWins - baseWins;
  const biggestGain = horizons
    .slice()
    .sort((left, right) => left.comparison.maeDelta - right.comparison.maeDelta)[0];
  const biggestLoss = horizons
    .slice()
    .sort((left, right) => right.comparison.maeDelta - left.comparison.maeDelta)[0];

  return {
    adjustedWins,
    baseWins,
    ties,
    preferredModel: adjustedWins > baseWins ? MODEL_ADJUSTED : MODEL_BASE,
    biggestGain: biggestGain && biggestGain.comparison.maeDelta < 0
      ? { horizonMinutes: biggestGain.horizonMinutes, maeDelta: biggestGain.comparison.maeDelta }
      : null,
    biggestLoss: biggestLoss && biggestLoss.comparison.maeDelta > 0
      ? { horizonMinutes: biggestLoss.horizonMinutes, maeDelta: biggestLoss.comparison.maeDelta }
      : null
  };
}

function buildTripleComparisonSummary(horizons) {
  const winCounts = {
    [MODEL_BASE]: 0,
    [MODEL_ADJUSTED]: 0,
    [MODEL_RESIDUAL]: 0
  };
  let adjustedWins = 0;
  let residualWins = 0;
  let adjustedResidualTies = 0;

  for (const horizon of horizons) {
    if (winCounts[horizon.bestModelKey] !== undefined) {
      winCounts[horizon.bestModelKey] += 1;
    }

    const headToHeadWinner = horizon.comparisons.residualVsAdjusted.winner;
    if (headToHeadWinner === MODEL_ADJUSTED) adjustedWins += 1;
    else if (headToHeadWinner === MODEL_RESIDUAL) residualWins += 1;
    else adjustedResidualTies += 1;
  }

  const biggestResidualGain = horizons
    .slice()
    .sort((left, right) => left.comparisons.residualVsAdjusted.maeDelta - right.comparisons.residualVsAdjusted.maeDelta)[0];
  const biggestResidualLoss = horizons
    .slice()
    .sort((left, right) => right.comparisons.residualVsAdjusted.maeDelta - left.comparisons.residualVsAdjusted.maeDelta)[0];
  const preferredModel = Object.entries(winCounts)
    .sort((left, right) => right[1] - left[1])[0]?.[0] || MODEL_BASE;

  return {
    winCounts,
    preferredModel,
    headToHeadAdjustedVsResidual: {
      adjustedWins,
      residualWins,
      ties: adjustedResidualTies,
      preferredModel: residualWins > adjustedWins ? MODEL_RESIDUAL : MODEL_ADJUSTED
    },
    biggestResidualGain: biggestResidualGain && biggestResidualGain.comparisons.residualVsAdjusted.maeDelta < 0
      ? {
          horizonMinutes: biggestResidualGain.horizonMinutes,
          maeDelta: biggestResidualGain.comparisons.residualVsAdjusted.maeDelta
        }
      : null,
    biggestResidualLoss: biggestResidualLoss && biggestResidualLoss.comparisons.residualVsAdjusted.maeDelta > 0
      ? {
          horizonMinutes: biggestResidualLoss.horizonMinutes,
          maeDelta: biggestResidualLoss.comparisons.residualVsAdjusted.maeDelta
        }
      : null
  };
}

function buildModelFeaturePayload(featureKeys, coefficients) {
  return (featureKeys || []).map((key, index) => ({
    ...featureDocPayload(key),
    coefficient: round3(coefficients?.[index] || 0)
  }));
}

function buildAdjustedMethodPayload(optimizedLibrary, restaurant, horizonMinutes) {
  const spec = optimizedUnifiedRuleFor(optimizedLibrary, restaurant, horizonMinutes);
  return {
    key: MODEL_ADJUSTED,
    label: '현재 운영식',
    formulaKey: spec.formulaKey,
    formulaLabel: spec.formulaLabel,
    formula: formulaMeta(spec.formulaKey).formula,
    note: `${formulaMeta(spec.formulaKey).note} 안전장치를 함께 적용합니다.`,
    featureCount: spec.featureKeys?.length || 0,
    sampleCount: spec.sampleCount,
    baseMae: spec.baseMae,
    optimizedMae: spec.optimizedMae,
    safetyApplied: Boolean(spec.safetyProfile),
    features: buildModelFeaturePayload(spec.featureKeys, spec.coefficients)
  };
}

function buildSafetyGuardPayload() {
  return {
    type: 'blendAndCap',
    minSamples: OPTIMIZED_MIN_SAMPLES,
    gainFullEffect: SAFETY_GAIN_FULL_EFFECT,
    sampleFullEffect: SAFETY_SAMPLE_FULL_EFFECT,
    signalFullEffect: SAFETY_SIGNAL_FULL_EFFECT,
    reliabilityWeights: {
      gainFactor: 0.55,
      sampleFactor: 0.2,
      signalFactor: 0.25
    },
    formulas: {
      gainFactor: `clamp((gain + 0.2) / ${SAFETY_GAIN_FULL_EFFECT}, 0, 1)`,
      sampleFactor: `clamp((sampleCount - ${OPTIMIZED_MIN_SAMPLES}) / ${SAFETY_SAMPLE_FULL_EFFECT}, 0, 1)`,
      signalFactor: `clamp((pressureGap + baselineShiftPos + gapPos) / ${SAFETY_SIGNAL_FULL_EFFECT}, 0, 1)`,
      minBlendRatio: `clamp(${SAFETY_BLEND_MIN} + sampleFactor × 0.15, ${SAFETY_BLEND_MIN}, 0.90)`,
      reliability: 'gainFactor × 0.55 + sampleFactor × 0.20 + signalFactor × 0.25',
      blendRatio: 'minBlendRatio + (1 - minBlendRatio) × reliability',
      positiveCap: `${SAFETY_POSITIVE_CAP_BASE} + pressureGap × ${SAFETY_POSITIVE_CAP_PRESSURE_WEIGHT} + baselineShiftPos × ${SAFETY_POSITIVE_CAP_SHIFT_WEIGHT} + gapPos × ${SAFETY_POSITIVE_CAP_GAP_WEIGHT}`,
      negativeCap: `${SAFETY_NEGATIVE_CAP_BASE} + |currentGap| × ${SAFETY_NEGATIVE_CAP_GAP_WEIGHT} + gapPos × ${SAFETY_NEGATIVE_CAP_EXCESS_WEIGHT}`
    },
    constants: {
      minBlendBase: SAFETY_BLEND_MIN,
      positiveCapBase: SAFETY_POSITIVE_CAP_BASE,
      positiveCapPressureWeight: SAFETY_POSITIVE_CAP_PRESSURE_WEIGHT,
      positiveCapShiftWeight: SAFETY_POSITIVE_CAP_SHIFT_WEIGHT,
      positiveCapGapWeight: SAFETY_POSITIVE_CAP_GAP_WEIGHT,
      negativeCapBase: SAFETY_NEGATIVE_CAP_BASE,
      negativeCapGapWeight: SAFETY_NEGATIVE_CAP_GAP_WEIGHT,
      negativeCapExcessWeight: SAFETY_NEGATIVE_CAP_EXCESS_WEIGHT
    }
  };
}

function buildFixedModelPayload(optimizedLibrary) {
  const featureKeys = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).featureKeys || [];
  const rows = RESTAURANTS.flatMap((restaurant) => HORIZONS.map((horizonMinutes) => {
    const spec = optimizedUnifiedRuleFor(optimizedLibrary, restaurant, horizonMinutes);
    return {
      restaurant,
      horizonMinutes,
      formulaKey: spec.formulaKey || FORMULA_BASE,
      formulaLabel: spec.formulaLabel || formulaMeta(FORMULA_BASE).label,
      sampleCount: spec.sampleCount || 0,
      baseMae: spec.baseMae || 0,
      optimizedMae: spec.optimizedMae || 0,
      gain: round1((spec.baseMae || 0) - (spec.optimizedMae || 0)),
      coefficients: coefficientMap(spec.featureKeys || featureKeys, spec.coefficients || []),
      safetyProfile: spec.safetyProfile
        ? {
            gain: round3(spec.safetyProfile.gain || 0),
            gainFactor: round3(spec.safetyProfile.gainFactor || 0),
            sampleFactor: round3(spec.safetyProfile.sampleFactor || 0),
            minBlendRatio: round3(spec.safetyProfile.minBlendRatio || 0)
          }
        : null
    };
  }));

  return {
    scope: '식당 × 30/60/90분 고정',
    formulaLabel: formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).label,
    featureKeys,
    rows,
    safetyGuard: buildSafetyGuardPayload()
  };
}

function buildResidualMethodPayload(optimizedLibrary, restaurant, horizonMinutes) {
  const spec = residualRuleFor(optimizedLibrary, restaurant, horizonMinutes);
  return {
    key: MODEL_RESIDUAL,
    label: '제안식',
    formulaKey: spec.formulaKey,
    formulaLabel: spec.formulaLabel,
    formula: formulaMeta(spec.formulaKey).formula,
    note: formulaMeta(spec.formulaKey).note,
    featureCount: spec.featureKeys?.length || 0,
    sampleCount: spec.sampleCount,
    baseMae: spec.baseMae,
    optimizedMae: spec.optimizedMae,
    safetyApplied: false,
    features: buildModelFeaturePayload(spec.featureKeys, spec.coefficients)
  };
}

function buildBaseMethodPayload(weight) {
  return {
    key: MODEL_BASE,
    label: '기본식',
    formulaKey: FORMULA_BASE,
    formulaLabel: '기본식',
    formula: `목표 평소값 + (현재값 - 현재 평소값) × ${round1(weight)}`,
    note: '선택한 기본 반영률만 사용합니다.',
    featureCount: 1,
    sampleCount: null,
    baseMae: null,
    optimizedMae: null,
    safetyApplied: false,
    features: []
  };
}

function mae(rows) {
  if (!rows.length) return 0;
  return round1(rows.reduce((sum, row) => sum + row.error, 0) / rows.length);
}

function withinRate(rows, threshold) {
  if (!rows.length) return 0;
  const count = rows.filter((row) => row.error <= threshold).length;
  return round1((count * 100) / rows.length);
}

function summarizeTimeBands(rows) {
  const bandMap = new Map();

  for (const row of rows) {
    const band = timeBandFor(row.targetTime);
    if (!bandMap.has(band.key)) {
      bandMap.set(band.key, {
        key: band.key,
        label: band.label,
        count: 0,
        errorSum: 0,
        signedErrorSum: 0,
        within10Count: 0
      });
    }

    const summary = bandMap.get(band.key);
    summary.count += 1;
    summary.errorSum += row.error;
    summary.signedErrorSum += row.signedError;
    if (row.error <= 10) summary.within10Count += 1;
  }

  return Array.from(bandMap.values())
    .map((band) => ({
      key: band.key,
      label: band.label,
      count: band.count,
      mae: round1(band.errorSum / band.count),
      bias: round1(band.signedErrorSum / band.count),
      within10Rate: round1((band.within10Count * 100) / band.count)
    }))
    .sort((left, right) => right.mae - left.mae || right.count - left.count);
}

function pickExtremeRow(rows, predicate, compare) {
  const matches = rows.filter(predicate);
  if (!matches.length) return null;
  return matches.slice().sort(compare)[0];
}

function limitRows(rows, limit) {
  if (!limit || rows.length <= limit) return rows;
  if (limit === 1) return [rows[Math.floor(rows.length / 2)]];

  const lastIndex = rows.length - 1;
  const sampled = [];
  for (let index = 0; index < limit; index += 1) {
    sampled.push(rows[Math.round((index * lastIndex) / (limit - 1))]);
  }

  return sampled;
}

function seasonMonths(month) {
  if (month <= 2) return [1, 2];
  if (month <= 6) return [3, 4, 5, 6];
  if (month <= 8) return [7, 8];
  return [9, 10, 11, 12];
}

function seasonLabel(month) {
  if (month <= 2) return '겨울 비수기';
  if (month <= 6) return '1학기';
  if (month <= 8) return '여름 비수기';
  return '2학기';
}

function baselineModeLabel(restaurant) {
  return restaurant === '학생생활관' ? '전 기간 평균' : '시즌성 평균 우선';
}

function ruleLabelBase(restaurant, weight) {
  const baselineLabel = restaurant === '학생생활관' ? '전 기간 평균' : '시즌성 평균';
  if (weight === 0) return `${baselineLabel}만 사용`;
  if (weight >= 0.85) return `${baselineLabel} + 현재 차이를 매우 크게 반영`;
  if (weight >= 0.65) return `${baselineLabel} + 현재 차이를 크게 반영`;
  if (weight >= 0.45) return `${baselineLabel} + 현재 차이를 중간 반영`;
  return `${baselineLabel} + 현재 차이를 약하게 반영`;
}

function optimizedRuleLabel(optimizedLibrary, restaurant, horizonMinutes) {
  const bandSpecs = Object.values(optimizedLibrary.groups[restaurant]?.[horizonMinutes] || {});
  const counts = new Map();
  for (const spec of bandSpecs) {
    const key = spec.formulaKey || FORMULA_BASE;
    counts.set(key, (counts.get(key) || 0) + 1);
  }
  const summary = Array.from(counts.entries())
    .sort((left, right) => right[1] - left[1])
    .map(([key, count]) => `${formulaMeta(key).shortLabel} ${count}개`)
    .join(' / ');

  return summary ? `시간대별 고정 운영식 · ${summary}` : '시간대별 고정 운영식 정보 없음';
}

function ruleLabelAdjusted(spec) {
  const scopeLabel = spec.scopeLabel || spec.bandLabel;
  if (spec.formulaKey !== FORMULA_BASE) {
    const peakNote = spec.penaltyWeight > 0 ? ` · 피크 벌점 ${spec.penaltyWeight}` : '';
    const safetyNote = spec.safetyProfile ? ' · 안전장치 적용' : '';
    return `${scopeLabel}: ${spec.formulaLabel} · 튜닝 ${spec.sampleCount}건 · MAE ${spec.baseMae}→${spec.optimizedMae}${peakNote}${safetyNote}`;
  }

  return `${scopeLabel}는 기본식 유지 · 전체 데이터 튜닝에서 기본식이 가장 안정적이었음`;
}

function ruleLabelResidual(spec) {
  const scopeLabel = spec.scopeLabel || spec.bandLabel;
  if (spec.formulaKey !== FORMULA_BASE) {
    return `${scopeLabel}: 기본식 잔차를 ${spec.formulaLabel}으로 보정 쨌 학습 ${spec.sampleCount}건 쨌 MAE ${spec.baseMae}->${spec.optimizedMae}`;
  }

  return `${scopeLabel}: 기본식 유지`;
}

function mysqlDayOfWeek(date) {
  const day = date.getDay();
  return day === 0 ? 1 : day + 1;
}

function addMinutes(date, minutes) {
  return new Date(date.getTime() + minutes * 60 * 1000);
}

function addDays(date, days) {
  return new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
}

function formatDateTime(date) {
  return `${dateOnly(date)} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function dateOnly(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function formatTime(date) {
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function pad(value) {
  return String(value).padStart(2, '0');
}

function round1(value) {
  return Math.round(value * 10) / 10;
}

function round2(value) {
  return Math.round(value * 100) / 100;
}

function round3(value) {
  return Math.round(value * 1000) / 1000;
}

function freezeWeights(weights) {
  return Object.freeze({
    30: round1(Number(weights[30])),
    60: round1(Number(weights[60])),
    90: round1(Number(weights[90]))
  });
}

function timeBandFor(timeText) {
  const [hours, minutes] = timeText.split(':').map(Number);
  const totalMinutes = hours * 60 + minutes;
  return TIME_BANDS.find((band) => totalMinutes >= band.start && totalMinutes <= band.end) || TIME_BANDS[TIME_BANDS.length - 1];
}

function trainingBaselineForLibrary(restaurant, date, baselineMaps) {
  const dow = mysqlDayOfWeek(date);
  const time = formatTime(date);

  if (restaurant !== '학생생활관') {
    const seasonal = getAverageFromMap(baselineMaps.seasonMap, `${seasonKey(date)}|${dow}|${time}`);
    if (seasonal !== null) return seasonal;
  }

  return getAverageFromMap(baselineMaps.allMap, `${dow}|${time}`) ?? 0;
}

function buildTrainingBaselineMaps(restaurant, rows, activeSlotMap) {
  const allMap = new Map();
  const seasonMap = new Map();

  for (const row of rows) {
    const time = formatTime(row.date);
    if (!activeSlotMap.get(time)?.active) continue;
    const dow = mysqlDayOfWeek(row.date);
    addAverageToMap(allMap, `${dow}|${time}`, row.connected);
    if (restaurant !== '학생생활관') {
      addAverageToMap(seasonMap, `${seasonKey(row.date)}|${dow}|${time}`, row.connected);
    }
  }

  return { allMap, seasonMap };
}

function createWeightCandidateMap() {
  return new Map(WEIGHT_CANDIDATES.map((weight) => [weight, { errorSum: 0, count: 0 }]));
}

function createWeightScorebook() {
  return {
    common: Object.fromEntries(HORIZONS.map((horizonMinutes) => [horizonMinutes, createWeightCandidateMap()])),
    restaurant: Object.fromEntries(
      RESTAURANTS.map((restaurant) => [
        restaurant,
        Object.fromEntries(HORIZONS.map((horizonMinutes) => [horizonMinutes, createWeightCandidateMap()]))
      ])
    )
  };
}

function addWeightCandidateError(map, weight, error) {
  const current = map.get(weight);
  if (!current) return;
  current.errorSum += error;
  current.count += 1;
}

function addSampleToWeightScorebook(scorebook, restaurant, horizonMinutes, targetBaseline, currentGap, actual) {
  for (const weight of WEIGHT_CANDIDATES) {
    const predicted = Math.max(0, Math.round(targetBaseline + currentGap * weight));
    const error = Math.abs(predicted - actual);
    addWeightCandidateError(scorebook.common[horizonMinutes], weight, error);
    addWeightCandidateError(scorebook.restaurant[restaurant][horizonMinutes], weight, error);
  }
}

function pickBestWeight(scoreMap, fallbackWeight) {
  let bestWeight = fallbackWeight;
  let bestMae = Number.POSITIVE_INFINITY;

  for (const [weight, item] of scoreMap.entries()) {
    if (!item.count) continue;
    const mae = item.errorSum / item.count;
    if (
      mae + 1e-9 < bestMae
      || (Math.abs(mae - bestMae) < 1e-9 && Math.abs(weight - fallbackWeight) < Math.abs(bestWeight - fallbackWeight))
    ) {
      bestWeight = weight;
      bestMae = mae;
    }
  }

  return round1(bestWeight);
}

function pickBestWeightSet(commonScores, restaurantScores) {
  return {
    common: freezeWeights({
      30: pickBestWeight(commonScores[30], FALLBACK_COMMON_WEIGHTS[30]),
      60: pickBestWeight(commonScores[60], FALLBACK_COMMON_WEIGHTS[60]),
      90: pickBestWeight(commonScores[90], FALLBACK_COMMON_WEIGHTS[90])
    }),
    restaurant: Object.freeze(Object.fromEntries(
      RESTAURANTS.map((restaurant) => [
        restaurant,
        freezeWeights({
          30: pickBestWeight(restaurantScores[restaurant][30], FALLBACK_RESTAURANT_WEIGHTS[restaurant][30]),
          60: pickBestWeight(restaurantScores[restaurant][60], FALLBACK_RESTAURANT_WEIGHTS[restaurant][60]),
          90: pickBestWeight(restaurantScores[restaurant][90], FALLBACK_RESTAURANT_WEIGHTS[restaurant][90])
        })
      ])
    ))
  };
}

function seasonKey(date) {
  const month = date.getMonth() + 1;
  if (month <= 2) return 'winter';
  if (month <= 6) return 'spring';
  if (month <= 8) return 'summer';
  return 'fall';
}

function addAverageToMap(map, key, value) {
  const current = map.get(key) || { sum: 0, count: 0 };
  current.sum += value;
  current.count += 1;
  map.set(key, current);
}

function getAverageFromMap(map, key) {
  const item = map.get(key);
  if (!item || !item.count) return null;
  return item.sum / item.count;
}

function recentTrendFromRows(rows, currentIndex, currentDate, minutes) {
  const cutoff = addMinutes(currentDate, -minutes);
  for (let index = currentIndex - 1; index >= 0; index -= 1) {
    if (rows[index].date <= cutoff) return rows[currentIndex].connected - rows[index].connected;
  }
  return 0;
}

function formulaMeta(formulaKey) {
  if (formulaKey === FORMULA_BASE) {
    return {
      label: '기본식',
      shortLabel: 'base',
      featureKeys: ['intercept'],
      formula: '목표 평소값 + (현재값 - 현재 시점 평소값) × 반영률',
      note: '현재 차이를 고정 반영률로 유지한다.'
    };
  }

  return FORMULA_FAMILIES[formulaKey] || FORMULA_FAMILIES[FORMULA_PRESSURE_TREND];
}

function sharedDerivedValues(shared) {
  const rise15 = Math.max(shared.trend15, 0);
  const rise30 = Math.max(shared.trend30, 0);
  const fall15 = Math.max(-shared.trend15, 0);
  const fall30 = Math.max(-shared.trend30, 0);
  const baselineShiftPos = Math.max(shared.baselineShiftSigned, 0);
  const pressureGap = Math.max(shared.targetBaseline - shared.currentValue, 0);
  const surge15 = (baselineShiftPos * rise15) / 50;
  const surge30 = (pressureGap * rise30) / 50;

  return {
    intercept: 1,
    currentValue: shared.currentValue,
    targetBaseline: shared.targetBaseline,
    gapPos: shared.gapPos,
    gapNeg: shared.gapNeg,
    baselineShiftSigned: shared.baselineShiftSigned,
    baselineShiftAbs: shared.baselineShiftAbs,
    trend15: shared.trend15,
    trend30: shared.trend30,
    rise15,
    rise30,
    fall15,
    fall30,
    baselineShiftPos,
    pressureGap,
    surge15,
    surge30,
    isHoliday: Number(shared.isHoliday || 0),
    menuDataAvailable: Number(shared.menuDataAvailable || 0),
    menuOpenAny: Number(shared.menuOpenAny || 0),
    menuOpenCount: Number(shared.menuOpenCount || 0),
    menuMenuCount: Number(shared.menuMenuCount || 0),
    menuClosedAll: Number(shared.menuClosedAll || 0)
  };
}

function featureValueMapForKeys(shared, featureKeys) {
  const values = sharedDerivedValues(shared);
  return Object.fromEntries(featureKeys.map((key) => [key, values[key] ?? 0]));
}

function formulaFeatureValueMap(shared, formulaKey) {
  return featureValueMapForKeys(shared, formulaMeta(formulaKey).featureKeys);
}

function featureVector(featureKeys, values) {
  return featureKeys.map((key) => values[key] ?? 0);
}

function coefficientMap(featureKeys, coefficients) {
  return Object.fromEntries(featureKeys.map((key, index) => [key, round3(coefficients[index] || 0)]));
}

function contributionMap(featureKeys, coefficients, values) {
  return Object.fromEntries(
    featureKeys.map((key, index) => [key, round3((coefficients[index] || 0) * (values[key] ?? 0))])
  );
}

function predictWithCoefficients(coefficients, featureKeys, values) {
  const vector = featureVector(featureKeys, values);
  return vector.reduce((sum, value, index) => sum + value * (coefficients[index] || 0), 0);
}

function optimizedRuleFor(optimizedLibrary, restaurant, horizonMinutes, bandKey) {
  const band = TIME_BANDS.find((item) => item.key === bandKey);
  return optimizedLibrary.groups[restaurant]?.[horizonMinutes]?.[bandKey] || {
    mode: 'base',
    formulaKey: FORMULA_BASE,
    formulaLabel: '기본식',
    formulaShortLabel: 'base',
    featureKeys: formulaMeta(FORMULA_BASE).featureKeys,
    sampleCount: 0,
    baseMae: 0,
    optimizedMae: 0,
    basePeakUnder: 0,
    optimizedPeakUnder: 0,
    bandKey,
    bandLabel: band ? band.label : '시간대 정보 없음',
    coefficients: null,
    penaltyWeight: peakScoreWeight(bandKey),
    safetyProfile: null
  };
}

function optimizedUnifiedRuleFor(optimizedLibrary, restaurant, horizonMinutes) {
  return optimizedLibrary.fixedGroups[restaurant]?.[horizonMinutes] || {
    mode: 'base',
    formulaKey: FORMULA_BASE,
    formulaLabel: '기본식',
    formulaShortLabel: 'base',
    featureKeys: formulaMeta(FORMULA_BASE).featureKeys,
    sampleCount: 0,
    baseMae: 0,
    optimizedMae: 0,
    basePeakUnder: 0,
    optimizedPeakUnder: 0,
    bandKey: 'all',
    bandLabel: `${horizonMinutes}분 전체 시간대`,
    scopeLabel: `${horizonMinutes}분 식당 고정식`,
    strategyKey: STRATEGY_RESTAURANT_HORIZON,
    coefficients: null,
    penaltyWeight: 0,
    safetyProfile: null
  };
}

function residualRuleFor(optimizedLibrary, restaurant, horizonMinutes) {
  return optimizedLibrary.residualGroups?.[restaurant]?.[horizonMinutes] || {
    mode: 'base',
    formulaKey: FORMULA_BASE,
    formulaLabel: '기본식',
    formulaShortLabel: 'base',
    featureKeys: [],
    sampleCount: 0,
    baseMae: 0,
    optimizedMae: 0,
    basePeakUnder: 0,
    optimizedPeakUnder: 0,
    scopeLabel: `${restaurant} ${horizonMinutes}분`,
    coefficients: null,
    penaltyWeight: 0,
    safetyProfile: null
  };
}

function optimizedRuleForStrategy(optimizedLibrary, restaurant, horizonMinutes, bandKey, formulaStrategy) {
  if (formulaStrategy === STRATEGY_RESTAURANT_HORIZON) {
    return optimizedUnifiedRuleFor(optimizedLibrary, restaurant, horizonMinutes);
  }
  return optimizedRuleFor(optimizedLibrary, restaurant, horizonMinutes, bandKey);
}

function chooseBestCandidate(samples, scorer, basePredictor, baseRawPredictor) {
  const baseStats = scorer(samples, basePredictor);
  const family = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY);
  const coefficients = fitFormulaCoefficients(samples, FORCED_OPTIMIZED_FORMULA_KEY, family.lambda);
  const rawPredictor = (sample) => predictWithCoefficients(
    coefficients,
    family.featureKeys,
    formulaFeatureValueMap(sample, FORCED_OPTIMIZED_FORMULA_KEY)
  );
  const unguardedStats = scorer(
    samples,
    (sample) => Math.max(0, Math.round(rawPredictor(sample)))
  );
  const safetyProfile = buildSafetyProfile(samples.length, baseStats.mae, unguardedStats.mae);
  const stats = scorer(
    samples,
    (sample) => Math.max(0, Math.round(applyPredictionSafetyGuard(
      baseRawPredictor(sample),
      rawPredictor(sample),
      sample,
      safetyProfile
    ).rawPrediction))
  );

  const bestCandidate = {
    formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
    formulaLabel: family.label,
    formulaShortLabel: family.shortLabel,
    featureKeys: family.featureKeys,
    coefficients: coefficients.map(round3),
    stats,
    safetyProfile
  };

  return {
    baseStats,
    bestCandidate
  };
}

function buildBandedSpec(restaurant, horizonMinutes, bandKey, samples, baseWeight) {
  const band = TIME_BANDS.find((item) => item.key === bandKey);
  const scorer = (candidateSamples, predictor) => scoreCandidateSamples(candidateSamples, predictor, bandKey);
  const baseRawPredictor = (sample) => sample.targetBaseline + sample.currentGap * baseWeight;
  const { baseStats, bestCandidate } = chooseBestCandidate(
    samples,
    scorer,
    (sample) => Math.max(0, Math.round(baseRawPredictor(sample))),
    baseRawPredictor
  );

  return {
    mode: 'formula',
    formulaKey: bestCandidate.formulaKey,
    formulaLabel: bestCandidate.formulaLabel,
    formulaShortLabel: bestCandidate.formulaShortLabel,
    featureKeys: bestCandidate.featureKeys,
    bandKey,
    bandLabel: band ? band.label : '시간대 정보 없음',
    scopeLabel: `${band ? band.label : '시간대'} 고정식`,
    strategyKey: STRATEGY_BANDED,
    sampleCount: samples.length,
    baseMae: round1(baseStats.mae),
    optimizedMae: round1(bestCandidate.stats.mae),
    basePeakUnder: round1(baseStats.peakUnder),
    optimizedPeakUnder: round1(bestCandidate.stats.peakUnder),
    baseScore: round2(baseStats.score),
    optimizedScore: round2(bestCandidate.stats.score),
    penaltyWeight: peakScoreWeight(bandKey),
    coefficients: bestCandidate.coefficients,
    safetyProfile: bestCandidate.safetyProfile
  };
}

function buildUnifiedSpec(restaurant, horizonMinutes, samples, baseWeight) {
  const baseRawPredictor = (sample) => sample.targetBaseline + sample.currentGap * baseWeight;
  const { baseStats, bestCandidate } = chooseBestCandidate(
    samples,
    scoreCandidateSamplesMixed,
    (sample) => Math.max(0, Math.round(baseRawPredictor(sample))),
    baseRawPredictor
  );

  return {
    mode: 'formula',
    formulaKey: bestCandidate.formulaKey,
    formulaLabel: bestCandidate.formulaLabel,
    formulaShortLabel: bestCandidate.formulaShortLabel,
    featureKeys: bestCandidate.featureKeys,
    bandKey: 'all',
    bandLabel: '전체 시간대',
    scopeLabel: `${restaurant} ${horizonMinutes}분 고정식`,
    strategyKey: STRATEGY_RESTAURANT_HORIZON,
    sampleCount: samples.length,
    baseMae: round1(baseStats.mae),
    optimizedMae: round1(bestCandidate.stats.mae),
    basePeakUnder: round1(baseStats.peakPenalty || 0),
    optimizedPeakUnder: round1(bestCandidate.stats.peakPenalty || 0),
    baseScore: round2(baseStats.score),
    optimizedScore: round2(bestCandidate.stats.score),
    penaltyWeight: 0,
    coefficients: bestCandidate.coefficients,
    safetyProfile: bestCandidate.safetyProfile
  };
}

function chooseBestResidualCandidate(samples, baseWeight) {
  const baseRawPredictor = (sample) => sample.targetBaseline + sample.currentGap * baseWeight;
  const family = formulaMeta(FORMULA_BASE_RESIDUAL);
  const coefficients = fitResidualFormulaCoefficients(
    samples,
    FORMULA_BASE_RESIDUAL,
    family.lambda,
    baseWeight
  );
  const baseStats = scoreCandidateSamplesMixed(
    samples,
    (sample) => Math.max(0, Math.round(baseRawPredictor(sample)))
  );
  const stats = scoreCandidateSamplesMixed(
    samples,
    (sample) => {
      const adjustment = predictWithCoefficients(
        coefficients,
        family.featureKeys,
        featureValueMapForKeys(sample, family.featureKeys)
      );
      return Math.max(0, Math.round(baseRawPredictor(sample) + adjustment));
    }
  );

  return {
    baseStats,
    bestCandidate: {
      formulaKey: FORMULA_BASE_RESIDUAL,
      formulaLabel: family.label,
      formulaShortLabel: family.shortLabel,
      featureKeys: family.featureKeys,
      coefficients: coefficients.map(round3),
      stats,
      safetyProfile: null
    }
  };
}

function buildResidualSpec(restaurant, horizonMinutes, samples, baseWeight) {
  const { baseStats, bestCandidate } = chooseBestResidualCandidate(samples, baseWeight);

  return {
    mode: 'residual',
    formulaKey: bestCandidate.formulaKey,
    formulaLabel: bestCandidate.formulaLabel,
    formulaShortLabel: bestCandidate.formulaShortLabel,
    featureKeys: bestCandidate.featureKeys,
    bandKey: 'all',
    bandLabel: `${horizonMinutes}분 전체 시간대`,
    scopeLabel: `${restaurant} ${horizonMinutes}분 잔차 보정`,
    strategyKey: STRATEGY_RESTAURANT_HORIZON,
    sampleCount: samples.length,
    baseMae: round1(baseStats.mae),
    optimizedMae: round1(bestCandidate.stats.mae),
    basePeakUnder: round1(baseStats.peakPenalty || 0),
    optimizedPeakUnder: round1(bestCandidate.stats.peakPenalty || 0),
    baseScore: round2(baseStats.score),
    optimizedScore: round2(bestCandidate.stats.score),
    penaltyWeight: 0,
    coefficients: bestCandidate.coefficients,
    safetyProfile: null
  };
}

function buildBandedStrategyRows(groups) {
  return RESTAURANTS.flatMap((restaurant) => HORIZONS.map((horizonMinutes) => ({
    restaurant,
    horizonMinutes,
    bands: TIME_BANDS.map((band) => {
      const spec = groups[restaurant]?.[horizonMinutes]?.[band.key];
      return {
        key: band.key,
        label: band.label,
        formulaKey: spec?.formulaKey || FORMULA_BASE,
        formulaLabel: spec?.formulaLabel || '기본식',
        formulaShortLabel: spec?.formulaShortLabel || 'base',
        sampleCount: spec?.sampleCount || 0,
        baseMae: spec?.baseMae || 0,
        optimizedMae: spec?.optimizedMae || 0,
        gain: round1((spec?.baseMae || 0) - (spec?.optimizedMae || 0))
      };
    })
  })));
}

function buildUnifiedStrategyRows(groups) {
  return RESTAURANTS.flatMap((restaurant) => HORIZONS.map((horizonMinutes) => {
    const spec = groups[restaurant]?.[horizonMinutes];
    return {
      restaurant,
      horizonMinutes,
      formulaKey: spec?.formulaKey || FORMULA_BASE,
      formulaLabel: spec?.formulaLabel || '기본식',
      formulaShortLabel: spec?.formulaShortLabel || 'base',
      sampleCount: spec?.sampleCount || 0,
      baseMae: spec?.baseMae || 0,
      optimizedMae: spec?.optimizedMae || 0,
      gain: round1((spec?.baseMae || 0) - (spec?.optimizedMae || 0))
    };
  }));
}

function getOptimizedLibrary(profile = DEFAULT_PROFILE) {
  const resolvedProfile = parseProfile(profile);
  if (!optimizedLibraryPromiseByProfile.has(resolvedProfile)) {
    optimizedLibraryPromiseByProfile.set(
      resolvedProfile,
      trainOptimizedLibrary(LIBRARY_PROFILES[resolvedProfile] || LIBRARY_PROFILES[DEFAULT_PROFILE])
    );
  }
  return optimizedLibraryPromiseByProfile.get(resolvedProfile);
}

async function trainOptimizedLibrary(profileConfig) {
  const trainingDateExclusive = dateOnly(parseDateTime(profileConfig.trainingEndExclusive));
  const [[rows], [holidayRows], [menuRows]] = await Promise.all([
    queryWithRetry(
      'select restaurant, time, connected from connection where time < ? order by restaurant, time',
      [profileConfig.trainingEndExclusive]
    ),
    queryWithRetry(
      'select date, is_holiday from holiday where date < ? order by date',
      [trainingDateExclusive]
    ),
    queryWithRetry(
      `select restaurant, date, count(*) as menuCount,
              sum(case when is_open then 1 else 0 end) as openCount
         from menu
        where date < ?
        group by restaurant, date
        order by restaurant, date`,
      [trainingDateExclusive]
    )
  ]);
  const holidayMap = buildHolidayMap(holidayRows);
  const menuFeatureMap = buildMenuFeatureMap(menuRows);

  const rawByRestaurant = new Map(RESTAURANTS.map((restaurant) => [restaurant, []]));
  let observedStart = null;
  let observedEnd = null;
  for (const row of rows) {
    const restaurant = String(row.restaurant);
    if (!rawByRestaurant.has(restaurant)) continue;
    const date = normalizeSqlDate(row.time);
    rawByRestaurant.get(restaurant).push({
      date,
      connected: Number(row.connected)
    });
    if (!observedStart || date < observedStart) observedStart = new Date(date.getTime());
    if (!observedEnd || date > observedEnd) observedEnd = new Date(date.getTime());
  }

  if (!observedStart || !observedEnd) {
    throw new Error('튜닝에 사용할 혼잡도 데이터가 없습니다.');
  }

  const cleanRowsByRestaurant = {};
  const activeSlots = {};
  const removedRowsByRestaurant = {};
  const activeSlotCountByRestaurant = {};
  const baselineMapsByRestaurant = {};
  const groupedSamples = new Map();
  const unifiedSamplesByKey = new Map();
  const weightScorebook = createWeightScorebook();
  let removedRowsTuning = 0;

  for (const restaurant of RESTAURANTS) {
    const qualityFiltered = filterQualityRows(rawByRestaurant.get(restaurant));
    const activeSlotMap = buildActiveSlotMap(qualityFiltered.rows);
    const restaurantRows = qualityFiltered.rows;
    const dayMap = groupRowsByDate(restaurantRows);

    cleanRowsByRestaurant[restaurant] = restaurantRows;
    activeSlots[restaurant] = activeSlotMap;
    baselineMapsByRestaurant[restaurant] = buildTrainingBaselineMaps(restaurant, restaurantRows, activeSlotMap);
    removedRowsByRestaurant[restaurant] = {
      removedCount: qualityFiltered.removedCount,
      removedReasons: qualityFiltered.removedReasons
    };
    activeSlotCountByRestaurant[restaurant] = activeSlotCount(activeSlotMap);
    removedRowsTuning += qualityFiltered.removedCount;

    const dayKeys = Array.from(dayMap.keys()).sort();
    for (const dayKey of dayKeys) {
      const dayRows = filterRowsByActiveSlots(dayMap.get(dayKey), activeSlotMap);
      if (!dayRows.length) continue;
      const actualByTime = new Map(dayRows.map((row) => [formatTime(row.date), row.connected]));

      for (let index = 0; index < dayRows.length; index += 1) {
        const row = dayRows[index];
        const currentBaseline = trainingBaselineForLibrary(restaurant, row.date, baselineMapsByRestaurant[restaurant]);
        const trend15 = recentTrendFromRows(dayRows, index, row.date, 15);
        const trend30 = recentTrendFromRows(dayRows, index, row.date, 30);
        const currentGap = row.connected - currentBaseline;
        const gapPos = Math.max(currentGap, 0);
        const gapNeg = Math.max(-currentGap, 0);

        for (const horizonMinutes of HORIZONS) {
          const targetDate = addMinutes(row.date, horizonMinutes);
          if (dateOnly(targetDate) !== dayKey) continue;
          if (!activeSlotMap.get(formatTime(targetDate))?.active) continue;

          const actual = actualByTime.get(formatTime(targetDate));
          if (actual === undefined) continue;

          const targetBaseline = trainingBaselineForLibrary(restaurant, targetDate, baselineMapsByRestaurant[restaurant]);
          const targetDateKey = dateOnly(targetDate);
          const band = timeBandFor(formatTime(targetDate));
          const key = `${restaurant}|${horizonMinutes}|${band.key}`;
          const unifiedKey = `${restaurant}|${horizonMinutes}`;
          const list = groupedSamples.get(key) || [];
          const unifiedList = unifiedSamplesByKey.get(unifiedKey) || [];
          addSampleToWeightScorebook(weightScorebook, restaurant, horizonMinutes, targetBaseline, currentGap, actual);
          const sample = {
            restaurant,
            horizonMinutes,
            targetDateKey,
            actual,
            currentValue: row.connected,
            targetBaseline,
            currentGap,
            gapPos,
            gapNeg,
            baselineShiftSigned: targetBaseline - currentBaseline,
            baselineShiftAbs: Math.abs(targetBaseline - currentBaseline),
            bandKey: band.key,
            trend15,
            trend30,
            ...externalFeatureValuesFor(restaurant, targetDateKey, holidayMap, menuFeatureMap)
          };
          list.push(sample);
          unifiedList.push(sample);
          groupedSamples.set(key, list);
          unifiedSamplesByKey.set(unifiedKey, unifiedList);
        }
      }
    }
  }

  const recommendedWeightSet = pickBestWeightSet(weightScorebook.common, weightScorebook.restaurant);

  const groups = {};
  const fixedGroups = {};
  const residualGroups = {};
  const totals = {
    30: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    60: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    90: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 }
  };
  const fixedTotals = {
    30: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    60: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    90: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 }
  };
  const residualTotals = {
    30: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    60: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 },
    90: { baseErrorSum: 0, optimizedErrorSum: 0, count: 0 }
  };
  let selectedFormulaGroupCount = 0;
  let baseFallbackGroupCount = 0;
  const selectedFamilyCounts = {};
  let fixedFormulaGroupCount = 0;
  let fixedBaseFallbackGroupCount = 0;
  const fixedSelectedFamilyCounts = {};

  for (const [groupKey, samples] of groupedSamples) {
    const [restaurant, horizonText, bandKey] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    const baseWeight = recommendedWeightSet.restaurant[restaurant][horizonMinutes];
    const spec = buildBandedSpec(restaurant, horizonMinutes, bandKey, samples, baseWeight);
    const optimizedErrorSum = spec.optimizedMae * samples.length;

    if (spec.formulaKey === FORMULA_BASE) {
      baseFallbackGroupCount += 1;
    } else {
      selectedFormulaGroupCount += 1;
      selectedFamilyCounts[spec.formulaKey] = (selectedFamilyCounts[spec.formulaKey] || 0) + 1;
    }

    if (!groups[restaurant]) groups[restaurant] = {};
    if (!groups[restaurant][horizonMinutes]) groups[restaurant][horizonMinutes] = {};
    groups[restaurant][horizonMinutes][bandKey] = spec;

    totals[horizonMinutes].baseErrorSum += spec.baseMae * samples.length;
    totals[horizonMinutes].optimizedErrorSum += optimizedErrorSum;
    totals[horizonMinutes].count += samples.length;
  }

  for (const [groupKey, samples] of unifiedSamplesByKey) {
    const [restaurant, horizonText] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    const baseWeight = recommendedWeightSet.restaurant[restaurant][horizonMinutes];
    const spec = buildUnifiedSpec(restaurant, horizonMinutes, samples, baseWeight);
    const optimizedErrorSum = spec.optimizedMae * samples.length;

    if (!fixedGroups[restaurant]) fixedGroups[restaurant] = {};
    fixedGroups[restaurant][horizonMinutes] = spec;

    if (spec.formulaKey === FORMULA_BASE) {
      fixedBaseFallbackGroupCount += 1;
    } else {
      fixedFormulaGroupCount += 1;
      fixedSelectedFamilyCounts[spec.formulaKey] = (fixedSelectedFamilyCounts[spec.formulaKey] || 0) + 1;
    }

    fixedTotals[horizonMinutes].baseErrorSum += spec.baseMae * samples.length;
    fixedTotals[horizonMinutes].optimizedErrorSum += optimizedErrorSum;
    fixedTotals[horizonMinutes].count += samples.length;
  }

  for (const [groupKey, samples] of unifiedSamplesByKey) {
    const [restaurant, horizonText] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    const baseWeight = recommendedWeightSet.restaurant[restaurant][horizonMinutes];
    const spec = buildResidualSpec(restaurant, horizonMinutes, samples, baseWeight);
    const optimizedErrorSum = spec.optimizedMae * samples.length;

    if (!residualGroups[restaurant]) residualGroups[restaurant] = {};
    residualGroups[restaurant][horizonMinutes] = spec;

    residualTotals[horizonMinutes].baseErrorSum += spec.baseMae * samples.length;
    residualTotals[horizonMinutes].optimizedErrorSum += optimizedErrorSum;
    residualTotals[horizonMinutes].count += samples.length;
  }

  const validationByHorizon = Object.fromEntries(
    HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      {
        baseMae: round1(totals[horizonMinutes].baseErrorSum / totals[horizonMinutes].count),
        optimizedMae: round1(totals[horizonMinutes].optimizedErrorSum / totals[horizonMinutes].count),
        gain: round1(
          totals[horizonMinutes].baseErrorSum / totals[horizonMinutes].count
          - totals[horizonMinutes].optimizedErrorSum / totals[horizonMinutes].count
        ),
        count: totals[horizonMinutes].count
      }
    ])
  );

  const fixedValidationByHorizon = Object.fromEntries(
    HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      {
        baseMae: round1(fixedTotals[horizonMinutes].baseErrorSum / fixedTotals[horizonMinutes].count),
        optimizedMae: round1(fixedTotals[horizonMinutes].optimizedErrorSum / fixedTotals[horizonMinutes].count),
        gain: round1(
          fixedTotals[horizonMinutes].baseErrorSum / fixedTotals[horizonMinutes].count
          - fixedTotals[horizonMinutes].optimizedErrorSum / fixedTotals[horizonMinutes].count
        ),
        count: fixedTotals[horizonMinutes].count
      }
    ])
  );

  const residualValidationByHorizon = Object.fromEntries(
    HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      {
        baseMae: round1(residualTotals[horizonMinutes].baseErrorSum / residualTotals[horizonMinutes].count),
        optimizedMae: round1(residualTotals[horizonMinutes].optimizedErrorSum / residualTotals[horizonMinutes].count),
        gain: round1(
          residualTotals[horizonMinutes].baseErrorSum / residualTotals[horizonMinutes].count
          - residualTotals[horizonMinutes].optimizedErrorSum / residualTotals[horizonMinutes].count
        ),
        count: residualTotals[horizonMinutes].count
      }
    ])
  );

  const tuningWindow = {
    startDate: new Date(observedStart.getTime()),
    endDate: parseDateTime(profileConfig.trainingEndExclusive)
  };
  const featureAnalysis = buildFeatureAnalysis(unifiedSamplesByKey, fixedGroups);

  return {
    profile: buildProfilePayload(profileConfig),
    groups,
    fixedGroups,
    residualGroups,
    cleanRowsByRestaurant,
    activeSlots,
    tuningWindow,
    featureAnalysis,
    validationByHorizon,
    fixedValidationByHorizon,
    residualValidationByHorizon,
    summary: {
      tuningWindow: {
        start: formatDateTime(observedStart),
        end: formatDateTime(observedEnd),
        startDate: dateOnly(observedStart),
        endDate: dateOnly(observedEnd),
        label: `${dateOnly(observedStart)} ~ ${dateOnly(observedEnd)}`,
        rowCount: rows.length
      },
      recommendedWeights: recommendedWeightSet,
      selectedFormulaGroupCount,
      baseFallbackGroupCount,
      selectedFamilyCounts,
      fixedFormulaGroupCount,
      fixedBaseFallbackGroupCount,
      fixedSelectedFamilyCounts,
      minSamples: OPTIMIZED_MIN_SAMPLES,
      removedRowsTuning,
      removedRowsByRestaurant,
      activeSlotCountByRestaurant
    }
  };
}

function buildHolidayMap(rows) {
  return new Map(rows.map((row) => [
    parseDate(normalizeSqlDay(row.date)),
    Number(row.is_holiday) ? 1 : 0
  ]));
}

function buildMenuFeatureMap(rows) {
  const map = new Map();
  for (const row of rows) {
    const restaurant = String(row.restaurant);
    if (!RESTAURANTS.includes(restaurant)) continue;
    const date = parseDate(normalizeSqlDay(row.date));
    const menuCount = Number(row.menuCount || 0);
    const openCount = Number(row.openCount || 0);
    map.set(`${restaurant}|${date}`, {
      menuDataAvailable: menuCount > 0 ? 1 : 0,
      menuOpenAny: openCount > 0 ? 1 : 0,
      menuOpenCount: openCount,
      menuMenuCount: menuCount,
      menuClosedAll: menuCount > 0 && openCount === 0 ? 1 : 0
    });
  }
  return map;
}

function externalFeatureValuesFor(restaurant, targetDateKey, holidayMap, menuFeatureMap) {
  const menu = menuFeatureMap.get(`${restaurant}|${targetDateKey}`) || {
    menuDataAvailable: 0,
    menuOpenAny: 0,
    menuOpenCount: 0,
    menuMenuCount: 0,
    menuClosedAll: 0
  };

  return {
    isHoliday: Number(holidayMap.get(targetDateKey) || 0),
    menuDataAvailable: Number(menu.menuDataAvailable || 0),
    menuOpenAny: Number(menu.menuOpenAny || 0),
    menuOpenCount: Number(menu.menuOpenCount || 0),
    menuMenuCount: Number(menu.menuMenuCount || 0),
    menuClosedAll: Number(menu.menuClosedAll || 0)
  };
}

featureDocPayload = function featureDocPayload(key) {
  switch (key) {
    case 'intercept':
      return {
        key,
        label: '절편',
        meaning: '모든 입력값이 0일 때 시작점',
        detail: '실제값과 예측값의 평균 차이를 줄이도록 전체 데이터에서 같이 학습한 고정 상수입니다.',
        source: 'internal'
      };
    case 'targetBaseline':
      return {
        key,
        label: '목표 평소값',
        meaning: '같은 식당·같은 요일·같은 시각의 과거 평균',
        detail: '30/60/90분 뒤 시점이 원래 얼마나 붐비는 시간대인지 보여주는 기준값입니다.',
        source: 'internal'
      };
    case 'currentValue':
      return {
        key,
        label: '현재값',
        meaning: '기준 시각의 실제 혼잡도',
        detail: '지금 시점에 실제로 얼마나 붐비는지 직접 반영합니다.',
        source: 'internal'
      };
    case 'pressureGap':
      return {
        key,
        label: '피크 압력',
        meaning: 'max(목표 평소값 - 현재값, 0)',
        detail: '앞으로 더 붐빌 시간대로 가는 중이면 값이 커집니다.',
        source: 'internal'
      };
    case 'baselineShiftPos':
      return {
        key,
        label: '상승 평소값 이동',
        meaning: 'max(목표 평소값 - 현재 평소값, 0)',
        detail: '현재보다 목표 시각이 원래 더 붐비는 구간인지 반영합니다.',
        source: 'internal'
      };
    case 'gapPos':
      return {
        key,
        label: '현재 초과분',
        meaning: 'max(현재값 - 현재 평소값, 0)',
        detail: '지금이 평소보다 얼마나 높게 시작했는지 나타냅니다.',
        source: 'internal'
      };
    case 'gapNeg':
      return {
        key,
        label: '현재 부족분',
        meaning: 'max(현재 평소값 - 현재값, 0)',
        detail: '현재 혼잡도가 평소보다 낮을 때, 기본식을 더 낮춰야 하는지를 보는 값입니다.',
        source: 'internal'
      };
    case 'rise30':
      return {
        key,
        label: '최근 30분 상승',
        meaning: 'max(최근 30분 변화량, 0)',
        detail: '직전 30분 동안 실제 혼잡도가 얼마나 올라왔는지 보는 값입니다.',
        source: 'internal'
      };
    case 'surge30':
      return {
        key,
        label: '30분 가속',
        meaning: '(피크 압력 × 최근 30분 상승분) / 50',
        detail: '앞으로 더 붐빌 시간대인데 최근 30분도 실제로 오르는 중일 때만 추가 반응을 줍니다.',
        source: 'internal'
      };
    case 'isHoliday':
      return {
        key,
        label: '공휴일 여부',
        meaning: '공휴일이면 1, 아니면 0',
        detail: '휴일에는 식당 운영 패턴과 수요가 평일과 달라질 수 있는지 확인하는 후보 변수입니다.',
        source: 'holiday'
      };
    case 'menuOpenAny':
      return {
        key,
        label: '메뉴 운영 여부',
        meaning: '해당 날짜에 open 메뉴가 1개 이상이면 1',
        detail: '그날 실제로 운영 중인 메뉴가 있었는지 여부입니다.',
        source: 'menu'
      };
    case 'menuOpenCount':
      return {
        key,
        label: '운영 메뉴 수',
        meaning: '해당 날짜에 open 상태인 메뉴 개수',
        detail: '운영 메뉴 수가 많을수록 혼잡도가 달라지는지 확인하는 후보 변수입니다.',
        source: 'menu'
      };
    case 'menuMenuCount':
      return {
        key,
        label: '등록 메뉴 수',
        meaning: '해당 날짜에 등록된 전체 메뉴 개수',
        detail: '운영 여부와 별개로 메뉴 구성이 얼마나 많았는지 보는 후보 변수입니다.',
        source: 'menu'
      };
    case 'menuClosedAll':
      return {
        key,
        label: '메뉴 전부 마감',
        meaning: '메뉴 데이터는 있는데 open 메뉴가 0개면 1',
        detail: '메뉴는 등록돼 있었지만 실제 운영이 거의 없었던 날을 구분하는 후보 변수입니다.',
        source: 'menu'
      };
    default:
      return {
        key,
        label: key,
        meaning: key,
        detail: '',
        source: 'internal'
      };
  }
}

function sameFeatureKeys(left, right) {
  if (!Array.isArray(left) || !Array.isArray(right) || left.length !== right.length) return false;
  return left.every((key, index) => key === right[index]);
}

function roundPrediction(value) {
  return Math.max(0, Math.round(value));
}

function emptyMaeTotals() {
  return Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    { errorSum: 0, count: 0 }
  ]));
}

function evaluateFeatureSet(unifiedSamplesByKey, fixedGroups, featureKeys, options = {}) {
  const lambda = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).lambda;
  const useExistingCoefficients = Boolean(options.useExistingCoefficients);
  const totalsByHorizon = emptyMaeTotals();
  let errorSum = 0;
  let count = 0;

  for (const [groupKey, samples] of unifiedSamplesByKey) {
    const [restaurant, horizonText] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    const spec = fixedGroups[restaurant]?.[horizonMinutes];
    const coefficients = useExistingCoefficients && sameFeatureKeys(spec?.featureKeys, featureKeys)
      ? spec.coefficients
      : fitCoefficientsForFeatureKeys(samples, featureKeys, lambda).map(round3);

    if (!coefficients?.length) continue;

    let groupErrorSum = 0;
    for (const sample of samples) {
      const values = featureValueMapForKeys(sample, featureKeys);
      const predicted = roundPrediction(predictWithCoefficients(coefficients, featureKeys, values));
      groupErrorSum += Math.abs(predicted - sample.actual);
    }

    errorSum += groupErrorSum;
    count += samples.length;
    totalsByHorizon[horizonMinutes].errorSum += groupErrorSum;
    totalsByHorizon[horizonMinutes].count += samples.length;
  }

  return {
    mae: count ? round3(errorSum / count) : 0,
    count,
    byHorizon: Object.fromEntries(HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      {
        mae: totalsByHorizon[horizonMinutes].count
          ? round3(totalsByHorizon[horizonMinutes].errorSum / totalsByHorizon[horizonMinutes].count)
          : 0,
        count: totalsByHorizon[horizonMinutes].count
      }
    ]))
  };
}

function createImpactBucket(featureKeys) {
  return {
    baseErrorSum: 0,
    count: 0,
    byFeature: Object.fromEntries(featureKeys.map((key) => [
      key,
      {
        removedErrorSum: 0,
        absContributionSum: 0,
        nonZeroCount: 0
      }
    ]))
  };
}

function updateImpactBucket(bucket, featureKeys, coefficients, values, rawPrediction, predicted, actual) {
  bucket.baseErrorSum += Math.abs(predicted - actual);
  bucket.count += 1;

  for (let index = 0; index < featureKeys.length; index += 1) {
    const key = featureKeys[index];
    const contribution = (coefficients[index] || 0) * (values[key] ?? 0);
    const removedPrediction = roundPrediction(rawPrediction - contribution);
    const stat = bucket.byFeature[key];
    stat.removedErrorSum += Math.abs(removedPrediction - actual);
    stat.absContributionSum += Math.abs(contribution);
    if (Math.abs(contribution) > 1e-9) stat.nonZeroCount += 1;
  }
}

function finalizeImpactBucket(bucket, featureKeys) {
  const currentMae = bucket.count ? bucket.baseErrorSum / bucket.count : 0;
  return {
    mae: round3(currentMae),
    count: bucket.count,
    rows: featureKeys.map((key) => {
      const stat = bucket.byFeature[key];
      const removedMae = bucket.count ? stat.removedErrorSum / bucket.count : 0;
      return {
        ...featureDocPayload(key),
        maeWithout: round3(removedMae),
        maeDelta: round3(removedMae - currentMae),
        averageAbsContribution: round3(bucket.count ? stat.absContributionSum / bucket.count : 0),
        nonZeroRate: round3(bucket.count ? stat.nonZeroCount / bucket.count : 0)
      };
    }).sort((left, right) => right.maeDelta - left.maeDelta)
  };
}

function buildImpactStatus(overallRow, byHorizon) {
  const horizonDeltas = HORIZONS.map((horizonMinutes) => byHorizon[horizonMinutes]?.maeDelta || 0);
  const removeCandidate = overallRow.maeDelta < FEATURE_PRUNE_OVERALL_MAX_MAE_DELTA
    && horizonDeltas.every((value) => value < FEATURE_PRUNE_HORIZON_MAX_MAE_DELTA);
  const surgeKeep = overallRow.key === 'surge30'
    && (
      overallRow.maeDelta >= SURGE30_KEEP_OVERALL_MIN_MAE_DELTA
      || horizonDeltas.some((value) => value >= SURGE30_KEEP_HORIZON_MIN_MAE_DELTA)
    );
  const worstHorizon = HORIZONS
    .map((horizonMinutes) => ({
      horizonMinutes,
      maeDelta: byHorizon[horizonMinutes]?.maeDelta || 0
    }))
    .sort((left, right) => right.maeDelta - left.maeDelta)[0];

  if (removeCandidate) {
    return {
      status: 'removeCandidate',
      statusLabel: '제거 후보',
      reason: `전체 MAE 증가는 +${round1(overallRow.maeDelta)}이고, 30/60/90분 모두 +${FEATURE_PRUNE_HORIZON_MAX_MAE_DELTA.toFixed(1)} 이하라 단순화 후보입니다.`
    };
  }

  if (surgeKeep) {
    return {
      status: 'keep',
      statusLabel: '유지',
      reason: `피크 진입 구간 보호용 항목으로 유지합니다. 가장 영향이 큰 구간은 ${worstHorizon.horizonMinutes}분 후(+${round1(worstHorizon.maeDelta)})입니다.`
    };
  }

  return {
    status: 'keep',
    statusLabel: '유지',
    reason: `빼면 전체 MAE가 +${round1(overallRow.maeDelta)} 악화됩니다. 가장 영향이 큰 구간은 ${worstHorizon.horizonMinutes}분 후(+${round1(worstHorizon.maeDelta)})입니다.`
  };
}

function featureCoverageForCandidate(unifiedSamplesByKey, candidate) {
  const overall = { coveredCount: 0, totalCount: 0 };
  const byHorizon = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    { coveredCount: 0, totalCount: 0 }
  ]));

  for (const [groupKey, samples] of unifiedSamplesByKey) {
    const [, horizonText] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    for (const sample of samples) {
      const covered = candidate.coverageKey ? Boolean(sample[candidate.coverageKey]) : true;
      overall.totalCount += 1;
      byHorizon[horizonMinutes].totalCount += 1;
      if (!covered) continue;
      overall.coveredCount += 1;
      byHorizon[horizonMinutes].coveredCount += 1;
    }
  }

  return {
    coveredCount: overall.coveredCount,
    totalCount: overall.totalCount,
    coverageRate: overall.totalCount ? round3(overall.coveredCount / overall.totalCount) : 0,
    byHorizon: Object.fromEntries(HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      {
        coveredCount: byHorizon[horizonMinutes].coveredCount,
        totalCount: byHorizon[horizonMinutes].totalCount,
        coverageRate: byHorizon[horizonMinutes].totalCount
          ? round3(byHorizon[horizonMinutes].coveredCount / byHorizon[horizonMinutes].totalCount)
          : 0
      }
    ]))
  };
}

function buildExternalCandidateResult(candidate, unifiedSamplesByKey, fixedGroups, currentEvaluation, addedKeys, stepBaseMae = null) {
  const evaluation = evaluateFeatureSet(
    unifiedSamplesByKey,
    fixedGroups,
    formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).featureKeys.concat(addedKeys)
  );
  const coverage = featureCoverageForCandidate(unifiedSamplesByKey, candidate);
  const maeImprovement = round3(currentEvaluation.mae - evaluation.mae);
  const incrementalImprovement = stepBaseMae === null ? maeImprovement : round3(stepBaseMae - evaluation.mae);
  const horizonMaeDelta = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    round3(evaluation.byHorizon[horizonMinutes].mae - currentEvaluation.byHorizon[horizonMinutes].mae)
  ]));
  const stableHorizonCount = HORIZONS.filter(
    (horizonMinutes) => horizonMaeDelta[horizonMinutes] <= EXTERNAL_FEATURE_MAX_HORIZON_DEGRADATION
  ).length;
  const coverageEligible = candidate.source !== 'menu' || coverage.coverageRate >= EXTERNAL_MENU_MIN_COVERAGE;
  const accepted = coverageEligible
    && maeImprovement >= EXTERNAL_FEATURE_MIN_IMPROVEMENT
    && stableHorizonCount >= 2;

  let status = 'rejected';
  let statusLabel = '제외';
  let reason = `전체 MAE 개선 ${round1(maeImprovement)}로 기준 ${EXTERNAL_FEATURE_MIN_IMPROVEMENT.toFixed(2)}에 못 미칩니다.`;

  if (!coverageEligible) {
    status = 'coverageLow';
    statusLabel = '커버리지 부족';
    reason = `메뉴 데이터 커버리지가 ${round1(coverage.coverageRate * 100)}%라 기준 85% 미만입니다.`;
  } else if (stableHorizonCount < 2) {
    status = 'rejected';
    statusLabel = '구간 불안정';
    reason = `30/60/90분 중 2개 이상에서 악화폭을 +${EXTERNAL_FEATURE_MAX_HORIZON_DEGRADATION.toFixed(2)} 이하로 유지하지 못했습니다.`;
  } else if (accepted) {
    status = 'candidate';
    statusLabel = '추가 후보';
    reason = `전체 MAE ${currentEvaluation.mae} -> ${evaluation.mae}, 개선폭 ${round1(maeImprovement)}입니다.`;
  }

  return {
    ...featureDocPayload(candidate.key),
    sourceLabel: candidate.source === 'menu' ? 'menu' : 'holiday',
    addedKeys,
    coverageRate: coverage.coverageRate,
    coveredCount: coverage.coveredCount,
    totalCount: coverage.totalCount,
    mae: evaluation.mae,
    maeImprovement,
    incrementalImprovement,
    horizonMaeDelta,
    stableHorizonCount,
    status,
    statusLabel,
    reason,
    accepted,
    evaluation
  };
}

function buildExternalCandidateAnalysis(unifiedSamplesByKey, fixedGroups, currentEvaluation) {
  const singleResults = EXTERNAL_FEATURE_CANDIDATES.map((candidate) => (
    buildExternalCandidateResult(candidate, unifiedSamplesByKey, fixedGroups, currentEvaluation, [candidate.key])
  ));
  const acceptedSingles = singleResults
    .filter((item) => item.accepted)
    .sort((left, right) => right.maeImprovement - left.maeImprovement);

  const selectedKeys = [];
  const selectedDocs = [];
  const steps = [];
  let selectedMae = currentEvaluation.mae;
  let totalImprovement = 0;

  const firstSelected = acceptedSingles[0] || null;
  if (firstSelected) {
    selectedKeys.push(firstSelected.key);
    selectedDocs.push(featureDocPayload(firstSelected.key));
    selectedMae = firstSelected.mae;
    totalImprovement = round3(currentEvaluation.mae - selectedMae);
    firstSelected.status = 'selected';
    firstSelected.statusLabel = '선택';
    steps.push({
      step: 1,
      key: firstSelected.key,
      label: firstSelected.label,
      mae: firstSelected.mae,
      maeImprovement: firstSelected.maeImprovement,
      note: `단일 후보 중 개선폭이 가장 커서 먼저 선택했습니다.`
    });

    const secondCandidates = EXTERNAL_FEATURE_CANDIDATES
      .filter((candidate) => candidate.key !== firstSelected.key)
      .map((candidate) => buildExternalCandidateResult(
        candidate,
        unifiedSamplesByKey,
        fixedGroups,
        currentEvaluation,
        [firstSelected.key, candidate.key],
        firstSelected.mae
      ))
      .filter((item) => item.accepted)
      .sort((left, right) => right.maeImprovement - left.maeImprovement);

    const secondSelected = secondCandidates[0] || null;
    if (secondSelected) {
      selectedKeys.push(secondSelected.key);
      selectedDocs.push(featureDocPayload(secondSelected.key));
      selectedMae = secondSelected.mae;
      totalImprovement = round3(currentEvaluation.mae - selectedMae);
      const singleMatch = singleResults.find((item) => item.key === secondSelected.key);
      if (singleMatch) {
        singleMatch.status = 'selected';
        singleMatch.statusLabel = '선택';
      }
      steps.push({
        step: 2,
        key: secondSelected.key,
        label: secondSelected.label,
        mae: secondSelected.mae,
        maeImprovement: secondSelected.maeImprovement,
        note: `${firstSelected.label} 이후 남은 후보 중 전체 MAE가 가장 낮아지는 조합입니다.`
      });
    }
  }

  const selectionReason = selectedKeys.length
    ? `${selectedDocs.map((item) => item.label).join(', ')}을 더하면 전체 MAE가 ${currentEvaluation.mae}에서 ${selectedMae}로 내려갑니다.`
    : '공휴일·메뉴 후보를 넣어도 기준 개선폭을 안정적으로 넘는 조합이 없어 현재 식을 그대로 유지합니다.';

  return {
    list: singleResults
      .map((item) => ({
        ...item,
        evaluation: undefined
      }))
      .sort((left, right) => right.maeImprovement - left.maeImprovement),
    recommendedAdditions: {
      selectedKeys,
      selected: selectedDocs,
      maeImprovement: totalImprovement,
      finalMae: round3(selectedMae),
      steps,
      selectionReason
    }
  };
}

function buildFeatureAnalysis(unifiedSamplesByKey, fixedGroups) {
  const featureKeys = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).featureKeys;
  const currentEvaluation = evaluateFeatureSet(unifiedSamplesByKey, fixedGroups, featureKeys, {
    useExistingCoefficients: true
  });
  const overallBucket = createImpactBucket(featureKeys);
  const bucketByHorizon = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    createImpactBucket(featureKeys)
  ]));

  for (const [groupKey, samples] of unifiedSamplesByKey) {
    const [restaurant, horizonText] = groupKey.split('|');
    const horizonMinutes = Number(horizonText);
    const spec = fixedGroups[restaurant]?.[horizonMinutes];
    if (!spec?.coefficients?.length) continue;

    for (const sample of samples) {
      const values = featureValueMapForKeys(sample, featureKeys);
      const rawPrediction = predictWithCoefficients(spec.coefficients, featureKeys, values);
      const predicted = roundPrediction(rawPrediction);
      updateImpactBucket(overallBucket, featureKeys, spec.coefficients, values, rawPrediction, predicted, sample.actual);
      updateImpactBucket(bucketByHorizon[horizonMinutes], featureKeys, spec.coefficients, values, rawPrediction, predicted, sample.actual);
    }
  }

  const overallImpact = finalizeImpactBucket(overallBucket, featureKeys);
  const byHorizonImpact = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    {
      horizonMinutes,
      label: `${horizonMinutes}분 후`,
      ...finalizeImpactBucket(bucketByHorizon[horizonMinutes], featureKeys)
    }
  ]));
  const rowByKeyByHorizon = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    Object.fromEntries(byHorizonImpact[horizonMinutes].rows.map((row) => [row.key, row]))
  ]));
  const overallRows = overallImpact.rows.map((row) => {
    const status = buildImpactStatus(row, Object.fromEntries(HORIZONS.map((horizonMinutes) => [
      horizonMinutes,
      rowByKeyByHorizon[horizonMinutes][row.key]
    ])));
    return {
      ...row,
      ...status
    };
  });
  const statusByKey = Object.fromEntries(overallRows.map((row) => [row.key, row]));
  const byHorizonRows = Object.fromEntries(HORIZONS.map((horizonMinutes) => [
    horizonMinutes,
    {
      ...byHorizonImpact[horizonMinutes],
      rows: byHorizonImpact[horizonMinutes].rows.map((row) => ({
        ...row,
        status: statusByKey[row.key]?.status || 'keep',
        statusLabel: statusByKey[row.key]?.statusLabel || '유지'
      }))
    }
  ]));
  const removedFeatures = overallRows.filter((row) => row.status === 'removeCandidate');
  const keptFeatures = overallRows.filter((row) => row.status !== 'removeCandidate');
  const externalCandidateAnalysis = buildExternalCandidateAnalysis(unifiedSamplesByKey, fixedGroups, currentEvaluation);

  return {
    currentFormula: {
      formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
      label: formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).label,
      formula: formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY).formula,
      mae: currentEvaluation.mae,
      count: currentEvaluation.count,
      features: featureKeys.map((key) => featureDocPayload(key))
    },
    impact: {
      overallMae: currentEvaluation.mae,
      count: currentEvaluation.count,
      overall: overallRows,
      byHorizon: byHorizonRows
    },
    recommendedSimplifiedFormula: {
      features: keptFeatures.map((row) => featureDocPayload(row.key)),
      removedFeatures: removedFeatures.map((row) => featureDocPayload(row.key)),
      selectionReason: removedFeatures.length
        ? `${removedFeatures.map((row) => row.label).join(', ')}은 제거 기준을 만족해 단순화 후보로 분류했습니다.`
        : '현재 항목 모두 제거 시 MAE 손실이 있어 그대로 유지하는 편이 안전합니다.'
    },
    externalCandidates: externalCandidateAnalysis.list,
    recommendedAdditions: externalCandidateAnalysis.recommendedAdditions
  };
}

function fitCoefficientsForFeatureKeys(samples, featureKeys, lambda) {
  const featureVectors = samples.map((sample) => featureVector(featureKeys, featureValueMapForKeys(sample, featureKeys)));
  const dimension = featureKeys.length;
  const xtx = Array.from({ length: dimension }, () => Array(dimension).fill(0));
  const xty = Array(dimension).fill(0);

  for (let sampleIndex = 0; sampleIndex < samples.length; sampleIndex += 1) {
    const vector = featureVectors[sampleIndex];
    for (let row = 0; row < dimension; row += 1) {
      xty[row] += vector[row] * samples[sampleIndex].actual;
      for (let column = 0; column < dimension; column += 1) {
        xtx[row][column] += vector[row] * vector[column];
      }
    }
  }

  for (let index = 1; index < dimension; index += 1) {
    xtx[index][index] += lambda;
  }

  return solveLinearSystem(xtx, xty);
}

function fitFormulaCoefficients(samples, formulaKey, lambda) {
  return fitCoefficientsForFeatureKeys(samples, formulaMeta(formulaKey).featureKeys, lambda);
}

function fitResidualCoefficientsForFeatureKeys(samples, featureKeys, lambda, baseWeight) {
  const featureVectors = samples.map((sample) => featureVector(featureKeys, featureValueMapForKeys(sample, featureKeys)));
  const dimension = featureKeys.length;
  const xtx = Array.from({ length: dimension }, () => Array(dimension).fill(0));
  const xty = Array(dimension).fill(0);

  for (let sampleIndex = 0; sampleIndex < samples.length; sampleIndex += 1) {
    const sample = samples[sampleIndex];
    const vector = featureVectors[sampleIndex];
    const baseRawPrediction = sample.targetBaseline + sample.currentGap * baseWeight;
    const residualTarget = sample.actual - baseRawPrediction;

    for (let row = 0; row < dimension; row += 1) {
      xty[row] += vector[row] * residualTarget;
      for (let column = 0; column < dimension; column += 1) {
        xtx[row][column] += vector[row] * vector[column];
      }
    }
  }

  for (let index = 0; index < dimension; index += 1) {
    xtx[index][index] += lambda;
  }

  return solveLinearSystem(xtx, xty);
}

function fitResidualFormulaCoefficients(samples, formulaKey, lambda, baseWeight) {
  return fitResidualCoefficientsForFeatureKeys(samples, formulaMeta(formulaKey).featureKeys, lambda, baseWeight);
}

function scoreCandidateSamples(samples, predictor, bandKey) {
  if (!samples.length) {
    return { mae: 0, peakUnder: 0, score: 0 };
  }

  let errorSum = 0;
  let peakUnderSum = 0;
  let peakUnderCount = 0;
  const threshold = peakUnderThreshold(samples);

  for (const sample of samples) {
    const predicted = predictor(sample);
    const signedError = predicted - sample.actual;
    errorSum += Math.abs(signedError);

    if (sample.actual >= threshold && signedError < 0) {
      peakUnderSum += Math.abs(signedError);
      peakUnderCount += 1;
    }
  }

  const mae = errorSum / samples.length;
  const peakUnder = peakUnderCount ? peakUnderSum / peakUnderCount : 0;
  return {
    mae,
    peakUnder,
    score: mae + peakUnder * peakScoreWeight(bandKey)
  };
}

function scoreCandidateSamplesMixed(samples, predictor) {
  if (!samples.length) {
    return { mae: 0, peakPenalty: 0, score: 0 };
  }

  let errorSum = 0;
  const thresholds = peakUnderThresholdsByBand(samples);
  const peakUnderByBand = {};

  for (const sample of samples) {
    const predicted = predictor(sample);
    const signedError = predicted - sample.actual;
    errorSum += Math.abs(signedError);

    const bandKey = sample.bandKey;
    const weight = peakScoreWeight(bandKey);
    if (!weight) continue;
    const threshold = thresholds[bandKey] ?? 0;
    if (sample.actual < threshold || signedError >= 0) continue;

    const current = peakUnderByBand[bandKey] || { sum: 0, count: 0 };
    current.sum += Math.abs(signedError);
    current.count += 1;
    peakUnderByBand[bandKey] = current;
  }

  const mae = errorSum / samples.length;
  const peakPenalty = Object.entries(peakUnderByBand).reduce((sum, [bandKey, item]) => {
    const peakUnder = item.count ? item.sum / item.count : 0;
    return sum + peakUnder * peakScoreWeight(bandKey);
  }, 0);

  return {
    mae,
    peakPenalty: round3(peakPenalty),
    score: mae + peakPenalty
  };
}

function peakUnderThreshold(samples) {
  const sortedActuals = samples.map((sample) => sample.actual).slice().sort((left, right) => left - right);
  const index = Math.max(0, Math.floor(sortedActuals.length * PEAK_UNDER_QUANTILE) - 1);
  return sortedActuals[index] ?? 0;
}

function peakUnderThresholdsByBand(samples) {
  const grouped = {};
  for (const sample of samples) {
    if (!peakScoreWeight(sample.bandKey)) continue;
    if (!grouped[sample.bandKey]) grouped[sample.bandKey] = [];
    grouped[sample.bandKey].push(sample);
  }

  return Object.fromEntries(
    Object.entries(grouped).map(([bandKey, bandSamples]) => [bandKey, peakUnderThreshold(bandSamples)])
  );
}

function peakScoreWeight(bandKey) {
  return PEAK_SCORE_WEIGHTS[bandKey] || 0;
}

function solveLinearSystem(matrix, vector) {
  const size = matrix.length;
  const augmented = matrix.map((row, index) => row.slice().concat(vector[index]));

  for (let column = 0; column < size; column += 1) {
    let pivot = column;
    for (let row = column + 1; row < size; row += 1) {
      if (Math.abs(augmented[row][column]) > Math.abs(augmented[pivot][column])) pivot = row;
    }

    if (Math.abs(augmented[pivot][column]) < 1e-9) continue;
    if (pivot !== column) [augmented[column], augmented[pivot]] = [augmented[pivot], augmented[column]];

    const pivotValue = augmented[column][column];
    for (let index = column; index <= size; index += 1) {
      augmented[column][index] /= pivotValue;
    }

    for (let row = 0; row < size; row += 1) {
      if (row === column) continue;
      const factor = augmented[row][column];
      if (Math.abs(factor) < 1e-12) continue;
      for (let index = column; index <= size; index += 1) {
        augmented[row][index] -= factor * augmented[column][index];
      }
    }
  }

  return augmented.map((row) => row[size]);
}

function featureDocPayload(key) {
  switch (key) {
    case 'intercept':
      return {
        key,
        label: '절편',
        meaning: '모든 입력값이 0일 때의 시작점',
        detail: '설명성 검토 결과 최종 운영식에서는 제거한 상수항입니다.',
        source: 'internal'
      };
    case 'targetBaseline':
      return {
        key,
        label: '목표 평소값',
        meaning: '같은 식당·같은 요일·같은 시각의 과거 평균',
        detail: '30/60/90분 뒤 시점이 원래 얼마나 붐비는 시간대인지 보여주는 기준값입니다.',
        source: 'internal'
      };
    case 'currentValue':
      return {
        key,
        label: '현재값',
        meaning: '기준 시각의 실제 혼잡도',
        detail: '지금 시점에 실제로 얼마나 붐비는지 직접 반영합니다.',
        source: 'internal'
      };
    case 'pressureGap':
      return {
        key,
        label: '피크 압력',
        meaning: 'max(목표 평소값 - 현재값, 0)',
        detail: '앞으로 더 붐빌 시간대로 가는 중이면 값이 커집니다.',
        source: 'internal'
      };
    case 'baselineShiftPos':
      return {
        key,
        label: '상승 평소값 이동',
        meaning: 'max(목표 평소값 - 현재 평소값, 0)',
        detail: '현재보다 목표 시각이 원래 더 붐비는 구간인지 반영합니다.',
        source: 'internal'
      };
    case 'gapPos':
      return {
        key,
        label: '현재 초과분',
        meaning: 'max(현재값 - 현재 평소값, 0)',
        detail: '지금이 평소보다 얼마나 높게 시작했는지 나타냅니다.',
        source: 'internal'
      };
    case 'surge30':
      return {
        key,
        label: '30분 가속',
        meaning: '(피크 압력 × 최근 30분 상승분) / 50',
        detail: '앞으로 더 붐빌 시간대인데 최근 30분도 실제로 오르는 중일 때만 추가 반응을 줍니다.',
        source: 'internal'
      };
    case 'isHoliday':
      return {
        key,
        label: '공휴일 여부',
        meaning: '공휴일이면 1, 아니면 0',
        detail: '후보 변수로 검토했지만 최종 운영식에는 넣지 않았습니다.',
        source: 'holiday'
      };
    case 'menuOpenAny':
      return {
        key,
        label: '메뉴 운영 여부',
        meaning: '해당 날짜에 open 메뉴가 1개 이상이면 1',
        detail: '후보 변수로 검토했지만 최종 운영식에는 넣지 않았습니다.',
        source: 'menu'
      };
    case 'menuOpenCount':
      return {
        key,
        label: '운영 메뉴 수',
        meaning: '해당 날짜에 open 상태인 메뉴 개수',
        detail: '후보 변수로 검토했지만 최종 운영식에는 넣지 않았습니다.',
        source: 'menu'
      };
    case 'menuMenuCount':
      return {
        key,
        label: '등록 메뉴 수',
        meaning: '해당 날짜에 등록된 전체 메뉴 개수',
        detail: '후보 변수로 검토했지만 최종 운영식에는 넣지 않았습니다.',
        source: 'menu'
      };
    case 'menuClosedAll':
      return {
        key,
        label: '메뉴 전부 마감',
        meaning: '메뉴 데이터는 있는데 open 메뉴가 0개면 1',
        detail: '후보 변수로 검토했지만 최종 운영식에는 넣지 않았습니다.',
        source: 'menu'
      };
    default:
      return {
        key,
        label: key,
        meaning: key,
        detail: '',
        source: 'internal'
      };
  }
};

formulaPayloadV2 = function formulaPayloadV2(optimizedLibrary) {
  const pressureFamily = formulaMeta(FORCED_OPTIMIZED_FORMULA_KEY);
  return {
    base: '예측값 = 목표 평소값 + (현재값 - 현재 시점 평소값) × 반영률',
    optimized: {
      key: pressureFamily.shortLabel,
      formulaKey: FORCED_OPTIMIZED_FORMULA_KEY,
      label: '피크 압력식',
      shortLabel: pressureFamily.shortLabel,
      formula: '예측값 = (목표 평소값 계수 × 목표 평소값) + (현재값 계수 × 현재값) + (피크 압력 계수 × 피크 압력) + (상승 평소값 이동 계수 × 상승 평소값 이동) + (현재 초과분 계수 × 현재 초과분) + (30분 가속 계수 × 30분 가속)',
      note: '최종 운영식은 절편을 제외한 6개 항목만 사용합니다. 공휴일과 메뉴 항목은 후보로 검토했지만 운영식에는 넣지 않았고, 최종값에는 기본식 기준 안전장치를 함께 적용합니다.'
    },
    featureDocs: [
      featureDocPayload('targetBaseline'),
      featureDocPayload('currentValue'),
      featureDocPayload('pressureGap'),
      featureDocPayload('baselineShiftPos'),
      featureDocPayload('gapPos'),
      featureDocPayload('surge30')
    ],
    training: {
      objective: 'sum((actual - predicted)^2) + lambda * sum(coef^2)',
      lambda: pressureFamily.lambda,
      fitNote: '같은 식당의 같은 예측 구간(30/60/90분) 데이터로 ridge regression을 돌려 계수를 정합니다.',
      interceptNote: '최종 운영식에서는 절편을 제거했습니다. 그래서 식은 6개 항목만으로 설명됩니다.',
      fixedNote: '계수는 식당별 30/60/90분 그룹마다 고정되고, 날짜가 바뀌어도 같은 그룹이면 같은 계수를 사용합니다.',
      dynamicNote: '예측할 때마다 다시 계산되는 값은 목표 평소값, 현재값, 피크 압력, 상승 평소값 이동, 현재 초과분, 30분 가속이고, 마지막에는 과한 보정을 줄이는 안전장치를 한 번 더 적용합니다.',
      scopeNote: '현재 운영 모드는 식당별 30/60/90분 단위의 고정식입니다.'
    },
    selection: `${tuningWindowLabel(optimizedLibrary.summary.tuningWindow)} 기준으로 검증했을 때, 피크 압력식을 기본 운영식으로 두고 식당별 30/60/90분만 다르게 쓰는 구성이 가장 설명하기 쉬우면서 성능도 안정적이었습니다. 여기에 기본식을 기준점으로 삼는 안전장치를 더해 일부 날짜의 과한 보정도 눌렀습니다.`
  };
};
