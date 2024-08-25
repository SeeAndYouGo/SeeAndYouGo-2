import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faAngleLeft, faMapLocationDot, faCalendarDays } from "@fortawesome/free-solid-svg-icons";
import Modal from "../../components/Modal";
import ModalLocation from "../RestaurantDetailPage/ModalLocation";
import ModalMenuTable from "../RestaurantDetailPage/ModalMenuTable";

const Container = styled.div`
	display: flex;
	flex-wrap: wrap;
  justify-content: space-between;
  margin: 15px 0;
`;

const InfoContent = styled.p`
  margin: 0;
  font-size: 13px;
  color: #777;
  font-weight: 400;
  &:first-child {
    margin-bottom: 4px;
  }
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
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
`;

const operatingTime = {
  1: [
    {
      time: '11:00-19:00'
    }
  ],
  2: [
    {
      title: '조식',
      time: '08:00-09:00'
    },
    {
      title: '중식',
      time: '11:30-14:00'
    }
  ],
  3: [
    {
      title: '중식',
      time: '11:30-14:00'
    },
    {
      title: '석식',
      time: '17:30-19:00'
    },
  ],
  4: [
    {
      title: '중식',
      time: '11:30-14:00'
    },
  ],
  5: [
    {
      title: '중식',
      time: '11:30-14:00'
    },
  ]
};

const Info = ({ idx = 1 }) => {
	const [visible1, setVisible1] = useState(false);
	const [visible2, setVisible2] = useState(false);

	return (
		<Container>
			<div>
        <InfoContent>운영시간</InfoContent>
          {operatingTime[idx]?.map((item) => <InfoContent>{item?.title} {' '} {item?.time}</InfoContent>)}
			</div>
			<div>
				<div style={{display: 'flex', marginLeft: 'auto'}}>
					<ModalContent
						style={idx === 1 ? { marginLeft: 90 } : { marginLeft: 30 }}
						onClick={() => setVisible1(true)}
					>
						<FontAwesomeIcon icon={faMapLocationDot} />
						<p style={{ fontSize: 11 }}>식당위치</p>
					</ModalContent>
					<Modal visible={visible1} onClose={() => setVisible1(false)}>
						<ModalLocation restaurant={idx} />
					</Modal>
					{idx !== 1 ? (
						<>
							<ModalContent
								style={{ marginLeft: 5 }}
								onClick={() => setVisible2(true)}
							>
								<FontAwesomeIcon icon={faCalendarDays} />
								<p style={{ fontSize: 11 }}>식단표</p>
							</ModalContent>
							<Modal visible={visible2} onClose={() => setVisible2(false)}>
								<ModalMenuTable idx={idx} />
							</Modal>
						</>
					) : null}
				</div>
			</div>
		</Container>
	);
};

export default Info;
