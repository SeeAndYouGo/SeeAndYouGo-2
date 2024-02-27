import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import ReviewItem from "./ReviewItem";

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

const ReviewList = ({
	idx,
	nowReviewList,
	wholeReviewList,
	setWholeReviewList,
}) => {
	const [isChecked, setIsChecked] = useState(false);
	const [sortOrder, setSortOrder] = useState("latest");

	const toggleOnlyImageReviewVisiblity = () => {
		setIsChecked(!isChecked);
	};

	const initialSetting = () => {
		setIsChecked(false);
		setSortOrder("latest");
	};

	useEffect(() => {
		initialSetting();
		const beforeReviewList = [...wholeReviewList];
		beforeReviewList[idx] = nowReviewList.sort(sortFunctions["latest"]);
		setWholeReviewList(beforeReviewList);
	}, [idx]);

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
				{nowReviewList.length === 0 ? (
					<NoReviewMessage>첫 리뷰의 주인공이 되어주세요!</NoReviewMessage>
				) : (
					nowReviewList.map((nowReview, nowIndex) => {
						if (isChecked && nowReview.imgLink === "") {
							return null;
						}
						return (
							<ReviewItem
								review={nowReview}
								key={nowIndex}
								isTotal={idx === 0 ? true : false}
								wholeReviewList={wholeReviewList}
								setWholeReviewList={setWholeReviewList}
							/>
						);
					})
				)}
			</div>
			<div className="blankSpace">&nbsp;</div>
		</>
	);
};

export default ReviewList;
