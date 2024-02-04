import styled from "@emotion/styled";
import * as config from "../../config";

const RemoveBtn = styled.span`
	position: absolute;
	font-size: 22px;
	top: -1px;
	right: 7px;
`;

const MyKeywordItem = ({ keyword, setKeywordList, setToastDeleteSuccess, setToastDeleteFail }) => {
	const handleSubmit = async () => {
		const url = config.BASE_URL + "/keyword";

		const res = await fetch(url, {
			headers: {
				"Content-Type": "application/json",
			},
			method: "DELETE",
			body: JSON.stringify({
				keyword: keyword,
				user_id: localStorage.getItem("token"),
			}),
		});
		if (res.ok) {
			const result = await res.json();
			setKeywordList(result.keywords);
			setToastDeleteSuccess(true);
		} else {
			setToastDeleteFail(true);
		}
	};

	return (
		<div style={{ margin: "5px 0", padding: "8px 10px" }}>
			<div
				style={{
					margin: "0 0 0 5px",
					position: "relative",
					fontWeight: 500,
				}}
			>
				#&nbsp;{`${keyword}`}
				<button
					style={{
						backgroundColor: "transparent",
						cursor: "pointer",
					}}
					onClick={handleSubmit}
				>
					<RemoveBtn className="material-symbols-outlined">
						do_not_disturb_on
					</RemoveBtn>
				</button>
			</div>
		</div>
	);
};

export default MyKeywordItem;
