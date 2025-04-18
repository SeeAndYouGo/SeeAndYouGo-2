import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { Swiper, SwiperSlide } from "swiper/react";
import "swiper/css";
import axios from "axios";
import Loading from "../../components/Loading";
import BarChart from "./BarChart";
import { get } from "../../api";

const Slider = styled.div`
	background-color: #fff;
	border-radius: 10px;
	padding: 10px 20px;
	font-size: 18px;
	font-weight: 600;
	width: 100%;
	overflow-x: scroll;
	touch-action: none;
	box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.1);
	&::-webkit-scrollbar {
		display: none;
	}
	-webkit-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
`;

const TabButton = styled.div`
	font-weight: 700;
	cursor: pointer;
	color: #c0c0c0;
	${({ $active }) =>
		$active &&
		`
    color: #111;
  `}
	transform: translateX(${(props) => props.slide}px);
	transition: 0.5s ease;
	margin: 0;
`;

const ChartWrapper = styled.div`
	width: 100%;
	margin: 20px 0 20px 0;
	background: #fff;
	border-radius: 20px;
	padding: 20px;
`;

const restaurantArray = ["1학생회관", "2학생회관", "3학생회관", "상록회관", "생활과학대", "기숙사식당"];

const TabBar = ({ currentTab = 0, setCurrentTab }) => {
	const [swiper, setSwiper] = useState(null);

	useEffect(() => {
    if (swiper) {
      swiper.slideTo(currentTab < 3 ? 0 : currentTab);
    }
  }, [currentTab, swiper]);

	const StatisticsSwiperSlide = () => {
		const result = [];

		for (let i = 0; i < restaurantArray.length; i++) {
			result.push(
				<SwiperSlide key={i} className="sw-item">
					<TabButton
						$active={currentTab === i}
						onClick={() => {
							setCurrentTab(i)
						}}
					>
						{restaurantArray[i]}
					</TabButton>
				</SwiperSlide>
			);
		}
		return result;
	}

	return (
		<>
			<Slider>
				<Swiper
					className="sw-tap"
					style={{ textAlign: "center", fontSize: 18 }}
					initialSlide={
            currentTab < 3 ? 0 : currentTab
          }
					speed={1000}
					slidesPerView={3.5}
          onSwiper={setSwiper}
				>
					{StatisticsSwiperSlide()}
				</Swiper>
			</Slider>
		</>
	)
};

const StatisticsPage = () => {
	const [datas, setDatas] = useState([]);
	const [currentTab, setCurrentTab] = useState(0);

	const createUrl = (idx) => "/statistics/restaurant" + idx;
	
	useEffect(() => {
		const fetchData = async () => {
			try {
				const url = [];
				for (let i = 0; i < restaurantArray.length; i++) {
					url.push(createUrl(i + 1));
				}
				await axios.all(url.map((path) => get(path))).then((res) => {
					setDatas(res.map((data) => data.data));
				});
			} catch (error) {
				console.error(error);
			}
		};
		fetchData();
	}, []);

	return (
		<div className="App3">
			<div style={{ textAlign: "center" }}>
				<p style={{ fontSize: 20, margin: 10 }}>혼잡도 통계보기</p>
				<p style={{ margin: "0 0 20px 0", fontWeight: 600, fontSize: 15 }}>
					학생 식당의 시간별 평균 인원 수를 확인하세요.
				</p>
			</div>
			<TabBar 
				currentTab={currentTab}
				setCurrentTab={setCurrentTab}
			/>
			<ChartWrapper>
				{datas.length === 0 ? (
					<Loading />
				) : (
					<div>
						<BarChart datas={datas} tab={currentTab} />
					</div>
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
