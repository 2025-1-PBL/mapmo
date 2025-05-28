from tkinter.constants import ACTIVE

import requests
from bs4 import BeautifulSoup
from db_connect.db_connection_utils import create_database_connection, insert_event

# 기본 도메인 URL 및 브랜드 리스트 정의
base_url = "https://pyony.com"
brands = {
    "GS25": 1,
    "CU": 2,
    "세븐일레븐": 3,
    "이마트24": 4
}

# MySQL 연결 설정
conn = create_database_connection()
if conn:
    for brand, brand_id in brands.items():
        target_url = f"{base_url}/posts/?category=&q={brand}"

        # 브랜드별 목록 페이지 요청
        response = requests.get(target_url)
        if response.status_code == 200:
            soup = BeautifulSoup(response.text, "html.parser")

            # 게시글 데이터 수집
            posts = soup.find_all("a", class_="deco-none")
            if posts:
                for post in posts:
                    # 게시글 제목 추출
                    small_tag = post.find("small", class_="font-weight-bold text-success")
                    if small_tag:
                        title = small_tag.get_text(strip=True)

                        # 게시글 URL 추출
                        link = post.get("href")
                        full_link = base_url + link if link.startswith("/") else link

                        # 게시글 내부에 접근하여 상세 내용 및 이미지 확인
                        post_response = requests.get(full_link)
                        if post_response.status_code == 200:
                            post_soup = BeautifulSoup(post_response.text, "html.parser")

                            # 내용 추출
                            content = post_soup.find("div", class_="clearfix small mt-3")
                            content_text = content.get_text(strip=True) if content else "내용이 없습니다."

                            # 데이터 형식에 맞춰 테이블 삽입
                            event_data = (
                                title,  # title
                                content_text,  # content
                                full_link,  # url
                                brand_id,  # brand_id (숫자로 처리)
                                None,  # start_date (None으로 처리)
                                None,  # end_date (None으로 처리)
                                "Active"  # status
                            )
                            # 중복된 URL 무시
                            insert_event(conn, event_data)
                        else:
                            print(f"게시글 {full_link}에 접속 실패: 상태 코드 {post_response.status_code}")
        else:
            print(f"{brand}의 목록 페이지 요청 실패: 상태 코드 {response.status_code}")

    # DB 연결 닫기
    conn.close()
else:
    print("DB 연결에 실패하여 데이터를 저장할 수 없습니다.")