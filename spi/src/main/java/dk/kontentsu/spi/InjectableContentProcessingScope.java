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
package dk.kontentsu.spi;

import javax.enterprise.context.Dependent;

import dk.kontentsu.spi.ContentProcessingContextManager;
import dk.kontentsu.spi.ContentProcessingTask;
import dk.kontentsu.spi.StartableContentContext;

/**
 * Execute some code in the CDI content processing scope with injected content
 *
 * @see dk.kontentsu.spi.ContentProcessingScoped
 *
 * @author Jens Borch Christiansen
 */
@Dependent
public class InjectableContentProcessingScope {

    private StartableContentContext context = ContentProcessingContextManager.getInstance().context();

    public void execute(final ContentProcessingTask task) {
        try {
            context.enter(null);
            task.run();
        } finally {
            context.exit();
        }
    }

    public void execute(final ContentProcessingTask task, final ScopedContent content) {
        try {
            context.enter(content);
            task.run();
        } finally {
            context.exit();
        }
    }

}
