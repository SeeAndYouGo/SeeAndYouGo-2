import React from "react";
import styled from "@emotion/styled";
import * as config from "../../config";

const ReportButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

// 아이콘 눌러 신고하기 버튼 기능
const ReviewReport = ({ reportTarget }) => {
	const handleSubmit = () => {
		const url = config.DEPLOYMENT_BASE_URL + `/report/${reportTarget}`;

		fetch(url, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
			},
		})
			.then(() => {
				alert("신고가 접수되었습니다! \n감사합니다.");
			})
			.catch((err) => console.log(err));
	};

	return (
		<>
			<ReportButton
				onClick={() => {
					if (window.confirm("이 리뷰를 신고하시겠습니까?")) {
						handleSubmit();
					} else {
						return;
					}
				}}
			>
				신고하기
			</ReportButton>
		</>
	);
};

export default ReviewReport;
