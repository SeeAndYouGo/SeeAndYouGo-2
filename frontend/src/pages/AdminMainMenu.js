import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import * as config from "../config";
import Toast from "../components/Toast";

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

const AdminMainMenu = () => {
	const [isAdmin, setIsAdmin] = useState(false);
	const [password, setPassword] = useState("");
	const [toast, setToast] = useState(false);

	const handlePasswordChange = (e) => {
		setPassword(e.target.value);
	};

	const handleAdminLogin = () => {
		if (password === process.env.REACT_APP_ADMIN_PASSWORD) {
			setIsAdmin(true);
		} else {
			setToast(true);
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
				"/weeklyMenu" +
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
			// 일단 index 0번의 값으로 mainResult를 초기화
			data.map((val, index) =>
				setMainResult((prevArray) => {
					const tempArray = [...prevArray];
					const tempObject = {
						mainDishName: val.dishList[0],
						subDishList: val.dishList.slice(1),
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
			const selectedIndex = prevArray[e.target.name].subDishList.indexOf(
				e.target.value
			);
			const tempsubDishList = [...prevArray[e.target.name].subDishList];
			tempsubDishList[selectedIndex] =
				prevArray[e.target.name].mainDishName;

			const tempArray = [...prevArray];
			const tempObject = {
				mainDishName: e.target.value,
				subDishList: tempsubDishList,
				restaurantName: selectedRestaurantName,
				dept: selectedDept,
				date: selectedDateTime,
			};

			tempArray[e.target.name] = tempObject;
			return tempArray;
		});
	};

	const handleSubmit = () => {
		// 전송할 데이터 정리
		const sendData = [];
		mainResult.map((val) =>
			val.mainDishName !== undefined ? sendData.push(val) : null
		);
		console.log("전송 데이터 확인", sendData);

		const url = config.DEPLOYMENT_BASE_URL + "/mainMenu";
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
				<div style={{ marginTop: 70 }}>
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
			{toast ? (
				<Toast
					message="비밀번호가 틀렸습니다."
					type="error"
					setToast={setToast}
				/>
			) : null}
		</>
	);
};

export default AdminMainMenu;
