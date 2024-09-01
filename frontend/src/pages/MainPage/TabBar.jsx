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

const TabButton = styled.div`
  font-weight: 700;
  cursor: pointer;
  color: #c0c0c0;
  ${({ $active }) => $active && `
    color: #111;
  `}
`;

const TabBar = ({ restaurantId = 1 ,setRestaurantId }) => {
  return (
    <Slider>
      <Container>
        <TabButton 
          $active={restaurantId === 1} 
          onClick={() => setRestaurantId(1)}
        >
          1학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 2} 
          onClick={() => setRestaurantId(2)}
        >
          2학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 3} 
          onClick={() => setRestaurantId(3)}
        >
          3학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 4} 
          onClick={() => setRestaurantId(4)}
        >
          상록회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 5} 
          onClick={() => setRestaurantId(5)}
        >
          생활과학대
        </TabButton>
      </Container>
    </Slider>
  );
}

export default TabBar;