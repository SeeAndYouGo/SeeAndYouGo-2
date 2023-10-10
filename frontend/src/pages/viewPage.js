import React, { useEffect, useState } from "react";
import Review from "../components/Review";
import "../App.css";
import { useParams } from "react-router-dom";

import DetailHeader from "../components/DetailHeader";
import TypeSelect from "../components/TypeSelect";


function View() {
	const params = useParams();
	const restaurant = params.restaurant;
	return(
		<>
			<div className="App2">
				<DetailHeader idx={restaurant-1} rate={50}/>
				{restaurant == 1 ? null : <TypeSelect idx={restaurant} />}
				<Review />
			</div>
		</>
	);
}

export default View;