package com.tenant.tenant_service.service;

import com.tenant.tenant_service.dto.*;
import com.tenant.tenant_service.exception.BadRequestException;
import com.tenant.tenant_service.model.Organization;
import com.tenant.tenant_service.model.OrganizationMember;
import com.tenant.tenant_service.model.Role;
import com.tenant.tenant_service.repository.OrganizationMemberRepo;
import com.tenant.tenant_service.repository.OrganizationRepo;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


@Service
public class OrganizationService {
    private final OrganizationRepo organizationRepo;
    private final OrganizationMemberRepo memberRepo;
    private final UserService userService;

    public OrganizationService(OrganizationRepo organizationRepo, OrganizationMemberRepo memberRepo, UserService userService) {
        this.organizationRepo = organizationRepo;
        this.memberRepo = memberRepo;
        this.userService = userService;
    }

    public OrganizationResponse createOrganization(CreateOrganizationRequest request){
        Organization existOrg = organizationRepo.findByName(request.getName());
        if(existOrg != null){
            throw new BadRequestException("Organization already exists");
        }
        if (request.getAuthId() == null) {
            throw new BadRequestException("Owner authId is required");
        }
        Organization org = Organization.builder()
                .name(request.getName())
                .ownerAuthId(request.getAuthId())
                .domain(request.getDomain())
                .build();

        Organization savedOrg = organizationRepo.save(org);
        OrganizationMember owner = OrganizationMember.builder()
                .orgId(savedOrg.getId())
                .authId(savedOrg.getOwnerAuthId())
                .role(Role.OWNER)
                .joinedAt(OffsetDateTime.now())
                .build();

        memberRepo.save(owner);
        return toOrganizationResponse(savedOrg);

    }

    public MemberResponse addMember(String orgId, AddMemberRequest request){
        System.out.println("kamarial gole gole nrng");
        System.out.println(request.getPerformedBy());
        Role actorRole = getRoleByAuthId(orgId, request.getPerformedBy());
        System.out.println("Role : "+actorRole);
        if (actorRole == null ||
                (actorRole != Role.ADMIN &&
                        actorRole != Role.OWNER &&
                        actorRole != Role.MANAGER)) {

            throw new BadRequestException("You are not authorized to add member");
        }

       UserLookupResponse userResponse = userService.lookupUserByEmail(request.getEmail());
        if (!userResponse.exists){
            System.out.println("User not found");
            System.out.println("You need to send a invitation to user");
            throw new BadRequestException("User not found");
        }




        String authId = userResponse.authId;
        System.out.println("AuthId : "+authId);
        //Check user is already member of organization
        OrganizationMember existMember = memberRepo.findByOrgIdAndAuthId(orgId, authId);
        System.out.println("ExistMember : "+existMember);
        if(existMember != null){
            System.out.println("User is already member of organization");
            throw new BadRequestException("User is already member of organization");
        }
        OrganizationMember newMember = OrganizationMember.builder()
                .authId(authId)
                .orgId(orgId)
                .role(request.getRole())
                .joinedAt(OffsetDateTime.now())
                .build();

        OrganizationMember savedMember = memberRepo.save(newMember);


        return toMemberResponse(savedMember);

    }

    private Role getRoleByAuthId(String orgId, String performedBy) {

        OrganizationMember member = memberRepo.findByOrgIdAndAuthId(orgId, performedBy);

        if (member == null) {
            System.out.println("Actor is not a member of this organization");
            return null;   // or throw an exception if preferred
        }

        return member.getRole();
    }



    private OrganizationResponse toOrganizationResponse(Organization org){
        return OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .ownerAuthId(org.getOwnerAuthId())
                .domain(org.getDomain())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }

    private MemberResponse toMemberResponse(OrganizationMember member){
        return MemberResponse.builder()
                .id(member.getId())
                .authId(member.getAuthId())
                .orgId(member.getOrgId())
                .role(String.valueOf(member.getRole()))
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
