import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../../redux/slice/UserSlice";
import ReviewList from "./ReviewList";
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
		@media (min-width: 576px) {
			display: none;
			padding: 4px 17px;
		}
	}
	.submenu.tablet {
		display: none;
		@media (min-width: 576px) {
			display: block;
		}
	}

	.focused {
		background-color: black;
		color: white;
	}
`;

const reviewTitleArray = ["전체", "1학", "2학", "3학", "상록회관", "생과대"];
const tabletReviewTitleArray = ["전체", "1학생회관", "2학생회관", "3학생회관", "상록회관", "생활과학대학"];

const ReviewSelect = ({idx = 0}) => {
	const token_id = useSelector((state) => state.user.value.token);
	const [currentTab, clickTab] = useState(idx);
	const dispatch = useDispatch();

	const [reviewArray, setReviewArray] = useState([]);
	const CreateReviewUrl = (restaurantUrl) => config.BASE_URL + restaurantUrl + (config.NOW_STATUS === 0 ? ".json" : (token_id !== '' ? `/${token_id}` : ""));

	useEffect(() => {
		const reviewUrl = [
			CreateReviewUrl("/total-review"), CreateReviewUrl("/review/restaurant1"), CreateReviewUrl("/review/restaurant2"), CreateReviewUrl("/review/restaurant3"), CreateReviewUrl("/review/restaurant4"), CreateReviewUrl("/review/restaurant5"),
		];
		Promise.all(
			reviewUrl.map((path) =>
				fetch(path).then((response) => response.json())
			)
		)
		.then((dataArray) => {
			return setReviewArray(dataArray)
		})
		.catch((error) => console.error("Error fetching JSON:", error));
	}, [dispatch, token_id]);

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
				{tabletReviewTitleArray.map((el, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused tablet" : "submenu tablet"
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
					{
						reviewArray.length !== 0 &&
							<ReviewList
								idx={currentTab}
								nowReviewList={reviewArray[currentTab]}
								wholeReviewList={reviewArray}
								setWholeReviewList={setReviewArray}
							/>
					}
				</div>
			</div>
		</>
	);
};

export default ReviewSelect;

{/* {currentTab < 2 ? ( // 전체, 1학 탭
						reviewArray.length > 0 &&
						<ReviewList
							idx={currentTab}
							nowReviewList={reviewArray[currentTab]}
							wholeReviewList={reviewArray}
							setWholeReviewList={setReviewArray}
						/>
					) : currentTab > 3 ? ( // 상록회관, 생과대 탭
						<>
							{menuArray.length > 0 && <ReviewInfo nowMenu={menuArray[currentTab - 2]} />}
							{
								reviewArray.length > 0 && 
								<ReviewList
									idx={currentTab}
									nowReviewList={reviewArray[currentTab]}
									wholeReviewList={reviewArray}
									setWholeReviewList={setReviewArray}
								/>
							}
						</>
					) : (
						// 2학, 3학 탭
						reviewArray.length > 0 && menuArray.length > 0 &&
						<ReviewListType2
							idx={currentTab}
							nowReviewList={reviewArray[currentTab]}
							nowMenu={menuArray[currentTab - 2]}
							wholeReviewList={reviewArray}
							setWholeReviewList={setReviewArray}
						/>
					)} */}