import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import { useSelector, useDispatch } from "react-redux";
import { faChevronRight, faStar } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import MyProgress from "./MyProgress";
import { logout } from "../../redux/slice/UserSlice";
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

// 키워드 포함된 식단 표시
const Highlight = styled.img`
	position: relative;
	top: -2px;
	width: 25px;
	height: 25px;
	display: flex;
`;

// dept 표시
const Dept = styled.p`
	padding: 3px 7px 0px 7px;
	margin: 0px 5px;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 400;
	height: 20px;
`;

// 메뉴 가격 표시
const Price = styled.label`
	color: "#777777";
	margin: 2px 5px;
	font-size: 13px;
	font-weight: 400;
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
const Menu = ({ menuDept, menuPrice, menuName, keywordList, dishList }) => {
	const [isKeyword, setIsKeyword] = useState(false);

	useEffect(() => {
		if (keywordList.length === 0) return;

		dishList.forEach((dish) => {
			keywordList.forEach((keyword) => {
				if (dish.includes(keyword)) {
					setIsKeyword(true);
					return;
				}
			});
		});
	}, [dishList, keywordList]);

	return (
		<div>

			<div
				style={{
					display: "flex",
					margin: "10px 0px 5px 0px",
					justifyContent: "center",
				}}
			>
				{isKeyword ? (
					<Highlight
						src="/assets/images/highlight.png"
						alt="highlight"
					/>
				) : null}
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

const FirstRowWrapper = styled.div`
display: flex;
margin-left: auto;
margin-right: 15px;
`;

const Cafeteria = ({ idx, value }) => {
	const user = useSelector((state) => state.user.value);
	const token = user.token;
	const dispatch = useDispatch();

	const CafeteriaContainer = styled.div`
		width: 100%;
		height: 120px;
		margin-top: 15px;
		background-color: white;
		border-radius: 20px;

		${idx === 1 ? "height: 50px;" : null}
	`;
	const FirstRow = styled.div`
		width: 100%;
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
		margin-left: 15px;
		font-weight: 700;
		${idx === 4 ? "margin-left: 15px;" : null}

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
	const [restaurant1Rate, setRestaurant1Rate] = useState(0);

	useEffect(() => {
			if (idx !== 1) return;

    const url = config.DEPLOYMENT_BASE_URL + `/restaurant/1/rate/main`;
		axios.get(url)
		.then((res) => {
			// console.log(res);
			setRestaurant1Rate(res.data.totalAvgRate);
		}).catch((err) => {
			console.log(err);
		});
	}, [idx]);

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
				`/daily-menu/restaurant${idx}` +
				(config.NOW_STATUS === 0 ? ".json" : `${token ? "/"+token : ""}`);

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
		}).catch((err) => {
			console.log(err);
			dispatch(logout());
			window.location.reload();
		});
	}, [value, idx, token, dispatch]);

	return (
		<CafeteriaContainer>
			<FirstRow>
				<CafeteriaName>
					{nameList[idx]}
				</CafeteriaName>
				{idx === 1 ? (
					<span style={{ fontSize: 11, fontWeight: 300}}>
						<FontAwesomeIcon icon={faStar} style={{color: "#ffd700", marginRight: 2}} />
						{restaurant1Rate.toFixed(1)}
					</span> 
				) : null}

				<FirstRowWrapper>
					<span style={{ fontWeight: 500, fontSize: 11}}>
						{status}
					</span>
					<MyProgress value={value} />
					<FontAwesomeIcon
						icon={faChevronRight}
						style={{ color: "#b0b0b0", marginLeft: 10 }}
					/>
				</FirstRowWrapper>

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
								keywordList={val.keywordList}
								dishList={val.dishList}
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
										keywordList={val.keywordList}
										dishList={val.dishList}
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
