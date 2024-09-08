import React, { useState } from "react";
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
	[],
	["전체", "조식", "중식-학생", "중식-교직원"],
	["전체", "중식-학생", "중식-교직원", "석식"],
	[],
	[],
];
// ["전체", "라면&간식", "양식", "스낵", "일식", "중식", "한식"],

const TopReview = ({ idx, nowReviewList = [] }) => {
	const [isChecked, setIsChecked] = useState(false);

	const toggleOnlyImageReviewVisiblity = () => {
		setIsChecked(!isChecked);
	};

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

			{idx === 1 || idx === 4 || idx === 5 ? null : (
				<TopReviewSelect>
					{SelectOptionArray[idx].map((item, index) => (
						<option key={index} value={index}>
							{item}
						</option>
					))}
				</TopReviewSelect>
			)}
			{nowReviewList.length === 0 ? (
				<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
			) : (
				nowReviewList.map((nowData, index) => {
					if (isChecked && nowData.imgLink === "") {
						return null;
					}
					return <ReviewItem review={nowData} key={index} />;
				})
			)}

			<div className="blankSpace">&nbsp;</div>
		</>
	);
};

export default TopReview;
