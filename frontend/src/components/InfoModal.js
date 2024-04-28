import styled from "@emotion/styled";
import React from "react";

const CloseButton = styled.button`
  float: right;
  border: none;
  color: #000;
  z-index: -1;
  border-radius: 5px;
  box-shadow:
    -7px -7px 20px 0px #fff9,
    -4px -4px 5px 0px #fff9,
    7px 7px 20px 0px #0002,
    4px 4px 5px 0px #0001;
  transition: all 0.3s ease;
`;

const UnderLine = styled.div`
  border-bottom: solid 3px #ba946f;
  margin-bottom: 10px;
`;

const InfoModal = ({ setVisible, comment }) => {
	return (
		<div>
      <div style={{textAlign: "center"}}>
        <span style={{fontSize:24}}>
          안&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;내
        </span>
        <UnderLine />
      </div>
			<div style={{ margin: "10px 5px", fontSize: 15 }}>
				{comment.map((value, index) => {
					return (
						<p key={index} style={{ margin: 0, fontWeight: 400 }}>
							{value}
						</p>
					);
				})}
			</div>
			<UnderLine />
			<div style={{ fontSize: 13, margin: "0 5px", fontWeight: 400 }}>
				<span
					style={{ textDecoration: "underline", cursor: "pointer" }}
					onClick={() => {
            localStorage.setItem("seeandyougoModalShown", "false");
            setVisible(false);
          }}
				>
					더 이상 보지 않기
				</span>
        <CloseButton onClick={() => setVisible(false)}>닫기</CloseButton>
			</div>
		</div>
	);
};

export default InfoModal;
