import React from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { deleteWithToken } from "../../api";

const RemoveButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

const ReviewDelete = ({ deleteTarget, targetRestaurant, wholeReviewList, setWholeReviewList, onDeleteSuccess }) => {
	const dispatch = useDispatch();

	// deleteTarget: 삭제할 리뷰의 id
	const handleSubmit = () => {
		deleteWithToken(`/reviews/${deleteTarget}`)
			.then((res) => {
				if (res.data.success === true) { // 리뷰 삭제 성공
					dispatch(showToast({ contents: "review", toastIndex: 3 }));
					if (onDeleteSuccess) {
						onDeleteSuccess();
					} else {
						setTimeout(() => {
							window.location.reload();
						}, 1000);
					}
				} else { // 리뷰 삭제 권한이 없음
					dispatch(showToast({ contents: "review", toastIndex: 2 }));
				}
			})
			.catch(() => { // 리뷰 삭제 실패
				dispatch(showToast({ contents: "review", toastIndex: 4 }));
			});
	};

	return (
		<>
			<RemoveButton
				onClick={() => {
					if (window.confirm("본인이 작성한 리뷰만 삭제가 가능합니다.\n삭제하시겠습니까?")) {
						handleSubmit();
					} else {
						return;
					}
				}}
			>
				삭제하기
			</RemoveButton>
		</>
	);
};

export default ReviewDelete;
