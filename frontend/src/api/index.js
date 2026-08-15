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

const forceLogout = (options = {}) => {
  const { message } = options;
  const cookies = new Cookies();

  store.dispatch(logout());
  store.dispatch(
    showToast({
      contents: message ? "error" : "login",
      toastIndex: message ? 0 : 5,
      message: message || null,
    })
  );
  cookies.remove('refreshToken', { path: '/' });

  setTimeout(() => {
    window.location.reload();
  }, 1000);
};

const getResponseData = (error) => {
	const data = error?.response?.data;
	if (typeof data === "string") {
		try {
			return JSON.parse(data);
		} catch {
			return null;
		}
	}
	return data ?? null;
};

export const getErrorCode = (error) => {
	const responseCode = getResponseData(error)?.code;
	if (responseCode) return responseCode;
	if (!error?.response) return error?.code;
	return undefined;
};

export const getErrorMessage = (error) => {
	const dataMessage = getResponseData(error)?.message;
	if (dataMessage) return dataMessage;
	if (!error?.response) return error?.message;
	return undefined;
};

const showErrorToast = (message) => {
	store.dispatch(
		showToast({
			contents: "error",
			toastIndex: 0,
			message: message || "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
		})
	);
};

// AUTH_001: access token 재발급 후 원래 요청 재시도
const reissueAccessTokenAndRetry = async (method, url, data, config, refreshToken) => {
	const state = store.getState();
	const user = state.user?.value;
	const nickname = user?.nickname;
	const restaurantId = user?.selectedRestaurant;

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
		const axiosConfig = {
			...config,
			headers: {
				...config.headers,
				Authorization: `Bearer ${newAccessToken}`,
			},
		};

		return unwrapApiResponse(await sendRequest(method, url, data, axiosConfig));
	} catch (retryError) {
		const retryCode = getErrorCode(retryError);
		if (retryCode === "AUTH_001" || retryCode === "AUTH_003") {
			console.error("refresh token 만료/불일치로 인한 재발급 요청 실패:", retryError);
		} else {
			console.error("refresh token 만료가 아닌 다른 문제 발생", retryError);
		}
		// 로그아웃 처리 (토스트는 서버 message 한 번만)
		console.error("access token 재발급 요청 실패:", retryError);
		forceLogout({ message: getErrorMessage(retryError) });
		throw retryError;
	}
};

const requestWithToken = async (method, url, data = null, config = {}) => {
	const state = store.getState();
	const user = state.user?.value;
	const accessToken = user?.token;

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
		const code = getErrorCode(error);
		const message = getErrorMessage(error);

		// AUTH_001: access token 만료/인증 실패 → 재발급 후 재시도
		if (code === "AUTH_001" && refreshToken) {
			return reissueAccessTokenAndRetry(method, url, data, config, refreshToken);
		}

		// 인증 자체가 불가한 경우만 로그아웃 (일반 비즈니스 에러는 toast만)
		if (code === "AUTH_001" || code === "AUTH_003") {
			forceLogout({ message });
		} else {
			showErrorToast(message);
		}

		throw error;
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
	// alert(`에러 발생: ${errorMessage}`);
};

export const getWithToken = async (url, config = {}) =>
	requestWithToken("get", url, null, config);

export const postWithToken = async (url, data, config = {}) =>
	requestWithToken("post", url, data, config);

export const deleteWithToken = async (url, config = {}) =>
	requestWithToken("delete", url, null, config);

export const putWithToken = async (url, data, config = {}) =>
	requestWithToken("put", url, data, config);
