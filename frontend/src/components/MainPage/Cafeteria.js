import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import { faChevronRight } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import MyProgress from "./MyProgress";
import * as config from "../../config";

// 2번째 Row (메뉴 이름과 가격 정보)
const SecondRow = styled.div`
	display: flex;
	justify-content: center;
	align-items: center;
	text-align: center;
	height: 50%;

	> div {
		border-left: dashed 1.5px #d1d1d1;
		flex-basis: 50%;
	}

	> div:first-of-type {
		border-left: none;
	}
`;

// dept 표시
const Dept = styled.p`
	padding: 1px 7px;
	margin: 0px 5px;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 400;
`;

// 메뉴 가격 표시
const Price = styled.label`
	color: "#777777";
	margin: 2px 5px;
	font-size: 12px;
	font-weight: 300;
`;

// 식당별 메인 메뉴 표시
const MenuItem = styled.p`
	align-items: center;
	font-size: 14px;
	font-weight: 500;
	margin: 0px 0px 10px 0px;
`;

const NoMenuInfo = styled.p`
	align-items: center;
	font-size: 14px;
	font-weight: 500;
	margin: 0px;
`;

// Row 2번째에서의 메뉴 이름과 가격
const Menu = ({ menuDept, menuPrice, menuName }) => {
	return (
		<div>
			<div
				style={{
					display: "flex",
					margin: "10px 0px 5px 0px",
					justifyContent: "center",
				}}
			>
				<Dept>{menuDept === "STAFF" ? "교직원" : "학생"}</Dept>
				<Price>{menuPrice}</Price>
			</div>
			<MenuItem>{menuName}</MenuItem>
		</div>
	);
};

// 식당 이름 배열
const nameList = [
	"",
	"1학생회관\u00a0",
	"2학생회관\u00a0",
	"3학생회관\u00a0",
	"\u00a0상록회관\u00a0",
	"생활과학대",
];

const Cafeteria = ({ idx, value }) => {
	const CafeteriaContainer = styled.div`
		width: 100%;
		height: 120px;
		margin-top: 15px;
		background-color: white;
		border-radius: 20px;

		${idx === 1 ? "height: 50px;" : null}
	`;
	const FirstRow = styled.div`
		display: flex;
		align-items: center;
		padding-top: 10px;
		height: 40%;
		${idx === 1
			? "top: 50%; transform: translateY(-50%); padding: 0; position:relative;"
			: null}
	`;
	// 식당 이름 표시
	const CafeteriaName = styled.p`
		font-size: 15px;
		margin-left: 20px;
		font-weight: 700;
		${idx === 4 ? "margin-left: 25px;" : null}

		::after {
			content: "";
			display: block;
			width: 50px;
			border-bottom: 3px solid #000000;
			margin: 0 auto;
			padding-top: 2px;
		}
	`;

	const [status, setStatus] = useState("원활");
	const [staffMenu, setStaffMenu] = useState([]);
	const [studentMenu, setStudentMenu] = useState([]);

	useEffect(() => {
		if (value >= 66) {
			setStatus("혼잡");
		} else if (value >= 33) {
			setStatus("보통");
		} else {
			setStatus("원활");
		}

		if (idx === 1) return;

		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/dailyMenu/restaurant${idx}` +
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
	}, [value, idx]);

	return (
		<CafeteriaContainer>
			<FirstRow>
				<CafeteriaName>{nameList[idx]}</CafeteriaName>
				<span style={{ fontWeight: 500, fontSize: 11, marginLeft: 20 }}>
					{status}
				</span>
				<MyProgress value={value} />
				<FontAwesomeIcon
					icon={faChevronRight}
					style={{ color: "#b0b0b0", marginLeft: 10 }}
				/>
			</FirstRow>
			{idx === 1 ? null : (
				<SecondRow>
					{studentMenu.map((val, index) => {
						return val.dishList.length === 0 ? (
							<div key={index}>
								<NoMenuInfo>메뉴 없음</NoMenuInfo>
							</div>
						) : (
							<Menu
								key={index}
								menuDept={val.dept}
								menuPrice={val.price}
								menuName={val.dishList[0]}
							/>
						);
					})}
					{idx === 2 || idx === 3
						? staffMenu.map((val, index) => {
								return val.dishList.length === 0 ? (
									<div>
										<NoMenuInfo>메뉴 없음</NoMenuInfo>
									</div>
								) : (
									<Menu
										key={index}
										menuDept={val.dept}
										menuPrice={val.price}
										menuName={val.dishList[0]}
									/>
								);
						  })
						: null}
				</SecondRow>
			)}
		</CafeteriaContainer>
	);
};

export default Cafeteria;
