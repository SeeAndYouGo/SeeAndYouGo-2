import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import ReviewItem from "./ReviewItem";

const MyRadio = styled.input`
	margin-left: 10px;
	accent-color: black;
	-ms-transform: scale(1.5) /* IE 9 */;
	-webkit-transform: scale(1.5) /* Chrome, Safari, Opera */;
	transform: scale(1.5);
`;

const MenuInfo = ({ mainMenu, subMenu }) => {
	const subMenuString = subMenu.join(", ");
	return (
		<>
			<p
				style={{
					fontSize: 12,
					margin: "0px 0px 4px 0px",
					color: "#555",
				}}
			>
				오늘의 메뉴
			</p>
			<p style={{ fontSize: 18, margin: 0 }}>{mainMenu}</p>
			<p
				style={{
					fontSize: 12,
					margin: 0,
					fontWeight: 400,
					color: "#777",
				}}
			>
				{subMenuString}
			</p>
		</>
	);
};

const CheckBoxInput = styled.input`
	display: none;

	&:checked + label {
		background: #111;
	}
	&:checked + label::after {
		color: white;
	}
`;
const CheckBoxLabel = styled.label`
	cursor: pointer;
	display: inline-block;
	width: 15px;
	height: 15px;
	background-color: #d9d9d9;
	position: relative;
	top: 1.5px;
	border-radius: 3px;
	&::after {
		content: "✔";
		color: #fff;
		font-size: 10px;
		width: 15px;
		height: 15px;
		text-align: center;
		position: absolute;
		left: 0;
		top: 0;
	}
`;

const SortingSelect = styled.select`
	width: 105px;
	height: 20px;
	font-size: 12px;
	float: right;
	padding: 0px 0.5em;
	border: 0px;
	border-radius: 5px;
	font-family: inherit;
	background: url("/assets/images/arrow.png") no-repeat 95% 50%;
	background-size: contain;
	background-color: white;
	-webkit-appearance: none;
	-moz-appearance: none;
	appearance: none;
	::-ms-expand {
		display: none;
	}
`;

const NoReviewMessage = styled.p`
	margin-top: 10px;
	font-weight: 400;
	width: 100%;
	font-size: 14px;
	text-align: center;
	color: #777;
	padding: 15px 0;
	background: #fff;
	border-radius: 20px;
`;

const ReviewListType2 = ({
	idx,
	nowReviewList,
	nowMenu,
	wholeReviewList,
	setWholeReviewList,
}) => {
	const [isChecked, setIsChecked] = useState(false);
	const [sortOrder, setSortOrder] = useState("latest");

	const [staffMenu, setStaffMenu] = useState([]);
	const [studentMenu, setStudentMenu] = useState([]);
	const [radioValue, setRadioValue] = useState("STUDENT");

	const initialSetting = () => {
		setIsChecked(false);
		setSortOrder("latest");
		setRadioValue("STUDENT");
	};

	const handleRadioChange = (event) => {
		const nowType = event.target.value;
		setRadioValue(nowType);
	};

	useEffect(() => {
		const staffMenuData = nowMenu.filter((item) => item.dept === "STAFF");
		setStaffMenu(staffMenuData);
		const studentMenuData = nowMenu.filter((item) => item.dept !== "STAFF");
		setStudentMenu(studentMenuData);

		initialSetting();
		const beforeReviewList = [...wholeReviewList];
		beforeReviewList[idx] = nowReviewList.sort(sortFunctions["latest"]);
		setWholeReviewList(beforeReviewList);
	}, [idx, nowMenu]);

	const toggleOnlyImageReviewVisiblity = () => {
		setIsChecked(!isChecked);
	};

	const sortFunctions = {
		latest: (a, b) => new Date(b.madeTime) - new Date(a.madeTime),
		earliest: (a, b) => new Date(a.madeTime) - new Date(b.madeTime),
		highRate: (a, b) => b.rate - a.rate,
		lowRate: (a, b) => a.rate - b.rate,
	};

	// 리뷰 정렬 구현하는 곳
	const handleSortChange = (event) => {
		const selectedSortOrder = event.target.value;
		setSortOrder(selectedSortOrder);

		if (selectedSortOrder && sortFunctions[selectedSortOrder]) {
			const sortedReview = [...nowReviewList].sort(sortFunctions[selectedSortOrder]);
			const afterReviewList = [...wholeReviewList];
			afterReviewList[idx] = sortedReview;
			setWholeReviewList(afterReviewList);
		}
	};

	return (
		<>
			<div>
				<MyRadio
					type="radio"
					name="menu"
					value="STUDENT"
					id="menu1"
					checked={radioValue === "STUDENT"}
					onChange={handleRadioChange}
				/>
				<label htmlFor="menu1" style={{ padding: "0px 5px" }}>
					학생식당
				</label>
				<MyRadio
					type="radio"
					name="menu"
					value="STAFF"
					id="menu2"
					checked={radioValue === "STAFF"}
					onChange={handleRadioChange}
				/>
				<label htmlFor="menu2" style={{ padding: "0px 5px" }}>
					교직원식당
				</label>
				<div
					style={{
						margin: "10px 0px",
						padding: "15px",
						borderRadius: "10px",
						width: "100%",
						backgroundColor: "white",
					}}
				>
					{radioValue === "STUDENT"
						? studentMenu.map((item, index) => {
								return (
									<div key={index}>
										<MenuInfo
											mainMenu={item.dishList[0]}
											subMenu={item.dishList.slice(1)}
										/>
									</div>
								);
						  })
						: staffMenu.map((item, index) => {
								return (
									<div key={index}>
										<MenuInfo
											mainMenu={item.dishList[0]}
											subMenu={item.dishList.slice(1)}
										/>
									</div>
								);
						  })}
				</div>
			</div>
			<div>
				<CheckBoxInput
					type="checkbox"
					id="check"
					checked={isChecked}
					onChange={toggleOnlyImageReviewVisiblity}
				/>
				<CheckBoxLabel htmlFor="check" />
				<label htmlFor="check" style={{ marginLeft: 5, cursor: "pointer" }}>
					사진 리뷰만 보기
				</label>
				<SortingSelect value={sortOrder} onChange={handleSortChange}>
					<option value="latest">최근 등록순</option>
					<option value="earliest">오래된순</option>
					<option value="lowRate">별점 낮은순</option>
					<option value="highRate">별점 높은순</option>
				</SortingSelect>

				{radioValue === "STUDENT" ? (
					nowReviewList.filter((item) => item.dept === "STUDENT").length === 0 ? (
						<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
					) : (
						nowReviewList.map((nowReview, nowIndex) => {
							if (isChecked && nowReview.imgLink === "") {
								return null;
							}
							return radioValue === nowReview.dept ? (
								<ReviewItem
									review={nowReview}
									key={nowIndex}
									wholeReviewList={wholeReviewList}
									setWholeReviewList={setWholeReviewList}
								/>
							) : null;
						})
					)
				) : nowReviewList.filter((item) => item.dept === "STAFF").length === 0 ? (
					<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
				) : (
					nowReviewList.map((nowReview, nowIndex) => {
						if (isChecked && nowReview.imgLink === "") {
							return null;
						}
						return radioValue === nowReview.dept ? (
							<ReviewItem
								review={nowReview}
								key={nowIndex}
								wholeReviewList={wholeReviewList}
								setWholeReviewList={setWholeReviewList}
							/>
						) : null;
					})
				)}
			</div>
			<div className="blankSpace">&nbsp;</div>
		</>
	);
};

export default ReviewListType2;
