import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import * as config from "../../config";
import { useDispatch } from "react-redux";
import { login, setNickname } from "../../redux/slice/UserSlice";
import { changeToastIndex } from "../../redux/slice/ToastSlice";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");
	const dispatch = useDispatch();

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
				dispatch(login({token: data.token, nickname: "", loginState: true}));

				if (data.message === "join") { // 회원가입인 경우 닉네임 설정 창으로 이동
					alert("회원가입을 축하합니다!\n닉네임을 설정해주세요.");
					navigator("/set-nickname");
				} else { // 이미 등록된 회원인 경우 닉네임 가져오기
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
						dispatch(setNickname(res.nickname));
					});
					dispatch(changeToastIndex(0));
					navigator("/");
				}
			})
			.catch((err) => {
				console.log(err);
				alert("로그인에 실패했습니다.");
				navigator("/login-page");
			});
	}, [code, navigator, dispatch]);

	return (
		<div className="LoginHandeler">
			<p>로그인 중입니다.</p>
			<p>잠시만 기다려주세요.</p>
			{/* 로그인 중 로딩 상황(애니메이션?) 보여주기 */}
		</div>
	);
};

export default KakaoCallBack;
