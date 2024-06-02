import React, { useEffect, useState } from "react";
import "../../App.css";
import { Link } from "react-router-dom";
import UpdateLog from "./UpdateLog";
import Cafeteria from "./Cafeteria";
import * as config from "../../config";
// import Modal from "../../components/Modal";
// import InfoModal from "../../components/InfoModal";

// const modalComment = [
//   "안녕하세요. 씨앤유고 팀입니다.",
// 	"서버 제공 측과의 소통 오류로 인해 데이터들이 모두 소실되고, 서비스가 약 1달 동안 중단되었습니다.",
// 	"기존에 저희 서비스를 이용하신 분들은 재가입이 필요한 상황입니다.",
// 	"이용에 불편을 드려 죄송합니다. 앞으로는 이런 일이 재발하지 않도록 주의하겠습니다.",
// 	"감사합니다.",
// 	"-씨앤유고 팀 일동-",
// ]

const MainPage = () => {
	// const [modalVisible, setModalVisible] = useState(false);
	const [restaurantData, setRestaurantData] = useState([]);
	const createUrl = (restaurantIdx) => config.BASE_URL + "/connection/restaurant" + restaurantIdx + (config.NOW_STATUS === 0 ? ".json" : "");
  
	// useEffect(() => {
	// 	const seeandyougoModalShown = localStorage.getItem("seeandyougoModalShown");
	// 	console.log(seeandyougoModalShown)
	// 	if(!seeandyougoModalShown) {
	// 		setModalVisible(true);
	// 	} 
	// }, []);

	// useEffect(() => {
	// 	if(modalVisible) {
	// 		document.body.style.overflow = "hidden";
	// 	} else {
	// 		document.body.style.overflow = "auto";
	// 	}
	// 	return () => {
	// 		document.body.style.overflow = "auto";
	// 	}
	// }, [modalVisible])

	useEffect(() => {
		const url = [createUrl(1), createUrl(2), createUrl(3), createUrl(4), createUrl(5)];

		Promise.all(
			url.map((path) => fetch(path).then((response) => response.json()))
		)
			.then((dataArray) => setRestaurantData(dataArray))
			.catch((error) => console.error("Error fetching JSON:", error));
	}, []);

	return (
		<div className="App">
      {/* <Modal visible={modalVisible}>
        <InfoModal comment={modalComment} setVisible={setModalVisible}/>
      </Modal> */}
			{restaurantData.map((val, idx) =>
				idx === 0 ? (
					<UpdateLog key={idx} updateTime={val.dateTime} />
				) : null
			)}
			{restaurantData.map((val, idx) => {
				return (
					<Link to={`/view/${idx + 1}`} key={idx + 1}>
						<Cafeteria
							idx={idx + 1}
							key={idx}
							value={(val.connected / val.capacity) * 100}
						/>
					</Link>
				);
			})}
		</div>
	);
}

export default MainPage;
