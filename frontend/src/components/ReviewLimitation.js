import styled from "@emotion/styled";

const WriteImpossible = styled.div`
	position: absolute;
	width: 100%;
	height: 100%;
	left: 0px;
	top: 0px;
	background-color: rgba(20, 20, 20, 0.25);
	z-index: 6;
	border-radius: 20px;
	text-align: center;
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: 20px;
	cursor: default;
	backdrop-filter: blur(1.5px);
`;

const GoToLogin = styled.span`
	cursor: pointer;
	display: flex;
	justify-content: center;
	align-items: center;
	font-size: 16px;
	color: #222;
	& > * {
		font-weight: 400;
	}
`;

const limitaionList = [
	["", "주말에는 작성할 수 없습니다."], // 주말인 경우
	["로그인이 되어 있지 않아", "리뷰 작성이 불가능합니다."], // 로그인하지 않은 경우
	["관리자가 메인메뉴를 등록하지 않아\n 리뷰 작성 기능을 이용할 수 없습니다.", "불편을 드려 죄송합니다."], // 관리자 페이지에서 메인 메뉴 등록하지 않은 경우
	// 추후에 필요시 밑에 4번째 항목은 코드 수정 후 작업
	["휴일 또는 공휴일로 인해", "X 현재 식당을 운영하지 않습니다. X"], // 휴일 또는 공휴일인 경우
];

const ReviewLimitation = ({ num }) => {
	return (
		<WriteImpossible>
			<p style={{whiteSpace: "pre-line"}}>{limitaionList[num - 1][0]}</p>
			<p style={{whiteSpace: "pre-line"}}>{limitaionList[num - 1][1]}</p>
      {num === 2 && (
        <GoToLogin
          onClick={() => {
            window.location.href = "/login-page";
          }}
        >
          <span>로그인 하러가기</span>
          <span className="material-symbols-outlined">chevron_right</span>
        </GoToLogin>
      )}
		</WriteImpossible>
	);
};

export default ReviewLimitation;