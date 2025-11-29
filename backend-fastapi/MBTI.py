import numpy as np
from fastapi import HTTPException
from sqlalchemy.orm import Session

from KNN import get_user_bookmarks, calculate_similarity
from recommendations import Member, Board


def mbti_to_vector(mbti: str):

    # Convert MBTI string into a numerical vector.

    mbti_map = {
        'E': 1, 'I': 0,
        'S': 1, 'N': 0,
        'T': 1, 'F': 0,
        'J': 1, 'P': 0
    }
    return [mbti_map[char] for char in mbti]



def calculate_mbti_similarity(target_mbti: str, other_mbti: str):

    # Calculate cosine similarity between two MBTI types.

    target_vector = mbti_to_vector(target_mbti)
    other_vector = mbti_to_vector(other_mbti)

    dot_product = sum(t * o for t, o in zip(target_vector, other_vector))
    norm_a = sum(t ** 2 for t in target_vector) ** 0.5
    norm_b = sum(o ** 2 for o in other_vector) ** 0.5

    if norm_a == 0 or norm_b == 0:
        return 0  # Avoid division by zero
    return dot_product / (norm_a * norm_b)



# MBTI 기반 추천 함수
def get_mbti_based_recommendations(db: Session, user_id: int, k: int = 5):
    """
    Get recommendations using only MBTI similarity.
    """
    # Step 1: Get user MBTI
    user = db.query(Member).filter(Member.member_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail=f"User with ID {user_id} not found.")
    user_mbti = user.mbti

    # Step 2: Calculate MBTI similarities with other users
    all_users = db.query(Member).filter(Member.member_id != user_id).all()
    mbti_similarities = [
        (other.member_id, calculate_mbti_similarity(user_mbti, other.mbti))
        for other in all_users
    ]
    mbti_similarities = sorted(mbti_similarities, key=lambda x: x[1], reverse=True)[:k]

    # Step 3: Get recommendations from similar users
    user_bookmarks = get_user_bookmarks(db)
    recommended_boards = set()
    for similar_user_id, _ in mbti_similarities:
        recommended_boards.update(user_bookmarks.get(similar_user_id, []))

    # Step 4: Return recommendations as a list
    return list(recommended_boards)



def get_mbti_recommendations_by_search(db: Session, search_mbti: str, k: int = 5):
    """
    Search for recommendations based on a specified MBTI type.
    """
    # Step 1: Validate the search MBTI
    if len(search_mbti) != 4 or any(char not in 'EISNTFJP' for char in search_mbti):
        raise HTTPException(status_code=400, detail="Invalid MBTI format.")

    # Step 2: Calculate MBTI similarities with all users
    all_users = db.query(Member).all()
    mbti_similarities = [
        (user.member_id, calculate_mbti_similarity(search_mbti, user.mbti))
        for user in all_users if user.mbti
    ]
    mbti_similarities = sorted(mbti_similarities, key=lambda x: x[1], reverse=True)[:k]

    # Step 3: Get recommendations from similar users
    user_bookmarks = get_user_bookmarks(db)
    recommended_boards = set()
    for similar_user_id, _ in mbti_similarities:
        recommended_boards.update(user_bookmarks.get(similar_user_id, []))

    # Step 4: Fetch detailed board information
    board_details = db.query(Board).filter(Board.board_id.in_(recommended_boards)).all()

    return board_details




def get_hybrid_recommendations(db: Session, user_id: int, k: int = 5, mbti_weight: float = 0.6):
    """
    Get recommendations using a hybrid of KNN and MBTI similarity.
    """
    # Step 1: Get user MBTI
    user = db.query(Member).filter(Member.member_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail=f"User with ID {user_id} not found.")
    user_mbti = user.mbti

    # Step 2: Get KNN recommendations
    user_bookmarks = get_user_bookmarks(db)
    similarity_matrix, user_ids = calculate_similarity(user_bookmarks)
    user_index = user_ids.index(user_id)
    user_similarities = similarity_matrix[user_index]
    sorted_indices = np.argsort(-user_similarities)
    nearest_neighbors = [user_ids[i] for i in sorted_indices[:k] if user_ids[i] != user_id]

    knn_recommended_boards = set()
    for neighbor_id in nearest_neighbors:
        knn_recommended_boards.update(user_bookmarks.get(neighbor_id, []))

    # Step 3: Get MBTI-based recommendations
    all_users = db.query(Member).filter(Member.member_id != user_id).all()
    mbti_similarities = [
        (other.member_id, calculate_mbti_similarity(user_mbti, other.mbti))
        for other in all_users
    ]
    mbti_similarities = sorted(mbti_similarities, key=lambda x: x[1], reverse=True)[:k]
    mbti_recommended_boards = set()
    for similar_user_id, _ in mbti_similarities:
        mbti_recommended_boards.update(user_bookmarks.get(similar_user_id, []))

    # Step 4: Combine recommendations
    knn_boards = {board: 1 for board in knn_recommended_boards}
    mbti_boards = {board: 1 for board in mbti_recommended_boards}
    combined_boards = {}

    for board in set(knn_boards.keys()).union(mbti_boards.keys()):
        combined_boards[board] = (
            mbti_weight * mbti_boards.get(board, 0) +
            (1 - mbti_weight) * knn_boards.get(board, 0)
        )

    # Step 5: Sort by combined score
    sorted_recommendations = sorted(combined_boards.items(), key=lambda x: x[1], reverse=True)
    recommendations = [board for board, _ in sorted_recommendations]

    return recommendations

