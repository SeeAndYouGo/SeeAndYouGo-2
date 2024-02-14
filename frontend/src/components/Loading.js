import React from "react";
import { SyncLoader } from "react-spinners";

const Loading = () => {
	return (
		<div style={{ textAlign: "center", marginTop: 70 }}>
			<h3>잠시만 기다려주세요.</h3>
			<SyncLoader color={"#a9a9a9"} size={10} />
		</div>
	);
};

export default Loading;
