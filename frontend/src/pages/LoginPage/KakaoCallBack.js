import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { login, setNickname } from "../../redux/slice/UserSlice";
import { showToast } from "../../redux/slice/ToastSlice";
import Loading from "../../components/Loading";
import { useCookies } from "react-cookie";
import { getWithToken } from "../../api";

const KakaoCallBack = () => {
	// 백엔드에서 access_token 받아오고 정보 가져오는거까지 처리
	const [cookies, setCookie, removeCookie] = useCookies(["refreshToken"]);
	const navigator = useNavigate();
	const dispatch = useDispatch();

	const restaurantId = useSelector((state) => state.user).value
		.selectedRestaurant;

	const code = new URL(document.location.toString()).searchParams.get("code");

	useEffect(() => {
		if (!code) return;

		const fetchData = async () => {
			try {
				const { token, refreshToken, message } = await getWithToken(
					`/oauth/kakao?code=${code}`
				);

				// refresh token을 쿠키에 저장
				setCookie("refreshToken", refreshToken, {
					path: "/",
					maxAge: 14 * 24 * 60 * 60, // 14일
					secure: true,
					sameSite: "strict",
				});

				dispatch(
					login({
						token,
						nickname: "",
						loginState: true,
						selectedRestaurant: restaurantId,
					}),
				);

				if (message === "join") { // 회원가입인 경우 닉네임 설정 창으로 이동
					dispatch(showToast({ contents: "login", toastIndex: 1 }));
					navigator("/set-nickname");
					return;
				} 

				// 이미 등록된 회원인 경우 닉네임 가져오기
				const { nickname } = await getWithToken("/user/nickname");
				dispatch(setNickname(nickname));
				dispatch(showToast({ contents: "login", toastIndex: 2 }));
				navigator("/");
			} catch (error) {
				console.log(error);
				dispatch(showToast({ contents: "login", toastIndex: 3 }));
				navigator("/login-page");
			}
		};

		fetchData();
	}, [code, dispatch, navigator, restaurantId, setCookie]);

	return <Loading />;
};

export default KakaoCallBack;
