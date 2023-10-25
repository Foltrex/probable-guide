package com.scn.confluence.spm.impl.domain.service;

import com.atlassian.confluence.api.service.content.SpaceService;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.core.persistence.ContentEntityObjectDao;
import com.atlassian.confluence.pages.Page;
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
            Page homePage = space.getHomePage();
            List<ContentPermission> contentPermissions = homePage.getPermissions();
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
        ContentPermission contentPermission = ContentPermission.createUserPermission(
            permissionLevel, user
        );
        Space currentSpace = spaceManager.getSpace(spacePermissionDto.getSpaceKey());
        Page homePage = currentSpace.getHomePage();
        contentPermissionManager.addContentPermission(contentPermission, homePage);

        return SpacePermissionDto.builder()
            .id(String.format("%s-%s", spacePermissionDto.getSpaceKey(), spacePermissionDto.getUsername()))
            .spaceKey(spacePermissionDto.getSpaceKey())
            .permissionLevel(spacePermissionDto.getPermissionLevel())
            .username(spacePermissionDto.getUsername())
            .build();
    }

    @Override
    public SpacePermissionDto updateSpacePermission(String spaceKey, String username, SpacePermissionDto spacePermissionDto) {
        Space currentSpace = spaceManager.getSpace(spaceKey);
        Page homePage = currentSpace.getHomePage();
        homePage.getPermissions()
            .forEach(contentPermission -> {
                ConfluenceUser user = userAccessor.getUserByName(username);
                if (Objects.equals(contentPermission.getUserSubject(), user)) {
                    contentPermissionManager.removeContentPermission(contentPermission);
                }
            });

        ConfluenceUser user = userAccessor.getUserByName(username);
        String permissionLevel = spacePermissionDto.getPermissionLevel();
        ContentPermission contentPermission = ContentPermission.createUserPermission(
            permissionLevel, user
        );

        contentPermissionManager.addContentPermission(contentPermission, homePage);

        return SpacePermissionDto.builder()
            .id(String.format("%s-%s", spaceKey, username))
            .spaceKey(spaceKey)
            .permissionLevel(spacePermissionDto.getPermissionLevel())
            .username(username)
            .build();
    }

    @Override
    public void deleteSpacePermission(String spaceKey, String username) {
        Space currentSpace = spaceManager.getSpace(spaceKey);
        Page homePage = currentSpace.getHomePage();
        homePage.getPermissions()
            .forEach(contentPermission -> {
                ConfluenceUser user = userAccessor.getUserByName(username);
                if (Objects.equals(contentPermission.getUserSubject(), user)) {
                    contentPermissionManager.removeContentPermission(contentPermission);
                }
            });
    }


}
