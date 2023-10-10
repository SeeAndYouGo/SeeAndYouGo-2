import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import Review from "../components/Review";
import "../App.css";
import { useParams } from "react-router-dom";

import DetailHeader from "../components/DetailHeader";
import TypeSelect from "../components/TypeSelect";
import Navigation from "../components/Navigation";

const CheckBoxInput = styled.input`
	display: none;
	&:checked + label {
		background: #111;
	}
	&:checked + label::after {
		color: white;
	}
`;
const CheckBoxLabel = styled.label`
	cursor: pointer;
	display: inline-block;
	width: 15px;
	height: 15px;
	background-color: #D9D9D9;
	position: relative;
	border-radius: 3px;
	&::after {
		content:'✔';
		color: #fff;
        font-size: 10px;
        width: 15px;
        height: 15px;
        text-align: center;
        position: absolute;
        left: 0;
        top:0;
	}
`;

function View() {
	const params = useParams();
	const restaurant = params.restaurant;
	return (
		<>
			<div className="App2">
				<DetailHeader idx={restaurant - 1} rate={50} />
				{restaurant == 1 ? null : <TypeSelect idx={restaurant} />}
				<Review />
				{/* <CheckBoxInput type="checkbox" id="check" />
				<CheckBoxLabel htmlFor="check"></CheckBoxLabel> */}
				<Navigation />
			</div>
		</>
	);
}

export default View;