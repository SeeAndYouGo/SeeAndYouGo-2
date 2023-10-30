import styled from "@emotion/styled";
import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

import StarsRating from "react-star-rate";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar as solidStar } from "@fortawesome/free-solid-svg-icons";
import { faCamera } from "@fortawesome/free-solid-svg-icons";
import { faCircleUser } from "@fortawesome/free-solid-svg-icons";
import { faPen } from "@fortawesome/free-solid-svg-icons";
import { faTrash } from "@fortawesome/free-solid-svg-icons";

import 'rsuite/dist/rsuite-no-reset.min.css';
import { Cascader } from 'rsuite';

const ReviewWriteContainer = styled.form`
	width: 100%;
	background: #fff;
	padding: 15px;
	border-radius: 20px;
	margin: 10px 0 20px 0;
	float: left;

	& .rs-picker-toggle-placeholder, & .rs-picker-search-bar-input {
		font-size: 12px;
		font-weight: 400;
	}
	& .rs-picker-cascader-menu-items {
		font-size: 12px;
	}
`;
const ReviewStarRating = styled.span`
	float: left;
	color: #d9d9d9;
	margin-top: -5px;
	margin-left: 10px;
	> ul {
		font-size: 20px;
	}
	> ul > li {
		margin-right: 3px;
	}
`;
const ReviewWriteInput = styled.input`
	color: #999;
	border: 1px solid #d9d9d9;
	border-radius: 10px;
	padding: 0 10px;
	height: 35px;
	outline: none;
	float:right;
	width: 180px;

	&::placeholder {
		font-weight: 400;
		font-size: 12px;
	}
`;
const ReviewWriteCamera = styled.label`
	color: #d9d9d9;
	font-size: 22px;
	position: absolute;
	right: 10px;
	line-height: 35px;
	cursor: pointer;
`;
const ReviewWriteButton = styled.button`
	width: 45px;
	font-size: 12px;
	background: #d9d9d9;
	color: "#777";
	border-radius: 10px;
	border: none;
	height: 35px;
	float: right;
	font-weight: 400;
	cursor: pointer;
`;
const ReviewWriteNameChekbox = styled.input`
	float: left;
	width: 15px;
	height: 15px;
	margin-left: 5px;
	top: 50%;
	transform: translateY(-50%);
	position: absolute;
`;

const ReviewSubmit = (e) => {
	e.preventDefault();
	const formData = new FormData();
}

const ReviewMenuSelect = () => {
	const [menuData, setMenuData] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			const res = await fetch(`/assets/json/Restaurant1Menu.json`, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			setMenuData(data);
		});
	}, []);

	return (
		<div style={{ display: 'block', marginBottom: 10 }}>
			<p style={{ margin: "0", float: "left", fontSize: 15 }}>메뉴 선택</p>
			<Cascader
				style={{ width: "100%", marginTop: 5 }}
				placeholder="메뉴를 선택해주세요"
				data={menuData}
			/>
		</div>
	);
}

const ReviewWrite = () => {
	const [starVal, setStarVal] = useState(0);
	const params = useParams();
	const restaurant = params.restaurant;

	return (
		<ReviewWriteContainer>
			{restaurant == 1 ? <ReviewMenuSelect /> : null}
			<div style={{ width: "50%", float: "left" }}>
				<p style={{ margin: "0", float: "left", fontSize: 15 }}>별점</p>
				<ReviewStarRating>
					<StarsRating
						value={starVal}
						onChange={(value) => {
							setStarVal(value);
						}}
					/>
				</ReviewStarRating>
			</div>
			<div style={{ width: "50%", float: "left", height: 30 }}>

				<ReviewWriteInput placeholder="닉네임" style={{ height: 25, float: "right", width: "90%", lineHeight: 25 }} />
			</div>
			<div style={{ width: "100%", float: "left", marginTop: 5 }}>
				<div style={{ position: "relative", width: "calc(100% - 55px)", float: "left" }}>
					<input type="file" id="Review-file-input" hidden></input>
					<p style={{ margin: "0", float: "left", fontSize: 15, lineHeight: "35px" }}>익명</p>
					<ReviewWriteNameChekbox type="checkbox" />
					<ReviewWriteInput placeholder="리뷰를 남겨주세요 :)" />

					<ReviewWriteCamera htmlFor="Review-file-input">
						<FontAwesomeIcon icon={faCamera} />
					</ReviewWriteCamera>
				</div>
				<ReviewWriteButton onClick={ReviewSubmit}>작성</ReviewWriteButton>
			</div>
		</ReviewWriteContainer>
	);
};

const ReviewItemContainer = styled.div`
	width: 100%;
	background: #fff;
	padding: 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
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
const ReviewItemButtons = styled.div`
	float: right;
	font-size: 14px;
	color: #999;
	> svg {
		margin-left: 5px;
		cursor: pointer;
	}
`;
const ReviewItemContent = styled.p`
	width: 100%;
	font-size: 14px;
	font-weight: 400;
	margin: 5px 0 0 0;
`;

// backend에서 받아온 데이터를 이용하여 리뷰 아이템 생성
// 시간은 어떻게 받아올 것인지 고민해보기
// 수정, 삭제는 어떻게 백엔드와 연동할 것인지 고민해보기
const ReviewItem = ({ user, time, content, img, rate }) => {
	return (
		<ReviewItemContainer>
			<div className="Row1" style={{ width: "100%", float: "left" }}>
				<ReviewItemIcon>
					<FontAwesomeIcon icon={faCircleUser} />
				</ReviewItemIcon>
				<ReviewItemProfile>
					<p>
						{user}
						<ReviewItemStar>
							<FontAwesomeIcon icon={solidStar} />
							{rate}
						</ReviewItemStar>
					</p>
					<p>{time}분 전</p>
				</ReviewItemProfile>
				<ReviewItemButtons>
					<FontAwesomeIcon icon={faPen} />
					<FontAwesomeIcon icon={faTrash} />
				</ReviewItemButtons>
			</div>
			<div className="Row2" style={{ float: "left", width: "100%" }}>
				<ReviewItemContent>{content}</ReviewItemContent>
			</div>
			<div className="Row3" style={{ width: "100%", float: "left" }}>
				<img
					src={img}
					alt="Loading.."
					style={{ maxHeight: 120, float: "left", marginTop: 5 }}
				></img>
			</div>
		</ReviewItemContainer>
	);
};

const Review = () => {
	return (
		<div style={{ float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 18, margin: 0 }}>오늘 메뉴의 리뷰</p>
			<ReviewWrite />
			<ReviewItem
				user={"익명1"}
				time={20}
				rate={5}
				content={"맵지만 맛있게 먹었다 !"}
				img={"/assets/images/menu1.jpg"}
			/>
			<ReviewItem
				user={"익명2"}
				time={55}
				rate={4}
				content={"닭갈비 은근 양 많다"}
			/>
			<ReviewItem user={"익명3"} time={55} rate={3} content={"맛있음"} />
		</div>
	);
};

export default Review;
