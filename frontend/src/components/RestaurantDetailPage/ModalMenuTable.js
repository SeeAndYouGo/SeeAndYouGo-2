import styled from "@emotion/styled";
import React, { useEffect, useState } from "react";
import * as config from "../../config";

const Table = styled.table`
	width: 80%;
	border-collapse: collapse;
	text-align: center;
	margin: 0px auto;

	& tbody th,
	td {
		font-size: 14px;
	}
	&:first-of-type {
		margin-bottom: 10px;
	}
`;
const days = ["월요일", "화요일", "수요일", "목요일", "금요일"];

// props로 menu정보를 가져오는 것이 좋아보임
const ModalMenuTable = ({ idx }) => {
	const [studentMain, setStudentMain] = useState([]);
	const [staffMain, setStaffMain] = useState([]);

	useEffect(() => {
		const fetchData = async () => {
			const url =
				config.BASE_URL +
				`/weeklyMenu/restaurant${idx}` +
				(config.NOW_STATUS === 0 ? ".json" : "");

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
			if (idx === 2 || idx === 3) {
				const studentMainData = data.filter(
					(item) => item.dept === "STUDENT"
				);
				const staffMainData = data.filter(
					(item) => item.dept === "STAFF"
				);

				setStudentMain(studentMainData);
				setStaffMain(staffMainData);
			} else {
				setStudentMain(data);
			}
		});
	}, [idx]);

	const MenuTable = ({ inputValue, dept }) => {
		return (
			<>
				<Table>
					<thead style={{ border: "none" }}>
						<tr>
							<th colSpan={2} style={{ textAlign: "left" }}>
								{dept === 0 ? "학생식당" : "교직원식당"}
							</th>
						</tr>
					</thead>
					<tbody>
						{inputValue.map((nowValue, index) => {
							return (
								<tr key={index}>
									<th
										key={index + 1}
										style={{
											padding: "5px",
											border: "1px solid black",
										}}
									>
										{days[index]}
									</th>
									<td
										key={index + 2}
										style={{
											padding: "5px 20px",
											border: "1px solid black",
											fontWeight: 400,
										}}
									>
										{nowValue.dishList[0]}
									</td>
								</tr>
							);
						})}
					</tbody>
				</Table>
			</>
		);
	};

	return (
		<>
			<MenuTable inputValue={studentMain} dept={0} />
			{idx === 2 || idx === 3 ? (
				<MenuTable inputValue={staffMain} dept={1} />
			) : null}
		</>
	);
};
export default ModalMenuTable;
