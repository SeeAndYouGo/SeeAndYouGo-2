import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import MyReviewItem from "./MyReviewItem";
import { getWithToken } from "../../api";

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

const ReviewWrapper = styled.div`
	width: 100%;
	padding: 30px 15px;
	font-size: 14px;
	/* @media (min-width: 576px) {
		padding: 30px 20px;
	} */
`;

const MyReviewPage = () => {
	const [reviewList, setReviewList] = useState([]);
	const navigator = useNavigate();
	const nowToken = useSelector((state) => state.user.value.token);

	useEffect(() => {
		const fetchData = async () => {
			const result = await getWithToken(`/reviews/${nowToken}`);
			return result.data;
		};
		fetchData().then((result) => {
			setReviewList(result);
		});
	}, [nowToken]);

	return (
		<>
			{nowToken ? (
				<ReviewWrapper>
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
				</ReviewWrapper>
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
