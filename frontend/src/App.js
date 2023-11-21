import { Routes, Route } from "react-router-dom";
import Index from "./pages/index";
import View from "./pages/viewPage";
import ReviewPage from "./pages/ReviewPage";
import AdminMainMenu from "./pages/AdminMainMenu";
import RouteChangeTracker from "./RouteChangeTracker";


const App = () => {
	RouteChangeTracker();
	return (
		<Routes>
			<Route exact path="/" element={<Index />} />
			<Route exact path="/View/:restaurant" element={<View />} />
			<Route exact path="/ReviewPage" element={<ReviewPage />} />
			<Route exact path="/Admin/MainMenu" element={<AdminMainMenu />} />
			<Route path="*" element={<h1>404 Not Found</h1>} />
		</Routes>

	);
};

export default App;
