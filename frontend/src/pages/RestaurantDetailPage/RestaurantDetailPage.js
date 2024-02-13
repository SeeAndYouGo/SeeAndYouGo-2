import React from "react";
import { useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import "../../App.css";
import DetailHeader from "./DetailHeader";
import MenuInfo from "./MenuInfo";
import ReviewWrite from "./ReviewWrite";
import TopReview from "./TopReview";

const RestaurantDetailPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	const nowDept = useSelector((state) => state.dept).value;

	return (
		<>
			<div className="App2">
				<DetailHeader idx={restaurant} />
				<MenuInfo idx={restaurant} />
				<ReviewWrite restaurantNum={restaurant} deptNum={nowDept} />
				<TopReview
					idx={restaurant}
					nowDept={nowDept === 1 ? "STUDENT" : "STAFF"}
				/>
			</div>
			<div className="blankSpace" style={{ marginBottom: 20 }}>&nbsp;</div>
		</>
	);
};

export default RestaurantDetailPage;
