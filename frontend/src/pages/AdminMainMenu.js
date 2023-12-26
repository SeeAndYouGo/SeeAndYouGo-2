import React, { useEffect, useState } from "react";
import Navigation from "../components/Navigation";
import * as config from "../config";

const AdminMainMenu = () => {
	const initialArray = Array(30).fill(null);
	const [menuList, setMenuList] = useState([]);
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
			setMenuList(data);
		});
	}, []);

	const handleChange = (
		selectedRestaurantName,
		selectedDept,
		selectedDateTime,
		e
	) => {
		setMainResult((prevArray) => {
			const tempArray = [...prevArray];
			const tempObject = {
				mainDishName: e.target.value,
				restaurantName: selectedRestaurantName,
				dept: selectedDept,
				date: selectedDateTime,
			};

			tempArray[e.target.name] = tempObject;
			return tempArray;
		});
	};

	const handleSubmit = () => {
		const nullCount = mainResult.filter((val) => val === null).length;

		if (menuList.length === mainResult.length - nullCount) {
			// 여기에 PUT으로 전송하도록 합니다.
			const url = config.DEPLOYMENT_BASE_URL + "/mainMenu";
			fetch(url, {
				method: "PUT",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(mainResult),
			})
				.then((res) => res.json())
				.then((data) => console.log(data));

			alert("전송 성공");
			window.location.replace("/");
		} else {
			alert("라디오 버튼을 모두 선택해주세요.");
		}
	};

	return (
		<>
			<div className="AdminPage">
				<div style={{ textAlign: "center", marginTop: "20px" }}>
					<span>비밀 주소입니다. 어떻게 오셨죠?</span>
				</div>
				{menuList.map((val, index) => {
					return val.dishList[0] === null ? null : (
						<div key={index}>
							<p>
								{val.date} / {val.restaurantName} / {val.dept}
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
			</div>
			<button
				type="confirm"
				onClick={() => {
					if (window.confirm("제출하시겠습니까?")) {
						handleSubmit();
						console.log(mainResult);
					}
				}}
				style={{ float: "right" }}
			>
				제출하기
			</button>
			<div style={{ height: "100px" }}></div>
			<Navigation />
		</>
	);
};

export default AdminMainMenu;
