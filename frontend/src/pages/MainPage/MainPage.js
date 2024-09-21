import React, { useEffect, useState } from "react";
import "../../App.css";
import TabBar from "./TabBar";
import * as config from "../../config";
import Info from "./Info";
import Progress from "./Progress";
import TopReview from "./TopReview";
import TodayMenu from "./TodayMenu";
import ReviewWriteForm from "./ReviewForm";
import { useSelector } from "react-redux";
import axios from "axios";
import { useDispatch } from "react-redux";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeDept } from "../../redux/slice/DeptSlice";
import { setSelectedRestaurant } from "../../redux/slice/UserSlice";
import MenuInfoForRestaurant1 from "../RestaurantDetailPage/MenuInfoForRestaurant1";

const MainPage = () => {
	const [restaurantData, setRestaurantData] = useState([]);
	const [menuData, setMenuData] = useState([]);
	const [topReviewData, setTopReviewData] = useState([]);
	const token = useSelector((state) => state.user).value.token;
	console.log("token", token);
	const restaurantId = useSelector((state) => state.user).value
		.selectedRestaurant;
	const nowDept = useSelector((state) => state.dept).value;
	const ratio =
		(restaurantData[restaurantId - 1]?.connected /
			restaurantData[restaurantId - 1]?.capacity) *
		100;
	const dispatch = useDispatch();

	useEffect(() => {
		dispatch(changeDept(1));
		if (restaurantId === 2) {
			dispatch(changeMenuType("BREAKFAST"));
		}
	}, [restaurantId, dispatch]);

	const handleSetRestaurantId = (id) => {
		dispatch(setSelectedRestaurant(id));
	};

	const fetchRestaurantData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 5; i++) {
				const response = await axios.get(
					`${config.BASE_URL}/connection/restaurant${i + 1}`
				);
				results.push(response.data);
			}
			setRestaurantData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	const fetchMenuData = async () => {
		const results = [[]];
		try {
			// 1학은 메뉴 정보 필요 없음
			for (let i = 1; i < 5; i++) {
				const response = await axios.get(
					`${config.BASE_URL}/daily-menu/restaurant${i + 1}`
				);
				results.push(response.data);
			}
			setMenuData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	const fetchTopReviewData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 5; i++) {
				const response = await axios.get(
					`${config.BASE_URL}/review/restaurant${i + 1}/${token}`
				);
				results.push(response.data);
			}
			setTopReviewData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	useEffect(() => {
		fetchRestaurantData();
		fetchMenuData();
		fetchTopReviewData();
	}, []);

	return (
		<div className="App">
			<TabBar
				restaurantId={restaurantId}
				setRestaurantId={handleSetRestaurantId}
			/>
			<Info idx={restaurantId} />
			<Progress
				ratio={ratio}
				time={restaurantData[restaurantId - 1]?.dateTime}
			/>
			{restaurantId === 1 ? (
				<MenuInfoForRestaurant1 />
			) : (
				<TodayMenu idx={restaurantId} data={menuData[restaurantId - 1]} />
			)}
			<ReviewWriteForm restaurantNum={restaurantId} deptNum={nowDept} />
			<TopReview
				nowReviewList={topReviewData[restaurantId - 1]}
				idx={restaurantId}
				wholeReviewList={topReviewData}
				setWholeReviewList={setTopReviewData}
			/>
		</div>
	);
};

export default MainPage;
