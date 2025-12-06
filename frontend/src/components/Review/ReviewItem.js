import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useSelector, useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faHeart } from "@fortawesome/free-regular-svg-icons";
import DropDown from "./DropDown";
import { postWithToken } from "../../api";

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
  width: calc(100% + 30px);
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
  idx,
  wholeReviewList,
  setWholeReviewList,
  onDeleteSuccess,
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
  } = review;

  const [likeCountState, setLikeCountState] = useState(likeCount);
  const [likeState, setLikeState] = useState(false);
  const [likeLoading, setLikeLoading] = useState(false);
  const [buttonDisabled, setButtonDisabled] = useState(false);
  const user = useSelector((state) => state.user.value);
  const token_id = user.token;
  const dispatch = useDispatch();

  useEffect(() => {
    setLikeState(like);
    setLikeCountState(likeCount);
  }, [review, idx])

  const getRestuarantIndex = (restaurantName) => {
    switch (restaurantName) {
      case "제1학생회관":
        return 1;
      case "제2학생회관":
        return 2;
      case "제3학생회관":
        return 3;
      case "상록회관":
        return 4;
      case "생활과학대":
        return 5;
      default:
        return 0;
    }
  };

  const updateWholeReviewList = (targetId, isLike) => {
    const newReviewList = wholeReviewList[idx - 1].map((review) => {
      if (review.reviewId === targetId) {
        review.like = isLike;
        review.likeCount = isLike ? review.likeCount + 1 : review.likeCount - 1;
      }
      return review;
    });
    setWholeReviewList([...wholeReviewList.slice(0, idx - 1), newReviewList, ...wholeReviewList.slice(idx)]);
  }

  const handleLike = async (targetId) => {
    if (likeLoading) return;
    setLikeLoading(true);

    if (buttonDisabled) return;
    setButtonDisabled(true);

    if (user.loginState === false) {
      dispatch(showToast({ contents: "login", toastIndex: 0 }));
      return;
    }
    
    try {
      const res = await postWithToken(`/review/like/${reviewId}`);
      const { like: isLike, mine: isMine } = res.data;
      
      if (isMine === true) { // 본인이 작성한 리뷰라 공감 불가
          dispatch(showToast({ contents: "review", toastIndex: 9 }));
          setLikeLoading(false);
          return;
      }
      
      if (isLike === true) { // 공감 했을 때
          setLikeState(true);
          setLikeCountState(likeCountState + 1);
          dispatch(showToast({ contents: "review", toastIndex: 7 }));
      } else { // 공감 취소 했을 때
          setLikeState(false);
          setLikeCountState(likeCountState - 1);
          dispatch(showToast({ contents: "review", toastIndex: 8 }));
      }
      
      updateWholeReviewList(targetId, isLike);
    } catch (error) {
        console.error(error);
        dispatch(showToast({ contents: "error", toastIndex: 0 }));
    } finally {
        setLikeLoading(false);
        setButtonDisabled(false);
    }
  };

  return (
    <ReviewItemContainer>
      {
        imgLink === "" ? null : (
          <ReviewImage
            src={imgLink}
            alt="Loading.."
          />
        )
      }
      <div className="Row1" style={{display:"flex", width: "100%", justifyContent: 'space-between'}}>
        <div>
          <span style={{fontSize: 14}}>{writer}</span>
          <ReviewItemStar style={{ fontWeight: 500 }}>
            <FontAwesomeIcon icon={solidStar} />
            {rate % 1 === 0 ? rate + ".0" : rate}
          </ReviewItemStar>
        </div>
        <div style={{position:"relative", display: 'flex', flex: 1}}>
          <span style={{ fontWeight: 400, fontSize: 14, position: "absolute", top:"2px", right:"20px" }}>
            {DisplayWriteTime(madeTime)}
          </span>
          {
            token_id
            ? (
              <div style={{ position:"absolute", right:"0px"}} >
                <DropDown targetId={reviewId} 
                targetRestaurant={getRestuarantIndex(restaurant)}
                wholeReviewList={wholeReviewList} setWholeReviewList={setWholeReviewList}
                onDeleteSuccess={onDeleteSuccess} />
              </div>
            ) : null
          }
        </div>
      </div>
      <div className="Row2" style={{ width: "100%", marginBottom: "10px" }}>
        <ReviewItemComment>{comment}</ReviewItemComment>
      </div>
      <div className="Row3" style={{width: "100%"}}>
        {mainDishList && mainDishList.map((menu, index) => (
          <MenuName key={index}>{menu}</MenuName>
        ))}
        <ReviewLike onClick={() => handleLike(reviewId)} 
          style={{pointerEvents: buttonDisabled ? "none" : "auto"}}
          className={likeState ? 'liked' : ''}
        >
          <FontAwesomeIcon icon={faHeart} /> {likeCountState}
        </ReviewLike>
      </div>
    </ReviewItemContainer>
  );
};

export default ReviewItem;
