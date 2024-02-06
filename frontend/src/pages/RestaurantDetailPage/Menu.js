import styled from "@emotion/styled";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUtensils } from "@fortawesome/free-solid-svg-icons";

const MenuContainer = styled.div`
	width: 100%;
	background-color: white;
	border-radius: 20px;
	margin-top: 15px;
	padding-bottom: 25px;
`;

const TypeName = styled.p`
	font-size: 15px;
	margin-left: 15px;
	font-weight: 700;
	color: #555555;

	::after {
		content: "";
		display: block;
		width: 50px;
		border-bottom: 3px solid #000000;
		margin: 0px 10px;
		align-items: center;
		text-align: center;
		padding-top: 2px;
	}
`;

const MainMenuContainer = styled.div`
	display: flex;
	justify-content: center;
	align-items: center;
	text-align: center;
`;

const MainMenu = styled.p`
	margin: 0;
	padding-left: 5px;
	font-size: 20px;
	text-align: center;

	&.need-underline {
		text-decoration: underline 2px;
	}
`;

const NormalMenu = styled.p`
	margin: 0;
	font-size: 15px;
	font-weight: 400;

	&.need-underline {
		text-decoration: underline 1.5px;
	}
`;

const NoMenuInfo = styled.p`
	margin: 0px;
	font-size: 18px;
`;

const Price = styled.p`
	width: 60px;
	margin: 20px 10px;
	text-align: center;
	background-color: #555555;
	color: white;
	border-radius: 5px;
	font-size: 12px;
	text-align: center;
	font-weight: 400;
`;

const Menu = ({ value }) => {
	return (
		<MenuContainer>
			<div style={{ display: "flex" }}>
				<TypeName>
					{value.dept === "STAFF" ? "교직원식당" : "학생식당"}
				</TypeName>
				<Price>{value.price}</Price>
			</div>
			<div style={{ textAlign: "center" }}>
				{value.dishList.length === 0 ? (
					<NoMenuInfo>메뉴 없음</NoMenuInfo>
				) : (
					value.dishList.map((menu, index) => {
						const containsKeyword = value.keywordList.some((keyword) =>
							menu.includes(keyword)
						);
						const isKeyword = containsKeyword ? "need-underline" : null;
						return index === 0 ? (
							<MainMenuContainer key={index}>
								<FontAwesomeIcon icon={faUtensils} fontSize={25} />
								<MainMenu className={isKeyword}>{menu}</MainMenu>
							</MainMenuContainer>
						) : (
							<NormalMenu className={isKeyword} key={index}>
								{menu}
							</NormalMenu>
						);
					})
				)}
			</div>
		</MenuContainer>
	);
};

export default Menu;
