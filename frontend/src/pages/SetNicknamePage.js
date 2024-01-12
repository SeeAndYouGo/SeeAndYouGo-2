import styled from "@emotion/styled";
import React, { useState } from "react";
import axios from "axios";
import * as config from "../config";


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
`;

const SetNicknamePage = () => {
	const [nickname, setNickname] = useState("");

  const nicknameCheck = () => {
    console.log("nickname", nickname);

    const url = config.DEPLOYMENT_BASE_URL + `/user/nickname/check/${nickname}`;

    axios.get(url)
    .then((res) => {
      console.log(res.redundancy);

      if (res.redundancy == true) {
        alert("중복된 닉네임입니다.");
      } else {
        
      }
    }).catch((err) => {
      console.log(err);
    });
  }

  const nicknameSet = () => {
    const url = config.DEPLOYMENT_BASE_URL + `/user/nickname`;

    axios.put(url)
    .then((res) => {
      console.log(res.data);
    }).catch((err) => {
      console.log(err);
    });
  }

	return (
		<div className="App3">
      <div className="setNicknameWrapper" style={{background: "#fff", padding: "30px 20px", borderRadius: 20, float: "left"}}>
        <p style={{margin: "0 0 10px 0", fontSize: 20}}>닉네임 설정</p>
        <p style={{fontSize: 12, color: "#555", fontWeight: 300, margin: "0 0 20px 0"}}>커뮤니티 활동을 위한 닉네임을 설정해주세요. 건너뛰기 클릭시 익명으로 처리됩니다.</p>
        <InputWrapper>
          <NicknameInput type="text" placeholder="닉네임 입력" minLength={2} maxLength={6} onChange={(val) => setNickname(val.target.value)}></NicknameInput>
          <button onClick={nicknameCheck}>중복확인</button>
          <p style={{fontSize: 12, float: "left", color: "#999", fontWeight: 300, margin: "5px 0 0 0"}}>* 닉네임은 2~6자 사이로 설정가능합니다.</p>
        </InputWrapper>
        <SetButton style={{border: "solid 1px #ddd", color: "#333", background: "#d9d9d9"}}>건너뛰기</SetButton>
        <SetButton onClick={nicknameSet}>설정완료</SetButton>
      </div>
		</div>
	);
};

export default SetNicknamePage;
