import React, { useEffect, useState } from "react";
import "../../App.css";
import TabBar from "./TabBar";
import * as config from "../../config";
import Info from "./Info";
import Progress from "./Progress";
import ReviewPreview from "./ReviewPreview";
import TodayMenu from "./TodayMenu";
import ReviewWriteForm from "./ReviewForm";
import { useSelector } from "react-redux";
import axios from "axios";
import { useDispatch } from "react-redux";
import { setSelectedRestaurant } from "../../redux/slice/UserSlice";
import MenuInfoForRestaurant1 from "../RestaurantDetailPage/MenuInfoForRestaurant1";

const MainPage = () => {
	const [restaurantData, setRestaurantData] = useState([]);
	const [menuData, setMenuData] = useState([]);
	const [topReviewData, setTopReviewData] = useState([]);
	const restaurantId = useSelector((state) => state.user).value.selectedRestaurant;
  const nowDept = useSelector((state) => state.dept).value;
	const ratio = restaurantData[restaurantId-1]?.connected / restaurantData[restaurantId-1]?.capacity * 100;
	const dispatch = useDispatch();

	const handleSetRestaurantId = (id) => {
		dispatch(setSelectedRestaurant(id));
	}

	const fetchRestaurantData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 5; i++) {
				const response = await axios.get(`${config.BASE_URL}/connection/restaurant${i + 1}`);
				results.push(response.data);
			}
			setRestaurantData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	}

	const fetchMenuData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 5; i++) {
				const response = await axios.get(`${config.BASE_URL}/daily-menu/restaurant${i + 1}`);
				results.push(response.data);
			}
			setMenuData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	}

	const fetchTopReviewData = async () => {
		const results = [];
		try {
			for (let i = 0; i < 5; i++) {
				const response = await axios.get(`${config.BASE_URL}/top-review/restaurant${i + 1}`);
				results.push(response.data);
			}
			setTopReviewData(results);
		} catch (error) {
			console.error("Error fetching JSON:", error);
		}
	}

	useEffect(() => {
		fetchRestaurantData();
		fetchMenuData();
		fetchTopReviewData();
	}, []);

	return (
		<div className="App">
			<TabBar restaurantId={restaurantId} setRestaurantId={handleSetRestaurantId} />
      <Info idx={restaurantId} />
      <Progress ratio={ratio} time={restaurantData[restaurantId-1]?.dateTime} />
			{restaurantId === 1 ?  
				<MenuInfoForRestaurant1 />
				: <TodayMenu data={menuData[restaurantId-1]} />
			}
			<ReviewWriteForm restaurantNum={restaurantId} deptNum={nowDept}/>
			<ReviewPreview data={topReviewData[restaurantId-1]} idx={restaurantId} />
		</div>
	);
}

export default MainPage;
