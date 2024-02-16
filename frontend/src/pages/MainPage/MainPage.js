import React, { useEffect, useState } from "react";
import "../../App.css";
import { Link } from "react-router-dom";
import UpdateLog from "./UpdateLog";
import Cafeteria from "./Cafeteria";
import * as config from "../../config";

const MainPage = () => {
	const [restaurantData, setRestaurantData] = useState([]);

	const createUrl = (restaurantIdx) => config.BASE_URL + "/connection/restaurant" + restaurantIdx + (config.NOW_STATUS === 0 ? ".json" : "");

	useEffect(() => {
		const url = [createUrl(1), createUrl(2), createUrl(3), createUrl(4), createUrl(5)];

		Promise.all(
			url.map((path) => fetch(path).then((response) => response.json()))
		)
			.then((dataArray) => setRestaurantData(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));
	}, []);

	return (
		<div className="App">
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
