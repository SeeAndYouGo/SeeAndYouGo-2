import styled from "@emotion/styled";

const ColorContainer = styled.div`
	display: flex;
	margin-left: 20px;
	border: 1px solid #dddddd;
	border-radius: 5px;
	overflow: hidden;
`;

const ColoredDiv = ({ disabledColor, color, index }) => {
	const divStyle = {
		float: "left",
		backgroundColor: disabledColor ? "#FFFFFF" : color,
		width: "14px",
		height: "12px",
		borderLeft:
			index === 0 ? "none" : disabledColor ? "1px solid #dddddd" : "none",
	};

	return <div style={divStyle}></div>;
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
