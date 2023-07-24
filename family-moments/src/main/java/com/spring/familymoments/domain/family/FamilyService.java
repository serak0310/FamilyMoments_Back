package com.spring.familymoments.domain.family;

import com.spring.familymoments.domain.common.UserFamilyRepository;
import com.spring.familymoments.domain.common.entity.UserFamily;
import com.spring.familymoments.domain.family.entity.Family;
import com.spring.familymoments.domain.family.model.FamilyDto;
import com.spring.familymoments.domain.family.model.PostFamilyReq;
import com.spring.familymoments.domain.family.model.PostFamilyRes;
import com.spring.familymoments.domain.user.UserRepository;
import com.spring.familymoments.domain.user.UserService;
import com.spring.familymoments.domain.user.entity.User;
import com.sun.xml.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.spring.familymoments.domain.common.entity.UserFamily.Status.ACTIVE;
import static com.spring.familymoments.domain.common.entity.UserFamily.Status.DEACCEPT;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final UserService userService;
    private final UserFamilyRepository userFamilyRepository;
    private final UserRepository userRepository;

    public PostFamilyRes createFamily(User owner, PostFamilyReq postFamilyReq) {
        Family family = Family.builder()
                .owner(owner)
                .familyName(postFamilyReq.getFamilyName())
                .uploadCycle(postFamilyReq.getUploadCycle())
                .inviteCode("1111111")
                .representImg(postFamilyReq.getRepresentImg())
                .build();
        Family saveFamily = familyRepository.save(family);

        return new PostFamilyRes(saveFamily.getFamilyId(), owner.getNickname(), saveFamily.getInviteCode(), owner.getProfileImg(), saveFamily.getRepresentImg(), saveFamily.getCreatedAt());
    }


    //특정 가족 정보 조회
    public FamilyDto getFamily(Long id){
        Optional<Family> family = familyRepository.findById(id);

        if (family.isEmpty()) {
            throw new NoSuchElementException("존재하지 않습니다");
        }

        return FamilyDto.builder()
                .owner(family.get().getOwner().getNickname())
                .familyName(family.get().getFamilyName())
                .uploadCycle(family.get().getUploadCycle())
                .inviteCode(family.get().getInviteCode())
                .representImg(family.get().getRepresentImg())
                .build();

    }

    public FamilyDto getFamilyByInviteCode(String inviteCode){
        Optional<Family> family = familyRepository.findByInviteCode(inviteCode);

        if (family.isEmpty()) {
            throw new NoSuchElementException("존재하지 않습니다");
        }

        return FamilyDto.builder()
                .owner(family.get().getOwner().getNickname())
                .familyName(family.get().getFamilyName())
                .uploadCycle(family.get().getUploadCycle())
                .inviteCode(family.get().getInviteCode())
                .representImg(family.get().getRepresentImg())
                .build();

    }

    // 가족 초대
    public void inviteUser(List<Long> userIdList, Long familyId) throws IllegalAccessException {
        Optional<Family> familyOptional = familyRepository.findById(familyId);
        // 1. 리스트의 유저들이 family에 가입했는지 확인
        // 가입 -> 테이블에 존재&&상태 ACTIVE
        if (familyOptional.isPresent()) {
            Family family = familyOptional.get();

            for (Long ids : userIdList) {
                Optional<UserFamily> byUserId = userFamilyRepository.findByUserId(userRepository.findById(ids));

                if (byUserId.isPresent() && byUserId.get().getStatus() == ACTIVE) {
                    throw new IllegalAccessException("이미 가족에 가입된 회원입니다.");
                }if(byUserId.isPresent() && byUserId.get().getStatus() == DEACCEPT && byUserId.get().getFamilyId() == family){
                    throw new IllegalAccessException("이미 초대 요청을 보낸 회원입니다.");
                } else {
                    User user = userRepository.findById(ids)
                            .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

                    UserFamily userFamily = UserFamily.builder()
                            .familyId(family)
                            .userId(user)
                            .status(DEACCEPT).build();

                    userFamilyRepository.save(userFamily);
                }
            }
        } else {
            throw new NoSuchElementException("가족을 찾을 수 없습니다.");
        }
    }

    // 가족 초대 수락
    public void acceptFamily(Long userId, Long familyId){
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Family> familyOptional = familyRepository.findById(familyId);

        if (userOptional.isPresent() && familyOptional.isPresent()) {
            User user = userOptional.get();
            Family family = familyOptional.get();

            Optional<UserFamily> userFamily = userFamilyRepository.findByUserIdAndFamilyId(user, family);

            // 1. 매핑 테이블에서 userId와 familyId로 검색
            if (userFamily.isPresent()) {
                // 2. 상태 바꿔줌
                UserFamily updatedUserFamily = userFamily.get().toBuilder()
                        .status(ACTIVE)
                        .build();

                userFamilyRepository.save(updatedUserFamily);
            } else {
                throw new NoSuchElementException("존재하지 않는 초대 내역입니다.");
            }
        } else {
            throw new NoSuchElementException("존재하지 않는 사용자 또는 가족입니다.");
        }
    }

}