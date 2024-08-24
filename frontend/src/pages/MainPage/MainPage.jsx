import React, { useEffect, useState } from "react";
import "../../App.css";
// import TabBar from "./TabBar";
import * as config from "../../config";
import Info from "./Info";
import Progress from "./Progress";
import ReviewPreview from "./ReviewPreview";

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
			{/* <TabBar /> */}
      <Info idx={2} />
      <Progress />
			<ReviewPreview />
		</div>
	);
}

export default MainPage;
