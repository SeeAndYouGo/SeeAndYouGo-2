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

	// 백엔드로부터 가져온 데이터
	const [menuList, setMenuList] = useState([]);
	// 메인 메뉴를 저장하는 배열
	const initialArray = Array(30).fill(null);
	const [mainResult, setMainResult] = useState(initialArray);
	// 바뀐 index를 임시 저장하는 배열
	const [changedIndex, setChangedIndex] = useState([]);

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
			setMenuList(data);

			data.map((val, index) =>
				setMainResult((prevArray) => {
					const tempArray = [...prevArray];
					const tempObject = {
						mainDishName: val.mainDishName,
						subDishList: val.subDishList,
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

	const handleChange = (selectedData, e) => {
		setChangedIndex((prevArray) => {
			if (prevArray.includes(e.target.name)) {
				return prevArray;
			} else {
				const tempArray = [...prevArray];
				tempArray.push(e.target.name);
				return tempArray;
			}
		});

		const { restaurantName, dept, date } = selectedData;
		setMainResult((prevArray) => {
			if (e.target.value === prevArray[e.target.name].mainDishName) {
				return prevArray;
			}
			const tempArray = [...prevArray];
			if (prevArray[e.target.name].mainDishName === "") {
				// 처음으로 버튼 클릭한 경우
				const tempSubDishList = [
					...prevArray[e.target.name].subDishList,
				].filter((val) => val !== e.target.value);
				const tempObject = {
					mainDishName: e.target.value,
					subDishList: tempSubDishList,
					restaurantName: restaurantName,
					dept: dept,
					date: date,
				};
				tempArray[e.target.name] = tempObject;
			} else {
				// 라디오 버튼 변경한 경우
				const selectedIndex = prevArray[e.target.name].subDishList.indexOf(
					e.target.value
				);
				const tempSubDishList = [...prevArray[e.target.name].subDishList];
				tempSubDishList[selectedIndex] = prevArray[e.target.name].mainDishName;

				const tempObject = {
					mainDishName: e.target.value,
					subDishList: tempSubDishList,
					restaurantName: restaurantName,
					dept: dept,
					date: date,
				};
				tempArray[e.target.name] = tempObject;
			}

			return tempArray;
		});
	};

	const handleSubmit = () => {
		// 전송할 데이터 정리
		const sendData = [];
		changedIndex.map((val) => sendData.push(mainResult[val]));
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
				setChangedIndex([]);
			})
			.catch((err) => {
				console.log(err);
				alert("전송 실패");
			});
	};

	return (
		<>
			{!isAdmin ? (
				<div style={{ margin: "80px auto", width: "360px" }}>
					<label>
						비밀번호:&nbsp;
						<input
							type="password"
							value={password}
							onChange={handlePasswordChange}
							onKeyDown={handleKeyPress}
						/>
					</label>
					<SubmitButton onClick={handleAdminLogin}>로그인</SubmitButton>
				</div>
			) : (
				<div className="AdminPage">
					<div style={{ textAlign: "center", marginTop: 70 }}>
						<p>비밀 주소입니다. 어떻게 오셨죠?</p>
						<p>메인 메뉴가 선정되어 있는 부분은 배경색이 표시!!</p>
						<p>한 번 전송하면 선택했던 데이터는 초기화</p>
						<p>But, 화면에는 선택 표기가 남아있습니다..</p>
					</div>
					{menuList.map((val, index) => {
						return val.subDishList.length === 0 &&
							val.mainDishName === "" ? null : (
							<div key={index} style={{ marginLeft: 15 }}>
								<p>
									{val.date} / {val.restaurantName} /{" "}
									{val.dept === "STAFF" ? "교직원" : "학생"}
								</p>
								{val.mainDishName !== "" ? (
									<label style={{ backgroundColor: "#e9bd15" }}>
										<input
											type="radio"
											name={index}
											value={val.mainDishName}
											id={val.mainDishName}
											onChange={(e) => handleChange(val, e)}
										/>
										{val.mainDishName}
									</label>
								) : null}
								{val.subDishList.map((val2, index2) => {
									return (
										<div key={index2}>
											<label>
												<input
													type="radio"
													name={index}
													value={val2}
													id={val2}
													onChange={(e) => handleChange(val, e)}
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
