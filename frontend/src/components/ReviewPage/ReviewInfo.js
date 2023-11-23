import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import ReviewList from "./ReviewList";

const MyRadio = styled.input`
	margin-left: 10px;
	accent-color: black;
	-ms-transform: scale(1.5) /* IE 9 */;
	-webkit-transform: scale(1.5) /* Chrome, Safari, Opera */;
	transform: scale(1.5);
`;

const MenuInfo = ({ mainMenu, subMenu }) => {
	const subMenuString = subMenu.join(", ");
	return (
		<>
			<p
				style={{
					fontSize: 12,
					margin: "0px 0px 4px 0px",
					color: "#555",
				}}
			>
				오늘의 메뉴
			</p>
			<p style={{ fontSize: 18, margin: 0 }}>{mainMenu}</p>
			<p
				style={{
					fontSize: 12,
					margin: 0,
					fontWeight: 400,
					color: "#777",
				}}
			>
				{subMenuString}
			</p>
		</>
	);
};

const ReviewMenuInfo = ({ idx }) => {
	const [menuData, setMenuData] = useState([]);
	const [radioValue, setRadioValue] = useState("menu1");

	const initialSetting = () => {
		setRadioValue("menu1");
	};

	useEffect(() => {
		const fetchData = async () => {
			// const nowUrl = `/api/dailyMenu/restaurant${idx}`;
			// const nowUrl = `http://27.96.131.182/api/dailyMenu/restaurant${idx}`;
			const nowUrl = "/assets/json/myMenu.json";
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
			setMenuData(data);
			initialSetting();
		});
	}, [idx]);

	const handleRadioChange = (event) => {
		const nowType = event.target.value;
		setRadioValue(nowType);
	};

	return (
		<>
			<div>
				{idx === 2 || idx === 3 ? (
					<>
						<MyRadio
							type="radio"
							name="menu"
							value="menu1"
							id="menu1"
							checked={radioValue === "menu1"}
							onChange={handleRadioChange}
						/>
						<label htmlFor="menu1" style={{ padding: "0px 5px" }}>
							학생식당
						</label>
						<MyRadio
							style={{ accentColor: "black" }}
							type="radio"
							name="menu"
							id="menu2"
							value="menu2"
							checked={radioValue === "menu2"}
							onChange={handleRadioChange}
						/>
						<label htmlFor="menu2" style={{ padding: "0px 5px" }}>
							교직원식당
						</label>
					</>
				) : null}

				<div
					style={{
						margin: "10px 0px",
						padding: "15px",
						borderRadius: "10px",
						width: "100%",
						backgroundColor: "white",
					}}
				>
					{menuData.map((nowValue, index) => {
						if (radioValue === "menu1") {
							return index === 1 ? null : (
								<div key={index}>
									<MenuInfo
										mainMenu={nowValue.dishList[0]}
										subMenu={nowValue.dishList.slice(1)}
									/>
								</div>
							);
						} else {
							return index === 0 ? null : (
								<MenuInfo
									mainMenu={nowValue.dishList[0]}
									subMenu={nowValue.dishList.slice(1)}
									key={index}
								/>
							);
						}
					})}
				</div>
				{<ReviewList idx={idx} nowDept={radioValue === "menu1" ? "STUDENT":"STAFF"}/>}
			</div>
		</>
	);
};

export default ReviewMenuInfo;
