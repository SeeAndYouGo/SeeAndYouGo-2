import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import * as config from "../../config";

const SubmitButton = styled.button`
	background: white;
	border: 2px solid #ddd;
	border-radius: 10px;
	font-size: 20px;
	cursor: pointer;
	position: sticky;
	float: right;
	bottom: 30px;
	font-weight: 500;
`;

const SetMainMenuPage = () => {
	const [isAdmin, setIsAdmin] = useState(false);
	const [password, setPassword] = useState("");
	const dispatch = useDispatch();

	const handlePasswordChange = (e) => {
		setPassword(e.target.value);
	};

	const handleAdminLogin = () => {
		if (password === process.env.REACT_APP_ADMIN_PASSWORD) {
			setIsAdmin(true);
		} else {
			dispatch(showToast({ contents: "admin", toastIndex: 0 }));
		}
	};

	const handleKeyPress = (e) => {
		if (e.key === "Enter") {
			handleAdminLogin();
		}
	};

	const initialArray = Array(30).fill(null);
	// 백엔드로부터 가져온 데이터
	const [menuList, setMenuList] = useState([]);
	// 메인 메뉴를 저장하는 배열
	const [mainResult, setMainResult] = useState(initialArray);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				"/weekly-menu" +
				(config.NOW_STATUS === 0 ? ".json" : "");

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			console.log("가져온 데이터 확인", data);
			// menuList에 저장
			setMenuList(data);

			data.map((val, index) =>
				setMainResult((prevArray) => {
					const tempArray = [...prevArray];
					const tempObject = {
						subDishList: val.dishList,
						restaurantName: val.restaurantName,
						dept: val.dept,
						date: val.date,
					};

					tempArray[index] = tempObject;
					return tempArray;
				})
			);
		});
	}, []);

	const handleChange = (
		selectedRestaurantName,
		selectedDept,
		selectedDateTime,
		e
	) => {
		setMainResult((prevArray) => {
			if (e.target.value === prevArray[e.target.name].mainDishName) {
				return prevArray;
			}

			const tempArray = [...prevArray];
			if (prevArray[e.target.name].mainDishName === undefined) {
				// 처음으로 버튼 클릭한 경우
				const tempSubDishList = [
					...prevArray[e.target.name].subDishList,
				].filter((val) => val !== e.target.value);
				const tempObject = {
					mainDishName: e.target.value,
					subDishList: tempSubDishList,
					restaurantName: selectedRestaurantName,
					dept: selectedDept,
					date: selectedDateTime,
				};
				tempArray[e.target.name] = tempObject;
			} else {
				// 라디오 버튼 변경한 경우
				const selectedIndex = prevArray[
					e.target.name
				].subDishList.indexOf(e.target.value);
				const tempSubDishList = [
					...prevArray[e.target.name].subDishList,
				];
				tempSubDishList[selectedIndex] =
					prevArray[e.target.name].mainDishName;

				const tempObject = {
					mainDishName: e.target.value,
					subDishList: tempSubDishList,
					restaurantName: selectedRestaurantName,
					dept: selectedDept,
					date: selectedDateTime,
				};
				tempArray[e.target.name] = tempObject;
			}

			return tempArray;
		});
	};

	const handleSubmit = () => {
		// 전송할 데이터 정리
		const sendData = [];
		mainResult.map((val) =>
			val?.mainDishName !== undefined ? sendData.push(val) : null
		);
		console.log("전송 데이터 확인", sendData);

		const url = config.DEPLOYMENT_BASE_URL + "/main-menu";
		fetch(url, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(sendData),
		})
			.then((res) => res.json())
			.then(() => {
				alert("전송 성공");
			})
			.catch((err) => {
				console.log(err);
				alert("전송 실패");
			});
	};

	return (
		<>
			{!isAdmin ? (
				<div style={{ margin: "80px 5px 0 5px" }}>
					<label>
						비밀번호:&nbsp;
						<input
							type="password"
							value={password}
							onChange={handlePasswordChange}
							onKeyDown={handleKeyPress}
						/>
					</label>
					<SubmitButton onClick={handleAdminLogin}>
						로그인
					</SubmitButton>
				</div>
			) : (
				<div className="AdminPage">
					<div style={{ textAlign: "center", marginTop: 70 }}>
						<span>비밀 주소입니다. 어떻게 오셨죠?</span>
					</div>
					{menuList.map((val, index) => {
						return val.dishList.length === 0 ? null : (
							<div key={index}>
								<p>
									{val.date} / {val.restaurantName} /{" "}
									{val.dept === "STAFF" ? "교직원" : "학생"}
								</p>
								{val.dishList.map((val2, index2) => {
									return (
										<div key={index2}>
											<label>
												<input
													type="radio"
													name={index}
													value={val2}
													id={val2}
													onChange={(e) =>
														handleChange(
															val.restaurantName,
															val.dept,
															val.date,
															e
														)
													}
												/>
												{val2}
											</label>
										</div>
									);
								})}
							</div>
						);
					})}
					<SubmitButton
						type="confirm"
						onClick={() => {
							if (window.confirm("제출하시겠습니까?")) {
								handleSubmit();
							}
						}}
					>
						전송
					</SubmitButton>
					<div style={{ height: "100px" }}></div>
				</div>
			)}
		</>
	);
};

export default SetMainMenuPage;
