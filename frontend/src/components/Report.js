import React from "react";
import * as config from "../config";

// 아이콘 눌러 신고하기 버튼 기능
const Report = ({ reportTarget }) => {
	const handleSubmit = () => {
		const url = config.DEPLOYMENT_BASE_URL + `/report/${reportTarget}`;

		fetch(url, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
			},
		})
			.then(() => {
				alert(`신고가 접수되었습니다! \n감사합니다.`);
			})
			.catch((err) => console.log(err));
	};

	return (
		<>
			<img
				src="/assets/images/Report.png"
				style={{
					width: 25,
					height: 25,
					cursor: "pointer",
					color: "#777",
					fontSize: 20,
				}}
				alt="신고하기"
				onClick={() => {
					if (window.confirm("이 리뷰를 신고하시겠습니까?")) {
						handleSubmit();
					}
				}}
			/>
		</>
	);
};

export default Report;
