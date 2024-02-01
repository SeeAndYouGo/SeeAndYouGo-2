import { useState } from "react";
import styled from "@emotion/styled";
import * as config from "../../config";

const KeywordInput = styled.input`
	width: 100%;
	height: 40px;
	font-size: 14px;
	padding: 10px 20px;
	border-radius: 10px;
	border: none;
`;

const MyKeywordInput = ({
	setKeywordList,
	setToastSuccess,
	setToastFail,
	setToastExisted,
	setToastImpossible,
	existedKeywordList
}) => {
	const [keyword, setKeyword] = useState("");

	const handleSubmit = async () => {
		if (existedKeywordList.includes(keyword)) {
			setToastExisted(true);
		} else if (keyword.length > 0 && keyword.length < 7) {
			const url = config.BASE_URL + "/keyword";

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "POST",
				body: JSON.stringify({
					keyword: keyword,
					user_id: localStorage.getItem("token"),
				}),
			});
			if (res.ok) {
				const result = await res.json();
				setKeywordList(result.keywords);
				setKeyword("");
				setToastSuccess(true);
			} else {
				setToastFail(true);
			}
		} else {
			setToastImpossible(true);
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
