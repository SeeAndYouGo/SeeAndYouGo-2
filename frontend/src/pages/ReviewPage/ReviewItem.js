import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import moment from "moment";
import axios from "axios";
import { useSelector, useDispatch } from "react-redux";
import { changeToastIndex } from "../../redux/slice/ToastSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import { faSpoon } from "@fortawesome/free-solid-svg-icons";
import { faHeart } from "@fortawesome/free-regular-svg-icons";
import DropDown from "../../components/Review/DropDown";
import Modal from "../../components/Modal";
import ModalImageZoom from "./ModalImageZoom";
import Toast from "../../components/Toast";
import * as config from "../../config";

const ReviewItemContainer = styled.div`
	width: 100%;
	background: #fff;
	padding: 15px;
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
	width: 70%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0;
	float: left;
`;

const RestaurantName = styled.p`
	font-size: 12px;
	font-weight: 500;
	color: #555555;
	margin: 0;
	float: right;
	width: 100%;
	text-align: right;

	& > .colorTag1 {
		color: #ff0000;
	}
	& > .colorTag2 {
		color: #ff8000;
	}
	& > .colorTag3 {
		color: #f4aa19;
	}
	& > .colorTag4 {
		color: #07903e;
	}
	& > .colorTag5 {
		color: #2274ee;
	}
`;

const DeptName = styled.p`
	margin: 2px 0 0 0;
	text-align: center;
	// background-color: rgba(0, 0, 0, 0.3);
	color: #999;
	border-radius: 5px;
	font-size: 11px;
	text-align: center;
	font-weight: 400;
	float: right;
`;

const MenuName = styled.p`
	font-size: 12px;
	margin: 5px 0 0 0;
	font-weight: 500;
	float: left;
	border: 1px solid #ccc;
	padding: 2px 10px;
	border-radius: 20px;
`;

const ReviewImage = styled.img`
	max-height: 80px;
	max-width: 80px;
	float: left;
	margin-top: 5;
	cursor: zoom-in;
`;

const ReviewLike = styled.div`
	position: absolute;
	bottom: 15px;
	right: 15px;
	// float: right;
	border: solid 1px #d9d9d9;
	border-radius: 10px;
	padding: 1px 8px 0 8px;
	font-size: 12px;
	color: #777;
	font-weight: 400;
	cursor: pointer;
	&.liked {
		color: #ff0000;
		border: solid 1px #ff0000;
	}
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
	userName,
	restaurant,
	dept,
	time,
	content,
	img,
	rate,
	isTotal,
	menuName,
	reviewId,
	liked,
	likeCount
}) => {
	const tempTargetTime = moment().format("YYYY-MM-DD HH:mm:ss");
	const targetTime = moment(tempTargetTime);
	const [like, setLike] = useState(false);
	const [imgVisible, setImgVisible] = useState(false);
  const user = useSelector((state) => state.user.value);
	const token_id = user.token;
	const dispatch = useDispatch();

	console.log("liked",liked);

	const getRestuarantIndex = (restaurantName) => {
		switch (restaurantName) {
			case "1학생회관":
				return 1;
			case "2학생회관":
				return 2;
			case "3학생회관":
				return 3;
			case "상록회관":
				return 4;
			case "생활과학대":
				return 5;
			default:
				return 0;
		}
	};

	useEffect(() => {
		setLike(liked);
	}, []);

	useEffect(() => {
		console.log(like)
	}, [like]);


	// useEffect(() => {
	// 	if (user.loginState === true) {
	// 		axios.post(config.DEPLOYMENT_BASE_URL + `/review/like/${reviewId}/${token_id}`, {
	// 		}).then((res) => {
	// 			const isLike = JSON.parse(res.request.response).like;
	// 			console.log("init:",isLike)
	// 			if (isLike === true) { // true면 공감상태
	// 				// setLike(true);
	// 			} else {
	// 				// setLike(false);
	// 			}
	// 		}).catch((err) => {
	// 			console.log(err);
	// 		});
	// 	}
	// }, [])

	const handleLike = () => {
		// review id와 token_id를 보내서 공감상태인지 아닌지 확인
		// if (user === user.nickname) {
		// 	dispatch(changeToastIndex(6));
		// }
		if (user.loginState === false) {
			dispatch(changeToastIndex(5));
			return;
		} else {
			axios.post(config.DEPLOYMENT_BASE_URL + `/review/like/${reviewId}/${token_id}`, {
			}).then((res) => {
				const isLike = JSON.parse(res.request.response).like;
				console.log("islike",isLike)
				if (isLike === true) { // true면 공감상태
					setLike(false);
				} else { // false면 공감 취소
					setLike(true);
				}
			}).catch((err) => {
				console.log(err);
			});
		}
	};

	return (
			<ReviewItemContainer>
				<div className="Row1" style={{ width: "100%", float: "left" }}>
					<ReviewItemIcon>
						<FontAwesomeIcon icon={faCircleUser} />
					</ReviewItemIcon>
					<ReviewItemProfile>
						<p>{userName}</p>
						<div style={{ marginTop: 2 }}>
							<ReviewItemStar style={{ fontWeight: 500 }}>
								<FontAwesomeIcon icon={solidStar} />
								{rate % 1 === 0 ? rate + ".0" : rate}
							</ReviewItemStar>
							<span style={{ fontWeight: 400 }}>
								{CalculateWriteTime(targetTime, time)}
							</span>
						</div>
					</ReviewItemProfile>
					<div style={{position: "relative",float: "right", marginLeft: 5}} >
						<DropDown targetId={reviewId} />
					</div>
					{isTotal && (
						<div style={{ float: "right", width: "45%" }}>
							<RestaurantName>
								<span
									className={
										"colorTag" +
										getRestuarantIndex(restaurant)
									}
								>
									●&nbsp;
								</span>
								{restaurant}
							</RestaurantName>

							{getRestuarantIndex(restaurant) !== 1 ? (
								<DeptName>
									{dept === "STAFF"
										? "교직원식당"
										: "학생식당"}
								</DeptName>
							) : null}
						</div>
					)}
				</div>
				<div
					className="Row2"
					style={{ float: "left", width: "100%", marginTop: 5 }}
				>
					<ReviewItemContent>{content}</ReviewItemContent>
					{img === "" ? null : (
						<ReviewImage
							src={
								config.NOW_STATUS === 0
									? `/assets/images/${img}`
									: `${img}`
							}
							alt="Loading.."
							onClick={() => {
								setImgVisible(true);
							}}
						/>
					)}
					<Modal
						visible={imgVisible}
						onClose={() => {
							setImgVisible(false);
						}}
					>
						<ModalImageZoom
							imgLink={
								config.NOW_STATUS === 0
									? `/assets/images/${img}`
									: `${img}`
							}
						/>
					</Modal>
				</div>
				<ReviewLike onClick={handleLike} className={like ? 'liked' : ''}>
					<FontAwesomeIcon icon={faHeart} /> {likeCount}
				</ReviewLike>
				{isTotal && menuName && (
					<MenuName>
						{menuName}&nbsp;
						<FontAwesomeIcon icon={faSpoon} />
					</MenuName>
				)}

			</ReviewItemContainer>
	);
};

export default ReviewItem;
