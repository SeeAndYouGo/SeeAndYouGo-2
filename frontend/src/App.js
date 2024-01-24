import React from "react";
import { Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import Footer from "./components/Footer";
import Index from "./pages/index";
import View from "./pages/viewPage";
import SetNickname from "./pages/SetNicknamePage";
import ReviewPage from "./pages/ReviewPage";
import AdminMainMenu from "./pages/AdminMainMenu";
import KakaoCallBack from "./components/LoginPage/KakaoCallBack";
import styled from "@emotion/styled";
import LoginPage from "./pages/LoginPage";
import MyReviewPage from "./pages/MyReviewPage";
import RouteChangeTracker from "./RouteChangeTracker";
import NoticePage from "./pages/NoticePage";
// import LoadingPage from "./pages/LoadingPage";

const HeaderWrapper = styled.div`
	z-index: 10;
	width: 100%;
	height: 50px;
	background-color: #222;
	position: fixed;
	top: 0;
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
`;

const App = () => {
  RouteChangeTracker();

  return (
    // <LoadingPage />
    <>
			<HeaderWrapper>
				<Header />
			</HeaderWrapper>
      <div className="pageWrapper" style={{maxWidth:"360px", margin:"0 auto", marginTop: 50}}>
        <Routes>
          <Route exact path="/" element={<Index />} />
          <Route exact path="/View/:restaurant" element={<View />} />
          <Route exact path="/ReviewPage/:restaurant" element={<ReviewPage />} />
          <Route exact path="/Admin/MainMenu" element={<AdminMainMenu />} />
          <Route exact path="/oauth" element={<KakaoCallBack />} />
          <Route exact path="/LoginPage" element={<LoginPage />} />
          <Route exact path="/SetNickname" element={<SetNickname />} />
          <Route exact path="/MyReviewPage" element={<MyReviewPage />} />
          <Route exact path="/NoticePage" element={<NoticePage />} />
          <Route path="*" element={<h1>404 Not Found</h1>} />
        </Routes>
      </div>
			<Footer />
    </>
  );
};

export default App;
