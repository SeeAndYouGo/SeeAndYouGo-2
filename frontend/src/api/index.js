import axios from "axios";
import * as config from "../config";
import store from "../redux/store";
import { showToast } from '../redux/slice/ToastSlice';
import { login, logout } from "../redux/slice/UserSlice";
import { Cookies } from 'react-cookie';

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
		const response = await axiosClient.get("/oauth/token/reissue", {
			headers: {
				refreshToken: refreshToken,
			},
		});

		return response.data.token;
	} catch (error) {
		throw error;
	}
};

const requestWithToken = async (method, url, data = null, config = {}) => {
	const state = store.getState(); // Redux 상태 직접 가져오기
	const user = state.user?.value;
	const accessToken = user?.token;
	const nickname = user?.nickname;
	const restaurantId = user?.selectedRestaurant;

	const cookies = new Cookies();
	const refreshToken = cookies.get('refreshToken');

	try {
		const headers = {
			...config.headers,
			Authorization: `Bearer ${accessToken}`,
		};

		const axiosConfig = {
			...config,
			headers,
		}

		if (method === "get" || method === "delete") {
			axiosConfig.params = data;
			return await axiosClient[method](url, axiosConfig);
		} else {
			return await axiosClient[method](url, data, axiosConfig);
		}

	} catch (error) {
		console.error("요청 실패:", error);
		if (error.response?.status === 401 && refreshToken) {
			try {
				console.log("access token 만료로 인한 재발급 요청");
				const newAccessToken = await getNewAccessToken(refreshToken);

				// 새로운 accessToken 저장
				store.dispatch(
					login({
						token: newAccessToken,
						nickname: nickname,
						loginState: true,
						selectedRestaurant: restaurantId,
					})
				);

				// 새로운 accessToken을 사용하여 원래 요청 재시도
				const newHeaders = {
					...config.headers,
					Authorization: `Bearer ${newAccessToken}`,
				};
				const axiosConfig = {
					...config,
					headers: newHeaders,
				};
				if (method === "get" || method === "delete") {
					axiosConfig.params = data;
					return await axiosClient[method](url, axiosConfig);
				} else {
					return await axiosClient[method](url, data, axiosConfig);
				}

			} catch (error) {
				if (error.response.status === 401) {
					console.error("refresh token 만료로 인한 재발급 요청 실패:", error);
				} else {
					console.error("refresh token 만료가 아닌 다른 문제 발생", error);
					alert("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				}
				// 로그아웃 처리
				console.error("access token 재발급 요청 실패:", error);
				store.dispatch(logout());
				store.dispatch(showToast({ contents: "login", toastIndex: 5 }));
				cookies.remove('refreshToken', { path: '/' });
				setTimeout(() => {
					window.location.reload();
				}, 1000);
				throw error;
			}
		} else {
			console.error(`${method} 요청 실패:`, error);
			alert("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			store.dispatch(logout());
			store.dispatch(showToast({ contents: "login", toastIndex: 5 }));
			cookies.remove('refreshToken', { path: '/' });
			setTimeout(() => {
				window.location.reload();
			}, 1000);
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

export const put = async (url, data, config = {}) => {
	try {
		const response = axiosClient.put(url, data, config);

		return response;
	} catch (error) {
		console.error("PUT 요청 실패:", error);
		throw error;
	}
}

export const erase = async (url, config = {}) => {
	// delete라는 변수를 사용할 수 없어서 erase로 작성
	try {
		const response = axiosClient.delete(url, config);

		return response;
	} catch (error) {
		console.error("DELETE 요청 실패:", error);
		throw error;
	}
}

export const getWithToken = async (url, config = {}) =>
	requestWithToken("get", url, null, config);

export const postWithToken = async (url, data, config = {}) =>
	requestWithToken("post", url, data, config);

export const deleteWithToken = async (url, config = {}) =>
	requestWithToken("delete", url, null, config);

export const putWithToken = async (url, data, config = {}) =>
	requestWithToken("put", url, data, config);
