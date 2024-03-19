import React, { useState } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import * as config from "../../config";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { setNickname } from "../../redux/slice/UserSlice";
import { showToast } from "../../redux/slice/ToastSlice";

const SetNicknameWrapper = styled.div`
  width: 100%;
  max-width: 800px;
  float: left;
  padding: 30px 20px;
  border-radius: 20px;
  background: #fff;
  margin: 0 auto;
  position: relative;
  left: 50%;
  transform: translateX(-50%);
  @media (min-width: 576px) {
    margin-top: 30px;
  }
`;

const NicknameInfo = styled.p`
  font-size: 12px;
  color: #555;
  font-weight: 300;
  margin: 0;
  @media (min-width: 576px) {
    font-size: 14px;
  }
`;

const NicknameInput = styled.input`
color: #999;
border: 1px solid #d9d9d9;
border-radius: 10px;
padding: 0 10px;
padding-right: 35px;
height: 35px;
outline: none;
float: right;
font-size: 12px;
font-weight: 400;
width: 100%;

&.success {
  border: 1px solid #52d017;
}
&::placeholder {
  font-weight: 400;
  font-size: 12px;
}
`;

const InputWrapper = styled.div`
  width: 100%;
  float: left;
  margin-bottom: 10px;
  & > input {
    width: calc(100% - 80px);
    float: left;
  }
  & > button {
    width: 70px;
    float: right;
    height: 35px;
    border-radius: 10px;
    border: none;
    background: #d9d9d9;
    font-size: 12px;
    font-weight: 400;
    color: #777;
    cursor: pointer;
  }
  @media (min-width: 576px) {
    margin: 20px 0 30px 0;
    & > input, & > button {
      font-size: 14px;
      height: 40px;
    }
    & > input {
      padding: 0 15px;
      width: calc(100% - 100px);
    }
    & > button {
      width: 90px;
    }
  }
`;

const SetButton = styled.button`
  width: 100%;
  margin-top: 5px;
  font-size: 12px;
  background: #222;
  color: #fff;
  border-radius: 10px;
  border: none;
  height: 30px;
  float: left;
  font-weight: 400;
  cursor: pointer;

  &.success {
    background: #222;
  }
  &.error {
    background: #e9e9e9;
    color: #999;
  }
`;

const NicknmaeWarning = styled.p`
  font-size: 12px;
  float: left;
  color: red;
  font-weight: 300;
  margin: 5px 0 0 0;
  @media (min-width: 576px) {
    font-size: 14px;
  }
`;

const SetNicknamePage = () => {
  const navigator = useNavigate();
	const [nicknameValue, setNicknameValue] = useState("");
  const [nicknameCheck, setNicknameCheck] = useState(false); // 중복확인 버튼 클릭 여부
  const user = useSelector((state) => state.user.value);
  const [nicknameDate, setNicknameDate] = useState(""); // 닉네임 변경 가능 날짜 [YYYY-MM-DD
  const [nicknameDateCheck, setNicknameDateCheck] = useState(true); // 닉네임 변경 가능 여부 [true: 변경 가능, false: 변경 불가능
  const dispatch = useDispatch();

  const handleInputChange = (val) => {
    setNicknameCheck(false);
    setNicknameValue(val.target.value);
  }

  const CheckNickname = () => {
    if (nicknameValue.length < 2) { // 2자 이상 입력하지 않은 경우
      dispatch(showToast({ contents: "nickname", toastIndex: 0 }));
      return;
    }
    const url = config.DEPLOYMENT_BASE_URL + `/user/nickname/check/${nicknameValue}`;
    axios.get(url)
    .then((res) => {
      if (res.data.redundancy === true) { // 중복인 경우
        dispatch(showToast({ contents: "nickname", toastIndex: 1 }));
        setNicknameCheck(false);
      } else { // 중복이 아닌 경우
        dispatch(showToast({ contents: "nickname", toastIndex: 2 }));
        setNicknameCheck(true);
      }
    }).catch(() => {
      dispatch(showToast({ contents: "error", toastIndex: 0 }));
    });
  }

  const NicknameSet = () => {
    const url = config.DEPLOYMENT_BASE_URL + `/user/nickname`;
    const Token = user.token;

    const nicknameRequestJson = {
      "token": Token,
      "nickname": nicknameValue
    }
    
		fetch(url, {
			method: "PUT",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify(nicknameRequestJson),
		})
    .then((res) => { // 닉네임 설정 완료
      if (res.status === 200) {
        setNicknameDateCheck(true);
        dispatch(setNickname(nicknameValue));
        dispatch(showToast({ contents: "nickname", toastIndex: 3 }));
        navigator("/");
      } else {
        setNicknameDateCheck(false);
        const date = new Date(res.last_update);
        date.setDate(date.getDate() + 15);
        setNicknameDate(date.toISOString().substring(0,10));
        dispatch(showToast({ contents: "nickname", toastIndex: 4 }));
      }
    }).catch((res) => { // 닉네임 설정 실패
      console.log(res);
      dispatch(showToast({ contents: "nickname", toastIndex: 4 }));
    });
  }

	return (
		<div className="App3">
      <SetNicknameWrapper>
        <p style={{margin: "0 0 10px 0", fontSize: 20}}>닉네임 설정</p>
        <NicknameInfo style={{marginBottom: "2px"}}>커뮤니티 활동을 위한 닉네임을 설정해주세요. 건너뛰기 클릭시 익명으로 처리됩니다. </NicknameInfo>
        <NicknameInfo style={{margin: "0 0 20px 0"}}>(닉네임 변경시 <span style={{fontWeight: 500}}>15일</span> 후에 변경가능합니다.)</NicknameInfo>
        <InputWrapper>
          <NicknameInput 
            type="text" 
            placeholder="2~6자 사이로 입력해주세요."
            className={nicknameCheck ? "success" : "null"}
            minLength={2} maxLength={6} 
            onChange={(val) => handleInputChange(val)
          }>
          </NicknameInput>
          <button onClick={CheckNickname}>중복확인</button>
          {!nicknameDateCheck ? 
            <NicknmaeWarning>* 닉네임은 {nicknameDate}이후부터 변경가능합니다.</NicknmaeWarning>
            : null
          }
        </InputWrapper>
        <SetButton onClick={() => {navigator("/")}} style={{border: "solid 1px #ddd", color: "#333", background: "#d9d9d9"}}>건너뛰기</SetButton>
        {
          nicknameCheck ? 
          <SetButton onClick={NicknameSet} className="success">설정완료</SetButton> : 
          <SetButton disabled className="error">설정완료</SetButton>
        }
      </SetNicknameWrapper>
		</div>
	);
};

export default SetNicknamePage;
