import React, { useState } from "react";
import styled from "@emotion/styled";
import ReviewList from "./ReviewList";
import ReviewInfo from "./ReviewInfo";

const TabMenu = styled.ul`
	color: black;
	font-size: 12px;
	display: flex;
	list-style: none;
	border: solid 1.5px black;
	border-radius: 20px;
	padding: 5px;

	.submenu {
		text-align: center;
		padding: 4px 10px;
		margin: 0 auto;
		border-radius: 20px;
		cursor: pointer;
	}

	.focused {
		background-color: black;
		color: white;
	}
`;

const reviewArray = [
	{
		name: "전체",
		content: <ReviewList idx={0}/>,
	},
	{
		name: "1학",
		content: (
			<>
				<ReviewList idx={1}/>
			</>
		),
	},
	{
		name: "2학",
		content: (
			<>
				<ReviewInfo idx={2} />
			</>
		),
	},
	{
		name: "3학",
		content: (
			<>
				<ReviewInfo idx={3} />
			</>
		),
	},
	{
		name: "상록회관",
		content: (
			<>
				<ReviewInfo idx={4} />
			</>
		),
	},
	{
		name: "생과대",
		content: (
			<>
				<ReviewInfo idx={5} />
			</>
		),
	},
];

const ReviewSelect = () => {
	const [currentTab, clickTab] = useState(0);

	const selectMenuHandler = (index) => {
		clickTab(index);
	};

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{reviewArray.map((el, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused" : "submenu"
						}
						onClick={() => selectMenuHandler(index)}
					>
						{el.name}
					</li>
				))}
			</TabMenu>
		);
	};

	return (
		<>
			<div>
				<TabMenuUl />
				<div className="desc">
					<div>{reviewArray[currentTab].content}</div>
				</div>
			</div>
		</>
	);
};

export default ReviewSelect;
