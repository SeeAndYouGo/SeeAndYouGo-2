import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { showToast } from "../../redux/slice/ToastSlice";
import { get, put } from "../../api/index";

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
	const [buttonDisabled, setButtonDisabled] = useState(false);
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

	useEffect(() => {
		const fetchData = async () => {
			const response = await get("/weekly-menu");
			const result = response.data;
			console.log(result, "가져온 데이터 확인");
			return result;
		};
		fetchData().then((data) => {
			setMenuList(data);
		});
	}, []);

	const handleCheckboxChange = (menuListIndex, selectedMenu) => {
		const nowMenu = menuList[menuListIndex];

		if (nowMenu.mainDishList.includes(selectedMenu)) {
			nowMenu.mainDishList = nowMenu.mainDishList.filter(
				(val) => val !== selectedMenu
			);
			nowMenu.sideDishList.push(selectedMenu);
		} else {
			nowMenu.sideDishList = nowMenu.sideDishList.filter(
				(val) => val !== selectedMenu
			);
			nowMenu.mainDishList.push(selectedMenu);
		}

		setMenuList((prev) => {
			return prev.map((val, idx) => {
				if (idx === menuListIndex) {
					return nowMenu;
				} else {
					return val;
				}
			});
		});
	};

	const handleSubmit = async () => {
		if (buttonDisabled) return;
		setButtonDisabled(true);
		
		const jsonData = JSON.stringify(menuList);
		await put("/main-menu", jsonData)
			.then(() => {
				alert("전송 성공");
			})
			.catch((err) => {
				console.log(err);
				alert("전송 실패");
			}).finally(() => {
				setButtonDisabled(false);
			});
	};

	return (
		<>
			{
				!isAdmin ? (
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
						<div style={{ textAlign: "center", margin: "60px 0 20px" }}>
							<p>비밀 주소입니다. 어떻게 오셨죠?</p>
							<p>메인 메뉴가 선정되어 있는 부분은 배경색이 표시!!</p>
							<p>한 번 전송하면 선택했던 데이터는 초기화</p>
							<p>But, 화면에는 선택 표기가 남아있습니다..</p>
						</div>

						{menuList.map((val1, idx1) => {
							return (
								<div key={idx1} style={{ marginBottom: 10 }}>
									<span>
										{val1.date} {val1.restaurantName}
									</span>
									<p style={{ backgroundColor: "#e3a1b1" }}>{val1.menuId}</p>
									{val1.mainDishList.map((val2, idx2) => {
										return (
											<div key={idx2}>
												<label>
													<input
														type="checkbox"
														checked={menuList[idx1].mainDishList.includes(val2)}
														onChange={() => handleCheckboxChange(idx1, val2)}
													/>
													{val2}
												</label>
											</div>
										);
									})}
									{val1.sideDishList.map((val2, idx2) => {
										return (
											<div key={idx2}>
												<label>
													<input
														type="checkbox"
														checked={menuList[idx1].mainDishList.includes(val2)}
														onChange={() => handleCheckboxChange(idx1, val2)}
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
							disabled={buttonDisabled}
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
				)
			}
		</>
	);
};

export default SetMainMenuPage;
