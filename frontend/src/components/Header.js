import styled from "@emotion/styled";
import React, { useState } from "react";
import SideBar from "./SideBar";
import { Link } from "react-router-dom";

const HeaderContainer = styled.div`
  max-width: 360px;
	margin: 0 auto;
  padding-top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
	line-height: 50px;
`;

const Header = () => {
  const [isOpen, setIsOpen] = useState(false);
  const toggleMenu = () => {
    setIsOpen(true);
  };
  return (
    <>
      <HeaderContainer>
        <h1 style={{ margin: 0, fontSize: 28 }}>
					<Link to="/" style={{fontFamily: "Jua", color: "#fff", fontWeight: 500}}>
          	SeeAndYouGo
					</Link>
        </h1>
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
