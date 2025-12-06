import React, { useEffect, useState } from "react";
import "../../App.css";
import SwipeableTab from "./SwipeableTab";
import Info from "./Info";
import Progress from "./Progress";
import TopReview from "./TopReview";
import TodayMenu from "./TodayMenu";
import ReviewWriteForm from "./ReviewForm";
import { useSelector, useDispatch } from "react-redux";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeDept } from "../../redux/slice/DeptSlice";
import { setSelectedRestaurant } from "../../redux/slice/UserSlice";
import MenuInfoForRestaurant1 from "../RestaurantDetailPage/MenuInfoForRestaurant1";
import Loading from "../../components/Loading";
import { get, getWithToken } from "../../api/index";
import LoginModal from "../../components/LoginModal";

const MainPage = () => {
	const [loading, setLoading] = useState(true);
	const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
	const [restaurantData, setRestaurantData] = useState([]);
	const [menuData, setMenuData] = useState([]);
	const [topReviewData, setTopReviewData] = useState([]);
	const restaurantId = useSelector((state) => state.user).value
		.selectedRestaurant;
	const nowDept = useSelector((state) => state.dept).value;
	const ratio = // 백엔드에서 받아온 혼잡도 데이터가 -1 인 경우 -1로 전달
		restaurantData[restaurantId - 1]?.connected === -1
			? -1
			: (restaurantData[restaurantId - 1]?.connected /
					restaurantData[restaurantId - 1]?.capacity) *
			  100;
	const dispatch = useDispatch();

	useEffect(() => {
		if (restaurantId === 6) {
			dispatch(changeDept("DORM_A"));
		} else {
			dispatch(changeDept("STUDENT"));
		}
		if (restaurantId === 2) {
			dispatch(changeMenuType(1));
		}
	}, [restaurantId, dispatch]);

	const handleSetRestaurantId = (id) => {
		dispatch(setSelectedRestaurant(id));
	};

	const fetchRestaurantData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 6; i++) {
				const response = await get(`/connection/restaurant${i + 1}`);
				results.push(response.data);
			}
			setRestaurantData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	const fetchMenuData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 6; i++) {
				const response = await get(`/daily-menu/restaurant${i + 1}`);
				if (i === 0) {
					const tempObject = {};
					for (let j = 0; j < response.data.length; j++) {
						tempObject[response.data[j].mainDishList[0]] = response.data[j].menuId;
					}
					results.push(tempObject);
				} else {
					results.push(response.data);
				}
			}
			setMenuData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	const fetchTopReviewData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 6; i++) {
				const response = await getWithToken(`/review/restaurant${i + 1}`);
				results.push(response.data);
			}
			setTopReviewData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	};

	useEffect(() => {
		const fetchData = async () => {
			try {
				await Promise.all([
					fetchRestaurantData(),
					fetchMenuData(),
					fetchTopReviewData()
				])
			} catch (error) {
				console.error("Error fetching JSON:", error);
			} finally {
				setLoading(false);
			}
		}

		fetchData();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	return (
		<div className="App">
			{ // 데이터 로드 중
				loading ? <Loading /> : 
				restaurantId !== 0 && 
				<>
					<SwipeableTab 
						restaurantId={restaurantId}
						setRestaurantId={handleSetRestaurantId}
						menuData={menuData}
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
				{menuData.length > 0 && (
					<ReviewWriteForm
						restaurantNum={restaurantId}
						deptNum={nowDept}
						menuInfoForRestaurant1={restaurantId === 1 ? menuData[0] : null}
						onReviewSubmitted={fetchTopReviewData}
						setIsLoginModalOpen={setIsLoginModalOpen}
					/>
				)}
					<TopReview
						idx={restaurantId}
						wholeReviewList={topReviewData}
						setWholeReviewList={setTopReviewData}
						onDeleteSuccess={fetchTopReviewData}
						setIsLoginModalOpen={setIsLoginModalOpen}
					/>
				</>
			}
			<LoginModal
				visible={isLoginModalOpen}
				onClose={() => setIsLoginModalOpen(false)}
			/>
		</div>
	);
};

export default MainPage;
