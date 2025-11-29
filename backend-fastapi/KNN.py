import numpy as np
from fastapi import HTTPException
from sqlalchemy.orm import Session

from recommendations import Bookmark, Member


def get_user_bookmarks(db: Session):

    # Retrieve a dictionary mapping user IDs to the list of bookmarked board IDs.

    bookmarks = db.query(Bookmark.member_id, Bookmark.board_id).all()
    user_bookmarks = {}
    for member_id, board_id in bookmarks:
        if member_id not in user_bookmarks:
            user_bookmarks[member_id] = []
        user_bookmarks[member_id].append(board_id)
    return user_bookmarks



def calculate_similarity(user_bookmarks):

    # Calculate the similarity matrix for users based on their bookmarks.

    user_ids = list(user_bookmarks.keys())
    num_users = len(user_ids)

    # Create a user-festival matrix
    all_festivals = set(festival for bookmarks in user_bookmarks.values() for festival in bookmarks)
    festival_to_index = {festival: idx for idx, festival in enumerate(all_festivals)}
    user_festival_matrix = np.zeros((num_users, len(all_festivals)))

    for i, user_id in enumerate(user_ids):
        for festival in user_bookmarks[user_id]:
            user_festival_matrix[i, festival_to_index[festival]] = 1

        # Calculate the similarity matrix for users based on their bookmarks.

        user_ids = list(user_bookmarks.keys())
        num_users = len(user_ids)
    # Calculate cosine similarity
    similarity_matrix = np.dot(user_festival_matrix, user_festival_matrix.T)
    norms = np.linalg.norm(user_festival_matrix, axis=1)
    similarity_matrix /= np.outer(norms, norms)

    return similarity_matrix, user_ids



def get_knn_recommendations(db: Session, user_id, k: int = 5):
    """
    Get recommendations for a user using the K-Nearest Neighbors algorithm.
    """
    user_bookmarks = get_user_bookmarks(db)

    if not user_bookmarks:
        raise HTTPException(status_code=404, detail="No bookmark data found.")

    if user_id not in user_bookmarks:
        raise HTTPException(status_code=404, detail=f"User with ID {user_id} has no bookmarks.")

    similarity_matrix, user_ids = calculate_similarity(user_bookmarks)

    try:
        user_index = user_ids.index(user_id)
    except ValueError:
        raise HTTPException(status_code=404, detail=f"User with ID {user_id} not found in similarity matrix.")

    user_similarities = similarity_matrix[user_index]
    sorted_indices = np.argsort(-user_similarities)
    nearest_neighbors = [user_ids[i] for i in sorted_indices[:k] if user_ids[i] != user_id]

    recommended_boards = set()
    for neighbor_id in nearest_neighbors:
        recommended_boards.update(user_bookmarks[neighbor_id])

    user_boards = set(user_bookmarks[user_id])
    recommendations = recommended_boards - user_boards

    if not recommendations:
        raise HTTPException(status_code=404, detail="No recommendations available.")

    return list(recommendations)



# def mbti_to_vector(mbti: str):
#     """
#     Convert MBTI string into a numerical vector.
#     """
#     mbti_map = {
#         'E': 1, 'I': 0,
#         'S': 1, 'N': 0,
#         'T': 1, 'F': 0,
#         'J': 1, 'P': 0
#     }
#     return [mbti_map[char] for char in mbti]
#
#
#
# def calculate_mbti_similarity(target_mbti: str, other_mbti: str):
#     """
#     Calculate cosine similarity between two MBTI types.
#     """
#     target_vector = mbti_to_vector(target_mbti)
#     other_vector = mbti_to_vector(other_mbti)
#
#     dot_product = sum(t * o for t, o in zip(target_vector, other_vector))
#     norm_a = sum(t ** 2 for t in target_vector) ** 0.5
#     norm_b = sum(o ** 2 for o in other_vector) ** 0.5
#
#     if norm_a == 0 or norm_b == 0:
#         return 0  # Avoid division by zero
#     return dot_product / (norm_a * norm_b)
#
#
#
# def get_hybrid_recommendations(db: Session, user_id: int, k: int = 5, mbti_weight: float = 0.5):
#     """
#     Get recommendations using a hybrid of KNN and MBTI similarity.
#     """
#     # Step 1: Get user MBTI
#     user = db.query(Member).filter(Member.member_id == user_id).first()
#     if not user:
#         raise HTTPException(status_code=404, detail=f"User with ID {user_id} not found.")
#     user_mbti = user.mbti
#
#     # Step 2: Get KNN recommendations
#     user_bookmarks = get_user_bookmarks(db)
#     similarity_matrix, user_ids = calculate_similarity(user_bookmarks)
#     user_index = user_ids.index(user_id)
#     user_similarities = similarity_matrix[user_index]
#     sorted_indices = np.argsort(-user_similarities)
#     nearest_neighbors = [user_ids[i] for i in sorted_indices[:k] if user_ids[i] != user_id]
#
#     knn_recommended_boards = set()
#     for neighbor_id in nearest_neighbors:
#         knn_recommended_boards.update(user_bookmarks.get(neighbor_id, []))
#
#     # Step 3: Get MBTI-based recommendations
#     all_users = db.query(Member).filter(Member.member_id != user_id).all()
#     mbti_similarities = [
#         (other.member_id, calculate_mbti_similarity(user_mbti, other.mbti))
#         for other in all_users
#     ]
#     mbti_similarities = sorted(mbti_similarities, key=lambda x: x[1], reverse=True)[:k]
#     mbti_recommended_boards = set()
#     for similar_user_id, _ in mbti_similarities:
#         mbti_recommended_boards.update(user_bookmarks.get(similar_user_id, []))
#
#     # Step 4: Combine recommendations
#     knn_boards = {board: 1 for board in knn_recommended_boards}
#     mbti_boards = {board: 1 for board in mbti_recommended_boards}
#     combined_boards = {}
#
#     for board in set(knn_boards.keys()).union(mbti_boards.keys()):
#         combined_boards[board] = (
#             mbti_weight * mbti_boards.get(board, 0) +
#             (1 - mbti_weight) * knn_boards.get(board, 0)
#         )
#
#     # Step 5: Sort by combined score
#     sorted_recommendations = sorted(combined_boards.items(), key=lambda x: x[1], reverse=True)
#     recommendations = [board for board, _ in sorted_recommendations]
#
#     return recommendations


