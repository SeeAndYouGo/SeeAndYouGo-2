import styled from "@emotion/styled";

const ColorContainer = styled.div`
	width: calc(100% - 50px);
	display: flex;
	margin-left: 10px;
	border: 1px solid #dddddd;
	border-radius: 5px;
	overflow: hidden;
`;

const ColorDivItem = styled.div`
	float: left;
	width: 15px;
	@media (min-width: 576px) {
		width: calc(100% / 10);
	}
`;

const ColoredDiv = ({ disabledColor, color, index }) => {
	const divStyle = {
		backgroundColor: disabledColor ? "#FFFFFF" : color,
		borderLeft:
			index === 0 ? "none" : disabledColor ? "1px solid #dddddd" : "none",
	};

	// if (window.matchMedia("(min-width: 576px)").matches) {
  //   divStyle.width = "calc((100% - 120px) / 10)"
  // }

	return <ColorDivItem style={divStyle}></ColorDivItem>;
};

const colors = [
	"#14F10F",
	"#61FF00",
	"#9EFF00",
	"#EBFF00",
	"#FFF500",
	"#FFD600",
	"#FFB800",
	"#FF8A00",
	"#FF5C00",
	"#FF0000",
];

// 입력받은 value값을 기준으로 색상을 표시
const MyProgress = ({ value }) => {
	return (
		<ColorContainer>
			{colors.map((color, index) => (
				<ColoredDiv
					key={index}
					disabledColor={
						index === 0 ? false : Math.round(value / 10) < index + 1
					}
					color={color}
					index={index}
				/>
			))}
		</ColorContainer>
	);
};

export default MyProgress;
