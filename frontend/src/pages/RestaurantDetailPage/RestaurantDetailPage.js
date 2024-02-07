import React from "react";
import { useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import "../../App.css";
import DetailHeader from "./DetailHeader";
import TypeSelect from "./TypeSelect";
import Cafeteria1Info from "./Cafeteria1Info";
import TopReview from "./TopReview";
import ReviewWrite from "./ReviewWrite";
import Toast from "../../components/Toast";

const toastList = [
	["해당 리뷰 신고가 접수되었습니다!", "success"],
	["리뷰 신고에 실패했습니다.", "error"],
	["리뷰 삭제에 성공했습니다.", "success"],
	["리뷰 삭제에 실패했습니다.", "error"],
	["리뷰 삭제에 권한이 없습니다.", "error"],
	["리뷰가 등록되었습니다.", "success"],
	["리뷰 작성에 실패했습니다.", "error"],
];

const RestaurantDetailPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	const toastIndex = useSelector((state) => state.toast).value;

	return (
		<>
			{toastIndex !== null && (
				<Toast
					message={toastList[toastIndex][0]}
					type={toastList[toastIndex][1]}
				/>
			)}
			<div className="App2">
				<DetailHeader idx={restaurant} />
				{restaurant === 1 ? (
					<>
						<Cafeteria1Info />
						<ReviewWrite restaurantName={1} />
					</>
				) : (
					<TypeSelect idx={restaurant} />
				)}
				<TopReview idx={restaurant} />
			</div>
		</>
	);
};

export default RestaurantDetailPage;
