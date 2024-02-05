import { useState } from "react";
import styled from "@emotion/styled";
import { useDispatch, useSelector } from "react-redux";
import { changeToastIndex } from "../../redux/slice/ToastSlice";
import * as config from "../../config";

const KeywordInput = styled.input`
	width: 100%;
	height: 40px;
	font-size: 14px;
	padding: 10px 20px;
	border-radius: 10px;
	border: none;
`;

const MyKeywordInput = ({ setKeywordList, existedKeywordList }) => {
	const [keyword, setKeyword] = useState("");
	const user = useSelector((state) => state.user.value);
	const token = user.token;
	const dispatch = useDispatch();

	const handleSubmit = async () => {
		if (existedKeywordList.includes(keyword)) { // 이미 등록된 키워드
			dispatch(changeToastIndex(2));
		} else if (keyword.length > 0 && keyword.length < 7) {
			const url = config.BASE_URL + "/keyword";

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "POST",
				body: JSON.stringify({
					keyword: keyword,
					user_id: token,
				}),
			});
			if (res.ok) { // 키워드 등록 성공
				const result = await res.json();
				setKeywordList(result.keywords);
				setKeyword("");
				dispatch(changeToastIndex(0));
			} else { // 키워드 등록 실패
				dispatch(changeToastIndex(1));
			}
		} else { // 키워드 조건 확인
			dispatch(changeToastIndex(3));
		}
	};

	const handleKeywordChange = (e) => {
		setKeyword(e.target.value);
	};

	const handleKeyPress = (e) => {
		if (e.key === "Enter") {
			handleSubmit();
		}
	};

	return (
		<div style={{ margin: "20px 10px", position: "relative" }}>
			<KeywordInput
				type="text"
				placeholder="키워드를 입력해주세요! (1~6글자)"
				min={2}
				maxLength={6}
				value={keyword}
				onKeyDown={handleKeyPress}
				onChange={handleKeywordChange}
			/>
			<span
				className="material-symbols-outlined"
				style={{
					position: "absolute",
					top: "20%",
					right: "17px",
					cursor: "pointer",
				}}
				onClick={handleSubmit}
			>
				add_circle
			</span>
		</div>
	);
};

export default MyKeywordInput;