import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import MyKeywordInput from "./MyKeywordInput";
import MyKeywordItem from "./MyKeywordItem";
import * as config from "../../config";

const MyKeywordWrapper = styled.div`
	font-size: 14px;
	padding: 30px 15px 50px;
	@media (min-width: 576px) {
		padding: 30px 20px 50px;
	}
`;

const MyKeywordList = styled.div`
	background-color: white;
	margin: 0;
	padding-bottom: 5px;
	border-radius: 10px;
	overflow: hidden;
`;

const NoKeywordMessage = styled.p`
	margin: 10px 20px;
	text-align: center;
	font-weight: 600;
`;

const NotLogin = styled.div`
	position: absolute;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	background-color: rgba(20, 20, 20, 0.3);
	z-index: 6;
	text-align: center;
	font-size: 20px;
	text-decoration: underline;
	padding-top: 100px;
`;

const GoToLogin = styled.span`
	cursor: pointer;
	:hover {
		color: red;
		opacity: 0.7;
	}
`;

const MyKeywordPage = () => {
	const [keywordList, setKeywordList] = useState([]);
	const navigator = useNavigate();
	const user = useSelector((state) => state.user.value);
	const token = user.token;

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				"/keyword" +
				(config.NOW_STATUS === 1 ? `/${token}` : ".json");

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
	}, [token]);

	return (
		<>
			{token ? (
				<MyKeywordWrapper>
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
					<p style={{ margin: "10px" }}>
						등록된 키워드 ({keywordList ? keywordList.length : 0}/10)
					</p>
					{keywordList.length === 0 ? (
						<NoKeywordMessage>등록된 키워드가 없습니다.</NoKeywordMessage>
					) : (
						<MyKeywordList>
							{keywordList.map((val, index) => (
								<MyKeywordItem
									key={index}
									keyword={val}
									setKeywordList={setKeywordList}
								/>
							))}
						</MyKeywordList>
					)}
				</MyKeywordWrapper>
			) : (
				<NotLogin>
					<GoToLogin
						onClick={() => {
							navigator("/login-page");
						}}
					>
						로그인이 필요합니다 !!
					</GoToLogin>
				</NotLogin>
			)}
		</>
	);
};

export default MyKeywordPage;
