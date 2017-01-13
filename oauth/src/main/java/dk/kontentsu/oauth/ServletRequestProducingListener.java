/*
 * The MIT License
 *
 * Copyright 2016 Jens Borch Christiansen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dk.kontentsu.oauth;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet request listener and CDI producer making the HttpServletRequest
 * injectable in a request scoped CDI bean,
 *
 * @author Jens Borch Christiansen
 */
@WebListener
public class ServletRequestProducingListener implements ServletRequestListener {

    private static final ThreadLocal<ServletRequest> SERVLET_REQUESTS = new ThreadLocal<ServletRequest>();

    @Override
    public void requestInitialized(final ServletRequestEvent event) {
        SERVLET_REQUESTS.set(event.getServletRequest());
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent event) {
        SERVLET_REQUESTS.remove();
    }

    @RequestScoped
    @Produces
    private HttpServletRequest getServletRequest() {
        ServletRequest request = SERVLET_REQUESTS.get();
        if (request != null && request instanceof HttpServletRequest) {
            return (HttpServletRequest) request;
        } else {
            throw new RuntimeException("HttpServletRequest not avaible for injection");
        }
    }
}
