# 알림 발송 시스템(Notification Service)

## 프로젝트 개요

상품 재고가 품절되면 유저가 알림을 신청할 수 있으며, 재입고 시 알림을 신청한 유저에게 순차적으로 알림을 발송하는 시스템입니다. 알림 발송 기록은 데이터베이스에 저장되며, 외부 서드파티 연동은 없습니다.

---

## 요구사항

### 비즈니스 요구사항

1. **재입고 조건:**

   - 상품이 품절 상태일 때만 재입고 이벤트 발생.

2. **알림 신청:**

   - 유저는 품절 상태일 때만 알림 신청 가능.

3. **알림 발송:**

   - 재입고 발생 시 등록 순서대로 유저에게 알림 전송.
   - 오류로 인한 수동 알림 발송시 마지막 발송 유저 기준으로 이어서 발송.

4. **중단 조건:**

   - 재고 소진 발생 시 알림 작업 중단 및 상태 기록.
   - 알림 발송 오류 발생 시 발송 중단 및 오류 기록.

---

### 기술적 요구사항

- **알림 전송 속도:** 최대 1초에 500개 전송 가능.
- **비동기 처리:** 시스템 상 비동기 처리는 없음.
- **예외 처리:**
  - 수동 API 호출 시 발송히스토리의 마지막 발송 유저 이후부터 재발송 가능.

---

## 시스템 아키텍처 및 ERD
### ERD 설계
![AlarmSeviceERD](https://github.com/user-attachments/assets/3a3a1d23-ca9c-4d00-8147-cc13eb289675)

1. **Product (상품):**

   - 상품 ID
   - 재입고 회차
   - 재고 상태 (`IN_STOCK`, `OUT_OF_STOCK`)

2. **ProductNotificationHistory (알림 발송 이력):**

   - 상품 ID
   - 재입고 회차
   - 발송 상태 (`IN_PROGRESS`, `COMPLETED`, `CANCELED_BY_SOLD_OUT`, `CANCELED_BY_ERROR`)
   - 마지막 발송 유저 ID

3. **ProductUserNotification (알림 신청 유저):**

   - 상품 ID
   - 유저 ID (유니크 키)
   - 활성화 여부
   - 생성 날짜
   - 수정 날짜

4. **ProductUserNotificationHistory (발송 기록):**

   - 상품 ID
   - 유저 ID
   - 재입고 회차
   - 발송 날짜

---

## 주요 로직 설명

### 알림 발송 로직

1. 상품 상태가 `OUT_OF_STOCK`인지 확인.
2. 재입고 회차 증가 및 히스토리 초기화.
3. 알림 신청 유저 조회 후 순차 발송:
   - 발송 성공 시 기록 업데이트.
   - 발송 실패 시 오류 기록.
   - 품절 시 발송 중단.
4. 모든 유저에게 발송 완료 시 `COMPLETED` 상태로 업데이트.

---

### 재입고 발생 시 처리 시나리오

| **상황**             | **행동**              |
| ------------------ | ------------------- |
| **재고 품절 → 알림 중단**  | 마지막 발송 유저 ID 저장     |
| **재입고 발생 (회차 증가)** | 제품알람신청한유저 전체 발송  |
| **알림 신청자 없음**      | 알림 없음, 회차 증가 없음     |
| **알림 발송 완료**       | 상태 업데이트 `COMPLETED` |

---

## REST API 설계

### 알림 발송 API

| **HTTP 메서드** | **URL**                 | **설명**   |
| ------------ | ----------------------- | -------- |
| `POST`       | `/api/products/restock` | 상품 재입고   |
| `POST`       | `/api/products/soldout` | 상품 품절 처리 |
| `POST`       | `/api/products/manual-notify` | 수동 재발송   |

---

## 테스트 시나리오

### 1. 성공적인 알림 발송

- 유저가 정상적으로 알림을 받는 시나리오 검증.
- 알림 발송 기록이 `IN_PROGRESS`로 업데이트됨.

### 2. 알림 신청자 없음

- 유저가 없는 경우 알림 발송이 발생하지 않는지 검증.
- 알림 상태가 `COMPLETED`로 설정됨.

### 3. 품절 시 알림 중단

- 상품이 품절 상태일 때 발송이 중단되는 시나리오 검증.
- 오류 메시지 확인 (`재고 소진 발생: 유저 ID 101`).

### 4. 발송 중 오류 발생

- 발송 도중 예외가 발생하는 시나리오 검증.
- 오류 기록이 `CANCELED_BY_ERROR`로 업데이트됨.

---

## 체크리스트

- \- [x]  상품 관리 API 구현

  \- [x]  알림 발송 로직 구현

  \- [x]  품절 상태 처리 로직 구현

  \- [x]  수동 재발송 API 구현

  \- [x]  테스트 케이스 작성 및 검증

---

## 참고 사항 및 주의점
- 외부 API 연동은 현재 없는 상태.


