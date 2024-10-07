import React from "react";
import styled from "@emotion/styled";
import moment from "moment";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar, faCircleUser, faSpoon } from "@fortawesome/free-solid-svg-icons";
import * as config from "../../config";

const ReviewItemContainer = styled.div`
	width: 100%;
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
	margin: 5px 0;

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
	white-space: pre-wrap;
`;

const MenuName = styled.p`
	font-size: 12px;
	margin: 10px 0 0 0;
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
	margin-top: 10px;
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
	const { writer, madeTime, comment, rate, imgLink, menuName } =
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
			</div>
			<div className="Row2" style={{ width: "100%", display: "inline-block" }}>
				<ReviewItemComment>{comment}</ReviewItemComment>
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
			<div className="Row3" style={{ width: "100%" }}>
				<MenuName>
					{menuName}&nbsp;
					<FontAwesomeIcon icon={faSpoon} />
				</MenuName>
			</div>
		</ReviewItemContainer>
	);
};

export default TopReviewItem;
