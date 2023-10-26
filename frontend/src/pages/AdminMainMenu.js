import React, { useEffect, useState } from "react";

const AdminMainMenu = () => {
	const initialArray = Array(30).fill(null);
	const [menuList, setMenuList] = useState([]);
	const [mainResult, setMainResult] = useState(initialArray);
	const [nullLength, setNullLength] = useState(0);

	useEffect(() => {
		const fetchData = async () => {
			// const nowUrl = "/api/get_menu_list";
			const nowUrl = "/assets/json/tempMenuList.json";
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
			data.map((val, _) =>
				val.menu[0] === null ? setNullLength((n) => n + 1) : null
			);

			setMenuList(data);
		});
	}, []);

	const handleChange = (selectedRestaurantName, selectedDateTime, e) => {
		setMainResult((prevArray) => {
			const tempArray = [...prevArray];
			const tempObject = {
				menu: e.target.value,
				restaurantName: selectedRestaurantName,
				dateTime: selectedDateTime,
			};

			tempArray[e.target.name] = tempObject;
			return tempArray;
		});
	};

	const handleSubmit = (e) => {
		e.preventDefault();

		const nullCount = mainResult.filter((val) => val === null).length;

		if (nullCount === nullLength) {
			console.log("전송 가능 상태입니다.");
			// 여기에 POST로 전송하도록 합니다.
		} else {
			console.log("전송 불가능 상태입니다.");
		}
		console.log("결과확인합니다.", mainResult);
	};

	return (
		<>
			<div className="AdminPage">
				<p style={{ textAlign: "center" }}>
					비밀 주소입니다. 어떻게 오셨죠?
				</p>
				<button onClick={handleSubmit}>결과확인</button>
				{menuList.map((val, index) => {
					return val.menu[0] === null ? null : (
						<div key={index}>
							<p>
								{val.restaurantName} / {val.dateTime}
							</p>
							{val.menu.map((val2, index2) => {
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
														val.dateTime,
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
		</>
	);
};

export default AdminMainMenu;
