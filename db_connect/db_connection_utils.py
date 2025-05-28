# database_utils.py

import pymysql


# MySQL 연결 설정
def create_database_connection():
    """
    MySQL 데이터베이스 연결을 생성하는 함수.

    Returns:
        Connection: 성공하면 pymysql Connection 객체 반환, 실패하면 None 반환
    """
    try:
        # 고정된 데이터베이스 연결 정보
        conn = pymysql.connect(
            host='localhost',  # MySQL 서버 호스트 (Docker는 localhost 또는 컨테이너 내부 IP 사용)
            port=3309,  # MySQL Docker 컨테이너 포트
            user='mapmo',  # 고정된 사용자명
            password='map123',  # 고정된 비밀번호
            database='MAP_MO',  # 고정된 데이터베이스 이름
            charset='utf8mb4'  # 문자셋 (utf8mb4로 고정)
        )
        print("DB 연결 성공!")
        return conn
    except pymysql.MySQLError as e:
        print("DB 연결 실패:", e)
        return None


# 이벤트 데이터를 MySQL 테이블에 삽입
def insert_event(conn, event_data):
    """
    MySQL 데이터베이스에 이벤트 데이터를 삽입하는 함수.
    
    Args:
        conn: pymysql Connection 객체
        event_data (tuple): 삽입할 이벤트 데이터 (title, content, url, brand_id, start_date, end_date, status)

    Returns:
        None
    """
    try:
        cursor = conn.cursor()
        sql = """
            INSERT IGNORE INTO event (title, content, url, brand_id, start_date, end_date, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """
        cursor.execute(sql, event_data)
        conn.commit()
        print("DB 삽입 성공:", event_data)
    except pymysql.MySQLError as e:
        print("DB 삽입 실패:", e)