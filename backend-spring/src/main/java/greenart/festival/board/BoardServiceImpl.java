package greenart.festival.board;

import greenart.festival.bookmark.dto.BoardBookmarkedDTO;
import greenart.festival.bookmark.entity.Bookmark;
import greenart.festival.bookmark.repository.BookmarkRepository;
import greenart.festival.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;


    @Override
    public List<BoardBookmarkedDTO> getAllList(String email) {
//        List<BoardDTO> allList = boardRepository.findAllList();
//
//        Set<String> titles = new HashSet<>();
//        List<BoardDTO> uniqueList = new ArrayList<>();
//
//        for(BoardDTO boardDTO : allList) {
//            if(!titles.contains(boardDTO.getTitle())) {
//                titles.add(boardDTO.getTitle());
//                uniqueList.add(boardDTO);
//            }
//        }
//        return uniqueList;

        List<BoardBookmarkedDTO> bookmarkedList = bookmarkRepository.findAllBookmarkedList(email);
        for (BoardBookmarkedDTO bookmarkedDTO : bookmarkedList) {
            Boolean bookmark = bookmarkedDTO.getBookmark();
            System.out.println("bookmark = " + bookmark);
            if(bookmark == null || !bookmark) {
                bookmarkedDTO.setBookmark(false);
            }
        }


        return  bookmarkedList;
    }

    @Override
    public BoardBookmarkedDTO getBookmarkedBoard(Long boardId, String email) {

        BoardBookmarkedDTO bookmarkedBoard = bookmarkRepository.findBookmarkByBoardIdAndEmail(boardId, email);
        Boolean bookmark = bookmarkedBoard.getBookmark();

        if(bookmark == null || !bookmark) {
            bookmarkedBoard.setBookmark(false);
        }
        System.out.println("bookmarkedBoard = " + bookmarkedBoard);

        return bookmarkedBoard;
    }

    @Override
    public BoardDTO getBoard(Long boardId) {
        return boardRepository.findByBoardId(boardId);
    }

    @Override
    public List<BoardViewsDTO> getPopularBoards() {
        return boardRepository.findTop10ByViews();
    }

    @Transactional
    @Override
    public void increaseViewCount(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Festival not found"));

        Board increaseBoard = board.toBuilder()
                .views(board.getViews() + 1)
                .build();
        boardRepository.save(increaseBoard);
    }

    @Override
    public List<Board> searchFestivals(String keyword) {

        List<Board> festivals = boardRepository.findAll().stream()
                .filter(festival ->
                        festival.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                festival.getContent().toLowerCase().contains(keyword.toLowerCase()) ||
                                festival.getLocation().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());


        System.out.println("festivals = " + festivals);

        return festivals;
    }
}
