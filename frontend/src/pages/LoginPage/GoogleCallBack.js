import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { useCookies } from 'react-cookie';
import { get, getWithToken } from "../../api";
import { showToast } from "../../redux/slice/ToastSlice";
import { login, setNickname } from "../../redux/slice/UserSlice";
import Loading from "../../components/Loading";

//TODO 코드 자체는 같으니 추후에 google, kakao 등 통합해서 한개의 파일로 관리할 수 있도록 수정 
const GoogleCallBack = () => {
	const [cookies, setCookie, removeCookie] = useCookies(['refreshToken']);
	const navigator = useNavigate();
	const params = new URL(document.location.toString()).searchParams;
	const code = params.get("code");
	const dispatch = useDispatch();
	const restaurantId = useSelector((state) => state.user).value
		.selectedRestaurant;

	useEffect(() => {
		const getJWTToken = async (authorizationCode) => {
			const response = await get(`/oauth/google?code=${authorizationCode}`);

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

export default GoogleCallBack;