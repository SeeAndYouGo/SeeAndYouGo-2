import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import UpdateLog from "../components/UpdateLog";
import Cafeteria from "../components/Cafeteria";
import "../App.css";
import { Link } from "react-router-dom";
import Navigation from "../components/Navigation";

// 시간 정보가 포함된 식단 인원 정보 request
function App() {
	const [restaurantData, setRestaurantData] = useState([]);

	useEffect(() => {
		// JSON 파일
		const jsonFilePaths = [
			"assets/json/restaurant1.json",
			"assets/json/restaurant2.json",
			"assets/json/restaurant3.json",
			"assets/json/restaurant3.json",
			"assets/json/restaurant3.json",
		];
		// API URL 주소
		// const jsonFilePaths = [
		// 	"http://localhost:8080/get_congestion/restaurant1",
		// 	"http://localhost:8080/get_congestion/restaurant2",
		// 	"http://localhost:8080/get_congestion/restaurant3",
		// ];

		// 여러 JSON 파일 가져오기
		Promise.all(
			jsonFilePaths.map((path) =>
				fetch(path).then((response) => response.json())
			)
		)
			.then((dataArray) => setRestaurantData(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));
	}, []);

	return (
		<div className="App">
			<a
				href="https://docs.google.com/forms/d/e/1FAIpQLSfGeuHsAH4fXrazXBLzSn1J7z3ux8in1OQInDH2LYHQnRiU5Q/viewform"
				target="_blank"
				rel="noreferrer noopener"
			>
				<img
					style={{ width: 30, height: 25, float: "right" }}
					src="/assets/images/survey.png"
					alt="Survey"
				/>
			</a>
			<Header />
			{restaurantData.map((val, idx) =>
				idx === 0 ? (
					<UpdateLog key={idx} updateTime={val.dateTime} />
				) : null
			)}
			{restaurantData.map((val, idx) => {
				return (
					<Link to={`/View/${idx + 1}`} key={idx + 1}>
						<Cafeteria
							idx={idx + 1}
							key={idx}
							value={(val.connected / val.capacity) * 100}
						/>
					</Link>
				);
			})}
			<Navigation />
		</div>
	);
}

export default App;
