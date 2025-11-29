package greenart.festival.member.dto;

import greenart.festival.member.entity.MBTI;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class AddtionalInfoDTO {

    private String phoneNumber;

    private LocalDate birthDate;

    private MBTI mbti;
}
