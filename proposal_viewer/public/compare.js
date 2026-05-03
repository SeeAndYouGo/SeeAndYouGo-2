const MODEL_BASE = 'base';
const MODEL_ADJUSTED = 'adjusted';
const MODEL_RESIDUAL = 'residual';
const DEFAULT_PROFILE = 'allData';
const DEFAULT_DATE = '2026-04-01';

const FEATURE_LABELS = {
  targetBaseline: '목표 평소값',
  currentValue: '현재값',
  pressureGap: '피크 압력',
  baselineShiftPos: '상승 평소값 이동',
  gapPos: '현재 초과분',
  gapNeg: '현재 부족분',
  surge30: '30분 가속',
  rise30: '최근 30분 상승'
};

const MODEL_META = {
  [MODEL_BASE]: {
    label: '기본식',
    badgeClass: 'badge--base',
    color: '#c76b2d'
  },
  [MODEL_ADJUSTED]: {
    label: '현재 운영식',
    badgeClass: 'badge--adjusted',
    color: '#285a8c'
  },
  [MODEL_RESIDUAL]: {
    label: '잔차 보정식',
    badgeClass: 'badge--residual',
    color: '#0f766e'
  }
};

const state = {
  metadata: null,
  profile: DEFAULT_PROFILE,
  selectedHorizon: 60,
  data: null
};

const compareForm = document.getElementById('compareForm');
const restaurantSelect = document.getElementById('restaurant');
const dateInput = document.getElementById('date');
const profileSwitch = document.getElementById('profileSwitch');
const profileHint = document.getElementById('profileHint');
const runButton = document.getElementById('runButton');
const statusPanel = document.getElementById('statusPanel');
const comparePanel = document.getElementById('comparePanel');
const summaryStrip = document.getElementById('summaryStrip');
const horizonStrip = document.getElementById('horizonStrip');
const focusEyebrow = document.getElementById('focusEyebrow');
const focusTitle = document.getElementById('focusTitle');
const chartLegend = document.getElementById('chartLegend');
const chartStage = document.getElementById('chartStage');
const metricsStack = document.getElementById('metricsStack');
const methodGrid = document.getElementById('methodGrid');
const sampleRows = document.getElementById('sampleRows');

compareForm.addEventListener('submit', (event) => {
  event.preventDefault();
  void loadComparison();
});

document.addEventListener('click', (event) => {
  const profileButton = event.target.closest('[data-profile]');
  if (profileButton && profileSwitch.contains(profileButton)) {
    state.profile = profileButton.dataset.profile;
    renderProfileSwitch();
    void loadComparison();
    return;
  }

  const horizonButton = event.target.closest('[data-horizon]');
  if (horizonButton && horizonStrip.contains(horizonButton)) {
    state.selectedHorizon = Number(horizonButton.dataset.horizon);
    renderComparison();
  }
});

void init();

async function init() {
  dateInput.value = DEFAULT_DATE;
  runButton.disabled = true;
  showStatus('초기 설정을 불러오는 중입니다.', false);

  try {
    const response = await fetch('/api/restaurants');
    const metadata = await response.json();
    if (!response.ok) throw new Error(metadata.message || '식당 목록을 불러오지 못했습니다.');
    state.metadata = metadata;
    state.profile = metadata.defaultProfile || DEFAULT_PROFILE;
    state.selectedHorizon = 60;
    renderRestaurantOptions();
    renderProfileSwitch();
    await loadComparison();
  } catch (error) {
    showStatus(error.message || '초기 데이터를 불러오지 못했습니다.');
  } finally {
    runButton.disabled = false;
  }
}

function renderRestaurantOptions() {
  const restaurants = state.metadata?.restaurants || [];
  restaurantSelect.innerHTML = restaurants
    .map((restaurant) => `<option value="${escapeHtml(restaurant)}">${escapeHtml(restaurant)}</option>`)
    .join('');
  restaurantSelect.value = restaurants[0] || '';
}

function renderProfileSwitch() {
  const profiles = state.metadata?.profiles || {};
  profileSwitch.innerHTML = Object.values(profiles)
    .map((entry) => entry?.profile)
    .filter(Boolean)
    .map((profile) => `
      <button
        type="button"
        class="profile-button ${profile.key === state.profile ? 'is-active' : ''}"
        data-profile="${escapeHtml(profile.key)}"
        aria-pressed="${String(profile.key === state.profile)}"
      >
        ${escapeHtml(profile.label)}
      </button>
    `)
    .join('');
  profileHint.textContent = profiles[state.profile]?.profile?.description || '';
}

async function loadComparison() {
  if (!restaurantSelect.value || !dateInput.value) return;
  runButton.disabled = true;
  showStatus('비교 데이터를 불러오는 중입니다.', false);

  try {
    const params = new URLSearchParams({
      restaurant: restaurantSelect.value,
      date: dateInput.value,
      profile: state.profile
    });
    const response = await fetch(`/api/history-compare?${params.toString()}`);
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || '비교 데이터를 불러오지 못했습니다.');

    state.data = data;
    if (!data.horizons?.some((item) => item.horizonMinutes === state.selectedHorizon)) {
      state.selectedHorizon = data.horizons?.[0]?.horizonMinutes || 60;
    }

    hideStatus();
    renderComparison();
  } catch (error) {
    state.data = null;
    comparePanel.hidden = true;
    showStatus(error.message || '비교 데이터를 불러오지 못했습니다.');
  } finally {
    runButton.disabled = false;
  }
}

function renderComparison() {
  const data = state.data;
  if (!data) return;
  comparePanel.hidden = false;
  renderSummary(data);
  renderHorizonCards(data);
  renderFocus(data);
}

function renderSummary(data) {
  const summary = data.comparisonSummary || {};
  const headToHead = summary.headToHeadAdjustedVsResidual || {
    adjustedWins: 0,
    residualWins: 0,
    ties: 0
  };

  summaryStrip.innerHTML = [
    renderSummaryCard('승수', `${headToHead.adjustedWins}`, '현재 운영식이 더 낮은 MAE를 기록한 구간 수'),
    renderSummaryCard('승수', `${headToHead.residualWins}`, '잔차 보정식이 더 낮은 MAE를 기록한 구간 수'),
    renderSummaryCard('동률', `${headToHead.ties}`, '두 식의 차이가 거의 없었던 구간 수'),
    renderSummaryCard(
      '최대 개선',
      summary.biggestResidualGain ? `${formatSigned(summary.biggestResidualGain.maeDelta)}명` : '0.0명',
      summary.biggestResidualGain
        ? `${summary.biggestResidualGain.horizonMinutes}분 후에서 잔차 보정식이 더 좋았던 폭`
        : '현재 운영식 대비 눈에 띄는 개선 없음'
    )
  ].join('');
}

function renderSummaryCard(eyebrow, value, sub) {
  return `
    <article class="summary-card">
      <p class="summary-card__eyebrow">${escapeHtml(eyebrow)}</p>
      <div class="summary-card__value">${escapeHtml(value)}</div>
      <div class="summary-card__sub">${escapeHtml(sub)}</div>
    </article>
  `;
}

function renderHorizonCards(data) {
  horizonStrip.innerHTML = data.horizons.map((horizon) => {
    const winnerKey = primaryWinnerKey(horizon);
    const winnerModel = horizon.models[winnerKey];

    return `
      <button type="button" class="horizon-card ${horizon.horizonMinutes === state.selectedHorizon ? 'is-active' : ''}" data-horizon="${horizon.horizonMinutes}">
        <div class="horizon-card__top">
          <div>
            <p class="panel__eyebrow">${horizon.horizonMinutes}분 후</p>
            <h3>${formatNumber(winnerModel.mae, 1)} MAE</h3>
          </div>
          <span class="badge ${MODEL_META[winnerKey].badgeClass}">${escapeHtml(MODEL_META[winnerKey].label)}</span>
        </div>
        <div class="horizon-card__stats">
          ${renderMiniStat('현재 운영식', formatNumber(horizon.models.adjusted.mae, 1))}
          ${renderMiniStat('잔차 보정식', formatNumber(horizon.models.residual.mae, 1))}
          ${renderMiniStat('기본식 참고', formatNumber(horizon.models.base.mae, 1))}
        </div>
      </button>
    `;
  }).join('');
}

function renderMiniStat(label, value) {
  return `
    <div class="mini-stat">
      <div class="mini-stat__label">${escapeHtml(label)}</div>
      <div class="mini-stat__value">${escapeHtml(value)}</div>
    </div>
  `;
}

function renderFocus(data) {
  const horizon = selectedHorizonRecord(data);
  if (!horizon) return;

  const winnerKey = primaryWinnerKey(horizon);
  focusEyebrow.textContent = `${data.restaurant} · ${data.date}`;
  focusTitle.textContent = `${horizon.horizonMinutes}분 후 · ${MODEL_META[winnerKey].label} 우세`;

  renderLegend();
  renderChart(horizon);
  renderMetrics(horizon);
  renderMethodCards(horizon);
  renderSampleTable(horizon);
}

function selectedHorizonRecord(data) {
  return data.horizons?.find((item) => item.horizonMinutes === state.selectedHorizon) || data.horizons?.[0] || null;
}

function primaryWinnerKey(horizon) {
  const winner = horizon?.comparisons?.residualVsAdjusted?.winner;
  if (winner === MODEL_RESIDUAL) return MODEL_RESIDUAL;
  return MODEL_ADJUSTED;
}

function renderLegend() {
  const items = [
    { label: '실제값', color: '#153036' },
    { label: MODEL_META[MODEL_ADJUSTED].label, color: MODEL_META[MODEL_ADJUSTED].color },
    { label: MODEL_META[MODEL_RESIDUAL].label, color: MODEL_META[MODEL_RESIDUAL].color },
    { label: MODEL_META[MODEL_BASE].label, color: MODEL_META[MODEL_BASE].color }
  ];

  chartLegend.innerHTML = items.map((item) => `
    <div class="legend__item">
      <span class="legend__swatch" style="background:${item.color}"></span>
      <span>${escapeHtml(item.label)}</span>
    </div>
  `).join('');
}

function renderChart(horizon) {
  const rows = mergedRows(horizon);
  if (!rows.length) {
    chartStage.innerHTML = '<div class="empty-state">그릴 수 있는 데이터가 없습니다.</div>';
    return;
  }

  const width = 960;
  const height = 360;
  const padX = 48;
  const padTop = 20;
  const padBottom = 36;
  const padRight = 18;
  const allValues = rows.flatMap((row) => [row.actual, row.base, row.adjusted, row.residual]);
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const yMin = Math.max(0, Math.floor((minValue - 8) / 10) * 10);
  const yMax = Math.ceil((maxValue + 8) / 10) * 10 || 10;
  const xStep = rows.length > 1 ? (width - padX - padRight) / (rows.length - 1) : 0;
  const yScale = (height - padTop - padBottom) / Math.max(1, yMax - yMin);
  const y = (value) => height - padBottom - (value - yMin) * yScale;
  const points = (key) => rows.map((row, index) => `${round2(padX + xStep * index)},${round2(y(row[key]))}`).join(' ');
  const yTicks = Array.from({ length: 5 }, (_, index) => yMin + ((yMax - yMin) * index) / 4);
  const xLabels = sampleIndexes(rows.length, Math.min(6, rows.length));

  chartStage.innerHTML = `
    <svg viewBox="0 0 ${width} ${height}" class="chart-svg" role="img" aria-label="예측 비교 그래프">
      ${yTicks.map((tick) => `
        <g>
          <line x1="${padX}" x2="${width - padRight}" y1="${y(tick)}" y2="${y(tick)}" stroke="#e4eded" stroke-width="1" />
          <text x="${padX - 10}" y="${y(tick) + 4}" text-anchor="end" font-size="11" fill="#647b81">${formatNumber(tick, 0)}</text>
        </g>
      `).join('')}
      ${xLabels.map((index) => `
        <text x="${padX + xStep * index}" y="${height - 10}" text-anchor="middle" font-size="11" fill="#647b81">${escapeHtml(rows[index].targetTime.slice(0, 5))}</text>
      `).join('')}
      <polyline fill="none" stroke="#153036" stroke-width="3" points="${points('actual')}" />
      <polyline fill="none" stroke="${MODEL_META[MODEL_ADJUSTED].color}" stroke-width="2.5" points="${points('adjusted')}" />
      <polyline fill="none" stroke="${MODEL_META[MODEL_RESIDUAL].color}" stroke-width="2.5" points="${points('residual')}" />
      <polyline fill="none" stroke="${MODEL_META[MODEL_BASE].color}" stroke-width="2.5" points="${points('base')}" />
    </svg>
  `;
}

function renderMetrics(horizon) {
  metricsStack.innerHTML = [
    renderMetricCard(horizon, MODEL_ADJUSTED, primaryWinnerKey(horizon) === MODEL_ADJUSTED),
    renderMetricCard(horizon, MODEL_RESIDUAL, primaryWinnerKey(horizon) === MODEL_RESIDUAL),
    renderMetricCard(horizon, MODEL_BASE, false, true)
  ].join('');
}

function renderMetricCard(horizon, modelKey, isWinner, isReference = false) {
  const model = horizon.models[modelKey];
  const comparisonNote = buildComparisonNote(modelKey, horizon.comparisons);
  return `
    <article class="metric-card ${isWinner ? 'is-best' : ''}">
      <div class="metric-card__top">
        <div>
          <p class="metric-card__eyebrow">${isReference ? '참고' : `${horizon.horizonMinutes}분 후`}</p>
          <h3>${escapeHtml(MODEL_META[modelKey].label)}</h3>
        </div>
        <span class="badge ${MODEL_META[modelKey].badgeClass}">${isReference ? '기준' : (isWinner ? '우세' : '비교')}</span>
      </div>
      <div class="metric-card__value">${formatNumber(model.mae, 1)} MAE</div>
      <div class="metric-grid">
        <div class="metric-grid__item">
          <div class="metric-grid__label">10명 이내</div>
          <div class="metric-grid__value">${formatNumber(model.within10Rate, 1)}%</div>
        </div>
        <div class="metric-grid__item">
          <div class="metric-grid__label">평균 보정</div>
          <div class="metric-grid__value">${formatSigned(model.averageAdjustment)}</div>
        </div>
        <div class="metric-grid__item">
          <div class="metric-grid__label">비교 건수</div>
          <div class="metric-grid__value">${formatInt(model.count)}</div>
        </div>
      </div>
      <div class="metric-card__rule">${escapeHtml(comparisonNote)}<br>${escapeHtml(model.rule || '')}</div>
    </article>
  `;
}

function buildComparisonNote(modelKey, comparisons) {
  if (modelKey === MODEL_ADJUSTED) {
    return `잔차 보정식 대비 ${formatSigned(-comparisons.residualVsAdjusted.maeDelta)}명`;
  }

  if (modelKey === MODEL_RESIDUAL) {
    return `현재 운영식 대비 ${formatSigned(comparisons.residualVsAdjusted.maeDelta)}명`;
  }

  return `참고용 기준식 · 현재 운영식 대비 ${formatSigned(comparisons.adjustedVsBase.maeDelta)}명`;
}

function renderMethodCards(horizon) {
  const specs = horizon.methodSpecs;
  const cards = [
    renderMethodCard(specs.adjusted, MODEL_ADJUSTED, horizon),
    renderMethodCard(specs.residual, MODEL_RESIDUAL, horizon),
    renderMethodCard(specs.base, MODEL_BASE, horizon, true)
  ];
  methodGrid.innerHTML = cards.join('');
}

function renderMethodCard(spec, modelKey, horizon, isReference = false) {
  const formulaText = formulaTextFor(modelKey, horizon);
  const chips = (spec.features || []).map((feature) => `
    <span class="chip">${escapeHtml(featureLabel(feature.key))} ${formatSigned(feature.coefficient, 3)}</span>
  `).join('');
  const metaText = spec.sampleCount
    ? `${formatInt(spec.sampleCount)}건 학습 · 기본식 ${formatNumber(spec.baseMae, 1)} -> ${formatNumber(spec.optimizedMae, 1)}`
    : (spec.note || '선택한 반영률만 사용합니다.');

  return `
    <article class="method-card">
      <div class="method-card__eyebrow">${isReference ? '참고식' : '비교식'}</div>
      <h3>${escapeHtml(MODEL_META[modelKey].label)}</h3>
      <div class="method-card__formula">${escapeHtml(formulaText)}</div>
      <div class="method-card__meta">${escapeHtml(metaText)}</div>
      <div class="chip-row">${chips || `<span class="chip">반영률 ${formatNumber(horizon.weight, 1)}</span>`}</div>
    </article>
  `;
}

function formulaTextFor(modelKey, horizon) {
  if (modelKey === MODEL_ADJUSTED) {
    return '기본식 + 6항 계산식 + 안전장치';
  }

  if (modelKey === MODEL_RESIDUAL) {
    return '기본식 + a×피크 압력 + b×상승 평소값 이동 + c×현재 초과분 + d×현재 부족분 + e×최근 30분 상승';
  }

  return `목표 평소값 + (현재값 - 현재 평소값) × ${formatNumber(horizon.weight, 1)}`;
}

function renderSampleTable(horizon) {
  const rows = mergedRows(horizon);
  if (!rows.length) {
    sampleRows.innerHTML = '<tr><td colspan="5">표시할 데이터가 없습니다.</td></tr>';
    return;
  }

  const picked = sampleIndexes(rows.length, Math.min(8, rows.length)).map((index) => rows[index]);
  sampleRows.innerHTML = picked.map((row) => {
    const bestError = Math.min(row.adjustedError, row.residualError);
    return `
      <tr>
        <td>${escapeHtml(row.targetTime.slice(0, 5))}</td>
        <td>${formatInt(row.actual)}</td>
        <td>${formatInt(row.base)}</td>
        <td class="${row.adjustedError === bestError ? 'is-best' : ''}">${formatInt(row.adjusted)}</td>
        <td class="${row.residualError === bestError ? 'is-best' : ''}">${formatInt(row.residual)}</td>
      </tr>
    `;
  }).join('');
}

function mergedRows(horizon) {
  const baseRows = horizon.models.base.rows || [];
  const adjustedRows = horizon.models.adjusted.rows || [];
  const residualRows = horizon.models.residual.rows || [];

  return baseRows.map((baseRow, index) => ({
    targetTime: baseRow.targetTime,
    actual: baseRow.actual,
    base: baseRow.predicted,
    adjusted: adjustedRows[index]?.predicted ?? baseRow.predicted,
    residual: residualRows[index]?.predicted ?? baseRow.predicted,
    baseError: baseRow.error,
    adjustedError: adjustedRows[index]?.error ?? baseRow.error,
    residualError: residualRows[index]?.error ?? baseRow.error
  }));
}

function sampleIndexes(length, targetCount) {
  if (length <= targetCount) return Array.from({ length }, (_, index) => index);
  if (targetCount <= 1) return [Math.floor(length / 2)];
  const lastIndex = length - 1;
  return Array.from({ length: targetCount }, (_, index) => Math.round((index * lastIndex) / (targetCount - 1)));
}

function featureLabel(key) {
  return FEATURE_LABELS[key] || key;
}

function formatNumber(value, fractionDigits = 1) {
  const number = Number(value);
  if (!Number.isFinite(number)) return '-';
  return number.toLocaleString('ko-KR', {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits
  });
}

function formatInt(value) {
  const number = Number(value);
  if (!Number.isFinite(number)) return '-';
  return Math.round(number).toLocaleString('ko-KR');
}

function formatSigned(value, fractionDigits = 1) {
  const number = Number(value);
  if (!Number.isFinite(number)) return '-';
  const absText = formatNumber(Math.abs(number), fractionDigits);
  if (number > 0) return `+${absText}`;
  if (number < 0) return `-${absText}`;
  return formatNumber(0, fractionDigits);
}

function round2(value) {
  return Math.round(Number(value) * 100) / 100;
}

function showStatus(message, isError = true) {
  statusPanel.hidden = false;
  statusPanel.textContent = message;
  statusPanel.style.color = isError ? '#8e4b1f' : '#2a5b62';
  statusPanel.style.borderColor = isError ? '#f0d5c0' : '#d0e6e8';
  statusPanel.style.background = isError ? '#fff8f2' : '#f3fbfb';
}

function hideStatus() {
  statusPanel.hidden = true;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}
