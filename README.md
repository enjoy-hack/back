# 🏠 SmartAir - Smart Air 공기질 관리 시스템

<div align="center">

<!-- 💻 Backend -->
<img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white"/>
<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/MQTT-660066?style=for-the-badge&logo=mqtt&logoColor=white"/>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>

<!-- ☁️ Infra / DevOps -->
<img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white"/>
<img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=amazon-s3&logoColor=white"/>
<img src="https://img.shields.io/badge/Amazon%20AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white"/>
<img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>

<!-- 🛠 Tools -->
<img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"/>
<img src="https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white"/>

</div>

## 📝 프로젝트 소개

SmartAir는 실시간 공기질 모니터링과 자동 제어를 통해 건강한 실내 환경을 제공하는 스마트 홈 솔루션입니다. IoT 센서와 MQTT 프로토콜을 활용하여 실시간으로 공기질 데이터를 수집하고, 사용자에게 직관적인 대시보드를 제공합니다.

“건강한 실내 환경을 위한 스마트한 선택”

## ✨ 주요 기능

### 🌡️ 실시간 공기질 모니터링
- IoT 센서를 통한 실시간 데이터 수집
- MQTT 프로토콜 기반의 안정적인 데이터 통신
- 동시성 제어를 통한 신뢰성 있는 데이터 처리

### 📊 스마트 대시보드
- 실시간 공기질 지수 시각화
- 시간대별 공기질 추이 분석
- 예측 공기질 시각화

### 📬 이상치 탐지 및 알림
- 예측값과 실측값 비교와 시간대별 가중치를 통한 오차 감지
- Firebase Cloud Messaging 기반 실시간 푸시 알림

### ⚙️ 자동 제어 및 스마트홈 연동
- LG ThinQ API 연동 (OAuth2 + PAT 기반 디바이스 제어)
- 실내 공기 상태에 따라 공기청정기, 에어컨 제어

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.3
- **Language**: Java 17
- **Database**: MySQL (Amazon RDS)
- **Message Broker**: MQTT (Mosquitto)
- **Infra**: AWS EC2, S3, Route53, ALB, CodeDeploy
- **Build Tool**: Gradle

### 주요 라이브러리
- Spring Data JPA, Security + JWT + OAuth2
- Firebase Cloud Messaging (FCM)
- LG ThinQ API (AES 암호화 기반 통신)
- Lombok
- Swagger, Gradle, JUnit5

## 🔄 시스템 아키텍처
![image](https://github.com/user-attachments/assets/f74d37b1-586f-4e4b-9e0f-3bd7c84de0ec)
### 아키텍처 구성 요소

- **Developer & GitHub Actions**  
  개발자는 GitHub에 코드를 푸시하고, GitHub Actions를 통해 CI/CD가 자동으로 실행됩니다.

- **AWS CodeDeploy & EC2**  
  빌드된 애플리케이션은 AWS CodeDeploy를 통해 EC2 인스턴스에 자동 배포됩니다.

- **ALB & Route53**  
  클라이언트의 요청은 Route53(도메인)과 ALB(Application Load Balancer)를 거쳐 EC2로 전달됩니다.

- **Spring Boot & MQTT**  
  EC2 내부에서 Spring Boot 백엔드와 MQTT 브로커가 함께 동작하며, IoT 센서 데이터와 클라이언트 요청을 처리합니다.

- **Amazon RDS**  
  모든 서비스 데이터는 Amazon RDS(MySQL)에 저장됩니다.

- **외부 API 연동**  
  Firebase, LG ThinQ 등 외부 서비스와 연동하여 다양한 스마트홈 기능을 제공합니다.

- **S3 & Endpoint Gateway**  
  파일 업로드/다운로드 등은 Endpoint Gateway를 통해 S3에 안전하게 저장됩니다.

## 🔄 데이터 처리 흐름

1. **데이터 수집**
   - IoT 센서에서 MQTT를 통해 실시간 데이터 전송
   - 동시성 제어를 통한 안정적인 데이터 처리

2. **데이터 처리**
   - 실시간 공기질 지수 계산
   - 사용자 설정에 따른 알림 처리
   - 자동 제어 로직 실행

3. **데이터 저장**
   - MySQL 데이터베이스에 효율적으로 저장
   - 인덱싱을 통한 빠른 시간 기반 데이터 조회
   - 실시간 데이터 처리 및 분석

## 🚀 시작하기

### 필수 요구사항
- Java 17 이상
- MySQL 8.0 이상
- MQTT Broker (Mosquitto)

### 설치 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/your-username/SmartAir.git

# 프로젝트 디렉토리로 이동
cd SmartAir

# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 📚 API 문서
- Swagger UI: `http://localhost:8080/swagger-ui.html`

<div align="center">

### 🌟 함께 만들어가는 건강한 실내 환경 🌟

</div>

---
