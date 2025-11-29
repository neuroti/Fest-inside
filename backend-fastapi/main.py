import re
import time
from datetime import datetime
from selenium.webdriver.support import expected_conditions as EC

from fastapi import FastAPI, HTTPException, Depends
from selenium import webdriver
from selenium.webdriver import Keys
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.wait import WebDriverWait
from sqlalchemy.orm import Session

from starlette.requests import Request
from starlette.responses import HTMLResponse
from starlette.staticfiles import StaticFiles
from starlette.templating import Jinja2Templates

from MBTI import get_hybrid_recommendations, get_mbti_based_recommendations, get_mbti_recommendations_by_search
from KNN import get_knn_recommendations
from oracleDB import initialize_database, insert_update_festivals
from recommendations import SessionLocal

app = FastAPI()

app.mount("/static", StaticFiles(directory="static"), name="static") # static 폴더

templates = Jinja2Templates(directory="templates")

CHROMEDRIVER_PATH = "C:/chromedriver/chromedriver.exe"

@app.get("/recommend")
async def recommend(request: Request):
    return



@app.get("/", response_class=HTMLResponse)
async def read_festivals(request: Request, search_keyword: str, region_code: str):
    # FastAPI 엔드포인트
    try:
        initialize_database()
        festivals = get_festivals(search_keyword, region_code)
        if "error" in festivals:
            # 에러가 있을 경우 빈 리스트를 전달
            return templates.TemplateResponse("index.html", {"request": request, "festivals": []})

        insert_update_festivals(festivals.get("festivals", [])) # 오라클에 데이터 삽입

        return templates.TemplateResponse("index.html", {"request": request, "festivals": festivals.get("festivals", [])})

    except Exception as e:
        return templates.TemplateResponse("index.html", {"request": request, "festivals": [], "error": str(e)})

@app.get("/festivals")
def get_festivals(search_keyword : str, region_code: str):
    # Chrome 옵션 설정 (브라우저 창을 열지 않으려면 headless 모드 사용)
    options = webdriver.ChromeOptions()
    options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')

    # Chrome 드라이버 시작
    service = Service(executable_path=CHROMEDRIVER_PATH)
    driver = webdriver.Chrome(service=service, options=options)

    try:
        # 웹사이트 로드
        url = "https://www.mcst.go.kr/kor/s_culture/festival/festivalList.jsp"
        driver.get(url)

        # 검색지역 체크박스
        select_element = driver.find_element(By.ID, "pSido")
        Select(select_element).select_by_value(region_code)

        time.sleep(1)

        # 검색어 입력
        search_input = driver.find_element(By.ID, "pSearchWord")
        search_input.clear()
        search_input.send_keys(search_keyword)
        search_input.send_keys(Keys.RETURN)

        time.sleep(1)

        try:
            WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CLASS_NAME, "title")))
        except Exception as e:
            print(f"검색 결과 로드 실패: {e}")
            return {"error": "검색 결과 없음"}

        results = []

        def calculate_d_day(start_date_str):
            try:
                # 시작 날짜 문자열을 datetime 객체로 변환
                start_date = datetime.strptime(start_date_str, "%Y-%m-%d")
                today = datetime.today().date()

                # 디데이 계산
                delta = start_date.date() - today

                if delta.days > 0:
                    return f"D-{delta.days}"
                elif delta.days < 0:
                    return f"D+{abs(delta.days)}"
                else:
                    return "D-DAY"
            except Exception as e:
                return None # 날짜 형식이 잘못된 경우

        # 세부 내용을 파싱하는 함수 정의
        def parse_detail(detail_text):
            # 정규식을 사용하여 '기간', '장소', '문의'를 추출
            period_match = re.search(r'기간:\s*([\d\.\s\(\)~\-]+(?:\s*\|\s*[\d\.\s\(\)\-:~]+)?)', detail_text)
            location_match = re.search(r'장소:\s*([^\n]+)', detail_text)
            contact_match = re.search(r'문의:\s*([\d-]+)', detail_text)

            period = period_match.group(1) if period_match else None
            formatted_period = format_period(period)

            location = location_match.group(1) if location_match else None
            contact = contact_match.group(1) if contact_match else None

            start_date = extract_start_date(formatted_period)
            d_day = calculate_d_day(start_date) if start_date else None

            return {
                "기간": formatted_period,
                "장소": location,
                "문의": contact,
                "디데이": d_day
            }

        # 기간에서 시작일을 추출
        def extract_start_date(formatted_period):
            try:
                # 기간 문자열에서 시작일 부분을 추출
                start_date_match = re.search(r'(\d{4}\.\s*\d{1,2}\.\s*\d{1,2})', formatted_period)
                if start_date_match:
                    start_date_str = start_date_match.group(1).replace(' ', '').replace('.', '') # 공백 제거 및 . 제거
                    return f"{start_date_str[:4]}-{start_date_str[4:6]}-{start_date_str[6:]}"  # 'YYYY-MM-DD' 형태로 반환
                return None
            except Exception as e:
                return None

        def format_period(period):
            # 기간 정보에서 날짜 부분만 추출 (시간 정보는 제거)
            period = period.split("|")[0].strip()  # "|" 이후의 시간 정보 제거
            dates = re.split(r'\s*-\s*', period)

            try:
                # 시작일과 종료일을 표준화된 형식으로 변환
                start_date = parse_date(dates[0].strip())
                end_date = parse_date(dates[1].strip() if len(dates) > 1 else start_date)

                # 표준화된 형식으로 출력
                formatted_period = f"{start_date.strftime('%Y. %m. %d.')} ~ {end_date.strftime('%m. %d.')}"
                start_date = start_date.strftime('%Y-%m-%d')
                print(start_date)
                return formatted_period

            except Exception as e:
                # 날짜 형식 변환에 실패할 경우 원본 기간 반환
                return period

        def parse_date(date_str):
            # 날짜 문자열에서 요일 제거
            date_str = re.sub(r'\(\w\)', '', date_str).strip()  # (목), (토) 등 제거

            # 연도 정보가 없는 경우 현재 연도 추가
            if re.match(r'^\d{1,2}\.\s*\d{1,2}\.$', date_str):
                current_year = datetime.now().year
                date_str = f"{current_year}. {date_str}"

            # 날짜 문자열을 datetime 객체로 변환
            return datetime.strptime(date_str, "%Y. %m. %d.")

        while True:
            # 각 페이지에서 데이터 추출
            titles = driver.find_elements(By.CLASS_NAME, "title")
            contents = driver.find_elements(By.CLASS_NAME, "ny")
            details = driver.find_elements(By.CLASS_NAME, "detail_info")
            images = driver.find_elements(By.CSS_SELECTOR, "div.img img")

            for i in range(len(titles)):
                title = titles[i].text if len(titles) else None
                content = contents[i].text if i < len(contents) else None
                detail = details[i].text if i < len(details) else None
                image_src = images[i].get_attribute("src") if i < len(images) else None

                parsed_detail = parse_detail(detail)

                results.append({
                    "제목": title,
                    "내용": content,
                    "세부 내용": parsed_detail,
                    "이미지": image_src
                })

            # 페이지 번호와 현재 페이지 추적
            page_bar = driver.find_element(By.CSS_SELECTOR, "div.paging.pc")
            pages = page_bar.find_elements(By.CSS_SELECTOR, "a")
            page_now = page_bar.find_element(By.CSS_SELECTOR, ".on").text.strip()

            # 다음 페이지로 이동
            next_page = None
            for page in pages:
                page_num = page.text.strip()
                if page_num.isdigit() and int(page_num) > int(page_now):
                    next_page = page
                    break

            if next_page:
                next_page.click()
                time.sleep(3)  # 페이지 로드 대기
            else:
                # 더 이상 다음 페이지가 없으면 종료
                break

        return {"festivals": results}

    except Exception as e:
        return {"error": str(e)}

    finally:
        driver.quit()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.get("/knn_recommendations/{user_id}")
def knn_recommendations(user_id: int, k: int = 5, db: Session = Depends(get_db)):
    # Get recommendations for a user using K-Nearest Neighbors.

    recommendations = get_knn_recommendations(db, user_id, k)
    return {"user_id": user_id, "recommendations": recommendations}


@app.get("/mbti_recommendations/{user_id}")
def mbti_recommendations(user_id: int, k: int = 5, db: Session = Depends(get_db)):
    # Get recommendations using a hybrid of KNN and MBTI similarity.

    recommendations = get_mbti_based_recommendations(db, user_id, k)
    return {"user_id": user_id, "recommendations": recommendations}

@app.get("/recommendations/mbti")
def search_mbti_recommendations(search_mbti: str, k: int = 5, db: Session = Depends(get_db)):
    """
    API endpoint to get recommendations based on the search MBTI.
    """
    return get_mbti_recommendations_by_search(db, search_mbti, k)


@app.get("/hybrid_recommendations/{user_id}")
def hybrid_recommendations(user_id: int, k: int = 3, mbti_weight: float = 0.6, db: Session = Depends(get_db)):
    # Get recommendations using a hybrid of KNN and MBTI similarity.

    recommendations = get_hybrid_recommendations(db, user_id, k, mbti_weight)
    return {"user_id": user_id, "recommendations": recommendations}