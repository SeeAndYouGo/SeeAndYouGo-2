import { useState } from "react";
import styled from "@emotion/styled";
import React, { useEffect } from "react";
import Menu from "./Menu";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircleExclamation } from "@fortawesome/free-solid-svg-icons";
import ReviewWrite from "./ReviewWrite";

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
		padding: 5px 10px;
		margin-right: 5px;
		text-align: center;
		font-size: 10px;
		transition: 0.5s;
		border-radius: 20px;
		cursor: pointer;
	}

	.focused {
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

const TypeSelect = ({ idx }) => {
	const [currentTab, clickTab] = useState(0);
	const [staffMenu, setStaffMenu] = useState([]);
	const [studentMenu, setStudentMenu] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			// const nowUrl = `/api/dailyMenu/restaurant${idx}`;
			const nowUrl = `http://27.96.131.182/api/dailyMenu/restaurant${idx}`;
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
			if (idx === 2 || idx === 3) {
				const staffMenuData = data.filter(
					(item) => item.dept === "STAFF"
				);
				setStaffMenu(staffMenuData);
			}
			const studentMenuData = data.filter(
				(item) => item.dept !== "STAFF"
			);
			setStudentMenu(studentMenuData);
		});
	}, [idx]);

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{studentMenu.map((nowValue, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused" : "submenu"
						}
						onClick={() => selectMenuHandler(index)}
					>
						{nowValue.dept === "STAFF" ? "교직원식당" : "학생식당"}
					</li>
				))}
				{staffMenu.map((nowValue, index) => (
					<li
						key={index}
						className={
							index + 1 === currentTab
								? "submenu focused"
								: "submenu"
						}
						onClick={() => selectMenuHandler(index + 1)}
					>
						{nowValue.dept === "STAFF" ? "교직원식당" : "학생식당"}
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
				{studentMenu.map((nowValue, index) => {
					return (
						<Desc key={index}>
							{currentTab === index ? (
								<>
									<Menu value={nowValue} />
									<ReviewWrite
										restaurantName={idx}
										deptName={"STUDENT"}
										nowMainMenu={nowValue.dishList[0]}
									/>
								</>
							) : null}
						</Desc>
					);
				})}
				{staffMenu.map((nowValue, index) => {
					return (
						<Desc key={index}>
							{currentTab === index + 1 ? (
								<>
									<Menu value={nowValue} />
									<ReviewWrite
										restaurantName={idx}
										deptName={"STAFF"}
										nowMainMenu={nowValue.dishList[0]}
									/>
								</>
							) : null}
						</Desc>
					);
				})}
			</div>
		</>
	);
};

export default TypeSelect;
