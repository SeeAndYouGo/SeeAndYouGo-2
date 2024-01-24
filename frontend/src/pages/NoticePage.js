import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";

// json 사용 예시
// const tempNotice = {
// 	title: "v.1.2.3 업데이트 안내입니다.",
// 	date: "2024-01-10",
// 	version: "v.1.2.3",
// 	comment: "안녕하세요.",
// 	new: [
// 		["new1", "new1 설명"],
// 		["new2", "new2 설명"],
// 		["new3", "new3 설명"],
// 	],
// 	feature: [
// 		[
// 			"리뷰 작성 기능 추가",
// 			"별점, 닉네임, 코멘트를 입력해 리뷰 작성이 가능합니다.",
// 		],
// 		[
// 			"리뷰 확인 기능 추가",
// 			"리뷰 페이지를 통해 전체 또는 학생 식당별 당일에 작성된 리뷰가 확인 가능합니다.",
// 		],
// 		[
// 			"개발자 정보 추가",
// 			"문의사항이나 요청사항이 따로 있으시다면 화면 하단에 있는 메일로 남겨주시면 감사하겠습니다.",
// 		],
// 	],
// 	change: [
// 		["변경1", "before1", "after1"],
// 		["변경2", "before2", "after2"],
// 		["변경3", "before3", "after3"],
// 	],
// 	fixed: [
// 		["오류 수정", "설명"],
// 		["버그 수정", "설명"],
// 	],
// 	deprecated: [
// 		["사용 중단1", "설명1"],
// 		["사용 중단2", "설명2"],
// 		["사용 중단3", "설명3"],
// 	],
// };

const Title = styled.span`
	border-bottom-style: solid;
	border-bottom-width: 2px;
	padding: 0 100px;
`;

const WriteDate = styled.span`
	border-bottom-style: solid;
	border-bottom-width: 2px;
	padding: 0 30px;
	margin-left: 12px;
`;

const NoticeItemContainer = styled.div`
	display: flex;
	flex-direction: row;
	margin: 5px 5px;
	font-size: 14px;
	cursor: pointer;
`;

const ItemTitle = styled.div`
	width: 230px;
	border-bottom-style: solid;
	border-bottom-color: gray;
	border-bottom-width: 1px;
	text-align: center;
	font-weight: 500;
`;

const ItemWriteDate = styled.div`
	width: 106px;
	border-bottom-style: solid;
	border-bottom-color: gray;
	border-bottom-width: 1px;
	text-align: center;
	margin-left: 12px;
	font-weight: 500;
`;

const NewName = styled.span`
	color: #403194;
	background-color: #eae5ff;
	padding: 2px 10px;
	border-radius: 5px;
	margin: 5px;
`;

const FeatureName = styled.span`
	color: #016744;
	background-color: #e3fcee;
	padding: 2px 10px;
	border-radius: 5px;
	margin: 5px;
`;

const ChangeName = styled.span`
	color: #975213;
	background-color: #fffae6;
	padding: 2px 10px;
	border-radius: 5px;
	margin: 5px;
`;

const FixedName = styled.span`
	color: #2866d3;
	background-color: #ddebff;
	padding: 2px 10px;
	border-radius: 5px;
	margin: 5px;
`;

const DeprecatedName = styled.span`
	color: #df3b12;
	background-color: #feeae6;
	padding: 2px 10px;
	border-radius: 5px;
	margin: 5px;
`;

const DescriptionType1 = ({ val1, val2 }) => {
	return (
		<div style={{ padding: 2 }}>
			<p style={{ margin: "2px 5px", fontWeight: 500, fontSize: 15 }}>
				- {val1}
			</p>
			<div style={{ margin: "2px 5px" }}>
				<span style={{ fontWeight: 400 }}>: {val2}</span>
			</div>
		</div>
	);
};

const DescriptionType2 = ({ val1, val2, val3 }) => {
	const FrontSpan = styled.span`
		display: inline-block;
		font-weight: 400;
		text-align: center;
	`;
	return (
		<div style={{ padding: 2 }}>
			<p style={{ margin: "2px 5px", fontWeight: 500, fontSize: 15 }}>
				- {val1}
			</p>
			<div style={{ margin: "3px 5px" }}>
				<FrontSpan>{"[ 기존 ]"}&nbsp;</FrontSpan>
				<span style={{ fontWeight: 400 }}>{val2}</span>
			</div>
			<div style={{ margin: "3px 5px" }}>
				<FrontSpan>{"[ 현재 ]"}&nbsp;</FrontSpan>
				<span style={{ fontWeight: 400 }}>{val3}</span>
			</div>
		</div>
	);
};

const NoticePage = () => {
	const [noticeList, setNoticeList] = useState([]);
	const [openStates, setOpenStates] = useState(Array(2).fill(false));

	const handleOuterItemClick = (index) => {
		setOpenStates((prev) => {
			const newStates = [...prev];
			newStates[index] = !newStates[index];
			return newStates;
		});
	};

	useEffect(() => {
		const fetchData = async () => {
			const url = "/assets/json/updateNote.json";
			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData()
			.then((data) => {
				// 역순으로 정렬해 setNoticeList 값 넣어주기
				setNoticeList(
					[...data].sort(
						(a, b) => new Date(b.date) - new Date(a.date)
					)
				);
			})
			.catch((error) => {
				console.log(error);
			});
	}, []);

	return (
		<>
			<div>
				<div style={{ textAlign: "center", margin: "70px 5px 0 5px" }}>
					<p style={{ fontSize: 20 }}>공지사항</p>
				</div>
				<div
					style={{
						textAlign: "center",
						borderTop: "8px solid black",
						paddingTop: 5,
						margin: "0 5px",
					}}
				>
					<Title>제목</Title>
					<WriteDate>작성일</WriteDate>
				</div>

				{noticeList.map((tempNotice, index) => {
					return (
						<div key={tempNotice.title}>
							{/* 외부 표지 */}
							<NoticeItemContainer
								onClick={() => {
									handleOuterItemClick(index);
								}}
							>
								<ItemTitle>{tempNotice.title}</ItemTitle>
								<ItemWriteDate>{tempNotice.date}</ItemWriteDate>
							</NoticeItemContainer>

							{/* 내부 표지 */}
							{openStates[index] && (
								<div style={{ fontSize: 14 }}>
									<div
										style={{
											fontWeight: 400,
											fontSize: 15,
											margin: "20px 5px",
										}}
									>
										{tempNotice.comment.map(
											(myValue, idx) => {
												return (
													<p
														key={idx}
														style={{
															fontWeight: 400,
															margin: "0 0 2px 0",
														}}
													>
														{myValue}
													</p>
												);
											}
										)}
										{tempNotice.version} 업데이트가{" "}
										{tempNotice.date} 진행되었습니다.
									</div>

									{/* NEW */}
									{tempNotice["new"].length === 0 ? null : (
										<div style={{ marginTop: 15 }}>
											<NewName>NEW</NewName>
											{tempNotice.new.map(
												(newItem, idx) => {
													return (
														<div key={idx}>
															<DescriptionType1
																val1={
																	newItem[0]
																}
																val2={
																	newItem[1]
																}
															/>
															{newItem[2] && (
																<img
																	src={`${newItem[2]}`}
																	alt="loading.."
																	style={{
																		marginLeft: 10,
																	}}
																/>
															)}
														</div>
													);
												}
											)}
										</div>
									)}

									{/* 추가 */}
									{tempNotice.feature.length === 0 ? null : (
										<div style={{ marginTop: 15 }}>
											<FeatureName>추가</FeatureName>
											{tempNotice.feature.map(
												(newItem, idx) => {
													return (
														<DescriptionType1
															val1={newItem[0]}
															val2={newItem[1]}
															key={idx}
														/>
													);
												}
											)}
										</div>
									)}

									{/* 변경 */}
									{tempNotice.change.length === 0 ? null : (
										<div style={{ marginTop: 15 }}>
											<ChangeName>변경</ChangeName>
											{tempNotice.change.map(
												(newItem, idx) => {
													return (
														<DescriptionType2
															val1={newItem[0]}
															val2={newItem[1]}
															val3={newItem[2]}
															key={idx}
														/>
													);
												}
											)}
										</div>
									)}

									{/* 해결 */}
									{tempNotice.fixed.length === 0 ? null : (
										<div style={{ marginTop: 15 }}>
											<FixedName>해결</FixedName>
											{tempNotice.fixed.map(
												(newItem, idx) => {
													return (
														<DescriptionType1
															val1={newItem[0]}
															val2={newItem[1]}
															key={idx}
														/>
													);
												}
											)}
										</div>
									)}

									{/* 삭제 */}
									{tempNotice.deprecated.length ===
									0 ? null : (
										<div style={{ marginTop: 15 }}>
											<DeprecatedName>
												삭제
											</DeprecatedName>
											{tempNotice.deprecated.map(
												(newItem, idx) => {
													return (
														<DescriptionType1
															val1={newItem[0]}
															val2={newItem[1]}
															key={idx}
														/>
													);
												}
											)}
										</div>
									)}
									<div
										style={{
											borderBottom: "2px solid",
											paddingBottom: 20,
											width: "100%",
										}}
									></div>
								</div>
							)}
						</div>
					);
				})}
				<div className="blankSpace">&nbsp;</div>
				<div className="blankSpace">&nbsp;</div>
			</div>
		</>
	);
};

export default NoticePage;
