import axios from "axios";
import React, { useEffect } from "react";
import * as config from "../../config";
import { useNavigate } from "react-router-dom";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");
	console.log("code확인: ", code);

	useEffect(() => {
		const getJWTToken = async (authorizationCode) => {
			const url =
				config.DEPLOYMENT_BASE_URL +
				`/auth/kakao?code=${authorizationCode}`;
			// 일단 POST 요청으로 보내보기
			const response = await axios({
				method: "POST",
				url: url,
				data: {
					authorizationCode: authorizationCode,
				},
				headers: {
					"Content-Type": "application/json",
				},
			});
			const nowToken = response.data;

			return nowToken;
		};
		getJWTToken(code)
			.then((data) => {
				console.log("JWT Token 확인합니다", data);
				alert("로그인에 성공했습니다.")
				localStorage.setItem("loginToken", data);
				navigator("/");
			})
			.catch((err) => {
				console.log(err);
				alert("로그인에 실패했습니다.");
				localStorage.setItem("loginToken", "not setting");
				navigator("/Login");
			});
	}, [code, navigator]);

	return (
		<div className="LoginHandeler">
			<p>로그인 중입니다.</p>
			<p>잠시만 기다려주세요.</p>
			{/* 로그인 중 로딩 상황 보여주기 */}
		</div>
	);
};

export default KakaoCallBack;
