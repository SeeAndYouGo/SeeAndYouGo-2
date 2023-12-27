import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";

const MenuInfo = ({ mainMenu, subMenu }) => {
	const subMenuString = subMenu.join(", ");
	return (
		<>
			<p
				style={{
					fontSize: 12,
					margin: "0px 0px 4px 0px",
					color: "#555",
				}}
			>
				오늘의 메뉴
			</p>
			<p style={{ fontSize: 18, margin: 0 }}>{mainMenu}</p>
			<p
				style={{
					fontSize: 12,
					margin: 0,
					fontWeight: 400,
					color: "#777",
				}}
			>
				{subMenuString}
			</p>
		</>
	);
};

const ReviewInfoContainer = styled.div`
	margin: "10px 0px";
	padding: "15px";
	border-radius: "10px";
	width: "100%";
	background-color: "white";
`;

const ReviewInfo = ({ nowMenu }) => {
	const [menuData, setMenuData] = useState([]);

	useEffect(() => {
		nowMenu && setMenuData(nowMenu);
	}, [nowMenu]);

	return (
		<>
			<ReviewInfoContainer>
				{menuData.map((nowValue, index) => {
					return (
						<div key={index}>
							<MenuInfo
								mainMenu={nowValue.dishList[0]}
								subMenu={nowValue.dishList.slice(1)}
							/>
						</div>
					);
				})}
			</ReviewInfoContainer>
		</>
	);
};

export default ReviewInfo;
