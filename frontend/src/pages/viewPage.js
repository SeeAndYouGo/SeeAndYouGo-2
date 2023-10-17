import React from "react";
import Review from "../components/Review";
import "../App.css";
import { useParams } from "react-router-dom";
import DetailHeader from "../components/DetailHeader";
import TypeSelect from "../components/TypeSelect";
import Navigation from "../components/Navigation";


function View() {
	const params = useParams();
	const restaurant = parseInt(params.restaurant);
	return (
		<>
			<div className="App2">
				<DetailHeader idx={restaurant - 1} rate={50} />
				{restaurant === 1 ? null : <TypeSelect idx={restaurant} />}
				<Review />
				<Navigation />
			</div>
		</>
	);
}

export default View;