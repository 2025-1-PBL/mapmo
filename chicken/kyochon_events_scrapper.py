import requests
from bs4 import BeautifulSoup
from datetime import datetime
from db_connect.db_connection_utils import create_database_connection, insert_event

# 기본 URL
BASE_URL = "https://www.kyochon.com"
EVENT_URL = "https://www.kyochon.com/event/"

# 진행 중인 이벤트 리스트 페이지에서 링크 추출
def get_event_links():
    page_number = 1
    all_links = []

    while True:
        response = requests.get(f"{EVENT_URL}ing.asp?page={page_number}")
        soup = BeautifulSoup(response.text, 'html.parser')

        # 현재 페이지에서 링크 추출
        event_list = soup.select('ul.eventList.mt50 li a[href]')
        if not event_list:  # 페이지가 더 이상 없을 경우 종료
            break

        # 링크 추가
        links = [EVENT_URL + a['href'] for a in event_list]
        all_links.extend(links)

        print(f"Page {page_number} links extracted: {links}")

        page_number += 1  # 다음 페이지로 이동

    return all_links

# 상세 페이지에서 데이터 추출
def get_event_details(event_url):
    response = requests.get(event_url)
    soup = BeautifulSoup(response.text, 'html.parser')

    # 이벤트 제목
    title = soup.select_one('th.event').text.strip().splitlines()[0].strip()

    # 이벤트 기간
    date_text = soup.select_one('td.noP').text.strip().replace("이벤트기간 : ", "")
    start_date, end_date = map(lambda x: x.strip(), date_text.split('~'))

    # 이미지 URL
    img_tag = soup.select_one('div.vConts.tac img')
    img_url = BASE_URL + img_tag['src'] if img_tag else None

    return {
        "title": title,
        "start_date": datetime.strptime(start_date, '%Y-%m-%d'),
        "end_date": datetime.strptime(end_date, '%Y-%m-%d'),
        "img_url": img_url,
        "url": event_url
    }

def main():
    # DB 연결
    conn = create_database_connection()
    if not conn:
        print("DB 연결 실패! 종료합니다.")
        return

    try:
        # 이벤트 링크 추출
        links = get_event_links()

        # 각 링크를 순회하며 데이터 저장
        for link in links:
            event_details = get_event_details(link)
            event_data = (
                event_details['title'],
                event_details['img_url'],
                event_details['url'],
                6,  # 고정된 brand_id 사용
                event_details['start_date'],
                event_details['end_date'],
                "ACTIVE"  # 상태는 진입 항목에서 추출
            )
            insert_event(conn, event_data)
    finally:
        conn.close()

if __name__ == "__main__":
    main()