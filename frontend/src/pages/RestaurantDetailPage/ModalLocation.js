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
			return "학생생활관";
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
		</div>
	);
};

export default ModalLocation;
