import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import MyReviewItem from "./MyReviewItem";
import * as config from "../../config";

const NotLogin = styled.div`
	position: absolute;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	background-color: rgba(20, 20, 20, 0.3);
	z-index: 6;
	text-align: center;
	font-size: 20px;
	text-decoration: underline;
	padding-top: 100px;
`;

const GoToLogin = styled.span`
	cursor: pointer;
	:hover {
		color: red;
		opacity: 0.7;
	}
`;

const MyReviewPage = () => {
	const [reviewList, setReviewList] = useState([]);
	const navigator = useNavigate();
	const nowToken = useSelector((state) => state.user.value.token);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.NOW_STATUS === 0
					? config.BASE_URL + "/total-review.json"
					: config.BASE_URL + `/reviews/${nowToken}`;
			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((result) => {
			setReviewList(result);
		});
	}, [nowToken]);

	return (
		<>
			{nowToken ? (
				<div style={{ fontSize: 14, margin: "70px 15px 0 15px" }}>
					<div style={{ textAlign: "center" }}>
						<p style={{ fontSize: 20, margin: 10 }}>작성한 리뷰</p>
						<p style={{ margin: 0, fontWeight: 600 }}>
							내가 작성한 리뷰를 확인하세요!
						</p>
					</div>
					<p style={{ margin: "10px 10px 5px" }}>
						내가 작성한 총 리뷰 {reviewList ? reviewList.length : 0}개
					</p>
					{reviewList.length === 0 ? (
						<p key={0} style={{ textAlign: "center" }}>
							첫 리뷰를 작성해보세요!
						</p>
					) : (
						reviewList.map((nowReview) => (
							<MyReviewItem
								key={nowReview.reviewId}
								review={nowReview}
								beforeReviewList={reviewList}
								setReviewList={setReviewList}
							/>
						))
					)}
					<div className="blankSpace" style={{ marginBottom: 20 }}>&nbsp;</div>
				</div>
			) : (
				<NotLogin>
					<GoToLogin
						onClick={() => {
							navigator("/login-page");
						}}
					>
						로그인이 필요합니다 !!
					</GoToLogin>
				</NotLogin>
			)}
		</>
	);
};

export default MyReviewPage;
