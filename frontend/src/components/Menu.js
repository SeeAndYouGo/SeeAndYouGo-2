import styled from "@emotion/styled";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHeart } from "@fortawesome/free-regular-svg-icons";
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
`;

const MenuArray = [
	"김치찌개 & 계란후라이",
	"생선까스",
	"어묵채볶음",
	"건파래무침",
	"깍두기",
];

const Menu = () => {
	return (
		<MenuContainer>
			<div style={{ display: "flex" }}>
				<TypeName>학생식당</TypeName>
				<Price>4,500</Price>
			</div>
			<div style={{textAlign: "center"}}>
				{MenuArray.map((menu, index) =>
					index === 0 ? (
						<MainMenuContainer key={index}>
							<FontAwesomeIcon icon={faUtensils} fontSize={25} />
							<MainMenu key={menu}>{menu}</MainMenu>
						</MainMenuContainer>
					) : (
						<p
							key={index}
							style={{ margin: 0, fontSize: 15, fontWeight: 400 }}
						>
							{menu}
						</p>
					)
				)}
			</div>
			<FontAwesomeIcon
				icon={faHeart}
				style={{ float: "right", paddingRight: 10 }}
			/>
		</MenuContainer>
	);
};

export default Menu;
