import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import "rsuite/dist/rsuite-no-reset.min.css";
import { Cascader } from "rsuite";

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
			const url = "/assets/json/restaurant1-menu.json";
			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			setMenuData(data);
		});
	}, []);

	const handleMenuClick = (value) => {
		menuData.forEach((list) => {
			list.children.forEach((menu) => {
				if (menu.label === value) {
					onSelectMenu({value: value, category: list.value});
				}
			});
		});
	};

	return (
		<MenuSelectorContainer>
			<Cascader
				style={{ width: "100%", marginTop: 5 }}
				placeholder="메뉴를 선택해주세요"
				data={menuData}
				onClean={() => onSelectMenu({})}
				onChange={(value) => {
					handleMenuClick(value);
				}}
				menuWidth={150}
			/>
		</MenuSelectorContainer>
	);
};

export default MenuSelector;
