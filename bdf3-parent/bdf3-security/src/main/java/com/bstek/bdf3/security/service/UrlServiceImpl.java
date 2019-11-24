package com.bstek.bdf3.security.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.malagu.linq.JpaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.bstek.bdf3.security.decision.manager.SecurityDecisionManager;
import com.bstek.bdf3.security.orm.Permission;
import com.bstek.bdf3.security.orm.Url;
import com.bstek.bdf3.security.orm.User;

/**
 * @author Kevin Yang (mailto:kevin.yang@bstek.com)
 * @since 2016年1月30日
 */
@Service
@Transactional(readOnly = true)
public class UrlServiceImpl implements UrlService {
	@Autowired
	private UrlServiceCache urlServiceCache;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SecurityDecisionManager securityDecisionManager;
	
	@Autowired
	private GrantedAuthorityService grantedAuthorityService;
	
	@Override
	public List<Url> findAll() {
		List<Url> urls = JpaUtil.linq(Url.class).list();
		List<Permission> permissions = JpaUtil.linq(Permission.class).equal("resourceType", Url.RESOURCE_TYPE).list();
		if (!permissions.isEmpty()) {
			Map<String, Url> urlMap = JpaUtil.index(urls);
			for (Permission permission : permissions) {
				Url url = urlMap.get(permission.getResourceId());
				List<ConfigAttribute> configAttributes = url.getAttributes();
				if (configAttributes == null) {
					configAttributes = new ArrayList<ConfigAttribute>();
					url.setAttributes(configAttributes);
				}
				configAttributes.add(permission);
			}
		}
		return urls;
	}
	
	public List<Url> findTree() {
		return urlServiceCache.findTree();
	}
	
	@Override
	public List<Url> findTreeByUsername(String username) {
		List<Url> urls = urlServiceCache.findTree();
		List<Url> result = new ArrayList<Url>();
		rebuildLoginUserGrantedAuthorities();
		for (Url url : urls) {
			Url copy = copyFor(url);
			if (decide(username, copy, url, userService.isAdministrator())) {
				result.add(copy);
			}
		}
		return result;
	}
	
	protected Url copyFor(Url url) {
		Url u = new Url();
		u.setId(url.getId());
		u.setIcon(url.getIcon());
		u.setDescription(url.getDescription());
		u.setName(url.getName());
		u.setNavigable(url.isNavigable());
		u.setOrder(url.getOrder());
		u.setParentId(url.getParentId());
		u.setPath(url.getPath());
		return u;
	}
	
	@Override
	public List<Url> getAccessibleUrlsByUsername(String username) {
		List<Url> urls = urlServiceCache.findTree();
		List<Url> result = new ArrayList<Url>();
		for (Url url : urls) {
			Url copy = copyFor(url);
			if (decide(username, copy, url, userService.isAdministrator(username))) {
				result.add(copy);
			}
		}
		return result;
	}
		
	private void rebuildLoginUserGrantedAuthorities() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		user.setAuthorities(grantedAuthorityService.getGrantedAuthorities(user));
	}

	private boolean decide(String username, Url copy, Url url, boolean administrator) {
		if (!url.isNavigable()) {
			return false;
		}
		if (administrator || securityDecisionManager.decide(username, url)) {
			List<Url> children = url.getChildren();
			List<Url> newChildren = new ArrayList<Url>();
			copy.setChildren(newChildren);
			if (!CollectionUtils.isEmpty(children)) {
				for (Url child : children) {
					Url copyChild = copyFor(child);
					if (decide(username, copyChild, child, administrator)) {
						newChildren.add(copyChild);
					}
				}
			}
			return true;
		}
		return false;
		
		
	}


}
