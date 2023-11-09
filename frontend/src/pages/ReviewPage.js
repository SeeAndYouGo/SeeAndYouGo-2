import React from "react";
import ReviewHeader from "../components/ReviewPage/ReviewHeader";
import ReviewSelect from "../components/ReviewPage/ReviewSelect";
import Navigation from "../components/Navigation";

const ReviewPage = () => {
	return (
		<>
			<div className="App3">
				<ReviewHeader />
				<ReviewSelect />
				<Navigation />
			</div>
		</>
		
	);
};

export default ReviewPage;
