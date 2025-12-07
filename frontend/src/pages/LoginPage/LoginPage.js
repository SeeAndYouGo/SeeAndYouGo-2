import React from "react";

const LoginPage = () => {
	const KAKAO_CLIENT_ID = process.env.REACT_APP_KAKAO_RESTAPI_KEY;
	const KAKAO_REDIRECT_URI = process.env.REACT_APP_KAKAO_REDIRECT_URI;

	const kakaoURL = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${KAKAO_REDIRECT_URI}&response_type=code`;
	const handleKakaoLogin = () => {
		window.location.href = kakaoURL;
	};

	const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID;
  const GOOGLE_REDIRECT_URI = process.env.REACT_APP_GOOGLE_REDIRECT_URI;

  const googleURL = `https://accounts.google.com/o/oauth2/v2/auth?
			client_id=${GOOGLE_CLIENT_ID}
			&redirect_uri=${GOOGLE_REDIRECT_URI}
			&response_type=code
			&scope=email`;
	const handleGoogleLogin = () => {
		window.location.href = googleURL;
	};

	return (
		<>
			<div style={{ marginTop: 100, width: "100%", textAlign: "center" }}>
				<p>SeeAndYouGo에 오신 것을 환영합니다.</p>
			</div>
			<div style={{ textAlign: "center" }}>
				<button style={{ display: "inline-block" }}>
					<img
						onClick={handleKakaoLogin}
						src="/assets/images/kakao_login_medium_narrow.png"
						alt="Kakao Login"
					/>
				</button>
				<button style={{ display: "inline-block" }}>
					<img
						onClick={handleGoogleLogin}
						src="/assets/images/google_login_neutral_sq_SI@1x.png"
						alt="Google Login"
					/>
				</button>
			</div>
		</>
	);
};

export default LoginPage;
