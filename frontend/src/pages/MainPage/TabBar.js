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
  touch-action: none;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  &::-webkit-scrollbar {
    display: none;
  }
  -webkit-user-select:none;
  -moz-user-select:none;
  -ms-user-select:none;
  user-select:none
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
  transform: translateX(${(props) => props.slide}px);
  transition: 0.5s ease;
`;

const TabBar = ({ restaurantId = 1 ,setRestaurantId, menuData }) => {
  const [slider, setSlider] = useState(false);
  const [nowSlide, setNowSlide] = useState(0);
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

  useEffect(() => {
    setNowSlide(slider ? -130 : 0);
  }, [slider]);

  return (
    <>
      <div style={{display: "flex", width: 380, marginLeft: -25, position: "absolute", top: 80}}>
        {
          slider ? 
          <div className="prevBtn" onClick={() => setSlider(false)} style={{position: "absolute", left: 0, cursor: "pointer" }}>
            <span className="material-symbols-outlined" onClick={() => setSlider(false)}>chevron_left</span>
          </div>
          :
          <div className="nextBtn" onClick={() => setSlider(true)} style={{position: "absolute", right: 0, cursor: "pointer" }}>
            <span className="material-symbols-outlined" onClick={() => setSlider(true)}>chevron_right</span>
          </div>
        }
      </div>
      <Slider>
        <Container>
          <TabButton
            slide={nowSlide}
            $active={restaurantId === 1} 
            onClick={() => initialSetting(1)}
          >
            1학생회관
          </TabButton>
          <TabButton
            slide={nowSlide}
            $active={restaurantId === 2} 
            onClick={() => initialSetting(2, "BREAKFAST")}
          >
            2학생회관
          </TabButton>
          <TabButton
            slide={nowSlide}
            $active={restaurantId === 3} 
            onClick={() => initialSetting(3, "LUNCH")}
          >
            3학생회관
          </TabButton>
          <TabButton
            slide={nowSlide}
            $active={restaurantId === 4} 
            onClick={() => initialSetting(4, "LUNCH")}
          >
            상록회관
          </TabButton>
          <TabButton
            slide={nowSlide}
            $active={restaurantId === 5} 
            onClick={() => initialSetting(5, "LUNCH")}
          >
            생활과학대
          </TabButton>
        </Container>
      </Slider>
    </>
  );
}

export default TabBar;