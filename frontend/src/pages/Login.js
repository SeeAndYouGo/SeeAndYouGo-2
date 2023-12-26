import React from "react";

const Login = () => {
	// alert(`hello ${process.env.REACT_APP_KAKAO_REDIRECT_URI}, ${process.env.REACT_APP_KAKAO_REST_API_KEY}`);
	const REDIRECT_URI = process.env.REACT_APP_KAKAO_REDIRECT_URI;
	const CLIENT_ID = process.env.REACT_APP_KAKAO_RESTAPI_KEY;

	const kakaoURL = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;
	const handleLogin = () => {
		window.location.href = kakaoURL;
	};
	return (
		<>
			<div style={{ margin: "30px ", textAlign: "center"}}>
				<p>
					SeeAndYouGo에 오신 것을 환영합니다.
				</p>
			</div>
			<div style={{textAlign: "center"}}>
				<button style={{display: "inline-block"}}>
					<img
						onClick={handleLogin}
						src="/assets/images/kakao_login_medium_narrow.png"
						alt="Kakao Login"
					/>
				</button>
			</div>
		</>
	);
};

export default Login;
