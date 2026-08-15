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

const unwrapApiResponse = (response) => {
  const { code, message, data } = response.data;

  if (code !== 'SUCCESS') {
    const error = new Error(message);
    error.code = code;
    throw error;
  }

  return data;
};

const sendRequest = (method, url, data, config) => {
  if (method === 'get' || method === 'delete') {
    return axiosClient[method](url, config);
  }
  return axiosClient[method](url, data, config);
};

// access token 재발급 요청
const getNewAccessToken = async (refreshToken) => {
  const response = await axiosClient.get('/oauth/token/reissue', {
    headers: { refreshToken },
  });

  return unwrapApiResponse(response).token;
};

const forceLogout = () => {
  const cookies = new Cookies();

  store.dispatch(logout());
  store.dispatch(showToast({ contents: 'login', toastIndex: 5 }));
  cookies.remove('refreshToken', { path: '/' });

  setTimeout(() => {
    window.location.reload();
  }, 1000);
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

		return unwrapApiResponse(await sendRequest(method, url, data, axiosConfig));

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

				return unwrapApiResponse(await sendRequest(method, url, data, axiosConfig));

			} catch (retryError) {
				if (retryError.response?.status === 401) {
					console.error("refresh token 만료로 인한 재발급 요청 실패:", retryError);
				} else {
					console.error("refresh token 만료가 아닌 다른 문제 발생", retryError);
					alert("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				}
				// 로그아웃 처리
				console.error("access token 재발급 요청 실패:", retryError);
				forceLogout();
				throw retryError;
			}
		} else {
			console.error(`${method} 요청 실패:`, error);
			alert("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			forceLogout();
			throw error;
		}
	}
};

export const errorWithAuth = (codeNum, errorMessage) => {
	if (codeNum === "AUTH_001") { // 로그인이 필요한 API에 자격 증명 없이 접근한 경우
		console.log("로그인이 필요합니다.(토큰이 만료된 경우)")
	} else if (codeNum === "AUTH_002") { // 인증은 되었지만, 해당 리소스에 대한 권한이 없는 경우
		console.log("해당 리소스에 대한 권한이 없습니다.(리뷰 삭제와 같은 동작)")
	} else if (codeNum === "AUTH_003") { // 전달한 토큰 값이 서버에 저장된 값과 다른 경우
		console.log("전달한 토큰 값이 서버에 저장된 값과 다른 경우(토큰 위조)");
	} else {
		console.log("Auth와 관련 없는 에러 입니다. 확인이 필요합니다.");
	}
	alert(`에러 발생: ${errorMessage}`);
};

export const getWithToken = async (url, config = {}) =>
	requestWithToken("get", url, null, config);

export const postWithToken = async (url, data, config = {}) =>
	requestWithToken("post", url, data, config);

export const deleteWithToken = async (url, config = {}) =>
	requestWithToken("delete", url, null, config);

export const putWithToken = async (url, data, config = {}) =>
	requestWithToken("put", url, data, config);
