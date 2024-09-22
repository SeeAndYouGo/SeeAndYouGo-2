import React, { useEffect, useRef, useState } from "react";
import styled from "@emotion/styled";
import { useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import axios from "axios";
import moment from "moment";
import StarsRating from "react-star-rate";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCamera } from "@fortawesome/free-solid-svg-icons";
import MenuSelector from "../RestaurantDetailPage/MenuSelector";
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
	height: 40px;
	width: 100%;
  display: flex;
  margin-top: 5px;
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
	height: 40px;
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
	line-height: 40px;
	cursor: pointer;
	border-radius: 5px;
	float: right;
  margin: 0;
	padding: 0 10px;
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
	height: 70px;
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

const MenuName = styled.p`
  font-size: 12px;
  margin: 5px 0 0 0;
  font-weight: 500;
  float: left;
  border: 1px solid #ccc;
  padding: 2px 10px;
  border-radius: 20px;
	margin-right: 5px;
`;

const ReviewWrite = ({ restaurantNum, deptNum, menuInfo }) => {
	const [starVal, setStarVal] = useState(0);
	const [anonymous, setAnonymous] = useState(false);
	const [comment, setComment] = useState("");
	const [selectedMenu, setSelectedMenu] = useState({});
	const [image, setImage] = useState();
	const [imageURL, setImageURL] = useState("");
	const imageRef = useRef(null);
	const navigator = useNavigate();
	const dispatch = useDispatch();

	const token = useSelector((state) => state.user.value.token);
	const nowMainMenuList = useSelector((state) => state.nowMenuInfo.value).mainMenuList;
	const nowMenuId = useSelector((state) => state.nowMenuInfo.value).menuId;

	const todayDate = moment().toDate(); 
	const todayDay = moment(new Date(todayDate)).format("dddd"); // 현재 요일
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

		let selectedMenuId = 0;

		if (restaurantNum === 1) {
			for (let key in menuInfo) {
				if (key === selectedMenu.value) {
					selectedMenuId = menuInfo[key];
					break;
				}
			}
		}

		const dto = {
			restaurant: restaurantNum,
			dept: restaurantNum === 1 ? selectedMenu.category : (deptNum === 1 ? "STUDENT" : "STAFF"),
			menuName: restaurantNum === 1 ? selectedMenu.value : "",
			rate: starVal,
			writer: token,
			anonymous: anonymous,
			comment: comment,
			menuId: restaurantNum === 1 ? selectedMenuId : nowMenuId,
		};

		formdata.append("image", image);
		formdata.append(
			"dto", new Blob([JSON.stringify(dto)], { type: 'application/json' })
		);

		axios
			.post(config.DEPLOYMENT_BASE_URL + "/review", formdata, {
				headers: {
					"Content-Type": "multipart/form-data",
				},
			})
			.then(() => { // 리뷰 작성 성공
				dispatch(showToast({ contents: "review", toastIndex: 0 }));
				setTimeout(() => {
					window.location.reload();
				}, 2000);
			})
			.catch(() => { // 리뷰 작성 실패
				dispatch(showToast({ contents: "review", toastIndex: 1 }));
				console.log(dto, "리뷰 전달 확인");
			})
	};

	return (
		<ReviewWriteContainer>
			{
				// isWeekend ? ( // 주말인 경우
				false ? ( // TODO 임시 처리용 << 수정 필요
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
					</ReviewWriteInputWrapper>
					<div style={{ width: "100%", float: "left" }}>
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
					</div>
					<div>
					{
						restaurantNum === 1 ? null :
							(nowMainMenuList?.length > 0) && nowMainMenuList.map((dish, index) => (
								<MenuName key={index}>{dish}</MenuName>
							))
					}
					</div>
				</div>
				{(starVal !== 0 && (restaurantNum === 1 ? selectedMenu.value : true))  ? (
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

const ReviewWriteForm = ({ restaurantNum, deptNum, menuInfoForRestaurant1 }) => {
	return (
		<div style={{ width: "100%", float: "left", marginTop: 20 }}>
			<p style={{ fontSize: 22, margin: 0, textAlign: "left", fontWeight: 700 }}>
				메뉴 리뷰 남기기
			</p>
			<ReviewWrite restaurantNum={restaurantNum} deptNum={deptNum} menuInfo={menuInfoForRestaurant1}/>
		</div>
	);
};

export default ReviewWriteForm;
