package com.scn.jira.worklog.core.wl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.util.DefaultBaseUrl;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.google.common.collect.Lists;
import com.scn.jira.worklog.core.lazyloading.LazyLoadingCache;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named("defaultExtendedConstantsManager")
public class DefaultExtendedConstantsManager implements ExtendedConstantsManager {
    // private List worklogTypes;
    // private Map worklogTypeObjectsMap;
    private ConstantsManager constantsManager;
    private JiraAuthenticationContext authenticationContext;
    private final TranslationManager translationManager;
    private OfBizDelegator ofBizDelegator;
    private final LazyLoadingCache<ConstantsCache<WorklogType>> worklogTypeCache = new LazyLoadingCache<ConstantsCache<WorklogType>>(
        new WorklogTypeCacheLoader());

    protected static final List<String> ORDER_BY_LIST = Lists.newArrayList("sequence ASC");

    public DefaultExtendedConstantsManager() {
        this.translationManager = ComponentAccessor.getTranslationManager();
        this.authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        this.ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        this.constantsManager = ComponentAccessor.getConstantsManager();
    }

    public GenericValue getConstant(String constantType, String id) {
        if ("WorklogType".equalsIgnoreCase(constantType))
            return getWorklogType(id);

        throw null;
    }

    public IssueConstant getConstantObject(String constantType, String id) {
        if ("WorklogType".equalsIgnoreCase(constantType))
            return getWorklogTypeObject(id);

        return this.constantsManager.getConstantObject(constantType, id);
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV) {
        if (issueConstantGV == null)
            return null;

        if ("WorklogType".equals(issueConstantGV.getEntityName())) {
            return getWorklogTypeObject(issueConstantGV.getString("id"));
        }
        return this.constantsManager.getIssueConstant(issueConstantGV);
    }

    public WorklogType getWorklogTypeObject(String id) {
        return (WorklogType) ((ConstantsCache<WorklogType>) this.worklogTypeCache.getData()).getObject(id);
    }

    public Collection<GenericValue> getWorklogTypes() {
        return ((ConstantsCache<WorklogType>) this.worklogTypeCache.getData()).getGenericValues();
    }

    public Collection<WorklogType> getWorklogTypeObjects() {
        return ((ConstantsCache<WorklogType>) this.worklogTypeCache.getData()).getObjects();
    }

    public GenericValue getWorklogType(String id) {
        return getConstant((List<GenericValue>) getWorklogTypes(), id);
    }

    public synchronized void refreshWorklogTypes() {
        this.worklogTypeCache.reload();
    }

    protected GenericValue getConstant(List<GenericValue> constants, String id) {
        if (id == null)
            return null;

        return EntityUtil.getOnly(EntityUtil.filterByAnd(constants, MapBuilder.build("id", id)));
    }

    protected List<GenericValue> getConstants(String type) {
        return getConstantsWithSort(type, ORDER_BY_LIST);
    }

    protected List<GenericValue> getConstantsWithSort(String type, List<String> sortList) {
//		try such an exaption is better to throw outside
//		{
        List<GenericValue> gvs = this.ofBizDelegator.findAll(type, sortList);
        if (gvs == null)
            gvs = Collections.emptyList();

        return Collections.unmodifiableList(Collections.synchronizedList(gvs));
//		}
//		catch (Exception e)
//		{
//			log.error("Error getting constants of type: " + type + " : " + e, e);
//			return Collections.emptyList();
//		}
    }

    protected ConstantsCache<? extends IssueConstant> getConstantsCache(String constantType) {
        if ("WorklogType".equalsIgnoreCase(constantType))
            return (ConstantsCache<WorklogType>) this.worklogTypeCache.getData();

        throw new IllegalArgumentException("Unknown constant type '" + constantType + "'.");
    }

    public IssueConstant getIssueConstantByName(String constantType, String name) {
        ConstantsCache<?> constantsCache = getConstantsCache(constantType);

        for (IssueConstant issueConstant : constantsCache.getObjects()) {
            if (StringUtils.equals(name, issueConstant.getName()))
                return issueConstant;
        }

        return null;
    }

    public GenericValue getConstantByName(String constantType, String name) {
        return convertToGenericValue(getIssueConstantByName(constantType, name));
    }

    private GenericValue convertToGenericValue(IssueConstant issueConstant) {
        if (issueConstant == null)
            return null;

        return null;// !!!return issueConstant.getGenericValue();
    }

    private class WorklogTypeCacheLoader
        implements LazyLoadingCache.CacheLoader<DefaultExtendedConstantsManager.ConstantsCache<WorklogType>> {
        private WorklogTypeCacheLoader() {
        }

        public DefaultExtendedConstantsManager.ConstantsCache<WorklogType> loadData() {
            List<GenericValue> worklogTypes = DefaultExtendedConstantsManager.this.getConstants("WorklogType");
            Map<String, WorklogType> priorityObjectsMap = new LinkedHashMap<String, WorklogType>();
            // haven't found other way to retrieve it
            BaseUrl baseUrl = new DefaultBaseUrl(
                new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()));
            for (GenericValue priorityGV : worklogTypes) {
                WorklogTypeImpl worklogType = new WorklogTypeImpl(priorityGV,
                    DefaultExtendedConstantsManager.this.translationManager,
                    DefaultExtendedConstantsManager.this.authenticationContext, baseUrl);
                priorityObjectsMap.put(priorityGV.getString("id"), worklogType);
            }

            return new DefaultExtendedConstantsManager.ConstantsCache<WorklogType>(worklogTypes, priorityObjectsMap);
        }
    }

    protected static class ConstantsCache<T extends IssueConstant> {
        private final List<GenericValue> gvList;
        private final Map<String, T> idObjectMap;

        public ConstantsCache(List<GenericValue> gvList, Map<String, T> idObjectMap) {
            this.gvList = gvList;
            this.idObjectMap = idObjectMap;
        }

        List<GenericValue> getGenericValues() {
            return this.gvList;
        }

        Collection<T> getObjects() {
            return this.idObjectMap.values();
        }

        T getObject(String id) {
            return (T) this.idObjectMap.get(id);
        }
    }
}
