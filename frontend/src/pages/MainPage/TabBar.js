import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeMenuInfo, changeToInitialState } from "../../redux/slice/NowMenuSlice";
import { changeDept } from "../../redux/slice/DeptSlice";

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

const TabBar = ({ restaurantId = 1 ,setRestaurantId, menuData }) => {
  const [menu, setMenu] = useState([]);
	const dispatch = useDispatch();

  const initialSetting = ( numValue, typeValue ) => {
    setRestaurantId(numValue);
    if (numValue === 1) {
      dispatch(changeToInitialState());
      return;
    } else {
      dispatch(changeMenuType(typeValue));
      dispatch(changeDept(1));
    }
    if (numValue === 2) {
      const initialMenu = menu[numValue - 1].filter(item => item.menuType === typeValue)[0];
      dispatch(changeMenuInfo({mainMenuList: initialMenu.mainDishList, menuId: initialMenu.menuId}));
    } else {
      const initialMenu = menu[numValue - 1].filter(item => item.dept === "STUDENT")[0];
      dispatch(changeMenuInfo({mainMenuList: initialMenu.mainDishList, menuId: initialMenu.menuId}));
    }
  }

  useEffect(() => {
    setMenu(menuData);
  }, [menuData]);

  return (
    <Slider>
      <Container>
        <TabButton 
          $active={restaurantId === 1} 
          onClick={() => initialSetting(1)}
        >
          1학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 2} 
          onClick={() => initialSetting(2, "BREAKFAST")}
        >
          2학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 3} 
          onClick={() => initialSetting(3, "LUNCH")}
        >
          3학생회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 4} 
          onClick={() => initialSetting(4, "LUNCH")}
        >
          상록회관
        </TabButton>
        <TabButton 
          $active={restaurantId === 5} 
          onClick={() => initialSetting(5, "LUNCH")}
        >
          생활과학대
        </TabButton>
      </Container>
    </Slider>
  );
}

export default TabBar;