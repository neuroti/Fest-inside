package greenart.festival.member.service;

import greenart.festival.bookmark.service.BookmarkService;
import greenart.festival.member.dto.AddtionalInfoDTO;
import greenart.festival.member.dto.MemberRegisterDTO;
import greenart.festival.member.entity.MBTI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Random;

@SpringBootTest
class MemberRegisterServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private BookmarkService bookmarkService;



    @Test
    void registerMember() {

        MBTI[] mbtiTypes = MBTI.values();
        Random random = new Random();



        for (int i = 1; i <= 1000 ; i++) {


            MBTI mbti = mbtiTypes[random.nextInt(mbtiTypes.length)];
            String phoneNumber = "010-" + (1000 + random.nextInt(9000))
                    + "-" +(1000 + random.nextInt(9000));
            LocalDate birthday = LocalDate.of((1985 + random.nextInt(25)), (1 + random.nextInt(12)), (1 + random.nextInt(28)));


            MemberRegisterDTO memberRegisterDTO = MemberRegisterDTO.builder()
                    .email("user"+ i +"@example.com")
                    .name("User "+i)
                    .password("1111")
                    .build();

            String email = memberRegisterDTO.getEmail();


            AddtionalInfoDTO addtionalInfoDTO = AddtionalInfoDTO.builder()
                    .phoneNumber(phoneNumber)
                    .birthDate(birthday)
                    .mbti(mbti)
                    .build();

            memberService.register(memberRegisterDTO);

            memberService.updateAddtionalInfo(email,addtionalInfoDTO);
        }

    }


    @Test
    void randomBookmarking(){

        Random random = new Random();

        for (int i = 1; i <= 1000 ; i++) {
            String email = "user" + i + "@example.com";

            for (int j = 1; j <=3; j++) {
                Long boardId = (1 + random.nextLong(46));

                bookmarkService.bookmarking(email, boardId);
            }

        }
    }
}