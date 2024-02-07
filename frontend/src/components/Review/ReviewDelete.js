import React from "react";
import styled from "@emotion/styled";
import * as config from "../../config";
import { useSelector, useDispatch } from "react-redux";
import { changeToastIndex } from "../../redux/slice/ToastSlice";

const RemoveButton = styled.span`
	width: 25px;
	height: 25px;
	cursor: pointer;
	color: #777;
	font-size: 15px;
`;

// 삭제하기 기능
const ReviewDelete = ({ deleteTarget }) => {
	const dispatch = useDispatch();
	const nowToken = useSelector((state) => state.user.value.token);

	// deleteTarget: 삭제할 리뷰의 id
	const handleSubmit = () => {
		const url =
			config.DEPLOYMENT_BASE_URL + `/reviews/${deleteTarget}/${nowToken}`;

		fetch(url, {
			method: "DELETE",
			headers: {
				"Content-Type": "application/json",
			},
		})
			.then((res) => res.json())
			.then((res) => {
				// 토스트 완성되면 변경 예정
				if (res.success === true) {
					dispatch(changeToastIndex(2));
					window.location.reload();
				} else {
					dispatch(changeToastIndex(4));
				}
			})
			.catch(() => {
				dispatch(changeToastIndex(3));
			});
	};

	return (
		<>
			<RemoveButton
				onClick={() => {
					if (
						window.confirm(
							"본인이 작성한 리뷰만 삭제가 가능합니다.\n삭제하시겠습니까?"
						)
					) {
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
