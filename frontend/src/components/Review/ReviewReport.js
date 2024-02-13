import React from "react";
import styled from "@emotion/styled";
import * as config from "../../config";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";

const ReportButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

const ReviewReport = ({ reportTarget }) => {
	const dispatch = useDispatch();

	const handleSubmit = () => {
		const url = config.DEPLOYMENT_BASE_URL + `/report/${reportTarget}`;

		fetch(url, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
			},
		})
			.then(() => {
				dispatch(showToast({ contents: "review", toastIndex: 5 }));
			})
			.catch(() => {
				dispatch(showToast({ contents: "review", toastIndex: 6 }));
			});
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
