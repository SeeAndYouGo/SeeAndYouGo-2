import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import { faChevronRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import MyProgress from "./MyProgress";

// 식당 이름 표시
const CafeteriaName = styled.p`
	font-size: 15px;
	margin-left: 20px;
	font-weight: 700;

	::after {
		content: "";
		display: block;
		width: 50px;
		border-bottom: 3px solid #000000;
		margin: 0 auto;
		padding-top: 2px;
	}
`;

// 2번째 Row (메뉴 이름과 가격 정보)
const SecondRow = styled.div`
	display: flex;
	justify-content: center;
	align-items: center;
	text-align: center;
	height: 65%;

	> div {
		border-left: dashed 1.5px #d1d1d1;
		flex-basis: 50%;
	}

	> div:first-of-type {
		border-left: none;
	}
`;

const MenuSlider = styled.div`
	padding-bottom: 5px;
	display: flex;
	justify-content: center;
	align-items: center;
	overflow: hidden;
`;

const MenuItem = styled.p`
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 14px;
	font-weight: 500;
	position: absolute;
	transition: transform 0.5s ease-in-out, opacity 0.5s ease-in-out;
	opacity: 0;
	transform: translateY(100%);

	${({ active }) =>
		active &&
		`
        opacity: 1;
        transform: translateY(0);
    `}
`;

// 메뉴 이름 1.5초마다 변경되어 표시
const MenuList = ({ nowList }) => {
	const [currentIndex, setCurrentIndex] = useState(0);

	useEffect(() => {
		const interval = setInterval(() => {
			setCurrentIndex((prevIndex) => (prevIndex + 1) % nowList.length);
		}, 1500); // 1.5초마다 변경

		return () => clearInterval(interval);
	}, [nowList.length, currentIndex]);

	return (
		<MenuSlider>
			{nowList.map((item, index) => (
				<MenuItem key={index} active={index === currentIndex}>
					{item}
				</MenuItem>
			))}
		</MenuSlider>
	);
};

// 메뉴 가격 표시
const Price = styled.label`
	color: "#777777";
	margin: 5px 0;
	font-size: 12px;
	font-weight: 300;
`;

// Row 2번째에서의 메뉴 이름과 가격
const Menu = ({ menuName, priceValue }) => {
	return (
		<div>
			<MenuList nowList={menuName} />
			<Price>{priceValue}</Price>
		</div>
	);
};

// 식당 이름 배열
const nameList = [
	"1학생회관",
	"2학생회관",
	"3학생회관",
	"상록회관",
	"생활과학대",
];

const Cafeteria = ({ idx, value }) => {

	const CafeteriaContainer = styled.div`
	width: 100%;
	height: 120px;
	margin-top: 15px;
	background-color: white;
	border-radius: 20px;

	${idx == 1 ?
		"height: 50px;"
		: null
	}
`;
	const FirstRow = styled.div`
	display: flex;
	align-items: center;
	padding-top: 10px;
	height: 40%;
	${idx == 1 ?
			"top: 50%; transform: translateY(-50%); padding: 0; position:relative;"
			: null
		}
`;

	const [status, setStatus] = useState("원활");
	const [rate, setRate] = useState(value);
	const [menuData, setMenuData] = useState([]);

	useEffect(() => {
		if (rate >= 66) {
			setStatus("혼잡");
		} else if (rate >= 33) {
			setStatus("보통");
		} else {
			setStatus("원활");
		}
		setRate(value);

		const fetchData = async () => {
			// "http:localhost:8080/get_menu/{name}/{date}"
			const res = await fetch(`/assets/json/myMenu.json`, {
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
		});
	}, [value, rate]);

	return (
		<CafeteriaContainer>
			<FirstRow>
				<CafeteriaName>{nameList[idx - 1]}</CafeteriaName>
				<span style={{ fontWeight: 500, fontSize: 11, marginLeft: 10 }}>
					{status}
				</span>
				<MyProgress value={rate} />
				<FontAwesomeIcon
					icon={faChevronRight}
					style={{ color: "#b0b0b0", marginLeft: 10 }}
				/>
			</FirstRow>
			{idx == 1 ? null :
				<SecondRow>
					{menuData.map((val, index) => {
						return (
							<Menu
								key={index}
								menuName={val.menu}
								priceValue={val.price}
							/>
						);
					})}
				</SecondRow>
			}

		</CafeteriaContainer>
	);
};

export default Cafeteria;