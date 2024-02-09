import React from "react";
import styled from "@emotion/styled";
import moment from "moment";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar, faCircleUser, faSpoon } from "@fortawesome/free-solid-svg-icons";
import DropDown from "../../components/Review/DropDown";
import * as config from "../../config";

const ReviewItemContainer = styled.div`
	width: 330px;
	background: #fff;
	padding: 8px 15px;
	border-radius: 20px;
	margin-bottom: 10px;
	float: left;
	position: relative;
`;

const ReviewItemIcon = styled.p`
	float: left;
	font-size: 35px;
	color: #555;
	margin: 0 10px 0 0;
`;

const ReviewItemProfile = styled.div`
	float: left;
	margin-top: 5px;

	> p {
		margin: 0;
		font-size: 16px;
	}
	> p:last-child {
		color: #777;
		font-weight: 400;
		font-size: 12px;
		margin-top: -2px;
	}
`;

const ReviewItemStar = styled.span`
	font-size: 12px;
	margin-left: 5px;
	> svg {
		color: #ffc107;
		font-size: 15px;
		margin-right: 2px;
	}
`;

const ReviewItemComment = styled.p`
	width: 100%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0 0 0;
`;

const DropDownContainer = styled.div`
	position: absolute;
	right: 15px;
	top: 10px;
`;

const MenuName = styled.p`
	font-size: 12px;
	margin: 5px 0 0 0;
	font-weight: 500;
	float: left;
	border: 1px solid #ccc;
	padding: 3px 10px;
	border-radius: 20px;
`;

const ReviewImage = styled.img`
	max-height: 80px;
	max-width: 80px;
	float: left;
	margin-top: 5;
`;

const calculateWriteTime = (inputTime, nowTime) => {
	const checkMinutes = moment.duration(inputTime.diff(nowTime)).asMinutes();
	if (checkMinutes < 60) {
		return `${Math.floor(checkMinutes)}분 전`;
	} else if (checkMinutes < 1440) {
		return `${Math.floor(checkMinutes / 60)}시간 전`;
	} else {
		return `${Math.floor(checkMinutes / 1440)}일 전`;
	}
};

const TopReviewItem = ({ nowReview }) => {
	const { reviewId, writer, madeTime, comment, rate, imgLink, menuName } =
		nowReview;

	const tempTargetTime = moment().format("YYYY-MM-DD HH:mm:ss");
	const targetTime = moment(tempTargetTime);

	return (
		<ReviewItemContainer>
			<div className="Row1" style={{ width: "100%", float: "left" }}>
				<ReviewItemIcon>
					<FontAwesomeIcon icon={faCircleUser} />
				</ReviewItemIcon>
				<ReviewItemProfile>
					<div>
						{writer}
						<ReviewItemStar>
							<FontAwesomeIcon icon={solidStar} />
							{rate % 1 === 0 ? rate + ".0" : rate}
						</ReviewItemStar>
					</div>
					<span style={{ fontWeight: 400, fontSize: 12 }}>
						{calculateWriteTime(targetTime, madeTime)}
					</span>
				</ReviewItemProfile>
				<DropDownContainer>
					<DropDown targetId={reviewId} />
				</DropDownContainer>
			</div>
			<div className="Row2" style={{ width: "100%" }}>
				<ReviewItemComment>{comment}</ReviewItemComment>
			</div>
			<div className="Row3" style={{ width: "100%", marginBottom: "10px" }}>
				{imgLink === "" ? null : (
					<ReviewImage
						src={
							config.NOW_STATUS === 0
								? `/assets/images/${imgLink}`
								: `${imgLink}`
						}
						alt="Loading.."
					/>
				)}
			</div>
			<MenuName>
				{menuName}&nbsp;
				<FontAwesomeIcon icon={faSpoon} />
			</MenuName>
		</ReviewItemContainer>
	);
};

export default TopReviewItem;
