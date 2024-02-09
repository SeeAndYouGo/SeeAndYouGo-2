import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { Link } from "react-router-dom";
import TopReviewItem from "./TopReviewItem";
import * as config from "../../config";

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

const TopReview = ({ idx, nowDept }) => {
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

	const toTotalButtonStyle = {
		float: "right",
		fontWeight: 400,
		fontSize: 14,
		color: "royalblue",
		marginTop: 3,
	};

	return (
		<div style={{ width: "100%", float: "left" }}>
			<p style={{ fontSize: 18, marginBottom: 10 }}>
				리뷰 미리보기
				<Link to={`/review-page/${idx}`} style={toTotalButtonStyle}>
					전체보기 {">"}
				</Link>
			</p>
			{reviewArr.length === 0 ? (
				<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
			) : (
				reviewArr.map((nowData, index) => {
					return nowDept === nowData.dept ? (
						<TopReviewItem key={index} nowReview={nowData} />
					) : null;
				})
			)}
		</div>
	);
};

export default TopReview;
