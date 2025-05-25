import React from "react";
import styled from "@emotion/styled";

const MenuItemContainer = styled.div`
	margin: 10px 0px;
	padding: 15px;
	border-radius: 10px;
	width: 100%;
	background-color: white;
`;

const MainMenuContent = styled.p`
  font-size: 18px;
  margin: 0;
`;

const SubMenuContent = styled.p`
	font-size: 12px;
	margin: 5px 0 10px;
	font-weight: 400;
	color: #777;
`;

const Dept = styled.div`
	padding: 3px 15px;
	font-size: 12px;
	font-weight: 400;
	background-color: #000;
	color: white;
	border-radius: 5px;
`;

const MenuPrice = styled.div`
	margin-left: 10px;
	padding: 3px 15px;
	font-size: 12px;
	font-weight: 400;
	background-color: #d9d9d9;
	border-radius: 5px;
`;


const MenuItem = ({ menu, restaurantId }) => {
	const mainMenu = menu.mainDishList?.slice(0).join(", ");
	const subMenu = menu.sideDishList?.slice(0).join(", ");

	const deptLabel = () => {
		if (restaurantId === 6) {
			return menu.dept === "DORM_C" ? "메뉴 C" : menu.dept === "DORM_A" ? "메뉴 A" : menu.dept;
		} else {
			return menu.dept === "STAFF" ? "교직원" : "학생";
		}
	}

	return (
		<MenuItemContainer>
			<MainMenuContent>{mainMenu}</MainMenuContent>
			<SubMenuContent>{subMenu}</SubMenuContent>
			<div style={{ display: "flex" }}>
				{restaurantId !== 6 && (
					<>
						<Dept>{deptLabel()}</Dept>
						<MenuPrice>{menu.price}</MenuPrice>
					</>
				)}
			</div>
		</MenuItemContainer>
	);
};

export default MenuItem;
