import React, { useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { login, setNickname } from "../../redux/slice/UserSlice";
import { showToast } from "../../redux/slice/ToastSlice";
import Loading from "../../components/Loading";
import * as config from "../../config";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");
	const dispatch = useDispatch();

	useEffect(() => {
		const getJWTToken = async (authorizationCode) => {
			const url = config.DEPLOYMENT_BASE_URL + `/oauth/kakao?code=${authorizationCode}`;

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

		const fetchData = async () => {
			try {
				const nowToken = await getJWTToken(code);
				dispatch(
					login({ token: nowToken.token, nickname: "", loginState: true })
				);

				if (nowToken.message === "join") { // 회원가입인 경우 닉네임 설정 창으로 이동
					dispatch(showToast({ contents: "login", toastIndex: 1 }));
					navigator("/set-nickname");
				} else { // 이미 등록된 회원인 경우 닉네임 가져오기
					const urlForNickname =
						config.BASE_URL +
						`/user/nickname/${nowToken.token}` +
						(config.NOW_STATUS === 0 ? ".json" : "");
					const res = await fetch(urlForNickname, {
						headers: {
							"Content-Type": "application/json",
						},
						method: "GET",
					});
					const result = await res.json();
					dispatch(setNickname(result.nickname));
					dispatch(showToast({ contents: "login", toastIndex: 2 }));
					navigator("/");
				}
			} catch (err) {
				console.log(err);
				dispatch(showToast({ contents: "login", toastIndex: 3 }));
				navigator("/login-page");
			}
		};

		if (code) {
			fetchData();
		}
	}, [code, dispatch, navigator]);

	return (
		<Loading />
	);
};

export default KakaoCallBack;
