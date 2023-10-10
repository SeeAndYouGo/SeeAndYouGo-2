import styled from "@emotion/styled";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faAngleLeft } from "@fortawesome/free-solid-svg-icons";
import { faClock } from "@fortawesome/free-regular-svg-icons";
import { faMapLocationDot } from "@fortawesome/free-solid-svg-icons";
import { faCalendarDays } from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";
import Modal from "./Modal";
import ModalLocation from "./ModalLocation";
import ModalMenuTable from "./ModalMenuTable";
import { Link } from "react-router-dom";

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
	text-align: center;
	padding-top: 5px;
	color: #777777;
`;

const Congestion = styled.div`
	text-align: center;
	padding-top: 15px;
	padding-left: 5px;
`;

const Location = styled.div`
	width: 50px;
	height: 50px;
	margin-top: 5px;
	margin-left: 30px;
	padding: 5px;
	text-align: center;
	background: white;
	border-radius: 5px;
	cursor: pointer;
`;

const MenuTable = styled.div`
	width: 50px;
	height: 50px;
	margin-top: 5px;
	margin-left: 5px;
	padding: 5px;
	text-align: center;
	background: white;
	border-radius: 5px;
	cursor: pointer;
`;

const operatingTime = [
	["1학생회관", "11:30-14:00"],
	["2학생회관", "11:00-13:30"],
	["3학생회관", "11:00-13:30"],
	["상록회관", "11:00-14:00"],
	["생활과학대", "11:00-14:00"],
];

const DetailHeader = ({ idx, rate }) => {
	const [visible1, setVisible1] = useState(false);
	const [visible2, setVisible2] = useState(false);

	return (
		<div style={{ display: "flex" }}>
			<div style={{ width: 160 }}>
				<CafeteriaName>
					<Link to={`/`}>
						<FontAwesomeIcon icon={faAngleLeft} />
					</Link>
					<p style={{ margin: "0px 0px 0px 5px" }}>
						{operatingTime[idx][0]}{" "}
					</p>
				</CafeteriaName>

				<TimeInfo>
					<FontAwesomeIcon icon={faClock} />
					<label style={{ marginLeft: 5 }}>
						운영 시간 {operatingTime[idx][1]}
					</label>
				</TimeInfo>
			</div>
			<Congestion>
				<img src={"/assets/images/People.png"} alt={"Loading..."} />
				<p style={{ margin: 0, fontSize: 10 }}>
					{rate < 33 ? "원활" : rate < 66 ? "보통" : "혼잡"}
				</p>
			</Congestion>

			<Location onClick={() => setVisible1(true)}>
				<FontAwesomeIcon icon={faMapLocationDot} />
				<p style={{ margin: 0, fontSize: 10 }}>식당위치</p>
			</Location>
			<Modal visible={visible1} onClose={() => setVisible1(false)}>
				<ModalLocation restaurant={2} />
			</Modal>

			<MenuTable onClick={() => setVisible2(true)}>
				<FontAwesomeIcon icon={faCalendarDays} />
				<p style={{ margin: 0, fontSize: 10 }}>식단표</p>
			</MenuTable>
			<Modal visible={visible2} onClose={() => setVisible2(false)}>
				<ModalMenuTable/>
			</Modal>
		</div>
	);
};

export default DetailHeader;
