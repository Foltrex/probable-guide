package com.scn.jira.wl.wltypes.service;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.mapstruct.factory.Mappers;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.scn.jira.wl.BLException;
import com.scn.jira.wl.wltypes.bl.IWLTypesManager;
import com.scn.jira.wl.wltypes.dal.WLTypeEntity;

@Path("/wltypes")
@PublicApi
public class WLTypesService {
	private final IWLTypesManager manager;
	private final GlobalPermissionManager globalPermissionManager;
	private final JiraAuthenticationContext authContext;

	@Inject
	public WLTypesService(IWLTypesManager manager,
			@ComponentImport final GlobalPermissionManager globalPermissionManager,
			@ComponentImport final JiraAuthenticationContext authContext) {
		this.manager = manager;
		this.globalPermissionManager = globalPermissionManager;
		this.authContext = authContext;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	@PublicApi
	public Response getWLTypes() throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		WLTypeEntity[] list = manager.getAllWLTypes();
		WLTypeMapper mapper = Mappers.getMapper(WLTypeMapper.class);
		List<WLTypeModel> model = mapper.WLTypeEntityToWLTypeModels(Arrays.asList(list));

		return Response.ok(model).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/byname/{name}")
	@PublicApi
	public Response getWLTypesByName(@PathParam("name") String name) throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		WLTypeEntity[] list = manager.getWLTypesByName(name);
		WLTypeMapper mapper = Mappers.getMapper(WLTypeMapper.class);
		List<WLTypeModel> model = mapper.WLTypeEntityToWLTypeModels(Arrays.asList(list));

		return Response.ok(model).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@PublicApi
	public Response getWLType(@PathParam("key") int id) throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		WLTypeEntity entity = manager.getWLTypeById(id);
		WLTypeMapper mapper = Mappers.getMapper(WLTypeMapper.class);
		WLTypeModel model = mapper.WLTypeEntityToWLTypeModel(entity);

		return Response.ok(model).build();
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	@PublicApi
	public Response createWLType(@Context HttpServletRequest request, WLTypeModel wltype)
			throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		WLTypeEntity entity;
		try {
			entity = manager.addWLType(wltype);
		} catch (BLException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		WLTypeMapper mapper = Mappers.getMapper(WLTypeMapper.class);
		WLTypeModel model = mapper.WLTypeEntityToWLTypeModel(entity);

		return Response.ok(model).build();
	}

	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/")
	@PublicApi
	public Response updateWLType(@Context HttpServletRequest request, WLTypeModel wltype)
			throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		WLTypeEntity entity;
		try {
			entity = manager.editWLType(wltype);
		} catch (BLException e) {
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		WLTypeMapper mapper = Mappers.getMapper(WLTypeMapper.class);
		WLTypeModel model = mapper.WLTypeEntityToWLTypeModel(entity);

		return Response.ok(model).build();
	}

	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@PublicApi
	public Response deleteWLType(@PathParam("id") int id) throws RemoteException, SQLException {
		if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, authContext.getLoggedInUser())) {
			return Response.status(Status.UNAUTHORIZED).build();
		}
		manager.deleteWLType(id);

		return Response.ok().build();
	}

	// ================= FOR DEV TESTING PURPOSES =================================

	@GET
	@AnonymousAllowed
	@Produces("text/plain")
	@Path("test/")
	public String TestAnonimous() {
		return "OK";
	}

	@GET
	@Produces("text/plain")
	@Path("testauth/")
	public String TestWithAuth() {
		return "OK";
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/samplemodel")
	public Response getsingleSample() {
		return Response.ok(new SampleModel("Fred", "Bloggs")).build();
	}

	@POST
	@Produces("text/plain")
	@Path("/samplemodel")
	public Response postsingleSample(@Context HttpServletRequest request, @FormParam("name") String name) {
		return Response.ok(name).build();
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/samplemodels")
	public Response getSamplesList() {
		List<SampleModel> list = new ArrayList<SampleModel>();
		list.add(new SampleModel("AAA", "BBB"));
		list.add(new SampleModel("C", "D"));
		list.add(new SampleModel("E", "F"));
		return Response.ok(list).build();
	}

}
