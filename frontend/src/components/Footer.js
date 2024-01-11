import styled from "@emotion/styled";
import React, { useState } from "react";

const FooterWrapper = styled.div`
  width: 100%;
  height: 50px;
  background-color: #333;
  text-align: center;
  position: absolute;
  bottom: 0;
`;

const ContentWrapper = styled.div`
  position: relative;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  float: left;
  & > p {
    font-weight: 400;
    margin: 0 auto;
  }
`;

const Footer = () => {
  return(
    <FooterWrapper>
      <ContentWrapper>
        <p style={{color: "#ddd", fontSize: 12}}>
          <span className="material-symbols-outlined" style={{fontSize: 15, float: "left", marginRight: 5, }}>mail</span>
          skj4244@naver.com
        </p>
        <p style={{color: "#777", fontSize: 10, paddingTop: 5, width: "100%"}}>© 2023 SeeAndYouGo v2.0.1</p>
      </ContentWrapper>
    </FooterWrapper>
  );
};

export default Footer;
