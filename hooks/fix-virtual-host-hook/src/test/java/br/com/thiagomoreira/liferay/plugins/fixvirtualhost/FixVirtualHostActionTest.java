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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thiago Moreira
 */
public class FixVirtualHostActionTest {

	@Test
	public void testFirstPass() {
		FixVirtualHostAction action = new FixVirtualHostAction();
		Map<String, String> mapping = new HashMap<>();

		mapping.put("www", "dev-www");
		mapping.put("staging", "dev-staging");

		String oldVirtualVost = "staging.liferay.com";

		String newVirtualVost = action.fixVirtualHost(oldVirtualVost, mapping);

		Assert.assertEquals("dev-staging.liferay.com", newVirtualVost);
	}

	@Test
	public void testSecondPass() {
		FixVirtualHostAction action = new FixVirtualHostAction();
		Map<String, String> mapping = new HashMap<>();

		mapping.put("www", "dev-www");
		mapping.put("staging", "dev-staging");

		String oldVirtualVost = "dev-staging.liferay.com";

		String newVirtualVost = action.fixVirtualHost(oldVirtualVost, mapping);

		Assert.assertEquals(null, newVirtualVost);
	}

}