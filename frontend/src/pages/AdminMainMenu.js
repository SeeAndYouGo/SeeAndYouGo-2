import React, { useEffect, useState } from "react";
import Navigation from "../components/Navigation";

const AdminMainMenu = () => {
	const initialArray = Array(30).fill(null);
	const [menuList, setMenuList] = useState([]);
	const [mainResult, setMainResult] = useState(initialArray);
	const [nullLength, setNullLength] = useState(0);

	useEffect(() => {
		const fetchData = async () => {
			const nowUrl = "http://localhost:8080/api/weeklyMenu";
			// const nowUrl = "/assets/json/tempMenuList.json";
			const res = await fetch(nowUrl, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			setNullLength(0);
			data.map((val, _) =>
				val.dishList[0] === null ? setNullLength((n) => n + 1) : null
			);

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

	const handleSubmit = (e) => {
		e.preventDefault();

		const nullCount = mainResult.filter((val) => val === null).length;

		console.log("count", nullCount);
		console.log("length", nullLength);

		if (nullCount === nullLength) {
			console.log("전송 가능 상태입니다.");
			// 여기에 POST로 전송하도록 합니다.
			const nowUrl = "http://localhost:8080/api/mainMenu";
			fetch(nowUrl, {
				method: "PUT",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(mainResult),
			}).then((res) => res.json())
			.then((data) => console.log(data));

			alert("전송 성공");
		} else {
			alert("라디오 버튼을 모두 선택해주세요.")
			console.log("전송 불가능 상태입니다.");
		}
		console.log("결과확인합니다.", mainResult);
	};

	return (
		<>
			<div className="AdminPage">
				<div style={{ textAlign: "center", marginTop: "20px" }}>
					<span>
						비밀 주소입니다. 어떻게 오셨죠?
					</span>
					<button onClick={handleSubmit} style={{ float: "right" }}>
						제출하기
					</button>
				</div>
				{menuList.map((val, index) => {
					return val.dishList[0] === null ? null : (
						<div key={index}>
							<p>
								{val.date} / {val.restaurantName} /{" "}
								{val.dept}
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
			<Navigation />
		</>
	);
};

export default AdminMainMenu;
