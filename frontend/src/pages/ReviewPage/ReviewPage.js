import React from "react";
import { useParams } from "react-router-dom";
import ReviewHeader from "./ReviewHeader";
import ReviewSelect from "./ReviewSelect";

const ReviewPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	return (
		<>
			<div className="App3">
				<ReviewHeader />
				<ReviewSelect idx={restaurant} />
			</div>
		</>
		
	);
};

export default ReviewPage;
