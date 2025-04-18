import React from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { putWithToken } from "../../api";

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
		const url = `/report/${reportTarget}`;

		putWithToken(url)
			.then((res) => {
				if (res.data.success === true) {
					dispatch(showToast({ contents: "review", toastIndex: 5 }));
				}
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
