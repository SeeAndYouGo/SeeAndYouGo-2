import React, { useRef, useState } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import StarsRating from "react-star-rate";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera } from "@fortawesome/free-solid-svg-icons";
import MenuSelector from "./MenuSelector";
import * as config from "../../config";

const ReviewWriteContainer = styled.form`
	width: 100%;
	background: #fff;
	padding: 15px;
	border-radius: 20px;
	margin-top: 10px;
	float: left;
	position: relative;

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
	height: 120px;
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
	height: 60px;
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
	padding: 7px 12px;
	cursor: pointer;
	border: dashed 1px #e5e5e5;
	border-radius: 5px;
	float: left;
	margin: 5px 5px 0 10px;
`;

const ReviewWriteButton = styled.button`
	width: 100%;
	margin-top: 10px;
	font-size: 12px;
	background: #d9d9d9;
	color: #777;
	border-radius: 10px;
	border: none;
	height: 30px;
	float: left;
	font-weight: 400;
	cursor: pointer;
	&.success {
		background: #222;
		color: white;
	}
`;

const ReviewWriteRatingLabel = styled.p`
	margin: 0 10px 0 0;
	line-height: 30px;
	float: left;
	font-size: 15px;
	text-align: left;
	`;

const ReviewWriteAnonymousLabel = styled.p`
	margin: 0 10px 0 0;
	line-height: 30px;
	float: left;
	font-size: 15px;
`;

const ReviewWriteNameCheckbox = styled.input`
	float: left;
	width: 16px;
	height: 16px;
	margin: 1px 2px 0 0;
	position: relative;
	top: 50%;
	transform: translateY(-50%);
`;

const ReviewPreviewImage = styled.img`
	max-width: 220px;
	height: 42.5px;
	border-radius: 5px;
	float: left;
	margin-top: 5px;
	border: dashed 1px #e5e5e5;
`;
const ReviewImageDelete = styled.div`
	width: 18px;
	height: 18px;
	padding: 0;
	color: #fff;
	position: absolute;
	top: -2px;
	right: -7px;
	background: #ccc;
	cursor: pointer;
	border-radius: 20px;
	& > span {
		width: 100%;
		text-align: center;
		line-height: 18px;
		font-size: 14px;
	}
`;

const NotLogin = styled.div`
	position: absolute;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	background-color: rgba(20, 20, 20, 0.3);
	z-index: 6;
	border-radius: 20px;
	text-align: center;
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: 20px;
	text-decoration: underline;
`;

const GoToLogin = styled.span`
	cursor: pointer;

	:hover {
		color: red;
		opacity: 0.7;
	}
`;

const ReviewWriteForm = ({ restaurantName, deptName }) => {
	const [starVal, setStarVal] = useState(0);
	const [anonymous, setAnonymous] = useState(false);
	const [comment, setComment] = useState("");
	const [selectedMenu, setSelectedMenu] = useState("");
	const [image, setImage] = useState();
	const [imageURL, setImageURL] = useState("");
	const imageRef = useRef(null);
	const navigator = useNavigate();

	const token = localStorage.getItem("token")
		? localStorage.getItem("token")
		: "";

	const onChangeImage = (e) => {
		const reader = new FileReader();
		if (e.target.files[0]) {
			reader.readAsDataURL(e.target.files[0]);
			setImage(e.target.files[0]);
		}

		reader.onloadend = (e) => {
			setImageURL(e.target.result);
		};
	};

	const deleteImage = () => {
		setImage(null);
		setImageURL("");
		imageRef.current.value = null;
	};

	const handleSelectMenu = (value) => {
		setSelectedMenu(value);
	};

	const ReviewSubmit = async (e) => {
		e.preventDefault();

		const formdata = new FormData();
		formdata.append("restaurant", restaurantName);
		formdata.append("dept", deptName);
		// 1학 부분을 위해 selectedMenu 넣은건데 확인 필요합니다.
		formdata.append("menuName", restaurantName === 1 ? selectedMenu : "");
		formdata.append("rate", starVal);
		formdata.append("writer", token);
		formdata.append("anonymous", anonymous);
		formdata.append("comment", comment);
		formdata.append("image", image);

		// formdata 확인
		let entries = formdata.entries();
		for (const pair of entries) {
			console.log(pair[0] + ": " + pair[1]);
		}

		axios
			.post(config.DEPLOYMENT_BASE_URL + "/review", formdata, {
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
			{
				// 로그인 안했을 때
				!token &&
					<NotLogin>
						<GoToLogin onClick={() => { navigator("/LoginPage")}}>로그인이 필요합니다 !!</GoToLogin>
					</NotLogin>
			}
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
				<div style={{ float: "right", height: 30 }}>
					<ReviewWriteAnonymousLabel>익명</ReviewWriteAnonymousLabel>
					<ReviewWriteNameCheckbox
						type="checkbox"
						onChange={() => {
							setAnonymous(!anonymous);
						}}
					/>
				</div>
			</div>

			{restaurantName === 1 ? (
				<MenuSelector onSelectMenu={handleSelectMenu} />
			) : null}

			<div style={{ width: "100%", float: "left" }}>
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
						ref={imageRef}
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
						{imageURL ? (
							<div
								className="PrevWrapper"
								style={{ float: "left", position: "relative" }}
							>
								<ReviewPreviewImage src={imageURL} />
								<ReviewImageDelete onClick={deleteImage}>
									<span className="material-symbols-outlined">close</span>
								</ReviewImageDelete>
							</div>
						) : null}
					</ReviewWriteInputWrapper>
				</div>
				{starVal !== 0 ? (
					<ReviewWriteButton className="success" onClick={ReviewSubmit}>
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
		<div style={{ width: "100%", float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 18, margin: 0, textAlign: "left" }}>
				메뉴 리뷰 남기기
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
