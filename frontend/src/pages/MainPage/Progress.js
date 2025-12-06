import React from "react";
import styled from "@emotion/styled";
import { Tooltip } from 'react-tooltip';

const Container = styled.div`
  display: flex;
  position: relative;
  flex-direction: column;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  background-color: #fff;
  border-radius: 10px;
  padding: 15px 20px;
`;

const ProgressContainer = styled.div`
  background-color: #d9d9d9;
  width: 100%;
  overflow: hidden;
  height: 10px;
  border-radius: 5px;
  margin-top: 6px;
`;

const ProgressBar = styled.div`
  width: ${({ $value }) => $value}%;
  background-color: #ff6b6b;
  height: 100%;
  border-radius: 5px;
  background-color: ${({ $value }) => {
    if ($value <= 25) return '#4caf50';
    if ($value <= 50) return '#ffeb3b';
    if ($value <= 75) return '#ff9800';
    return '#f44336';
  }};
`;

const ContentBlur = styled.div`
	position: absolute;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	background-color: rgba(20, 20, 20, 0.25);
	z-index: 6;
	border-radius: 10px;
	text-align: center;
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: 16px;
	cursor: default;
	backdrop-filter: blur(1.5px);
`;

const TooltipContent = styled.p`
  font-size: 14px;
  font-weight: 500;
  color: #999;
  white-space: pre;
`;

const Text = [
  '매우 원활',
  '원활',
  '보통',
  '혼잡',
  '매우 혼잡',
  ''
];

const Progress = ({ ratio = 0, time }) => {
  const nowTime = time?.split(" ")[1].split(":").slice(0, 2).join(":");

  return (
    <div>
      <div style={{ display: 'flex'}}>
        <p style={{ fontSize: 22, marginBottom: 6, fontWeight: 700 }}>혼잡도</p>
        <span 
          data-tooltip-id='progress-tooltip' 
          data-tooltip-offset={0} 
          className="material-symbols-outlined"
          style={{ cursor: 'pointer', fontSize: 18, lineHeight: '25px', color: '#999', marginLeft: 3}}
        >
            help
        </span>
        <Tooltip
          id='progress-tooltip'
          place='top-start'
          effect='solid'
          className='tooltipContent'
          style={{
            padding: '12px',
            borderRadius: '10px',
            backgroundColor: '#fff',
            boxShadow: '-1px -1px 5px rgba(0, 0, 0, 0.15), 1px 1px 5px rgba(0, 0, 0, 0.15)',
          }}
        >
          <TooltipContent>
            학생 식당에 있는 Wifi와 연결된 기기의 접속자 수를 통해{'\n'}
            얻은 데이터로, 실제 인원 수와 다를 수 있습니다.{'\n'}
            혼잡도 정보는 06:00 ~ 19:30 사이에만 제공됩니다.
          </TooltipContent>
        </Tooltip>
      </div>
      <Container>
        <div style={{ display: 'flex' }}>
          <p style={{ fontSize: 18, fontWeight: 700 }}>
            {Text[Math.floor(ratio === -1 ? 5 : ratio / 20)]}
          </p>
          <span className="material-symbols-outlined" style={{ fontSize: 14, lineHeight: '18px', marginLeft: 'auto', color: '#999', marginRight: 5}}>schedule</span>
          <p style={{ fontSize: 14, color: '#999', fontWeight: 400 }}>{nowTime} 기준</p>
        </div>
        <ProgressContainer>
          <ProgressBar $value={Math.max(ratio, 0)} />
        </ProgressContainer>
        { ratio === -1 &&
          <ContentBlur> 
            <p>혼잡도 정보를 제공하지 않는 시간대입니다.</p>
          </ContentBlur>
        }
      </Container>
    </div>
  );
}

export default Progress;