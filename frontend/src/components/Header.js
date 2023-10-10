import styled from "@emotion/styled";

const HeaderContainer = styled.div`
	width: 100%;
	padding-top: 0;
	display: flex;
	align-items: center;
	justify-content: center;
`;

// 이미지와 로고 표시
const Header = () => {
	return (
		<HeaderContainer>
			<img
				src={"/assets/images/logo.png"}
				alt={"Loading..."}
				style={{ height: 40, marginRight: 10 }}
			/>
			<h1 style={{ margin: 0, fontFamily: "Jua" }}>See&YouGo</h1>
		</HeaderContainer>
	);
};

export default Header;
