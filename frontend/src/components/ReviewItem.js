import styled from "@emotion/styled";
import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import moment from "moment";
// const ReviewItemContainer = styled.div`
//     /* display: flex;
//     flex-direction: column;
//     justify-content: center;
//     align-items: center;
//     width: 100%;
//     height: 100%; */
// `;

// const ReviewItemFirstRow = styled.div`

// `;

// // 리뷰 작성자 정보
// const MemberInfo = styled.div`

// `;

// const MemberPicture = styled.img`

// `;

// const MemberName = styled.p`

// `;

// const MemberRate = styled.p`

// `;

// const MemberWriteTime = styled.p`

// `;

// // 리뷰 작성된 식당 정보
// const ReviewPlaceInfo = styled.p`

// `;

// const ReviewPlaceName = styled.p`

// `;

// const ReviewPlaceDeptName = styled.p`

// `;

// // 리뷰 코멘트 및 이미지
// const ReviewItemSecondRow = styled.div`

// `;

// const ReviewComment = styled.p`

// `;

// const ReviewImage = styled.img`

// `;

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

const CalculateWriteTime = (inputTime, nowTime) => {
	const checkMinutes = moment.duration(inputTime.diff(nowTime)).asMinutes();
	if (checkMinutes < 60) {
		return `${Math.floor(checkMinutes)}분 전`;
	} else {
		return `${Math.floor(checkMinutes / 60)}시간 전`;
	}
};

const ReviewItem = ({ user, time, content, img, rate }) => {
	const targetTime = moment("2023-10-14 15:00:00");

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
								{rate}
							</ReviewItemStar>
							<span style={{ fontWeight: 400 }}>
								{CalculateWriteTime(targetTime, time)}
							</span>
						</div>
					</ReviewItemProfile>
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
