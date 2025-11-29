package greenart.festival.bookmark.repository;

import greenart.festival.bookmark.dto.BoardBookmarkedDTO;
import greenart.festival.bookmark.dto.BookmarkDTO;
import greenart.festival.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark,Long> {

    @Query("select new greenart.festival.bookmark.dto.BookmarkDTO(bm.bookmarkId, bm.email, bm.bookmark, bm.board.boardId, bm.member.id) from Bookmark bm left outer join Member m on bm.member.id = m.id where bm.email =:email and bm.bookmark=true")
    List<BookmarkDTO> findBookmarksByEmail(@Param("email") String email);


    @Query("select new greenart.festival.bookmark.dto.BookmarkDTO(bm.bookmarkId, bm.email, bm.bookmark, bm.board.boardId, bm.member.id) from Bookmark bm left outer join Member m on bm.member.id = m.id left outer join Board b on bm.board.boardId = b.boardId where bm.email =:email and bm.board.boardId =:boardId and bm.bookmark=true")
    BookmarkDTO findBookmarkByEmailAndBoard(@Param("email") String email, @Param("boardId") Long boardId);

//    로그인 x 일 경우 축제 리스트를 볼 수 있도록 쿼리 추가 필요
    @Query("select new greenart.festival.bookmark.dto.BoardBookmarkedDTO" +
            "(b.boardId, b.title, b.content, b.period, b.location, b.latitude, b.longitude, b.contact, b.imageUrl, bm.bookmark) " +
            "from Board b left outer join Bookmark bm  on b.boardId = bm.board.boardId and bm.email =:email order by b.period ")
    List<BoardBookmarkedDTO> findAllBookmarkedList(@Param("email") String email);
    
//    bookmark 값만 변경 후, boardService 영역에서 구현(삭제, 추가)
@Query("select new greenart.festival.bookmark.dto.BoardBookmarkedDTO" +
        "(b.boardId, b.title, b.content, b.period, b.location, b.latitude, b.longitude, b.contact, b.imageUrl, bm.bookmark) " +
        "from Board b left outer join Bookmark bm on b.boardId = bm.board.boardId and bm.email =:email where b.boardId =:boardId order by b.period")
    BoardBookmarkedDTO findBookmarkByBoardIdAndEmail(Long boardId, String email);
}
