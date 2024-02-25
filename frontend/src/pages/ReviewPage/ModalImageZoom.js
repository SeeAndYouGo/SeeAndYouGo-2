import React from "react";

const ModalImageZoom = ({ imgLink }) => {
	return (
		<div style={{ padding: 15 }}>
			<img
				src={imgLink}
				alt={"Loading..."}
				style={{ margin: "auto", display: "block", width: "100%" }}
			/>
		</div>
	);
};

export default ModalImageZoom;
