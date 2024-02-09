import React from "react";
import MenuInfoForRestaurant1 from "./MenuInfoForRestaurant1";
import MenuInfoWith1Dept from "./MenuInfoWith1Dept";
import MenuInfoWith2Dept from "./MenuInfoWith2Dept";

const MenuInfo = ({ idx }) => {
	return (
		<>
			{idx === 1 ? (
				<MenuInfoForRestaurant1 />
			) : idx === 2 || idx === 3 ? (
				<MenuInfoWith2Dept idx={idx} />
			) : (
				<MenuInfoWith1Dept idx={idx} />
			)}
		</>
	);
};

export default MenuInfo;
