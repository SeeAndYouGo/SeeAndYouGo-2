import React from "react";
import { useParams } from "react-router-dom";

const ModalLocation = () => {
	const publicUrl = process.env.PUBLIC_URL;
	const params = useParams();
	const restaurant = params.restaurant;

	return (
		<div style={{ padding: 20 }}>
			<p style={{ margin: "0 0 5px 0" }}>{restaurant}학생회관 위치</p>
			<img
				src={`${publicUrl}/assets/images/maps/${restaurant}.png`}
				alt={"Loading..."}
				style={{ margin: "auto", display: "block", width: "100%" }}
			/>
		</div>
	);
};

export default ModalLocation;
