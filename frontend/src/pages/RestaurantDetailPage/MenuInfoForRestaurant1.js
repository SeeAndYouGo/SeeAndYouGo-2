import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar } from "@fortawesome/free-solid-svg-icons";
import { get } from "../../api";

const Restaurant1MenuList = [
	{
		classification: "라면&간식",
		menuList: [
		],
		operatingTime: "10:00 ~ 14:00",
	},
	{
		classification: "양식",
		menuList: [
		],
		operatingTime: "11:00 ~ 14:00",
	},
	{
		classification: "스낵",
		menuList: [
		],
		operatingTime: "11:00 ~ 14:30",
	},
	{
		classification: "일식",
		menuList: [
		],
		operatingTime: "11:00 ~ 19:00",
	},
	{
		classification: "중식",
		menuList: [
		],
		operatingTime: "11:00 ~ 14:00",
	},
	{
		classification: "한식",
		menuList: [
		],
		operatingTime: "11:00 ~ 14:00",
	},
];


const InnerList = styled.li`
	list-style: none;
	// border-bottom: solid 1px #ddd;
	border-width: 1px 0px;
	padding: 2px 0px;
`;

const ListContainer = styled.div`
	background: white;
	padding: 7px 15px;
	border-radius: 10px;
	margin: 8px 0px;
	font-size: 15px;
	cursor: pointer;
`;

const CafeteriaSpan = ({ str1, str3, str4 }) => {
	return (
		<>
			<div>
				<span>{str1}</span>
				<span style={{ marginLeft: "10px", float: "right", fontSize: 12, color: "#999" }}>{str4}</span>
				<span style={{ float: "right", fontWeight: 400, fontSize: 14}}>{str3}</span>
			</div>
		</>
	);
};

const MenuInfoForRestaurant1 = () => {
	const [menuArray, setMenuArray] = useState(Restaurant1MenuList);
	const [menus, setMenus] = useState("");
	const dispatch = useDispatch();

	const toggleMenu = (type) => {
		if (menus === type) {
			setMenus("");
		} else {
			setMenus(type);
		}
	};

	useEffect(() => {
		get(`/restaurant/1/rate/detail`)
		.then((res) => {
			res.data.map((val, idx) => Restaurant1MenuList[idx].menuList = val.avgRateByMenu);
			setMenuArray(Restaurant1MenuList);
		}).catch((err) => {
			console.log(err);
		});
	} ,[]);

	useEffect(() => {
		get(`/daily-menu/restaurant1`)
		.then((res) => {
			if (res.data.length === 0) {
				dispatch(changeMenuInfo({mainMenuList: [], menuId: 0, menuIsOpen: false}));
				console.log("1학 메뉴 정보가 없습니다...")
			}
		}).catch((err) => {
			console.log(err);
		});
	}, []);

	return (
		<div style={{ marginTop: 20 }}>
			<p style={{ fontSize: 22, margin: 0, fontWeight: 700 }}>메뉴</p>
			{menuArray.map((nowList, index1) => {
				const { classification, menuList, operatingTime } = nowList;
				const listItem = menuList.map((val) => (
						<InnerList key={val.menuName}>
							<span style={{ fontSize: 12, marginLeft: 5, fontWeight: 300}}>
								<FontAwesomeIcon icon={faStar} style={{color: "#ffd700", marginRight: 2}} />
								{val.averageRate.toFixed(1)}
							</span>
							<span style={{ paddingLeft: "20px", fontSize: 14, fontWeight: 400, color: "#333" }}>{val.menuName}</span>
							<span style={{ float: "right", paddingRight: "20px", fontWeight: 400 }}>
								{val.price.toLocaleString()}
							</span>
						</InnerList>
				));
				return (
					<ListContainer
						key={index1}
						onClick={() => toggleMenu(classification)}
					>
						<CafeteriaSpan
							str1={classification}
							str3={operatingTime}
							str4={menus === classification ? "△" : "▽"}
						/>
						{menus === classification && <ul style={{ padding: 5, margin: "5px 0"}}>{listItem}</ul>}
					</ListContainer>
				);
			})}
		</div>
	);
};

export default MenuInfoForRestaurant1;
