import time
import pandas as pd
import cx_Oracle
from geopy import Nominatim
from sqlalchemy import create_engine, false, null
from sqlalchemy import text

# 오라클 데이터베이스 연결
oracle_connection_string = 'oracle+cx_oracle://{username}:{password}@{host}:{port}/{database}'
engine = create_engine(
    oracle_connection_string.format(
        username='FESTIVAL',
        password='1234',
        host='localhost',
        port='1521',
        database='xe'
))

# 위도, 경도 캐시 저장을 위한 geolocator 인스턴스 생성
geolocator = Nominatim(user_agent="festival_locator")

# 위도, 경도 캐시 저장을 위한 딕셔너리
location_cache = {}
# 최초 데이터 베이스 초기화
is_initialized = False

# 주소를 입력받아 위도, 경도로 반환
def get_latitude_longitude(address):
    if address in location_cache: # 캐시에 저장되어 있다면
        return location_cache[address]

    location = geolocator.geocode(address) # 주소 기반으로 위치 정보 가져오기
    if location:
        location_cache[address] = (location.latitude, location.longitude)
        return location.latitude, location.longitude
    return None, None # 위치 정보 없으면 None 반환


def initialize_database():
    global is_initialized
    if not is_initialized:
        with engine.connect() as connection:
            try:
                check_query = text("SELECT COUNT(*) FROM BOARD")
                result = connection.execute(check_query)
                board_count = result.scalar()
                if board_count > 50:
                    delete_query1 = text("DELETE FROM REVIEW")
                    delete_query2 = text("DELETE FROM BOOKMARK")
                    delete_query3 = text("DELETE FROM BOARD")

                    connection.execute(delete_query1)
                    connection.execute(delete_query2)
                    connection.execute(delete_query3)
                    connection.commit()
                    print("데이터베이스 초기화 완료")
                    is_initialized = True
            except Exception as e:
                print(f"데이터베이스 초기화 실패: {e}")


# 축제 데이터를 오라클 데이터베이스에 삽입
def insert_update_festivals(festivals):
    # 축제 데이터 삽입 SQL문
    insert_query = text("""
    INSERT INTO BOARD (title, content, period, location, latitude, longitude, contact, image_url, views)
    VALUES (:title, :content, :period, :location, :latitude, :longitude, :contact, :image_url, :views)
    """)

    check_query = text("""
            SELECT COUNT(*) FROM BOARD WHERE title = :title AND period = :period
            """)


    # 연결 시작
    with engine.connect() as connection:
        # 트랜잭션 시작

            festival_data_list = []  # 중복되지 않은 데이터를 담을 리스트

            for festival in festivals:


                # 삽입할 축제 데이터 딕셔너리 형태로 준비
                festival_data = {
                    'title': festival['제목'],
                    'content': festival['내용'],
                    'period': festival['세부 내용']['기간'],
                    'location': festival['세부 내용']['장소'],
                    'contact': festival['세부 내용']['문의'],
                    'image_url': festival['이미지'],
                    'views': 0, # 조회수 0
                }

                # 위도, 경도 추출
                latitude, longitude = get_latitude_longitude(festival['세부 내용']['장소'])
                # 추출한 위도, 경도 데이터 추가
                festival_data['latitude'] = latitude
                festival_data['longitude'] = longitude

                # 중복 여부 확인
                result = connection.execute(check_query,
                                            {'title': festival_data['title'], 'period': festival_data['period']})

                # 중복된 레코드 수 가져오기
                count = result.scalar()
                if count == 0:  # 중복이 없으면 리스트에 추가
                    festival_data_list.append(festival_data)
                else:
                    print(f"중복된 데이터: {festival_data['title']} ({festival_data['period']})")

            # 중복되지 않은 데이터 한 번에 삽입
            if festival_data_list:
                try:
                    connection.execute(insert_query, festival_data_list)
                    connection.commit()
                    print(f"{len(festival_data_list)}개의 데이터를 성공적으로 삽입했습니다.")
                except Exception as e:
                    print(f"데이터 삽입 실패: {e}")
            else:
                print("삽입할 데이터가 없습니다.")



def get_bookmark():
    select_query = text("""SELECT * FROM bookmark""")
    with engine.connect() as connection:
        df_bookmarks = pd.read_sql(select_query, connection)
        print(df_bookmarks.head())


def get_board():
    select_query = text("""SELECT * FROM board""")
    with engine.connect() as connection:
        df_board = pd.read_sql(select_query, connection)
        print(df_board.head())