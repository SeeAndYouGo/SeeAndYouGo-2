import Header from "../components/MainPage/Header";
import "../App.css";

const LoadingPage = () => {
    return (
		<div className="App">
        {/* <div className="App" style={{marginTop: 20}}> */}
            <Header />
            <h1>죄송합니다..</h1>
            <p>수정 작업중입니다.</p>
        </div>
    );
};

export default LoadingPage;