import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import ReviewList from "./ReviewList";
import ReviewInfo from "./ReviewInfo";
import ReviewListType2 from "./ReviewListType2";
import * as config from "../../config";

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

const reviewTitleArray = ["전체", "1학", "2학", "3학", "상록회관", "생과대"];

const ReviewSelect = () => {
	const [currentTab, clickTab] = useState(0);

	const [reviewArray, setReviewArray] = useState([]);
	const [menuArray, setMenuArray] = useState([]);

	useEffect(() => {
		const reviewUrl = [
			config.BASE_URL +
				"/totalReview" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/review/restaurant1" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/review/restaurant2" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/review/restaurant3" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/review/restaurant4" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/review/restaurant5" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
		];

		Promise.all(
			reviewUrl.map((path) =>
				fetch(path).then((response) => response.json())
			)
		)
			.then((dataArray) => setReviewArray(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));

		const menuUrl = [
			config.BASE_URL +
				"/dailyMenu/restaurant2" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/dailyMenu/restaurant3" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/dailyMenu/restaurant4" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
			config.BASE_URL +
				"/dailyMenu/restaurant5" +
				(config.NOW_STATUS === 0 ? ".json" : ""),
		];

		Promise.all(
			menuUrl.map((path) =>
				fetch(path).then((response) => response.json())
			)
		)
			.then((dataArray) => setMenuArray(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));
	}, []);

	const selectMenuHandler = (index) => {
		clickTab(index);
	};

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{reviewTitleArray.map((el, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused" : "submenu"
						}
						onClick={() => selectMenuHandler(index)}
					>
						{el}
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
					{currentTab < 2 ? ( // 전체, 1학 탭
						<ReviewList
							idx={currentTab}
							nowReviewList={reviewArray[currentTab]}
						/>
					) : currentTab > 3 ? ( // 상록회관, 생과대 탭
						<>
							<ReviewInfo nowMenu={menuArray[currentTab - 2]} />
							<ReviewList
								idx={currentTab}
								nowReviewList={reviewArray[currentTab]}
							/>
						</>
					) : (
						// 2학, 3학 탭
						<ReviewListType2
							nowReviewList={reviewArray[currentTab]}
							nowMenu={menuArray[currentTab - 2]}
							idx={currentTab}
						/>
					)}
				</div>
			</div>
		</>
	);
};

export default ReviewSelect;
