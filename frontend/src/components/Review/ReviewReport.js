import React from "react";
import styled from "@emotion/styled";
import { postWithToken } from "../../api";

const ReportButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

const ReviewReport = ({ reportTarget }) => {
	// 신고 누적 및 삭제 여부는 서버에서 판단한다. 결과 토스트는 서버 message로 표시된다.
	const handleSubmit = async () => {
		try {
			await postWithToken(`/reviews/${reportTarget}/reports`);
		} catch (error) {
			console.log(error);
		}
	};

	return (
		<ReportButton
			onClick={() => {
				if (window.confirm("이 리뷰를 신고하시겠습니까?")) {
					handleSubmit();
				}
			}}
		>
			신고하기
		</ReportButton>
	);
};

export default ReviewReport;
