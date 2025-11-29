package greenart.festival.member.entity;

import greenart.festival.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

    @Entity
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public class Member extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "member_id")
        private Long id;

        @Column(nullable = false, unique = true)
        private String email;

        private String name;

        @Column(nullable = false)
        private String password;

        @Enumerated(EnumType.STRING)
        @Builder.Default
        private Social provider = Social.NONE;

        @ElementCollection(fetch = FetchType.EAGER)
        @Enumerated(EnumType.STRING)
        @Builder.Default
        private Set<MemberRole> roles = new HashSet<>();


        public void addMemberRole(MemberRole role) {
            roles.add(role);
        }

//      추가정보 입력란
        @Column(name = "phone_number")
        private String phoneNumber;

        @Column(name = "birth_date")
        private LocalDate birthDate;

        @Enumerated(EnumType.STRING)
        private MBTI mbti;


//        public void setPassword(String password) {
//            this.password = password;
//        }
//
//        public void setPhoneNumber(String newPhoneNumber) {
//            this.phoneNumber = newPhoneNumber;
//        }
//
//        public void setBirthDate(String newBirthDate) {
//            this.birthDate = newBirthDate;
//        }
//
//        public void setMbti(MBTI newMbti) {
//            this.mbti = newMbti;
//        }

    }