import styled from "@emotion/styled";
import React, { useState } from "react";
import StarsRating from "react-star-rate";
import MenuSelector from "./MenuSelector";
import axios from "axios";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera } from "@fortawesome/free-solid-svg-icons";
import * as config from "../../config";

const ReviewWriteContainer = styled.form`
	width: 100%;
	background: #fff;
	padding: 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;

	& .rs-picker-toggle-placeholder,
	& .rs-picker-search-bar-input {
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
	> ul {
		font-size: 22px;
	}
	> ul > li {
		margin-right: 5px;
	}
`;
const ReviewWriteInputWrapper = styled.div`
	height: 100px;
	width: 100%;
	float: left;
	margin-bottom: 10px;
	border: solid 1px #e5e5e5;
	position: relative;
	border-radius: 10px;
`;
const ReviewWriteInput = styled.textarea`
	border: none;
	background: none;
	resize: none;
	color: #999;
	padding: 10px;
	height: 50px;
	outline: none;
	float: right;
	font-size: 12px;
	font-weight: 400;
	width: 100%;
	&::placeholder {
		color: #888;
		font-weight: 400;
		font-size: 12px;
	}
`;
const ReviewWriteCamera = styled.label`
	color: #d9d9d9;
	font-size: 22px;
	position: absolute;
	padding: 3px 7px;
	cursor: pointer;
	border: solid 1px #eee;
	border-radius: 5px;
	bottom: 10px;
	left: 10px;
`;

const ReviewWriteButton = styled.button`
	width: 100%;
	margin-top: 10px;
	font-size: 12px;
	background: #d9d9d9;
	color: "#777";
	border-radius: 10px;
	border: none;
	height: 30px;
	float: left;
	font-weight: 400;
	cursor: pointer;
`;
const ReviewWriteNameCheckbox = styled.input`
	float: left;
	width: 15px;
	height: 15px;
	margin-left: 5px;
	margin-right: 10px;
	margin-top: 3px;
	position: relative;
	top: 50%;
	transform: translateY(-50%);
`;
const ReviewWriteRatingLabel = styled.p`
	margin: 0 10px 0 0;
	line-height: 30px;
	float: left;
	font-size: 15px;
	text-align: left;
`;

const ReviewWriteForm = ({ restaurantName, deptName }) => {
	// const [checked, setChecked] = useState(false);
	const [starVal, setStarVal] = useState(0);
	// const [writerName, setWriterName] = useState("");
	const [comment, setComment] = useState("");
	const [selectedMenu, setSelectedMenu] = useState("");
	const [image, setImage] = useState();
	// 이미지 이름 필요 없다고 생각되어 일단 삭제

	const token = localStorage.getItem("token");

	const onChangeImage = (e) => {
		setImage(e.target.files[0]);
		if (e.target.files[0] == null) {
			return;
		}
	};

	const handleSelectMenu = (value) => {
		setSelectedMenu(value);
	};

	const ReviewSubmit = async (e) => {
		e.preventDefault();

		const formdata = new FormData();
		// 식당이름 restaurant
		formdata.append("restaurant", restaurantName);
		// 식당구분 dept
		formdata.append("dept", deptName);
		// 메뉴이름 menuName
		// 1학 부분을 위해 selectedMenu 넣은건데 확인 필요합니다.
		formdata.append("menuName", restaurantName === 1 ? selectedMenu : "");
		// 평점 rate
		formdata.append("rate", starVal);
		// 작성자 writer
		// formdata.append("writer", writerName === "" ? "익명" : writerName);
		formdata.append("writer", token);
		// 리뷰 comment
		formdata.append("comment", comment);
		// 이미지 추가
		formdata.append("image", image);

		// formdata 확인
		let entries = formdata.entries();
		for (const pair of entries) {
			console.log(pair[0] + ": " + pair[1]);
		}

		axios
			.post(config.DEPLOYMENT_BASE_URL+"/review", formdata, {
				headers: {
					"Content-Type": "multipart/form-data",
				},
			})
			.then((response) => {
				console.log(response);
				alert("리뷰가 등록되었습니다.");
				console.log(response.data);
				window.location.reload();
			})
			.catch((error) => console.log("error", error));
	};

	return (
		<ReviewWriteContainer>

			<div style={{ width: "100%", float: "left" }}>
				<ReviewWriteRatingLabel>별점</ReviewWriteRatingLabel>
				<ReviewStarRating>
					<StarsRating
						value={starVal}
						onChange={(value) => {
							setStarVal(value);
						}}
					/>
				</ReviewStarRating>
			</div>

			{restaurantName === 1 ? (
				<MenuSelector onSelectMenu={handleSelectMenu} />
			) : null}

			<div style={{ width: "100%", float: "left"}}>
				<div
					style={{
						position: "relative",
						width: "100%",
						float: "left",
					}}
				>
					<input
						hidden
						type="file"
						accept="image/*"
						id="Review-file-input"
						onChange={onChangeImage}
					/>
					<ReviewWriteInputWrapper>
						<ReviewWriteInput
							type="text"
							onChange={(val) => setComment(val.target.value)}
							placeholder="리뷰를 남겨주세요 :)"
						/>
						<ReviewWriteCamera htmlFor="Review-file-input">
							<FontAwesomeIcon icon={faCamera} />
						</ReviewWriteCamera>
					</ReviewWriteInputWrapper>

				</div>
				{starVal !== 0 ? (
					<ReviewWriteButton onClick={ReviewSubmit}>
						작성
					</ReviewWriteButton>
				) : (
					<ReviewWriteButton disabled onClick={ReviewSubmit}>
						작성
					</ReviewWriteButton>
				)}
			</div>
		</ReviewWriteContainer>
	);
};

const ReviewWrite = ({ restaurantName, deptName, nowMainMenu }) => {
	return (
		<div style={{ width:"100%", float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 18, margin: 0, textAlign: "left" }}>
				오늘의 메뉴 리뷰 남기기
			</p>
			<ReviewWriteForm
				restaurantName={restaurantName}
				deptName={restaurantName === 1 ? "STUDENT" : deptName}
				nowMainMenu={nowMainMenu}
			/>
		</div>
	);
};

export default ReviewWrite;
