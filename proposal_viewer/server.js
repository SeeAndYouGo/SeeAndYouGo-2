import express from 'express';
import mysql from 'mysql2/promise';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

loadDotEnv();

const app = express();
const port = Number(process.env.PORT || 4321);
const restaurants = ['제1학생회관', '제2학생회관', '제3학생회관', '상록회관', '생활과학대', '학생생활관'];

const pool = mysql.createPool({
  host: process.env.DB_HOST || '219.255.141.135',
  port: Number(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || 'seeandyougo',
  password: process.env.DB_PASSWORD || 'seeandyougo',
  database: process.env.DB_NAME || 'seeandyougo',
  waitForConnections: true,
  connectionLimit: 5
});

app.use(express.static(join(process.cwd(), 'public')));

app.get('/api/restaurants', (req, res) => {
  res.json({ restaurants });
});

app.get('/api/predictions', async (req, res) => {
  try {
    const restaurant = parseRestaurant(req.query.restaurant);
    const currentTime = parseDateTime(String(req.query.time || ''));
    const currentValue = await findLatestConnected(restaurant, currentTime);
    const currentBaseline = await baseline(restaurant, currentTime);

    const predictions = [];
    for (const horizonMinutes of [30, 60, 90]) {
      const targetTime = addMinutes(currentTime, horizonMinutes);
      const targetBaseline = await baseline(restaurant, targetTime);
      const weight = currentGapWeight(restaurant, horizonMinutes);
      const predictedValue = Math.max(0, Math.round(targetBaseline + (currentValue - currentBaseline) * weight));
      predictions.push({
        horizonMinutes,
        targetTime: formatDateTime(targetTime),
        targetBaseline: round1(targetBaseline),
        currentGapWeight: weight,
        predictedValue,
        rule: ruleLabel(restaurant, horizonMinutes)
      });
    }

    res.json({
      restaurant,
      time: formatDateTime(currentTime),
      currentValue,
      currentBaseline: round1(currentBaseline),
      baselineMode: restaurant === '학생생활관' ? '전 기간 평균' : '시즌성 평균',
      season: seasonLabel(currentTime.getMonth() + 1),
      formula: 'targetBaseline + (currentValue - currentBaseline) * currentGapWeight',
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
    const weights = {
      30: numberParam(req.query.w30, currentGapWeight(restaurant, 30)),
      60: numberParam(req.query.w60, currentGapWeight(restaurant, 60)),
      90: numberParam(req.query.w90, currentGapWeight(restaurant, 90))
    };
    const summaryOnly = String(req.query.summaryOnly || '').toLowerCase() === 'true';

    const dayRows = await loadActualDay(restaurant, date);
    if (!dayRows.length) throw new Error(`선택한 날짜의 혼잡도 데이터가 없습니다: ${restaurant}, ${date}`);

    const referenceDate = parseDateTime(`${date} 12:00:00`);
    const baselineMaps = {
      season: await loadBaselineMap(restaurant, referenceDate, true),
      allYear: await loadBaselineMap(restaurant, referenceDate, false)
    };
    const actualByTime = new Map(dayRows.map((row) => [formatTime(row.date), row.connected]));

    const horizons = [30, 60, 90].map((horizonMinutes) => {
      const rows = [];
      for (const row of dayRows) {
        const targetDate = addMinutes(row.date, horizonMinutes);
        if (dateOnly(targetDate) !== date) continue;
        const actual = actualByTime.get(formatTime(targetDate));
        if (actual === undefined) continue;

        const currentBaseline = baselineFromMaps(restaurant, row.date, baselineMaps);
        const targetBaseline = baselineFromMaps(restaurant, targetDate, baselineMaps);
        const predicted = Math.max(0, Math.round(targetBaseline + (row.connected - currentBaseline) * weights[horizonMinutes]));
        rows.push({
          time: formatTime(row.date),
          targetTime: formatTime(targetDate),
          currentValue: row.connected,
          actual,
          predicted,
          error: Math.abs(actual - predicted),
          currentBaseline: round1(currentBaseline),
          targetBaseline: round1(targetBaseline)
        });
      }

      return {
        horizonMinutes,
        weight: weights[horizonMinutes],
        rule: ruleLabel(restaurant, horizonMinutes),
        mae: mae(rows),
        within10Rate: withinRate(rows, 10),
        count: rows.length,
        rows: summaryOnly ? [] : rows
      };
    });

    res.json({
      restaurant,
      date,
      baselineMode: restaurant === '학생생활관' ? '전 기간 평균' : '시즌성 평균',
      season: seasonLabel(referenceDate.getMonth() + 1),
      parameters: {
        weights,
        formula: 'targetBaseline + (currentValue - currentBaseline) * weight',
        trainingBaseline: '2025년 같은 요일, 같은 시간대 평균',
        seasonMode: restaurant === '학생생활관' ? '전 기간 평균' : '같은 시즌 우선'
      },
      horizons
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});

app.listen(port, () => {
  console.log(`congestion prediction viewer: http://localhost:${port}`);
});

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
    // .env is optional. Defaults are enough for local review.
  }
}

function parseRestaurant(value) {
  const input = String(value || '');
  const restaurant = restaurants.find((name, index) => input.includes(name) || input.includes(String(index + 1)));
  if (!restaurant) throw new Error(`식당명이 올바르지 않습니다: ${input}`);
  return restaurant;
}

function parseDateTime(value) {
  const normalized = value.trim().replace('T', ' ');
  const match = normalized.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (!match) throw new Error('time은 yyyy-MM-ddTHH:mm 또는 yyyy-MM-dd HH:mm:ss 형식이어야 합니다.');
  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3]), Number(match[4]), Number(match[5]), Number(match[6] || 0));
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

async function findLatestConnected(restaurant, time) {
  const [rows] = await pool.query(
    'select connected from connection where restaurant = ? and time <= ? order by time desc limit 1',
    [restaurant, formatDateTime(time)]
  );
  if (!rows.length) throw new Error(`해당 시각 이전의 혼잡도 데이터가 없습니다: ${restaurant}, ${formatDateTime(time)}`);
  return Number(rows[0].connected);
}

async function baseline(restaurant, time) {
  const timeOfDay = formatTime(time);
  const dayOfWeek = mysqlDayOfWeek(time);

  if (restaurant !== '학생생활관') {
    const seasonAverage = await averageConnected(restaurant, timeOfDay, dayOfWeek, seasonMonths(time.getMonth() + 1));
    if (seasonAverage !== null) return seasonAverage;
  }

  const allYearAverage = await averageConnected(restaurant, timeOfDay, dayOfWeek, null);
  if (allYearAverage !== null) return allYearAverage;

  const [rows] = await pool.query(
    "select avg(connected) as average from connection where restaurant = ? and time >= '2025-01-01' and time < '2026-01-01' and substring(time, 12, 5) = ?",
    [restaurant, timeOfDay]
  );
  return Number(rows[0]?.average || 0);
}

async function averageConnected(restaurant, timeOfDay, dayOfWeek, months) {
  const params = [restaurant, timeOfDay, dayOfWeek];
  let sql = "select avg(connected) as average from connection where restaurant = ? and time >= '2025-01-01' and time < '2026-01-01' and substring(time, 12, 5) = ? and dayofweek(str_to_date(time, '%Y-%m-%d %H:%i:%s')) = ?";

  if (months && months.length) {
    sql += ` and month(str_to_date(time, '%Y-%m-%d %H:%i:%s')) in (${months.map(() => '?').join(', ')})`;
    params.push(...months);
  }

  const [rows] = await pool.query(sql, params);
  const value = rows[0]?.average;
  return value === null || value === undefined ? null : Number(value);
}

async function loadActualDay(restaurant, date) {
  const [rows] = await pool.query(
    'select time, connected from connection where restaurant = ? and time >= ? and time < ? order by time',
    [restaurant, `${date} 00:00:00`, `${date} 23:59:59`]
  );
  return rows.map((row) => ({
    date: parseDateTime(String(row.time)),
    connected: Number(row.connected)
  }));
}

async function loadBaselineMap(restaurant, referenceDate, seasonOnly) {
  const params = [restaurant, mysqlDayOfWeek(referenceDate)];
  let sql = "select substring(time, 12, 5) as timeOfDay, avg(connected) as average from connection where restaurant = ? and time >= '2025-01-01' and time < '2026-01-01' and dayofweek(str_to_date(time, '%Y-%m-%d %H:%i:%s')) = ?";

  if (seasonOnly) {
    const months = seasonMonths(referenceDate.getMonth() + 1);
    sql += ` and month(str_to_date(time, '%Y-%m-%d %H:%i:%s')) in (${months.map(() => '?').join(', ')})`;
    params.push(...months);
  }

  sql += ' group by substring(time, 12, 5)';
  const [rows] = await pool.query(sql, params);
  return new Map(rows.map((row) => [row.timeOfDay, Number(row.average)]));
}

function baselineFromMaps(restaurant, date, baselineMaps) {
  const time = formatTime(date);
  if (restaurant !== '학생생활관' && baselineMaps.season.has(time)) return baselineMaps.season.get(time);
  if (baselineMaps.allYear.has(time)) return baselineMaps.allYear.get(time);
  return 0;
}

function mae(rows) {
  if (!rows.length) return 0;
  return round1(rows.reduce((sum, row) => sum + row.error, 0) / rows.length);
}

function withinRate(rows, threshold) {
  if (!rows.length) return 0;
  const count = rows.filter((row) => row.error <= threshold).length;
  return round1(count * 100 / rows.length);
}

function currentGapWeight(restaurant, horizonMinutes) {
  if (restaurant === '생활과학대') return 0.0;
  if (restaurant === '학생생활관') return horizonMinutes === 30 ? 0.2 : horizonMinutes === 60 ? 0.1 : 0.0;
  return horizonMinutes === 30 ? 0.6 : horizonMinutes === 60 ? 0.4 : 0.2;
}

function ruleLabel(restaurant, horizonMinutes) {
  if (restaurant === '생활과학대') return '시즌 평소값 중심';
  if (restaurant === '학생생활관') return horizonMinutes === 90 ? '전 기간 평소값 중심' : '전 기간 평소값 + 현재 차이 약하게 반영';
  if (horizonMinutes === 30) return '시즌 평소값 + 현재 차이 크게 반영';
  if (horizonMinutes === 60) return '시즌 평소값 + 현재 차이 중간 반영';
  return '시즌 평소값 + 현재 차이 약하게 반영';
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

function mysqlDayOfWeek(date) {
  const day = date.getDay();
  return day === 0 ? 1 : day + 1;
}

function addMinutes(date, minutes) {
  return new Date(date.getTime() + minutes * 60 * 1000);
}

function formatDateTime(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
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
