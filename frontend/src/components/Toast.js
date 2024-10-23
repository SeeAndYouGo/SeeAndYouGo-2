import React, { useEffect } from 'react';
import styled from '@emotion/styled';
import { useDispatch } from 'react-redux';
import { changeToInitialState } from '../redux/slice/ToastSlice';

const ToastContainer = styled.div`
  position: fixed;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 250px;
  max-width: 300px;
  z-index: 1000;
`;

const ToastMessage = styled.div`
  position: relative;
  border-radius: 20px;
  color: #fff;
  padding: 5px 20px;
  font-weight: 300;
  font-size: 14px;
  text-align: center;
  white-space: pre-wrap;
  &.success {
    background-color: #28a745;
  }
  &.error {
    background-color: #f44336;
  }
  &.alert {
    background-color: #777;
  }
`;

const toastList = {
	review: [
		["success", "리뷰가 등록되었습니다."],
		["error", "리뷰 작성에 실패했습니다."],
		["alert", "리뷰 삭제 권한이 없습니다."],
		["success", "리뷰 삭제에 성공했습니다."],
		["error", "리뷰 삭제에 실패했습니다."],
		["success", "해당 리뷰 신고가 접수되었습니다!"],
		["error", "리뷰 신고에 실패했습니다."],
		["success", "리뷰를 공감했습니다."],
		["alert", "리뷰 공감을 해제했습니다."],
		["error", "내가 쓴 리뷰는 공감할 수 없습니다."],
	],
	login: [
		["alert", "로그인이 필요한 서비스입니다."],
		["success", "회원가입을 축하합니다!\n 닉네임을 설정해주세요."],
		["success", "로그인에 성공했습니다."],
		["error", "로그인에 실패했습니다."],
		["alert", "로그아웃 되었습니다."],
	],
	keyword: [
		["success", "키워드가 등록되었습니다."],
		["error", "키워드 등록에 실패했습니다."],
		["alert", "이미 등록된 키워드입니다."],
    ["alert", "키워드는 최대 10개까지\n 등록 가능합니다."],
		["alert", "키워드 조건을 확인해주세요!"],
		["success", "키워드 삭제에 성공했습니다."],
		["error", "키워드 삭제에 실패했습니다."],
	],
	nickname: [
		["alert", "닉네임은 2자 이상 입력해주세요"],
		["alert", "이미 존재하는 닉네임입니다."],
		["alert", "사용 가능한 닉네임입니다."],
		["success", "닉네임이 설정되었습니다."],
		["error", "닉네임 설정에 실패했습니다."],
	],
	admin: [["alert", "비밀번호가 틀립니다."]],
	error: [["error", "에러가 발생했습니다."]],
};

const Toast = ({ contentsName, toastIndex }) => {
  const dispatch = useDispatch();
  const nowToast = toastList[contentsName][toastIndex];

  useEffect (() => {
    const timer = setTimeout(() => {
      dispatch(changeToInitialState());
    }, 1000);
    return () => {
      clearTimeout(timer);
    };
  }, [dispatch]);

  return (
    <ToastContainer>
      <ToastMessage className={`toast-content ${nowToast[0]}`}>{nowToast[1]}</ToastMessage>
    </ToastContainer>
  );
}

export default Toast;