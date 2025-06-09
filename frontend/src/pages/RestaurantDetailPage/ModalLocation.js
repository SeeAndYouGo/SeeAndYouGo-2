import KakaoMap from "../../components/KakaoMap";

const getRestuarantIndex = (restaurantNum) => {
	switch (restaurantNum) {
		case 1:
			return "1학생회관";
		case 2:
			return "2학생회관";
		case 3:
			return "3학생회관";
		case 4:
			return "상록회관";
		case 5:
			return "생활과학대";
		case 6:
			return "기숙사식당";
		default:
			return null;
	}
};

const ModalLocation = ({ restaurant=1, isOpen }) => {
	return (
		<div style={{ padding: 20 }}>
			<p style={{ margin: "0 0 10px 0" }}>
				{getRestuarantIndex(restaurant)} 위치
			</p>
			<KakaoMap restaurantId = {restaurant} modalOpen={isOpen} />
			<div style={{ marginLeft: "5px"}}>
				<p style={{ margin: "10px 0 0 0" }}>
					지도 좌측 하단 kakao 클릭 시
				</p>
				<p>
					카카오맵에서 위치를 확인할 수 있습니다.
				</p>
			</div>
		</div>
	);
};

export default ModalLocation;
