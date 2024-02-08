import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { Link } from "react-router-dom";
import moment from "moment";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import { faSpoon } from "@fortawesome/free-solid-svg-icons";
import DropDown from "../../components/Review/DropDown";
import * as config from "../../config";

const ReviewItemContainer = styled.div`
	width: 330px;
	background: #fff;
	padding: 5px 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
	position: relative;
`;
const ReviewItemIcon = styled.p`
	float: left;
	font-size: 35px;
	color: #555;
	margin: 0 10px 0 0;
`;
const ReviewItemProfile = styled.div`
	float: left;
	margin-top: 5px;

	> p {
		margin: 0;
		font-size: 16px;
	}
	> p:last-child {
		color: #777;
		font-weight: 400;
		font-size: 12px;
		margin-top: -2px;
	}
`;
const ReviewItemStar = styled.span`
	font-size: 12px;
	margin-left: 5px;
	> svg {
		color: #ffc107;
		font-size: 15px;
		margin-right: 2px;
	}
`;
const ReviewItemContent = styled.p`
	width: 100%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0 0 0;
`;

const DeptName = styled.p`
	width: 60px;
	margin: 10px 0px 10px 10px;
	padding-top: 2px;
	text-align: center;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 500;
`;

const MenuName = styled.p`
	font-size: 12px;
	margin: 5px 0 0 0;
	font-weight: 500;
	float: left;
	border: 1px solid #ccc;
	padding: 3px 10px;
	border-radius: 20px;
`;

const NoReviewMessage = styled.p`
	margin-top: 10px;
	font-weight: 400;
	width: 100%;
	font-size: 14px;
	text-align: center;
	color: #777;
	padding: 15px 0;
	background: #fff;
	border-radius: 20px;
`;

const CalculateWriteTime = (inputTime, nowTime) => {
	const checkMinutes = moment.duration(inputTime.diff(nowTime)).asMinutes();
	if (checkMinutes < 60) {
		return `${Math.floor(checkMinutes)}분 전`;
	} else if (checkMinutes < 1440) {
		return `${Math.floor(checkMinutes / 60)}시간 전`;
	} else {
		return `${Math.floor(checkMinutes / 1440)}일 전`;
	}
};

const ReviewItem = ({
	restaurant,
	user,
	time,
	content,
	img,
	rate,
	dept,
	reviewId,
	menuName,
}) => {
	const tempTargetTime = moment().format("YYYY-MM-DD HH:mm:ss");
	const targetTime = moment(tempTargetTime);

	return (
		<ReviewItemContainer>
			<div className="Row1" style={{ width: "100%", float: "left" }}>
				<ReviewItemIcon>
					<FontAwesomeIcon icon={faCircleUser} />
				</ReviewItemIcon>
				<ReviewItemProfile>
					<p>
						{user}
						<ReviewItemStar>
							<FontAwesomeIcon icon={solidStar} />
							{rate % 1 === 0 ? rate + ".0" : rate}
						</ReviewItemStar>
					</p>
					<span style={{ fontWeight: 400, fontSize: 12 }}>
						{CalculateWriteTime(targetTime, time)}
					</span>
				</ReviewItemProfile>
				{restaurant === "1학생회관" ? null : (
					<div style={{ display: "flex", float: "right" }}>
						<DeptName>
							{dept === "STAFF" ? "교직원" : "학생"}
						</DeptName>
					</div>
				)}
			</div>
			<div className="Row2" style={{ float: "left", width: "100%" }}>
				<ReviewItemContent>{content}</ReviewItemContent>
			</div>
			<div
				className="Row3"
				style={{ width: "100%", float: "left", marginBottom: "10px" }}
			>
				{img === "" ? null : (
					<img
						src={
							config.NOW_STATUS === 0
								? `/assets/images/${img}`
								: `${img}`
						}
						alt="Loading.."
						style={{
							maxHeight: 80,
							maxWidth: 80,
							float: "left",
							marginTop: 5,
						}}
					/>
				)}
			</div>
			{restaurant === "1학생회관" ? (
				<MenuName>
					{menuName}&nbsp;
					<FontAwesomeIcon icon={faSpoon} />
				</MenuName>
			) : null}

			<div
				style={{
					position: "absolute",
					right: 15,
					bottom: 10,
				}}
			>
				<DropDown targetId={reviewId} />
			</div>
		</ReviewItemContainer>
	);
};

const TopReview = ({ idx }) => {
	const [reviewArr, setReviewArr] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/top-review/restaurant${idx}` +
				(config.NOW_STATUS === 0 ? ".json" : "");
			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			setReviewArr(data);
		});
	}, [idx]);

	return (
		<div style={{ width: "100%", float: "left" }}>
			<p style={{ fontSize: 18, marginBottom: 10 }}>
				리뷰 미리보기
			<Link 
				to={`/review-page/${idx}`}
				style={{float:"right", fontWeight: 400, fontSize: 14, color: "royalblue"}}>
					전체보기 {'>'}
			</Link>
			</p>
			{reviewArr.length === 0 ? (
				<NoReviewMessage>
					첫 리뷰의 주인공이 되어주세요!
				</NoReviewMessage>
			) : (
				reviewArr.map((el, index) => (
					<ReviewItem
						key={index}
						restaurant={el.restaurant}
						user={el.writer}
						time={el.madeTime}
						rate={el.rate}
						content={el.comment}
						img={el.imgLink}
						dept={el.dept}
						reviewId={el.reviewId}
						menuName={el.menuName}
						likeCount={el.likeCount}
						liked={el.like}
					/>
				))
			)}
		</div>
	);
};

export default TopReview;
