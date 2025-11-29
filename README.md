# 🎉 Fest-inside

> Spring Boot와 FastAPI를 활용한 축제 추천 플랫폼

## 📌 프로젝트 개요

**Fest-inside**는 사용자의 선호도와 위치 정보를 기반으로 최적의 축제를 추천하는 하이브리드 아키텍처 기반 플랫폼입니다.

- **Spring Boot**: 메인 API 서버 (회원 관리, 인증, CRUD)
- **FastAPI**: AI/데이터 서버 (추천 알고리즘, 크롤링)

---

## 📂 프로젝트 구조
Fest-inside/
├── backend-spring/ # ☕ Main API Server
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/
│ │ │ │ └── greenart/festival/
│ │ │ │ ├── FestivalApplication.java
│ │ │ │ ├── controller/ # HTTP 요청 처리
│ │ │ │ ├── service/ # 비즈니스 로직
│ │ │ │ ├── repository/ # 데이터 접근 (JPA)
│ │ │ │ ├── entity/ # DB 엔티티
│ │ │ │ ├── dto/ # 데이터 전송 객체
│ │ │ │ ├── config/ # 설정
│ │ │ │ └── exception/ # 예외 처리
│ │ │ └── resources/
│ │ │ ├── application.properties
│ │ │ ├── templates/ # Thymeleaf 템플릿
│ │ │ └── static/ # CSS, JS, Images
│ │ └── test/
│ ├── build.gradle
│ └── settings.gradle
│
├── backend-fastapi/ # 🐍 AI & Data Server
│ ├── app/
│ │ ├── main.py # FastAPI Entry Point
│ │ ├── routers/ # API 라우트
│ │ ├── models/ # DB 모델
│ │ ├── schemas/ # Pydantic 스키마
│ │ ├── services/ # 비즈니스 로직
│ │ ├── core/ # 설정
│ │ └── utils/ # 유틸리티
│ ├── requirements.txt
│ └── .env
│
└── README.md

---

## 🛠 기술 스택

### Backend (Spring Boot)
- Java 17
- Spring Boot 3.x
- Gradle
- Spring Data JPA
- MySQL / H2

### Backend (FastAPI)
- Python 3.10+
- FastAPI
- Uvicorn
- Pydantic

---

## 🚀 시작하기

### 사전 요구사항
- **Java 17** 이상
- **Python 3.10** 이상
- **Gradle** (또는 IntelliJ IDEA 내장 Gradle 사용)

---

## ▶️ 실행 방법

cd backend-spring

Windows
gradlew.bat bootRun

Mac/Linux
./gradlew bootRun

text

- **서버 주소**: `http://localhost:8080`

---

### 2️⃣ FastAPI 서버 실행

각 서버는 **독립적으로 실행**됩니다. 터미널을 2개 열어주세요.

### 1️⃣ Spring Boot 서버 실행

cd backend-spring

Windows
gradlew.bat bootRun

Mac/Linux
./gradlew bootRun

- **서버 주소**: `http://localhost:8080`

---

### 2️⃣ FastAPI 서버 실행

cd backend-fastapi

가상환경 생성 (선택사항)
python -m venv venv
source venv/bin/activate # Windows: venv\Scripts\activate

의존성 설치
pip install -r requirements.txt

서버 실행
uvicorn app.main:app --reload


- **서버 주소**: `http://localhost:8000`
- **API 문서**: `http://localhost:8000/docs`

---

## 🤝 협업 가이드

### Git Branch 전략

- **main**: 배포 가능한 안정 버전
- **feature/기능명**: 새 기능 개발 시

### 커밋 메시지 컨벤션

feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드 추가
