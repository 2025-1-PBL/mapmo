import requests
from bs4 import BeautifulSoup
from datetime import datetime
from db_connect.db_connection_utils import create_database_connection, insert_event


def crawl_and_store_events():
    base_url = "https://www.bhc.co.kr/event/ing01.asp"
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.5672.126 Safari/537.36'}

    # 메인 페이지 요청
    response = requests.get(base_url, headers=headers, verify=False)
    if response.status_code != 200:
        print(f"메인 페이지 요청 실패: {response.status_code}")
        return

    soup = BeautifulSoup(response.text, "html.parser")

    # 이벤트 상세 페이지 링크 추출
    event_links = []
    for a_tag in soup.select("div.event_box a"):
        event_links.append(a_tag.get('href'))

    # 데이터베이스 연결
    conn = create_database_connection()
    if conn is None:
        print("데이터베이스 연결을 생성할 수 없습니다.")
        return

    for link in event_links:
        event_url = link if link.startswith("http") else f"https://www.bhc.co.kr/event/{link}"
        detail_response = requests.get(event_url, headers=headers, verify=False)
        if detail_response.status_code != 200:
            print(f"{event_url} 요청 실패: {detail_response.status_code}")
            continue

        detail_soup = BeautifulSoup(detail_response.text, "html.parser")

        try:
            # 제목 추출
            title = detail_soup.select_one("table.register02 th").text.strip()

            # 본문 이미지 태그에서 src 추출
            img_tag = detail_soup.select_one("tbody img")
            content = img_tag['src'] if img_tag else ''

            # 날짜 추출 (시작 날짜와 종료 날짜)
            date_element = detail_soup.select_one("td.date")
            if date_element:
                date_text = date_element.text.strip()  # 날짜 텍스트 추출

                # 날짜 텍스트를 시작 날짜와 종료 날짜로 분리
                try:
                    start_date_str, end_date_str = map(str.strip, date_text.split("~"))
                    start_date = datetime.strptime(start_date_str, "%Y-%m-%d")
                    end_date = datetime.strptime(end_date_str, "%Y-%m-%d")
                except Exception as e:
                    print(f"날짜 파싱 오류: {e}")
            else:
                print("해당하는 날짜 정보가 없습니다.")

            # 상태는 임의로 1로 설정
            status = "ACTIVE"
            brand_id = 7  # 브랜드 ID

            # 데이터베이스에 삽입할 이벤트 데이터
            event_data = (title, content, event_url, brand_id, start_date, end_date, status)
            insert_event(conn, event_data)

        except Exception as e:
            print(f"{event_url} 데이터 처리 중 오류 발생: {e}")
            continue

    # 데이터베이스 연결 닫기
    conn.close()


if __name__ == "__main__":
    crawl_and_store_events()