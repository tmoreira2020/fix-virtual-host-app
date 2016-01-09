/**
 * Copyright (C) 2015 Thiago Moreira (tmoreira2020@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.thiagomoreira.liferay.plugins.fixvirtualhost;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.LayoutSet;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.service.persistence.GroupActionableDynamicQuery;

/**
 * @author Thiago Moreira
 */
public class FixVirtualHostAction extends SimpleAction {

	private static Log log = LogFactoryUtil.getLog(FixVirtualHostAction.class);

	@Override
	public void run(String[] ids) throws ActionException {
		try {
			doRun(GetterUtil.getLong(ids[0]));
		} catch (Exception e) {
			throw new ActionException(e);
		}
	}

	private void doRun(final long companyId) throws Exception {
		final String virtualHostOldPrefix = PropsUtil
				.get("virtual.host.old.prefix");
		final String virtualHostNewPrefix = PropsUtil
				.get("virtual.host.new.prefix");

		if (Validator.isNotNull(virtualHostOldPrefix)
				&& !virtualHostOldPrefix.equals(virtualHostNewPrefix)) {
			Company company = CompanyLocalServiceUtil.getCompany(companyId);

			String companyVirtualHost = company.getVirtualHostname();
			companyVirtualHost = fixVirtualHost(companyVirtualHost,
					virtualHostOldPrefix, virtualHostNewPrefix);

			if (Validator.isNotNull(companyVirtualHost)) {
				log.info("Updating company virtual host to: "
						+ companyVirtualHost);

				CompanyLocalServiceUtil.updateCompany(companyId,
						companyVirtualHost, company.getMx(),
						company.getMaxUsers(), company.getActive());
			}

			GroupActionableDynamicQuery actionableDynamicQuery = new GroupActionableDynamicQuery() {

				@Override
				protected void performAction(Object object)
						throws PortalException, SystemException {
					Group group = (Group) object;

					LayoutSet layoutSet = group.getPublicLayoutSet();

					if (layoutSet != null) {
						String layoutVirtualHost = layoutSet
								.getVirtualHostname();
						layoutVirtualHost = fixVirtualHost(layoutVirtualHost,
								virtualHostOldPrefix, virtualHostNewPrefix);

						if (Validator.isNotNull(layoutVirtualHost)) {
							log.info("Updating layout virtual host to: "
									+ layoutVirtualHost);

							LayoutSetLocalServiceUtil.updateVirtualHost(
									group.getGroupId(), false,
									layoutVirtualHost);
						}
					}

					layoutSet = group.getPrivateLayoutSet();

					if (layoutSet != null) {
						String layoutVirtualHost = layoutSet
								.getVirtualHostname();
						layoutVirtualHost = fixVirtualHost(layoutVirtualHost,
								virtualHostOldPrefix, virtualHostNewPrefix);

						if (Validator.isNotNull(layoutVirtualHost)) {
							log.info("Updating layout virtual host to: "
									+ layoutVirtualHost);

							LayoutSetLocalServiceUtil
									.updateVirtualHost(group.getGroupId(),
											true, layoutVirtualHost);
						}
					}
				}

				@Override
				protected void addCriteria(DynamicQuery dynamicQuery) {
					Property property = PropertyFactoryUtil.forName("site");

					dynamicQuery.add(property.eq(true));
				}
			};

			actionableDynamicQuery.setCompanyId(companyId);
			actionableDynamicQuery.performActions();
		} else {
			log.info("Skiiping executing due properties values virtual.host.old.prefix="
					+ virtualHostOldPrefix
					+ " and virtual.host.new.prefix="
					+ virtualHostNewPrefix);
		}
	}

	private String fixVirtualHost(String virtualHost,
			String virtualHostOldPrefix, String virtualHostNewPrefix) {
		if (Validator.isNotNull(virtualHost)
				&& !virtualHost.startsWith(virtualHostNewPrefix)) {
			if (virtualHost.startsWith(virtualHostOldPrefix)) {
				return virtualHost.replaceFirst(virtualHostOldPrefix,
						virtualHostNewPrefix);
			} else {
				return virtualHostNewPrefix + StringPool.PERIOD + virtualHost;
			}
		}

		return null;
	}

}