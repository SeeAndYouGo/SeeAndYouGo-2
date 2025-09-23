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

const PriceLabel = styled.div`
	font-size: 12px;
	color: #555;
	background-color: #d9d9d9;
	border-radius: 5px;
	margin-top: 10px;
	padding: 3px 10px;
	display: inline-flex;
	flex-grow: 0;
`;

const deptValue = ["STUDENT", "STAFF"];

const TodayMenu = ({ idx, data = [] }) => {
	const dispatch = useDispatch();
	const [menu1, setMenu1] = useState([]);
	const [menu2, setMenu2] = useState([]);
	const [menu3, setMenu3] = useState([]);
	const nowDept = useSelector((state) => state.dept).value;
	const nowMenuType = useSelector((state) => state.menuType).value;
	const nowRestaurantId = useSelector((state) => state.user).value.selectedRestaurant;
	const nowMainMenuList = useSelector((state) => state.nowMenuInfo.value).mainMenuList;
	const nowMenuIsOpen = useSelector((state) => state.nowMenuInfo).value.menuIsOpen;
	const nowMenuId = useSelector((state) => state.nowMenuInfo).value.menuId;

	const handleDivClick = (clickedDept, menuList, id, isOpen) => {
		dispatch(changeDept(clickedDept));
		dispatch(changeMenuInfo({mainMenuList: menuList, menuId: id, menuIsOpen: isOpen}) );
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
		4: {
			menu1: "LUNCH",
		},
		5: {
			menu1: "LUNCH",
		},
		6: {
			menu1: "BREAKFAST",
			menu2: "LUNCH",
			menu3: "DINNER"
		}
	};

	useEffect(() => {
		if (data.length === 0) return;

		const menuTypes = MENU_TYPE_MAP[idx] || {};

		const customDeptOrder = (a, b) => {
			if (idx === 6) {
				if (a.dept === b.dept) return 0;
				if (a.dept === "DORM_A") return -1;
				if (b.dept === "DORM_A") return 1;
				return 0;
			} else {
				return deptValue.indexOf(a.dept) - deptValue.indexOf(b.dept);
			}
		};

		const menu1Data = data
			.filter(item => item.menuType === menuTypes.menu1)
			.sort(customDeptOrder);

		const menu2Data = data
			.filter(item => item.menuType === menuTypes.menu2)
			.sort(customDeptOrder);

		const menu3Data = data
			.filter(item => item.menuType === menuTypes.menu3)
			.sort(customDeptOrder);
		
		setMenu1(menu1Data);
		setMenu2(menu2Data);
		setMenu3(menu3Data);
	}, [data, idx, nowMenuType]);

	useEffect(() => {
		const menuDataList = [menu1, menu2, menu3];
		const targetMenuData = menuDataList[nowMenuType - 1];
		if (targetMenuData?.length > 0) {
      const selectedMenu = targetMenuData.find(item => item.mainDishList.length > 0) || targetMenuData[0];

      // if (JSON.stringify(selectedMenu.mainDishList) !== JSON.stringify(nowMainMenuList) || 
      //     selectedMenu.dept !== nowDept) {
      //   dispatch(changeMenuInfo({mainMenuList: selectedMenu.mainDishList, menuId: selectedMenu.menuId, menuIsOpen: selectedMenu.open}));
      //   dispatch(changeDept(selectedMenu.dept));
      // }
			dispatch(changeMenuInfo({mainMenuList: selectedMenu.mainDishList, menuId: selectedMenu.menuId, menuIsOpen: selectedMenu.open}));
    }
	}, [menu1, menu2, menu3]);


	const getMenuListByType = (type) => {
		if (type === 1) return menu1;
		if (type === 2) return menu2;
		if (type === 3) return menu3;
		return [];
	};

	const renderMenuList = () => {
		const menuList = getMenuListByType(nowMenuType);
		if (menuList.length === 0) return <div style={{marginTop: 10, color: '#aaa'}}>메뉴가 없습니다.</div>;
		return menuList.map((item, index) => (
			<SelectedDiv
				$active={nowDept === item.dept}
				key={index}
				onClick={() => {
					handleDivClick(item.dept, item.mainDishList, item.menuId, item.open);
				}}
			>
				<MenuItem menu={item} restaurantId={idx} />
			</SelectedDiv>
		));
	};

	return (
		<div style={{ marginTop: 30}}>
			<div style={{ display: "flex" }}>
				<div style={todayMenuStyle}>오늘의 메뉴</div>
				{(menu1.length > 0 || menu2.length > 0 || menu3.length > 0) && (
					<TypeTabMenu menu1={menu1} menu2={menu2} menu3={menu3} />
				)}
			</div>
			{idx === 6 && <PriceLabel>판매식 4,200</PriceLabel>}
			{renderMenuList()}
		</div>
	);
};

export default TodayMenu;
