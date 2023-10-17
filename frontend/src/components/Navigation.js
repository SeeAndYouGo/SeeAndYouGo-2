import styled from "@emotion/styled";
import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faHouse } from "@fortawesome/free-solid-svg-icons";
import { faCommentDots } from "@fortawesome/free-regular-svg-icons";
import { Link } from "react-router-dom";

const NavContainer = styled.div`
    z-index: 1;
    width: inherit;
    position: relative;
`;
const NavBox = styled.div`
    position: fixed;
    width: inherit;
    background: #fff;
    bottom: 0;
    padding: 10px 20px;
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-left: -15px;
    border-top-left-radius: 20px;
    border-top-right-radius: 20px;
`;
const NavItem = styled.div`
    font-size: 14px;
    cursor: pointer;
    & > a {
    font-size: 15px;
    }
`;

const Navigation = () => {
    return (
        <NavContainer>
            <NavBox>
                <NavItem>
                    <Link to="/">
                        <FontAwesomeIcon icon={faHouse} />
                        <span style={{fontWeight: 400}}> 메인페이지</span>
                    </Link>
                </NavItem>
                <NavItem>
                    <Link to="/ReviewPage">
                        <FontAwesomeIcon icon={faCommentDots} />
                        <span style={{fontWeight: 400}}> 리뷰페이지</span>
                    </Link>
                </NavItem>
            </NavBox>
        </NavContainer>
    );
}

export default Navigation;