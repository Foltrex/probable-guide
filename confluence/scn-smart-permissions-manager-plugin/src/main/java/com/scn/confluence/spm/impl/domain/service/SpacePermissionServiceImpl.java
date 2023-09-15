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

import java.util.*;
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
        List<Space> spaces = new ArrayList<>();
        List<SpacePermissionDto> spacePermissionDtos = new ArrayList<>();

        if (Strings.isNullOrEmpty(spaceKey)) {
            spaces.addAll(spaceManager.getAllSpaces());
        } else {
            spaces.add(spaceManager.getSpace(spaceKey));
        }

        for (Space space : spaces) {
            String statusName = SpaceStatus.CURRENT.name();
            List<SpaceContentEntityObject> contentEntityObjects = contentEntityObjectDao.findContentBySpaceIdAndStatus(space.getId(), statusName.toLowerCase(), 0, 10);
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
                        .spaceId(space.getId())
                        .spaceKey(space.getKey())
                        .permissionLevel(contentPermission.getType())
                        .username(confluenceUser.getEmail())
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
}
