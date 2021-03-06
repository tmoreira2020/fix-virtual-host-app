/**
 * Copyright © 2015 Thiago Moreira (tmoreira2020@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.thiagomoreira.liferay.plugins.fixvirtualhost;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.osgi.service.component.annotations.Component;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.events.LifecycleEvent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Thiago Moreira
 */
@Component(immediate = true, property = {"key=application.startup.events"}, service = LifecycleAction.class)
public class FixVirtualHostAction implements LifecycleAction {

	private static Log log = LogFactoryUtil.getLog(FixVirtualHostAction.class);

	@Override
	public void processLifecycleEvent(LifecycleEvent lifecycleEvent)
			throws ActionException {
		try {
			String[] companyIds = lifecycleEvent.getIds();
			for (String companyId : companyIds) {
				doRun(GetterUtil.getLong(companyId));
			}
		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

	private void doRun(final long companyId) throws Exception {
		String[] temp = StringUtil.split(PropsUtil.get("virtual.host.mapping"));

		final Map<String, String> virtualHostMapping = new HashMap<String, String>();

		for (String mapping : temp) {
			Properties properties = PropertiesUtil.load(mapping);
			virtualHostMapping.putAll((Map) properties);
		}

		for (String virtualHostNewPrefix : virtualHostMapping.keySet()) {

			final String virtualHostOldPrefix = virtualHostMapping
					.get(virtualHostNewPrefix);

			if (Validator.isNotNull(virtualHostOldPrefix)
					&& !virtualHostOldPrefix.equals(virtualHostNewPrefix)) {
				Company company = CompanyLocalServiceUtil.getCompany(companyId);

				String companyVirtualHost = company.getVirtualHostname();
				companyVirtualHost = fixVirtualHost(companyVirtualHost,
						virtualHostMapping);

				if (Validator.isNotNull(companyVirtualHost)) {
					log.info("Updating company virtual host to: "
							+ companyVirtualHost);

					CompanyLocalServiceUtil.updateCompany(companyId,
							companyVirtualHost, company.getMx(),
							company.getMaxUsers(), company.getActive());
				}

				ActionableDynamicQuery actionableDynamicQuery = GroupLocalServiceUtil
						.getActionableDynamicQuery();

				actionableDynamicQuery
						.setAddCriteriaMethod(new ActionableDynamicQuery.AddCriteriaMethod() {

							@Override
							public void addCriteria(DynamicQuery dynamicQuery) {
								Property property = PropertyFactoryUtil
										.forName("site");

								dynamicQuery.add(property.eq(true));
							}

						});

				actionableDynamicQuery
						.setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod<Group>() {

							@Override
							public void performAction(Group group)
									throws PortalException {

								LayoutSet layoutSet = group
										.getPublicLayoutSet();

								if (layoutSet != null) {
									String layoutVirtualHost = layoutSet
											.getVirtualHostname();
									layoutVirtualHost = fixVirtualHost(
											layoutVirtualHost,
											virtualHostMapping);

									if (Validator.isNotNull(layoutVirtualHost)) {
										log.info("Updating layout virtual host to: "
												+ layoutVirtualHost);

										LayoutSetLocalServiceUtil
												.updateVirtualHost(
														group.getGroupId(),
														false,
														layoutVirtualHost);
									}
								}

								layoutSet = group.getPrivateLayoutSet();

								if (layoutSet != null) {
									String layoutVirtualHost = layoutSet
											.getVirtualHostname();
									layoutVirtualHost = fixVirtualHost(
											layoutVirtualHost,
											virtualHostMapping);

									if (Validator.isNotNull(layoutVirtualHost)) {
										log.info("Updating layout virtual host to: "
												+ layoutVirtualHost);

										LayoutSetLocalServiceUtil.updateVirtualHost(
												group.getGroupId(), true,
												layoutVirtualHost);
									}
								}
							}

						});

				actionableDynamicQuery.setCompanyId(companyId);
				actionableDynamicQuery.performActions();
			} else {
				log.info("Skipping executing due properties values virtual.host.old.prefix="
						+ virtualHostOldPrefix
						+ " and virtual.host.new.prefix="
						+ virtualHostNewPrefix);
			}
		}
	}

	protected String fixVirtualHost(String virtualHost,
			Map<String, String> virtualHostMapping) {

		if (Validator.isNotNull(virtualHost)) {
			int endIndex = virtualHost.indexOf(StringPool.PERIOD);
			String currentPrefix = virtualHost.substring(0, endIndex);
			String virtualHostNewPrefix = virtualHostMapping.get(currentPrefix);

			if (Validator.isNotNull(virtualHostNewPrefix)) {
				return virtualHost.replaceFirst(currentPrefix,
						virtualHostNewPrefix);
			} else {
				if (!virtualHostMapping.containsValue(currentPrefix)) {
					String defaultPrefix = PropsUtil
							.get("virtual.host.default.prefix");

					if (!virtualHost.startsWith(defaultPrefix)) {
						return defaultPrefix + StringPool.PERIOD + virtualHost;
					}
				}
			}
		}
		return null;
	}

}