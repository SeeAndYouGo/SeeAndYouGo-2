import { useState } from "react";
import styled from "@emotion/styled";
import React, { useEffect } from "react";
import Menu from "./Menu";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircleExclamation } from "@fortawesome/free-solid-svg-icons";

const TabMenu = styled.ul`
	color: black;
	font-weight: bold;
	display: flex;
	flex-direction: row;
	align-items: center;
	list-style: none;
	margin-top: 10px;
	border: solid 1.5px black;
	border-radius: 20px;
	padding: 5px;

	.submenu {
		// 기본 Tabmenu 에 대한 CSS를 구현
		/* display: flex-start; */
		/* justify-content: space-between; */
		/*width: 380px;
    heigth: 30px; */
		/* width: calc(100% / 4); */
		padding: 5px 10px;
		margin-right: 5px;
		text-align: center;
		font-size: 10px;
		transition: 0.5s;
		border-radius: 20px;
		cursor: pointer;
	}

	.focused {
		//선택된 Tabmenu 에만 적용되는 CSS를 구현
		/* background-color: rgb(255, 255, 255); */
		/* color: rgb(21, 20, 20); */
		background-color: black;
		color: white;
	}

	& div.desc {
		text-align: center;
	}
`;

const Desc = styled.div`
	text-align: center;
`;

// TODO 학생 식당, 교직원 식당 구분해서 출력하도록 하기
const TypeSelect = ({ idx }) => {
	const [currentTab, clickTab] = useState(0);
	const [menuData, setMenuData] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			const nowUrl = `/api/dailyMenu/restaurant${idx}`;
			// const nowUrl = "/assets/json/myMenu.json";
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
		});
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{menuData.map((nowValue, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused" : "submenu"
						}
						onClick={() => selectMenuHandler(index)}
					>
						{nowValue.dept}
					</li>
				))}
				<FontAwesomeIcon
					icon={faCircleExclamation}
					style={{ marginLeft: 15, fontSize: 12 }}
				/>
				<span style={{ fontSize: 10, marginLeft: 5, fontWeight: 400 }}>
					교직원은 학생도 이용 가능합니다.
				</span>
			</TabMenu>
		);
	};

	const selectMenuHandler = (index) => {
		// parameter로 현재 선택한 인덱스 값을 전달해야 하며, 이벤트 객체(event)는 쓰지 않는다
		// 해당 함수가 실행되면 현재 선택된 Tab Menu 가 갱신.
		clickTab(index);
	};

	return (
		<>
			<div style={{ marginTop: 30 }}>
				{idx === 2 || idx === 3 ? <TabMenuUl /> : null}
				{menuData.map((nowValue, index) => {
					return (
						<Desc key={index}>
							{currentTab === index ? (
								<Menu value={nowValue} />
							) : null}
						</Desc>
					);
				})}
			</div>
		</>
	);
};

export default TypeSelect;
