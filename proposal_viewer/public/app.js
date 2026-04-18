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
const chart = document.getElementById('chart');
const comparisonRows = document.getElementById('comparisonRows');

dateInput.value = '2026-03-18';
syncDefaultWeights();

restaurantInput.addEventListener('change', syncDefaultWeights);

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  resultPanel.hidden = false;
  summary.textContent = '선택한 날짜의 실제값과 예측값을 비교하는 중입니다.';
  predictionCards.innerHTML = '';
  chart.innerHTML = '';
  comparisonRows.innerHTML = '';

  const params = new URLSearchParams({
    restaurant: restaurantInput.value,
    date: dateInput.value,
    w30: weightInputs[30].value,
    w60: weightInputs[60].value,
    w90: weightInputs[90].value
  });

  try {
    const response = await fetch(`/api/history?${params}`);
    const data = await readJsonResponse(response);
    if (!response.ok) throw new Error(data.message || '검증 데이터를 가져오지 못했습니다.');
    render(data);
  } catch (error) {
    summary.textContent = error.message;
  }
});

function render(data) {
  summary.innerHTML = `
    <h2>${data.restaurant} · ${data.date}</h2>
    <p>평균 기준 <strong>${data.baselineMode}</strong>, 시즌 <strong>${data.season}</strong>, 기준 데이터 <strong>${data.parameters.trainingBaseline}</strong></p>
  `;

  predictionCards.innerHTML = data.horizons.map((item) => `
    <article class="card">
      <span>${item.horizonMinutes}분 후</span>
      <strong>MAE ${item.mae}</strong>
      <dl>
        <dt>비교 건수</dt><dd>${item.count}개</dd>
        <dt>10명 이내</dt><dd>${item.within10Rate}%</dd>
        <dt>반영률</dt><dd>${item.weight}</dd>
        <dt>규칙</dt><dd>${item.rule}</dd>
      </dl>
    </article>
  `).join('');

  chart.innerHTML = data.horizons.map((item) => `
    <section class="mini-chart">
      <h3>${item.horizonMinutes}분 후 실제값 · 예측값</h3>
      ${chartSvg(item.rows)}
    </section>
  `).join('');

  const allRows = data.horizons.flatMap((horizon) =>
    horizon.rows.slice(0, 40).map((row) => ({ ...row, horizonMinutes: horizon.horizonMinutes }))
  );
  comparisonRows.innerHTML = allRows.map((row) => `
    <tr>
      <td>${row.time}</td>
      <td>${row.horizonMinutes}분 후</td>
      <td>${row.targetTime}</td>
      <td>${row.actual}명</td>
      <td>${row.predicted}명</td>
      <td>${row.error}명</td>
    </tr>
  `).join('');
}

async function readJsonResponse(response) {
  const contentType = response.headers.get('content-type') || '';
  const body = await response.text();
  if (!contentType.includes('application/json')) {
    const hint = body.trim().startsWith('<!DOCTYPE') || body.trim().startsWith('<html')
      ? 'HTML 문서가 응답됐습니다. 독립 데모 서버 주소인 http://localhost:4321 에서 열었는지 확인해 주세요.'
      : 'JSON이 아닌 응답이 돌아왔습니다.';
    throw new Error(`${hint} 요청 주소: ${response.url}`);
  }
  return JSON.parse(body);
}

function chartSvg(rows) {
  const points = rows.filter((_, index) => index % 2 === 0);
  if (!points.length) return '<p class="empty">비교할 데이터가 없습니다.</p>';

  const width = 960;
  const height = 260;
  const pad = { left: 42, right: 20, top: 18, bottom: 34 };
  const values = points.flatMap((point) => [point.actual, point.predicted]);
  const min = Math.min(...values, 0);
  const max = Math.max(...values, 1);
  const actualPath = pathFor(points, 'actual', min, max, width, height, pad);
  const predictedPath = pathFor(points, 'predicted', min, max, width, height, pad);

  return `
    <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="실제값 예측값 비교 그래프">
      <line x1="${pad.left}" y1="${height - pad.bottom}" x2="${width - pad.right}" y2="${height - pad.bottom}" stroke="#d9dfd6"/>
      <path d="${actualPath}" fill="none" stroke="#2563eb" stroke-width="3" stroke-linejoin="round" stroke-linecap="round"/>
      <path d="${predictedPath}" fill="none" stroke="#d4772f" stroke-width="3" stroke-dasharray="8 6" stroke-linejoin="round" stroke-linecap="round"/>
      <text x="${pad.left}" y="14" fill="#2563eb" font-size="13">실제값</text>
      <text x="${pad.left + 70}" y="14" fill="#d4772f" font-size="13">예측값</text>
      ${axisLabels(points, width, height, pad)}
    </svg>
  `;
}

function pathFor(points, key, min, max, width, height, pad) {
  const plotW = width - pad.left - pad.right;
  const plotH = height - pad.top - pad.bottom;
  return points.map((point, index) => {
    const x = pad.left + (points.length === 1 ? plotW / 2 : index * plotW / (points.length - 1));
    const y = pad.top + plotH - ((point[key] - min) / (max - min || 1)) * plotH;
    return `${index === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
  }).join(' ');
}

function axisLabels(points, width, height, pad) {
  const every = Math.max(1, Math.floor(points.length / 6));
  return points.map((point, index) => {
    if (index % every !== 0 && index !== points.length - 1) return '';
    const x = pad.left + (points.length === 1 ? (width - pad.left - pad.right) / 2 : index * (width - pad.left - pad.right) / (points.length - 1));
    return `<text x="${x.toFixed(1)}" y="${height - 10}" fill="#6f7d72" font-size="11" text-anchor="middle">${point.targetTime}</text>`;
  }).join('');
}

function syncDefaultWeights() {
  const restaurant = restaurantInput.value;
  if (restaurant === '생활과학대') {
    weightInputs[30].value = '0.0';
    weightInputs[60].value = '0.0';
    weightInputs[90].value = '0.0';
    return;
  }
  if (restaurant === '학생생활관') {
    weightInputs[30].value = '0.2';
    weightInputs[60].value = '0.1';
    weightInputs[90].value = '0.0';
    return;
  }
  weightInputs[30].value = '0.6';
  weightInputs[60].value = '0.4';
  weightInputs[90].value = '0.2';
}
