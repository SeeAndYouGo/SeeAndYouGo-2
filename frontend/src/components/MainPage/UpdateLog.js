import styled from "@emotion/styled";
import { faClock } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const UpdateLogContainer = styled.div`
	text-align: center;
	font-size: 12px;
	background-color: white;
	border-radius: 10px;
	margin: 5px 30px;
	padding: 2px;
`;

// updateTime은 'YYYY-MM-DD HH:MM:SS' 형태
// 전달받은 updateTime 값에서 시간:분 형태로 추출하여 표시
const UpdateLog = ({ updateTime }) => {
	const nowTime = updateTime.split(" ")[1].split(":").slice(0, 2).join(":");
	return (
		<UpdateLogContainer>
			<FontAwesomeIcon icon={faClock} />
			<label style={{ marginLeft: 5, fontWeight: 400 }}>
				{nowTime} 기준으로 반영된 정보입니다.
			</label>
		</UpdateLogContainer>
	);
};

export default UpdateLog;
