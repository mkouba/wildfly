/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.test.integration.ee.injection.support.servlet;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.as.test.integration.ee.injection.support.ComponentInterceptor;

@SuppressWarnings("serial")
@WebServlet("/TestListenerServlet")
public class TestListenerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String mode = req.getParameter("mode");
        resp.setContentType("text/plain");

        if ("field".equals(mode)) {
            assertEquals("Listener field not injected", "true", req.getAttribute("field.injected").toString());
        } else if ("method".equals(mode)) {
            assertEquals("Listener setter not injected", "true", req.getAttribute("setter.injected").toString());
        } else if ("interceptorReset".equals(mode)) {
            ComponentInterceptor.resetInterceptions();
            assertEquals(0, ComponentInterceptor.getInterceptions().size());
            resp.getWriter().append("" + ComponentInterceptor.getInterceptions().size());
        } else if ("interceptorVerify".equals(mode)) {
            assertEquals("Listener invocation not intercepted", 2, ComponentInterceptor.getInterceptions().size());
            assertEquals("requestInitialized", ComponentInterceptor.getInterceptions().get(0).getMethodName());
            assertEquals("requestDestroyed", ComponentInterceptor.getInterceptions().get(1).getMethodName());
            resp.getWriter().append("" + ComponentInterceptor.getInterceptions().size());
        } else {
            resp.setStatus(404);
        }
    }

}
