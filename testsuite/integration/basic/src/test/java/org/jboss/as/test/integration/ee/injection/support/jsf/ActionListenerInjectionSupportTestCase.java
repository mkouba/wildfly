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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
public class ActionListenerInjectionSupportTestCase extends InjectionSupportTestCase {

    private final Pattern resultListItemPattern = Pattern.compile("<li>([a-zA-Z_:0-9]*)</li>");
    private final Pattern viewStatePattern = Pattern.compile("id=\".*javax.faces.ViewState.*\" value=\"([^\"]*)\"");

    @Deployment
    public static WebArchive createTestArchive() {
        return createTestArchiveBase().addClasses(TestActionListener.class, Controller.class, Dummy.class)
                .addAsWebResource(ActionListenerInjectionSupportTestCase.class.getPackage(), "action-listener.xhtml", "action-listener.xhtml")
                .addAsWebInfResource(ActionListenerInjectionSupportTestCase.class.getPackage(), "faces-config-empty.xml", "faces-config.xml");
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
        String result = doPost();
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

    private String doPost() throws ClientProtocolException, IOException {

        // The code is largely based on org.jboss.as.test.integration.jsf.beanvalidation.cdi.BeanValidationCdiIntegrationTestCase
        HttpClient client = new DefaultHttpClient();

        try {
            // Create and execute a GET request
            String jsfViewState = null;
            String requestUrl = contextPath + "action-listener.jsf";
            HttpGet getRequest = new HttpGet(requestUrl);
            HttpResponse response = client.execute(getRequest);
            try {
                String responseString = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

                // Get the JSF view state
                Matcher jsfViewMatcher = viewStatePattern.matcher(responseString);
                if (jsfViewMatcher.find()) {
                    jsfViewState = jsfViewMatcher.group(1);
                }
            } finally {
                HttpClientUtils.closeQuietly(response);
            }

            // Create and execute a POST
            HttpPost post = new HttpPost(requestUrl);

            List<NameValuePair> list = new ArrayList<NameValuePair>();
            list.add(new BasicNameValuePair("javax.faces.ViewState", jsfViewState));
            list.add(new BasicNameValuePair("ping", "ping"));
            list.add(new BasicNameValuePair("ping:buttonPing", "Ping"));

            post.setEntity(new StringEntity(URLEncodedUtils.format(list, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED));
            response = client.execute(post);

            try {
                return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            } finally {
                HttpClientUtils.closeQuietly(response);
            }
        } finally {
            HttpClientUtils.closeQuietly(client);
        }
    }

}
