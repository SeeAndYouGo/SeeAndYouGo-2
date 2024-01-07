import axios from "axios";
import React, { useEffect } from "react";
import * as config from "../../config";
import { useNavigate } from "react-router-dom";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");

	useEffect(() => {
		const getJWTToken = async (authorizationCode) => {
			const url =
				config.DEPLOYMENT_BASE_URL +
				`/oauth/kakao?code=${authorizationCode}`;

			const response = await axios({
				method: "GET",
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
				localStorage.setItem("token", data.token);

				if (data.message === "join") {
					// 회원가입인 경우 닉네임 설정 창으로 이동
					alert("회원가입을 축하합니다!\n닉네임을 설정해주세요.");
					// 밑에 navigator 닉네임 설정화면으로 수정 필요
					navigator("/Register");
				} else {
					// 이미 등록된 회원인 경우 닉네임 가져오기
					const fetchData = async () => {
						const urlForNickname =
							config.BASE_URL +
							`/user/nickname/${data.token}` +
							(config.NOW_STATUS === 0 ? ".json" : "");

						const res = await fetch(urlForNickname, {
							headers: {
								"Content-Type": "application/json",
							},
							method: "GET",
						});
						const result = await res.json();
						return result;
					};
					fetchData().then((res) => {
						localStorage.setItem("nickname", res.nickname);
					});
					alert("로그인에 성공했습니다.");
					navigator("/");
				}
			})
			.catch((err) => {
				console.log(err);
				alert("로그인에 실패했습니다.");
				navigator("/LoginPage");
			});
	}, [code, navigator]);

	return (
		<div className="LoginHandeler">
			<p>로그인 중입니다.</p>
			<p>잠시만 기다려주세요.</p>
			{/* 로그인 중 로딩 상황(애니메이션?) 보여주기 */}
		</div>
	);
};

export default KakaoCallBack;
