/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.templates;

import org.jboss.forge.addon.resource.Resource;

import java.io.IOException;
import java.io.Writer;

/**
 * Process a template
 *
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public interface TemplateGenerator
{
   /**
    * Returns true if the given template can be handled by this {@link TemplateGenerator}
    */
   public boolean handles(Template template);

   /**
    * Returns true if the given template can be handled by this {@link TemplateGenerator}
    *
    * @deprecated Deprecated after Forge 2.1.1. Use the {@link Template} accepting method instead.
    */
   @Deprecated
   public boolean handles(Resource<?> template);

   /**
    * Processes the template specified by the {@link Template} parameter and writes the output to the {@link Writer}
    * parameter
    */
   public void process(Object dataModel, Template template, Writer writer) throws IOException;

   /**
    * Processes the template specified by the {@link Resource} parameter and writes the output to the {@link Writer}
    * parameter
    *
    * @deprecated Deprecated after Forge 2.1.1. Use the {@link Template} accepting method instead.
    */
   @Deprecated
   public void process(Object dataModel, Resource<?> template, Writer writer) throws IOException;
}
