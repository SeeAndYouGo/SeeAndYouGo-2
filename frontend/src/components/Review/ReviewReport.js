import React from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { putWithToken, deleteWithToken } from "../../api";

const ReportButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

const ReviewReport = ({ reportTarget }) => {
	const dispatch = useDispatch();

	const handleSubmit = async () => {
		try {
      const { reportCount } = await putWithToken(`/report/${reportTarget}`);

      if (reportCount >= 10) {
        const { success } = await deleteWithToken(`/review/report/${reportTarget}`);

        if (!success) throw new Error("Failed to delete review after 10 reports");

        dispatch(showToast({ contents: "review", toastIndex: 10 }));

        setTimeout(() => {
          window.location.reload();
        }, 1000);

        return;
      }

      if (reportCount > 0) {
        dispatch(showToast({ contents: "review", toastIndex: 5 }));
        return;
      }
			
    } catch (error) {
      dispatch(showToast({ contents: "review", toastIndex: 6 }));
      console.error(error);
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
