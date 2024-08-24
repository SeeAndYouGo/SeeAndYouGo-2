import React from "react";
import styled from "@emotion/styled";
import { Link } from "react-router-dom";



const Slider = styled.div`
  background-color: #fff;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 18px;
  font-weight: 600;
  width: 100%;
  overflow-x: scroll;
  -webkit-overflow-scrolling: touch;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  &::-webkit-scrollbar {
    display: none;
  }
`;

const Container = styled.div`
  display: flex;
  min-width: 420px;
  justify-content: space-between;
  display: flex;
`;

const TabBar = () => {
  return (
    <Slider>
      <Container>
        <Link to='/1' style={{ fontWeight: 700 }}>1학생회관</Link>
        <Link to='/2' style={{ fontWeight: 700 }}>2학생회관</Link>
        <Link to='/3' style={{ fontWeight: 700 }}>3학생회관</Link>
        <Link to='/4' style={{ fontWeight: 700 }}>상록회관</Link>
        <Link to='/5' style={{ fontWeight: 700 }}>생활과학대</Link>
      </Container>
    </Slider>
  );
}

export default TabBar;