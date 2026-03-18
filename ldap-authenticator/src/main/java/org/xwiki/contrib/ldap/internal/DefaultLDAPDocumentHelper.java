/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.ldap.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.contrib.usercommon.formatter.UserFormatterFactory;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.ldap.LDAPDocumentHelper;
import org.xwiki.contrib.ldap.XWikiLDAPConfig;
import org.xwiki.contrib.ldap.XWikiLDAPSearchAttribute;

import static org.xwiki.contrib.ldap.XWikiLDAPUtils.cleanXWikiUserPageName;

/**
 * Default implementation of {@link LDAPDocumentHelper}.
 *
 * @version $Id$
 * @since 9.10
 */
@Component
@Singleton
public class DefaultLDAPDocumentHelper implements LDAPDocumentHelper
{
    @Inject
    private Logger logger;

    @Inject
    private UserFormatterFactory userFormatterFactory;

    @Override
    public String getDocumentName(String documentNameFormat, String uidAttributeName,
        List<XWikiLDAPSearchAttribute> attributes, XWikiLDAPConfig config)
    {
        Map<String, String> memoryConfiguration = config.getMemoryConfiguration();
        Map<String, String> valueMap = new HashMap<>();
        if (attributes != null) {
            // Complete existing configuration
            valueMap.putAll(memoryConfiguration);

            // Inject attributes
            for (XWikiLDAPSearchAttribute attribute : attributes) {
                valueMap.put("ldap." + attribute.name, attribute.value);
                if (attribute.name.equals(uidAttributeName)) {
                    // Override the default uid value with the real one coming from LDAP
                    valueMap.put("uid", attribute.value);
                }
            }
        }

        this.logger.debug("User page name format: {}", documentNameFormat);
        this.logger.debug("User page name substitution map: {}", valueMap);

        String documentName = userFormatterFactory.create(valueMap).format(documentNameFormat);

        logger.debug("User page name : [{}]", documentName);

        // Do the minimal needed cleanup anyway, even if it is not requested.
        documentName = cleanXWikiUserPageName(documentName);

        logger.debug("Cleaned user page name : [{}]", documentName);

        return documentName;
    }
}
