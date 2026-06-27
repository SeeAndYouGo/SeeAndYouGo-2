import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { SyncLoader } from "react-spinners";
import { get } from "../../api";

const PredictDesc = styled.p`
  font-size: 13px;
  color: #999;
  margin-top: 8px;
  margin-bottom: 16px;
  line-height: 1.5;
`;

const TrafficItem = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
`;

const Circle = styled.div`
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background-color: ${({ $color }) => $color};
  box-shadow: 0 2px 5px ${({ $color }) => $color}88;
`;

const Divider = styled.div`
  width: 1px;
  height: 36px;
  background-color: #e8e8e8;
  margin-bottom: 6px;
  align-self: center;
`;

const CONGESTION_LEVELS = [
  { label: '원활', color: '#4caf50' },
  { label: '보통', color: '#ffc107' },
  { label: '혼잡', color: '#ff9800' },
  { label: '매우 혼잡', color: '#f44336' },
];

const toLevel = (prediction, capacity) => {
  // 0~40%: 원활(초록), 41~70%: 보통(노랑), 71~90%: 혼잡(주황), 91~100%: 매우 혼잡(빨강)
  const ratio = (prediction / capacity) * 100;
  if (ratio <= 40) return 0;
  if (ratio <= 70) return 1;
  if (ratio <= 90) return 2;
  return 3;
};

const formatTargetTime = (targetTimestamp) => {
  const timePart = targetTimestamp.includes('T')
    ? targetTimestamp.split('T')[1].slice(0, 5)
    : targetTimestamp.split(' ')[1].slice(0, 5);
  return timePart ?? '';
};

const PredictProgress = ({ restaurantId, time, capacity, ratio }) => {
  const [predictOpen, setPredictOpen] = useState(true);
  const [predictItems, setPredictItems] = useState(null);
  const [predictMessage, setPredictMessage] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!time || ratio === -1 || !restaurantId || !capacity) {
      setPredictItems(null);
      setPredictMessage(null);
      setLoading(false);
      return;
    }

    setLoading(true);
    setPredictItems(null);
    setPredictMessage(null);

    get('/connection/prediction', {
      params: {
        restaurant: `restaurant${restaurantId}`,
        observed_at: time,
      },
    }).then((res) => {
        const { status, message, predictions } = res.data;
        if (status === 'OK' && predictions?.results) {
          setPredictItems(
            predictions.results.map((item) => ({
              offset: item.horizon_min,
              time: formatTargetTime(item.target_timestamp),
              level: toLevel(item.prediction, capacity),
            }))
          );
        } else {
          setPredictItems([]);
          setPredictMessage(message || '예측 정보를 불러올 수 없습니다.');
        }
      }).catch((err) => {
        console.log(err);
        setPredictItems([]);
        setPredictMessage('예측 정보를 불러올 수 없습니다.');
      }).finally(() => {
        setLoading(false);
      });
  }, [restaurantId, time, capacity, ratio]);

  return (
    <>
      <div style={{ height: 1, width: '100%', backgroundColor: '#eee', margin: '16px 0' }} />
      <div onClick={() => setPredictOpen(v => !v)} style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', userSelect: 'none' }}>
        <p style={{ fontSize: 16, fontWeight: 700, marginRight: 4 }}>예상 혼잡도</p>
        <span className="material-symbols-outlined"style={{ marginLeft: 'auto', fontSize: 20, color: '#aaa', transition: 'transform 0.3s', transform: predictOpen ? 'rotate(0deg)' : 'rotate(180deg)' }} >
          expand_less
        </span>
      </div>

      <div style={{ overflow: 'hidden', maxHeight: predictOpen ? '200px' : '0', transition: 'max-height 0.3s ease' }}>
        <PredictDesc>
          {predictMessage ?? '과거 혼잡도 데이터를 기반으로 예측합니다.'}
        </PredictDesc>
        {loading && (
          <div style={{ textAlign: 'center', padding: '12px 0 16px' }}>
            <SyncLoader color="#a9a9a9" size={6} />
          </div>
        )}
        {!loading && predictItems?.length > 0 && (
          <div style={{ display: 'flex', justifyContent: 'space-around', alignItems: 'flex-end', paddingBottom: 4 }}>
            {predictItems.map((item, idx) => {
              const { color, label } = CONGESTION_LEVELS[item.level];
              return (
                <React.Fragment key={item.offset}>
                  <TrafficItem>
                    <span style={{ fontSize: 13, fontWeight: 600, color: '#555' }}>{item.offset}분 후</span>
                    <Circle $color={color} />
                    <span style={{ fontSize: 12, fontWeight: 600, color }}>{label}</span>
                  </TrafficItem>
                  {idx < predictItems.length - 1 && <Divider />}
                </React.Fragment>
              );
            })}
          </div>
        )}
      </div>
    </>
  );
};

export default PredictProgress;
