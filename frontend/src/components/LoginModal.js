import React from "react";
import styled from "@emotion/styled";
import Modal from "./Modal";

const Wrapper = styled.div`
	width: 100%;
	min-width: 300px;
	padding: 40px 32px 32px 32px;
	background: #ffffff;
	border-radius: 18px;
	display: flex;
	flex-direction: column;
	gap: 28px;
	position: relative;
`;

const CloseButton = styled.button`
	position: absolute;
	top: 5px;
	right: 5px;
	border: none;
	color: #222;
	font-size: 25px;
	cursor: pointer;
	display: flex;
  background: none;
`;

const BrandCard = styled.div`
	background: linear-gradient(145deg, #f8fbff 0%, #f2f6ff 100%);
	border-radius: 16px;
	padding: 14px 22px;
	display: flex;
	align-items: center;
	flex-direction: column;
	gap: 4px;
	box-shadow: inset 0 0 0 1px rgba(23, 198, 162, 0.08);
`;

const Logo = styled.img`
	height: 28px;
	object-fit: contain;
`;

const Description = styled.p`
	margin: 0;
	font-size: 14px;
	color: #555;
	line-height: 1.5;
	font-weight: 500;
`;

const ButtonBox = styled.div`
	display: flex;
	flex-direction: column;
	gap: 16px;
`;

const ButtonBoxHeader = styled.div`
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 15px;
	color: #303030;
`;

const SocialButton = styled.button`
	height: 48px;
	border: none;
	padding: 0;
	display: block;
	border-radius: 12px;
	overflow: hidden;
	box-shadow: 0 5px 12px rgba(0, 0, 0, 0.16);
	cursor: pointer;
	background-color: #F2F2F2;
`;

const LoginModal = ({ visible, onClose }) => {
	const KAKAO_CLIENT_ID = process.env.REACT_APP_KAKAO_RESTAPI_KEY;
	const KAKAO_REDIRECT_URI = process.env.REACT_APP_KAKAO_REDIRECT_URI;

	const GOOGLE_CLIENT_ID = process.env.REACT_APP_GOOGLE_CLIENT_ID;
	const GOOGLE_REDIRECT_URI = process.env.REACT_APP_GOOGLE_REDIRECT_URI;

	const kakaoURL = `https://kauth.kakao.com/oauth/authorize
		?client_id=${KAKAO_CLIENT_ID}
		&redirect_uri=${KAKAO_REDIRECT_URI}
		&response_type=code`;	
	const handleKakaoLogin = () => {
		window.location.href = kakaoURL;
	};

	const googleURL = `https://accounts.google.com/o/oauth2/v2/auth
		?client_id=${GOOGLE_CLIENT_ID}
		&redirect_uri=${GOOGLE_REDIRECT_URI}
		&response_type=code
		&scope=email`;
	const handleGoogleLogin = () => {
		window.location.href = googleURL;
	};

	return (
		<Modal width={420} visible={visible} onClose={onClose}>
			<Wrapper>
				<CloseButton onClick={onClose} aria-label="닫기">
					×
				</CloseButton>
				<BrandCard>
					<div>
						<Logo src="/assets/images/logo.png" alt="SeeAndYouGo" />
					</div>
					<Description>
            로그인하고 리뷰와 좋아요를 남겨보세요.
					</Description>
				</BrandCard>

				<ButtonBox>
					<ButtonBoxHeader>
						<strong>SNS 계정으로 간편하게 로그인</strong>
					</ButtonBoxHeader>
					<SocialButton onClick={handleKakaoLogin}>
						<img
							src="/assets/images/kakao_login_large_wide.png"
							alt="Kakao Login"
							style={{ width: "100%", display: "block" }}
						/>
					</SocialButton>
					<SocialButton onClick={handleGoogleLogin} style={{position: "relative"}}>
						<img
							src="/assets/images/google_login_neutral_sq_na@1x.png"
							alt="google Login"
							style={{float: "left", marginLeft: 7}}
						/>
						<div>
							<span style={{
								position: "absolute",
								transform: "translate(-65%, 30%)",
								fontFamily: "Roboto",
								fontWeight: "500",
							}}
							> Google 로그인
							</span>
						</div>
					</SocialButton>
				</ButtonBox>
			</Wrapper>
		</Modal>
	);
};

export default LoginModal;
