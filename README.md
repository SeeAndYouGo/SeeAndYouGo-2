# SeeAndYouGo
![seeandyougo_readme](https://github.com/user-attachments/assets/c5419b07-baac-4a61-b251-fe5e8a00f98d)


> 충남대학교 공공 API를 활용한 식당 혼잡도 · 메뉴 · 리뷰 커뮤니티 플랫폼

SeeAndYouGo는 충남대학교 학내 식당의 혼잡도와 메뉴 정보를 한눈에 확인하고, 학생들이 직접 리뷰를 남기며 정보를 공유할 수 있는 웹 기반 커뮤니티 서비스입니다.

---

## 📌 프로젝트 개요

- **프로젝트명**: SeeAndYouGo
- **주소**: [https://seeandyougo.com](https://seeandyougo.com)
- **목적**: 학내 식당 이용 편의성 향상
- **핵심 기능**
    - 실시간 식당별 혼잡도 조회 (충남대학교 공공 API 연동)
    - 식당별 메뉴 정보 제공
    - 사용자 리뷰 및 평점 시스템
    - 식당별 혼잡도 통계
- **프로젝트 지속 기간**: 2023-07 ~

---

## 👥 팀원 소개

| | | | |
|:---:|:---:|:---:|:---:|
| <img src="https://github.com/shxnzxxn.png" width="150" height="150" alt="신경준"> | <img src="https://github.com/yyeennyy.png" width="150" height="150" alt="김예은"> | <img src="https://github.com/woou4578.png" width="150" height="150" alt="서정찬"> | <img src="https://github.com/rlcz1.png" width="150" height="150" alt="전규리"> |
| **BE** | **BE** | **FE** | **FE** |
| [신경준](https://github.com/shxnzxxn) | [김예은](https://github.com/yyeennyy) | [서정찬](https://github.com/woou4578) | [전규리](https://github.com/rlcz1) |

---

## 🧩 주요 기능

### 📊 실시간 혼잡도 표시

충남대학교 공공 API를 통해 각 식당의 실시간 혼잡도를 시각적으로 제공합니다.

### 🍽️ 식당 메뉴 조회

식당별 메뉴 및 가격 정보를 한눈에 확인할 수 있습니다. 

### ✍️ 리뷰 & 평점 커뮤니티

학생들이 직접 리뷰와 평점을 남기고 정보를 공유할 수 있습니다.

### 📈 혼잡도 통계

식당별 혼잡도 통계를 차트로 확인할 수 있어, 시간대별 혼잡도 패턴을 파악할 수 있습니다.

---

## 🛠️ 기술 스택

### Frontend
- **Framework**: React 18.2.0
- **State Management**: Redux Toolkit, Redux Persist
- **Routing**: React Router DOM 6.20.0
- **UI Library**: Emotion, RSuite, Font Awesome
- **Chart**: Chart.js, React Chart.js 2
- **Image Processing**: React Easy Crop
- **Analytics**: React GA4
- **Build Tool**: CRACO

### Backend
- **Framework**: Spring Boot 2.7.16
- **Language**: Java 11
- **Database**: MySQL 8.0.27
- **Cache**: Redis
- **Security**: Spring Security, JWT
- **Storage**: AWS S3
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Gradle
