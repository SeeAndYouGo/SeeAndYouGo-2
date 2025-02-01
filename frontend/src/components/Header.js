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
  @media (min-width: 576px) {
    max-width: 992px;
  }
`;

const MenuWrapper = styled.div`
  position: absolute;
  right: 15px;
  height: 100%;
  display: flex;
  align-items: center;
  & > .event-icon {
    background-color: #d32f2f;
    border-radius: 50%;
    color: white;
    font-size: 13px;
    padding: 3px;
    position: absolute;
    right: -12px;
    top: 5px;
    border: 3px solid #222;
  }
`;


const Header = () => {
  const [isOpen, setIsOpen] = useState(false);
  const toggleMenu = () => {
    setIsOpen(true);
    document.body.style.overflow = "hidden";
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
        <MenuWrapper>
          <span
            onClick={toggleMenu}
            className="material-symbols-outlined"
            style={{color: "white", fontSize: 32, cursor: "pointer"}}
          >
            menu
          </span>
          {/* <span class="material-symbols-outlined event-icon">
            celebration
          </span> */}
        </MenuWrapper>
      </HeaderContainer>
      <SideBar isOpen={isOpen} setIsOpen={setIsOpen} />
    </>
  );
};

export default Header;
