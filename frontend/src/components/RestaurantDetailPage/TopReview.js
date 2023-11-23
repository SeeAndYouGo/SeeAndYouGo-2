import styled from "@emotion/styled";
import React, { useState, useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import moment from "moment";

const ReviewItemContainer = styled.div`
	width: 330px;
	background: #fff;
	padding: 5px 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
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

const ReviewItem = ({ user, time, content, img, rate, dept }) => {
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
				<div style={{ display: "flex", float: "right" }}>
					<DeptName>
						{dept === "STAFF" ? "교직원" : "학생"}
					</DeptName>
				</div>

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
						src={`/assets/images/${img}`}
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
		</ReviewItemContainer>
	);
};

const TopReview = ({ idx }) => {
	const [reviewArr, setReviewArr] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			// const nowUrl = `http://localhost:8080/api/topReview/restaurant${idx}`;
			const nowUrl = `http://27.96.131.182/api/topReview/restaurant${idx}`;
			// const nowUrl = "/assets/json/restaurant1Review.json";
			const res = await fetch(nowUrl, {
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
	}, []);

	return (
		<div style={{ width: "100%", float: "left" }}>
			<p style={{ fontSize: 18, marginBottom: 10 }}>
				오늘의 리뷰 미리보기
			</p>
			{reviewArr.length === 0 ? (
				<>
					<p
						style={{
							marginTop: 20,
							width: "100%",
							fontSize: 16,
							textAlign: "center",
						}}
					>
						첫 리뷰의 주인공이 되어주세요!!
					</p>
				</>
			) : (
				reviewArr.map((el, index) => (
					<ReviewItem
						key={index}
						user={el.writer}
						time={el.madeTime}
						rate={el.rate}
						content={el.comment}
						img={el.imgLink}
						dept={el.dept}
					/>
				))
			)}
		</div>
	);
};

export default TopReview;
