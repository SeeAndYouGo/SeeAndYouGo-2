import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import { useSelector, useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faHeart } from "@fortawesome/free-regular-svg-icons";
import DropDown from "./DropDown";
import * as config from "../../config";

const ReviewItemContainer = styled.div`
  width: 100%;
  background: #fff;
  padding: 15px;
  border-radius: 15px;
  margin-top: 10px;
  float: left;
  position: relative;
`;

const ReviewItemStar = styled.span`
  > svg {
    color: #ffc107;
    margin: 0 3px;
  }
`;

const ReviewItemComment = styled.p`
  width: 100%;
  font-size: 14px;
  font-weight: 400;
  margin: 5px 0 0 0;
  white-space: pre-wrap;
`;

const MenuName = styled.p`
  font-size: 12px;
  margin: 5px 0 0 0;
  font-weight: 500;
  float: left;
  border: 1px solid #ccc;
  padding: 2px 10px;
  border-radius: 20px;
	margin-right: 5px;
`;

const ReviewImage = styled.img`
  width: 330px;
  margin: -15px 0 10px -15px;
  border-radius: 15px 15px 0 0;  
  max-height: 200px;
  object-fit: cover;
`;

const ReviewLike = styled.div`
  float: right;
  margin: 5px 0 0;
  border: solid 1px #d9d9d9;
  border-radius: 10px;
  padding: 1px 8px 0 8px;
  font-size: 12px;
  color: #777;
  font-weight: 400;
  cursor: pointer;
  &.liked {
    color: #ff0000;
    border: solid 1px #ff0000;
  }
`;

const DisplayWriteTime = (inputTime) => {
  const writeTime = inputTime.split(":").slice(0, 2).join(":");
  return writeTime;
};

const ReviewItem = ({
  review,
  isTotal,
  wholeReviewList,
  setWholeReviewList,
}) => {
  const {
    reviewId,
    restaurant,
    writer,
    madeTime,
    rate,
    comment,
    imgLink,
    likeCount,
    like,
    mainDishList,
    menuName
  } = review;
  const [likeCountState, setLikeCountState] = useState(0);
  const [likeState, setLikeState] = useState(false);
  const user = useSelector((state) => state.user.value);
  const token_id = user.token;
  const dispatch = useDispatch();

  useEffect(() => {
    setLikeCountState(likeCount);
  }, [likeCount]);

  const getRestuarantIndex = (restaurantName) => {
    switch (restaurantName) {
      case "1학생회관":
        return 1;
      case "2학생회관":
        return 2;
      case "3학생회관":
        return 3;
      case "상록회관":
        return 4;
      case "생활과학대":
        return 5;
      default:
        return 0;
    }
  };

  useEffect(() => {
    setLikeState(!like);
  }, [like]);

  const handleLike = (targetId) => {
    if (user.loginState === false) { // 로그인 안되어있을 때
      dispatch(showToast({ contents: "login", toastIndex: 0 }));
      return;
    } else {
      axios.post(config.DEPLOYMENT_BASE_URL + `/review/like/${reviewId}/${token_id}`, {
      }).then((res) => {
        const isLike = res.data.like
        const isMine = res.data.mine;
        if (isMine === true) { // 본인이 작성한 리뷰라 공감 불가
          dispatch(showToast({ contents: "review", toastIndex: 9 }));
          return;
        }
				setLikeState(!isLike);
        if (isLike === true) { // true면 공감이 된 상태
          dispatch(showToast({ contents: "review", toastIndex: 7 }));
          setLikeCountState(likeCountState + 1);
          const beforeWholeReviewList = [...wholeReviewList];
					beforeWholeReviewList[0].forEach((item) => {
						if (item.reviewId === targetId) {
							item.like = true;
							item.likeCount += 1;
							return;
						}
					});
					const nowRestaurantIndex = getRestuarantIndex(restaurant);
					beforeWholeReviewList[nowRestaurantIndex].forEach((item) => {
						if (item.reviewId === targetId) {
							item.like = true;
							item.likeCount += 1;
							return;
						}
					});
        } else { // false면 공감 취소된 상태
          dispatch(showToast({ contents: "review", toastIndex: 8 }));
          setLikeCountState(likeCountState - 1);
          const beforeWholeReviewList = [...wholeReviewList];
					beforeWholeReviewList[0].forEach((item) => {
						if (item.reviewId === targetId) {
							item.like = false;
							item.likeCount -= 1;
							return;
						}
					});
          const nowRestaurantIndex = getRestuarantIndex(restaurant);
					beforeWholeReviewList[nowRestaurantIndex].forEach((item) => {
						if (item.reviewId === targetId) {
							item.like = false;
							item.likeCount -= 1;
							return;
						}
					});
        }
      }).catch((error) => {
        console.log(error);
        dispatch(showToast({ contents: "error", toastIndex: 0 }));
      });
    }
  };

  return (
      <ReviewItemContainer>
        {
          imgLink === "" ? null : (
            <ReviewImage
              src={
                config.NOW_STATUS === 0
                  ? 'https://seeandyougo.com'+imgLink
                  : `${imgLink}`
              }
              alt="Loading.."
            />
          )
        }
        <div className="Row1" style={{display:"flex", width: "100%", justifyContent: 'space-between'}}>
          <div >
            <span style={{fontSize: 14}}>{writer}</span>
            <ReviewItemStar style={{ fontWeight: 500 }}>
              <FontAwesomeIcon icon={solidStar} />
              {rate % 1 === 0 ? rate + ".0" : rate}
            </ReviewItemStar>
          </div>
          <div style={{position:"relative", width: "200px"}}>
            <span style={{ fontWeight: 400, fontSize: 14, position: "absolute", top:"2px", right:"20px" }}>
              {DisplayWriteTime(madeTime)}
            </span>
            {/* TODO drop down 부분 token_id 살리기 true를 token_id로 변경 */}
            {
              // token_id
              true 
              ? (
                <div style={{ position:"absolute", right:"0px"}} >
                  <DropDown targetId={reviewId} 
                  targetRestaurant={getRestuarantIndex(restaurant)}
                  wholeReviewList={wholeReviewList} setWholeReviewList={setWholeReviewList}/>
                </div>
              ) : null
            }
          </div>
        </div>
        <div className="Row2" style={{ width: "100%", marginBottom: "10px" }}>
          <ReviewItemComment>{comment}</ReviewItemComment>
        </div>
        <div className="Row3" style={{width: "100%"}}>
          {/* TODO 속성값이 menuName이냐, mainDishList이냐에 따라 사용 */}
          {
            menuName === "" ? null : (
              <MenuName>{menuName}</MenuName>
            )
          }
          {mainDishList && mainDishList.map((menu, index) => (
            <MenuName key={index}>{menu}</MenuName>
          ))}
          <ReviewLike onClick={() => handleLike(reviewId)} className={likeState ? '' : 'liked'}>
            <FontAwesomeIcon icon={faHeart} /> {likeCountState}
          </ReviewLike>
        </div>
      </ReviewItemContainer>
  );
};

export default ReviewItem;
