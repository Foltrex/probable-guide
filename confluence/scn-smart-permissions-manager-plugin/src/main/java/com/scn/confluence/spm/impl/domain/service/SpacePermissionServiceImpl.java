package com.scn.confluence.spm.impl.domain.service;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.core.persistence.ContentEntityObjectDao;
import com.atlassian.confluence.security.ContentPermission;
import com.atlassian.confluence.security.ContentPermissionSet;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.spaces.SpaceStatus;
import com.atlassian.confluence.user.ConfluenceUser;
import com.google.common.base.Strings;
import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpacePermissionServiceImpl implements SpacePermissionService {
    private final SpaceManager spaceManager;
    private final ContentEntityObjectDao<ContentEntityObject> contentEntityObjectDao;
    private final ContentPermissionManager contentPermissionManager;

    @Override
    public List<SpacePermissionDto> getSpacePermissionBySpaceKey(String spaceKey) {
        if (Strings.isNullOrEmpty(spaceKey)) {
            List<Space> spaces = spaceManager.getAllSpaces();
        } else {
            Space spaces = spaceManager.getSpace(spaceKey);
        }
//        for (String superAdminName : superAdminNames) {
//            ConfluenceUser user = userAccessor.getUserByName(superAdminName);
//            boolean isSuperUser = userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, superAdminName);
//            if (!contentPermissionManager.hasContentLevelPermission(user, ContentPermission.VIEW_PERMISSION, contentPermissionEvent.getContent())
//                && user != null
//            ) {
//                ContentPermission permission = ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user);
//                contentPermissionManager.addContentPermission(permission, contentPermissionEvent.getContent());
//            }
//        }
//        superAdminNames.forEach(superAdminName -> {
//            ConfluenceUser user = userAccessor.getUserByName(superAdminName);
//            boolean isSuperUser = userAccessor.hasMembership(UserAccessor.GROUP_CONFLUENCE_ADMINS, superAdminName);
//            if (!contentPermissionManager.hasContentLevelPermission(user, ContentPermission.VIEW_PERMISSION, contentPermissionEvent.getContent())
//                && user != null
//            ) {
//                ContentPermission permission = ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user);
//                contentPermissionManager.addContentPermission(permission, contentPermissionEvent.getContent());
//            }
//        });
        return Collections.emptyList();
    }

    @Override
    public List<SpacePermissionDto> getSpacePermissions() {
        List<SpacePermissionDto> spacePermissionDtos = new ArrayList<>();
        List<Space> spaces = spaceManager.getAllSpaces();
        for (Space space : spaces) {
                String statusName = SpaceStatus.CURRENT.name();
                List<SpaceContentEntityObject> contentEntityObjects = contentEntityObjectDao.findContentBySpaceIdAndStatus(space.getId(), statusName.toLowerCase(), 0, 10);
                for (SpaceContentEntityObject spaceContentEntityObject : contentEntityObjects) {
                    Stream<ContentPermission> viewContentPermissionStream = contentPermissionManager.getContentPermissionSets(spaceContentEntityObject, ContentPermission.VIEW_PERMISSION)
                        .stream()
                        .flatMap(contentPermissionSet -> contentPermissionSet.contentPermissionsCopy().stream());
                    Stream<ContentPermission> editContentPermissionStream = contentPermissionManager.getContentPermissionSets(spaceContentEntityObject, ContentPermission.EDIT_PERMISSION)
                        .stream()
                        .flatMap(contentPermissionSet -> contentPermissionSet.contentPermissionsCopy().stream());
//
                    Map<ConfluenceUser, List<String>> permissionsPerUser = Stream.concat(
                            viewContentPermissionStream,
                            editContentPermissionStream
                        )
                        .collect(
                            Collectors.groupingBy(
                                ContentPermission::getUserSubject,
                                Collectors.mapping(ContentPermission::getType, Collectors.toList())
                            )
                        );
//
//                    permissionsPerUser.keySet()
//                        .forEach(confluenceUser -> {
//                            List<String> userPermissions = permissionsPerUser.get(confluenceUser);
//                            SpacePermissionDto spacePermissionDto;
//                            if (userPermissions.contains(ContentPermission.EDIT_PERMISSION)) {
//                                spacePermissionDto = SpacePermissionDto.builder()
//                                    .spaceId(space.getId())
//                                    .spaceKey(space.getKey())
//                                    .username(confluenceUser.getEmail())
//                                    .permissionLevel("VIEW-EDIT")
//                                    .build();
//
//                            } else {
//                                spacePermissionDto = SpacePermissionDto.builder()
//                                    .spaceId(space.getId())
//                                    .spaceKey(space.getKey())
//                                    .username(confluenceUser.getEmail())
//                                    .permissionLevel("VIEW")
//                                    .build();
//
//                            }
//                            spacePermissionDtos.add(spacePermissionDto);
//                        });
                }
        }

//
//        System.out.println(spacePermissionDtos);

////        return spaceManager.getAllSpaces()
////            .stream()
////            .map(space -> {
////                contentEntityObjectDao.findContentBySpaceIdAndStatus(space.getId());
////                contentEntityObjectDao.find
////            })
////            .collect(Collectors.toList());
////        Space space = spaceManager.getSpace("NS");
////        return Collections.emptyList();

//        return spacePermissionDtos;
        return Collections.emptyList();
    }
}
