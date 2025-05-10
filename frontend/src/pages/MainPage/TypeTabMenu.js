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

const TypeTabMenu = ({ menu1 = [], menu2 = [] }) => {
  const [currentTab, setCurrentTab] = useState(0);
  const dispatch = useDispatch();
	const nowRestaurantId = useSelector((state) => state.user).value.selectedRestaurant;
  const nowMenuType = useSelector((state) => state.menuType).value;

  const selectMenuHandler = (index) => {
    // 기존 탭과 같은 탭을 클릭했을 때는 아무것도 하지 않음
		if (currentTab === index) return;
		if (nowRestaurantId === 3 && index === 1) {
			dispatch(changeDept("STAFF"));
		} else {
			dispatch(changeDept("STUDENT"));
		}
    dispatch(changeMenuType(index + 1));

		if (index === 0) {
			// 첫번째 탭 선택 시 menu1의 dishList로 menuList 업데이트
			dispatch(changeMenuInfo({mainMenuList: menu1[0]?.mainDishList || [], menuId: menu1[0]?.menuId || 0}));
		} else if (index === 1) {
			// 두번째 탭 선택 시 menu2의 dishList로 menuList 업데이트
			dispatch(changeMenuInfo({mainMenuList: menu2[0]?.mainDishList || [], menuId: menu2[0]?.menuId || 0}));
		}

		setCurrentTab(index);
	};

	const tabMenuList = {
		2: ['조식', '중식'],
		3: ['중식', '석식'],
		6: ['DORM_A', 'DORM_C'],
	}

  useEffect(() => {
    if (nowMenuType === 1) {
      setCurrentTab(0);
    } else if (nowMenuType === 2){
      setCurrentTab(1);
    }
  }, [nowMenuType]);


  const TabMenuUl = () => {
		return (
      <TabMenu>
        <li
          className={currentTab === 0 ? "submenu focused" : "submenu"}
          onClick={() => selectMenuHandler(0)}
        >
          {tabMenuList[nowRestaurantId][0]}
        </li>
        <li
          className={currentTab === 1 ? "submenu focused" : "submenu"}
          onClick={() => selectMenuHandler(1)}
        >
          {tabMenuList[nowRestaurantId][1]}
        </li>
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