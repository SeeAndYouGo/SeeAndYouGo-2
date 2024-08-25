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

const MainPage = () => {
	const [restaurantData, setRestaurantData] = useState([]);
	const [restaurantId, setRestaurantId] = useState(1);
  const nowDept = useSelector((state) => state.dept).value;
	const ratio = restaurantData[restaurantId-1]?.connected / restaurantData[restaurantId-1]?.capacity * 100;

	useEffect(() => {
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
		fetchRestaurantData();
		// const url = [createUrl(1), createUrl(2), createUrl(3), createUrl(4), createUrl(5)];

		// Promise.all(
		// 	url.map((path) => fetch(path).then((response) => response.json()))
		// )
		// 	.then((dataArray) => setRestaurantData(dataArray))
		// 	.catch((error) => console.error("Error fetching JSON:", error));

		// const dailyMenuUrl = config.BASE_URL + `/daily-menu/restaurant${restaurantId}`;

	}, []);

	return (
		<div className="App">
			<TabBar restaurantId={restaurantId} setRestaurantId={setRestaurantId} />
      <Info idx={restaurantId} />
      <Progress ratio={ratio} time={restaurantData[restaurantId-1]?.dateTime} />
			<TodayMenu />
			<ReviewWriteForm restaurantNum={restaurantId} deptNum={nowDept}/>
			<ReviewPreview idx={restaurantId} />
		</div>
	);
}

export default MainPage;
