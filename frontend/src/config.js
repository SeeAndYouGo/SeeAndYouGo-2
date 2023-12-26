// 더미 데이터 확인용 json 파일 경로
export const DEVELOPMENT_BASE_URL = "/assets/json";

// 배포용 경로 
// 확인을 위해 /api 앞에 붙여줘야하는 주소가 있다면 api 앞에 주소 붙여주면 됩니다.
export const DEPLOYMENT_BASE_URL = "/api";

// 0: development, 1: deployment
export const NOW_STATUS = 0;

export const BASE_URL = NOW_STATUS === 0 ? DEVELOPMENT_BASE_URL : DEPLOYMENT_BASE_URL;
