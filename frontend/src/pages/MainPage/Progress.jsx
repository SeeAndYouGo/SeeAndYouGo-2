import React from "react";
import styled from "@emotion/styled";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  background-color: #fff;
  border-radius: 10px;
  padding: 15px 20px;
`;

const ProgressContainer = styled.div`
  background-color: #d9d9d9;
  width: 100%;
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

const Text = [
  '매우 원활',
  '원활',
  '보통',
  '혼잡',
  '매우 혼잡'
];

const Progress = ({ ratio = 0, time }) => {
  const nowTime = time?.split(" ")[1].split(":").slice(0, 2).join(":");

  return (
    <>
      <p style={{ fontSize: 22, marginBottom: 6, fontWeight: 700 }}>혼잡도</p>
      <Container>
        <div style={{ display: 'flex' }}>
          <p style={{ fontSize: 18, fontWeight: 700 }}>
            {Text[Math.floor(ratio / 20)]}
          </p>
          <span class="material-symbols-outlined" style={{ fontSize: 14, lineHeight: '18px', marginLeft: 'auto', color: '#999', marginRight: 5}}>schedule</span>
          <p style={{ fontSize: 14, color: '#999', fontWeight: 400 }}>{nowTime} 기준</p>
        </div>
        <ProgressContainer>
          <ProgressBar $value={ratio} />
        </ProgressContainer>
      </Container>
    </>
  );
}

export default Progress;