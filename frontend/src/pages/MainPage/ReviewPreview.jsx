import React, { useEffect, useState } from 'react';
import styled from '@emotion/styled';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';
import axios from 'axios';
import * as config from '../../config';
import ModalLocation from '../RestaurantDetailPage/ModalLocation';
import Modal from '../../components/Modal';

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

const ReviewPreview = ({ idx }) => {
  const [reviewArr, setReviewArr] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      const res = await axios.get(`${config.BASE_URL}/top-review/restaurant${idx}`);
      setReviewArr(res.data);
    };
    fetchData();
  }, [idx]);

  return (
    <div style={{ marginTop: 20, float: 'left', width: '100%' }}>
      <p style={{ fontSize: 22, marginBottom: 6, fontWeight: 700 }}>리뷰 미리보기</p>
      {reviewArr.map((review, idx) => (
        <Item key={idx}>
          <img
            style={{ width: '100%', maxHeight: 200, objectFit: 'cover' }} 
            // 추후 수정
            src={'https://seeandyougo.com/'+review.imgLink}
          />
          <div style={{ padding: '15px 20px' }}>
            <div style={{ display: 'flex', flexDirection: 'row', gap: 4 }}>
              <p style={{ fontSize: 18 }}>
                {review.writer}
              </p>
              <FontAwesomeIcon icon={faStar} style={{color: "#ffd700", marginTop: 2, marginLeft: 4 }} />
              <p>{review.rate}</p>
              <p style={{ marginLeft: 'auto', fontSize: 14, color: '#555', fontWeight: 400 }}>
                {review.madeTime}
              </p>
            </div>
            <p style={{ marginTop: 4, fontSize: 14, fontWeight: 400 }}>{review.comment}</p>
          </div>
        </Item>
      ))}
      <ReviewLink to={`/review-page/${idx}`}>전체 리뷰 보러가기</ReviewLink>
    </div>
  );
}

export default ReviewPreview;