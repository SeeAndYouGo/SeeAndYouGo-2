import React from "react";
import { useParams } from "react-router-dom";
import ReviewHeader from "../components/ReviewPage/ReviewHeader";
import ReviewSelect from "../components/ReviewPage/ReviewSelect";
// import Navigation from "../components/Navigation";

const ReviewPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	return (
		<>
			<div className="App3">
				<ReviewHeader />
				<ReviewSelect idx={restaurant} />
				{/* <Navigation /> */}
			</div>
		</>
		
	);
};

export default ReviewPage;
