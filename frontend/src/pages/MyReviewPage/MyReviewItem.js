import React from "react";
import styled from "@emotion/styled";
import { useSelector, useDispatch } from "react-redux";
import { FaStar, FaStarHalf } from "react-icons/fa";
import { showToast } from "../../redux/slice/ToastSlice";
import { deleteWithToken } from "../../api";

const ReviewItemContainer = styled.div`
	width: 100%;
	background: #fff;
	padding: 6px 15px 10px 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
	position: relative;
`;

const DivContainer = styled.div`
	width: 100%;
	float: left;
`;

const RestaurantName = styled.span`
	font-size: 12px;
	font-weight: 500;
	color: #555555;
	margin: 0 auto;
	width: 100%;

	& > .colorTag1 {
		color: #ff0000;
	}
	& > .colorTag2 {
		color: #ff8000;
	}
	& > .colorTag3 {
		color: #f4aa19;
	}
	& > .colorTag4 {
		color: #07903e;
	}
	& > .colorTag5 {
		color: #2274ee;
	}
`;

const DeptName = styled.span`
	padding: 2px 5px;
	margin: 2px 0 0 10px;
	background-color: rgba(0, 0, 0, 0.3);
	color: white;
	border-radius: 5px;
	font-size: 11px;
	font-weight: 400;
`;

const MenuName = styled.p`
	font-size: 12px;
  margin: 0 5px 5px 0;
  font-weight: 500;
  float: left;
  border: 1px solid #ccc;
  padding: 2px 10px;
  border-radius: 20px;
`;

const ReviewItemContent = styled.p`
	width: 100%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0;
	white-space: pre-wrap;
`;

const ReviewImage = styled.img`
	max-height: 80px;
	max-width: 80px;
	float: left;
	margin: 5px 0;
`;

const RemoveButton = styled.p`
	float: right;
	font-size: 12px;
	font-weight: 300;
	margin: 0;
	padding: 3px 8px;
	background: #eee;
	border-radius: 10px;
	cursor: pointer;
`;

const DisplayWriteTime = (inputTime) => {
  const writeTime = inputTime.split(":").slice(0, 2).join(":");
  return writeTime;
};

const StarRating = ({ rating }) => {
	const renderStars = () => {
		const stars = [];

		for (let i = 0; i < 5; i++) {
			if (rating - i === 0.5) {
				stars.push(
					<div key={i * 0.01} style={{ position: "relative" }}>
						<div style={{ position: "absolute" }}>
							<FaStarHalf color="#FFD167" size={20} />
						</div>
					</div>
				);
				stars.push(<FaStar key={i * 0.1} color="#EDEFF0" size={20} />);
			} else if (rating - i > 0) {
				stars.push(<FaStar key={i * 0.1} color="#FFD167" size={20} />);
			} else {
				stars.push(<FaStar key={i * 0.1} color="#EDEFF0" size={20} />);
			}
		}
		return stars;
	};

	return <div style={{ display: "flex" }}>{renderStars()}</div>;
};

const MyReviewItem = ({ review, beforeReviewList, setReviewList }) => {
	const {
		reviewId,
		restaurant,
		dept,
		madeTime,
		mainDishList,
		rate,
		comment,
		imgLink,
	} = review;
	const nowToken = useSelector((state) => state.user.value.token);
	const dispatch = useDispatch();

	const removeReview = () => {
		if (window.confirm("이 리뷰를 삭제하시겠습니까?") === true) {
			deleteWithToken(`/reviews/${reviewId}`)
				.then((res) => {
					console.log(res, '리뷰 삭제 확인');
					if (res.data.success === true) { // 리뷰 삭제 성공
						dispatch(showToast({ contents: "review", toastIndex: 3 }));
						const updatedReviewArr = beforeReviewList.filter(
							(item) => item.reviewId !== reviewId
						);
						setReviewList(updatedReviewArr);
					} else { // 리뷰 삭제 권한이 없어 삭제 불가
						dispatch(showToast({ contents: "review", toastIndex: 2 }));
					}
				})
				.catch(() => { // 리뷰 삭제 실패
					dispatch(showToast({ contents: "review", toastIndex: 4 }));
				});
		} else {
			return;
		}
	};

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

	return (
		<>
			<ReviewItemContainer>
				<DivContainer style={{marginBottom: 5}}>
					<RestaurantName>
						<span className={"colorTag" + getRestuarantIndex(restaurant)}>
							●&nbsp;
						</span>
						{restaurant}
					</RestaurantName>
					{getRestuarantIndex(restaurant) !== 1 ? (
						<DeptName>{dept === "STAFF" ? "교직원식당" : "학생식당"}</DeptName>
					) : null}
					<RemoveButton id="remove" onClick={removeReview}>
						삭제
					</RemoveButton>
				</DivContainer>
				<hr />
				{mainDishList && mainDishList.map((menu, index) => (
					<MenuName key={index}>{menu}</MenuName>
				))}
				<DivContainer>
					<div style={{ float: "left" }}>
						<div style={{ display: "flex" }}>
							<StarRating rating={rate} />
							<span style={{ fontWeight: 400, fontSize: 13, padding: "4px 0 0 5px" }}>
              {DisplayWriteTime(madeTime)}
            </span>
						</div>
					</div>
				</DivContainer>
				<DivContainer>
					<ReviewItemContent>{comment}</ReviewItemContent>
					{imgLink === "" ? null : (
						<ReviewImage
							src={imgLink}
							alt="Loading.."
						/>
					)}
				</DivContainer>
			</ReviewItemContainer>
		</>
	);
};

export default MyReviewItem;
