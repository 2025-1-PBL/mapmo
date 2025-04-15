import requests
from bs4 import BeautifulSoup
import pandas as pd  # pandas 라이브러리로 엑셀 저장

# 기본 도메인 URL 및 브랜드 리스트 정의
base_url = "https://pyony.com"
brands = ["GS25", "CU", "세븐일레븐", "이마트24"]

# 결과 저장을 위한 리스트
results = []

# 모든 브랜드에 대해 작업 수행
for brand in brands:
    # print(f"\n=== {brand}에 대한 게시글 목록 수집 시작 ===")
    target_url = f"{base_url}/posts/?category=&q={brand}"

    # 브랜드별 목록 페이지 요청
    response = requests.get(target_url)
    if response.status_code == 200:
        soup = BeautifulSoup(response.text, "html.parser")

        # 게시글 데이터 수집
        posts = soup.find_all("a", class_="deco-none")

        if posts:
            # print(f"{brand} 게시글의 제목, URL 및 이미지 링크:")
            for post in posts:
                # 게시글 제목 추출
                small_tag = post.find("small", class_="font-weight-bold text-success")
                if small_tag:
                    title = small_tag.get_text(strip=True)

                    # 게시글 URL 추출
                    link = post.get("href")
                    full_link = base_url + link if link.startswith("/") else link

                    # 게시글 내부에 접근하여 이미지 수집
                    post_response = requests.get(full_link)
                    if post_response.status_code == 200:
                        post_soup = BeautifulSoup(post_response.text, "html.parser")

                        # 이미지 섹션 추출
                        image_tag = post_soup.find("div", class_="clearfix small mt-3")
                        if image_tag:
                            img = image_tag.find("img")
                            image_url = img.get("src") if img else "이미지가 없습니다."

                            # 데이터를 리스트로 저장
                            results.append({
                                "브랜드": brand,
                                "제목": title,
                                "게시글 URL": full_link,
                                "이미지 URL": image_url
                            })
                            # print(f"제목: {title}, URL: {full_link}, 이미지 링크: {image_url}")
                        else:
                            print(f"제목: {title}, URL: {full_link}, 이미지 섹션이 없습니다.")
                    else:
                        print(f"게시글 {full_link}에 접속 실패: 상태 코드 {post_response.status_code}")
        else:
            print(f"{brand}에 대한 게시글 데이터가 없습니다.")
    else:
        print(f"{brand}의 목록 페이지 요청 실패: 상태 코드 {response.status_code}")

# 수집된 결과를 엑셀 파일로 저장
df = pd.DataFrame(results)  # pandas 데이터프레임으로 변환
output_file = "편의점행사_결과.xlsx"  # 저장할 엑셀 파일 이름
df.to_excel(output_file, index=False)  # encoding 파라미터 제거

print(f"\n완료! 결과가 '{output_file}'에 저장되었습니다.")