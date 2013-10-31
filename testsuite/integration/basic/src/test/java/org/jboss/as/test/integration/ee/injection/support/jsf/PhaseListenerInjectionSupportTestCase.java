/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.as.test.integration.ee.injection.support.jsf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.ee.injection.support.InjectionSupportTestCase;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunAsClient
@RunWith(Arquillian.class)
public class PhaseListenerInjectionSupportTestCase extends InjectionSupportTestCase {

    private final Pattern resultListItemPattern = Pattern.compile("<li>([a-zA-Z_:0-9]*)</li>");

    @Deployment
    public static WebArchive createTestArchive() {
        return createTestArchiveBase().addClasses(TestPhaseListener.class, Controller.class)
                .addAsWebResource(PhaseListenerInjectionSupportTestCase.class.getPackage(), "home.xhtml", "home.xhtml")
                .addAsWebInfResource(PhaseListenerInjectionSupportTestCase.class.getPackage(), "faces-config.xml", "faces-config.xml");
    }

    @Test
    public void testInjection() throws IOException, ExecutionException, TimeoutException {
        assertEquals("Injection:true", getResultListItem(1));
    }

    @Test
    public void testPostConstruct() throws IOException, ExecutionException, TimeoutException {
        assertEquals("PostConstruct:true", getResultListItem(2));
    }

    @Test
    public void testInterceptor() throws IOException, ExecutionException, TimeoutException {
        assertEquals("Interceptions:afterPhase,", getResultListItem(3));
    }

    private String getResultListItem(int index) throws IOException, ExecutionException, TimeoutException {
        String result = doGetRequest("/home.jsf");
        Matcher matcher = resultListItemPattern.matcher(result);
        int idx = 1;
        while (matcher.find()) {
            if (index == idx) {
                return matcher.group(1);
            }
            idx++;
        }
        return null;
    }

}
