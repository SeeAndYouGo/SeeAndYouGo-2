import React, { useEffect, useState } from "react";
import Header from "../components/MainPage/Header";
import UpdateLog from "../components/MainPage/UpdateLog";
import Cafeteria from "../components/MainPage/Cafeteria";
import "../App.css";
import { Link } from "react-router-dom";
import Navigation from "../components/Navigation";

// 시간 정보가 포함된 식단 인원 정보 request
function App() {
	const [restaurantData, setRestaurantData] = useState([]);

	useEffect(() => {
		// const jsonFilePaths = [
		// 	"/api/connection/restaurant1",
		// 	"/api/connection/restaurant2",
		// 	"/api/connection/restaurant3",
		// 	"/api/connection/restaurant4",
		// 	"/api/connection/restaurant5",
		// ];

		const jsonFilePaths = [
			"http://27.96.131.182/api/connection/restaurant1",
			"http://27.96.131.182/api/connection/restaurant2",
			"http://27.96.131.182/api/connection/restaurant3",
			"http://27.96.131.182/api/connection/restaurant4",
			"http://27.96.131.182/api/connection/restaurant5",
		];

		// const jsonFilePaths = [
		// 	"http://27.96.131.182/api/connection/restaurant1",
		// 	"http://27.96.131.182/api/connection/restaurant2",
		// 	"http://27.96.131.182/api/connection/restaurant3",
		// 	"http://27.96.131.182/api/connection/restaurant4",
		// 	"http://27.96.131.182/api/connection/restaurant5",
		// ];
    
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
