import React, { useEffect, useState, useMemo } from "react";
import styled from "@emotion/styled";
import ReviewItem from "../../components/Review/ReviewItem";

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
	margin-left: 20px;
	margin-top: 5px;
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

const TopReviewSelect = styled.select`
	width: 105px;
	height: 20px;
	font-size: 12px;
	float: right;
	margin-top: 3px;
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

const SelectOptionArray = [
	[],
	["전체", "라면&간식", "스낵", "한식", "일식", "중식", "양식"],
	// ["전체", "조식", "중식-학생", "중식-교직원"],
	// ["전체", "중식-학생", "중식-교직원", "석식"],
];

const OptionValueArray = [
	[],
	["total", "noodle", "snack", "korean", "japanese", "chinese", "western"],
	// ["total", "breakfast", "lunch-student", "lunch-staff", "dinner"],
	// ["total", "lunch-student", "lunch-staff", "dinner"],
];

const TopReview = ({
	idx,
	wholeReviewList,
	setWholeReviewList,
	onDeleteSuccess,
	setIsLoginModalOpen
}) => {
	const [isChecked, setIsChecked] = useState(false);
	const [selectedReviewType, setSelectedReviewType] = useState("total");
	const [nowReviewList, setNowReviewList] = useState(wholeReviewList[idx - 1] || []);

	useEffect(() => {
		setNowReviewList(wholeReviewList[idx - 1] || []);
	}, [wholeReviewList, idx]);

	useEffect(() => {
		setSelectedReviewType("total");
		setIsChecked(false);
	}, [idx]);

	const sortFunctions = (beforeData, typeValue) => {
		switch (typeValue) {
			case "total":
				return beforeData;
			// case "breakfast":
			// 	return beforeData.filter((data) => data.menuType === "BREAKFAST");
			// case "lunch-student":
			// 	return beforeData.filter(
			// 		(data) => data.menuType === "LUNCH" && data.dept === "STUDENT"
			// 	);
			// case "lunch-staff":
			// 	return beforeData.filter(
			// 		(data) => data.menuType === "LUNCH" && data.dept !== "STUDENT"
			// 	);
			// case "dinner":
			// 	return beforeData.filter((data) => data.menuType === "DINNER");

			case "noodle":
				return beforeData.filter((data) => data.dept === "NOODLE");
			case "snack":
				return beforeData.filter((data) => data.dept === "SNACK");
			case "korean":
				return beforeData.filter((data) => data.dept === "KOREAN");
			case "japanese":
				return beforeData.filter((data) => data.dept === "JAPANESE");
			case "chinese":
				return beforeData.filter((data) => data.dept === "CHINESE");
			case "western":
				return beforeData.filter((data) => data.dept === "WESTERN");
			default:
				return beforeData;
		}
	};

	const toggleOnlyImageReviewVisiblity = () => {
		setIsChecked((prev) => !prev);
	};

	const handleSelectType = (e) => {
		const selectedType = e.target.value;
		setSelectedReviewType(selectedType);
	};

	const reviewData = useMemo(() => {
		const sortedData = sortFunctions(
			[...nowReviewList].sort(
				(a, b) => new Date(b.madeTime) - new Date(a.madeTime)
			),
			selectedReviewType
		);
		return isChecked
			? sortedData.filter((data) => data.imgLink !== "")
			: sortedData;
	}, [nowReviewList, isChecked, selectedReviewType]);

	return (
		<>
			<div className="blankSpace">&nbsp;</div>
			<span style={{ fontSize: 22, float: "left", fontWeight: 700 }}>
				리뷰 보기
			</span>

			<CheckBoxInput
				type="checkbox"
				id="check"
				checked={isChecked}
				onChange={toggleOnlyImageReviewVisiblity}
			/>
			<CheckBoxLabel htmlFor="check" />
			<label htmlFor="check" style={{ marginLeft: 5, cursor: "pointer" }}>
				사진 리뷰만
			</label>

			{/* {idx === 4 || idx === 5 ? null : ( */}
			{idx === 1 ?  
				<TopReviewSelect value={selectedReviewType} onChange={handleSelectType}>
					{(SelectOptionArray[idx] || []).map((item, index) => (
					<option key={index} value={OptionValueArray[idx][index]}>
						{item}
					</option>
					))}
				</TopReviewSelect>
				: null
			}

			{reviewData.length === 0 ? (
			<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
			) : (
			reviewData.map((nowData, index) => (
				<ReviewItem
					idx={idx}
					key={index}
					setIsLoginModalOpen={setIsLoginModalOpen}
					review={nowData}
					wholeReviewList={wholeReviewList}
					setWholeReviewList={setWholeReviewList}
					onDeleteSuccess={onDeleteSuccess}
				/>
			))
			)}

			<div className="blankSpace">&nbsp;</div>
		</>
	);
};

export default TopReview;
