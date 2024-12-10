import React, { useState } from "react";
import styled from "@emotion/styled";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMapLocationDot, faCalendarDays } from "@fortawesome/free-solid-svg-icons";
import Modal from "../../components/Modal";
import ModalLocation from "../RestaurantDetailPage/ModalLocation";
import MenuTableModal from "./MenuTableModal";

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
  &:first-of-type {
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
  const [visible, setVisible] = useState({ map: false, menu: false });

	return (
		<Container>
			<div>
        <InfoContent>운영시간</InfoContent>
          {operatingTime[idx]?.map((item, index) => <InfoContent key={index}>{item?.title} {' '} {item?.time}</InfoContent>)}
			</div>
			<div>
				<div style={{display: 'flex', marginLeft: 'auto'}}>
        {idx !== 1 ? (
						<>
							<ModalContent
								style={{ marginRight: 5 }}
								onClick={() => setVisible({...visible, menu: true})}
							>
								<FontAwesomeIcon icon={faCalendarDays} />
								<p style={{ fontSize: 11 }}>식단표</p>
							</ModalContent>
              <Modal visible={visible.menu} onClose={() => setVisible({...visible, menu: false})}>
								<MenuTableModal idx={idx} onClose={() => setVisible({...visible, menu: false})} />
							</Modal>
						</>
					) : null}
					<ModalContent
            onClick={() => setVisible({...visible, map: true})}
					>
						<FontAwesomeIcon icon={faMapLocationDot} />
						<p style={{ fontSize: 11 }}>식당위치</p>
					</ModalContent>
          <Modal visible={visible.map} onClose={() => setVisible({...visible, map: false})}>
						<ModalLocation restaurant={idx} />
					</Modal>
				</div>
			</div>
		</Container>
	);
};

export default Info;
