import React, { useEffect, useState } from "react";
import TodayMenu from "./TodayMenu";
import ReviewWriteForm from "./ReviewForm";
import { useSelector } from "react-redux";
import TopReview from "./TopReview";
import * as config from "../../config";

const NewPage = () => {
	// nowRestaurant: 현재 접속한 탭의 식당 번호; redux로 selectedRestaurant에서 가져옴
	const nowRestaurant = 2;
	const nowDept = useSelector((state) => state.dept).value;
	const [menuArray, setMenuArray] = useState([]);
	const [reviewArray, setReviewArray] = useState([]);

	useEffect(() => {
    // TODO url 설정 필요
		const nowUrl = "https://seeandyougo.com/test/api/review/restaurant1";

		fetch(nowUrl, {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
			},
		})
			.then((res) => res.json())
			.then((data) => {
				setReviewArray(data);
				console.log(data);
			});
	}, []);

	useEffect(() => {
    // TODO url 설정 필요
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/daily-menu/restaurant2` +
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
		fetchData()
			.then((data) => {
				setMenuArray(data);
			})
			.catch((err) => {
				console.log(err);
			});
	}, []);

	return (
		<div>
			<h3 style={{ marginBottom: 50 }}>NEW 오늘의 메뉴, 메뉴 작성 확인하기</h3>
			{menuArray.length === 0 ? (
				<div>메뉴가 없습니다.</div>
			) : (
				<TodayMenu todayMenuData={menuArray} />
			)}
			<ReviewWriteForm restaurantNum={nowRestaurant} deptNum={nowDept} />
			<TopReview idx={nowRestaurant} nowReviewList={reviewArray} />
		</div>
	);
};

export default NewPage;
