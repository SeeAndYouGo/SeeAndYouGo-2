import styled from "@emotion/styled";
import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import moment from "moment";

const ReviewItemContainer = styled.div`
	width: 100%;
	background: #fff;
	padding: 10px 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
`;
const ReviewItemIcon = styled.p`
	float: left;
	font-size: 30px;
	color: #555;
	margin: 0px;
	position: relative;
	top: -3px;
`;
const ReviewItemProfile = styled.div`
	float: left;
	margin-left: 5px;
	> p {
		margin: 0;
		font-size: 16px;
	}
	> div {
		color: #777;
		font-size: 10px;
		margin-top: -2px;
	}
`;

const ReviewItemStar = styled.span`
	font-size: 10px;
	margin-right: 5px;
	> svg {
		color: #ffc107;
		font-size: 10px;
		margin-right: 2px;
	}
`;

const ReviewItemContent = styled.p`
	width: 100%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0;
`;

const RestaurantName = styled.p`
	font-size: 12px;
	font-weight: 500;
	color: #555555;
`;

const DeptName = styled.p`
	width: 60px;
	margin: 10px 0px 10px 10px;
	padding-top: 2px;
	text-align: center;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 500;
`;

const CalculateWriteTime = (inputTime, nowTime) => {
	const checkMinutes = moment.duration(inputTime.diff(nowTime)).asMinutes();
	if (checkMinutes < 60) {
		return `${Math.floor(checkMinutes)}분 전`;
	} else if (checkMinutes < 1440) {
		return `${Math.floor(checkMinutes / 60)}시간 전`;
	} else {
		return `${Math.floor(checkMinutes / 1440)}일 전`;
	}
};

const ReviewItem = ({
	user,
	restaurant,
	dept,
	time,
	content,
	img,
	rate,
	isTotal,
}) => {
	const tempTargetTime = moment().format("YYYY-MM-DD HH:mm:ss");
	const targetTime = moment(tempTargetTime);

	return (
		<>
			<ReviewItemContainer>
				<div className="Row1" style={{ width: "100%", float: "left" }}>
					<ReviewItemIcon>
						<FontAwesomeIcon icon={faCircleUser} />
					</ReviewItemIcon>
					<ReviewItemProfile>
						<p>{user}</p>
						<div>
							<ReviewItemStar style={{ fontWeight: 500 }}>
								<FontAwesomeIcon icon={solidStar} />
								{rate % 1 === 0 ? rate + ".0" : rate}
							</ReviewItemStar>
							<span style={{ fontWeight: 400 }}>
								{CalculateWriteTime(targetTime, time)}
							</span>
							
						</div>
						
					</ReviewItemProfile>
					{isTotal && (
								<div style={{display: "flex", float:"right" }}>
									<RestaurantName>
										{restaurant}
									</RestaurantName>
									<DeptName>
										{dept === "STAFF"
											? "교직원식당"
											: "학생식당"}
									</DeptName>
								</div>
							)}
				</div>
				<div className="Row2" style={{ float: "left", width: "100%" }}>
					<ReviewItemContent>{content}</ReviewItemContent>
					{img === "" ? null : (
						<img
							src={`/assets/images/${img}`}
							alt="Loading.."
							style={{
								maxHeight: 80,
								maxWidth: 80,
								float: "left",
								marginTop: 5,
							}}
						/>
					)}
				</div>
			</ReviewItemContainer>
		</>
	);
};

export default ReviewItem;
