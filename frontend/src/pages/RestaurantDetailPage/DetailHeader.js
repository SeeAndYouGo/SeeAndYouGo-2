import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faAngleLeft, faMapLocationDot, faCalendarDays } from "@fortawesome/free-solid-svg-icons";
import { faClock } from "@fortawesome/free-regular-svg-icons";
import Modal from "../../components/Modal";
import ModalLocation from "./ModalLocation";
import ModalMenuTable from "./ModalMenuTable";
import * as config from "../../config";

const DetailHeaderContainer = styled.div`
	display: flex;
	flex-wrap: wrap;
`;

const LeftDiv = styled.div`
	display: flex;
	flex: 1;
	order: 1;
`;

const RightDiv = styled.div`
	flex: 1;
	order: 2;
`;

const CafeteriaName = styled.div`
	display: flex;
	align-items: center;
	text-align: center;
	font-size: 26px;
	font-weight: bold;
`;

const TimeInfo = styled.div`
	font-weight: 400;
	font-size: 10px;
	padding-top: 5px;
	margin-left: 15px;
	color: #777777;
`;

const Congestion = styled.div`
	text-align: center;
	padding-top: 10px;
`;

const ModalContainer = styled.div`
	display: flex;
	float: right;
`;

const ModalContent = styled.div`
	width: 50px;
	height: 50px;
	margin-top: 5px;
	padding: 5px;
	text-align: center;
	background: white;
	border-radius: 5px;
	cursor: pointer;
`;

const operatingTime = [
	[],
	["1학생회관", ""],
	["2학생회관", "11:00-13:30"],
	["3학생회관", "11:00-13:30"],
	["\u00a0상록회관", "11:00-14:00"],
	["생활과학대", "11:00-14:00"],
];

const DetailHeader = ({ idx }) => {
	const [visible1, setVisible1] = useState(false);
	const [visible2, setVisible2] = useState(false);
	const [rate, setRate] = useState(0);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/connection/restaurant${idx}` +
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
		fetchData().then((val) => {
			setRate((val.connected / val.capacity) * 100);
		});
	}, [idx]);

	return (
		<DetailHeaderContainer>
			<LeftDiv>
				<div style={{ width: 160 }}>
					<CafeteriaName>
						<Link to={`/`}>
							<FontAwesomeIcon icon={faAngleLeft} />
						</Link>
						<p style={{ margin: "0px 0px 0px 5px" }}>{operatingTime[idx][0]} </p>
					</CafeteriaName>
					<TimeInfo>
						{idx === 1 ? null : (
							<>
								<FontAwesomeIcon icon={faClock} />
								<label style={{ marginLeft: 5 }}>
									운영 시간 {operatingTime[idx][1]}
								</label>
							</>
						)}
					</TimeInfo>
				</div>
				<Congestion>
					<img src={"/assets/images/People.png"} alt={"Loading..."} />
					<p style={{ margin: "0", fontSize: 10 }}>
						{rate < 33 ? "원활" : rate < 66 ? "보통" : "혼잡"}
					</p>
				</Congestion>
			</LeftDiv>
			<RightDiv>
				<ModalContainer>
					<ModalContent
						style={idx === 1 ? { marginLeft: 90 } : { marginLeft: 30 }}
						onClick={() => setVisible1(true)}
					>
						<FontAwesomeIcon icon={faMapLocationDot} />
						<p style={{ margin: 0, fontSize: 10 }}>식당위치</p>
					</ModalContent>
					<Modal visible={visible1} onClose={() => setVisible1(false)}>
						<ModalLocation restaurant={2} />
					</Modal>
					{idx !== 1 ? (
						<>
							<ModalContent
								style={{ marginLeft: 5 }}
								onClick={() => setVisible2(true)}
							>
								<FontAwesomeIcon icon={faCalendarDays} />
								<p style={{ margin: 0, fontSize: 10 }}>식단표</p>
							</ModalContent>
							<Modal visible={visible2} onClose={() => setVisible2(false)}>
								<ModalMenuTable idx={idx} />
							</Modal>
						</>
					) : null}
				</ModalContainer>
			</RightDiv>
		</DetailHeaderContainer>
	);
};

export default DetailHeader;
