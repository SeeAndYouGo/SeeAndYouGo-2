import styled from "@emotion/styled";
import React, { useState } from "react";
import SideBar from "./SideBar";
import { Link } from "react-router-dom";

const HeaderContainer = styled.div`
  max-width: 360px;
  height: inherit;
	margin: 0 auto;
  padding-top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
	line-height: 50px;
`;

const Logo = styled.img`

`;

const Header = () => {
  const [isOpen, setIsOpen] = useState(false);
  const toggleMenu = () => {
    setIsOpen(true);
  };
  return (
    <>
      <HeaderContainer>
        <Link 
          to="/" 
          style={{display: "block", padding: "10px 0", height: "100%"}}
        >
          <img 
            src={"/assets/images/logo_white.png"} 
            alt={"Logo"}
            style={{height: "100%"}}
          />
        </Link>
        <span
          onClick={toggleMenu}
          className="material-symbols-outlined"
          style={{
            position: "absolute",
            right: 15,
            fontSize: 30,
            cursor: "pointer",
						color: "#fff"
          }}
        >
          menu
        </span>
      </HeaderContainer>
      <SideBar isOpen={isOpen} setIsOpen={setIsOpen} />
    </>
  );
};

export default Header;
