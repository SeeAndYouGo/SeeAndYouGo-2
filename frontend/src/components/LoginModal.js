import React, { useMemo } from "react";
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

const KakaoButton = styled.button`
	background: none;
	border: none;
	padding: 0;
	width: 100%;
	display: block;
	border-radius: 12px;
	overflow: hidden;
	box-shadow: 0 5px 12px rgba(0, 0, 0, 0.16);
	cursor: pointer;
`;

const LoginModal = ({ visible, onClose }) => {
	const REDIRECT_URI = process.env.REACT_APP_KAKAO_REDIRECT_URI;
	const CLIENT_ID = process.env.REACT_APP_KAKAO_RESTAPI_KEY;

	const kakaoURL = useMemo(() => {
		if (!CLIENT_ID || !REDIRECT_URI) return "";
		return `https://kauth.kakao.com/oauth/authorize?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;
	}, [CLIENT_ID, REDIRECT_URI]);

	const handleKakaoLogin = () => {
		if (!kakaoURL) return;
		window.location.href = kakaoURL;
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
					<KakaoButton onClick={handleKakaoLogin}>
						<img
							src="/assets/images/kakao_login_large_wide.png"
							alt="Kakao Login"
							style={{ width: "100%", display: "block" }}
						/>
					</KakaoButton>
				</ButtonBox>
			</Wrapper>
		</Modal>
	);
};

export default LoginModal;
