import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Index from './pages/index';
import View from './pages/viewPage';

const App = () => {
	return (
		<Routes>
			<Route path="/" exact element={<Index />} />
			<Route path="/View/:restaurant" element={<View />} />
		</Routes>
	);
}

export default App;
