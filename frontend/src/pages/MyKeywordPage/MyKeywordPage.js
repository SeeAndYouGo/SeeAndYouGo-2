import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
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

const MyKeywordPage = () => {
	const [keywordList, setKeywordList] = useState([]);
	const [toastSuccess, setToastSuccess] = useState(false);
	const [toastFail, setToastFail] = useState(false);
	const [toastExisted, setToastExisted] = useState(false);
	const [toastImpossible, setToastImpossible] = useState(false);
	const [toastDeleteSuccess, setToastDeleteSuccess] = useState(false);
	const [toastDeleteFail, setToastDeleteFail] = useState(false);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				"/keyword" +
				(config.NOW_STATUS === 1
					? `/${localStorage.getItem("token")}`
					: ".json");

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			setKeywordList(result.keywords);
		};
		fetchData();
	}, []);

	return (
		<>
			{toastSuccess && (
				<Toast
					message="키워드가 등록되었습니다."
					type="success"
					setToast={setToastSuccess}
				/>
			)}
			{toastFail && (
				<Toast
					message="키워드 등록에 실패했습니다."
					type="error"
					setToast={setToastFail}
				/>
			)}
			{toastExisted && (
				<Toast
					message="이미 등록된 키워드입니다."
					type="error"
					setToast={setToastExisted}
				/>
			)}
			{toastImpossible && (
				<Toast
					message="키워드 조건을 확인해주세요!"
					type="error"
					setToast={setToastImpossible}
				/>
			)}
			{toastDeleteSuccess && (
				<Toast
					message="키워드 삭제에 성공했습니다."
					type="success"
					setToast={setToastDeleteSuccess}
				/>
			)}
			{toastDeleteFail && (
				<Toast
					message="키워드 삭제에 실패했습니다."
					type="error"
					setToast={setToastDeleteFail}
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
					setToastSuccess={setToastSuccess}
					setToastFail={setToastFail}
					setToastExisted={setToastExisted}
					setToastImpossible={setToastImpossible}
					existedKeywordList={keywordList}
				/>
				<p style={{ margin: "10px 20px" }}>
					등록된 키워드 ({keywordList.length}/10)
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
								<MyKeywordItem
									keyword={val}
									setKeywordList={setKeywordList}
									setToastDeleteSuccess={setToastDeleteSuccess}
									setToastDeleteFail={setToastDeleteFail}
								/>
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
