import React, { useRef, useState } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import axios from "axios";
import moment from "moment";
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

const WriteImpossible = styled.div`
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
	cursor: default;
`;

const GoToLogin = styled.span`
	cursor: pointer;
	:hover {
		color: red;
		opacity: 0.7;
	}
`;

const ReviewWriteForm = ({ restaurantNum, deptNum }) => {
	const [starVal, setStarVal] = useState(0);
	const [anonymous, setAnonymous] = useState(false);
	const [comment, setComment] = useState("");
	const [selectedMenu, setSelectedMenu] = useState("");
	const [image, setImage] = useState();
	const [imageURL, setImageURL] = useState("");
	const imageRef = useRef(null);
	const navigator = useNavigate();
	const dispatch = useDispatch();

	const token = useSelector((state) => state.user.value.token);

	const todayDate = moment().toDate(); // 현재 날짜 utc 시간
	const myValue = 9; // 한국 시간으로 변환하기 위한 값, local에서 실행하는 시간으로 조정하고 싶으면 이 값을 0으로 조정하면 됩니다.
	const localDateValue = moment(todayDate).add(myValue, 'hours').format('LLLL'); // 현재 날짜 한국 시간으로 변환
	const todayDay = moment(new Date(localDateValue)).format("dddd"); // 현재 요일
	const isWeekend = todayDay === "Saturday" || todayDay === "Sunday"; // 주말인지 확인

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

		formdata.append("restaurant", restaurantNum);
		formdata.append("dept", deptNum === 1 ? "STUDENT" : "STAFF");
		formdata.append("menuName", restaurantNum === 1 ? selectedMenu : "");
		formdata.append("rate", starVal);
		formdata.append("writer", token);
		formdata.append("anonymous", anonymous);
		formdata.append("comment", comment);
		formdata.append("image", image);

		axios
			.post(config.DEPLOYMENT_BASE_URL + "/review", formdata, {
				headers: {
					"Content-Type": "multipart/form-data",
				},
			})
			.then(() => { // 리뷰 작성 성공
				dispatch(showToast({ contents: "review", toastIndex: 0 }));
				window.location.reload();
			})
			.catch(() => { // 리뷰 작성 실패
				dispatch(showToast({ contents: "review", toastIndex: 1 }));
			})
	};

	return (
		<ReviewWriteContainer>
			{isWeekend ? ( // 주말인 경우
				<WriteImpossible>주말에는 작성할 수 없습니다.</WriteImpossible>
			) : !token ? ( // 로그인 안한 경우
				<WriteImpossible>
					<GoToLogin
						onClick={() => {
							navigator("/login-page");
						}}
					>
						로그인이 필요합니다 !!
					</GoToLogin>
				</WriteImpossible>
			) : null}
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

			{restaurantNum === 1 ? (
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

const ReviewWrite = ({ restaurantNum, deptNum }) => {
	return (
		<div style={{ width: "100%", float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 18, margin: 0, textAlign: "left" }}>
				메뉴 리뷰 남기기
			</p>
			<ReviewWriteForm restaurantNum={restaurantNum} deptNum={deptNum} />
		</div>
	);
};

export default ReviewWrite;
