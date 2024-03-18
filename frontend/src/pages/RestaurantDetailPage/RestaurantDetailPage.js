import React from "react";
import styled from "@emotion/styled";
import { useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import "../../App.css";
import DetailHeader from "./DetailHeader";
import MenuInfo from "./MenuInfo";
import ReviewWrite from "./ReviewWrite";
import TopReview from "./TopReview";

const DetailPageWrapper = styled.div`
	@media (min-width: 616px) {
		padding: 20px;
	}
`;

const DetailContent = styled.div`
	@media (min-width: 576px) {
		display: flex;
	};
`;

const Content1 = styled.div`
	@media (min-width: 576px) {
		width: 50%;
		min-width: 320px;
	}
`;

const Content2 = styled.div`
	@media (min-width: 576px) {
		width: 40%;
		margin-left: auto;
		margin-top: 55px;
	}
`;

const RestaurantDetailPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	const nowDept = useSelector((state) => state.dept).value;

	return (
		<>
			<DetailPageWrapper className="App2">
				<DetailHeader idx={restaurant} />
				<DetailContent>
					<Content1>
						<MenuInfo idx={restaurant} />
						<ReviewWrite restaurantNum={restaurant} deptNum={nowDept} />
					</Content1>
					<Content2>
						<TopReview
							idx={restaurant}
							nowDept={nowDept === 1 ? "STUDENT" : "STAFF"}
						/>
					</Content2>
				</DetailContent>
			</DetailPageWrapper>
			<div className="blankSpace" style={{ marginBottom: 20 }}>&nbsp;</div>
		</>
	);
};

export default RestaurantDetailPage;
