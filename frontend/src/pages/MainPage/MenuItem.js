import React, { useState, useEffect, useRef } from "react";
import styled from "@emotion/styled";

const MenuItemContainer = styled.div`
	margin: 10px 0px;
	padding: 15px;
	border-radius: 10px;
	width: 100%;
	background-color: white;
`;

const SubMenuContent = styled.p`
	font-size: 12px;
	margin: 5px 0 10px;
	font-weight: 400;
	color: #777;
`;

const Dept = styled.div`
	padding: 3px 15px;
	font-size: 12px;
	font-weight: 400;
	background-color: #000;
	color: white;
	border-radius: 5px;
`;

const MenuPrice = styled.div`
	margin-left: 10px;
	padding: 3px 15px;
	font-size: 12px;
	font-weight: 400;
	background-color: #d9d9d9;
	border-radius: 5px;
`;
const TooltipWrapper = styled.div`
  position: relative;
  display: inline-block;
`;

const TooltipText = styled.div`
  position: absolute;
  bottom: 120%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #333;
  color: #fff;
  padding: 10px 14px;
  white-space: nowrap;
  opacity: ${({ visible }) => (visible ? 1 : 0)};
  transition: opacity 0.3s ease;
  pointer-events: none;
	border-radius: 12px;

  &::after {
    content: "";
    position: absolute;
    top: 100%;
    left: 50%;
    margin-left: -6px;
    border-width: 6px;
    border-style: solid;
    border-color: #333 transparent transparent transparent;
  }
`;

const NewBadge = styled.span`
  display: inline-block;
  background-color: black;
  color: white;
  font-weight: bold;
  font-size: 10px;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
	margin-right: 2px;
	position: relative;
	top: -3px;
`;

// TODO 추후에 seeandyougo logo를 사용한 기능이 추가될 수 있음
const TooltipComponent = ({ dishName = "", isNewDish }) => {
  const [visible, setVisible] = useState(false);
	const timerRef = useRef(null);

  const showTooltip = () => {
		if (!isNewDish) return;
    setVisible(true);

    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(() => {
      setVisible(false);
      timerRef.current = null;
    }, 1000);
  };

  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  return (
    <TooltipWrapper
      onMouseEnter={showTooltip}
      onClick={showTooltip}
    >
			{/* 2번 */}
			{isNewDish && <NewBadge>N</NewBadge>}

      <span>{dishName}</span>
      <TooltipText visible={visible}>
        새롭게 등장한 메뉴입니다.
      </TooltipText>
    </TooltipWrapper>
  );
};

const MenuItem = ({ menu, restaurantId }) => {
	const subMenu = menu.sideDishList?.slice(0).join(", ");

	const deptLabel = () => {
		if (restaurantId === 6) {
			return menu.dept === "DORM_C" ? "메뉴 C" : menu.dept === "DORM_A" ? "메뉴 A" : menu.dept;
		} else {
			return menu.dept === "STAFF" ? "교직원" : "학생";
		}
	}

	return (
		<MenuItemContainer>
			<div>
				{
					menu.mainDishList.map((dish, index) => (
						<div key={index} style={{ display: "inline-block", fontSize: "18px" }}>
							<TooltipComponent
								dishName={dish} 
								isNewDish={menu.newDishList.includes(dish)} 
							/>
							<span>
								{index < menu.mainDishList.length - 1 && ","}&nbsp;
							</span>
						</div>
					))
				}
			</div>

			<SubMenuContent>{subMenu}</SubMenuContent>
			<div style={{ display: "flex" }}>
				{restaurantId !== 6 && (
					<>
						<Dept>{deptLabel()}</Dept>
						<MenuPrice>{menu.price}</MenuPrice>
					</>
				)}
			</div>
		</MenuItemContainer>
	);
};

export default MenuItem;
