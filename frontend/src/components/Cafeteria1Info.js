import React, { useState } from "react";

const Cafeteria1MenuList = [
	{
		classification: "라면&간식",
		engName: "Ramyun&Snack",
		menuList: [
			["일반라면", 2500],
			["떡만두라면", 3000],
			["치즈라면", 3000],
			["야채김밥", 2500],
			["공기밥", 500],
		],
		operatingTime: "10:00 ~ 14:00",
		point: "* 저녁 운영 안함",
	},
	{
		classification: "양식",
		engName: "Western Food",
		menuList: [
			["등심왕돈까스", 5500],
			["눈꽃치즈돈까스", 6000],
			["닭다리살스테이크", 6000],
			["투움바파스타", 5800],
		],
		operatingTime: "11:00 ~ 14:00",
		point: "* 저녁 운영 안함",
	},
	{
		classification: "스낵",
		engName: "Fusion Snack",
		menuList: [
			["고기플러스알밥", 5300],
			["자연산치즈플러스알밥", 6000],
			["고로케플러스알밥", 5800],
			["떡갈비플러스알밥", 6000],
			["모듬알밥", 6500],
			["바삭한 감자칩", 4000],
			["콜라/사이다", 800],
		],
		operatingTime: "11:00 ~ 14:30",
		point: "* 저녁 운영 안함",
	},
	{
		classification: "한식",
		engName: "Korean Food",
		menuList: [
			["묵은지김치찌개", 5800],
			["뚝배기닭갈비덮밥", 5500],
			["뚝배기제육덮밥", 5500],
			["공기밥", 500],
			["고구마튀김", 1000],
			["구운계란2개", 1000],
		],
		operatingTime: "11:00 ~ 14:00",
		point: "",
	},
	{
		classification: "일식",
		engName: "Japanese Food",
		menuList: [
			["치킨가라아게마요", 5500],
			["가라아게카레덮밥", 5700],
			["블랙제육덜밥", 5500],
			["치킨가라아게샐러드", 5500],
			["세곱배기우동", 6500],
			["생우동", 5300],
			["매운우동", 5300],
			["닭강정", 7000],
			["참치오니기리 셀프주먹밥", 3000],
			["고소한팝콘", 1500],
			["매운겉절이", 1000],
		],
		operatingTime: "11:00 ~ 19:00",
		point: "",
	},
	{
		classification: "중식",
		engName: "Chinese Food",
		menuList: [
			["차돌온면", 6500],
			["매운차돌온면", 6500],
			["온국밥", 6500],
			["매운온국밥", 6500],
			["비빔면", 5500],
			["냉면", 5300],
			["마라맛추가", 500],
			["짜장면", 5500],
			["새우볶음밥", 7000],
			["공기밥", 500],
		],
		operatingTime: "11:00 ~ 14:00",
		point: "",
	},
];

const CafeteriaSpan = ({ str1, str3, str4 }) => {
	return (
		<>
			<div>
				<span>{str1}</span>
				<span style={{ marginLeft: "10px", float: "right" }}>{str4}</span>
				<span style={{ float: "right" }}>{str3}</span>
			</div>
		</>
	);
};

const Cafeteria1Info = () => {
	const [menus, setMenus] = useState("");

	const toggleMenu = (type) => {
		console.log(type);
		if (menus === type) {
			setMenus("");
		} else {
			setMenus(type);
		}
	};

	return (
		<div className="Cafeteria1">
			<p style={{fontSize:"18px"}}>메뉴</p>
			{Cafeteria1MenuList.map((nowList, index1) => {
				const { classification, menuList, operatingTime } =
					nowList;
				const listItem = menuList.map((name) => (
					<li
						key={name}
						style={{
							marginLeft: -40,
							listStyle: "none",
							border: "solid black",
							borderWidth: "1px 0px",
							padding: "2px 0px",
						}}
					>
						<span style={{ paddingLeft: "40px" }}>{name[0]}</span>
						<span style={{ float: "right", paddingRight: "40px" }}>
							{name[1]}
						</span>
					</li>
				));
				return (
					<div
						style={{background:"white", padding: "5px 15px", borderRadius: "10px", margin:"8px 0px"}}
						key={index1}
						onClick={() => toggleMenu(classification)}
					>
						<CafeteriaSpan
							str1={classification}
							str3={operatingTime}
							str4={menus === classification ? "△" : "▽"}
						/>
						{menus === classification && <ul>{listItem}</ul>}
					</div>
				);
			})}
		</div>
	);
};

export default Cafeteria1Info;
