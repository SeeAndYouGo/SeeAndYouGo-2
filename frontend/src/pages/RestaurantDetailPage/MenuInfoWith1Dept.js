import React, { useState, useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import { changeToInitialState } from "../../redux/slice/DeptSlice";
import { logout } from "../../redux/slice/UserSlice";
import MenuItem from "./MenuItem";
import * as config from "../../config";

const MenuInfoWith1Dept = ({ idx }) => {
	const [menuData, setMenuData] = useState([]);
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
			setMenuData(data);
		}).catch((err) => {
			console.log(err);
			dispatch(logout());
			window.location.reload();
		});
	}, [idx, token, dispatch]);

	return (
		<div style={{ marginTop: 20, textAlign: "center" }}>
			{menuData.map((val, idx) => {
				return <MenuItem key={idx} menuData={val} />;
			})}
		</div>
	);
};

export default MenuInfoWith1Dept;
