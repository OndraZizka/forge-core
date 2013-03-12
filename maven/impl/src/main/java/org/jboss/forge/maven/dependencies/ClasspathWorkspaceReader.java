/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.maven.dependencies;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.forge.parser.xml.Node;
import org.jboss.forge.parser.xml.XMLParser;
import org.jboss.forge.parser.xml.XMLParserException;
import org.jboss.shrinkwrap.resolver.impl.maven.util.Validate;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

/**
 * {@link WorkspaceReader} implementation capable of reading from the ClassPath
 * 
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:mmatloka@gmail.com">Michal Matloka</a>
 */
public class ClasspathWorkspaceReader implements WorkspaceReader
{
   private static final Logger log = Logger.getLogger(ClasspathWorkspaceReader.class.getName());

   /**
    * class path entry
    */
   private static final String CLASS_PATH_KEY = "java.class.path";

   /**
    * surefire cannot modify class path for test execution, so it have to store it in a different variable
    */
   private static final String SUREFIRE_CLASS_PATH_KEY = "surefire.test.class.path";

   /**
    * Contains File object and retrieved cached isFile and isDirectory values
    */
   private static final class FileInfo
   {
      private final File file;
      private final boolean isFile;
      private final boolean isDirectory;

      private FileInfo(final File file, final boolean isFile, final boolean isDirectory)
      {
         this.file = file;
         this.isFile = isFile;
         this.isDirectory = isDirectory;
      }

      private FileInfo(final File file)
      {
         this(file, file.isFile(), file.isDirectory());
      }

      private FileInfo(final String classpathEntry)
      {
         this(new File(classpathEntry));
      }

      private File getFile()
      {
         return file;
      }

      private boolean isFile()
      {
         return isFile;
      }

      private boolean isDirectory()
      {
         return isDirectory;
      }
   }

   private final Set<String> classPathEntries = new LinkedHashSet<String>();

   /**
    * Cache classpath File objects and retrieved isFile isDirectory values. Key is a classpath entry
    * 
    * @see #getClasspathFileInfo(String)
    */
   private final Map<String, FileInfo> classpathFileInfoCache = new HashMap<String, FileInfo>();

   /**
    * Cache pom File objects and retrieved isFile isDirectory values. Key - child File
    * 
    * @see #getPomFileInfo(java.io.File)
    */
   private final Map<File, FileInfo> pomFileInfoCache = new HashMap<File, FileInfo>();

   /**
    * Cache Found in classpath artifacts. Key is a pom file.
    * 
    * @see #getFoundArtifact(java.io.File)
    */
   private final Map<File, Artifact> foundArtifactCache = new HashMap<File, Artifact>();

   public ClasspathWorkspaceReader()
   {
      final String classPath = System.getProperty(CLASS_PATH_KEY);
      final String surefireClassPath = System.getProperty(SUREFIRE_CLASS_PATH_KEY);

      this.classPathEntries.addAll(getClassPathEntries(surefireClassPath));
      this.classPathEntries.addAll(getClassPathEntries(classPath));
   }

   @Override
   public WorkspaceRepository getRepository()
   {
      return new WorkspaceRepository("classpath");
   }

   @Override
   public File findArtifact(final Artifact artifact)
   {
      for (String classpathEntry : classPathEntries)
      {
         final FileInfo fileInfo = getClasspathFileInfo(classpathEntry);
         final File file = fileInfo.getFile();

         if (fileInfo.isDirectory())
         {
            // TODO: This is not reliable, file might have different name
            // FIXME: Surefire might user jar in the classpath instead of the target/classes
            final FileInfo pomFileInfo = getPomFileInfo(file);
            final File pomFile = pomFileInfo.getFile();
            if (pomFileInfo.isFile())
            {
               final Artifact foundArtifact = getFoundArtifact(pomFile);

               if (foundArtifact.getGroupId().equals(artifact.getGroupId())
                        && foundArtifact.getArtifactId().equals(artifact.getArtifactId())
                        && foundArtifact.getVersion().equals(artifact.getVersion()))
               {
                  if (log.isLoggable(Level.FINE))
                  {
                     log.fine("################################# Artifact: " + artifact + " File: " + pomFile);
                  }
                  if ("pom".equals(artifact.getExtension()))
                  {
                     return pomFile;
                  }
                  else
                  {
                     return new File(file.getParentFile(), "classes");
                  }
               }
            }
         }
         // this is needed for Surefire when runned as 'mvn package'
         else if (fileInfo.isFile())
         {
            final StringBuilder name = new StringBuilder(artifact.getArtifactId()).append("-").append(
                     artifact.getVersion());

            // TODO: This is nasty
            // we need to get a a pom.xml file to be sure we fetch transitive deps as well
            if (file.getAbsolutePath().contains(name.toString()))
            {
               if ("pom".equals(artifact.getExtension()))
               {
                  // try to get pom file for the project
                  final File pomFile = new File(file.getParentFile().getParentFile(), "pom.xml");
                  if (pomFile.isFile())
                  {
                     if (log.isLoggable(Level.FINE))
                     {
                        log.fine("################################# Artifact: " + artifact + " File: " + pomFile);
                     }
                     return pomFile;
                  }
               }
               // SHRINKRES-102, consider classifier as well
               if (!Validate.isNullOrEmpty(artifact.getClassifier()))
               {
                  name.append("-").append(artifact.getClassifier());
               }

               // we are looking for a non pom artifact, let's get it
               name.append(".").append(artifact.getExtension());
               if (file.getAbsolutePath().endsWith(name.toString()))
               {
                  // return raw file
                  if (log.isLoggable(Level.FINE))
                  {
                     log.fine("################################# Artifact: " + artifact + " File: " + file);
                  }
                  return file;
               }
            }
         }
      }
      return null;
   }

   @Override
   public List<String> findVersions(final Artifact artifact)
   {
      List<String> versions = new ArrayList<String>();
      for (String classpathEntry : classPathEntries)
      {
         final FileInfo fileInfo = getClasspathFileInfo(classpathEntry);
         final File file = fileInfo.getFile();

         if (fileInfo.isDirectory())
         {
            // TODO: This is not reliable, file might have different name
            // FIXME: Surefire might user jar in the classpath instead of the target/classes
            final FileInfo pomFileInfo = getPomFileInfo(file);
            final File pomFile = pomFileInfo.getFile();
            if (pomFileInfo.isFile())
            {
               final Artifact foundArtifact = getFoundArtifact(pomFile);

               if (foundArtifact.getGroupId().equals(artifact.getGroupId())
                        && foundArtifact.getArtifactId().equals(artifact.getArtifactId()))
               {
                  versions.add(foundArtifact.getVersion());
               }
            }
         }
         // this is needed for Surefire when runned as 'mvn package'
         else if (fileInfo.isFile())
         {
            // TODO: This is nasty
            // we need to get a a pom.xml file to be sure we fetch transitive deps as well
            if (file.getAbsolutePath().contains(artifact.getArtifactId()))
            {
               if ("pom".equals(artifact.getExtension()))
               {
                  // try to get pom file for the project
                  final File pomFile = new File(file.getParentFile().getParentFile(), "pom.xml");
                  if (pomFile.isFile())
                  {
                     final Artifact foundArtifact = getFoundArtifact(pomFile);

                     if (foundArtifact.getGroupId().equals(artifact.getGroupId())
                              && foundArtifact.getArtifactId().equals(artifact.getArtifactId()))
                     {
                        versions.add(foundArtifact.getVersion());
                     }
                  }
               }
            }
         }
      }
      if (log.isLoggable(Level.FINE))
      {
         log.fine("################################# Artifact: " + artifact + " Versions: " + versions);
      }
      return versions;
   }

   private Set<String> getClassPathEntries(final String classPath)
   {
      if (Validate.isNullOrEmpty(classPath))
      {
         return Collections.emptySet();
      }
      return new LinkedHashSet<String>(Arrays.asList(classPath.split(File.pathSeparator)));
   }

   private FileInfo getClasspathFileInfo(final String classpathEntry)
   {
      FileInfo classpathFileInfo = classpathFileInfoCache.get(classpathEntry);
      if (classpathFileInfo == null)
      {
         classpathFileInfo = new FileInfo(classpathEntry);
         classpathFileInfoCache.put(classpathEntry, classpathFileInfo);
      }
      return classpathFileInfo;
   }

   private FileInfo getPomFileInfo(final File childFile)
   {
      FileInfo pomFileInfo = pomFileInfoCache.get(childFile);
      if (pomFileInfo == null)
      {
         pomFileInfo = createPomFileInfo(childFile);
         pomFileInfoCache.put(childFile, pomFileInfo);
      }
      return pomFileInfo;
   }

   private FileInfo createPomFileInfo(final File childFile)
   {
      final File pomFile = new File(childFile.getParentFile().getParentFile(), "pom.xml");
      return new FileInfo(pomFile);
   }

   private Artifact getFoundArtifact(final File pomFile)
   {
      Artifact foundArtifact = foundArtifactCache.get(pomFile);
      if (foundArtifact == null)
      {
         foundArtifact = createFoundArtifact(pomFile);
         foundArtifactCache.put(pomFile, foundArtifact);
      }
      return foundArtifact;
   }

   private Artifact createFoundArtifact(final File pomFile)
   {
      try
      {
         if (log.isLoggable(Level.FINE))
         {
            log.fine("Processing " + pomFile.getAbsolutePath() + " for classpath artifact resolution");
         }

         final Node pom = loadPom(pomFile);

         String groupId = pom.getTextValueForPatternName("groupId");
         String artifactId = pom.getTextValueForPatternName("artifactId");
         String type = pom.getTextValueForPatternName("packaging");
         String version = pom.getTextValueForPatternName("version");

         if (Validate.isNullOrEmpty(groupId))
         {
            groupId = pom.getTextValueForPatternName("parent/groupId");
         }
         if (Validate.isNullOrEmpty(type))
         {
            type = "jar";
         }
         if (version == null || version.equals(""))
         {
            version = pom.getTextValueForPatternName("parent/version");
         }

         final Artifact foundArtifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + type + ":" + version);
         foundArtifact.setFile(pomFile);
         return foundArtifact;
      }
      catch (final Exception e)
      {
         throw new RuntimeException("Could not parse pom.xml: " + pomFile, e);
      }
   }

   private Node loadPom(final File pom) throws XMLParserException, FileNotFoundException
   {
      return XMLParser.parse(pom);
   }

}
