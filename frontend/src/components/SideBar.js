import React from 'react';
import { useRef } from 'react';
import styled from "@emotion/styled";
import { Link } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../redux/slice/UserSlice";

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

const SideBar = ({isOpen, setIsOpen}) => {
  const dispatch = useDispatch();
  const outside = useRef();
  // const [loginState, setLoginState] = useState(false);
  // const [nickname, setNickname] = useState("");
  const user = useSelector((state) => state.user.value);
  const nickname = user.nickname;
  const loginState = user.loginState;

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

  // useEffect(() => {
  //   const checkLogin = () => {
  //     if (localStorage.getItem("token")) {
  //       setLoginState(true);
  //       setNickname(localStorage.getItem("nickname") ? localStorage.getItem("nickname") : "");
  //     } else {
  //       setLoginState(false);
  //     }
  //   };
  //   checkLogin();
  // }, []);

  return (
    <>
      <Background onClick={toggleMenu} className={isOpen ? 'open' : ''}></Background>
      <SideBarWrap ref={outside} className={isOpen ? 'open' : ''}>
        <div style={{width: "100%", margin: "0 auto", padding: "0 15px"}}>
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
                      dispatch(logout());
                    }}>로그아웃</LogoutBtn>
                  </AccountWrap>
                </div>
              </Title>
            ) :(
              <Link to="/LoginPage" onClick={toggleMenu} style={{display: "block"}}>
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
            <Link to="/MyReviewPage" onClick={loginForMemberContents} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>rate_review</span>
                <span>작성한 리뷰</span>
              </MenuName>
            </Link>
            <Link to="/MyMenuPage" onClick={loginForMemberContents} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20}}>favorite</span>
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
            <Link to="/ReviewPage/0" onClick={toggleMenu} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>chat</span>
                <span>리뷰페이지</span>
              </MenuName>
            </Link>
            <Link to="/NoticePage" onClick={toggleMenu} style={{marginBottom: 10}}>
              <MenuName>
                <span className="material-symbols-outlined" style={{fontSize: 20, marginTop: -1}}>info</span>
                <span>공지사항</span>
              </MenuName>
            </Link>
          </MenuList>
        </div>
      </SideBarWrap>
    </>
  );
}

export default SideBar;