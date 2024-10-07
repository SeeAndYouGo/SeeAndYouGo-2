import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import { useSelector, useDispatch } from "react-redux";
import { changeDept, changeToInitialState } from "../../redux/slice/DeptSlice";
import { logout } from "../../redux/slice/UserSlice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircleExclamation } from "@fortawesome/free-solid-svg-icons";
import MenuItem from "./MenuItem";
import * as config from "../../config";

const TabMenu = styled.ul`
	color: black;
	display: flex;
	flex-direction: row;
	align-items: center;
	list-style: none;
	border: solid 1.5px black;
	border-radius: 20px;
	padding: 5px;

	.submenu {
		padding: 5px 10px;
		margin-right: 5px;
		text-align: center;
		font-size: 10px;
		transition: 0.5s;
		border-radius: 20px;
		cursor: pointer;
	}
	.focused {
		background-color: black;
		color: white;
	}
`;

const Desc = styled.div`
	text-align: center;
`;

const MenuInfoWith2Dept = ({ idx }) => {
	const [currentTab, clickTab] = useState(0);
	const [staffMenu, setStaffMenu] = useState([]);
	const [studentMenu, setStudentMenu] = useState([]);
	const user = useSelector((state) => state.user.value);
	const token = user.token;
	const dispatch = useDispatch();

	useEffect(() => {
		dispatch(changeToInitialState());
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/daily-menu/restaurant${idx}` +
				(config.NOW_STATUS === 0 ? ".json" : `${token ? "/" + token : ""}`);

			const res = await fetch(url, {
				headers: {
					"Content-Type": "application/json",
				},
				method: "GET",
			});
			const result = await res.json();
			return result;
		};
		fetchData().then((data) => {
			const staffMenuData = data.filter((item) => item.dept === "STAFF");
			setStaffMenu(staffMenuData);
			const studentMenuData = data.filter((item) => item.dept !== "STAFF");
			setStudentMenu(studentMenuData);
		}).catch((err) => {
			console.log(err);
			dispatch(logout());
			window.location.reload();
		});
	}, [idx, token, dispatch]);

	const selectMenuHandler = (index) => {
		clickTab(index);
		dispatch(changeDept(index + 1));
	};

	const TabMenuUl = () => {
		return (
			<TabMenu>
				{studentMenu.map((nowValue, index) => (
					<li
						key={index}
						className={index === currentTab ? "submenu focused" : "submenu"}
						onClick={() => selectMenuHandler(index)}
					>
						{nowValue.dept === "STAFF" ? "교직원식당" : "학생식당"}
					</li>
				))}
				{staffMenu.map((nowValue, index) => (
					<li
						key={index}
						className={index + 1 === currentTab ? "submenu focused" : "submenu"}
						onClick={() => selectMenuHandler(index + 1)}
					>
						{nowValue.dept === "STAFF" ? "교직원식당" : "학생식당"}
					</li>
				))}

				<FontAwesomeIcon
					icon={faCircleExclamation}
					style={{ marginLeft: 5, fontSize: 12 }}
				/>
				<span style={{ fontSize: 10, marginLeft: 5, fontWeight: 400 }}>
					교직원은 학생도 이용 가능합니다.
				</span>
			</TabMenu>
		);
	};

	return (
		<div style={{ marginTop: 20 }}>
			<TabMenuUl />
			<Desc>
				{currentTab === 0
					? studentMenu.map((val, index) => {
							return <MenuItem key={index} menuData={val} restaurantNum={idx} />;
					  })
					: staffMenu.map((val, index) => {
							return <MenuItem key={index} menuData={val} />;
					  })}
			</Desc>
		</div>
	);
};

export default MenuInfoWith2Dept;
