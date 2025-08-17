import React, { useRef, useEffect, useState } from 'react';
import styled from "@emotion/styled";
import { Link } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../redux/slice/UserSlice";
import { showToast } from '../redux/slice/ToastSlice';
import { useCookies } from 'react-cookie';
import { get } from '../api';

const Background = styled.div`
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.5);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9;
  display: none;
  &.open {
    display: block;
  }
`;

const SideBarWrap = styled.div`
  width: 85%;
  background-color: #f1f1f1;
  position: fixed;
  top: 0;
  right: 0;
  height: 100%;
  z-index: 10;
  right: -100%;
  transition: all 0.3s ease-in-out;
  &.open {
    right: 0;
  }
`;

const Title = styled.div`
  float: left;
  background-color: #fff;
  padding: 0 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  width: 100%;
  height: 50px;
  border-bottom: 1px solid #ddd;
  & * {
    margin: 0;
  }
`;

const MenuList = styled.div`
  width: 100%;
  & > a {
    width: 100%;
    float: left;
  }
`;

const MenuName = styled.p`
  width: 100%;
  font-size: 14px;
  float: left;
  background-color: #fff;
  margin: 0;
  border-radius: 10px;
  padding: 0 15px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  & > * {
    float: left;
    font-weight: 400;
    margin-right: 7px;
    line-height: 35px;
  }
`;

const AccountWrap = styled.div`
  width: calc(100% - 40px);
  height: 100%;
  float: left;
  top: 50%;
  transform: translateY(-50%);
  position: relative;
`;

const LogoutBtn = styled.span`
  top: 50%;
  transform: translateY(-50%);
  position: relative;
  padding: 0 10px;
  border: solid 1px red;
  border-radius: 5px;
  color: red;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
  cursor: pointer;
  display: block;
  float: right;
`;

const VisitorBadge = styled.div`
  float: right;
  display: inline-flex;
  position: relative;
  border-radius: 5px;
  background-color: #fff;
  overflow: hidden;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  margin-bottom: 10px;
  font-size: 12px;
  & > p {
    margin: 0;
  }
`;

const SideBar = ({isOpen, setIsOpen}) => {
  const [visitTodayData, setVisitTodayData] = useState(-1);
  const [visitTotalData, setVisitTotalData] = useState(-1);
  const dispatch = useDispatch();
  const outside = useRef();
  const user = useSelector((state) => state.user.value);
  const nickname = user.nickname;
  const loginState = user.loginState;
  const [cookies, setCookie, removeCookie] = useCookies(['refreshToken']);
  const toggleMenu = () => {
    setIsOpen(false);
  };

  const loginForMemberContents = (e) => {
    if (loginState) {
      toggleMenu()  
    }else {
      e.preventDefault();
      dispatch(showToast({ contents: "login", toastIndex: 0 }));
    }
  };

  useEffect(() => {
    const fetchVisitData = async () => {
      try {
        const response = await get(`/visitors/count`);
        console.log("방문자 데이터 확인", response.data);
        setVisitTodayData(response.data.visitToday);
        setVisitTotalData(response.data.visitTotal);
      } catch (error) {
        console.error("Error fetching JSON:", error);
      }
    }
    fetchVisitData();
  },[]);

  return (
    <>
      <Background onClick={toggleMenu} className={isOpen ? 'open' : ''}></Background>
      <SideBarWrap ref={outside} className={isOpen ? 'open' : ''}>
        <div style={{width: "100%", margin: "0 auto", padding: "0 15px", position:"relative", height: "100vh"}}>
          <div style={{width: "100%", float: "left", margin: "20px 0 10px 0"}}>
            <span
              className="material-symbols-outlined"
              style={{float: "right", fontSize: 22, cursor: "pointer"}}
              onClick={toggleMenu}
            >
              close
            </span>
          </div>
          {
            loginState ? (
              <Title>
                <div style={{height: "100%"}}>
                  <span className="material-symbols-outlined" style={{fontSize:35, lineHeight: "50px", float: "left"}}>account_circle</span>
                  <AccountWrap>
                    <span style={{marginLeft: 10, float: "left", fontSize: 18, lineHeight: "50px"}}>
                      {nickname === "" ? "익명" : nickname}&nbsp;님
                    </span>
                    <LogoutBtn onClick={() => {
                      if (window.confirm("로그아웃 하시겠습니까?") === false) return;
                      removeCookie('refreshToken', { path: '/' });
                      dispatch(logout());
                      dispatch(showToast({ contents: "login", toastIndex: 4 }));
                      setTimeout(() => {
                        window.location.reload();
                      }, 1000);
                      toggleMenu();
                    }}>로그아웃</LogoutBtn>
                  </AccountWrap>
                </div>
              </Title>
            ) :(
              <Link to="/login-page" onClick={toggleMenu} style={{display: "block"}}>
                <Title>
                  <div style={{height: "100%"}}>
                    <span className="material-symbols-outlined" style={{fontSize:35, lineHeight: "50px", float: "left"}}>account_circle</span>
                    <AccountWrap>
                      <span style={{marginLeft: 5, float: "left", fontSize: 16, lineHeight: "50px"}}>로그인&nbsp;</span>
                      <span className="material-symbols-outlined" style={{float: "right", lineHeight: "50px", fontSize: 18}}>arrow_forward_ios</span>
                    </AccountWrap>
                  </div>
                </Title>
              </Link>
            )
          }

          <div style={{marginBottom: 10}}>
            <span>
              •&nbsp;MEMBER
            </span>
          </div>
          <MenuList>
            <Link to="/set-nickname" onClick={loginForMemberContents} style={{marginBottom: 10}}>
              <MenuName>
              <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>manage_accounts</span>
                <span>닉네임 수정</span>
              </MenuName>
            </Link>
            <Link to="/my-review-page" onClick={loginForMemberContents} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>rate_review</span>
                <span>작성한 리뷰</span>
              </MenuName>
            </Link>
          </MenuList>
          <div style={{marginBottom: 10}}>
            <span>
              •&nbsp;SERVICE
            </span>
          </div>
          <MenuList>
            <Link to="/statistics" onClick={toggleMenu} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>bar_chart</span>
                <span>혼잡도 통계보기</span>
              </MenuName>
            </Link>
            <Link to="/notice-page" onClick={toggleMenu} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>info</span>
                <span>공지사항</span>
              </MenuName>
            </Link>
          </MenuList>
          <MenuList>
            <Link to="https://forms.gle/xy71uZ7gzueWcK6UA" onClick={toggleMenu} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>chat_paste_go</span>
                <span>의견 보내기</span>
              </MenuName>
            </Link>
          </MenuList>
          <VisitorBadge>
            <p style={{backgroundColor: "#333", color: "#fff", padding: "2px 10px"}}>방문자수</p>
            <p style={{fontWeight: 500, padding: "2px 10px", borderRadius: 5}}>{Number(visitTodayData).toLocaleString()} / {Number(visitTotalData).toLocaleString()}</p>
          </VisitorBadge>
        </div>
      </SideBarWrap>
    </>
  );
}

export default SideBar;