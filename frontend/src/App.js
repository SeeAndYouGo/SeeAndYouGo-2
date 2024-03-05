import React from "react";
import styled from "@emotion/styled";
import RouteChangeTracker from "./RouteChangeTracker";
import { Routes, Route } from "react-router-dom";
import { useSelector } from "react-redux";
import Header from "./components/Header";
import Footer from "./components/Footer";
import MainPage from "./pages/MainPage/MainPage";
import RestaurantDetailPage from "./pages/RestaurantDetailPage/RestaurantDetailPage";
import SetNicknamePage from "./pages/SetNicknamePage/SetNicknamePage";
import ReviewPage from "./pages/ReviewPage/ReviewPage";
import SetMainMenuPage from "./pages/AdminPage/SetMainMenuPage";
import KakaoCallBack from "./pages/LoginPage/KakaoCallBack";
import LoginPage from "./pages/LoginPage/LoginPage";
import MyReviewPage from "./pages/MyReviewPage/MyReviewPage";
import NoticePage from "./pages/NoticePage/NoticePage";
import MyKeywordPage from "./pages/MyKeywordPage/MyKeywordPage";
import Toast from "./components/Toast";

const HeaderWrapper = styled.div`
	z-index: 10;
	width: 100%;
	height: 50px;
	background-color: #222;
	position: fixed;
	top: 0;
	box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
`;
const PageWrapper = styled.div`
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
  margin-top: 50px;
  @media (min-width: 576px) {
    max-width: 992px;
  }
`;

const App = () => {
  const toast = useSelector((state) => state.toast.value);
	const toastIndex = toast.toastIndex;
	const contents = toast.contents;

  RouteChangeTracker();

  return (
		<>
			{ toastIndex !== null && ( <Toast contentsName={contents} toastIndex={toastIndex} />) }
			<HeaderWrapper>
				<Header />
			</HeaderWrapper>
      <PageWrapper>
        <Routes>
          <Route exact path="/" element={<MainPage />} />
          <Route exact path="/view/:restaurant" element={<RestaurantDetailPage />} />
          <Route exact path="/review-page/:restaurant" element={<ReviewPage />} />
          <Route exact path="/admin/main-menu" element={<SetMainMenuPage />} />
          <Route exact path="/oauth" element={<KakaoCallBack />} />
          <Route exact path="/login-page" element={<LoginPage />} />
          <Route exact path="/set-nickname" element={<SetNicknamePage />} />
          <Route exact path="/my-review-page" element={<MyReviewPage />} />
          <Route exact path="/notice-page" element={<NoticePage />} />
          <Route exact path="/my-keyword-page" element={<MyKeywordPage />} />
          <Route path="*" element={<h1>404 Not Found</h1>} />
        </Routes>
      </PageWrapper>
			<Footer />
		</>
	);
};

export default App;
