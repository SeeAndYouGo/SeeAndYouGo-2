import { Routes, Route } from 'react-router-dom';
import Index from './pages/index';
import View from './pages/viewPage';
import ReviewPage from './pages/ReviewPage'

const App = () => {
	return (
		<Routes>
			<Route path="/" exact element={<Index />} />
			<Route path="/View/:restaurant" element={<View />} />
			<Route path="/ReviewPage" element={<ReviewPage />} />
		</Routes>
	);
}

export default App;
