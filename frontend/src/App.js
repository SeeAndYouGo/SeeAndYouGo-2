import { Routes, Route } from "react-router-dom";
import Index from "./pages/index";
import View from "./pages/viewPage";
import ReviewPage from "./pages/ReviewPage";
import AdminMainMenu from "./pages/AdminMainMenu";
import Login from "./pages/Login";
import KakaoCallBack from "./components/LoginPage/KakaoCallBack";
// import LoadingPage from "./pages/LoadingPage";
// import RouteChangeTracker from "./RouteChangeTracker";

const App = () => {
	// RouteChangeTracker();
	return (
		// <LoadingPage />
		<Routes>
			<Route exact path="/" element={<Index />} />
			<Route exact path="/View/:restaurant" element={<View />} />
			<Route exact path="/ReviewPage" element={<ReviewPage />} />
			<Route exact path="/Admin/MainMenu" element={<AdminMainMenu />} />
			<Route exact path="/Login" element={<Login />} />
			<Route exact path="/oauth" element={<KakaoCallBack />} />
			<Route path="*" element={<h1>404 Not Found</h1>} />
		</Routes>
	);
};

export default App;
