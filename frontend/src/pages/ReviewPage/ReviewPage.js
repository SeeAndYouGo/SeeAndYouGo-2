import React from "react";
import { useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import ReviewHeader from "./ReviewHeader";
import ReviewSelect from "./ReviewSelect";
import Toast from "../../components/Toast";

const toastList = [
	["해당 리뷰 신고가 접수되었습니다!", "success"],
	["리뷰 신고에 실패했습니다.", "error"],
	["리뷰 삭제에 성공했습니다.", "success"],
	["리뷰 삭제에 실패했습니다.", "error"],
	["리뷰 삭제에 권한이 없습니다.", "error"],
	["로그인이 필요한 서비스입니다.", "alert"],
	["내가 쓴 리뷰는 공감할 수 없습니다.", "error"],
];

const ReviewPage = () => {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	const toastIndex = useSelector((state) => state.toast).value;

	return (
		<>
			<div className="App3">
				{toastIndex !== null && (
					<Toast
						message={toastList[toastIndex][0]}
						type={toastList[toastIndex][1]}
					/>
				)}
				<ReviewHeader />
				<ReviewSelect idx={restaurant} />
			</div>
		</>
	);
};

export default ReviewPage;
