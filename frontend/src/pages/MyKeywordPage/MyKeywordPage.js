import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { useSelector } from "react-redux";
import MyKeywordInput from "./MyKeywordInput";
import MyKeywordItem from "./MyKeywordItem";
import Toast from "../../components/Toast";
import * as config from "../../config";

const UnderLine = styled.hr`
	border: 0;
	height: 2px;
	margin: 0 10px;
	background-color: #ccc;
`;

const MyKeywordList = styled.div`
	background-color: white;
	margin: 0 10px;
	padding-bottom: 5px;
	border-radius: 10px;
`;

const toastList = [
	["키워드가 등록되었습니다.", "success"],
	["키워드 등록에 실패했습니다.", "error"],
	["이미 등록된 키워드입니다.", "alert"],
	["키워드 조건을 확인해주세요!", "alert"],
	["키워드 삭제에 성공했습니다.", "success"],
	["키워드 삭제에 실패했습니다.", "error"],
];

const MyKeywordPage = () => {
	const [keywordList, setKeywordList] = useState([]);
	const toastIndex = useSelector((state) => state.toast).value;
  const user = useSelector((state) => state.user.value);
	const token = user.token;

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				"/keyword" +
				(config.NOW_STATUS === 1
					? `/${token}`
					: ".json");

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			setKeywordList(result.keywords);
			console.log(result);
		};
		fetchData();
	}, [token]);

	return (
		<>
			{toastIndex !== null && (
				<Toast
					message={toastList[toastIndex][0]}
					type={toastList[toastIndex][1]}
				/>
			)}

			<div style={{ fontSize: 14, margin: "70px 10px 0 10px" }}>
				<div style={{ textAlign: "center" }}>
					<p style={{ fontSize: 20, margin: 10 }}>나의 키워드</p>
					<p style={{ margin: 0, fontWeight: 600 }}>나의 키워드를 등록해</p>
					<p style={{ margin: 0, fontWeight: 600 }}>
						키워드가 들어있는 메뉴를 쉽게 확인하세요!
					</p>
				</div>
				<MyKeywordInput
					setKeywordList={setKeywordList}
					existedKeywordList={keywordList}
				/>
				<p style={{ margin: "10px 20px" }}>
					등록된 키워드 ({keywordList ? keywordList.length : 0}/10)
				</p>
				{keywordList.length === 0 ? (
					<p
						style={{
							margin: "10px 20px",
							textAlign: "center",
							fontWeight: 600,
						}}
					>
						등록된 키워드가 없습니다.
					</p>
				) : (
					<MyKeywordList>
						{keywordList.map((val, index) => (
							<div key={index}>
								<MyKeywordItem keyword={val} setKeywordList={setKeywordList} />
								<UnderLine />
							</div>
						))}
					</MyKeywordList>
				)}
			</div>
			<div className="blankSpace" style={{ marginBottom: 20 }}>
				&nbsp;
			</div>
		</>
	);
};

export default MyKeywordPage;
