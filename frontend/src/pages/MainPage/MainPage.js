import React, { useEffect, useState } from "react";
import "../../App.css";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import UpdateLog from "./UpdateLog";
import Cafeteria from "./Cafeteria";
import Toast from "../../components/Toast";
import * as config from "../../config";

const toastList = [
	["로그인에 성공했습니다.", "success"],
	["로그아웃 되었습니다.", "alert"],
  ["로그인이 필요한 서비스입니다.", "alert"],
];

// 시간 정보가 포함된 식단 인원 정보 request
const MainPage = () => {
  const toastIndex = useSelector((state) => state.toast).value;

	const [restaurantData, setRestaurantData] = useState([]);

	useEffect(() => {
		const url = [
			config.BASE_URL +
				"/connection/restaurant1" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/connection/restaurant2" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/connection/restaurant3" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/connection/restaurant4" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/connection/restaurant5" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
		];

		Promise.all(
			url.map((path) => fetch(path).then((response) => response.json()))
		)
			.then((dataArray) => setRestaurantData(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));
	}, []);

	return (
		<div className="App">
			{ toastIndex !== null && (
				<Toast
					message={toastList[toastIndex][0]}
					type={toastList[toastIndex][1]}
				/>
			)}
			{restaurantData.map((val, idx) =>
				idx === 0 ? (
					<UpdateLog key={idx} updateTime={val.dateTime} />
				) : null
			)}
			{restaurantData.map((val, idx) => {
				return (
					<Link to={`/view/${idx + 1}`} key={idx + 1}>
						<Cafeteria
							idx={idx + 1}
							key={idx}
							value={(val.connected / val.capacity) * 100}
						/>
					</Link>
				);
			})}
		</div>
	);
}

export default MainPage;
