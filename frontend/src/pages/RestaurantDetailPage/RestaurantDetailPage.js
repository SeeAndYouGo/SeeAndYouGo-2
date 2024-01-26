import React from "react";
import { useParams } from "react-router-dom";
import "../../App.css";
import DetailHeader from "./DetailHeader";
import TypeSelect from "./TypeSelect";
import Cafeteria1Info from "./Cafeteria1Info";
import TopReview from "./TopReview";
import ReviewWrite from "./ReviewWrite";

const RestaurantDetailPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	
	return (
		<>
			<div className="App2">
				<DetailHeader idx={restaurant} />
				{restaurant === 1 ? 
				<>
					<Cafeteria1Info />
					<ReviewWrite
						restaurantName={1}
					/>
				</>
				
				: <TypeSelect  idx={restaurant} />}
				<TopReview idx={restaurant}/>
			</div>
		</>
	);
}

export default RestaurantDetailPage;
