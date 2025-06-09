import React, { useEffect, useState } from "react";
import styled from "@emotion/styled";
import { useDispatch, useSelector } from "react-redux";
import { changeDept } from "../../redux/slice/DeptSlice";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";

const TabMenu = styled.ul`
	background-color: white;
	display: flex;
	list-style: none;
	border-radius: 20px;
	padding: 0 5px;
	margin: 0;

	.submenu {
		padding: 5px 10px;
		margin-right: 5px;
		font-size: 14px;
		border-radius: 20px;
		cursor: pointer;
	}

	.focused {
		font-weight: 900;
		text-decoration-line: underline;
		text-decoration-thickness: 3px;
		text-underline-offset: 8px;
	}
`;

const TypeTabMenu = ({ menu1 = [], menu2 = [], menu3 = [] }) => {
  const [currentTab, setCurrentTab] = useState(0);
  const dispatch = useDispatch();
	const nowRestaurantId = useSelector((state) => state.user).value.selectedRestaurant;
  const nowMenuType = useSelector((state) => state.menuType).value;

  const selectMenuHandler = (index) => {
    // 기존 탭과 같은 탭을 클릭했을 때는 아무것도 하지 않음
    if (currentTab === index) return;
		if (nowRestaurantId === 3 && index === 1) {
			dispatch(changeDept("STAFF"));
		} else if (nowRestaurantId === 6) {
			dispatch(changeDept("DORM_A"));
		} else {
			dispatch(changeDept("STUDENT"));
		}
    dispatch(changeMenuType(index + 1));

		if (index === 0) {
			dispatch(changeMenuInfo({mainMenuList: menu1[0]?.mainDishList || [], menuId: menu1[0]?.menuId || 0}));
		} else if (index === 1) {
			dispatch(changeMenuInfo({mainMenuList: menu2[0]?.mainDishList || [], menuId: menu2[0]?.menuId || 0}));
		} else if (index === 2) {
			dispatch(changeMenuInfo({mainMenuList: menu3[0]?.mainDishList || [], menuId: menu3[0]?.menuId || 0}));
		}

		setCurrentTab(index);
	};

	const tabMenuList = {
		2: ['조식', '중식'],
		3: ['중식', '석식'],
		4: ['중식'],
		5: ['중식'],
		6: ['조식', '중식', '석식'],
	}

  useEffect(() => {
    if (nowMenuType === 1) {
      setCurrentTab(0);
    } else if (nowMenuType === 2){
      setCurrentTab(1);
    } else if (nowMenuType === 3) {
      setCurrentTab(2);
    }
  }, [nowMenuType]);

  // 동적으로 탭 개수 결정
  const getTabNames = () => {
    // menu1, menu2, menu3의 데이터 유무에 따라 탭 이름을 동적으로 생성
    const names = [];
    if (menu1.length > 0) names.push(tabMenuList[nowRestaurantId]?.[0]);
    if (menu2.length > 0) names.push(tabMenuList[nowRestaurantId]?.[1]);
    if (menu3.length > 0) names.push(tabMenuList[nowRestaurantId]?.[2]);
    return names;
  };

  const TabMenuUl = () => {
    const tabNames = getTabNames();
    return (
      <TabMenu>
        {tabNames.map((name, idx) => (
          <li
            key={idx}
            className={currentTab === idx ? "submenu focused" : "submenu"}
            onClick={() => selectMenuHandler(idx)}
          >
            {name}
          </li>
        ))}
      </TabMenu>
    );
  };

	return (
		<div style={{marginLeft: 'auto'}}>
			<TabMenuUl />
		</div>
	);
}

export default TypeTabMenu;