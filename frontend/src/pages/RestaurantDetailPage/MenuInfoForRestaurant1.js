import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStar } from "@fortawesome/free-solid-svg-icons";
import * as config from "../../config";

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
		classification: "한식",
		menuList: [
		],
		operatingTime: "11:00 ~ 14:00",
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

	const toggleMenu = (type) => {
		if (menus === type) {
			setMenus("");
		} else {
			setMenus(type);
		}
	};

	useEffect(() => {
    const url = config.DEPLOYMENT_BASE_URL + `/restaurant/1/rate/detail`;
		axios.get(url)
		.then((res) => {
			res.data.map((val, idx) => {
				Restaurant1MenuList[idx].menuList = val.avgRateByMenu;
			});
			setMenuArray(Restaurant1MenuList);
		}).catch((err) => {
			console.log(err);
		});

	} ,[]);

	return (
		<div>
			<p style={{ fontSize: "18px", margin: 0 }}>메뉴</p>
			{menuArray.map((nowList, index1) => {
				const { classification, menuList, operatingTime } = nowList;
				const listItem = menuList.map((val) => (
						<InnerList key={val[0]}>
							<span style={{ fontSize: 12, fontWeight: 400, marginLeft: 5, fontWeight: 300}}>
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
