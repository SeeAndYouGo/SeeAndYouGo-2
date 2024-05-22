import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import Loading from "../../components/Loading";
import BarChart from "./BarChart";
import LineChart from "./LineChart";
import * as config from "../../config";

const MobileDisplay = styled.div`
	display: block;
	@media (min-width: 576px) {
		display: none;
	}
`;

const DesktopDisplay = styled.div`
	display: none;
	@media (min-width: 576px) {
		display: block;
	}
`;

const TabMenu = styled.ul`
	color: black;
	font-size: 12px;
	display: flex;
	list-style: none;
	border: solid 1.5px black;
	border-radius: 20px;
	padding: 5px;

	.submenu {
		text-align: center;
		padding: 4px 10px;
		margin: 0 auto;
		border-radius: 20px;
		cursor: pointer;
		@media (min-width: 576px) {
			display: none;
			padding: 4px 17px;
		}
	}
	.submenu.tablet {
		display: none;
		@media (min-width: 576px) {
			display: block;
		}
	}

	.focused {
		background-color: black;
		color: white;
	}
`;

const ChartWrapper = styled.div`
	width: 100%;
	margin: 20px 0 20px 0;
	background: #fff;
	border-radius: 20px;
	padding: 20px;
`;

const restaurantArray = ["1학", "2학", "3학", "상록회관", "생과대"];
const tabletRestaurantArray = [
	"1학생회관",
	"2학생회관",
	"3학생회관",
	"상록회관",
	"생활과학대학",
];

const StatisticsPage = () => {
	const [datas, setDatas] = useState([]);
	const [currentTab, setCurrentTab] = useState(0);

	const createUrl = (idx) => config.BASE_URL + "/statistics/restaurant" + idx + (config.NOW_STATUS === 0 ? ".json" : "");
	
	// 도메인 연결해서 통계 확인하기
	// const createUrl = (idx) => "https://seeandyougo.com/api"+"/statistics/restaurant" + idx;

	useEffect(() => {
		const fetchData = async () => {
			try {
				const url = [
					createUrl(1),
					createUrl(2),
					createUrl(3),
					createUrl(4),
					createUrl(5),
				];
				await axios.all(url.map((path) => axios.get(path))).then((res) => {
					setDatas(res.map((data) => data.data));
				});
			} catch (error) {
				console.error(error);
			}
		};
		fetchData();
	}, []);

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{restaurantArray.map((el, index) => (
					<li
						key={index}
						className={index === currentTab ? "submenu focused" : "submenu"}
						onClick={() => setCurrentTab(index)}
					>
						{el}
					</li>
				))}
				{tabletRestaurantArray.map((el, index) => (
					<li
						key={index}
						className={
							index === currentTab ? "submenu focused tablet" : "submenu tablet"
						}
						onClick={() => setCurrentTab(index)}
					>
						{el}
					</li>
				))}
			</TabMenu>
		);
	};

	return (
		<div className="App3">
			<div style={{ textAlign: "center" }}>
				<p style={{ fontSize: 20, margin: 10 }}>혼잡도 통계보기</p>
				<p style={{ margin: 0, fontWeight: 600, fontSize: 15 }}>
					학생 식당의 시간별 평균 인원 수를 확인하세요.
				</p>
			</div>
			<TabMenuUl />
			<ChartWrapper>
				{datas.length === 0 ? (
					<Loading />
				) : (
					<>
						<MobileDisplay>
							<BarChart datas={datas} tab={currentTab} />
						</MobileDisplay>
						<DesktopDisplay>
							<LineChart datas={datas} tab={currentTab} />
						</DesktopDisplay>
					</>
				)}
			</ChartWrapper>
			<div style={{ marginBottom: 30, fontSize: 15 }}>
				<p style={{ margin: "5px 0", fontWeight: 500 }}>
					! 본 데이터는 충남대학교 정보화본부에서 제공하는 공공데이터 "무선랜
					Wifi [무선랜 위치 및 위치별 접속자 수 데이터]"를 활용해
					제작되었습니다.
				</p>
				<p style={{ margin: "5px 0", fontWeight: 500 }}>
					! 학생 식당에 있는 Wifi와 연결된 기기의 접속자 수를 통해 얻은
					데이터로, 실제 인원 수와 다를 수 있습니다.
				</p>
				<p style={{ margin: "5px 0", fontWeight: 500 }}>
					! 정확한 수치가 아니니 점심시간 학생 식당에 사람이 어느 특정 시간대에
					몰리는 경향이 있는지 참고용으로 봐주시면 감사하겠습니다.
				</p>
			</div>
		</div>
	);
};

export default StatisticsPage;
