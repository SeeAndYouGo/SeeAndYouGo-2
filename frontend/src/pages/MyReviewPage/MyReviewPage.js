import React, { useEffect, useState } from "react";
import MyReviewItem from "./MyReviewItem";
import * as config from "../../config";
import { useSelector } from "react-redux";

const MyReviewPage = () => {
	const [reviewArr, setReviewArr] = useState([]);
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
			setReviewArr(result);
		});
	}, [nowToken]);

	return (
		<>
			<h3 style={{ margin: "70px 0 10px 15px" }}>
				내가 쓴 총 리뷰 {reviewArr.length}개
			</h3>
			{reviewArr.length === 0 ? (
				<p key={0} style={{ textAlign: "center" }}>
					첫 리뷰를 작성해보세요!
				</p>
			) : (
				reviewArr.map((nowReview) => (
					<MyReviewItem key={nowReview.reviewId} review={nowReview} />
				))
			)}
			<div className="blankSpace" style={{marginBottom: 20}}>&nbsp;</div>
		</>
	);
};

export default MyReviewPage;
