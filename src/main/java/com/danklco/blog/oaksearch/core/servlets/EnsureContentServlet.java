/*
 *  Copyright 2022 - Dan Klco
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.danklco.blog.oaksearch.core.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = { Servlet.class })
@SlingServletPaths("/bin/oak-search/ensurecontent")
public class EnsureContentServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(EnsureContentServlet.class);

    private static final String PATH_TESTS = "/tests";
    private static final long serialVersionUID = 1L;
    private static final String PN_TEST_CHILD = "test:child";
    private static final String PN_TEST_NAME = "test:name";
    private static final String PN_TEST_ITERATION = "test:iteration";
    private static final String PN_TEST_ITEM = "test:item";
    private static final String NT_TEST_CONTENT = "test:content";
    private static final String TEST_NAME = "oak-search";

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        ResourceResolver admin = request.getResourceResolver();
        if (admin.getResource(PATH_TESTS) == null) {
            log.info("Performing initial setup...");
            try {
                setupNodeTypes(admin);
                admin.refresh();
                createTestContent(admin);
                setupUsersAndGroups(admin);
                log.info("Setup complete!");
            } catch (Exception e) {
                response.sendError(500, "Failed to set up " + e);
                throw new ServletException("Failed to create users and groups", e);
            }
        } else {
            log.info("No need to perform setup");
        }
    }

    private void setupNodeTypes(ResourceResolver admin) throws IOException, InterruptedException {
        log.info("Setting up node types...");
        ResourceUtil.getOrCreateResource(admin,
                "/apps/system/config/org.apache.sling.jcr.repoinit.RepositoryInitializer~oak-search.cfg.json",
                Map.of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_FILE),
                JcrConstants.NT_FOLDER, false);
        ResourceUtil.getOrCreateResource(admin,
                "/apps/system/config/org.apache.sling.jcr.repoinit.RepositoryInitializer~oak-search.cfg.json/jcr:content",
                Map.of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_RESOURCE, JcrConstants.JCR_DATA,
                        new ByteArrayInputStream(
                                "{\n  \"scripts\": [\n    \"register nodetypes\\n<<===\\n    <test='http://www.danklco.com/oak/test/1.0'>\\n    [test:content] > nt:unstructured, nt:hierarchyNode\\n      - test:name (string)\\n===>>\"\n  ]\n}\n"
                                        .getBytes())),
                JcrConstants.NT_UNSTRUCTURED, false);
        admin.commit();
        TimeUnit.SECONDS.sleep(10);
    }

    private void createTestContent(ResourceResolver admin) throws PersistenceException {
        log.info("Creating test content...");
        ResourceUtil.getOrCreateResource(admin, PATH_TESTS,
                Map.of(JcrConstants.JCR_PRIMARYTYPE, NT_TEST_CONTENT, PN_TEST_NAME, TEST_NAME),
                NT_TEST_CONTENT, false);
        for (Integer iteration : IntStream.range(1, 10).toArray()) {
            ResourceUtil.getOrCreateResource(admin,
                    String.format("/tests/it-%d", iteration),
                    Map.of(JcrConstants.JCR_PRIMARYTYPE, NT_TEST_CONTENT, PN_TEST_NAME, TEST_NAME, PN_TEST_ITERATION,
                            iteration),
                    NT_TEST_CONTENT, false);
            for (Integer item : IntStream.range(1, 100 * iteration).toArray()) {
                ResourceUtil.getOrCreateResource(admin,
                        String.format("/tests/it-%d/item-%d", iteration, item),
                        Map.of(JcrConstants.JCR_PRIMARYTYPE, NT_TEST_CONTENT, PN_TEST_NAME, TEST_NAME,
                                PN_TEST_ITERATION, iteration, PN_TEST_ITEM, item),
                        NT_TEST_CONTENT, false);
                for (Integer child : IntStream.range(1, 100 * iteration).toArray()) {
                    ResourceUtil.getOrCreateResource(admin,
                            String.format("/tests/it-%d/item-%d/child-%d", iteration, item, child),
                            Map.of(JcrConstants.JCR_PRIMARYTYPE, NT_TEST_CONTENT, PN_TEST_NAME, TEST_NAME,
                                    PN_TEST_ITERATION, iteration, PN_TEST_ITEM, item,
                                    PN_TEST_CHILD, child),
                            NT_TEST_CONTENT, false);
                }
                admin.commit();
            }
        }
    }

    private void setupUsersAndGroups(ResourceResolver admin) throws RepositoryException, PersistenceException {
        log.info("Setting up users and groups...");
        UserManager userManager = admin.adaptTo(UserManager.class);
        User user = userManager.createUser("test-limited-access-user", "test-limited-access-user");

        Group group = userManager.createGroup("test-limited-access-group");
        group.addMember(user);

        Session session = admin.adaptTo(Session.class);

        AccessControlUtils.addAccessControlEntry(session, PATH_TESTS, group.getPrincipal(), new String[] { "jcr:all" },
                false);
        AccessControlUtils.addAccessControlEntry(session, "/tests/it-9", user.getPrincipal(),
                new String[] { "jcr:all" }, true);
        admin.commit();
    }
}
