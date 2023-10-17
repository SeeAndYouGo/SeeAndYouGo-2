import React from "react";
import ReviewHeader from "../components/ReviewHeader";
import ReviewSelect from "../components/ReviewSelect";
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
