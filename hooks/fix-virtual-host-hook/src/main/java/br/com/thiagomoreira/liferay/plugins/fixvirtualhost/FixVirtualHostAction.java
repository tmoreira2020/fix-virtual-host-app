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

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Company;
import com.liferay.portal.service.CompanyLocalServiceUtil;

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

	private void doRun(long companyId) throws Exception {
		String liferayVirtualHost = PropsUtil.get("liferay.virtual.host");

		if (Validator.isNotNull(liferayVirtualHost)) {
			Company company = CompanyLocalServiceUtil.getCompany(companyId);

			String virtualHost = company.getVirtualHostname();
			if (!virtualHost.equals(liferayVirtualHost)) {
				log.info("Updating virtual host to: " + liferayVirtualHost);

				CompanyLocalServiceUtil.updateCompany(companyId,
						liferayVirtualHost, company.getMx(),
						company.getMaxUsers(), company.getActive());
			} else {
				log.info("System property 'liferay.virtual.host' and virtual host has the same value: "
						+ liferayVirtualHost);
			}
		} else {
			log.info("System property 'liferay.virtual.host' is empty");
		}
	}
}