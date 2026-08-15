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
	const handleSubmit = async () => {
		try {
			const { success } = await deleteWithToken(`/review/${deleteTarget}`);

			if (!success) {
				dispatch(showToast({ contents: "review", toastIndex: 2 }));
				return;
			}

			dispatch(showToast({ contents: "review", toastIndex: 3 }));
			
			if (onDeleteSuccess) {
				onDeleteSuccess();
				return;
			}

			setTimeout(() => {
				window.location.reload();
			}, 1000);

		} catch (error) {
			dispatch(showToast({ contents: "review", toastIndex: 4 }));
			console.error(error);
		}
	};

	const handleClick = () => {
		if (window.confirm("본인이 작성한 리뷰만 삭제가 가능합니다.\n삭제하시겠습니까?")) {
			handleSubmit();
		}
	};

	return <RemoveButton onClick={handleClick}>삭제하기</RemoveButton>;
};

export default ReviewDelete;
