import styled from "@emotion/styled";
import React from "react";

const FooterWrapper = styled.div`
  width: 100%;
  height: 50px;
  background-color: #222;
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

const FooterMail = styled.p`
  color: #ddd;
  font-size: 12px;
  float: left;
  left: 50%;
  transform: translateX(-50%);
  position: relative;
  & > span {
    font-size: 15px;
    float: left;
    margin-right: 5px;
  }`;

const Footer = () => {
  return(
    <FooterWrapper>
      <ContentWrapper>
        <FooterMail>
          <span className="material-symbols-outlined">mail</span>
          skj4244@naver.com
        </FooterMail>
        <p style={{color: "#777", fontSize: 10, paddingTop: 5, width: "100%", float: "left"}}>© 2023. SeeAndYouGo. All rights reserved.</p>
      </ContentWrapper>
    </FooterWrapper>
  );
};

export default Footer;
