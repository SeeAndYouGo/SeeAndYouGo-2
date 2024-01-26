import React from "react";

const ModalImageZoom = ({ imgLink }) => {
	return (
		<div style={{ padding: 15 }}>
			<p style={{ margin: "0 0 5px 0", textAlign: "center" }}>
				리뷰 이미지 확인
			</p>
			<img
				src={imgLink}
				alt={"Loading..."}
				style={{ margin: "auto", display: "block", width: "100%" }}
			/>
		</div>
	);
};

export default ModalImageZoom;
