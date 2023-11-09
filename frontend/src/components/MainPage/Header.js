import styled from "@emotion/styled";

const HeaderContainer = styled.div`
	width: 100%;
	padding-top: 0;
	display: flex;
	align-items: center;
	justify-content: center;
`;

const Header = () => {
	return (
		<HeaderContainer>
			<h1 style={{ margin: 0, fontFamily: "Jua" }}>SeeAndYouGo</h1>
		</HeaderContainer>
	);
};

export default Header;
