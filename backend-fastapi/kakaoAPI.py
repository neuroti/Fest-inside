import requests
import pandas as pd
from sqlalchemy import create_engine


# API_KEY = '0b741865088d23db848333d7cf0233b8'
#
# def search_place(query):
#     url = "https://dapi.kakao.com/v2/local/search/keyword.json"
#     headers = {"Authorization": f"KakaoAK {API_KEY}"}
#     params = {"query": query}
#     response = requests.get(url, headers=headers, params=params)
#
#     return response.json()
#
#
#
# def get_places(query, category_group_code=None, x=None, y=None, radius=None):
#     url = "https://dapi.kakao.com/v2/local/search/keyword.json"
#     headers = {"Authorization": f"KakaoAK {API_KEY}"}
#     params = {"query": query, "size": 15}
#
#     if category_group_code:
#         params["category_group_code"] = category_group_code
#     if x and y and radius:
#         params.update({"x": x, "y": y, "radius": radius})
#
#     response = requests.get(url, headers=headers, params=params)
#
#     if response.status_code == 200:
#         return response.json()["documents"]
#     else:
#         raise Exception(f"API 호출 실패: {response.json()}")
#
# places = get_places("카페")
# df_places = pd.DataFrame(places)
# df_places_filtered = df_places[['place_name', 'address_name', 'category_group_name', 'x', 'y']]
# df_places_filtered = df_places_filtered.drop_duplicates(subset=['place_name', 'address_name'])
#
# print(df_places_filtered.head())
