import React from 'react';
import styled from '@emotion/styled';
import { Link } from 'react-router-dom';
import ReviewItem from '../ReviewPage/ReviewItem';

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

const ReviewPreview = ({ data = [], idx }) => {
  return (
    <div style={{ marginTop: 20, float: 'left', width: '100%' }}>
      <p style={{ fontSize: 22, marginBottom: 6, fontWeight: 700 }}>리뷰 미리보기</p>
      <div style={{ display: 'flex', flexDirection: 'column'}}>
        {data.map((review, idx) => (
          <ReviewItem key={idx} review={review} />
        ))}
      </div>
      <ReviewLink to={`/review-page/${idx}`}>전체 리뷰 보러가기</ReviewLink>
    </div>
  );
}

export default ReviewPreview;