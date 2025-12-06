import React, { useEffect, useState } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import "swiper/css";
import styled from "@emotion/styled";
import { useDispatch } from "react-redux";
import { changeMenuType } from "../../redux/slice/MenuTypeSlice";
import { changeMenuInfo } from "../../redux/slice/NowMenuSlice";
import { changeDept } from "../../redux/slice/DeptSlice";

const Slider = styled.div`
	background-color: #fff;
	border-radius: 10px;
	padding: 10px 15px;
	font-size: 18px;
	font-weight: 600;
	width: 100%;
	overflow-x: scroll;
	touch-action: none;
	box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.1);
	&::-webkit-scrollbar {
		display: none;
	}
	-webkit-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
`;

const TabButton = styled.div`
	font-weight: 700;
	cursor: pointer;
	color: #c0c0c0;
	${({ $active }) =>
		$active &&
		`
    color: #111;
  `}
	transform: translateX(${(props) => props.slide}px);
	transition: 0.5s ease;
	margin: 0;
`;

const restaurantList = [
	"1학생회관",
	"2학생회관",
	"3학생회관",
	"상록회관",
	"생활과학대",
	"학생생활관"
];

const SwipeableTab = ({ restaurantId = 1, setRestaurantId, menuData }) => {
	const [menu, setMenu] = useState([]);
  const [swiper, setSwiper] = useState(null);
	const dispatch = useDispatch();

  useEffect(() => {
    if (swiper) {
      swiper.slideTo(restaurantId < 4 ? 0 : restaurantId - 1);
    }
  }, [restaurantId])

	const initialSetting = (numValue, deptValue) => {
		setRestaurantId(numValue);
		if (numValue === 1) {
			return;
		} else {
			dispatch(changeMenuType(1));
			dispatch(changeDept(deptValue));
		}
		if (numValue === 2) { // 2학
			const initialMenu = menu[numValue - 1].filter(
				(item) => item.dept === deptValue
			)[0];

			if(initialMenu === undefined) {
				return;
			}
			dispatch(
				changeMenuInfo({
					mainMenuList: initialMenu.mainDishList,
					menuId: initialMenu.menuId,
					menuIsOpen: initialMenu.open
				})
			);
		} else if (numValue === 3) { // 3학
			const initialMenu = menu[numValue - 1]?.filter(
				(item) => item.dept === deptValue && item.menuType === "LUNCH"
			)[0];

			if(initialMenu === undefined) {
				return;
			}
			dispatch(
				changeMenuInfo({
					mainMenuList: initialMenu.mainDishList,
					menuId: initialMenu.menuId,
					menuIsOpen: initialMenu.open
				})
			);
		} else if (numValue === 4 || numValue === 5) { // 4학, 5학
			const initialMenu = menu[numValue - 1]?.filter(
				(item) => item.menuType === "LUNCH"
			)[0];

			if(initialMenu === undefined) {
				return;
			}
			dispatch(
				changeMenuInfo({
					mainMenuList: initialMenu.mainDishList,
					menuId: initialMenu.menuId,
					menuIsOpen: initialMenu.open
				})
			);
		} else { //학생생활관
			const initialMenu = menu[numValue - 1]?.filter(
				(item) => item.menuType === "DORM_A"
			)[0];

			if(initialMenu === undefined) {
				return;
			}
			dispatch(
				changeMenuInfo({
					mainMenuList: initialMenu.mainDishList,
					menuId: initialMenu.menuId,
					menuIsOpen: initialMenu.open
				})
			);
		}
	};

	useEffect(() => {
		setMenu(menuData);
	}, [menuData]);

	const RestaurantSwiperSlide = () => {
		const result = [];

		for (let i = 0; i < restaurantList.length; i++) {
			result.push(
				<SwiperSlide key={i} className="sw-item">
					<TabButton
						$active={restaurantId === i + 1}
						onClick={() =>
							initialSetting(
								i + 1,
								i === 0 ? null : "STUDENT"
							)
						}
					>
						{restaurantList[i]}
					</TabButton>
				</SwiperSlide>
			);
		}
		return result;
	};

	return (
		<>
			<Slider>
				<Swiper
					className="sw-tap"
					style={{ textAlign: "center", fontSize: 17 }}
					initialSlide={
            restaurantId < 4 ? 0 : restaurantId - 1
          }
					speed={1000}
					slidesPerView={3.6}
          onSwiper={setSwiper}
				>
					{RestaurantSwiperSlide()}
				</Swiper>
			</Slider>
		</>
	);
};

export default SwipeableTab;
