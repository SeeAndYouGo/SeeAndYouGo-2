import axios from "axios";
import * as config from "../config";
import store from "../redux/store";
import { login, logout } from "../redux/slice/UserSlice";

const baseURL = config.NOW_STATUS === 1 ? config.DEPLOYMENT_BASE_URL : "/api";

const axiosClient = axios.create({
	baseURL: baseURL,
	timeout: 5000,
	headers: {
		"Content-Type": "application/json",
	},
});

// access token 재발급 요청
const getNewAccessToken = async (refreshToken) => {
	try {
		const response = await axiosClient.post("/refresh", {
			headers: {
				refresh: refreshToken,
			},
		});

		return response.data.token;
	} catch (error) {
		throw error;
	}
};

const requestWithToken = async (method, url, data = null, config = {}) => {
	const state = store.getState();
	const accessToken = state.user.value.token;
	// TODO 쿠키에서 refresh token 가져오기
	const refreshToken = null;

	try {
		const response = await axiosClient[method](url, data, {
			...config,
			headers: {
				Authorization: `Bearer ${accessToken}`,
			},
		});

		return response;
	} catch (error) {
		if (error.response.status === 401 && refreshToken) {
			try {
				const newAccessToken = await getNewAccessToken(refreshToken);

				// 새로운 accessToken 저장
				store.dispatch(
					login({
						token: newAccessToken,
						nickname: state.user.value.nickname,
						loginState: true,
						selectedRestaurant: state.user.value.selectedRestaurant,
					})
				);

				// 요청 재시도
				const retryResponse = await axiosClient[method](url, data, {
					...config,
					headers: {
						Authorization: `Bearer ${newAccessToken}`,
					},
				});

				return retryResponse;
			} catch (error) {
				console.error("access token 재발급 요청 실패:", error);
				// refresh token 문제(refresh token 만료 또는 이슈 발생)로 인해 로그아웃
				// TODO 재로그인 필요하다는 내용 토스트 메시지로 띄우기
				// 로그인 인증이 만료되었습니다. 다시 로그인해주세요.
				store.dispatch(logout());
				setTimeout(() => {
					window.location.reload();
				}, 1000);
				throw error;
			}
		} else {
			console.error(`${method} 요청 실패:`, error);
			throw error;
		}
	}
};

export const get = async (url, config = {}) => {
	try {
		const response = axiosClient.get(url, config);

		return response;
	} catch (error) {
		console.error("GET 요청 실패:", error);
		throw error;
	}
};

export const getWithToken = async (url, config = {}) =>
	requestWithToken("get", url, null, config);

export const postWithToken = async (url, data, config = {}) =>
	requestWithToken("post", url, data, config);

export const deleteWithToken = async (url, config = {}) =>
	requestWithToken("delete", url, null, config);

export const putWithToken = async (url, data, config = {}) =>
	requestWithToken("put", url, data, config);
