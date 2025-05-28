# bbq_events_scrapper.py

import time
import re
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from db_connect.db_connection_utils import create_database_connection, insert_event

# WebDriver 설정
service = Service(ChromeDriverManager().install())
driver = webdriver.Chrome(service=service)

# BBQ 이벤트 메인 페이지
url = "https://bbq.co.kr/events"
driver.get(url)

# 동적 콘텐츠 로드 대기
time.sleep(3)

base_url = "https://bbq.co.kr"
event_links = []

# 이벤트 링크 수집
elements = driver.find_elements(By.CSS_SELECTOR, "div.sc-9ecd31b7-0.cxpNQy a.sc-9ecd31b7-0.gaMOcl")
for element in elements:
    relative_link = element.get_attribute("href")
    if relative_link:
        event_links.append(relative_link)

# MySQL 데이터베이스 연결
conn = create_database_connection()

if conn:  # DB 연결 성공 시 실행
    # 각 이벤트 세부 정보 수집
    for event_url in event_links:
        driver.get(event_url)
        time.sleep(3)  # 페이지 로드 대기

        # 1) 제목 추출 (2번째 span)
        try:
            span_elements = driver.find_elements(By.CSS_SELECTOR, "div.event-page-body span.sc-857a90a8-0")
            if len(span_elements) > 1:
                title = span_elements[1].text  # 두 번째 <span> 태그의 텍스트
            else:
                title = "N/A"
        except Exception as e:
            print("제목 추출 실패:", e)
            title = "N/A"

        # 2) 이벤트 기간 추출
        try:
            period_element = driver.find_element(By.XPATH, "//span[contains(text(), '이벤트 기간')]")
            period_text = period_element.text
            dates = re.findall(r"\d{4}-\d{2}-\d{2}", period_text)  # 정규식을 이용해 날짜 추출
            if dates and len(dates) == 2:
                start_date, end_date = dates  # 시작일과 종료일
            else:
                start_date, end_date = None, None
        except Exception as e:
            print("이벤트 기간 추출 실패:", e)
            start_date, end_date = None, None

        # 3) 이미지 URL 추출
        try:
            img_element = driver.find_element(By.CSS_SELECTOR, "div.event-article img")
            content = img_element.get_attribute("src")  # 이미지 URL을 content로 사용
        except Exception as e:
            print("이미지 추출 실패:", e)
            content = "N/A"

        # 4) 상태는 고정값 (예: "Active")으로 설정
        status = "Active"

        # 브랜드 ID는 BBQ 고유 ID로 설정 (예: 1)
        brand_id = 5

        # DB에 삽입할 이벤트 데이터
        event_data = (title, content, event_url, brand_id, start_date, end_date, status)
        insert_event(conn, event_data)
        print("-" * 50)

    # MySQL 연결 닫기
    conn.close()

# WebDriver 종료
driver.quit()