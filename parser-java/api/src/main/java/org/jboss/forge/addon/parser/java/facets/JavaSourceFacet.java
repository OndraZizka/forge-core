/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.parser.java.facets;

import java.io.FileNotFoundException;
import java.util.List;

import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.JavaSource;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface JavaSourceFacet extends ProjectFacet
{

   /**
    * Return the class name for the given {@link JavaResource} whether it exists or not.
    */
   String calculateName(JavaResource resource);

   /**
    * Return the package for the given {@link JavaResource} whether it exists or not.
    */
   public String calculatePackage(JavaResource resource);

   /**
    * Return the base Java {@link Package} for this project, returned as a {@link String}
    */
   public String getBasePackage();

   /**
    * Return the base Java {@link Package} for this project, returned as a {@link DirectoryResource}
    */
   public DirectoryResource getBasePackageDirectory();

   /**
    * Get a list of {@link DirectoryResource}s this project uses to contain {@link Project} source documents (such as
    * .java files.)
    */
   public List<DirectoryResource> getSourceDirectories();

   /**
    * Get the {@link DirectoryResource} this {@link Project} uses to store package-able source documents (such as .java
    * files.)
    */
   public DirectoryResource getSourceDirectory();

   /**
    * Get the {@link DirectoryResource} this {@link Project} uses to store test-scoped source documents (such as .java
    * files.) Files in this directory will never be packaged or deployed except when running Unit Tests.
    */
   public DirectoryResource getTestSourceDirectory();

   /**
    * Create or update a Java file in the primary source directory: {@link #getSourceDirectory()} - use information in
    * the given {@link JavaSource} to determine the appropriate package; packages will be created if necessary.
    *
    * @param source The java class to create
    * @return The created or updated {@link JavaResource}
    * @throws FileNotFoundException
    */
   public JavaResource saveJavaSource(JavaSource<?> source) throws FileNotFoundException;

   /**
    * Create or update a Java file in the primary source directory: {@link #getSourceDirectory()} - use information in
    * the given {@link org.jboss.forge.roaster.model.source.JavaSource} to determine the appropriate package; packages
    * will be created if necessary.
    *
    * @param source The java class to create
    * @return The created or updated {@link JavaResource}
    * @throws FileNotFoundException
    */
   public JavaResource saveJavaSource(org.jboss.forge.roaster.model.source.JavaSource<?> source)
            throws FileNotFoundException;

   /**
    * Create or update a Java file in the primary test source directory: {@link #getTestSourceDirectory()} - use
    * information in the given {@link JavaSource} to determine the appropriate package; packages will be created if
    * necessary.
    *
    * @param source The java class to create
    * @return The created or updated {@link JavaResource}
    */
   public JavaResource saveTestJavaSource(JavaSource<?> source) throws FileNotFoundException;

   /**
    * Create or update a Java file in the primary test source directory: {@link #getTestSourceDirectory()} - use
    * information in the given {@link org.jboss.forge.roaster.model.source.JavaSource} to determine the appropriate
    * package; packages will be created if necessary.
    *
    * @param source The java class to create
    * @return The created or updated {@link JavaResource}
    */
   public JavaResource saveTestJavaSource(org.jboss.forge.roaster.model.source.JavaSource<?> source)
            throws FileNotFoundException;

   /**
    * Return the {@link JavaClass} at the given path relative to {@link #getSourceDirectory()}.
    *
    * @param relativePath The file or package path of the target Java source file.
    * @throws FileNotFoundException if the target {@link JavaResource} does not exist
    */
   public JavaResource getJavaResource(String relativePath) throws FileNotFoundException;

   /**
    * Attempt to locate and re-parse the given {@link JavaClass} from its location on disk, relative to
    * {@link #getSourceDirectory()}. The given instance will not be modified, and a new instance will be returned.
    *
    * @param javaClass The {@link JavaClass} to re-parse.
    * @throws FileNotFoundException if the target {@link JavaResource} does not exist
    */
   public JavaResource getJavaResource(org.jboss.forge.roaster.model.source.JavaSource<?> source)
            throws FileNotFoundException;

   /**
    * Attempt to locate and re-parse the given {@link JavaSource} from its location on disk, relative to
    * {@link #getSourceDirectory()}. The given instance will not be modified, and a new instance will be returned.
    *
    * @param javaClass The {@link JavaClass} to re-parse.
    * @throws FileNotFoundException if the target {@link JavaResource} does not exist
    */
   public JavaResource getJavaResource(JavaSource<?> javaClass) throws FileNotFoundException;

   /**
    * Return the {@link JavaClass} at the given path relative to {@link #getTestSourceDirectory()}.
    *
    * @param relativePath The package path of the target Java source {@link JavaResource}.
    */
   public JavaResource getTestJavaResource(String relativePath) throws FileNotFoundException;

   /**
    * Attempt to locate and re-parse the given {@link JavaClass} from its location on disk, relative to
    * {@link #getTestSourceDirectory()}. The given instance will not be modified, and a new instance will be returned.
    *
    * @param javaClass The {@link JavaClass} to re-parse.
    * @throws FileNotFoundException if the target {@link JavaResource} does not exist
    */
   public JavaResource getTestJavaResource(JavaSource<?> javaClass) throws FileNotFoundException;

   /**
    * Attempt to locate and re-parse the given {@link JavaSource} from its location on disk, relative to
    * {@link #getTestSourceDirectory()}. The given instance will not be modified, and a new instance will be returned.
    *
    * @param javaClass The {@link JavaClass} to re-parse.
    * @throws FileNotFoundException if the target {@link JavaResource} does not exist
    */
   public JavaResource getTestJavaResource(org.jboss.forge.roaster.model.source.JavaSource<?> source)
            throws FileNotFoundException;

   /**
    * Recursively loops over all the source directories and for each java file it finds, calls the visitor.
    *
    * @param visitor The {@link JavaResourceVisitor} that processes all the found java files. Cannot be null.
    */
   public void visitJavaSources(JavaResourceVisitor visitor);

   /**
    * Recursively loops over all the test source directories and for each java file it finds, calls the visitor.
    *
    * @param visitor The {@link JavaResourceVisitor} that processes all the found java files. Cannot be null.
    */
   public void visitJavaTestSources(JavaResourceVisitor visitor);

}
