package com.spring.familymoments.domain.family;

import com.spring.familymoments.config.BaseResponse;
import com.spring.familymoments.domain.awsS3.AwsS3Service;
import com.spring.familymoments.domain.family.model.*;
import com.spring.familymoments.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/families")
@Tag(name = "Family", description = "가족 API Document")
public class FamilyController {

    private final FamilyService familyService;
    private final AwsS3Service awsS3Service;


    /**
     * 가족 생성 API
     * [POST] /family/:familyId
     *
     * @return BaseResponse<PostFamilyRes>
     */
    @PostMapping(value ="/family", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "가족 생성", description = "가족 그룹을 생성합니다.")
    public BaseResponse<PostFamilyRes> createFamily(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam(name = "representImg") MultipartFile representImg,
            @Valid @RequestPart PostFamilyReq postFamilyReq) {

        String fileUrl = awsS3Service.uploadProfileImage(representImg);        // 대표 이미지 넣기

        PostFamilyRes postFamilyRes = familyService.createFamily(user, postFamilyReq, fileUrl);
        return new BaseResponse<>(postFamilyRes);
    }

    /**
     * 가족 정보 조회 API
     * [GET] /familyId
     *
     * @return BaseResponse<FamilyDto>
     */
    @Operation(summary = "가족 정보 조회", description = "가족아이디로 가족 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping(value = "/{familyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<FamilyRes> getFamily(@PathVariable Long familyId) {
        FamilyRes familyRes = familyService.getFamily(familyId);
        return new BaseResponse<>(familyRes);
    }

    /**
     * 닉네임 및 가족 생성일 조회 API
     * [GET] /:familyId/created
     *
     * @return BaseResponse<FamilyDto>
     */
    @GetMapping("/{familyId}/created")
    @Operation(summary = "닉네임 및 가족 생성일 조회", description = "메인 페이지의 닉네임 및 가족 생성일 정보를 조회합니다.")
    public BaseResponse<GetFamilyCreatedNicknameRes> getFamilyCreatedNickname(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable Long familyId) {
        GetFamilyCreatedNicknameRes getFamilyCreatedNicknameRes = familyService.getFamilyCreatedNickname(user, familyId);
        return new BaseResponse<>(getFamilyCreatedNicknameRes);
    }

    /**
     * 가족원 전체 조회 API
     * [GET] /:familyId/users?includeSelf=false
     *
     * @return BaseResponse<FamilyDto>
     */
    @GetMapping("/{familyId}/users")
    @Operation(summary = "가족원 전체 조회", description = "현재 활동 중인 전체 가족 구성원을 조회합니다.")
    public BaseResponse<List<GetFamilyAllResInterface>> getFamilyAllMembers(
            @PathVariable Long familyId,
            @RequestParam(defaultValue = "true") boolean includeSelf,
            @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        List<GetFamilyAllResInterface> getFamilyAllRes = familyService.getFamilyAllMembers(familyId, includeSelf, user);
        return new BaseResponse<>(getFamilyAllRes);
    }

    /**
     * 초대코드로 가족 정보 조회 API
     * [GET] /{inviteCode}/inviteCode
     *
     * @return BaseResponse<FamilyDto>
     */
    @Operation(summary = "초대코드로 가족 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PostMapping(value = "/inviteCode", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<FamilyRes> getFamilyByInviteCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            schema = @Schema(type = "object", example = "{\"inviteCode\": \"https://family-moment.com/invite/dsnj-548\"}")
                    )
            )
            @RequestBody Map<String, String> inviteCodeReq) {
        FamilyRes familyRes = familyService.getFamilyByInviteCode(inviteCodeReq.get("inviteCode"));
        return new BaseResponse<>(familyRes);
    }

    @Operation(summary = "가족에 가입")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PostMapping(value = "/{familyId}/join", produces = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<String> joinFamily(@PathVariable Long familyId,
                                    @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        familyService.joinFamily(user, familyId);
        return new BaseResponse<>("가족에 가입되었습니다");
    }

    /**
     * 초대 API
     * [GET] /familyId
     *
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 초대 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PostMapping(value = "/{familyId}/invitations", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> inviteUser(@PathVariable Long familyId,
                                           @RequestParam List<String> userIds,
                                           @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        familyService.inviteUser(user, userIds, familyId);
        return new BaseResponse<>("초대 요청이 완료되었습니다.");
    }

    /**
     * 초대 수락 API
     * [GET] /{familyId}/invitations/accept
     *
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 초대 수락 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PatchMapping(value = "/{familyId}/invitations/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> acceptFamily(@PathVariable Long familyId,
                                             @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        familyService.acceptFamily(user, familyId);
        return new BaseResponse<>("초대가 수락되었습니다.");
    }

    /**
     * 초대 거절 API
     * [GET] /{familyId}/invitations/reject
     *
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 초대 거절 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PatchMapping(value = "/{familyId}/invitations/reject", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> rejectFamily(@PathVariable Long familyId,
                                             @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        familyService.rejectFamily(user, familyId);
        return new BaseResponse<>("초대가 거절되었습니다.");
    }

    /**
     * 업로드 주기 수정 API
     * [PATCH] /:familyId?uploadCycle={업로드주기}
     *
     * @return BaseResponse<String>
     */
    @PatchMapping("/{familyId}")
    @Operation(summary = "업로드 주기 수정", description = "가족 업로드 알림 주기를 수정합니다.")
    public BaseResponse<String> updateUploadCycle(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable Long familyId,
            @RequestParam("uploadCycle") int uploadCycle) {
        familyService.updateUploadCycle(user, familyId, uploadCycle);
        return new BaseResponse<>("업로드 주기가 수정되었습니다.");
    }

    /**
     * 가족 삭제 API
     * [DELETE] /:familyId
     *
     * @return BaseResponse<String>
     */
    @DeleteMapping("/{familyId}")
    @Operation(summary = "가족 삭제", description = "가족을 삭제합니다. 댓글, 게시글, 가족이 일괄 삭제됩니다.")
    public BaseResponse<String> deleteFamily(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable Long familyId) {
        familyService.deleteFamily(user, familyId);
        return new BaseResponse<>("가족이 삭제되었습니다.");
    }

    /**
     * 가족 정보수정 API
     * [GET] /families/{familyId}
     *
     * @return BaseResponse<FamilyDto>
     */
    @Operation(summary = "가족 정보 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PatchMapping(value ="/{familyId}/update",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<FamilyRes> updateFamily(@PathVariable Long familyId,
                                                @AuthenticationPrincipal @Parameter(hidden = true) User user,
                                                @RequestParam(name = "representImg") MultipartFile representImg,
                                                @Valid @RequestPart FamilyUpdateReq familyUpdateReq){
        String fileUrl = awsS3Service.uploadProfileImage(representImg);
        FamilyRes familyRes = familyService.updateFamily(user, familyId, familyUpdateReq, fileUrl);
        return new BaseResponse<>(familyRes);
    }

    /**
     * 가족 탈퇴 API
     * [DELETE] /families/{familyId}/withdraw
     *
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @DeleteMapping(value = "/{familyId}/withdraw", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> withdrawFamily(@PathVariable Long familyId,
                                               @AuthenticationPrincipal @Parameter(hidden = true) User user) {

        familyService.withdrawFamily(user, familyId);
        return new BaseResponse<>("가족에서 탈퇴되었습니다.");
    }

    /**
     * 가족 강제 탈퇴 API
     * [DELETE] /families/{familyId}/emission
     *
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 강제 탈퇴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @DeleteMapping(value = "/{familyId}/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> emissionFamily(@PathVariable Long familyId,
                                               @AuthenticationPrincipal @Parameter(hidden = true) User user,
                                               @RequestParam List<String> userIds) {

        familyService.emissionFamily(user, familyId, userIds);
        return new BaseResponse<>("가족에서 탈퇴되었습니다.");
    }

    /**
     * 가족 권한 수정 API
     * [DELETE] /faimlies/{familyId}/authority
     */
    @Operation(summary = "가족 권한 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @PatchMapping(value = "/{familyId}/authority", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> changeFamilyAuthority(@PathVariable Long familyId,
                                                      @AuthenticationPrincipal @Parameter(hidden = true) User user,
                                                      @Valid @RequestBody FamilyAuthorityReq familyAuthorityReq) {
        familyService.changeFamilyAuthority(user, familyId, familyAuthorityReq);
        return new BaseResponse<>("가족 대표가 변경되었습니다.");
    }

    /**
     * 가족 권한 확인 API
     * [GET] /families/{familyId}/authority
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 권한 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/{familyId}/authority", produces = MediaType.APPLICATION_JSON_VALUE)
    BaseResponse<Object> getFamilyAuthority(@PathVariable Long familyId,
                                    @AuthenticationPrincipal @Parameter(hidden = true) User user) {
        Map<String, Boolean> result = Map.of("isOwner", familyService.getFamilyAuthority(user, familyId));
        return new BaseResponse<>(result);
    }

    /**
     * 내 가족 리스트 조회 API
     * [GET] /myfamilies
     */
    @ResponseBody
    @Operation(summary = "가족 리스트 조회", description = "OK")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping(value = "/myfamilies", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<Object> getFamily(@AuthenticationPrincipal @Parameter(hidden = true) User user) {
        List<MyFamilyRes> myFamilies = familyService.getMyFamilies(user);

        Map<String, Object> result = Map.of(
                "count", myFamilies.size(),
                "list", myFamilies
        );

        return new BaseResponse<>(result);
    }

    /**
     * 가족 이름 조회 API
     * [GET] /families/{familyId}/famillyName
     * @return BaseResponse<String>
     */
    @Operation(summary = "가족 이름 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    @GetMapping(value = "/{familyId}/famillyName", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse<String> getFamilyName(@AuthenticationPrincipal @Parameter(hidden = true) User user,
                                              @PathVariable Long familyId) {
        String familyName = familyService.getFamilyName(user, familyId);
        return new BaseResponse<>(familyName);
    }

}