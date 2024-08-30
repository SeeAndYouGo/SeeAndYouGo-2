import React, { useEffect, useState } from "react";
import MenuItem from "./MenuItem";
import DeptTabMenu from "./DeptTabMenu";
import { useSelector, useDispatch } from "react-redux";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";
import * as config from "../../config";

const todayMenuStyle = {
	display: "flex",
	fontSize: 18,
	margin: "0px 0px 4px",
};

const menuTypeValue = ["BREAKFAST", "LUNCH", "DINNER"];

const TodayMenu = () => {
	const dispatch = useDispatch();
	const [staffMenu, setStaffMenu] = useState([]);
	const [studentMenu, setStudentMenu] = useState([]);
	const nowDept = useSelector((state) => state.dept).value;

	const [selectedDiv, setSelectedDiv] = useState({ 1: 0, 2: 0 });

	const handleDivClick = (tab, index, menuList, id) => {
		setSelectedDiv((prevSelectedDiv) => ({
			...prevSelectedDiv,
			[tab]: index,
		}));
		dispatch(changeMenuType(index));
		dispatch(changeMenuInfo({mainMenuList: menuList, menuId: id}));
	};

	const nowSelectedStyle = (index) => {
		return {
			outline: selectedDiv[nowDept] === index ? "3px solid black" : "none",
			margin: 0,
			padding: 0,
			cursor: "pointer",
		};
	};

	useEffect(() => {
		setSelectedDiv((prevSelectedDiv) => ({
			...prevSelectedDiv,
			[nowDept]: 0,
		}));
	}, [nowDept]);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/daily-menu/restaurant2` +
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
		fetchData()
			.then((data) => {
				const staffMenuData = data.filter((item) => item.dept === "STAFF");
				staffMenuData.sort((a, b) => {
					return (
						menuTypeValue.indexOf(a.menuType) -
						menuTypeValue.indexOf(b.menuType)
					);
				});
				const studentMenuData = data.filter((item) => item.dept !== "STAFF");
				studentMenuData.sort((a, b) => {
					return (
						menuTypeValue.indexOf(a.menuType) -
						menuTypeValue.indexOf(b.menuType)
					);
				});

				setStaffMenu(staffMenuData);
				setStudentMenu(studentMenuData);

				if (nowDept === 1 && studentMenuData.length > 0) {
					dispatch(changeMenuInfo({mainMenuList: studentMenuData[0].mainDishList, menuId: studentMenuData[0].menuId}));
				} else if (nowDept === 2 && staffMenuData.length > 0) {
					dispatch(changeMenuInfo({mainMenuList: staffMenuData[0].mainDishList, menuId: staffMenuData[0].menuId}));
				}
			})
			.catch((err) => {
				console.log(err);
			});
	}, [dispatch, nowDept]);

	return (
		<div>
			<div style={{ display: "flex", marginBottom: "15px" }}>
				<div style={todayMenuStyle}>오늘의 메뉴</div>
				<DeptTabMenu studentMenu={studentMenu} staffMenu={staffMenu} />
			</div>
			{nowDept === 1
				? studentMenu.map((item, index) => {
						return (
							<div
								style={nowSelectedStyle(index)}
								key={index}
								onClick={() => {
									handleDivClick(nowDept, index, item.mainDishList, item.menuId);
								}}
							>
								<MenuItem menu={item} />
							</div>
						);
					})
				: staffMenu.map((item, index) => {
						return (
							<div
								style={nowSelectedStyle(index)}
								key={index}
								onClick={() => {
									handleDivClick(nowDept, index, item.mainDishList, item.menuId);
								}}
							>
								<MenuItem menu={item} />
							</div>
						);
				  })}
		</div>
	);
};

export default TodayMenu;
