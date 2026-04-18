const HORIZONS = [30, 60, 90];
const TIME_BANDS = ['morning', 'preLunch', 'lunchPeak', 'postLunch', 'afternoon', 'dinner'];
const MODEL_BASE = 'base';
const MODEL_ADJUSTED = 'adjusted';
const FORMULA_BASE = 'base';
const STRATEGY_BANDED = 'banded';
const STRATEGY_RESTAURANT_HORIZON = 'restaurantHorizon';
const PROFILE_ALL_DATA = 'allData';
const PROFILE_TRAIN_TO_2025 = 'trainTo2025';
const DEFAULT_ROWS_LIMIT = 72;
const GRID_ROWS_LIMIT = 48;
const numberFormat = new Intl.NumberFormat('ko-KR');

const FALLBACK_COMMON_RECOMMENDED_WEIGHTS = Object.freeze({
  30: 0.8,
  60: 0.7,
  90: 0.6
});

const FALLBACK_RESTAURANT_RECOMMENDED_WEIGHTS = Object.freeze({
  '제1학생회관': Object.freeze({ 30: 0.9, 60: 0.7, 90: 0.7 }),
  '제2학생회관': Object.freeze({ 30: 0.9, 60: 0.7, 90: 0.6 }),
  '제3학생회관': Object.freeze({ 30: 0.9, 60: 0.6, 90: 0.6 }),
  '상록회관': Object.freeze({ 30: 0.9, 60: 0.8, 90: 0.8 }),
  '생활과학대': Object.freeze({ 30: 0.9, 60: 0.7, 90: 0.6 }),
  '학생생활관': Object.freeze({ 30: 0.6, 60: 0.4, 90: 0.2 })
});

const form = document.getElementById('predictionForm');
const restaurantInput = document.getElementById('restaurant');
const dateInput = document.getElementById('date');
const weightInputs = {
  30: document.getElementById('w30'),
  60: document.getElementById('w60'),
  90: document.getElementById('w90')
};
const resultPanel = document.getElementById('resultPanel');
const summary = document.getElementById('summary');
const predictionCards = document.getElementById('predictionCards');
const overviewGuide = document.getElementById('overviewGuide');
const chart = document.getElementById('chart');
const gridControls = document.getElementById('gridControls');
const restaurantGrid = document.getElementById('restaurantGrid');
const formulaList = document.getElementById('formulaList');
const formulaSelection = document.getElementById('formulaSelection');
const formulaPrimer = document.getElementById('formulaPrimer');
const strategyTables = document.getElementById('strategyTables');
const calculationGuide = document.getElementById('calculationGuide');
const detailControls = document.getElementById('detailControls');
const detailInsights = document.getElementById('detailInsights');
const comparisonRows = document.getElementById('comparisonRows');
const tableHint = document.getElementById('tableHint');
const modeHelp = document.getElementById('modeHelp');
const strategyHelp = document.getElementById('strategyHelp');
const profileHelp = document.getElementById('profileHelp');
const adjustedHelp = document.getElementById('adjustedHelp');
const applyPresetButton = document.getElementById('applyPreset');
const heroTuningRange = document.getElementById('heroTuningRange');
const presetEyebrow = document.getElementById('presetEyebrow');
const presetHint = document.getElementById('presetHint');
const commonPresetValue = document.getElementById('commonPresetValue');
const commonPresetNote = document.getElementById('commonPresetNote');
const restaurantPresetRows = document.getElementById('restaurantPresetRows');
const modeButtons = Array.from(document.querySelectorAll('[data-mode]'));
const strategyButtons = Array.from(document.querySelectorAll('[data-strategy]'));
const profileButtons = Array.from(document.querySelectorAll('[data-profile]'));

const allRestaurants = Array.from(restaurantInput.options).map((option) => option.value);

let commonRecommendedWeights = FALLBACK_COMMON_RECOMMENDED_WEIGHTS;
let restaurantRecommendedWeights = FALLBACK_RESTAURANT_RECOMMENDED_WEIGHTS;
let profileMetadataMap = {};
let tuningWindowMeta = null;
let selectedMode = 'common';
let selectedFormulaStrategy = STRATEGY_RESTAURANT_HORIZON;
let selectedProfile = PROFILE_ALL_DATA;
let lastPresetMode = 'common';
let lastRenderedData = null;
let selectedDetailHorizon = 60;
let selectedDetailModel = MODEL_ADJUSTED;
let selectedDetailView = 'largest';
let selectedGridHorizon = 60;
let lastGridData = [];
let gridRequestId = 0;

dateInput.value = '2026-04-01';
applyMode('common');
setFormulaStrategy(STRATEGY_RESTAURANT_HORIZON);
setProfile(PROFILE_ALL_DATA);
adjustedHelp.textContent = '피크 압력식 기준 설명 화면입니다. 계수는 고정이고, 현재값·평소값·상승량 같은 계산값만 예측 시점마다 다시 계산합니다.';
renderPresetSection();
void initializeMetadata();

restaurantInput.addEventListener('change', () => {
  if (selectedMode === 'restaurant') {
    applyMode('restaurant');
    return;
  }

  updateModeHelp();
  updateApplyPresetButton();
});

for (const button of modeButtons) {
  button.addEventListener('click', () => {
    const { mode } = button.dataset;
    if (mode === 'custom') {
      setMode('custom');
      return;
    }

    applyMode(mode);
  });
}

for (const button of strategyButtons) {
  button.addEventListener('click', () => {
    setFormulaStrategy(button.dataset.strategy || STRATEGY_BANDED);
    if (lastRenderedData) form.requestSubmit();
  });
}

for (const button of profileButtons) {
  button.addEventListener('click', () => {
    setProfile(button.dataset.profile || PROFILE_ALL_DATA, { syncDate: true });
    if (lastRenderedData) form.requestSubmit();
  });
}

for (const input of Object.values(weightInputs)) {
  input.addEventListener('input', () => {
    if (selectedMode !== 'custom') setMode('custom');
  });
}

applyPresetButton.addEventListener('click', () => {
  applyMode(lastPresetMode);
});

resultPanel.addEventListener('click', (event) => {
  const card = event.target.closest('[data-card-horizon]');
  if (card && lastRenderedData) {
    selectedDetailHorizon = Number(card.dataset.cardHorizon);
    selectedGridHorizon = selectedDetailHorizon;
    renderDetailSection();
    renderFormulaSectionPresentation(lastRenderedData);
    renderRestaurantGrid();
    return;
  }

  const horizonButton = event.target.closest('[data-detail-horizon]');
  if (horizonButton && lastRenderedData) {
    selectedDetailHorizon = Number(horizonButton.dataset.detailHorizon);
    renderDetailSection();
    renderFormulaSectionPresentation(lastRenderedData);
    return;
  }

  const modelButton = event.target.closest('[data-detail-model]');
  if (modelButton && lastRenderedData) {
    selectedDetailModel = modelButton.dataset.detailModel;
    renderDetailSection();
    renderFormulaSectionPresentation(lastRenderedData);
    return;
  }

  const viewButton = event.target.closest('[data-detail-view]');
  if (viewButton && lastRenderedData) {
    selectedDetailView = viewButton.dataset.detailView;
    renderDetailSection();
    renderFormulaSectionPresentation(lastRenderedData);
    return;
  }

  const gridHorizonButton = event.target.closest('[data-grid-horizon]');
  if (gridHorizonButton) {
    selectedGridHorizon = Number(gridHorizonButton.dataset.gridHorizon);
    renderGridControlsBar();
    renderRestaurantGrid();
  }
});

async function initializeMetadata() {
  try {
    const response = await fetch('/api/restaurants');
    const data = await readJsonResponse(response);
    if (!response.ok) throw new Error(data.message || '튜닝 메타데이터를 불러오지 못했습니다.');
    applyServerMetadata(data);
  } catch (error) {
    adjustedHelp.textContent = `${adjustedHelp.textContent} 서버 메타데이터를 불러오지 못해 기본 추천값으로 표시 중입니다.`;
  }
}

function applyServerMetadata(data) {
  if (data.profiles) {
    profileMetadataMap = data.profiles;
  }

  if (data.recommendedWeights?.common) {
    commonRecommendedWeights = freezeWeightSet(data.recommendedWeights.common);
  }

  if (data.recommendedWeights?.restaurant) {
    restaurantRecommendedWeights = freezeRestaurantWeightSets(data.recommendedWeights.restaurant);
  }

  tuningWindowMeta = data.optimizedSummary?.tuningWindow || null;
  if (tuningWindowMeta?.endDate) {
    dateInput.value = tuningWindowMeta.endDate;
  }

  setFormulaStrategy(STRATEGY_RESTAURANT_HORIZON);

  if (heroTuningRange) {
    heroTuningRange.textContent = tuningWindowMeta?.label ? `${tuningWindowMeta.label} 누적 데이터` : '전체 누적 데이터';
  }

  renderPresetSection();
  applyMode(lastPresetMode);
  setProfile(data.defaultProfile || PROFILE_ALL_DATA, { syncDate: true });
}

function renderPresetSection() {
  if (presetEyebrow) {
    presetEyebrow.textContent = tuningWindowMeta?.label ? `${tuningWindowMeta.label} 튜닝 기준` : '전체 데이터 튜닝 기준';
  }

  if (presetHint) {
    const windowText = tuningWindowMeta?.label ? `${tuningWindowMeta.label} 누적 데이터` : '전체 누적 데이터';
    presetHint.textContent = `현재 추천값은 ${windowText}에서 다시 튜닝한 결과입니다.`;
  }

  if (commonPresetValue) {
    commonPresetValue.textContent = formatWeightTriplet(commonRecommendedWeights);
  }

  if (commonPresetNote) {
    commonPresetNote.textContent = '식당별 예외를 두지 않을 때의 공통 운영 기준입니다.';
  }

  if (restaurantPresetRows) {
    restaurantPresetRows.innerHTML = allRestaurants.map((restaurant) => {
      const weights = restaurantRecommendedWeights[restaurant] || commonRecommendedWeights;
      return `
        <tr>
          <td>${restaurant}</td>
          <td>${formatWeightValue(weights[30])}</td>
          <td>${formatWeightValue(weights[60])}</td>
          <td>${formatWeightValue(weights[90])}</td>
        </tr>
      `;
    }).join('');
  }
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();

  resultPanel.hidden = false;
  summary.innerHTML = '<p class="status">선택 날짜의 실제값과 예측 비교를 불러오는 중입니다.</p>';
  predictionCards.innerHTML = '';
  overviewGuide.innerHTML = '';
  chart.innerHTML = '';
  gridControls.innerHTML = '';
  restaurantGrid.innerHTML = '';
  formulaList.innerHTML = '';
  formulaSelection.textContent = '';
  formulaPrimer.innerHTML = '';
  strategyTables.innerHTML = '';
  calculationGuide.innerHTML = '';
  detailControls.innerHTML = '';
  detailInsights.innerHTML = '';
  comparisonRows.innerHTML = '';
  tableHint.textContent = '';

  const params = new URLSearchParams({
    restaurant: restaurantInput.value,
    date: dateInput.value,
    profile: selectedProfile,
    mode: selectedMode,
    strategy: STRATEGY_RESTAURANT_HORIZON,
    w30: weightInputs[30].value,
    w60: weightInputs[60].value,
    w90: weightInputs[90].value,
    rowsLimit: String(DEFAULT_ROWS_LIMIT)
  });

  try {
    const response = await fetch(`/api/history?${params}`);
    const data = await readJsonResponse(response);
    if (!response.ok) throw new Error(data.message || '비교 데이터를 불러오지 못했습니다.');
    render(data);
    simplifyPresentationV2(data);
  } catch (error) {
    summary.innerHTML = `<p class="status status--error">${error.message}</p>`;
  }
});

function render(data) {
  lastRenderedData = data;
  selectedDetailHorizon = pickDefaultDetailHorizon(data.horizons);
  selectedGridHorizon = selectedDetailHorizon;
  selectedDetailModel = data.comparisonSummary.preferredModel || MODEL_ADJUSTED;
  selectedDetailView = 'largest';
  lastGridData = [];
  setFormulaStrategy(STRATEGY_RESTAURANT_HORIZON);
  tuningWindowMeta = data.parameters.tuningWindow || tuningWindowMeta;
  if (data.parameters.recommendations?.common) {
    commonRecommendedWeights = freezeWeightSet(data.parameters.recommendations.common);
  }
  if (data.parameters.recommendations?.restaurantAll) {
    restaurantRecommendedWeights = freezeRestaurantWeightSets(data.parameters.recommendations.restaurantAll);
  }
  renderPresetSection();
  updateModeHelp();

  adjustedHelp.textContent = `${data.parameters.formulaStrategy.label} · ${data.parameters.optimized.formulaLabel} · 고정 계수 + 실시간 계산값`;
  const qualitySummary = data.quality
    ? `원본 ${numberFormat.format(data.quality.rawCount)}건 중 품질 이상 ${numberFormat.format(data.quality.qualityRemovedCount)}건, 비활성 시간대 ${numberFormat.format(data.quality.inactiveSlotRemovedCount)}건을 제외하고 ${numberFormat.format(data.quality.keptCount)}건으로 비교했습니다.`
    : '';

  const bestBase = data.horizons.reduce((best, item) => (
    item.models.base.mae < best.models.base.mae ? item : best
  ), data.horizons[0]);
  const bestAdjusted = data.horizons.reduce((best, item) => (
    item.models.adjusted.mae < best.models.adjusted.mae ? item : best
  ), data.horizons[0]);

  summary.innerHTML = `
    <div class="summary-head">
      <div>
        <p class="eyebrow eyebrow--small">날짜 확인 결과</p>
        <h2>${data.restaurant} · ${data.date}</h2>
      </div>
      <div class="summary-badges">
        <span class="tag">${data.baselineMode}</span>
        <span class="tag">${data.season}</span>
        <span class="tag">${modeName(data.parameters.mode)}</span>
        <span class="tag">${data.parameters.formulaStrategy.label}</span>
      </div>
    </div>
    <p class="summary-copy">
      기본식 반영률은 <strong>${formatWeightSummary(data.parameters.weights)}</strong>이고,
      조정식은 모두 <strong>${data.formulas.optimized.label}</strong>으로 통일했고,
      <strong>${data.parameters.tuningWindow?.label || '전체 데이터'} 누적 데이터</strong>에서
      <strong>${data.parameters.formulaStrategy.label}</strong> 구조로 계수만 다시 학습해 씁니다.
      점심 진입과 점심 피크는 상위 혼잡 구간 과소예측 벌점까지 함께 반영해 피크 지연을 줄입니다.
    </p>
    <p class="summary-note">
      기본식 최소 MAE는 <strong>${bestBase.horizonMinutes}분 ${formatOneDecimal(bestBase.models.base.mae)}명</strong>,
      최적화식 최소 MAE는 <strong>${bestAdjusted.horizonMinutes}분 ${formatOneDecimal(bestAdjusted.models.adjusted.mae)}명</strong>입니다.
      ${buildHorizonObservationText(data.horizons)} ${buildComparisonSummaryText(data.comparisonSummary)}
    </p>
    <p class="summary-mini">${data.parameters.validationNote} · ${data.parameters.inspectionNote}${qualitySummary ? ` · ${qualitySummary}` : ''}</p>
  `;

  predictionCards.innerHTML = data.horizons.map((item) => {
    const comparison = item.comparison;
    const isFocus = item.horizonMinutes === selectedDetailHorizon;
    const worstBand = item.models.adjusted.timeBands[0] || item.models.base.timeBands[0];

    return `
      <article class="card card--comparison ${isFocus ? 'card--focus' : ''}" data-card-horizon="${item.horizonMinutes}">
        <div class="card-top">
          <span class="card-horizon">${item.horizonMinutes}분 후</span>
          <span class="chip">w ${formatWeightValue(item.weight)}</span>
        </div>
        <div class="compare-stack">
          <div class="compare-metric">
            <span>기본식 MAE</span>
            <strong>${formatOneDecimal(item.models.base.mae)}명</strong>
          </div>
          <div class="compare-metric compare-metric--accent">
            <span>최적화식 MAE</span>
            <strong>${formatOneDecimal(item.models.adjusted.mae)}명</strong>
          </div>
        </div>
        <div class="metric-grid metric-grid--triple">
          <div>
            <span>MAE 변화</span>
            <strong class="${deltaTone(comparison.maeDelta)}">${formatSignedNumber(comparison.maeDelta, '명')}</strong>
          </div>
          <div>
            <span>10명 이내 변화</span>
            <strong class="${deltaTone(-comparison.within10RateDelta)}">${formatSignedNumber(comparison.within10RateDelta, '%p')}</strong>
          </div>
          <div>
            <span>비교 건수</span>
            <strong>${numberFormat.format(item.models.adjusted.count)}건</strong>
          </div>
        </div>
        <p class="card-rule">${item.models.adjusted.rule}</p>
        <p class="card-emphasis">${buildCardTakeaway(item, worstBand)}</p>
        <p class="card-note">기본식 10명 이내 ${formatPercent(item.models.base.within10Rate)} · 최적화식 10명 이내 ${formatPercent(item.models.adjusted.within10Rate)}</p>
      </article>
    `;
  }).join('');

  overviewGuide.innerHTML = `
    ${renderOverviewCard('Step 1', '상세 비교 한 행', '한 행은 특정 기준 시각에서 30분·60분·90분 뒤를 예측한 뒤, 같은 날의 실제 목표 시각 값과 대조한 1건입니다. 하루 안에 기준 시각이 많아서 행 수가 많아집니다.')}
    ${renderOverviewCard('Step 2', '운영식 선택 방식', data.parameters.formulaStrategy.key === STRATEGY_RESTAURANT_HORIZON ? '조정식 종류는 피크 압력식 하나로 고정하고, 전체 누적 데이터로 식당별·30/60/90분별 계수만 다시 학습합니다. 날짜와 시간대가 바뀌어도 같은 식당의 같은 분 구간은 같은 피크 압력식을 사용합니다.' : '조정식 종류는 피크 압력식 하나로 고정하고, 전체 누적 데이터로 식당별·분별·목표 시간대별 계수만 다시 학습합니다. 날짜를 바꿔도 그 구간의 피크 압력식은 그대로 적용합니다.')}
    ${renderOverviewCard('Step 3', '피크와 데이터 품질 보정', data.restaurant === '제3학생회관' ? '제3학생회관은 점심 피크 상승이 빨라 기본식만 쓰면 늦게 따라가는 경우가 있어, 점심 진입/피크는 과소예측 벌점을 함께 넣어 반응형 식을 더 우선시합니다. 여기에 고립된 0·급반등 값·거의 운영되지 않는 시간대도 같이 제외합니다.' : '점심 진입과 점심 피크는 혼잡도가 급격히 바뀌므로 MAE만 보면 피크를 늦게 따라가는 식이 남을 수 있습니다. 그래서 상위 혼잡 구간 과소예측을 추가 벌점으로 반영하고, 고립된 0·급반등 값·비활성 시간대는 비교에서 제외합니다.') }
  `;

  chart.innerHTML = data.horizons.map((item) => `
    <section class="mini-chart">
      <div class="mini-chart__head">
        <div>
          <h3>${item.horizonMinutes}분 후 실제값과 예측값 비교</h3>
          <p>기본식 MAE ${formatOneDecimal(item.models.base.mae)}명 · 최적화식 MAE ${formatOneDecimal(item.models.adjusted.mae)}명 · 변화 ${formatSignedNumber(item.comparison.maeDelta, '명')}</p>
        </div>
        <div class="legend">
          <span class="legend-item"><i class="legend-line legend-line--actual"></i>실제값</span>
          <span class="legend-item"><i class="legend-line legend-line--base"></i>기본식</span>
          <span class="legend-item"><i class="legend-line legend-line--adjusted"></i>최적화식</span>
          <span class="legend-item"><i class="legend-line legend-line--gap"></i>실제-최적화 오차</span>
        </div>
      </div>
      ${buildChartSvg(buildChartPoints(item, 72), { width: 960, height: 320, compact: false })}
    </section>
  `).join('');

  renderGridControlsBar();
  restaurantGrid.innerHTML = '<p class="status status--soft">선택 날짜의 전체 식당 그래프를 불러오는 중입니다.</p>';
  renderDetailSection();
  renderFormulaSectionPresentation(data);
  void loadRestaurantGrid(data);
}

function renderOverviewCard(step, title, copy) {
  return `
    <article class="overview-card">
      <span class="overview-card__step">${step}</span>
      <h3 class="overview-card__title">${title}</h3>
      <p class="overview-card__copy">${copy}</p>
    </article>
  `;
}

function renderFormulaSectionEnhanced(data) {
  const baseFormula = {
    key: FORMULA_BASE,
    label: '기본식',
    shortLabel: 'base',
    formula: data.formulas.base,
    note: '현재 차이를 고정 반영률로 유지하는 가장 단순한 기준식입니다.'
  };
  const optimizedFormula = data.formulas.optimized;
  const formulas = [baseFormula, optimizedFormula];
  formulaList.innerHTML = formulas.map((family) => `
    <article class="formula-card">
      <div class="formula-card__top">
        <span class="formula-card__label">${family.label}</span>
        <span class="formula-card__short">${family.shortLabel}</span>
      </div>
      <code>${family.formula}</code>
      <p class="formula-card__note">${family.note}</p>
    </article>
  `).join('');

  formulaSelection.textContent = `${data.formulas.selection}. 현재 확인 중인 운영 구조는 ${data.parameters.formulaStrategy.label}이고, 조정식 종류는 전부 ${optimizedFormula.label}입니다. ${data.parameters.searchNote} ${data.parameters.selectionNote}`;
  formulaPrimer.innerHTML = renderFormulaPrimer(data.formulas);
  renderStrategyTablesV2();
}

function renderFormulaPrimer(formulas) {
  const featureRows = (formulas.featureDocs || []).map((feature) => `
    <tr>
      <td>${feature.label}</td>
      <td>${feature.meaning}</td>
      <td>${feature.detail}</td>
    </tr>
  `).join('');

  return `
    <div class="formula-primer__head">
      <h3>피크 압력식 변수 해설</h3>
      <p class="hint">절편과 각 항은 따로 손으로 정하는 값이 아니라, 튜닝 데이터에서 오차가 최소가 되도록 같이 학습됩니다.</p>
    </div>
    <div class="formula-primer__cards">
      <article class="primer-card">
        <span class="primer-card__label">절편은 어떻게 구하나</span>
        <p>${formulas.training.interceptNote}</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">계수 학습식</span>
        <p><code>${formulas.training.objective}</code></p>
        <p>여기서 λ는 <strong>${formulas.training.lambda}</strong>이고, ${formulas.training.fitNote}</p>
      </article>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact">
        <thead>
          <tr>
            <th>항</th>
            <th>무슨 뜻인가</th>
            <th>왜 넣는가</th>
          </tr>
        </thead>
        <tbody>
          ${featureRows}
        </tbody>
      </table>
    </div>
  `;
}

function renderStrategyTables(strategyData, activeStrategyKey) {
  if (!strategyData) {
    strategyTables.innerHTML = '';
    return;
  }

  const comparisonCards = (strategyData.comparisonByHorizon || []).map((item) => `
    <article class="strategy-compare-card">
      <span class="strategy-compare-card__label">${item.horizonMinutes}분 후</span>
      <strong>${formatOneDecimal(item.bandedMae)} → ${formatOneDecimal(item.restaurantHorizonMae)}명</strong>
      <p>단순화 손실 ${formatSignedNumber(item.delta, '명')}</p>
    </article>
  `).join('');

  strategyTables.innerHTML = `
    <section class="strategy-block">
      <div class="section-head section-head--tight">
        <div>
          <h3>고정식 구조 비교</h3>
          <p class="hint">현재 선택은 <strong>${strategyName(activeStrategyKey)}</strong>입니다. 아래 표로 시간대별 상세안과 식당×분 단순안을 같이 비교할 수 있습니다.</p>
        </div>
      </div>
      <div class="strategy-compare-grid">
        ${comparisonCards}
      </div>
    </section>

    <section class="strategy-block">
      <div class="section-head section-head--tight">
        <div>
          <h3>시간대별 고정식 표</h3>
          <p class="hint">식당 × 30/60/90분 × 목표 시간대별로 어떤 식이 고정됐는지 보여줍니다.</p>
        </div>
      </div>
      <div class="table-wrap table-wrap--plain">
        <table class="mapping-table">
          <thead>
            <tr>
              <th>식당</th>
              <th>예측</th>
              ${TIME_BANDS.map((bandKey) => `<th>${bandLabel(bandKey)}</th>`).join('')}
            </tr>
          </thead>
          <tbody>
            ${(strategyData.banded?.rows || []).map((row) => `
              <tr>
                <td>${row.restaurant}</td>
                <td>${row.horizonMinutes}분</td>
                ${row.bands.map((band) => `
                  <td>
                    <div class="mapping-cell">
                      <strong>${band.formulaShortLabel}</strong>
                      <span>${formatOneDecimal(band.baseMae)}→${formatOneDecimal(band.optimizedMae)}</span>
                    </div>
                  </td>
                `).join('')}
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </section>

    <section class="strategy-block">
      <div class="section-head section-head--tight">
        <div>
          <h3>식당×분 고정식 표</h3>
          <p class="hint">시간대를 구분하지 않고, 식당별 30/60/90분마다 하나의 고정식만 쓰는 단순 운영안입니다.</p>
        </div>
      </div>
      <div class="table-wrap table-wrap--plain">
        <table class="mapping-table mapping-table--compact">
          <thead>
            <tr>
              <th>식당</th>
              <th>예측</th>
              <th>선택 식</th>
              <th>튜닝 MAE</th>
              <th>개선</th>
              <th>비교 건수</th>
            </tr>
          </thead>
          <tbody>
            ${(strategyData.restaurantHorizon?.rows || []).map((row) => `
              <tr>
                <td>${row.restaurant}</td>
                <td>${row.horizonMinutes}분</td>
                <td><strong>${row.formulaShortLabel}</strong><br><span class="mapping-sub">${row.formulaLabel}</span></td>
                <td>${formatOneDecimal(row.baseMae)}→${formatOneDecimal(row.optimizedMae)}</td>
                <td class="${deltaTone(-row.gain)}">${formatSignedNumber(row.gain, '명')}</td>
                <td>${numberFormat.format(row.sampleCount)}건</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

async function loadRestaurantGrid(data) {
  const requestId = ++gridRequestId;
  const paramsByRestaurant = allRestaurants.map((restaurant) => {
    const params = new URLSearchParams({
      restaurant,
      date: data.date,
      profile: selectedProfile,
      mode: selectedMode,
      strategy: selectedFormulaStrategy,
      w30: weightInputs[30].value,
      w60: weightInputs[60].value,
      w90: weightInputs[90].value,
      rowsLimit: String(GRID_ROWS_LIMIT)
    });

    return { restaurant, params };
  });

  const results = await Promise.allSettled(paramsByRestaurant.map(async ({ restaurant, params }) => {
    const response = await fetch(`/api/history?${params}`);
    const payload = await readJsonResponse(response);
    if (!response.ok) throw new Error(payload.message || `${restaurant} 데이터를 불러오지 못했습니다.`);
    return payload;
  }));

  if (requestId !== gridRequestId) return;

  const successMap = new Map();
  const failed = [];
  for (const result of results) {
    if (result.status === 'fulfilled') {
      successMap.set(result.value.restaurant, result.value);
    } else {
      failed.push(result.reason.message);
    }
  }

  lastGridData = allRestaurants
    .map((restaurant) => successMap.get(restaurant))
    .filter(Boolean);

  if (!lastGridData.length) {
    restaurantGrid.innerHTML = `<p class="status status--error">${failed[0] || '전체 식당 데이터를 불러오지 못했습니다.'}</p>`;
    return;
  }

  renderRestaurantGrid(failed);
}

function renderGridControlsBar() {
  gridControls.innerHTML = `
    <div class="segmented" role="tablist" aria-label="전체 식당 그래프 구간 선택">
      ${HORIZONS.map((horizon) => `
        <button type="button" class="segmented-button ${selectedGridHorizon === horizon ? 'is-active' : ''}" data-grid-horizon="${horizon}">
          ${horizon}분 후
        </button>
      `).join('')}
    </div>
  `;
}

function renderRestaurantGrid(failed = []) {
  if (!lastGridData.length) return;

  const cards = lastGridData.map((data) => {
    const horizon = data.horizons.find((item) => item.horizonMinutes === selectedGridHorizon) || data.horizons[0];
    const points = buildChartPoints(horizon, 36);
    const comparison = horizon.comparison;
    const mainFormula = pickMainFormulaLabel(horizon.models.adjusted.rows);

    return `
      <article class="grid-card">
        <div class="grid-card__head">
          <div class="grid-card__title">
            <h3>${data.restaurant}</h3>
            <p>${selectedGridHorizon}분 후 · ${mainFormula}</p>
          </div>
          <span class="tag">${comparisonImpactLabel(comparison.maeDelta)}</span>
        </div>
        <div class="grid-metrics">
          <div>
            <span>기본식 MAE</span>
            <strong>${formatOneDecimal(horizon.models.base.mae)}명</strong>
          </div>
          <div>
            <span>최적화식 MAE</span>
            <strong class="${deltaTone(comparison.maeDelta)}">${formatOneDecimal(horizon.models.adjusted.mae)}명</strong>
          </div>
          <div>
            <span>MAE 변화</span>
            <strong class="${deltaTone(comparison.maeDelta)}">${formatSignedNumber(comparison.maeDelta, '명')}</strong>
          </div>
        </div>
        <div class="compact-chart">
          ${buildChartSvg(points, { width: 420, height: 200, compact: true })}
        </div>
        <p class="grid-card__note">${horizon.models.adjusted.rule}</p>
      </article>
    `;
  }).join('');

  const failureText = failed.length
    ? `<p class="status status--error">${failed.length}개 식당 데이터는 불러오지 못했습니다. 첫 오류: ${failed[0]}</p>`
    : '';

  restaurantGrid.innerHTML = `${failureText}${cards}`;
}

function renderDetailSection() {
  if (!lastRenderedData) return;

  const horizon = lastRenderedData.horizons.find((item) => item.horizonMinutes === selectedDetailHorizon) || lastRenderedData.horizons[0];
  const model = horizon.models[selectedDetailModel];
  const activeRows = selectedDetailView === 'largest' ? model.largestErrors : model.rows;
  const worstBand = model.timeBands[0];

  for (const card of predictionCards.querySelectorAll('.card')) {
    card.classList.toggle('card--focus', Number(card.dataset.cardHorizon) === horizon.horizonMinutes);
  }

  detailControls.innerHTML = `
    <div class="detail-toolbar">
      <div class="segmented" role="tablist" aria-label="예측 구간 선택">
        ${lastRenderedData.horizons.map((item) => `
          <button type="button" class="segmented-button ${item.horizonMinutes === horizon.horizonMinutes ? 'is-active' : ''}" data-detail-horizon="${item.horizonMinutes}">
            ${item.horizonMinutes}분 후
          </button>
        `).join('')}
      </div>
      <div class="segmented" role="tablist" aria-label="모델 선택">
        <button type="button" class="segmented-button ${selectedDetailModel === MODEL_BASE ? 'is-active' : ''}" data-detail-model="${MODEL_BASE}">기본식</button>
        <button type="button" class="segmented-button ${selectedDetailModel === MODEL_ADJUSTED ? 'is-active' : ''}" data-detail-model="${MODEL_ADJUSTED}">최적화식</button>
      </div>
      <div class="segmented" role="tablist" aria-label="상세 보기 방식">
        <button type="button" class="segmented-button ${selectedDetailView === 'largest' ? 'is-active' : ''}" data-detail-view="largest">오차 큰 순</button>
        <button type="button" class="segmented-button ${selectedDetailView === 'timeline' ? 'is-active' : ''}" data-detail-view="timeline">시간 순 샘플</button>
      </div>
    </div>
  `;

  detailInsights.innerHTML = `
    ${renderInsightCard('평균 편향', formatBiasValue(model.bias), `과소예측 ${formatPercent(model.underPredictRate)} · 과대예측 ${formatPercent(model.overPredictRate)}`, biasClassName(model.bias))}
    ${renderInsightCard('규칙 구성', buildMixHeadline(model, horizon), buildMixNote(model), selectedDetailModel === MODEL_ADJUSTED ? 'tone-cool' : '')}
    ${renderInsightCard('오차 집중 시간대', worstBand ? worstBand.label : '데이터 부족', worstBand ? `MAE ${formatOneDecimal(worstBand.mae)}명 · 비교 ${numberFormat.format(worstBand.count)}건` : '집계 대상이 없습니다.')}
    ${renderInsightCard('최대 과대/과소', extremeTitle(model.extremes.worstOver || model.extremes.worstUnder, '대표 오차 없음'), buildExtremePairNote(model))}
  `;

  comparisonRows.innerHTML = activeRows.map((row) => `
    <tr>
      <td>${row.time}</td>
      <td>${row.targetTime}</td>
      <td>${numberFormat.format(row.currentValue)}명</td>
      <td>${numberFormat.format(row.actual)}명</td>
      <td>${numberFormat.format(row.predicted)}명</td>
      <td><span class="badge ${row.error <= 10 ? 'badge--good' : 'badge--warn'}">${numberFormat.format(row.error)}명</span></td>
      <td><span class="badge ${decisionBadgeClass(row.signedError)}">${decisionLabel(row.signedError)}</span></td>
      <td>${adjustmentSummary(row, selectedDetailModel)}</td>
    </tr>
  `).join('');

  if (!activeRows.length) {
    comparisonRows.innerHTML = `
      <tr>
        <td colspan="8" class="empty-cell">표시할 비교 rows가 없습니다.</td>
      </tr>
    `;
  }

  renderCalculationGuideV2(horizon);

  if (selectedDetailView === 'largest') {
    tableHint.textContent = `${model.label} 기준 전체 비교 ${numberFormat.format(model.count)}건 중 오차가 큰 ${numberFormat.format(activeRows.length)}건을 보여줍니다.`;
    return;
  }

  tableHint.textContent = model.isSampled
    ? `${model.label} 시간 순 rows는 응답량을 줄이기 위해 ${numberFormat.format(model.returnedRowCount)}건만 표시합니다.`
    : `${model.label} 시간 순 비교 rows를 모두 보여주고 있습니다.`;
}

function renderCalculationGuide(horizon) {
  const baseRow = pickExampleRow(horizon.models.base.rows);
  const optimizedRow = pickExampleRow(horizon.models.adjusted.rows);
  const row = selectedDetailModel === MODEL_BASE ? baseRow : optimizedRow || baseRow;

  if (!row) {
    calculationGuide.innerHTML = `
      <article class="guide-card">
        <span class="guide-label">계산 해설</span>
        <p class="guide-copy">현재 응답에는 예시 계산에 쓸 rows가 없습니다. 다시 비교를 실행하면 이 구간의 실제 숫자로 계산 과정을 보여줍니다.</p>
      </article>
    `;
    return;
  }

  calculationGuide.innerHTML = `
    <div class="guide-grid">
      <article class="guide-card">
        <span class="guide-label">상세 비교 1행의 의미</span>
        <p class="guide-copy">
          기준 시각 <strong>${row.time}</strong> / 목표 시각 <strong>${row.targetTime}</strong>은
          "${row.time}에 ${horizon.horizonMinutes}분 뒤를 예측하고, 같은 날 ${row.targetTime} 실제값과 대조한 1건"을 뜻합니다.
        </p>
      </article>
      <article class="guide-card">
        <span class="guide-label">왜 60분 · 90분이 더 어렵나</span>
        <p class="guide-copy">
          현재 시각과 목표 시각 사이에 점심 진입, 피크, 종료처럼 흐름이 크게 바뀌는 구간이 끼면 현재 혼잡도를 그대로 끌고 가기 어렵습니다.
          그래서 최적화식은 피크 압력식을 기본으로 두고, 점심 구간은 과소예측 벌점까지 함께 반영해 상승 구간을 더 빨리 따라가도록 합니다.
        </p>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">현재 선택 구간 예시 계산</span>
        ${selectedDetailModel === MODEL_BASE ? renderBaseGuide(row, horizon) : renderOptimizedGuide(row, horizon)}
      </article>
    </div>
  `;
}

function renderBaseGuide(row, horizon) {
  return `
    <p class="guide-copy">${row.time}에 ${horizon.horizonMinutes}분 뒤(${row.targetTime})를 예측한 사례입니다. 기본식은 현재 차이를 고정 반영률로 그대로 끌고 갑니다.</p>
    <code>현재 차이 = ${formatOneDecimal(row.currentValue)} - ${formatOneDecimal(row.currentBaseline)} = ${formatSignedNumber(row.currentGap, '')}</code>
    <code>기본식 = ${formatOneDecimal(row.targetBaseline)} + (${formatSignedNumber(row.currentGap, '')}) × ${formatFactor(row.weight)} = ${formatOneDecimal(row.basePrediction)} → ${numberFormat.format(row.predicted)}</code>
    <p class="guide-copy">실제값은 ${numberFormat.format(row.actual)}명이고, 기본식 오차는 ${numberFormat.format(row.error)}명입니다.</p>
  `;
}

function renderOptimizedGuide(row, horizon) {
  const strategyKey = lastRenderedData?.parameters?.formulaStrategy?.key || STRATEGY_BANDED;
  const scopeText = strategyKey === STRATEGY_RESTAURANT_HORIZON
    ? `${horizon.horizonMinutes}분 구간 전체에서는`
    : `${row.targetBandLabel}에서는`;

  if (!row.optimizedFormulaKey || row.optimizedFormulaKey === FORMULA_BASE || !row.coefficients || !row.featureValues) {
    return `
      <p class="guide-copy">${scopeText} 전체 데이터 튜닝 기준으로 기본식이 가장 안정적이라, 최적화식도 기본식을 그대로 사용합니다.</p>
      <code>기본식 = ${formatOneDecimal(row.targetBaseline)} + (${formatSignedNumber(row.currentGap, '')}) × ${formatFactor(row.weight)} = ${formatOneDecimal(row.basePrediction)} → ${numberFormat.format(row.predicted)}</code>
      <p class="guide-copy">실제값은 ${numberFormat.format(row.actual)}명이고, ${strategyKey === STRATEGY_RESTAURANT_HORIZON ? '이 분 구간은' : '이 시간대는'} <strong>기본식 유지</strong>가 고정 운영 전략입니다.</p>
    `;
  }

  const coefficientParts = Object.entries(row.coefficients).map(([key, value]) => `${featureLabel(key)}×${formatCoefficient(value)}`);
  const valueParts = Object.entries(row.featureValues).map(([key, value]) => `${featureLabel(key)}=${formatOneDecimal(value)}`);
  const contributionParts = Object.entries(row.contributions).map(([key, value]) => `${featureLabel(key)} ${formatSignedNumber(value, '')}`);

  return `
    <p class="guide-copy">${scopeText} <strong>${row.optimizedFormulaLabel}</strong> 계수를 학습한 고정 운영식입니다. 전체 데이터 튜닝에서 ${strategyKey === STRATEGY_RESTAURANT_HORIZON ? '이 분 구간 전체' : '이 시간대'}의 평균 오차와 피크 과소예측을 함께 반영해 학습된 식입니다.</p>
    <code>${row.optimizedFormulaLabel} = ${coefficientParts.join(' + ')}</code>
    <code>이번 사례 입력값 = ${valueParts.join(' / ')}</code>
    <code>계수 반영 합계 = ${contributionParts.join(' + ')} = ${formatOneDecimal(row.rawPrediction)} → ${numberFormat.format(row.predicted)}</code>
    <p class="guide-copy">실제값은 ${numberFormat.format(row.actual)}명이고, 기본식 대비 보정량은 ${formatSignedNumber(row.adjustmentTotal, '명')}입니다.</p>
  `;
}

function renderInsightCard(label, value, note, tone = '') {
  return `
    <article class="insight-card">
      <span class="insight-card__label">${label}</span>
      <strong class="insight-card__value ${tone}">${value}</strong>
      <p class="insight-card__note">${note}</p>
    </article>
  `;
}

async function readJsonResponse(response) {
  const contentType = response.headers.get('content-type') || '';
  const body = await response.text();

  if (!contentType.includes('application/json')) {
    const trimmed = body.trim().toLowerCase();
    const hint = trimmed.startsWith('<!doctype') || trimmed.startsWith('<html')
      ? 'HTML 문서가 응답됐습니다. 반드시 proposal_viewer에서 npm start를 실행한 뒤 http://localhost:4321로 접속했는지 확인하세요.'
      : 'JSON이 아닌 응답을 받았습니다.';
    throw new Error(`${hint} 요청 주소: ${response.url}`);
  }

  return JSON.parse(body);
}

function buildChartSvg(points, options) {
  if (!points.length) return '<p class="empty">비교할 rows가 없습니다.</p>';

  const width = options.width;
  const height = options.height;
  const compact = Boolean(options.compact);
  const pad = compact
    ? { left: 42, right: 16, top: 18, bottom: 28 }
    : { left: 58, right: 26, top: 26, bottom: 42 };
  const values = points.flatMap((point) => [point.actual, point.basePredicted, point.adjustedPredicted]);
  const min = Math.min(...values, 0);
  const max = Math.max(...values, 1);
  const plotWidth = width - pad.left - pad.right;
  const plotHeight = height - pad.top - pad.bottom;
  const ticks = buildTicks(min, max, compact ? 3 : 4);
  const actualPath = pathFor(points, 'actual', min, max, plotWidth, plotHeight, pad);
  const basePath = pathFor(points, 'basePredicted', min, max, plotWidth, plotHeight, pad);
  const adjustedPath = pathFor(points, 'adjustedPredicted', min, max, plotWidth, plotHeight, pad);
  const markerEvery = compact ? Math.max(1, Math.floor(points.length / 6)) : Math.max(1, Math.floor(points.length / 14));
  const gapEvery = compact ? Math.max(1, Math.floor(points.length / 10)) : Math.max(1, Math.floor(points.length / 24));

  const gapLines = points.map((point, index) => {
    if (index % gapEvery !== 0 && index !== points.length - 1) return '';
    const x = scaleX(index, points.length, plotWidth, pad.left);
    const y1 = scaleY(point.actual, min, max, plotHeight, pad.top);
    const y2 = scaleY(point.adjustedPredicted, min, max, plotHeight, pad.top);
    return `<line x1="${x}" y1="${y1}" x2="${x}" y2="${y2}" stroke="rgba(127, 29, 29, 0.16)" stroke-width="${compact ? 4 : 6}" stroke-linecap="round"/>`;
  }).join('');

  const markers = points.map((point, index) => {
    if (index % markerEvery !== 0 && index !== points.length - 1) return '';
    const x = scaleX(index, points.length, plotWidth, pad.left);
    const actualY = scaleY(point.actual, min, max, plotHeight, pad.top);
    const adjustedY = scaleY(point.adjustedPredicted, min, max, plotHeight, pad.top);
    return `
      <circle cx="${x}" cy="${actualY}" r="${compact ? 3.3 : 4.5}" fill="#0f766e" stroke="#f7f5ee" stroke-width="2"/>
      <circle cx="${x}" cy="${adjustedY}" r="${compact ? 3.3 : 4.5}" fill="#7f1d1d" stroke="#f7f5ee" stroke-width="2"/>
    `;
  }).join('');

  return `
    <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="실제값과 예측값 비교 그래프">
      <rect x="${pad.left}" y="${pad.top}" width="${plotWidth}" height="${plotHeight}" rx="${compact ? 14 : 20}" fill="#fffdf7"/>
      ${ticks.map((tick) => {
        const y = scaleY(tick, min, max, plotHeight, pad.top);
        return `
          <line x1="${pad.left}" y1="${y}" x2="${width - pad.right}" y2="${y}" stroke="#d8ddd3" stroke-dasharray="4 6"/>
          <text x="${pad.left - 10}" y="${y + 4}" fill="#5d695f" font-size="${compact ? 10 : 12}" text-anchor="end">${formatAxisValue(tick)}</text>
        `;
      }).join('')}
      <line x1="${pad.left}" y1="${height - pad.bottom}" x2="${width - pad.right}" y2="${height - pad.bottom}" stroke="#c4ccbe"/>
      ${gapLines}
      <path d="${actualPath}" fill="none" stroke="#0f766e" stroke-width="${compact ? 3 : 4}" stroke-linejoin="round" stroke-linecap="round"/>
      <path d="${basePath}" fill="none" stroke="#d97706" stroke-width="${compact ? 2.8 : 3.5}" stroke-dasharray="10 8" stroke-linejoin="round" stroke-linecap="round"/>
      <path d="${adjustedPath}" fill="none" stroke="#7f1d1d" stroke-width="${compact ? 3.2 : 4.2}" stroke-linejoin="round" stroke-linecap="round"/>
      ${markers}
      ${axisLabels(points, width, height, pad, compact)}
    </svg>
  `;
}

function buildChartPoints(horizon, limit) {
  const baseRows = horizon.models.base.rows || [];
  const adjustedMap = new Map((horizon.models.adjusted.rows || []).map((row) => [`${row.time}|${row.targetTime}`, row]));
  const merged = baseRows.map((row) => {
    const adjustedRow = adjustedMap.get(`${row.time}|${row.targetTime}`) || row;
    return {
      targetTime: row.targetTime,
      actual: row.actual,
      basePredicted: row.predicted,
      adjustedPredicted: adjustedRow.predicted
    };
  });

  return merged.length > limit ? samplePoints(merged, limit) : merged;
}

function pathFor(points, key, min, max, plotWidth, plotHeight, pad) {
  return points.map((point, index) => {
    const x = scaleX(index, points.length, plotWidth, pad.left);
    const y = scaleY(point[key], min, max, plotHeight, pad.top);
    return `${index === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
  }).join(' ');
}

function axisLabels(points, width, height, pad, compact) {
  const labels = [];
  const step = Math.max(1, Math.floor(points.length / (compact ? 4 : 6)));
  const plotWidth = width - pad.left - pad.right;

  for (let index = 0; index < points.length; index += step) {
    const x = scaleX(index, points.length, plotWidth, pad.left);
    labels.push(`<text x="${x.toFixed(1)}" y="${height - (compact ? 10 : 14)}" fill="#5d695f" font-size="${compact ? 10 : 12}" text-anchor="middle">${points[index].targetTime}</text>`);
  }

  if ((points.length - 1) % step !== 0) {
    const x = scaleX(points.length - 1, points.length, plotWidth, pad.left);
    labels.push(`<text x="${x.toFixed(1)}" y="${height - (compact ? 10 : 14)}" fill="#5d695f" font-size="${compact ? 10 : 12}" text-anchor="middle">${points[points.length - 1].targetTime}</text>`);
  }

  return labels.join('');
}

function buildTicks(min, max, count) {
  const safeMax = Math.max(max, min + 1);
  return Array.from({ length: count + 1 }, (_, index) => min + ((safeMax - min) * index) / count);
}

function scaleX(index, total, plotWidth, left) {
  if (total <= 1) return left + plotWidth / 2;
  return left + (index * plotWidth) / (total - 1);
}

function scaleY(value, min, max, plotHeight, top) {
  return top + plotHeight - ((value - min) / (max - min || 1)) * plotHeight;
}

function samplePoints(rows, limit) {
  if (rows.length <= limit) return rows;
  if (limit === 1) return [rows[Math.floor(rows.length / 2)]];

  const sampled = [];
  const lastIndex = rows.length - 1;
  for (let index = 0; index < limit; index += 1) {
    sampled.push(rows[Math.round((index * lastIndex) / (limit - 1))]);
  }
  return sampled;
}

function applyMode(mode) {
  const weights = recommendedWeights(mode, restaurantInput.value);
  for (const horizon of HORIZONS) {
    weightInputs[horizon].value = formatWeightValue(weights[horizon]);
  }

  lastPresetMode = mode;
  setMode(mode);
}

function setMode(mode) {
  selectedMode = mode;
  for (const button of modeButtons) {
    const active = button.dataset.mode === mode;
    button.classList.toggle('is-active', active);
    button.setAttribute('aria-pressed', String(active));
  }

  updateModeHelp();
  updateApplyPresetButton();
}

function setFormulaStrategy(strategy) {
  selectedFormulaStrategy = strategy === STRATEGY_RESTAURANT_HORIZON
    ? STRATEGY_RESTAURANT_HORIZON
    : STRATEGY_BANDED;

  for (const button of strategyButtons) {
    const active = button.dataset.strategy === selectedFormulaStrategy;
    button.classList.toggle('is-active', active);
    button.setAttribute('aria-pressed', String(active));
  }

  updateStrategyHelp();
}

function activeProfileMeta() {
  return profileMetadataMap[selectedProfile] || null;
}

function defaultDateForProfile(profileKey, profileMeta) {
  if (profileKey === PROFILE_TRAIN_TO_2025) return '2026-04-01';
  return profileMeta?.optimizedSummary?.tuningWindow?.endDate || dateInput.value;
}

function setProfile(profile, options = {}) {
  selectedProfile = profile === PROFILE_TRAIN_TO_2025 ? PROFILE_TRAIN_TO_2025 : PROFILE_ALL_DATA;

  for (const button of profileButtons) {
    const active = button.dataset.profile === selectedProfile;
    button.classList.toggle('is-active', active);
    button.setAttribute('aria-pressed', String(active));
  }

  const profileMeta = activeProfileMeta();
  if (profileMeta?.recommendedWeights?.common) {
    commonRecommendedWeights = freezeWeightSet(profileMeta.recommendedWeights.common);
  }
  if (profileMeta?.recommendedWeights?.restaurant) {
    restaurantRecommendedWeights = freezeRestaurantWeightSets(profileMeta.recommendedWeights.restaurant);
  }

  tuningWindowMeta = profileMeta?.optimizedSummary?.tuningWindow || tuningWindowMeta;
  if (options.syncDate) {
    const defaultDate = defaultDateForProfile(selectedProfile, profileMeta);
    if (defaultDate) dateInput.value = defaultDate;
  }

  if (heroTuningRange) {
    heroTuningRange.textContent = tuningWindowMeta?.label ? `${tuningWindowMeta.label} 누적 데이터` : '전체 누적 데이터';
  }

  if (profileHelp) {
    profileHelp.textContent = profileMeta?.profile?.description || 'DB에 있는 전체 누적 데이터로 학습한 뒤 선택 날짜를 확인합니다.';
  }
  adjustedHelp.textContent = `${profileMeta?.profile?.label || '전체 누적 데이터'} 기준으로 고정 계수를 불러와 예측합니다.`;

  renderPresetSection();
  if (selectedMode === 'custom') {
    updateModeHelp();
    updateApplyPresetButton();
  } else {
    applyMode(lastPresetMode);
  }
  updateStrategyHelp();
}

function updateModeHelp() {
  const restaurant = restaurantInput.value;
  const tuningLabel = tuningWindowMeta?.label ? `${tuningWindowMeta.label} 기준` : '전체 데이터 기준';
  if (selectedMode === 'restaurant') {
    modeHelp.textContent = `${restaurant}의 ${tuningLabel} 추천값 ${formatWeightSummary(restaurantRecommendedWeights[restaurant] || commonRecommendedWeights)}`;
    return;
  }

  if (selectedMode === 'custom') {
    modeHelp.textContent = `직접 조정 중입니다. 추천값으로 되돌리려면 "${modeName(lastPresetMode)} 다시 적용" 버튼을 누르세요.`;
    return;
  }

  modeHelp.textContent = `${tuningLabel} 공통 추천값 ${formatWeightSummary(commonRecommendedWeights)}`;
}

function updateStrategyHelp() {
  if (!strategyHelp) return;

  if (selectedFormulaStrategy === STRATEGY_RESTAURANT_HORIZON) {
    strategyHelp.textContent = '조정식은 전부 피크 압력식으로 통일하고, 식당별 30/60/90분만 나누는 기본 운영안입니다. 설명과 적용이 가장 쉽습니다.';
    return;
  }

  strategyHelp.textContent = '조정식은 전부 피크 압력식으로 통일하고, 식당별 30/60/90분과 목표 시간대까지 나누는 상세 운영안입니다. 더 잘 맞지만 규칙은 복잡해집니다.';
}

function updateApplyPresetButton() {
  applyPresetButton.textContent = `${modeName(lastPresetMode)} 다시 적용`;
}

function recommendedWeights(mode, restaurant) {
  if (mode === 'restaurant') return restaurantRecommendedWeights[restaurant] || commonRecommendedWeights;
  return commonRecommendedWeights;
}

function strategyName(strategy) {
  if (strategy === STRATEGY_RESTAURANT_HORIZON) return '식당×분 고정식';
  return '시간대별 고정식';
}

function modeName(mode) {
  if (mode === 'restaurant') return '식당별 최적값';
  if (mode === 'custom') return '직접 조정';
  return '공통 추천값';
}

function bandLabel(key) {
  if (key === 'morning') return '오전';
  if (key === 'preLunch') return '점심 진입';
  if (key === 'lunchPeak') return '점심 피크';
  if (key === 'postLunch') return '점심 이후';
  if (key === 'afternoon') return '오후';
  if (key === 'dinner') return '저녁';
  return key;
}

function buildComparisonSummaryText(summaryData) {
  const parts = [];
  if (!summaryData.adjustedWins && !summaryData.baseWins) {
    parts.push('세 구간 모두 MAE 차이가 크지 않았습니다.');
  } else {
    parts.push(`최적화식 우세 ${summaryData.adjustedWins}개 구간, 기본식 우세 ${summaryData.baseWins}개 구간`);
  }
  if (summaryData.biggestGain) parts.push(`가장 큰 개선은 ${summaryData.biggestGain.horizonMinutes}분 ${formatSignedNumber(summaryData.biggestGain.maeDelta, '명')}`);
  if (summaryData.biggestLoss) parts.push(`가장 큰 악화는 ${summaryData.biggestLoss.horizonMinutes}분 ${formatSignedNumber(summaryData.biggestLoss.maeDelta, '명')}`);
  return parts.join(' · ');
}

function buildHorizonObservationText(horizons) {
  const nearTie = horizons.filter((item) => Math.abs(item.comparison.maeDelta) < 0.1).map((item) => `${item.horizonMinutes}분`);
  const best = horizons.slice().sort((left, right) => left.comparison.maeDelta - right.comparison.maeDelta)[0];

  if (!best || Math.abs(best.comparison.maeDelta) < 0.1) {
    return '이번 날짜에서는 세 구간 모두 기본식과 최적화식이 거의 비슷합니다.';
  }

  const parts = best.comparison.maeDelta < 0
    ? [`이번 날짜에서는 ${best.horizonMinutes}분 후에서 최적화식 개선 폭이 가장 큽니다.`]
    : ['이번 날짜에서는 최적화식이 뚜렷한 개선을 만들지 못했습니다.'];
  if (nearTie.length) parts.push(`${nearTie.join(', ')}은 기본식과 거의 비슷합니다.`);
  return parts.join(' ');
}

function pickDefaultDetailHorizon(horizons) {
  return horizons.reduce((worst, item) => {
    const score = Math.max(item.models.base.mae, item.models.adjusted.mae);
    const worstScore = Math.max(worst.models.base.mae, worst.models.adjusted.mae);
    return score > worstScore ? item : worst;
  }, horizons[0]).horizonMinutes;
}

function comparisonImpactLabel(maeDelta) {
  if (Math.abs(maeDelta) < 0.1) return '거의 동일';
  if (maeDelta < -0.7) return '개선 큼';
  if (maeDelta < 0) return '소폭 개선';
  if (maeDelta > 0.7) return '악화 큼';
  return '소폭 악화';
}

function deltaTone(value) {
  if (Math.abs(value) < 0.05) return '';
  return value < 0 ? 'tone-cool' : 'tone-warm';
}

function buildCardTakeaway(item, worstBand) {
  const comparisonText = item.comparison.winner === 'tie'
    ? '두 식이 거의 비슷합니다.'
    : item.comparison.winner === MODEL_ADJUSTED
      ? '최적화식이 더 유리했습니다.'
      : '기본식이 더 유리했습니다.';
  const bandText = worstBand
    ? `오차 집중 시간대는 ${worstBand.label} · MAE ${formatOneDecimal(worstBand.mae)}명입니다.`
    : '오차 집중 시간대 정보는 아직 없습니다.';
  return `${comparisonText} ${bandText}`;
}

function buildMixHeadline(model, horizon) {
  if (model.key === MODEL_BASE) return `고정 반영률 ${formatFactor(horizon.weight)}`;
  return `반응형 고정식 적용 ${formatPercent(model.regressionRate)}`;
}

function buildMixNote(model) {
  if (model.key === MODEL_BASE) return '이 모델은 같은 반영률을 모든 시간대에 사용합니다.';
  return `반응형 식 ${numberFormat.format(model.regressionCount)}건 / 기본식 유지 ${numberFormat.format(model.baseFallbackCount)}건`;
}

function buildExtremePairNote(model) {
  const under = model.extremes.worstUnder ? `과소 ${numberFormat.format(model.extremes.worstUnder.error)}명` : '과소 없음';
  const over = model.extremes.worstOver ? `과대 ${numberFormat.format(model.extremes.worstOver.error)}명` : '과대 없음';
  return `${under} · ${over}`;
}

function extremeTitle(row, fallback) {
  if (!row) return fallback;
  return `${row.time} → ${row.targetTime}`;
}

function decisionLabel(signedError) {
  if (Math.abs(signedError) < 0.5) return '일치';
  return signedError < 0 ? '과소예측' : '과대예측';
}

function decisionBadgeClass(signedError) {
  if (Math.abs(signedError) < 0.5) return 'badge--good';
  return signedError < 0 ? 'badge--warn' : 'badge';
}

function adjustmentSummary(row, modelKey) {
  if (modelKey === MODEL_BASE) return '기본식';
  if (!row.optimizedFormulaKey || row.optimizedFormulaKey === FORMULA_BASE) return '기본식 유지';
  return `${row.optimizedFormulaLabel} · ${formatSignedNumber(row.adjustmentTotal, '명')}`;
}

function pickExampleRow(rows) {
  if (!rows || !rows.length) return null;
  return rows.find((row) => row.optimizedFormulaKey && row.optimizedFormulaKey !== FORMULA_BASE) || rows[Math.floor(rows.length / 2)] || rows[0];
}

function pickMainFormulaLabel(rows) {
  if (!rows || !rows.length) return '고정식 정보 없음';
  const counts = new Map();
  for (const row of rows) {
    const key = row.optimizedFormulaLabel || '기본식';
    counts.set(key, (counts.get(key) || 0) + 1);
  }
  return Array.from(counts.entries()).sort((left, right) => right[1] - left[1])[0][0];
}

function featureLabel(key) {
  if (key === 'intercept') return '절편';
  if (key === 'currentValue') return '현재값';
  if (key === 'targetBaseline') return '목표 평소값';
  if (key === 'gapPos') return '초과차이';
  if (key === 'gapNeg') return '부족차이';
  if (key === 'baselineShiftSigned') return '평소값 이동량';
  if (key === 'baselineShiftAbs') return '|평소값 이동량|';
  if (key === 'trend15') return '15분 추세';
  if (key === 'trend30') return '30분 추세';
  if (key === 'rise15') return '15분 상승';
  if (key === 'rise30') return '30분 상승';
  if (key === 'fall15') return '15분 하강';
  if (key === 'fall30') return '30분 하강';
  if (key === 'baselineShiftPos') return '상승 평소값 이동';
  if (key === 'pressureGap') return '피크압력';
  if (key === 'surge15') return '15분 가속';
  if (key === 'surge30') return '30분 가속';
  return key;
}

function formatWeightSummary(weights) {
  return HORIZONS.map((horizon) => `${horizon}분 ${formatWeightValue(weights[horizon])}`).join(' / ');
}

function formatWeightTriplet(weights) {
  return HORIZONS.map((horizon) => formatWeightValue(weights[horizon])).join(' / ');
}

function freezeWeightSet(weights) {
  return Object.freeze({
    30: Number(weights[30]),
    60: Number(weights[60]),
    90: Number(weights[90])
  });
}

function freezeRestaurantWeightSets(weightMap) {
  return Object.freeze(Object.fromEntries(
    Object.entries(weightMap).map(([restaurant, weights]) => [restaurant, freezeWeightSet(weights)])
  ));
}

function formatWeightValue(value) {
  return Number(value).toFixed(1);
}

function formatFactor(value) {
  return Number(value).toLocaleString('ko-KR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

function formatCoefficient(value) {
  return Number(value).toLocaleString('ko-KR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 3
  });
}

function formatOneDecimal(value) {
  return Number(value).toLocaleString('ko-KR', {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1
  });
}

function formatPercent(value) {
  return `${formatOneDecimal(value)}%`;
}

function formatAxisValue(value) {
  return Number(value).toLocaleString('ko-KR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  });
}

function formatSignedNumber(value, unit) {
  if (Math.abs(value) < 0.05) return `±0.0${unit}`;
  const sign = value > 0 ? '+' : '-';
  return `${sign}${formatOneDecimal(Math.abs(value))}${unit}`;
}

function formatBiasValue(value) {
  if (Math.abs(value) < 0.05) return '거의 중립';
  return value > 0 ? `과대 +${formatOneDecimal(value)}명` : `과소 ${formatOneDecimal(Math.abs(value))}명`;
}

function biasClassName(value) {
  if (Math.abs(value) < 0.05) return '';
  return value > 0 ? 'tone-warm' : 'tone-cool';
}

function renderFormulaSection(data) {
  if (!data) {
    formulaList.innerHTML = '';
    formulaSelection.textContent = '';
    formulaPrimer.innerHTML = '';
    strategyTables.innerHTML = '';
    return;
  }

  const baseFormula = {
    key: FORMULA_BASE,
    label: '기본식',
    shortLabel: 'base',
    formula: data.formulas.base,
    note: '현재값과 현재 시점 평소값의 차이를 반영률 하나로 가져가는 비교 기준식입니다.'
  };
  const optimizedFormula = data.formulas.optimized;
  const formulas = [baseFormula, optimizedFormula];
  const horizon = selectedHorizonRecord(data);
  const exampleRow = pickFormulaExampleRow(horizon);
  const scopeText = buildFormulaScopeText(data, horizon, exampleRow);

  formulaList.innerHTML = formulas.map((family) => `
    <article class="formula-card">
      <div class="formula-card__top">
        <span class="formula-card__label">${family.label}</span>
        <span class="formula-card__short">${family.shortLabel}</span>
      </div>
      <code>${family.formula}</code>
      <p class="formula-card__note">${family.note}</p>
    </article>
  `).join('');

  formulaSelection.textContent = `${scopeText} 기준 설명입니다. 조정식 종류는 모두 ${optimizedFormula.label}이고, 계수는 고정값이며 예측 시점마다 feature 값만 다시 계산합니다. ${data.parameters.searchNote}`;
  formulaPrimer.innerHTML = renderFormulaPrimerEnhanced(data, horizon, exampleRow);
  renderStrategyTables(data.formulaStrategies, data.parameters.formulaStrategy.key);
}

function renderFormulaPrimerEnhanced(data, horizon, row) {
  const featureDocs = featureDocMap(data.formulas.featureDocs || []);
  const featureKeys = data.parameters?.optimized?.featureKeys || Object.keys(row?.coefficients || {});
  const sampleCount = row?.modelSampleCount || horizon?.models?.adjusted?.count || 0;
  const tuningWindowLabel = data.parameters?.tuningWindow?.label || '전체 튜닝 데이터';
  const scopeText = buildFormulaScopeText(data, horizon, row);

  if (!row || !row.coefficients || !row.featureValues || !featureKeys.length) {
    return `
      <div class="formula-primer__head">
        <h3>${scopeText}</h3>
        <p class="hint">현재 응답에는 계수 설명용 예시 행이 없어 기본 수식 설명만 표시합니다.</p>
      </div>
      <div class="formula-primer__cards">
        <article class="primer-card">
          <span class="primer-card__label">계수 선정 기준</span>
          <p><code>${data.formulas.training.objective}</code></p>
          <p>${data.formulas.training.fitNote}</p>
        </article>
        <article class="primer-card">
          <span class="primer-card__label">절편과 규제</span>
          <p>${data.formulas.training.interceptNote}</p>
        </article>
      </div>
    `;
  }

  const coefficientRows = featureKeys.map((key) => {
    const doc = featureDocs.get(key);
    return `
      <tr>
        <td><strong>${coefficientSymbol(featureKeys, key)}</strong></td>
        <td>${featureLabelEnhanced(key)}</td>
        <td>${formatCoefficient(row.coefficients[key] ?? 0)}</td>
        <td>${key === 'intercept' ? '고정 상수항' : '고정 계수'}</td>
        <td>${doc?.detail || doc?.meaning || ''}</td>
      </tr>
    `;
  }).join('');

  const runtimeRows = featureKeys
    .filter((key) => key !== 'intercept')
    .map((key) => {
      const doc = featureDocs.get(key);
      return `
        <tr>
          <td>${featureLabelEnhanced(key)}</td>
          <td><code>${featureRuntimeFormulaText(key, row, horizon)}</code></td>
          <td>${formatOneDecimal(row.featureValues[key] ?? 0)}</td>
          <td>${doc?.meaning || ''}</td>
        </tr>
      `;
    }).join('');

  const coefficientLegend = featureKeys
    .filter((key) => key !== 'intercept')
    .map((key) => `${coefficientSymbol(featureKeys, key)}=${featureLabelEnhanced(key)}`)
    .join(' / ');

  return `
    <div class="formula-primer__head">
      <h3>${scopeText}</h3>
      <p class="hint">${data.formulas.training.scopeNote} 현재 예시 행은 <strong>${row.time}</strong> 기준 <strong>${row.targetTime}</strong>를 예측한 케이스입니다.</p>
    </div>
    <div class="formula-primer__cards formula-primer__cards--wide">
      <article class="primer-card">
        <span class="primer-card__label">1. a, b, c…는 어떻게 정하나</span>
        <p><code>${data.formulas.training.objective}</code></p>
        <p>${tuningWindowLabel} 범위에서 ${numberFormat.format(sampleCount)}건을 학습했고, ${data.formulas.training.fitNote}</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">2. 무엇이 고정값인가</span>
        <p>${data.formulas.training.fixedNote}</p>
        <p>${coefficientLegend}</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">3. 무엇이 매번 다시 계산되나</span>
        <p>${data.formulas.training.dynamicNote}</p>
        <p>${data.restaurant} 평소값 기준은 <strong>${data.parameters.seasonMode}</strong>이고, 기준 시각 평소값과 목표 시각 평소값은 같은 요일·시간대의 과거값으로 계산합니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">4. 절편은 무엇인가</span>
        <p>${data.formulas.training.interceptNote}</p>
      </article>
    </div>
    <div class="formula-callout">
      <strong>중요</strong>
      <p>최종 예측은 기본식에 보정식을 더하는 구조가 아닙니다. 기본식과 피크 압력식을 각각 계산하고, 조정식을 쓰는 경우 최종값은 <strong>피크 압력식 결과</strong>입니다.</p>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>기호</th>
            <th>항</th>
            <th>현재 그룹 계수</th>
            <th>구분</th>
            <th>의미</th>
          </tr>
        </thead>
        <tbody>
          ${coefficientRows}
        </tbody>
      </table>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>이번 예측에서 계산하는 값</th>
            <th>계산식</th>
            <th>값</th>
            <th>설명</th>
          </tr>
        </thead>
        <tbody>
          ${runtimeRows}
        </tbody>
      </table>
    </div>
  `;
}

function renderCalculationGuideEnhanced(horizon) {
  const adjustedRow = pickFormulaExampleRow(horizon);
  const baseRows = horizon?.models?.base?.rows || [];
  const baseRow = findMatchingRow(baseRows, adjustedRow) || pickExampleRow(baseRows);

  if (!adjustedRow || !baseRow) {
    calculationGuide.innerHTML = `
      <article class="guide-card">
        <span class="guide-label">계산 예시</span>
        <p class="guide-copy">현재 응답에는 예시 계산에 사용할 상세 rows가 없습니다. rowsLimit을 늘리거나 다른 날짜를 선택하면 실제 숫자로 계산 과정을 보여줍니다.</p>
      </article>
    `;
    return;
  }

  if (!adjustedRow.coefficients || !adjustedRow.featureValues || adjustedRow.optimizedFormulaKey === FORMULA_BASE) {
    calculationGuide.innerHTML = `
      <div class="guide-grid">
        <article class="guide-card">
          <span class="guide-label">현재 예시</span>
          <p class="guide-copy">${adjustedRow.time} 기준 ${horizon.horizonMinutes}분 후 ${adjustedRow.targetTime}를 예측한 행입니다. 이 그룹은 기본식을 그대로 최종 예측으로 사용합니다.</p>
        </article>
        <article class="guide-card guide-card--wide">
          <span class="guide-label">기본식 계산</span>
          <code>현재 차이 = ${formatOneDecimal(baseRow.currentValue)} - ${formatOneDecimal(baseRow.currentBaseline)} = ${formatSignedNumber(baseRow.currentGap, '')}</code>
          <code>기본식 = ${formatOneDecimal(baseRow.targetBaseline)} + (${formatSignedNumber(baseRow.currentGap, '')}) × ${formatFactor(baseRow.weight)} = ${formatOneDecimal(baseRow.basePrediction)} → ${numberFormat.format(baseRow.predicted)}</code>
          <p class="guide-copy">실제값은 ${numberFormat.format(baseRow.actual)}명이고, 오차는 ${numberFormat.format(baseRow.error)}명입니다.</p>
        </article>
      </div>
    `;
    return;
  }

  const featureKeys = lastRenderedData?.parameters?.optimized?.featureKeys || Object.keys(adjustedRow.coefficients);
  const termCards = featureKeys.map((key) => `
    <article class="term-card">
      <span class="term-card__symbol">${coefficientSymbol(featureKeys, key)}</span>
      <strong>${featureLabel(key)}</strong>
      <p>${contributionTermText(featureKeys, key, adjustedRow)}</p>
      <em>${formatSignedNumber(adjustedRow.contributions[key] ?? 0, '')}</em>
    </article>
  `).join('');

  calculationGuide.innerHTML = `
    <div class="guide-grid guide-grid--formula">
      <article class="guide-card">
        <span class="guide-label">현재 예시 행</span>
        <p class="guide-copy">${lastRenderedData.restaurant}의 <strong>${adjustedRow.time}</strong> 시점에서 <strong>${adjustedRow.targetTime}</strong>를 예측한 행입니다. 실제 목표 시각 값은 <strong>${numberFormat.format(adjustedRow.actual)}명</strong>입니다.</p>
      </article>
      <article class="guide-card">
        <span class="guide-label">기본식 먼저 계산</span>
        <code>현재 차이 = ${formatOneDecimal(baseRow.currentValue)} - ${formatOneDecimal(baseRow.currentBaseline)} = ${formatSignedNumber(baseRow.currentGap, '')}</code>
        <code>기본식 = ${formatOneDecimal(baseRow.targetBaseline)} + (${formatSignedNumber(baseRow.currentGap, '')}) × ${formatFactor(baseRow.weight)} = ${formatOneDecimal(baseRow.basePrediction)} → ${numberFormat.format(baseRow.predicted)}</code>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">피크 압력식 합치기</span>
        <p class="guide-copy">고정 계수와 이번 시점에서 계산된 feature 값을 곱한 뒤 모두 더합니다.</p>
        <code>${buildCoefficientEquation(featureKeys)}</code>
        <code>${featureKeys.map((key) => contributionTermText(featureKeys, key, adjustedRow)).join(' + ')} = ${formatOneDecimal(adjustedRow.rawPrediction)} → ${numberFormat.format(adjustedRow.predicted)}</code>
        <div class="term-grid">
          ${termCards}
        </div>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">최종 사용값</span>
        <p class="guide-copy">최종 예측은 <strong>${numberFormat.format(adjustedRow.predicted)}명</strong>입니다. 기본식 ${numberFormat.format(baseRow.predicted)}명과 보정식을 다시 더하는 것이 아니라, 피크 압력식 결과를 최종값으로 사용합니다.</p>
        <p class="guide-copy">이 행에서는 기본식 대비 ${formatSignedNumber(adjustedRow.adjustmentTotal, '명')}만큼 움직였고, 실제값 ${numberFormat.format(adjustedRow.actual)}명과의 오차는 ${numberFormat.format(adjustedRow.error)}명입니다.</p>
      </article>
    </div>
  `;
}

function selectedHorizonRecord(data) {
  return data?.horizons?.find((item) => item.horizonMinutes === selectedDetailHorizon) || data?.horizons?.[0] || null;
}

function pickFormulaExampleRow(horizon) {
  return pickExampleRow(horizon?.models?.adjusted?.rows || []);
}

function findMatchingRow(rows, targetRow) {
  if (!targetRow || !rows?.length) return null;
  return rows.find((row) => row.time === targetRow.time && row.targetTime === targetRow.targetTime) || null;
}

function featureDocMap(featureDocs) {
  return new Map(featureDocs.map((doc) => [doc.key, doc]));
}

function coefficientSymbol(featureKeys, key) {
  if (key === 'intercept') return 'intercept';
  const keys = featureKeys.filter((item) => item !== 'intercept');
  const index = keys.indexOf(key);
  return index >= 0 ? String.fromCharCode(97 + index) : key;
}

function buildFormulaScopeText(data, horizon, row) {
  if (!data || !horizon) return '현재 선택 그룹';
  if (data.parameters?.formulaStrategy?.key === STRATEGY_RESTAURANT_HORIZON) {
    return `${data.restaurant} · ${horizon.horizonMinutes}분용 고정 계수`;
  }

  const bandLabel = row?.targetBandLabel || '선택 시간대';
  return `${data.restaurant} · ${horizon.horizonMinutes}분 · ${bandLabel} 계수`;
}

function buildCoefficientEquation(featureKeys) {
  return `예측값 = ${featureKeys.map((key) => {
    const symbol = coefficientSymbol(featureKeys, key);
    if (key === 'intercept') return 'intercept';
    return `${symbol}×${featureLabel(key)}`;
  }).join(' + ')}`;
}

function contributionTermText(featureKeys, key, row) {
  const coefficient = row.coefficients?.[key] ?? 0;
  const value = row.featureValues?.[key] ?? 0;
  if (key === 'intercept') {
    return `${formatCoefficient(coefficient)} × 1.0`;
  }
  return `${formatCoefficient(coefficient)} × ${formatOneDecimal(value)}`;
}

function featureRuntimeFormulaText(key, row, horizon) {
  const currentValue = row.currentValue ?? 0;
  const currentBaseline = row.currentBaseline ?? 0;
  const targetBaseline = row.targetBaseline ?? 0;
  const trend15 = row.trend15 ?? 0;
  const trend30 = row.trend30 ?? 0;
  const featureValues = row.featureValues || {};

  if (key === 'targetBaseline') {
    return `${horizon?.horizonMinutes || 0}분 뒤 같은 식당·같은 요일·같은 시간대 평소값 = ${formatOneDecimal(targetBaseline)}`;
  }
  if (key === 'currentValue') {
    return `기준 시각 실제 혼잡도 = ${formatOneDecimal(currentValue)}`;
  }
  if (key === 'pressureGap') {
    return `max(${formatOneDecimal(targetBaseline)} - ${formatOneDecimal(currentValue)}, 0) = ${formatOneDecimal(featureValues.pressureGap ?? 0)}`;
  }
  if (key === 'baselineShiftPos') {
    return `max(${formatOneDecimal(targetBaseline)} - ${formatOneDecimal(currentBaseline)}, 0) = ${formatOneDecimal(featureValues.baselineShiftPos ?? 0)}`;
  }
  if (key === 'rise15') {
    return `max(${formatOneDecimal(trend15)}, 0) = ${formatOneDecimal(featureValues.rise15 ?? 0)}`;
  }
  if (key === 'rise30') {
    return `max(${formatOneDecimal(trend30)}, 0) = ${formatOneDecimal(featureValues.rise30 ?? 0)}`;
  }
  if (key === 'gapPos') {
    return `max(${formatOneDecimal(currentValue)} - ${formatOneDecimal(currentBaseline)}, 0) = ${formatOneDecimal(featureValues.gapPos ?? 0)}`;
  }
  if (key === 'surge30') {
    return `${formatOneDecimal(featureValues.pressureGap ?? 0)} × ${formatOneDecimal(featureValues.rise30 ?? 0)} / 50 = ${formatOneDecimal(featureValues.surge30 ?? 0)}`;
  }

  return `${featureLabel(key)} = ${formatOneDecimal(featureValues[key] ?? 0)}`;
}

function featureLabelEnhanced(key) {
  if (key === 'intercept') return '절편';
  if (key === 'currentValue') return '현재값';
  if (key === 'targetBaseline') return '목표 시점 평소값';
  if (key === 'gapPos') return '현재 초과분';
  if (key === 'gapNeg') return '현재 부족분';
  if (key === 'baselineShiftSigned') return '평소값 이동량';
  if (key === 'baselineShiftAbs') return '평소값 이동 절댓값';
  if (key === 'trend15') return '최근 15분 변화량';
  if (key === 'trend30') return '최근 30분 변화량';
  if (key === 'rise15') return '15분 상승량';
  if (key === 'rise30') return '30분 상승량';
  if (key === 'fall15') return '15분 하락량';
  if (key === 'fall30') return '30분 하락량';
  if (key === 'baselineShiftPos') return '평소 상승 구간';
  if (key === 'pressureGap') return '피크 압력';
  if (key === 'surge15') return '15분 가속';
  if (key === 'surge30') return '30분 가속';
  return key;
}

function formatSignedNumberEnhanced(value, unit) {
  if (Math.abs(value) < 0.05) return `0.0${unit}`;
  const sign = value > 0 ? '+' : '-';
  return `${sign}${formatOneDecimal(Math.abs(value))}${unit}`;
}

function renderFormulaSectionV2(data) {
  if (!data) {
    formulaList.innerHTML = '';
    formulaSelection.textContent = '';
    formulaPrimer.innerHTML = '';
    return;
  }

  const horizon = selectedHorizonRecord(data);
  const exampleRow = pickFormulaExampleRow(horizon);
  const scopeText = buildFormulaScopeTextV2(data, horizon);
  const formulas = [
    {
      label: '기본식',
      shortLabel: '비교 기준',
      formula: '예측값 = 목표 평소값 + (현재값 - 현재 시점 평소값) × 반영률',
      note: 'w30 / w60 / w90이 직접 들어가는 단순 기준식'
    },
    {
      label: '피크 압력식',
      shortLabel: '도입식',
      formula: buildReadableOptimizedFormula(),
      note: '고정 계수 × 실시간 계산값을 모두 더해 최종 예측값을 만듦'
    }
  ];

  formulaList.innerHTML = formulas.map((family) => `
    <article class="formula-card">
      <div class="formula-card__top">
        <span class="formula-card__label">${family.label}</span>
        <span class="formula-card__short">${family.shortLabel}</span>
      </div>
      <code>${family.formula}</code>
      <p class="formula-card__note">${family.note}</p>
    </article>
  `).join('');

  formulaSelection.textContent = `${scopeText} 기준 설명입니다. 계수는 고정이고, 현재값·평소값·상승량 같은 계산값만 예측 시점마다 다시 구합니다.`;
  formulaPrimer.innerHTML = renderFormulaPrimerV2(data, horizon, exampleRow);
  renderStrategyTables(data.formulaStrategies, data.parameters.formulaStrategy.key);
}

function renderFormulaPrimerV2(data, horizon, row) {
  if (!row || !row.coefficients || !row.featureValues) {
    return `
      <article class="primer-card">
        <span class="primer-card__label">설명용 예시 없음</span>
        <p>현재 응답에는 계수 설명에 사용할 상세 예시 행이 없습니다.</p>
      </article>
    `;
  }

  const featureKeys = orderedFormulaKeysV2(row.coefficients);
  const sampleCount = row.modelSampleCount || horizon?.models?.adjusted?.count || 0;
  const tuningLabel = data.parameters?.tuningWindow?.label || '전체 튜닝 데이터';
  const fixedRows = featureKeys.map((key) => `
    <tr>
      <td>${coefficientNameV2(key)}</td>
      <td>${formatCoefficient(row.coefficients[key] ?? 0)}</td>
      <td>${coefficientMeaningV2(key)}</td>
    </tr>
  `).join('');
  const runtimeRows = runtimeValueRowsV2(row, horizon).map((item) => `
    <tr>
      <td>${item.label}</td>
      <td><code>${item.formula}</code></td>
      <td>${item.value}</td>
      <td>${item.note}</td>
    </tr>
  `).join('');

  return `
    <div class="formula-primer__head">
      <h3>${buildFormulaScopeTextV2(data, horizon)}</h3>
      <p class="hint">발표용 화면이라 a, b, c 대신 실제 항 이름을 그대로 보여줍니다.</p>
    </div>
    <div class="formula-primer__cards formula-primer__cards--wide">
      <article class="primer-card">
        <span class="primer-card__label">계수는 어떻게 정하나</span>
        <p>${tuningLabel} 범위의 과거 ${numberFormat.format(sampleCount)}건을 보고 실제값과 예측값 차이가 가장 작아지도록 맞춥니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">고정되는 값</span>
        <p>절편, 목표 평소값 계수, 현재값 계수, 피크 압력 계수처럼 이름 끝이 계수인 값들입니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">매번 다시 계산되는 값</span>
        <p>현재값, 목표 평소값, 피크 압력, 15분 상승량, 30분 상승량 같은 값들입니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">절편이란</span>
        <p>아무 값도 곱하기 전에 먼저 더해지는 기본 시작값입니다.</p>
      </article>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>고정 계수 이름</th>
            <th>이 그룹 계수</th>
            <th>무슨 역할인가</th>
          </tr>
        </thead>
        <tbody>
          ${fixedRows}
        </tbody>
      </table>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>이번 예측에서 계산한 값</th>
            <th>어떻게 구하나</th>
            <th>이번 값</th>
            <th>설명</th>
          </tr>
        </thead>
        <tbody>
          ${runtimeRows}
        </tbody>
      </table>
    </div>
  `;
}

function renderCalculationGuideV2(horizon) {
  const adjustedRow = pickFormulaExampleRow(horizon);
  const baseRows = horizon?.models?.base?.rows || [];
  const baseRow = findMatchingRow(baseRows, adjustedRow) || pickExampleRow(baseRows);

  if (!adjustedRow || !baseRow) {
    calculationGuide.innerHTML = `
      <article class="guide-card">
        <span class="guide-label">계산 예시</span>
        <p class="guide-copy">현재 응답에는 계산 예시에 쓸 상세 행이 없습니다.</p>
      </article>
    `;
    return;
  }

  const featureKeys = orderedFormulaKeysV2(adjustedRow.coefficients || {});
  const termCards = featureKeys.map((key) => `
    <article class="term-card">
      <strong>${termDisplayNameV2(key)}</strong>
      <p>${termEquationV2(key, adjustedRow)}</p>
      <em>${termResultV2(key, adjustedRow)}</em>
    </article>
  `).join('');

  calculationGuide.innerHTML = `
    <div class="guide-grid guide-grid--formula">
      <article class="guide-card">
        <span class="guide-label">예시 행</span>
        <p class="guide-copy">${buildFormulaScopeTextV2(lastRenderedData, horizon)}에서 <strong>${adjustedRow.time}</strong> 기준 <strong>${adjustedRow.targetTime}</strong>를 예측한 행입니다.</p>
      </article>
      <article class="guide-card">
        <span class="guide-label">기본식 계산</span>
        <code>현재 차이 = ${formatOneDecimal(baseRow.currentValue)} - ${formatOneDecimal(baseRow.currentBaseline)} = ${formatSignedNumberEnhanced(baseRow.currentGap, '')}</code>
        <code>기본식 = ${formatOneDecimal(baseRow.targetBaseline)} + (${formatSignedNumberEnhanced(baseRow.currentGap, '')}) × ${formatFactor(baseRow.weight)} = ${formatOneDecimal(baseRow.basePrediction)} → ${numberFormat.format(baseRow.predicted)}</code>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">피크 압력식 계산</span>
        <code>${buildReadableOptimizedFormula()}</code>
        <div class="term-grid">
          ${termCards}
        </div>
        <code>모든 항 합 = ${formatOneDecimal(adjustedRow.rawPrediction)} → 최종 예측 ${numberFormat.format(adjustedRow.predicted)}</code>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">실제와 비교</span>
        <p class="guide-copy">실제값은 <strong>${numberFormat.format(adjustedRow.actual)}명</strong>, 최종 예측은 <strong>${numberFormat.format(adjustedRow.predicted)}명</strong>, 오차는 <strong>${numberFormat.format(adjustedRow.error)}명</strong>입니다.</p>
        <p class="guide-copy">기본식 ${numberFormat.format(baseRow.predicted)}명에서 끝내지 않고, 피크 압력식으로 ${formatSignedNumberEnhanced(adjustedRow.adjustmentTotal, '명')} 보정한 결과를 최종값으로 사용합니다.</p>
      </article>
    </div>
  `;
}

function buildReadableOptimizedFormula() {
  return '예측값 = 절편 + (목표 평소값 계수 × 목표 평소값) + (현재값 계수 × 현재값) + (피크 압력 계수 × 피크 압력) + (상승 평소값 이동 계수 × 상승 평소값 이동) + (15분 상승 계수 × 15분 상승) + (30분 상승 계수 × 30분 상승) + (초과 차이 계수 × 초과 차이) + (30분 가속 계수 × 30분 가속)';
}

function buildFormulaScopeTextV2(data, horizon) {
  if (!data || !horizon) return '현재 선택 그룹';
  return data.parameters?.formulaStrategy?.key === STRATEGY_RESTAURANT_HORIZON
    ? `${data.restaurant} ${horizon.horizonMinutes}분용 고정식`
    : `${data.restaurant} ${horizon.horizonMinutes}분 ${horizon.targetBandLabel || '시간대'} 고정식`;
}

function orderedFormulaKeysV2(coefficients) {
  const order = ['intercept', 'targetBaseline', 'currentValue', 'pressureGap', 'baselineShiftPos', 'rise15', 'rise30', 'gapPos', 'surge30'];
  return order.filter((key) => Object.prototype.hasOwnProperty.call(coefficients || {}, key));
}

function coefficientNameV2(key) {
  if (key === 'intercept') return '절편';
  if (key === 'targetBaseline') return '목표 평소값 계수';
  if (key === 'currentValue') return '현재값 계수';
  if (key === 'pressureGap') return '피크 압력 계수';
  if (key === 'baselineShiftPos') return '상승 평소값 이동 계수';
  if (key === 'rise15') return '15분 상승 계수';
  if (key === 'rise30') return '30분 상승 계수';
  if (key === 'gapPos') return '초과 차이 계수';
  if (key === 'surge30') return '30분 가속 계수';
  return key;
}

function coefficientMeaningV2(key) {
  if (key === 'intercept') return '항상 먼저 더해지는 기본값';
  if (key === 'targetBaseline') return '목표 시각 평소 혼잡도를 얼마나 강하게 반영할지 정하는 값';
  if (key === 'currentValue') return '지금 실제 혼잡도를 얼마나 끌고 갈지 정하는 값';
  if (key === 'pressureGap') return '앞으로 더 붐빌 압력을 얼마나 크게 볼지 정하는 값';
  if (key === 'baselineShiftPos') return '평소 흐름상 앞으로 올라가는 구간인지 반영하는 값';
  if (key === 'rise15' || key === 'rise30') return '최근 상승 흐름을 얼마나 반영할지 정하는 값';
  if (key === 'gapPos') return '이미 평소보다 높은 상태를 얼마나 반영할지 정하는 값';
  if (key === 'surge30') return '압력과 최근 상승이 같이 클 때 추가 반응하는 값';
  return '';
}

function runtimeValueRowsV2(row, horizon) {
  return [
    {
      label: '현재 시점 평소값',
      formula: '같은 식당·같은 요일·같은 시간대의 과거 평균',
      value: formatOneDecimal(row.currentBaseline),
      note: '기본식과 초과 차이 계산의 기준'
    },
    {
      label: '목표 평소값',
      formula: `${horizon?.horizonMinutes || 0}분 뒤 같은 식당·같은 요일·같은 시간대의 과거 평균`,
      value: formatOneDecimal(row.targetBaseline),
      note: '목표 시각의 평소 혼잡도'
    },
    {
      label: '현재값',
      formula: '기준 시각 실제 혼잡도',
      value: formatOneDecimal(row.currentValue),
      note: '지금 실제로 몇 명인지'
    },
    {
      label: '피크 압력',
      formula: `max(${formatOneDecimal(row.targetBaseline)} - ${formatOneDecimal(row.currentValue)}, 0)`,
      value: formatOneDecimal(row.featureValues.pressureGap ?? 0),
      note: '앞으로 더 붐빌 여지'
    },
    {
      label: '상승 평소값 이동',
      formula: `max(${formatOneDecimal(row.targetBaseline)} - ${formatOneDecimal(row.currentBaseline)}, 0)`,
      value: formatOneDecimal(row.featureValues.baselineShiftPos ?? 0),
      note: '평소 흐름상 앞으로 더 올라가는 정도'
    },
    {
      label: '15분 상승',
      formula: `max(${formatOneDecimal(row.trend15 ?? 0)}, 0)`,
      value: formatOneDecimal(row.featureValues.rise15 ?? 0),
      note: '최근 15분 실제 상승량'
    },
    {
      label: '30분 상승',
      formula: `max(${formatOneDecimal(row.trend30 ?? 0)}, 0)`,
      value: formatOneDecimal(row.featureValues.rise30 ?? 0),
      note: '최근 30분 실제 상승량'
    },
    {
      label: '초과 차이',
      formula: `max(${formatOneDecimal(row.currentValue)} - ${formatOneDecimal(row.currentBaseline)}, 0)`,
      value: formatOneDecimal(row.featureValues.gapPos ?? 0),
      note: '지금 이미 평소보다 높은 정도'
    },
    {
      label: '30분 가속',
      formula: `${formatOneDecimal(row.featureValues.pressureGap ?? 0)} × ${formatOneDecimal(row.featureValues.rise30 ?? 0)} / 50`,
      value: formatOneDecimal(row.featureValues.surge30 ?? 0),
      note: '압력과 최근 상승이 함께 큰지 보는 값'
    }
  ];
}

function termDisplayNameV2(key) {
  if (key === 'intercept') return '절편';
  return `${coefficientNameV2(key)} × ${valueNameV2(key)}`;
}

function termEquationV2(key, row) {
  const coefficient = formatCoefficient(row.coefficients?.[key] ?? 0);
  if (key === 'intercept') {
    return `절편 ${coefficient} × 1.0`;
  }
  return `${coefficientNameV2(key)} ${coefficient} × ${valueNameV2(key)} ${formatOneDecimal(row.featureValues?.[key] ?? 0)}`;
}

function termResultV2(key, row) {
  return `= ${formatSignedNumberEnhanced(row.contributions?.[key] ?? 0, '')}`;
}

function valueNameV2(key) {
  if (key === 'targetBaseline') return '목표 평소값';
  if (key === 'currentValue') return '현재값';
  if (key === 'pressureGap') return '피크 압력';
  if (key === 'baselineShiftPos') return '상승 평소값 이동';
  if (key === 'rise15') return '15분 상승';
  if (key === 'rise30') return '30분 상승';
  if (key === 'gapPos') return '초과 차이';
  if (key === 'surge30') return '30분 가속';
  return key;
}

function renderStrategyTablesV2() {
  strategyTables.innerHTML = '';
}

function simplifyPresentation(data) {
  if (!data) return;

  const optimizedLabel = data.formulas?.optimized?.label || data.parameters?.optimized?.formulaLabel || '피크 압력식';
  const qualitySummary = data.quality
    ? `원본 ${numberFormat.format(data.quality.rawCount)}건 중 품질 이상 ${numberFormat.format(data.quality.qualityRemovedCount)}건과 비활성 시간대 ${numberFormat.format(data.quality.inactiveSlotRemovedCount)}건을 제외하고 ${numberFormat.format(data.quality.keptCount)}건으로 검증했습니다.`
    : '선택한 날짜 기준으로 실제값과 예측값을 비교합니다.';

  adjustedHelp.textContent = '최종 예측은 모두 피크 압력식으로 계산합니다. 고정 계수는 유지하고, 현재값과 상승량 같은 입력값만 예측할 때마다 다시 계산합니다.';
  summary.innerHTML = `
    <div class="summary-head">
      <div>
        <p class="eyebrow eyebrow--small">검증 결과</p>
        <h2>${data.restaurant} · ${data.date}</h2>
      </div>
      <div class="summary-badges">
        <span class="tag">${data.baselineMode}</span>
        <span class="tag">${data.season}</span>
        <span class="tag">${optimizedLabel}</span>
      </div>
    </div>
    <p class="summary-note">${qualitySummary}</p>
  `;

  predictionCards.innerHTML = data.horizons.map((item) => {
    const comparison = item.comparison;
    const isFocus = item.horizonMinutes === selectedDetailHorizon;

    return `
      <article class="card card--comparison ${isFocus ? 'card--focus' : ''}" data-card-horizon="${item.horizonMinutes}">
        <div class="card-top">
          <span class="card-horizon">${item.horizonMinutes}분 후</span>
          <span class="chip">피크 압력식</span>
        </div>
        <div class="compare-stack">
          <div class="compare-metric">
            <span>기본식 MAE</span>
            <strong>${formatOneDecimal(item.models.base.mae)}명</strong>
          </div>
          <div class="compare-metric compare-metric--accent">
            <span>피크 압력식 MAE</span>
            <strong>${formatOneDecimal(item.models.adjusted.mae)}명</strong>
          </div>
        </div>
        <div class="metric-grid metric-grid--triple">
          <div>
            <span>MAE 변화</span>
            <strong class="${deltaTone(comparison.maeDelta)}">${formatSignedNumber(comparison.maeDelta, '명')}</strong>
          </div>
          <div>
            <span>10명 이내</span>
            <strong>${formatPercent(item.models.adjusted.within10Rate)}</strong>
          </div>
          <div>
            <span>비교 건수</span>
            <strong>${numberFormat.format(item.models.adjusted.count)}건</strong>
          </div>
        </div>
        <p class="card-note">기본식 10명 이내 ${formatPercent(item.models.base.within10Rate)} · 피크 압력식 10명 이내 ${formatPercent(item.models.adjusted.within10Rate)}</p>
      </article>
    `;
  }).join('');

  overviewGuide.innerHTML = '';
  chart.innerHTML = data.horizons.map((item) => `
    <section class="mini-chart">
      <div class="mini-chart__head">
        <div>
          <h3>${item.horizonMinutes}분 후 실제값과 예측값 비교</h3>
          <p>기본식 MAE ${formatOneDecimal(item.models.base.mae)}명 · 피크 압력식 MAE ${formatOneDecimal(item.models.adjusted.mae)}명 · 변화 ${formatSignedNumber(item.comparison.maeDelta, '명')}</p>
        </div>
        <div class="legend">
          <span class="legend-item"><i class="legend-line legend-line--actual"></i>실제값</span>
          <span class="legend-item"><i class="legend-line legend-line--base"></i>기본식</span>
          <span class="legend-item"><i class="legend-line legend-line--adjusted"></i>피크 압력식</span>
          <span class="legend-item"><i class="legend-line legend-line--gap"></i>실제-피크 압력식 오차</span>
        </div>
      </div>
      ${buildChartSvg(buildChartPoints(item, 72), { width: 960, height: 320, compact: false })}
    </section>
  `).join('');

  renderFormulaSectionPresentation(data);
  tableHint.textContent = '';
}

function renderFormulaSectionPresentation(data) {
  if (!data) {
    formulaList.innerHTML = '';
    formulaSelection.textContent = '';
    formulaPrimer.innerHTML = '';
    calculationGuide.innerHTML = '';
    renderStrategyTablesV2();
    return;
  }

  const horizon = selectedHorizonRecord(data);
  const row = pickFormulaExampleRow(horizon);
  const scopeText = buildFormulaScopeTextV2(data, horizon);
  const optimizedFormula = buildReadableOptimizedFormulaPresentation();

  formulaList.innerHTML = `
    <article class="formula-card">
      <div class="formula-card__top">
        <span class="formula-card__label">비교용 기본식</span>
        <span class="formula-card__short">기준선</span>
      </div>
      <code>예측값 = 목표 평소값 + (현재값 - 현재 시점 평소값) × 반영률</code>
      <p class="formula-card__note">30분·60분·90분 반영률은 이 비교용 기본식에만 직접 들어갑니다.</p>
    </article>
    <article class="formula-card">
      <div class="formula-card__top">
        <span class="formula-card__label">최종 피크 압력식</span>
        <span class="formula-card__short">실제 적용</span>
      </div>
      <code>${optimizedFormula}</code>
      <p class="formula-card__note">현재 화면의 최종 예측은 모두 이 식으로 계산합니다.</p>
    </article>
  `;

  formulaSelection.textContent = `${scopeText} 기준 고정 계수와 이번 예측에서 다시 계산되는 값을 아래에 나눠서 보여줍니다.`;

  if (!row || !row.coefficients || !row.featureValues) {
    formulaPrimer.innerHTML = `
      <article class="primer-card">
        <span class="primer-card__label">설명용 예시 없음</span>
        <p>현재 응답에는 계산식 설명에 사용할 상세 예시 값이 없습니다.</p>
      </article>
    `;
    calculationGuide.innerHTML = `
      <article class="guide-card">
        <span class="guide-label">계산 예시</span>
        <p class="guide-copy">현재 응답에는 계산 예시를 만들 상세 값이 없습니다.</p>
      </article>
    `;
    renderStrategyTablesV2();
    return;
  }

  const tuningLabel = data.parameters?.tuningWindow?.label || '전체 누적 데이터';
  const sampleCount = row.modelSampleCount || horizon?.models?.adjusted?.count || 0;
  const fixedRows = orderedFormulaKeysV2(row.coefficients).map((key) => `
    <tr>
      <td>${coefficientNameV2(key)}</td>
      <td>${formatCoefficient(row.coefficients[key] ?? 0)}</td>
      <td>${coefficientMeaningV2(key)}</td>
    </tr>
  `).join('');
  const runtimeRows = runtimeValueRowsV2(row, horizon).map((item) => `
    <tr>
      <td>${item.label}</td>
      <td><code>${item.formula}</code></td>
      <td>${item.value}</td>
      <td>${item.note}</td>
    </tr>
  `).join('');

  formulaPrimer.innerHTML = `
    <div class="formula-primer__head">
      <h3>${scopeText}</h3>
      <p class="hint">a, b, c 대신 바로 의미가 보이는 이름으로 표시합니다.</p>
    </div>
    <div class="formula-primer__cards formula-primer__cards--wide">
      <article class="primer-card">
        <span class="primer-card__label">계수는 어떻게 정했나</span>
        <p>${tuningLabel} 범위의 과거 ${numberFormat.format(sampleCount)}건을 보고 실제값과 예측값 차이가 가장 작아지도록 맞춘 고정 계수입니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">고정되는 값</span>
        <p>절편과 각 항목 계수는 이 그룹에서 고정입니다. 같은 식당의 같은 분 구간이면 날짜가 바뀌어도 그대로 씁니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">매번 다시 계산되는 값</span>
        <p>현재값, 목표 평소값, 피크 압력, 15분 상승, 30분 상승 같은 값은 예측할 때마다 다시 계산합니다.</p>
      </article>
      <article class="primer-card">
        <span class="primer-card__label">반영률 사용 위치</span>
        <p>30분·60분·90분 반영률은 비교용 기본식에만 쓰고, 최종 피크 압력식에는 직접 넣지 않습니다.</p>
      </article>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>고정 계수 이름</th>
            <th>이 그룹 계수</th>
            <th>무슨 뜻인가</th>
          </tr>
        </thead>
        <tbody>
          ${fixedRows}
        </tbody>
      </table>
    </div>
    <div class="table-wrap table-wrap--plain">
      <table class="mapping-table mapping-table--compact formula-breakdown-table">
        <thead>
          <tr>
            <th>이번 예측에서 계산하는 값</th>
            <th>어떻게 구하나</th>
            <th>이번 값</th>
            <th>설명</th>
          </tr>
        </thead>
        <tbody>
          ${runtimeRows}
        </tbody>
      </table>
    </div>
  `;

  renderCalculationGuidePresentation(horizon);
  renderStrategyTablesV2();
}

function renderCalculationGuidePresentation(horizon) {
  const adjustedRow = pickFormulaExampleRow(horizon);
  const baseRows = horizon?.models?.base?.rows || [];
  const baseRow = findMatchingRow(baseRows, adjustedRow) || pickExampleRow(baseRows);

  if (!adjustedRow || !baseRow) {
    calculationGuide.innerHTML = `
      <article class="guide-card">
        <span class="guide-label">계산 예시</span>
        <p class="guide-copy">현재 응답에는 계산 예시를 만들 상세 값이 없습니다.</p>
      </article>
    `;
    return;
  }

  const termCards = orderedFormulaKeysV2(adjustedRow.coefficients || {}).map((key) => `
    <article class="term-card">
      <strong>${termDisplayNameV2(key)}</strong>
      <p>${termEquationV2(key, adjustedRow)}</p>
      <em>${termResultV2(key, adjustedRow)}</em>
    </article>
  `).join('');

  calculationGuide.innerHTML = `
    <div class="guide-grid guide-grid--formula">
      <article class="guide-card">
        <span class="guide-label">이번 예시</span>
        <p class="guide-copy">${buildFormulaScopeTextV2(lastRenderedData, horizon)}에서 <strong>${adjustedRow.time}</strong> 기준 <strong>${adjustedRow.targetTime}</strong>를 예측한 예시입니다.</p>
      </article>
      <article class="guide-card">
        <span class="guide-label">비교용 기본식</span>
        <code>현재 차이 = ${formatOneDecimal(baseRow.currentValue)} - ${formatOneDecimal(baseRow.currentBaseline)} = ${formatSignedNumberEnhanced(baseRow.currentGap, '')}</code>
        <code>기본식 = ${formatOneDecimal(baseRow.targetBaseline)} + (${formatSignedNumberEnhanced(baseRow.currentGap, '')}) × ${formatFactor(baseRow.weight)} = ${formatOneDecimal(baseRow.basePrediction)} → ${numberFormat.format(baseRow.predicted)}</code>
        <p class="guide-copy">30분·60분·90분 반영률은 이 비교용 기본식에서만 사용합니다.</p>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">최종 피크 압력식</span>
        <code>${buildReadableOptimizedFormulaPresentation()}</code>
        <div class="term-grid">
          ${termCards}
        </div>
        <code>모든 항의 합 = ${formatOneDecimal(adjustedRow.rawPrediction)} → 최종 예측 ${numberFormat.format(adjustedRow.predicted)}</code>
      </article>
      <article class="guide-card guide-card--wide">
        <span class="guide-label">실제값 비교</span>
        <p class="guide-copy">실제값이 <strong>${numberFormat.format(adjustedRow.actual)}명</strong>, 최종 예측이 <strong>${numberFormat.format(adjustedRow.predicted)}명</strong>, 오차가 <strong>${numberFormat.format(adjustedRow.error)}명</strong>입니다.</p>
        <p class="guide-copy">기본식 ${numberFormat.format(baseRow.predicted)}명에서 바로 쓰지 않고, 피크 압력식 보정 ${formatSignedNumberEnhanced(adjustedRow.adjustmentTotal, '명')}을 반영한 값을 최종 예측으로 사용합니다.</p>
      </article>
    </div>
  `;
}

function buildReadableOptimizedFormulaPresentation() {
  return '예측값 = 절편 + (목표 평소값 계수 × 목표 평소값) + (현재값 계수 × 현재값) + (피크 압력 계수 × 피크 압력) + (상승 평소값 이동 계수 × 상승 평소값 이동) + (15분 상승 계수 × 15분 상승) + (30분 상승 계수 × 30분 상승) + (초과 차이 계수 × 초과 차이) + (30분 가속 계수 × 30분 가속)';
}
function simplifyPresentationV2(data) {
  if (!data) return;

  const optimizedLabel = data.formulas?.optimized?.label || data.parameters?.optimized?.formulaLabel || '피크 압력식';
  const profileLabel = data.profile?.label || activeProfileMeta()?.profile?.label || '전체 누적 데이터';
  const qualitySummary = data.quality
    ? `원본 ${numberFormat.format(data.quality.rawCount)}건 중 품질 이상 ${numberFormat.format(data.quality.qualityRemovedCount)}건과 비활성 시간대 ${numberFormat.format(data.quality.inactiveSlotRemovedCount)}건을 제외하고 ${numberFormat.format(data.quality.keptCount)}건으로 검증했습니다.`
    : '선택한 날짜 기준으로 실제값과 예측값을 비교합니다.';

  adjustedHelp.textContent = `${profileLabel} 기준으로 고정 계수를 불러오고, 현재값과 상승량 같은 입력값만 예측할 때마다 다시 계산합니다.`;
  summary.innerHTML = `
    <div class="summary-head">
      <div>
        <p class="eyebrow eyebrow--small">검증 결과</p>
        <h2>${data.restaurant} · ${data.date}</h2>
      </div>
      <div class="summary-badges">
        <span class="tag">${profileLabel}</span>
        <span class="tag">${data.baselineMode}</span>
        <span class="tag">${data.season}</span>
        <span class="tag">${optimizedLabel}</span>
      </div>
    </div>
    <p class="summary-note">${qualitySummary}</p>
  `;

  predictionCards.innerHTML = data.horizons.map((item) => {
    const comparison = item.comparison;
    const isFocus = item.horizonMinutes === selectedDetailHorizon;

    return `
      <article class="card card--comparison ${isFocus ? 'card--focus' : ''}" data-card-horizon="${item.horizonMinutes}">
        <div class="card-top">
          <span class="card-horizon">${item.horizonMinutes}분 후</span>
          <span class="chip">${profileLabel}</span>
        </div>
        <div class="compare-stack">
          <div class="compare-metric">
            <span>기본식 MAE</span>
            <strong>${formatOneDecimal(item.models.base.mae)}명</strong>
          </div>
          <div class="compare-metric compare-metric--accent">
            <span>피크 압력식 MAE</span>
            <strong>${formatOneDecimal(item.models.adjusted.mae)}명</strong>
          </div>
        </div>
        <div class="metric-grid metric-grid--triple">
          <div>
            <span>MAE 변화</span>
            <strong class="${deltaTone(comparison.maeDelta)}">${formatSignedNumber(comparison.maeDelta, '명')}</strong>
          </div>
          <div>
            <span>10명 이내</span>
            <strong>${formatPercent(item.models.adjusted.within10Rate)}</strong>
          </div>
          <div>
            <span>비교 건수</span>
            <strong>${numberFormat.format(item.models.adjusted.count)}건</strong>
          </div>
        </div>
        <p class="card-note">기본식 10명 이내 ${formatPercent(item.models.base.within10Rate)} · 피크 압력식 10명 이내 ${formatPercent(item.models.adjusted.within10Rate)}</p>
      </article>
    `;
  }).join('');

  overviewGuide.innerHTML = '';
  chart.innerHTML = data.horizons.map((item) => `
    <section class="mini-chart">
      <div class="mini-chart__head">
        <div>
          <h3>${item.horizonMinutes}분 후 실제값과 예측값 비교</h3>
          <p>${profileLabel} · 기본식 MAE ${formatOneDecimal(item.models.base.mae)}명 · 피크 압력식 MAE ${formatOneDecimal(item.models.adjusted.mae)}명 · 변화 ${formatSignedNumber(item.comparison.maeDelta, '명')}</p>
        </div>
        <div class="legend">
          <span class="legend-item"><i class="legend-line legend-line--actual"></i>실제값</span>
          <span class="legend-item"><i class="legend-line legend-line--base"></i>기본식</span>
          <span class="legend-item"><i class="legend-line legend-line--adjusted"></i>피크 압력식</span>
          <span class="legend-item"><i class="legend-line legend-line--gap"></i>실제-피크 압력식 오차</span>
        </div>
      </div>
      ${buildChartSvg(buildChartPoints(item, 72), { width: 960, height: 320, compact: false })}
    </section>
  `).join('');

  renderFormulaSectionPresentation(data);
  tableHint.textContent = '';
}

function formatValidationDelta(item) {
  return `${item.baseMae}->${item.optimizedMae}`;
}
