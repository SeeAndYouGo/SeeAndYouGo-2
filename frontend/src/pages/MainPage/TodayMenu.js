import React, { useEffect, useState } from "react";
import MenuItem from "./MenuItem";
import TypeTabMenu from "./TypeTabMenu";
import { useSelector, useDispatch } from "react-redux";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";
import { changeDept } from "../../redux/slice/DeptSlice";
import styled from "@emotion/styled";

const todayMenuStyle = {
	display: "flex",
	fontSize: 22,
	fontWeight: 700,
	margin: "0px 0px 4px",
};

const SelectedDiv = styled.div`
	margin: 0;
	padding: 0;
	cursor: pointer;
	border-radius: 10px;
	
	${({ $active }) => $active && `
		outline: 2px solid black;
	`}
`;

const deptValue = ["STUDENT", "STAFF"];

const TodayMenu = ({ idx, data = [] }) => {
	const dispatch = useDispatch();
	const [menu1, setMenu1] = useState([]);
	const [menu2, setMenu2] = useState([]);
	const nowDept = useSelector((state) => state.dept).value;
	const nowMenuType = useSelector((state) => state.menuType).value;
	const nowRestaurantId = useSelector((state) => state.user).value.selectedRestaurant;

	const handleDivClick = (clickedDept, menuList, id) => {
		dispatch(changeDept(clickedDept));
		dispatch(changeMenuInfo({mainMenuList: menuList, menuId: id}));
	};

	const MENU_TYPE_MAP = {
    2: {
			menu1: "BREAKFAST",
			menu2: "LUNCH"
    },
		3: {
			menu1: "LUNCH",
			menu2: "DINNER"
		},
		6: {
			menu1: "DORM_A",
			menu2: "DORM_C"
		}
	};

	useEffect(() => {
		if (data.length === 0) return;
		
		const menuTypes = MENU_TYPE_MAP[idx];
		
		const menu1Data = data
			.filter(item => item.menuType === menuTypes.menu1)
			.sort((a, b) => deptValue.indexOf(a.dept) - deptValue.indexOf(b.dept));
			
		const menu2Data = data
			.filter(item => item.menuType === menuTypes.menu2)
			.sort((a, b) => deptValue.indexOf(a.dept) - deptValue.indexOf(b.dept));

		setMenu1(menu1Data);
		setMenu2(menu2Data);

		if (nowMenuType === 1 && menu1Data.length > 0) {
			dispatch(changeMenuInfo({mainMenuList: menu1Data[0].mainDishList, menuId: menu1Data[0].menuId}));
		} else if (nowMenuType === 2 && menu2Data.length > 0) {
			dispatch(changeMenuInfo({mainMenuList: menu2Data[0].mainDishList, menuId: menu2Data[0].menuId}));
		}
	}, [data]);

	return (
		<div style={{ marginTop: 30}}>
			<div style={{ display: "flex", marginBottom: "15px" }}>
				<div style={todayMenuStyle}>오늘의 메뉴</div>
				{idx in MENU_TYPE_MAP && (
					<TypeTabMenu menu1={menu1} menu2={menu2} />
				)}
			</div>
			{nowMenuType === 1
				? menu1.map((item, index) => {
						return (
							<SelectedDiv
								$active={nowDept === item.dept}
								key={index}
								onClick={() => {
									handleDivClick(item.dept, item.mainDishList, item.menuId);
								}}
							>
								<MenuItem menu={item} />
							</SelectedDiv>
						);
					})
				: menu2.map((item, index) => {
						return (
							<SelectedDiv
								$active={nowDept === item.dept}
								key={index}
								onClick={() => {
									handleDivClick(item.dept, item.mainDishList, item.menuId);
								}}
							>
								<MenuItem menu={item} />
							</SelectedDiv>
						);
				  })}
		</div>
	);
};

export default TodayMenu;
