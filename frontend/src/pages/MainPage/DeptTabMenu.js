import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useDispatch, useSelector } from "react-redux";
import { changeDept } from "../../redux/slice/DeptSlice";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";

const TabMenu = styled.ul`
	background-color: white;
	display: flex;
	list-style: none;
	border-radius: 20px;
	padding: 0 5px;
	margin: 0;

	.submenu {
		padding: 5px 10px;
		margin-right: 5px;
		font-size: 14px;
		border-radius: 20px;
		cursor: pointer;
	}

	.focused {
		font-weight: 900;
		text-decoration-line: underline;
		text-decoration-thickness: 3px;
		text-underline-offset: 8px;
	}
`;

const DeptTabMenu = ({ studentMenu, staffMenu }) => {
	const [currentTab, clickTab] = useState(0);
	const dispatch = useDispatch();
	const beforeDept = useSelector((state) => state.dept).value;
	const dept = useSelector((state) => state.dept).value;
	
	const selectMenuHandler = (index) => {
		if (beforeDept === index + 1) return;
		console.log(index+1)
		dispatch(changeDept(index + 1));
		dispatch(changeMenuType(0));

		if (index === 0 && studentMenu.length > 0) {
			// 학생 탭 선택 시 첫 번째 메뉴의 dishList로 menuList 업데이트
			dispatch(changeMenuInfo({mainMenuList:studentMenu[0].mainDishList, menuId: studentMenu[0].menuId}));
			// console.log(beforeDept, "이전 클릭 확인1");
		} else if (index === 1 && staffMenu.length > 0) {
			// 교직원 탭 선택 시 첫 번째 메뉴의 dishList로 menuList 업데이트
			dispatch(changeMenuInfo({mainMenuList:staffMenu[0].mainDishList, menuId: staffMenu[0].menuId}));
		}

		clickTab(index);
	};

	useEffect(() => {
		if (dept === 1) {
			clickTab(0);
		} else if (dept === 2) {
			clickTab(1);
		}
}, [dept]);

	const TabMenuUl = () => {
		return (
			<TabMenu>
				<li
					className={currentTab === 0 ? "submenu focused" : "submenu"}
					onClick={() => selectMenuHandler(0)}
				>
					{"학생"}
				</li>
				<li
					className={currentTab === 1 ? "submenu focused" : "submenu"}
					onClick={() => selectMenuHandler(1)}
				>
					{"교직원"}
				</li>
			</TabMenu>
		);
	};

	return (
		<div style={{marginLeft: 'auto'}}>
			<TabMenuUl />
		</div>
	);
};

export default DeptTabMenu;
