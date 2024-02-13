import React, { useState } from "react";
import styled from "@emotion/styled";
import axios from "axios";
import * as config from "../../config";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { setNickname } from "../../redux/slice/UserSlice";
import { showToast } from "../../redux/slice/ToastSlice";

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
    width: 210px;
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

const SetNicknamePage = () => {
  const navigator = useNavigate();
	const [nicknameValue, setNicknameValue] = useState("");
  const [nicknameCheck, setNicknameCheck] = useState(false); // 중복확인 버튼 클릭 여부
  const user = useSelector((state) => state.user.value);
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
    .then(() => { // 닉네임 설정 완료
      dispatch(setNickname(nicknameValue));
      dispatch(showToast({ contents: "nickname", toastIndex: 3 }));
      navigator("/");
    }).catch(() => { // 닉네임 설정 실패
      dispatch(showToast({ contents: "nickname", toastIndex: 4 }));
    });
  }

	return (
		<div className="App3">
      <div className="setNicknameWrapper" style={{background: "#fff", padding: "30px 20px", borderRadius: 20, float: "left"}}>
        <p style={{margin: "0 0 10px 0", fontSize: 20}}>닉네임 설정</p>
        <p style={{fontSize: 12, color: "#555", fontWeight: 300, margin: "0 0 20px 0"}}>커뮤니티 활동을 위한 닉네임을 설정해주세요. 건너뛰기 클릭시 익명으로 처리됩니다.</p>
        <InputWrapper>
          <NicknameInput 
            type="text" 
            placeholder="닉네임 입력"
            className={nicknameCheck ? "success" : "null"}
            minLength={2} maxLength={6} 
            onChange={(val) => handleInputChange(val)
          }>
          </NicknameInput>
          <button onClick={CheckNickname}>중복확인</button>
          <p style={{fontSize: 12, float: "left", color: "#999", fontWeight: 300, margin: "5px 0 0 0"}}>* 닉네임은 2~6자 사이로 설정가능합니다.</p>
        </InputWrapper>
        <SetButton onClick={() => {navigator("/")}} style={{border: "solid 1px #ddd", color: "#333", background: "#d9d9d9"}}>건너뛰기</SetButton>
        {
          nicknameCheck ? 
          <SetButton onClick={NicknameSet} className="success">설정완료</SetButton> : 
          <SetButton disabled className="error">설정완료</SetButton>
        }
      </div>
		</div>
	);
};

export default SetNicknamePage;
