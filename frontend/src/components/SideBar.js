import React from 'react';
import { useState, useEffect, useRef } from 'react';
import styled from "@emotion/styled";
import { Link } from "react-router-dom";

const SideBarWrap = styled.div`
  width: 100%;
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
  height: 60px;
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
  font-size: 18px;
  float: left;
  background-color: #fff;
  margin: 0;
  border-radius: 10px;
  padding: 0 15px;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
  & > * {
    float: left;
    font-weight: 400;
    margin-right: 10px;
    line-height: 40px;
  }
`;

const AccountWrap = styled.div`
  width: calc(100% - 40px);
  float: left;
  top: 50%;
  transform: translateY(-50%);
  position: relative;
`;

const JoinBtn = styled.span`
  padding: 0 10px;
  border: solid 1px #777;
  border-radius: 5px;
  color: #777;
  font-size: 14px;
  line-height: 22px;
  font-weight: 400;
`;

const LogoutBtn = styled.span`
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

const SideBar = ({isOpen, setIsOpen}) => {
  const outside = useRef();
  const [loginState, setLoginState] = useState(false);
  const [nickname, setNickname] = useState("");

  const toggleMenu = () => {
    setIsOpen(false);
  };

  const loginForMemberContents = (e) => {
    // toggleMenu();
    if (loginState) {
      toggleMenu()  
    }else {
      e.preventDefault(); 
      alert("로그인이 필요한 서비스입니다.")
    }
  };

  useEffect(() => {
    const checkLogin = () => {
      if (localStorage.getItem("token")) {
        setLoginState(true);
        setNickname(localStorage.getItem("nickname") ? localStorage.getItem("nickname") : "");
      } else {
        setLoginState(false);
      }
    };
    checkLogin();
  }, []);

  return (
    <SideBarWrap ref={outside} className={isOpen ? 'open' : ''}>
      <div style={{width: "100%", margin: "0 auto", padding: "0 15px"}}>
        <div style={{width: "100%", float: "left", margin: "20px 0 10px 0"}}>
          <span
            className="material-symbols-outlined"
            style={{float: "right", fontSize: 30, cursor: "pointer"}}
            onClick={toggleMenu}
          >
            close
          </span>
        </div>
        <Title>
          <div style={{height: "100%"}}>
            <span className="material-symbols-outlined" style={{fontSize:40, lineHeight: "60px", float: "left"}}>account_circle</span>
            
            <AccountWrap>
              {
                loginState ? (
                  <>
                    <span style={{marginLeft: 10, float: "left", fontSize: 20}}>{nickname === "" ? "익명" : nickname}&nbsp;님</span>
                    <LogoutBtn onClick={() => {
                      if (window.confirm("로그아웃 하시겠습니까?") === false) return;
                      localStorage.removeItem("token");
                      localStorage.removeItem("nickname");
                      setLoginState(false);
                    }}>로그아웃</LogoutBtn>
                  </>
                  ) : (
                  <>
                    <Link to="/LoginPage" onClick={toggleMenu} style={{display: "block"}}>
                      <span style={{marginLeft: 10, float: "left", fontSize: 20}}>로그인&nbsp;</span>
                      <span className="material-symbols-outlined" style={{float: "left"}}>arrow_forward_ios</span>
                    </Link>
                    <Link to="/JoinPage" style={{color: "#777", fontSize: 14, display: "block", float: "right"}} onClick={toggleMenu}>
                      <JoinBtn style={{float: "left", fontWeight: 400}}>회원가입</JoinBtn>
                    </Link>
                  </>
                  )
              }
            </AccountWrap>
          </div>
        </Title>
        <div style={{marginBottom: 10}}>
          <span>
            •&nbsp;MEMBER
          </span>
        </div>
        <MenuList>
          <Link to="/MyReviewPage" onClick={loginForMemberContents} style={{marginBottom: 10}}>
            <MenuName>
              <span className="material-symbols-outlined" style={{fontSize: 25, marginTop: -1}}>rate_review</span>
              <span>작성한 리뷰</span>
            </MenuName>
          </Link>
          <Link to="/MyMenuPage" onClick={loginForMemberContents} style={{marginBottom: 10}}>
            <MenuName>
              <span className="material-symbols-outlined" style={{fontSize: 25}}>favorite</span>
              <span>찜한 메뉴</span>
            </MenuName>
          </Link>
        </MenuList>
        <div style={{marginBottom: 10}}>
          <span>
            •&nbsp;SERVICE
          </span>
        </div>
        <MenuList>
          <Link to="/ReviewPage" onClick={toggleMenu} style={{marginBottom: 10}}>
            <MenuName>
              <span className="material-symbols-outlined" style={{fontSize: 25, marginTop: -1}}>chat</span>
              <span>리뷰페이지</span>
            </MenuName>
          </Link>
          <Link to="/NoticePage" onClick={toggleMenu} style={{marginBottom: 10}}>
            <MenuName>
              <span className="material-symbols-outlined" style={{fontSize: 25, marginTop: -1}}>info</span>
              <span>공지사항</span>
            </MenuName>
          </Link>
        </MenuList>
      </div>
    </SideBarWrap>
  );
}

export default SideBar;