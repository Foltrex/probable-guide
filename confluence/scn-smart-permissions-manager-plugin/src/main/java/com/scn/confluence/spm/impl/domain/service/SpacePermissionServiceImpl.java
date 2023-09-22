package com.scn.confluence.spm.impl.domain.service;

import com.atlassian.confluence.api.service.content.SpaceService;
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
import com.atlassian.confluence.user.UserAccessor;
import com.google.common.base.Strings;
import com.scn.confluence.spm.api.domain.service.SpacePermissionService;
import com.scn.confluence.spm.impl.domain.dto.SpacePermissionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpacePermissionServiceImpl implements SpacePermissionService {
    private final SpaceManager spaceManager;
    private final UserAccessor userAccessor;
    private final ContentPermissionManager contentPermissionManager;
    private final ContentEntityObjectDao<ContentEntityObject> contentEntityObjectDao;

    @Override
    public List<SpacePermissionDto> getSpacePermissionBySpaceKey(String spaceKey) {
        List<Space> spaces = new ArrayList<>();
        List<SpacePermissionDto> spacePermissionDtos = new ArrayList<>();

        if (Strings.isNullOrEmpty(spaceKey)) {
            spaces.addAll(spaceManager.getAllSpaces());
        } else {
            spaces.add(spaceManager.getSpace(spaceKey));
        }

        for (Space space : spaces) {
            String statusName = SpaceStatus.CURRENT.name();
//          TODO: Check what is 0 and 10
            List<SpaceContentEntityObject> contentEntityObjects = contentEntityObjectDao.findContentBySpaceIdAndStatus(space.getId(), statusName.toLowerCase(), 0, 100);
            for (SpaceContentEntityObject spaceContentEntityObject : contentEntityObjects) {

                List<ContentPermission> contentPermissions = spaceContentEntityObject.getPermissions();
                Map<ConfluenceUser, ContentPermission> mainPermissions = new HashMap<>();
                for (ContentPermission contentPermission : contentPermissions) {
                    ContentPermission mainPermission = mainPermissions.get(contentPermission.getUserSubject());
                    if (mainPermission == null || Objects.equals(mainPermission.getType(), ContentPermission.VIEW_PERMISSION)) {
                        mainPermissions.put(contentPermission.getUserSubject(), contentPermission);
                    }
                }

                mainPermissions.forEach((confluenceUser, contentPermission) -> {
                    SpacePermissionDto spacePermissionDto = SpacePermissionDto.builder()
                        .id(String.format("%s-%s", space.getKey(), confluenceUser.getName()))
                        .spaceId(space.getId())
                        .spaceKey(space.getKey())
                        .permissionLevel(contentPermission.getType())
                        .username(confluenceUser.getName())
                        .build();
                    spacePermissionDtos.add(spacePermissionDto);
                });
            }
        }

        return spacePermissionDtos;
    }

    @Override
    public List<SpacePermissionDto> getSpacePermissions() {
        return getSpacePermissionBySpaceKey("");
    }

    @Override
    public SpacePermissionDto createSpacePermission(SpacePermissionDto spacePermissionDto) {
        ConfluenceUser user = userAccessor.getUserByName(spacePermissionDto.getUsername());
        String permissionLevel = spacePermissionDto.getPermissionLevel();
        List<ContentPermission> contentPermissions = new ArrayList<>();
        contentPermissions.add(
            ContentPermission.createUserPermission(permissionLevel, user)
        );
        contentPermissions.add(
            ContentPermission.createUserPermission(ContentPermission.SHARED_PERMISSION, user)
        );
        if (permissionLevel.equals(ContentPermission.EDIT_PERMISSION)) {
           contentPermissions.add(
               ContentPermission.createUserPermission(ContentPermission.VIEW_PERMISSION, user)
           );
        }

        String statusName = SpaceStatus.CURRENT.name();
        Space space = spaceManager.getSpace(spacePermissionDto.getSpaceKey());
        List<SpaceContentEntityObject> contentEntityObjects = contentEntityObjectDao.findContentBySpaceIdAndStatus(space.getId(), statusName.toLowerCase(), 0, 100);

        contentPermissions.forEach(contentPermission -> {
            contentEntityObjects.forEach(contentEntityObject -> {
                contentPermissionManager.addContentPermission(contentPermission, contentEntityObject);
            });
        });

        return SpacePermissionDto.builder()
            .id(String.format("%s-%s", space.getKey(), spacePermissionDto.getUsername()))
            .spaceId(space.getId())
            .spaceKey(space.getKey())
            .permissionLevel(spacePermissionDto.getPermissionLevel())
            .username(spacePermissionDto.getUsername())
            .build();
    }

    @Override
    public void deleteSpacePermission(SpacePermissionDto spacePermissionDto) {
        String statusName = SpaceStatus.CURRENT.name();
        List<SpaceContentEntityObject> contentEntityObjects = contentEntityObjectDao.findContentBySpaceIdAndStatus(spacePermissionDto.getSpaceId(), statusName.toLowerCase(), 0, 100);
        for (SpaceContentEntityObject spaceContentEntityObject : contentEntityObjects) {
            for (ContentPermission contentPermission : spaceContentEntityObject.getPermissions()) {
                contentPermissionManager.removeContentPermission(contentPermission);
            }
        }
    }


}
