import styled from "@emotion/styled";
import { useDispatch, useSelector } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import * as config from "../../config";

const RemoveBtn = styled.span`
	position: absolute;
	font-size: 22px;
	top: -1px;
	right: 7px;
	cursor: pointer;
`;

const MyKeywordItem = ({ keyword, setKeywordList }) => {
	const user = useSelector((state) => state.user.value);
	const token = user.token;
	const dispatch = useDispatch();

	const handleSubmit = async () => {
		const url = config.BASE_URL + "/keyword";

		const res = await fetch(url, {
			headers: {
				"Content-Type": "application/json",
			},
			method: "DELETE",
			body: JSON.stringify({
				keyword: keyword,
				user_id: token,
			}),
		});
		if (res.ok) {
			// 키워드 삭제 성공
			const result = await res.json();
			setKeywordList(result.keywords);
			dispatch(showToast({ contents: "keyword", toastIndex: 5 }));
		} else {
			// 키워드 삭제 실패
			dispatch(showToast({ contents: "keyword", toastIndex: 6 }));
		}
	};

	return (
		<div style={{ margin: "5px 0", padding: "8px 10px" }}>
			<div
				style={{
					margin: "0 0 0 5px",
					position: "relative",
					fontWeight: 500,
				}}
			>
				#&nbsp;{`${keyword}`}
				<RemoveBtn className="material-symbols-outlined" onClick={handleSubmit}>
					do_not_disturb_on
				</RemoveBtn>
			</div>
		</div>
	);
};

export default MyKeywordItem;
