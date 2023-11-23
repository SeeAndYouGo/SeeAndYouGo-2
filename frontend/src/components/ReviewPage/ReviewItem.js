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
	position: relative;
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
	margin: 0;
	float: left;

	& > .colorTag1 {color: #ff0000;}
	& > .colorTag2 {color: #ff8000;}
	& > .colorTag3 {color: #F4AA19;}
	& > .colorTag4 {color: #07903E;}
	& > .colorTag5 {color: #2274EE;}
`;

const DeptName = styled.p`
	width: 60px;
	margin: 0;
	padding-top: 2px;
	text-align: center;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 500;
	float: right;
`;

const MenuName = styled.p`
	font-size: 12px;
	margin: 5px 0 0 0;
	font-weight: 500;
	text-align: right;
	width: 100%;
	float: right;
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
	menuName
}) => {
	const tempTargetTime = moment().format("YYYY-MM-DD HH:mm:ss");
	const targetTime = moment(tempTargetTime);

	const getRestuarantIndex = (restaurantName) => {
		switch (restaurantName) {
		  case '1학생회관':
			return 1;
		  case '2학생회관':
			return 2;
		  case '3학생회관':
			return 3;
		  case '상록회관':
			return 4;
		  case '생활과학대':
			return 5;
		  default:
			return 0;
		}
	  };
	
	return (
		<>
			<ReviewItemContainer>
				<div className="Row1" style={{ width: "100%", float: "left" }}>
					<ReviewItemIcon>
						<FontAwesomeIcon icon={faCircleUser} />
					</ReviewItemIcon>
					<ReviewItemProfile>
						<p>{user}</p>
						<div style={{marginTop: 2}}>
							<ReviewItemStar style={{ fontWeight: 500}}>
								<FontAwesomeIcon icon={solidStar} />
								{rate % 1 === 0 ? rate + ".0" : rate}
							</ReviewItemStar>
							<span style={{ fontWeight: 400 }}>
								{CalculateWriteTime(targetTime, time)}
							</span>
							
						</div>
						
					</ReviewItemProfile>
					{isTotal && (
						<div style={{float:"right", width:"45%" }}>
							<RestaurantName>
								<span className={"colorTag"+getRestuarantIndex(restaurant)}>●&nbsp;</span>
								{restaurant}
							</RestaurantName>
							<DeptName>
								{dept === "STAFF"
									? "교직원식당"
									: "학생식당"}
							</DeptName>
							{menuName && (
								<MenuName>
									<span style={{color: "#777", fontWeight: 400}}>메뉴 </span> 
									{menuName}
								</MenuName>
							)}
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
