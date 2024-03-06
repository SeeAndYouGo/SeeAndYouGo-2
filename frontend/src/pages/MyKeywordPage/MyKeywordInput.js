import { useState } from "react";
import styled from "@emotion/styled";
import { useDispatch, useSelector } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
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
			dispatch(showToast({ contents: "keyword", toastIndex: 2 }));
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
			const result = await res.json();
			if (result.isExceed) { // 키워드 등록 최대 개수 초과
				dispatch(showToast({ contents: "keyword", toastIndex: 3 }));
			} else { // 키워드 등록 성공
				setKeywordList(result.keywords);
				setKeyword("");
				dispatch(showToast({ contents: "keyword", toastIndex: 0 }));
			}
		} else { // 키워드 조건 확인 필요
			dispatch(showToast({ contents: "keyword", toastIndex: 4 }));
		}
	};

	const handleKeywordChange = (e) => {
		setKeyword(e.target.value);
	};

	const handleKeyPress = (e) => {
		if (e.isComposing || e.keyCode === 229) return;
		if (e.key === "Enter") {
			handleSubmit();
		}
	};

	return (
		<div style={{ margin: "20px 0", position: "relative" }}>
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
