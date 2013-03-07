package org.jboss.forge.projects;

import org.jboss.forge.container.services.Exported;
import org.jboss.forge.resource.DirectoryResource;

/**
 * Locates project root directories, and creates instances of projects for that type.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Exported
public interface ProjectLocator
{
   /**
    * Create a new or existing {@link Project} with the given {@link DirectoryResource} as
    * {@link Project#getProjectRoot()}.
    */
   public Project createProject(DirectoryResource targetDir);

   /**
    * Returns true if the given {@link DirectoryResource} contains an existing {@link Project}.
    */
   public boolean containsProject(DirectoryResource resource);
}