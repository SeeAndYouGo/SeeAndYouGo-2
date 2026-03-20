import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import "rsuite/dist/rsuite-no-reset.min.css";
import { Cascader } from "rsuite";
import { get } from "../../api/index";

const MenuSelectorContainer = styled.div`
	width: 100%;
	float: left;
	display: block;
	margin: 0 0 5px 0;
	& .rs-picker-toggle {
		border-radius: 10px;
	}
`;

const MenuSelector = ({ onSelectMenu }) => {
	const [menuData, setMenuData] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			const response = await get("/restaurant1-menu");
			const result = response.data;
			const formatted = result.map((dept) => ({
				label: dept.deptKo,
				value: dept.deptEn,
				children: dept.menus.map((menu) => ({
					label: menu.name,
					value: menu.name,
					price: menu.price,
				})),
			}));

			setMenuData(formatted);
		};

		fetchData();
	}, []);

	const handleMenuClick = (value) => {
		if (!value) return;
		
		const category = menuData.find((list) =>
			list.children.some((menu) => menu.label === value)
		)?.value;

		if (category) {
			onSelectMenu({ value, category });
		}
	};

	return (
		<MenuSelectorContainer>
			<Cascader
				style={{ width: "100%", marginTop: 5 }}
				placeholder="메뉴를 선택해주세요"
				data={menuData}
				onClean={() => onSelectMenu({})}
				onChange={handleMenuClick}
				menuWidth={150}
			/>
		</MenuSelectorContainer>
	);
};

export default MenuSelector;
