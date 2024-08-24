import React from 'react';
import styled from '@emotion/styled';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';

const Item = styled.div`
  display: flex;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  background-color: #fff;
  border-radius: 10px;
  flex-direction: column;
  overflow: hidden;
  margin-bottom: 10px;
`;

const ReviewLink = styled(Link)`
  width: 100%;
  height: 40px;
  background-color: #111;
  color: #fff;
  display: block;
  border-radius: 10px;
  text-align: center;
  line-height: 40px;
  margin-top: 15px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
`;

const ReviewPreview = ({ }) => {
  return (
    <div style={{ marginTop: 20 }}>
      <p style={{ fontSize: 22, marginBottom: 6, fontWeight: 700 }}>리뷰 미리보기</p>
      <Item>
        <img
          style={{ width: '100%', maxHeight: 200, objectFit: 'cover' }} 
          src='https://mblogthumb-phinf.pstatic.net/20161018_241/gosk8150_1476794457409nQRtS_JPEG/KakaoTalk_20161018_193910652.jpg?type=w800'
        />
        <div style={{ padding: '15px 20px' }}>
          <div style={{ display: 'flex', flexDirection: 'row', gap: 4 }}>
            <p style={{ fontSize: 18 }}>병아리</p>
						<FontAwesomeIcon icon={faStar} style={{color: "#ffd700", marginTop: 2, marginLeft: 4 }} />
            <p>5.0</p>
            <p style={{ marginLeft: 'auto', fontSize: 14, color: '#555', fontWeight: 400 }}>10일전</p>
          </div>
          <p style={{ marginTop: 4, fontSize: 14, fontWeight: 400 }}>충대생이라면 꼭 먹어봐야 하는 알밥!!! 짭짤한 거 좋아하는 제 입맛에 딱 맞았어요.</p>
        </div>
      </Item>

      <Item>
        <div style={{ padding: '15px 20px' }}>
          <div style={{ display: 'flex', flexDirection: 'row', gap: 4 }}>
            <p style={{ fontSize: 18 }}>병아리</p>
						<FontAwesomeIcon icon={faStar} style={{color: "#ffd700", marginTop: 2, marginLeft: 4 }} />
            <p>5.0</p>
            <p style={{ marginLeft: 'auto', fontSize: 14, color: '#555', fontWeight: 400 }}>10일전</p>
          </div>
          <p style={{ marginTop: 4, fontSize: 14, fontWeight: 400 }}>충대생이라면 꼭 먹어봐야 하는 알밥!!! 짭짤한 거 좋아하는 제 입맛에 딱 맞았어요.</p>
        </div>
      </Item>
      
      <ReviewLink>전체 리뷰 보러가기</ReviewLink>
    </div>
  );
}

export default ReviewPreview;