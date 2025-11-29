package greenart.festival.member.service;

import greenart.festival.member.dto.AddtionalInfoDTO;
import greenart.festival.member.dto.ChangePasswordDTO;
import greenart.festival.member.dto.MemberRegisterDTO;
import greenart.festival.member.dto.MyPageDTO;
import greenart.festival.member.entity.MBTI;
import greenart.festival.member.entity.Member;
import greenart.festival.member.entity.Social;
import greenart.festival.member.repository.MemberRepository;
import greenart.festival.review.dto.CommentDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberRepository memberRepository;


    @Override
    public Long register(MemberRegisterDTO dto) {
        String password = dto.getPassword();

        dto.setPassword(passwordEncoder.encode(password));
        Member member = dtoToEntity(dto);
        memberRepository.save(member);


        return member.getId();
    }


//    @Override
//    public MemberRegisterDTO read(String loginId) {
//        return null;
//    }

//    @Override
//    public MemberRegisterDTO read(String email) {
//
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Member not found"));
//
//        MemberRegisterDTO memberRegisterDTO = new MemberRegisterDTO(
//                member.getEmail(),
//                member.getPassword(),
//                member.getName()
//        );
//
//
//        return null;
//    }

    @Override
    public MyPageDTO getMyPage(String email) {
        Member member = memberRepository.findByEmail(email, Social.NONE)
                .orElseThrow(() -> new RuntimeException("Member not found"));




        List<CommentDTO> comments = memberRepository.findCommentIdsByEmail(member.getEmail());


        return new MyPageDTO(
                member.getEmail(),
                member.getName(),
                member.getPassword(),
                member.getProvider(),
                comments,
                member.getPhoneNumber(),
                member.getBirthDate(),
                member.getMbti()
        );
    }

    @Override
    @Transactional
    public void updateAddtionalInfo(String email, AddtionalInfoDTO dto) {
        LocalDate birthDate = dto.getBirthDate();
        String phoneNumber = dto.getPhoneNumber();
        MBTI mbti = dto.getMbti();

        memberRepository.addtionalInfoByEmail(email, birthDate, phoneNumber, mbti);
        Member member = memberRepository.findByEmail(email).get();
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordDTO request) {
        Member member = memberRepository.findByEmail(email).get();

        if(member.getProvider().equals(Social.NONE)) {

            if (passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
                System.out.println("비밀번호를 변경하겠습니다.");
                memberRepository.updatePassword(email, passwordEncoder.encode(request.getNewPassword()));
                memberRepository.save(member);
            }

        }else {
            System.out.println("기본 회원이 아닙니다. 소셜 로그인을 이용하거나 아이디 또는 비밀번호를 확인해주세요.");
        }

    }


//    @Override
//    public List<Long> getUserComments(String email) {
//        return List.of();
//    }


}
