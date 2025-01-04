import React from "react";

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

const ModalLocation = ({ restaurant = 1 }) => {
	return (
		<div style={{ padding: 20 }}>
			<p style={{ margin: "0 0 5px 0" }}>
				{getRestuarantIndex(restaurant)} 위치
			</p>
			<img
				src={`/assets/images/maps/${restaurant}.png`}
				alt={"Loading..."}
				style={{ margin: "auto", display: "block", width: "100%" }}
			/>
		</div>
	);
};

export default ModalLocation;
