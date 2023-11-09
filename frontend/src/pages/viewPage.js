import React from "react";
import "../App.css";
import { useParams } from "react-router-dom";
import DetailHeader from "../components/RestaurantDetailPage/DetailHeader";
import TypeSelect from "../components/RestaurantDetailPage/TypeSelect";
import Navigation from "../components/Navigation";
import Cafeteria1Info from "../components/RestaurantDetailPage/Cafeteria1Info";
import TopReview from "../components/RestaurantDetailPage/TopReview";

function View() {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	
	return (
		<>
			<div className="App2">
				<DetailHeader idx={restaurant} />
				{restaurant === 1 ? <Cafeteria1Info /> : <TypeSelect  idx={restaurant} />}
				{/* <Review idx={restaurant} /> */}
				<TopReview idx={restaurant}/>
				<Navigation />
			</div>
		</>
	);
}

export default View;
