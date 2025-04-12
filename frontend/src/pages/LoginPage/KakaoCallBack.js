import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { login, setNickname } from "../../redux/slice/UserSlice";
import { showToast } from "../../redux/slice/ToastSlice";
import Loading from "../../components/Loading";
import { useCookies } from 'react-cookie';
import { get, getWithToken } from "../../api";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const [cookies, setCookie, removeCookie] = useCookies(['refreshToken']);
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");
	const dispatch = useDispatch();
	const restaurantId = useSelector((state) => state.user).value
		.selectedRestaurant;

	useEffect(() => {
		const getJWTToken = async (authorizationCode) => {
			const response = await get(`/oauth/kakao?code=${authorizationCode}`);

			const nowToken = response.data.token;
			const refreshToken = response.data.refreshToken;
			const message = response.data.message;
			return { nowToken, refreshToken, message };
		};

		const fetchData = async () => {
			try {
				const { nowToken, refreshToken, message } = await getJWTToken(code);
				
				// refresh token을 쿠키에 저장
				setCookie('refreshToken', refreshToken, {
					path: '/',
					maxAge: 14 * 24 * 60 * 60, // 14일
					secure: true,
					sameSite: 'strict'
				});
				
				dispatch(
					login({ token: nowToken, nickname: "", loginState: true, selectedRestaurant: restaurantId })
				);

				if (message === "join") { // 회원가입인 경우 닉네임 설정 창으로 이동
					dispatch(showToast({ contents: "login", toastIndex: 1 }));
					navigator("/set-nickname");
				} else { // 이미 등록된 회원인 경우 닉네임 가져오기
					const res = await getWithToken('/user/nickname')
					dispatch(setNickname(res.data.nickname));
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
