import React from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faAngleLeft } from "@fortawesome/free-solid-svg-icons";
import { Link } from "react-router-dom";
import styled from "@emotion/styled";

const HeaderContainer = styled.div`
	display: flex;
	align-items: center;
	text-align: center;
	font-size: 26px;
	font-weight: bold;
    height: 40px;
`;

const ReviewHeader = () => {
	return (
		<>
            <HeaderContainer>
                <Link to={`/`}>
                    <FontAwesomeIcon icon={faAngleLeft} />
                </Link>
                <p style={{margin:"0px", marginLeft:10}}>오늘의 리뷰</p>
            </HeaderContainer>
		</>
	);
};

export default ReviewHeader;