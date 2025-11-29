package greenart.festival.board;

import greenart.festival.bookmark.dto.BoardBookmarkedDTO;
import greenart.festival.bookmark.dto.BookmarkDTO;
import greenart.festival.bookmark.entity.Bookmark;
import greenart.festival.bookmark.service.BookmarkService;
import greenart.festival.member.dto.MemberAuthDTO;
import greenart.festival.member.entity.Member;
import greenart.festival.member.repository.MemberRepository;
import greenart.festival.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BoardRestController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkService bookmarkService;


    @GetMapping("/list")
    public List<BoardBookmarkedDTO> getList(Principal principal) {
        String email = principal.getName();
        System.out.println("boardService.getAllList() = " + boardService.getAllList(email));
        return boardService.getAllList(email);
    }

    @GetMapping("/popular")
    public List<BoardViewsDTO> getPopularBoards() {
        return boardService.getPopularBoards();
    }

    @PostMapping("/popular/{boardId}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long boardId) {
        boardService.increaseViewCount(boardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<Bookmark>> getBookmarks(Principal principal) {

        String email = principal.getName();
        System.out.println("email = " + email);
        Member member = memberRepository.findByEmail(email).get();
        List<BookmarkDTO> bookmarkDTOS = bookmarkService.getBookmarks(member.getEmail());
        System.out.println("bookmarkDTOS = " + bookmarkDTOS);
        List<Bookmark> bookmarks = new ArrayList<>();


        for (BookmarkDTO bookmarkDTO : bookmarkDTOS) {
            BoardDTO boardDTO = boardService.getBoard(bookmarkDTO.getBoardId());
            Board board = boardService.dtoToEntity(boardDTO);


            Bookmark bookmark = Bookmark.builder()
                    .bookmarkId(bookmarkDTO.getBookmarkId())
                    .email(bookmarkDTO.getEmail())

                    .board(board)
                    .member(member)
                    .build();

            System.out.println("bookmark = " + bookmark);
            bookmarks.add(bookmark);
        }

        System.out.println("bookmarks = " + bookmarks);
        return ResponseEntity.ok(bookmarks);
    }


    @GetMapping("/map/{boardId}/bookmark")
    public ResponseEntity<Map<String, Object>> bookmark(@PathVariable("boardId") Long boardId, @AuthenticationPrincipal MemberAuthDTO memberAuthDTO) {
        Member member = memberService.dtoToEntity(memberAuthDTO);

        System.out.println("member = " + member);
        System.out.println("boardId = " + boardId);
        boolean isBookmarked = bookmarkService.bookmarking(member.getEmail(), boardId);
        System.out.println("isBookmarked = " + isBookmarked);

        Map<String, Object> response = new HashMap<>();
        response.put("boardId", boardId);
        response.put("isBookmarked", isBookmarked);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/map/{boardId}/bookmark-status")
//    public ResponseEntity<Map<String, Object>> getBookmarkStatus(@PathVariable("boardId") Long boardId,
//                                                                 @AuthenticationPrincipal MemberAuthDTO memberAuthDTO) {
//        Member member = memberService.dtoToEntity(memberAuthDTO);
//
//        boolean isBookmarked = bookmarkService.isBookmarked(member.getEmail(), boardId);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("isBookmarked", isBookmarked);
//
//        return ResponseEntity.ok(response);
//    }


    @GetMapping("/search")
    public List<Board> searchFestivals(@RequestParam String keyword) {

        System.out.println("keyword = " + keyword);

        return boardService.searchFestivals(keyword);
    }
}
