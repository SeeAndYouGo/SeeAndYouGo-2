import React from "react";

const LoginPage = () => {
	// kakao login 동작
	const REDIRECT_URI = process.env.REACT_APP_KAKAO_REDIRECT_URI;
	const CLIENT_ID = process.env.REACT_APP_KAKAO_RESTAPI_KEY;

	const kakaoURL = `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;
	const handleLogin = () => {
		window.location.href = kakaoURL;
	};

	return (
		<>
			{/* kakao login 동작 */}
			<div style={{ marginTop: 100, width: "100%", textAlign: "center" }}>
				<p>SeeAndYouGo에 오신 것을 환영합니다.</p>
			</div>
			<div style={{ textAlign: "center" }}>
				<button style={{ display: "inline-block" }}>
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

export default LoginPage;
